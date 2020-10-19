package local.nix.task.management.system.rest.service;

import local.nix.task.management.system.rest.model.user.KnownAuthority;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.model.user.UserAuthority;
import local.nix.task.management.system.rest.model.user.UserStatus;
import local.nix.task.management.system.rest.model.user.request.SaveUserRequest;
import local.nix.task.management.system.rest.model.user.response.UserResponse;
import local.nix.task.management.system.rest.repository.UserAuthorityRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserService userService;

    private UserRepository userRepository;

    private UserAuthorityRepository userAuthorityRepository;

    private PasswordEncoder passwordEncoder;

    private UserAuthority userAuthority;

    private UserAuthority adminAuthority;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userAuthorityRepository = mock(UserAuthorityRepository.class);
        passwordEncoder = new BCryptPasswordEncoder(12, new SecureRandom());
        userService = new UserService(userRepository, passwordEncoder, userAuthorityRepository);

        userAuthority = new UserAuthority();
        userAuthority.setValue(KnownAuthority.ROLE_USER);
        userAuthority.setId(1);

        adminAuthority = new UserAuthority();
        adminAuthority.setValue(KnownAuthority.ROLE_ADMIN);
        adminAuthority.setId(2);
    }


    @Test
    void createMethodTest() {

        SaveUserRequest request = new SaveUserRequest();
        request.setName("Test User");
        request.setPassword("test");
        request.setUsername("test_user");
        Long futureUserId = 1l;

        KnownAuthority authority = KnownAuthority.ROLE_USER;


        createMethodsTest(request, authority);
        UserResponse response = userService.create(request);
        assertThat(response.getId()).isEqualTo(futureUserId);
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getAuthorities()).contains(authority);


    }

    @Test
    void createAdminMethodTest() {

        SaveUserRequest request = new SaveUserRequest();
        request.setName("Test User");
        request.setPassword("test");
        request.setUsername("test_user");
        Long futureUserId = 1l;

        KnownAuthority authority = KnownAuthority.ROLE_ADMIN;

        createMethodsTest(request, authority);
        UserResponse response = userService.createAdmin(request);
        assertThat(response.getId()).isEqualTo(futureUserId);
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getUsername()).isEqualTo(request.getUsername());
        assertThat(response.getAuthorities()).contains(authority);

    }

    @Test
    void findByIdMethodTest() {
        long validId = 1l;
        long invalidId = 2l;
        User user = new User();
        user.setId(validId);
        user.setName("Test User");
        user.setStatus(UserStatus.ACTIVE);
        user.setUsername("test_user");
        user.setPassword(passwordEncoder.encode("test"));
        user.setCreatedAt(Instant.now());
        Map<KnownAuthority, UserAuthority> authorities = new HashMap<>();
        authorities.put(KnownAuthority.ROLE_USER, userAuthority);
        authorities.put(KnownAuthority.ROLE_ADMIN, adminAuthority);
        user.setAuthorities(authorities);

        when(userRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(userRepository.findById(validId)).thenReturn(Optional.of(user));

        Optional<UserResponse> responseWithInvalidId = userService.findById(invalidId);

        assertThat(responseWithInvalidId).isEmpty();
        verify(userRepository).findById(invalidId);

        Optional<UserResponse> responseWithValidId = userService.findById(validId);

        assertThat(responseWithValidId).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(userRepository).findById(validId);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findByUsernameMethodTest() {
        String validUsername = "test_user";
        String invalidUsername = "invalid_test_user";
        User user = new User();
        user.setId(1l);
        user.setName(validUsername);
        user.setStatus(UserStatus.ACTIVE);
        user.setUsername("test_user");
        user.setPassword(passwordEncoder.encode("test"));
        user.setCreatedAt(Instant.now());
        Map<KnownAuthority, UserAuthority> authorities = new HashMap<>();
        authorities.put(KnownAuthority.ROLE_USER, userAuthority);
        authorities.put(KnownAuthority.ROLE_ADMIN, adminAuthority);
        user.setAuthorities(authorities);

        when(userRepository.findUserByUsername(invalidUsername)).thenReturn(Optional.empty());
        when(userRepository.findUserByUsername(validUsername)).thenReturn(Optional.of(user));

        Optional<UserResponse> responseWithInvalidUsername = userService.findByUsername(invalidUsername);

        assertThat(responseWithInvalidUsername).isEmpty();
        verify(userRepository).findUserByUsername(invalidUsername);

        Optional<UserResponse> responseWithValidUsername = userService.findByUsername(validUsername);

        assertThat(responseWithValidUsername).hasValueSatisfying(userResponse ->
                assertUserMatchesResponse(user, userResponse));
        verify(userRepository).findUserByUsername(validUsername);

        verifyNoMoreInteractions(userRepository);
    }

    void createMethodsTest(SaveUserRequest request, KnownAuthority authority) {

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> userService.create(request))
                .satisfies(e -> assertThat(e.getStatus()).isSameAs(HttpStatus.BAD_REQUEST));

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userAuthorityRepository.findByValue(authority)).thenReturn(Optional.of(userAuthority));
        when(userAuthorityRepository.findByValueIn(UserAuthorityRepository.ADMIN_AUTHORITIES))
                .thenReturn(List.of(userAuthority, adminAuthority));

        when(userRepository.save(notNull())).thenAnswer(invocation -> {
            User entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getName()).isEqualTo(request.getName());
            assertThat(entity.getUsername()).isEqualTo(request.getUsername());
            entity.setId(1l);
            return entity;
        });

    }

    private static void assertUserMatchesResponse(User user, UserResponse userResponse) {
        assertThat(userResponse.getId()).isEqualTo(user.getId());
        assertThat(userResponse.getName()).isEqualTo(user.getName());
        assertThat(userResponse.getUsername()).isEqualTo(user.getUsername());
        assertThat(userResponse.getUserStatus()).isEqualTo(user.getStatus());
        assertThat(userResponse.getAuthorities()).isEqualTo(user.getAuthorities().keySet());
    }

}
