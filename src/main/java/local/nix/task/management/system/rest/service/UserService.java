package local.nix.task.management.system.rest.service;

import local.nix.task.management.system.rest.exception.TaskManagementSystemExceptions;
import local.nix.task.management.system.rest.model.task.Task;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.model.user.*;
import local.nix.task.management.system.rest.model.user.request.ChangeUserPasswordRequest;
import local.nix.task.management.system.rest.model.user.request.MergeUserRequest;
import local.nix.task.management.system.rest.model.user.request.SaveUserRequest;
import local.nix.task.management.system.rest.model.user.response.UserResponse;
import local.nix.task.management.system.rest.model.user.security.SecurityUser;
import local.nix.task.management.system.rest.repository.UserAuthorityRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserAuthorityRepository userAuthorityRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserAuthorityRepository userAuthorityRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAuthorityRepository = userAuthorityRepository;
    }

    @Transactional
    public UserResponse create(SaveUserRequest request) {
            validateUniqueFields(request);
            return UserResponse.fromUser(save(request, getRegularUserAuthorities()));
    }

    @Transactional
    public UserResponse createAdmin(SaveUserRequest request) {
        validateUniqueFields(request);
        return UserResponse.fromUser(save(request, getAdminAuthorities()));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::fromUserWithBasicAttributes);
    }


    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(UserResponse::fromUser);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findUserByUsername(username).map(UserResponse::fromUser);
    }

    @Transactional
    public UserResponse mergeById(long id, MergeUserRequest request) {
        User user = getUser(id);
        return UserResponse.fromUser(merge(user, request));
    }

    @Transactional
    public UserResponse mergeByUsername(String username, MergeUserRequest request) {
        User user = getUser(username);
        return UserResponse.fromUser(merge(user, request));
    }

    @Transactional
    public UserResponse changeStatusById(long id, UserStatus status) {
        User user = getUser(id);
        if (user.getStatus() != status) {
            user.setStatus(status);
            userRepository.save(user);
        }
        return UserResponse.fromUser(user);
    }

    @Transactional
    public UserResponse changePasswordById(long id, ChangeUserPasswordRequest request) {
        User user = getUser(id);
        changePassword(user, request.getOldPassword(), request.getNewPassword());
        return UserResponse.fromUser(user);
    }

    @Transactional
    public UserResponse changePasswordByUsername(String username, ChangeUserPasswordRequest request) {
        User user = getUser(username);
        changePassword(user, request.getOldPassword(), request.getNewPassword());
        return UserResponse.fromUser(user);
    }

    @Transactional
    public void deleteById(long id) {
        if (!userRepository.existsById(id)) throw TaskManagementSystemExceptions.userNotFound(id);
        userRepository.purgeById(id);
    }

    @Transactional
    public void deleteByUsername(String username) {
        if (!userRepository.existsByUsername(username)) throw TaskManagementSystemExceptions.userNotFound(username);
        userRepository.purgeByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not defined"));
        return new SecurityUser(user);
    }

    @Transactional
    public void mergeAdmins(@NotNull List<SaveUserRequest> requests) {
        if (requests.isEmpty()) return;
        Map<KnownAuthority,UserAuthority> authorities = getAdminAuthorities();
        for(SaveUserRequest request: requests) {
            String username = request.getUsername();
            String name = request.getName();
            User user = userRepository.findUserByName(name).orElseGet(() -> {
                User newUser = new User();
                newUser.setName(name);
                newUser.setCreatedAt(Instant.now());
                return newUser;
            });
            if (!username.equals(user.getUsername())) {
                if (userRepository.existsByUsername(username)) throw TaskManagementSystemExceptions.duplicateUsername(username);
                user.setUsername(username);
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.getAuthorities().putAll(authorities);
            userRepository.save(user);

        }
    }

    @Transactional
    public Page<TaskResponse> getUserTasks(String username, Pageable pageable) {
        User currentUser = getUser(username);
        Page<TaskResponse> result;
        if (currentUser.getAuthorities().keySet().contains(KnownAuthority.ROLE_ADMIN)) {
            result = convertTaskListToTaskResponsePage(currentUser.getTasks_to_review());
        } else {
            result = convertTaskListToTaskResponsePage(currentUser.getTasks_to_execute());
        }
        return result;
    }

    private Page<TaskResponse> convertTaskListToTaskResponsePage(List<Task> taskList) {
        List<TaskResponse> result = taskList.stream()
                .map(task -> TaskResponse.fromTask(task))
                .collect(Collectors.toList());
        return new PageImpl<>(result);
    }

    private Map<KnownAuthority, UserAuthority> getAdminAuthorities() {
        Set<KnownAuthority> values = UserAuthorityRepository.ADMIN_AUTHORITIES;
        List<UserAuthority> userAuthorityList = userAuthorityRepository.findByValueIn(values);
        return userAuthorityList.stream()
                .collect(Collectors.toMap(
                        UserAuthority::getValue,
                        Function.identity(),
                        (e1, e2) -> e2,
                        () -> new EnumMap<>(KnownAuthority.class)
                ));
    }

    private Map<KnownAuthority, UserAuthority> getRegularUserAuthorities() {
        UserAuthority authority = userAuthorityRepository
                .findByValue(KnownAuthority.ROLE_USER)
                .orElseThrow(() -> TaskManagementSystemExceptions.authorityNotFound(KnownAuthority.ROLE_USER.name()));
        Map<KnownAuthority, UserAuthority> authorities = new EnumMap<KnownAuthority, UserAuthority>(KnownAuthority.class);
        authorities.put(KnownAuthority.ROLE_USER, authority);
        return authorities;
    }

    private void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw TaskManagementSystemExceptions.wrongPassword();
        }
        if (newPassword.equals(oldPassword)) return;
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public Page<String> search(String keyword, Pageable pageable) {
        return userRepository.search(keyword, pageable);
    }


    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> TaskManagementSystemExceptions.userNotFound(id));
    }

    private User getUser(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> TaskManagementSystemExceptions.userNotFound(username));
    }

    private User save(SaveUserRequest request, Map<KnownAuthority, UserAuthority> authorities) {
        User user = new User();
        user.getAuthorities().putAll(authorities);
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setCreatedAt(Instant.now());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return user;
    }

    private User merge(User user, MergeUserRequest request) {
        String username = request.getUsername();
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) throw TaskManagementSystemExceptions.duplicateUsername(username);
            user.setUsername(username);
        }
        return userRepository.save(user);

    }

    private void validateUniqueFields(SaveUserRequest request) {
        String username = request.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw TaskManagementSystemExceptions.duplicateUsername(username);
        }
    }
}
