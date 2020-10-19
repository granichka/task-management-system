package local.nix.task.management.system.rest.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import local.nix.task.management.system.rest.Routes;
import local.nix.task.management.system.rest.config.security.filters.JWTAuthenticationFilter;
import local.nix.task.management.system.rest.config.security.filters.JWTAuthorizationFilter;
import local.nix.task.management.system.rest.config.security.properties.TaskManagementSystemSecurityProperties;
import local.nix.task.management.system.rest.model.user.request.SaveUserRequest;
import local.nix.task.management.system.rest.service.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(TaskManagementSystemSecurityProperties.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TaskManagementSystemSecurityProperties securityProperties;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper;

    public SecurityConfig(TaskManagementSystemSecurityProperties securityProperties, UserService userService,
                          PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        setupDefaultAdmins();
    }

    private void setupDefaultAdmins() {
        List<SaveUserRequest> requests = securityProperties.getAdmins().entrySet().stream()
                .map(entry -> new SaveUserRequest(
                        entry.getValue().getUsername(),
                        entry.getValue().getPassword(),
                        entry.getValue().getName()
                )).collect(Collectors.toList());
        userService.mergeAdmins(requests);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**").permitAll()
                .antMatchers(HttpMethod.POST, Routes.USERS, Routes.TOKEN + "/refresh").permitAll()
                .antMatchers(HttpMethod.POST, Routes.USERS + "/admins").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, Routes.TASKS + "/{id:\\d+}/take").hasRole("USER")
                .antMatchers(HttpMethod.DELETE,Routes.TASKS + "/{id:\\d+}").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH,Routes.TASKS + "/{id:\\d+}").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .addFilter(jwtAuthenticationFilter())
                .addFilter(jwtAuthorizationFilter())
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private JWTAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        JWTAuthenticationFilter filter = new JWTAuthenticationFilter(authenticationManager(), objectMapper);
        filter.setFilterProcessesUrl(Routes.TOKEN);
        return filter;
    }

    private JWTAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JWTAuthorizationFilter(authenticationManager(), securityProperties.getJwt());
    }


}
