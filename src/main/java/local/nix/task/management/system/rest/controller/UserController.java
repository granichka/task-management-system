package local.nix.task.management.system.rest.controller;

import io.swagger.v3.oas.annotations.Parameter;
import local.nix.task.management.system.rest.Routes;
import local.nix.task.management.system.rest.exception.TaskManagementSystemExceptions;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.model.user.request.ChangeUserPasswordRequest;
import local.nix.task.management.system.rest.model.user.request.ChangeUserStatusRequest;
import local.nix.task.management.system.rest.model.user.request.MergeUserRequest;
import local.nix.task.management.system.rest.model.user.request.SaveUserRequest;
import local.nix.task.management.system.rest.model.user.response.UserResponse;
import local.nix.task.management.system.rest.service.UserService;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(Routes.USERS)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signUp(@RequestBody @Valid SaveUserRequest request) {

        return userService.create(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal String username) {
        return userService.findByUsername(username).orElseThrow(() -> TaskManagementSystemExceptions.userNotFound(username));
    }

    @PatchMapping("/me")
    public UserResponse mergeCurrentUser(@AuthenticationPrincipal String username,
                                         @RequestBody @Valid MergeUserRequest request) {
        return userService.mergeByUsername(username, request);
    }

    @PatchMapping("/me/password")
    public UserResponse changeCurrentUserPassword(@AuthenticationPrincipal String username,
                                                  @RequestBody @Valid ChangeUserPasswordRequest request) {
        return userService.changePasswordByUsername(username, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUser(@AuthenticationPrincipal String username) {
        userService.deleteByUsername(username);
    }

    @GetMapping("/me/tasks")
    public Page<TaskResponse> getTasksByCurrentUser(
            @AuthenticationPrincipal String username,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return userService.getUserTasks(username, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable long id) {
        return userService.findById(id).orElseThrow(() -> TaskManagementSystemExceptions.userNotFound(id));
    }

    @GetMapping
    @PageableAsQueryParam
    public Page<UserResponse> listUsers(@Parameter(hidden = true) Pageable pageable) {
        return userService.list(pageable);
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerAdmin(@RequestBody @Valid SaveUserRequest request) {
        return userService.createAdmin(request);
    }


    @PatchMapping("/{id}")
    public UserResponse mergeUserById(@PathVariable long id,
                                      @RequestBody @Valid MergeUserRequest request) {
        return userService.mergeById(id, request);
    }

    @PatchMapping("/{id}/status")
    public UserResponse changeUserStatusById(@PathVariable long id,
                                             @RequestBody @Valid ChangeUserStatusRequest request) {
        return userService.changeStatusById(id, request.getStatus());
    }

    @PatchMapping("/{id}/password")
    public UserResponse changeUserPassword(@PathVariable long id,
                                           @RequestBody @Valid ChangeUserPasswordRequest request) {
        return userService.changePasswordById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable long id) {
        userService.deleteById(id);
    }

    @GetMapping("/search")
    @PageableAsQueryParam
    @ResponseBody
    public Page<String> search(HttpServletRequest request, @Parameter(hidden = true) Pageable pageable) {
        return userService.search(request.getParameter("term"), pageable);
    }

}
