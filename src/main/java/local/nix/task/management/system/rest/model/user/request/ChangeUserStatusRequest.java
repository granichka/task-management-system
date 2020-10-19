package local.nix.task.management.system.rest.model.user.request;

import local.nix.task.management.system.rest.model.user.UserStatus;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ChangeUserStatusRequest {

    @NotNull
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeUserStatusRequest that = (ChangeUserStatusRequest) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status);
    }

    @Override
    public String toString() {
        return "ChangeUserStatusRequest{" +
                "status=" + status +
                '}';
    }

}
