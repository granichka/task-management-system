package local.nix.task.management.system.rest.service;

import local.nix.task.management.system.rest.exception.TaskManagementSystemExceptions;
import local.nix.task.management.system.rest.model.task.Task;
import local.nix.task.management.system.rest.model.task.TaskStatus;
import local.nix.task.management.system.rest.model.task.request.SaveTaskRequest;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.repository.TaskRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;


@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse create(SaveTaskRequest request) {
        return TaskResponse.fromTask(save(request));
    }

    private Task save(SaveTaskRequest request) {
        Task task = new Task();
        LocalDateTime deadline = parseStringToLocalDateTime(request.getDeadline());
        task.setDeadline(deadline);
        task.setText(request.getText());
        if(!Objects.isNull(request.getExecutor())) {
            task.setExecutor(getUserByName(request.getExecutor()));
        }
        task.setReviewer(getUserByName(request.getReviewer()));
        task.setCreatedAt(Instant.now());
        taskRepository.save(task);
        return task;
    }


    @Transactional(readOnly = true)
    public Page<TaskResponse> list(Pageable pageable) {
        return taskRepository.findAll(pageable).map(TaskResponse::fromTask);
    }


    @Transactional(readOnly = true)
    public Page<TaskResponse> search(String keyword, Pageable pageable) {
        return taskRepository.search(keyword, pageable)
                .map(TaskResponse::fromTask);
    }

    @Transactional
    public TaskResponse mergeById(long id, SaveTaskRequest request) {
        Task task = getTask(id);
        return TaskResponse.fromTask(merge(task, request));
    }


    private Task merge(Task task, SaveTaskRequest request) {
        LocalDateTime requestDeadline = parseStringToLocalDateTime(request.getDeadline());
        LocalDateTime taskDeadline = task.getDeadline();
        if (!taskDeadline.equals(requestDeadline)) {
            if(!(requestDeadline.compareTo(taskDeadline) < 0)) {
                task.setDeadline(requestDeadline);
            }
        }
        String requestText = request.getText();
        String taskText = task.getText();
        if (!taskText.equals(requestText)) {
            task.setText(requestText);
        }
        String requestExecutorName = request.getExecutor();
        String taskExecutorName = task.getExecutor().getName();
        if(!taskExecutorName.equals(requestExecutorName)) {
            User newTaskExecutor = getUserByName(requestExecutorName);
            task.setExecutor(newTaskExecutor);
        }
        String requestReviewerName = request.getReviewer();
        String taskReviewerName = task.getReviewer().getName();
        if(!taskReviewerName.equals(requestReviewerName)) {
            User newTaskReviewer = getUserByName(requestReviewerName);
            task.setReviewer(newTaskReviewer);
        }
        return taskRepository.save(task);

    }

    @Transactional
    public TaskResponse changeStatusById(long id, TaskStatus status) {
        Task task = getTask(id);
        if (task.getStatus() != status) {
            task.setStatus(status);
            taskRepository.save(task);
        }
        return TaskResponse.fromTask(task);
    }

    @Transactional
    public TaskResponse changeExecutorById(long id, String executor) {
        Task task = getTask(id);
        if (Objects.isNull(task.getExecutor())) {
            User executorForTask = getUserByUsername(executor);
            task.setExecutor(executorForTask);
            taskRepository.save(task);
        }
        return TaskResponse.fromTask(task);
    }

    @Transactional
    public void deleteById(long id) {
        if (!taskRepository.existsById(id)) throw TaskManagementSystemExceptions.taskNotFound(id);
        taskRepository.purgeById(id);
    }

    @Transactional(readOnly = true)
    public Optional<TaskResponse> findById(Long id) {
        return taskRepository.findById(id).map(TaskResponse::fromTask);
    }


    private User getUserByName(String name) {
        return userRepository.findUserByName(name)
                .orElseThrow(() -> TaskManagementSystemExceptions.userWithSuchNameNotFound(name));
    }

    private User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> TaskManagementSystemExceptions.userNotFound(username));
    }

    private Task getTask(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> TaskManagementSystemExceptions.taskNotFound(id));
    }

    public static LocalDateTime parseStringToLocalDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
        return localDateTime;
    }
}
