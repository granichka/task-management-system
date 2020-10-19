package local.nix.task.management.system.rest.model.user.request;

import java.util.Objects;

public class MergeUserRequest {

    private String username;

    public MergeUserRequest() {

    }

    public MergeUserRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergeUserRequest that = (MergeUserRequest) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
