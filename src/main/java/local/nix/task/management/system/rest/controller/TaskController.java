package local.nix.task.management.system.rest.controller;

import io.swagger.v3.oas.annotations.Parameter;
import local.nix.task.management.system.rest.Routes;
import local.nix.task.management.system.rest.model.task.request.ChangeTaskStatusRequest;
import local.nix.task.management.system.rest.model.task.request.SaveTaskRequest;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(Routes.TASKS)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@RequestBody @Valid SaveTaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping
    public Page<TaskResponse> listTasks(@Parameter(hidden = true) Pageable pageable) {
        return taskService.list(pageable);
    }

    @PatchMapping("/{id}")
    public TaskResponse mergeTaskById(@PathVariable long id,
                                      @RequestBody @Valid SaveTaskRequest request) {
        return taskService.mergeById(id, request);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse changeTaskStatusById(@PathVariable long id,
                                             @RequestBody @Valid ChangeTaskStatusRequest request) {
        return taskService.changeStatusById(id, request.getStatus());
    }

    @PatchMapping("/{id}/take")
    public TaskResponse changeTaskExecutorById(@PathVariable long id,
                                               @AuthenticationPrincipal String username) {
        return taskService.changeExecutorById(id, username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskById(@PathVariable long id) {
        taskService.deleteById(id);
    }

    @GetMapping("/search")
    public Page<TaskResponse> search(@RequestParam String keyword, @Parameter(hidden = true) Pageable pageable) {
        return taskService.search(keyword, pageable);
    }
}
