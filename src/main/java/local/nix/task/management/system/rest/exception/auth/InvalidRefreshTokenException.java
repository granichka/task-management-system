package local.nix.task.management.system.rest.exception.auth;

public class InvalidRefreshTokenException extends Exception {
    public InvalidRefreshTokenException() {
        super();
    }

    public InvalidRefreshTokenException(Throwable cause) {
        super(cause);
    }
}

