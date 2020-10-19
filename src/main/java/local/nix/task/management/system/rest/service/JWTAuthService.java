package local.nix.task.management.system.rest.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import local.nix.task.management.system.rest.config.security.SecurityConstants;
import local.nix.task.management.system.rest.config.security.properties.TaskManagementSystemJWTProperties;
import local.nix.task.management.system.rest.config.security.properties.TaskManagementSystemSecurityProperties;
import local.nix.task.management.system.rest.exception.auth.InvalidRefreshTokenException;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.model.user.UserStatus;
import local.nix.task.management.system.rest.model.user.security.SecurityUser;
import local.nix.task.management.system.rest.model.user.security.response.AccessTokenResponse;
import local.nix.task.management.system.rest.model.user.security.token.RefreshToken;
import local.nix.task.management.system.rest.repository.RefreshTokenRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class JWTAuthService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final Duration jwtExpiration;

    private final Duration refreshExpiration;

    private final Algorithm algorithm;

    public JWTAuthService(TaskManagementSystemSecurityProperties securityProperties,
                          RefreshTokenRepository refreshTokenRepository,
                          UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        TaskManagementSystemJWTProperties jwtProperties = securityProperties.getJwt();
        this.jwtExpiration = jwtProperties.getAccessExpireIn();
        this.refreshExpiration = jwtProperties.getRefreshExpireIn();
        this.algorithm = Algorithm.HMAC512(jwtProperties.getSecret().getBytes());
    }

    @Transactional
    public AccessTokenResponse getToken(SecurityUser userDetails) {
        RefreshToken newToken = issueRefreshToken(userDetails.getSource());
        return response(userDetails.getUsername(), userDetails.getAuthorities(), newToken);
    }

    @Transactional
    public AccessTokenResponse refreshToken(String refreshToken)
            throws InvalidRefreshTokenException {

        RefreshToken storedToken = refreshTokenRepository.findIfValid(
                verifyRefreshToken(refreshToken),
                OffsetDateTime.now(),
                UserStatus.ACTIVE
        ).orElseThrow(InvalidRefreshTokenException::new);

        checkIfRotated(storedToken);

        User user = storedToken.getUser();

        RefreshToken nextToken = issueRefreshToken(user);

        refreshTokenRepository.updateChain(storedToken, nextToken);

        return response(user.getUsername(), user.getAuthorities().keySet(), nextToken);
    }

    @Transactional
    public void invalidateToken(String refreshToken, String ownerUsername) throws InvalidRefreshTokenException {
        UUID uuid = verifyRefreshToken(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findById(uuid)
                .orElseThrow(InvalidRefreshTokenException::new);
        checkOwner(storedToken, ownerUsername);
        checkIfRotated(storedToken);
        refreshTokenRepository.deleteChain(storedToken);
    }

    private void checkOwner(RefreshToken storedToken, String username) throws InvalidRefreshTokenException {
        User user = storedToken.getUser();
        if (!user.getUsername().equals(username)) {
            userRepository.changeStatusByUsername(username, UserStatus.SUSPENDED);
            refreshTokenRepository.deleteChain(storedToken);
            throw new InvalidRefreshTokenException();
        }
    }

    private void checkIfRotated(RefreshToken storedToken) throws InvalidRefreshTokenException {
        if (storedToken.getNext() != null) {
            refreshTokenRepository.deleteChain(storedToken.getNext());
            throw new InvalidRefreshTokenException();
        }
    }

    private RefreshToken issueRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        OffsetDateTime now = OffsetDateTime.now();
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(now.plus(refreshExpiration));
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    private AccessTokenResponse response(String subject,
                                         Collection<? extends GrantedAuthority> authorities,
                                         RefreshToken refreshToken) {
        String accessToken = issueJWT(subject, authorities);
        return new AccessTokenResponse(
                accessToken,
                signRefreshToken(refreshToken),
                jwtExpiration.toSeconds()
        );
    }

    private UUID verifyRefreshToken(String refreshJWT) throws InvalidRefreshTokenException {
        try {
            String id = JWT.require(algorithm)
                    .build()
                    .verify(refreshJWT)
                    .getId();
            Objects.requireNonNull(id, "jti must be present in refresh token");
            return UUID.fromString(id);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException(e);
        }
    }

    private String signRefreshToken(RefreshToken token) {
        return JWT.create()
                .withSubject(token.getUser().getUsername())
                .withJWTId(token.getValue().toString())
                .withIssuedAt(Date.from(token.getIssuedAt().toInstant()))
                .withExpiresAt(Date.from(token.getExpireAt().toInstant()))
                .sign(algorithm);
    }

    private String issueJWT(String subject, Collection<? extends GrantedAuthority> authorities) {
        long issuedAt = System.currentTimeMillis();
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(new Date(issuedAt))
                .withExpiresAt(new Date(issuedAt + jwtExpiration.toMillis()))
                .withArrayClaim(SecurityConstants.AUTHORITIES_CLAIM, authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .sign(algorithm);
    }



}
