package local.nix.task.management.system.rest.config.security.properties;


import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import javax.validation.constraints.NotBlank;
import java.time.Duration;

public class TaskManagementSystemJWTProperties {

    @NotBlank
    private String secret;

    @DurationMax(minutes = 30)
    @DurationMin(minutes = 1)
    private Duration accessExpireIn;

    @DurationMax(days = 7)
    @DurationMin(hours = 12)
    private Duration refreshExpireIn;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessExpireIn() {
        return accessExpireIn;
    }

    public void setAccessExpireIn(Duration accessExpireIn) {
        this.accessExpireIn = accessExpireIn;
    }

    public Duration getRefreshExpireIn() {
        return refreshExpireIn;
    }

    public void setRefreshExpireIn(Duration refreshExpireIn) {
        this.refreshExpireIn = refreshExpireIn;
    }

}
