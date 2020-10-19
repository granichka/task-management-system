package local.nix.task.management.system.rest.config.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "task-management-system.security")
public class TaskManagementSystemSecurityProperties {

    @Valid
    @NestedConfigurationProperty
    private TaskManagementSystemJWTProperties jwt;

    private Map<@NotBlank String, @Valid TaskManagementSystemAdminProperties> admins;

    public TaskManagementSystemJWTProperties getJwt() {
        return jwt;
    }

    public void setJwt(TaskManagementSystemJWTProperties jwt) {
        this.jwt = jwt;
    }

    public Map<String, TaskManagementSystemAdminProperties> getAdmins() {
        return admins;
    }

    public void setAdmins(Map<String, TaskManagementSystemAdminProperties> admins) {
        this.admins = admins;
    }
}
