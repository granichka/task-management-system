package local.nix.task.management.system.rest.model.user.security;

import local.nix.task.management.system.rest.model.user.UserStatus;
import org.springframework.security.core.userdetails.User;
import java.util.EnumSet;

public class SecurityUser extends User {

    private final local.nix.task.management.system.rest.model.user.User source;

    public SecurityUser(local.nix.task.management.system.rest.model.user.User source) {
        super(source.getUsername(),
                source.getPassword(),
                source.getStatus() == UserStatus.ACTIVE,
                true,
                true,
                true,
                EnumSet.copyOf(source.getAuthorities().keySet())
        );
        this.source = source;
    }

    public local.nix.task.management.system.rest.model.user.User getSource() {
        return source;
    }
}
