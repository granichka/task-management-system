package local.nix.task.management.system.rest.model.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import local.nix.task.management.system.rest.model.user.KnownAuthority;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.model.user.UserStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String username;
    private String name;
    private UserStatus userStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<KnownAuthority> authorities;

    public static UserResponse fromUser(User user) {
        UserResponse response = fromUserWithBasicAttributes(user);
        response.authorities = EnumSet.copyOf(user.getAuthorities().keySet());
        return response;
    }

    public static UserResponse fromUserWithBasicAttributes(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.name = user.getName();
        response.userStatus = user.getStatus();
        response.createdAt = user.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<KnownAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<KnownAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponse response = (UserResponse) o;
        return Objects.equals(id, response.id) &&
                Objects.equals(username, response.username) &&
                Objects.equals(name, response.name) &&
                userStatus == response.userStatus &&
                Objects.equals(createdAt, response.createdAt) &&
                Objects.equals(authorities, response.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, name, userStatus, createdAt, authorities);
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", status=" + userStatus +
                ", createdAt=" + createdAt +
                ", authorities=" + authorities +
                '}';
    }
}
