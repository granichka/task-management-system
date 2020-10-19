package local.nix.task.management.system.rest.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import local.nix.task.management.system.rest.Routes;
import local.nix.task.management.system.rest.exception.TaskManagementSystemExceptions;
import local.nix.task.management.system.rest.exception.auth.InvalidRefreshTokenException;
import local.nix.task.management.system.rest.model.user.security.SecurityUser;
import local.nix.task.management.system.rest.model.user.security.request.RefreshTokenRequest;
import local.nix.task.management.system.rest.model.user.security.request.UserLoginRequest;
import local.nix.task.management.system.rest.model.user.security.response.AccessTokenResponse;
import local.nix.task.management.system.rest.service.JWTAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(Routes.TOKEN)
public class AuthController {

    private final JWTAuthService authService;

    public AuthController(JWTAuthService authService) {
        this.authService = authService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = UserLoginRequest.class)))
    public AccessTokenResponse login(@AuthenticationPrincipal SecurityUser userDetails) {
        return authService.getToken(userDetails);
    }

    @PostMapping(
            value = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public AccessTokenResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        try {
            return authService.refreshToken(request.getRefreshToken());
        } catch (InvalidRefreshTokenException e) {
            throw TaskManagementSystemExceptions.invalidRefreshToken(e);
        }
    }

    @PostMapping(value = "/invalidate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidate(@RequestBody @Valid RefreshTokenRequest request, @AuthenticationPrincipal String email) {
        try {
            authService.invalidateToken(request.getRefreshToken(), email);
        } catch (InvalidRefreshTokenException e) {
            throw TaskManagementSystemExceptions.invalidRefreshToken(e);
        }
    }

}

