package local.nix.task.management.system.rest.model.task.request;

import local.nix.task.management.system.rest.model.task.TaskStatus;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class ChangeTaskStatusRequest {

    @NotNull
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeTaskStatusRequest that = (ChangeTaskStatusRequest) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return "ChangeTaskStatusRequest{" +
                "status=" + status +
                '}';
    }
}
