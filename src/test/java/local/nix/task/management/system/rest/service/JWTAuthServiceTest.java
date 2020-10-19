package local.nix.task.management.system.rest.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import local.nix.task.management.system.rest.config.security.properties.TaskManagementSystemJWTProperties;
import local.nix.task.management.system.rest.config.security.properties.TaskManagementSystemSecurityProperties;
import local.nix.task.management.system.rest.exception.auth.InvalidRefreshTokenException;
import local.nix.task.management.system.rest.model.task.Task;
import local.nix.task.management.system.rest.model.user.KnownAuthority;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.model.user.UserAuthority;
import local.nix.task.management.system.rest.model.user.UserStatus;
import local.nix.task.management.system.rest.model.user.security.SecurityUser;
import local.nix.task.management.system.rest.model.user.security.response.AccessTokenResponse;
import local.nix.task.management.system.rest.model.user.security.token.RefreshToken;
import local.nix.task.management.system.rest.repository.RefreshTokenRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

public class JWTAuthServiceTest {

    private TaskManagementSystemJWTProperties jwtProperties;

    private Duration expireIn;

    private Duration refreshExpireIn;

    private RefreshTokenRepository refreshTokenRepository;

    private UserRepository userRepository;

    private JWTAuthService authService;

    private Algorithm algorithm;

    @BeforeEach
    void setUp() {
            jwtProperties = new TaskManagementSystemJWTProperties();
            expireIn = Duration.ofMinutes(10);
            jwtProperties.setAccessExpireIn(expireIn);
            refreshExpireIn = Duration.ofDays(3);
            jwtProperties.setRefreshExpireIn(refreshExpireIn);
            String secret = "eitu9aichae7eitee9XiciweishohW3pieshaifasosai5xie9Oomobulohyu8ie";
            jwtProperties.setSecret(secret);
            algorithm = Algorithm.HMAC512(secret.getBytes());
            TaskManagementSystemSecurityProperties securityProperties = new TaskManagementSystemSecurityProperties();
            securityProperties.setJwt(jwtProperties);
            refreshTokenRepository = mock(RefreshTokenRepository.class);
            userRepository = mock(UserRepository.class);
            authService = new JWTAuthService(securityProperties,
                    refreshTokenRepository,
                    userRepository);


    }


    @Test
    void getTokenMethodTest() {
        User user = new User();
        String userName = "User for token test";
        user.setName(userName);
        user.setUsername("token_test_user");
        user.setPassword("test");
        user.setStatus(UserStatus.ACTIVE);

        Map<KnownAuthority, UserAuthority> authorities = new EnumMap<KnownAuthority, UserAuthority>(KnownAuthority.class);
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setValue(KnownAuthority.ROLE_USER);
        authorities.put(KnownAuthority.ROLE_USER, userAuthority);
        user.setAuthorities(authorities);
        userAuthority.setUsers(Set.of(user));


        SecurityUser securityUser = new SecurityUser(user);

        when(refreshTokenRepository.save(notNull())).thenAnswer(invocation -> {
            RefreshToken entity = invocation.getArgument(0);
            assertThat(entity.getUser().getName()).isEqualTo(userName);
            entity.setValue(UUID.randomUUID());
            return entity;
        });

        AccessTokenResponse response = authService.getToken(securityUser);
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        assertThat(response.getExpireIn()).isEqualTo(expireIn.toSeconds());

        assertThatCode(() -> JWT.require(algorithm)
                .build()
                .verify(response.getAccessToken()))
                .doesNotThrowAnyException();


    }

    @Test
    void refreshTokenMethodTest() throws InvalidRefreshTokenException {
        User user = new User();
        user.setName("User for token test");
        user.setUsername("token_test_user");
        user.setPassword("test");
        user.setStatus(UserStatus.ACTIVE);

        RefreshToken refreshToken = new RefreshToken();
        OffsetDateTime now = OffsetDateTime.now();
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(now.plus(refreshExpireIn));
        refreshToken.setUser(user);
        UUID value = UUID.randomUUID();
        refreshToken.setValue(value);

        String signedRefreshToken = JWT.create()
                .withSubject(refreshToken.getUser().getUsername())
                .withJWTId(refreshToken.getValue().toString())
                .withIssuedAt(Date.from(refreshToken.getIssuedAt().toInstant()))
                .withExpiresAt(Date.from(refreshToken.getExpireAt().toInstant()))
                .sign(algorithm);

        when(refreshTokenRepository.findIfValid(notNull(), notNull(), notNull())).thenReturn(Optional.of(refreshToken));

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setIssuedAt(now);
        newRefreshToken.setExpireAt(now.plus(refreshExpireIn));
        newRefreshToken.setUser(user);
        UUID value1 = UUID.randomUUID();
        newRefreshToken.setValue(value1);

        when(refreshTokenRepository.save(notNull())).thenReturn(newRefreshToken);

        authService.refreshToken(signedRefreshToken);
        verify(refreshTokenRepository).updateChain(refreshToken, newRefreshToken);

        refreshToken.setNext(new RefreshToken());
        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> authService.refreshToken(signedRefreshToken));

    }

    @Test
    void invalidateTokenMethodTest() throws InvalidRefreshTokenException {

        User user = new User();
        user.setName("User for token test");
        user.setUsername("token_test_user");
        user.setPassword("test");
        user.setStatus(UserStatus.ACTIVE);

        RefreshToken refreshToken = new RefreshToken();
        OffsetDateTime now = OffsetDateTime.now();
        refreshToken.setIssuedAt(now);
        refreshToken.setExpireAt(now.plus(refreshExpireIn));
        refreshToken.setUser(user);
        UUID value = UUID.randomUUID();
        refreshToken.setValue(value);

        String signedRefreshToken = JWT.create()
                .withSubject(refreshToken.getUser().getUsername())
                .withJWTId(refreshToken.getValue().toString())
                .withIssuedAt(Date.from(refreshToken.getIssuedAt().toInstant()))
                .withExpiresAt(Date.from(refreshToken.getExpireAt().toInstant()))
                .sign(algorithm);

        when(refreshTokenRepository.findById(notNull())).thenReturn(Optional.of(refreshToken));

        String validUsername = "token_test_user";
        authService.invalidateToken(signedRefreshToken, validUsername);
        verify(refreshTokenRepository).findById(value);
        verify(refreshTokenRepository).deleteChain(refreshToken);

        String invalidUsername = "token_test_invalid_user";
        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> authService.invalidateToken(signedRefreshToken, invalidUsername));

        refreshToken.setNext(new RefreshToken());
        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> authService.invalidateToken(signedRefreshToken, validUsername));

    }
}
