package local.nix.task.management.system.rest.exception;

import local.nix.task.management.system.rest.exception.auth.InvalidRefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TaskManagementSystemExceptions {

    private TaskManagementSystemExceptions() {

    }

    public static ResponseStatusException authorityNotFound(String value) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User authority " + value + " not defined");
    }

    public static ResponseStatusException duplicateUsername(String username) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username " + username + " already taken");
    }

    public static ResponseStatusException userNotFound(String username) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with username " + username + " not found");
    }

    public static ResponseStatusException userNotFound(long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found");
    }

    public static ResponseStatusException userWithSuchNameNotFound(String name) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + name + " not found");
    }

    public static ResponseStatusException wrongPassword() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is incorrect");
    }

    public static ResponseStatusException taskNotFound(long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with id " + id + " not found");

    }

    public static ResponseStatusException invalidRefreshToken(InvalidRefreshTokenException cause) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Refresh token is invalid! It may have been rotated, invalidated or expired naturally", cause);
    }




}
