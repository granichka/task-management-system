package local.nix.task.management.system.rest.service;

import local.nix.task.management.system.rest.model.task.Task;
import local.nix.task.management.system.rest.model.task.TaskStatus;
import local.nix.task.management.system.rest.model.task.request.SaveTaskRequest;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.model.user.User;
import local.nix.task.management.system.rest.repository.TaskRepository;
import local.nix.task.management.system.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    private TaskService taskService;

    private TaskRepository taskRepository;

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        userRepository = mock(UserRepository.class);
        taskService = new TaskService(taskRepository, userRepository);
    }

    @Test
    void findByIdMethodTest() {
        long validId = 1l;
        long invalidId = 2l;
        Task task = new Task();
        task.setId(validId);
        task.setText("test task");
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setDeadline(LocalDateTime.now());
        User testExecutor = createUserWithName("testExecutor");
        task.setExecutor(testExecutor);
        User testReviewer = createUserWithName("testReviewer");
        task.setReviewer(testReviewer);

        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(taskRepository.findById(validId)).thenReturn(Optional.of(task));

        Optional<TaskResponse> responseWithInvalidId = taskService.findById(invalidId);

        assertThat(responseWithInvalidId).isEmpty();
        verify(taskRepository).findById(invalidId);

        Optional<TaskResponse> responseWithValidId = taskService.findById(validId);

        assertThat(responseWithValidId).hasValueSatisfying(taskResponse ->
                assertTaskMatchesResponse(task, taskResponse));
        verify(taskRepository).findById(validId);

        verifyNoMoreInteractions(taskRepository);
    }


    @Test
    void createMethodTest() {
        SaveTaskRequest request = new SaveTaskRequest();
        request.setText("test");
        request.setDeadline("2020-10-12 18:00:00");
        LocalDateTime parsedString = TaskService.parseStringToLocalDateTime(request.getDeadline());
        request.setExecutor("Test Executor");
        request.setReviewer("Test Reviewer");
        long futureTaskId = 1l;

        String executorName = "Test Executor";
        User testExecutor = createUserWithName(executorName);
        String reviewerName = "Test Reviewer";
        User testReviewer = createUserWithName(reviewerName);

        when(userRepository.findUserByName(executorName)).thenReturn(Optional.of(testExecutor));
        when(userRepository.findUserByName(reviewerName)).thenReturn(Optional.of(testReviewer));


        when(taskRepository.save(notNull())).thenAnswer(invocation -> {
            Task entity = invocation.getArgument(0);
            assertThat(entity.getId()).isNull();
            assertThat(entity.getText()).isEqualTo(request.getText());
            assertThat(entity.getDeadline()).isEqualTo(parsedString);
            assertThat(entity.getExecutor().getName()).isEqualTo(executorName);
            assertThat(entity.getReviewer().getName()).isEqualTo(reviewerName);
            entity.setId(futureTaskId);
            return entity;
        });

        TaskResponse response = taskService.create(request);

        assertThat(response.getId()).isEqualTo(futureTaskId);
        assertThat(response.getText()).isEqualTo(request.getText());
        assertThat(response.getDeadline()).isEqualTo(parsedString);
        assertThat(response.getExecutor()).isEqualTo(request.getExecutor());
        assertThat(response.getReviewer()).isEqualTo(request.getReviewer());

        verify(taskRepository, only()).save(notNull());
    }

    @Test
    void mergeByIdMethodTest() {
        Task taskToUpdate = new Task();
        long taskId = 1l;
        taskToUpdate.setId(taskId);
        taskToUpdate.setDeadline(LocalDateTime.now());
        taskToUpdate.setText("test");
        taskToUpdate.setStatus(TaskStatus.NOT_STARTED);
        String executorName = "Test Executor";
        User testExecutor = createUserWithName(executorName);
        taskToUpdate.setExecutor(testExecutor);
        String reviewerName = "Test Reviewer";
        User testReviewer = createUserWithName(reviewerName);
        taskToUpdate.setReviewer(testReviewer);


        String newText = "new test";
        String newDeadline = "2020-10-13 18:00:00";
        String newExecutorName = "New Test Executor";
        User newTestExecutor = createUserWithName(newExecutorName);
        String newReviewerName = "Test Reviewer";
        User newTestReviewer = createUserWithName(newReviewerName);

        SaveTaskRequest request = new SaveTaskRequest();
        request.setText(newText);
        request.setDeadline(newDeadline);
        request.setExecutor(newExecutorName);
        request.setReviewer(newReviewerName);

        long invalidId = 2l;

        when(userRepository.findUserByName(executorName)).thenReturn(Optional.of(testExecutor));
        when(userRepository.findUserByName(reviewerName)).thenReturn(Optional.of(testReviewer));

        when(userRepository.findUserByName(newExecutorName)).thenReturn(Optional.of(newTestExecutor));
        when(userRepository.findUserByName(newReviewerName)).thenReturn(Optional.of(newTestReviewer));

        when(taskRepository.findById(invalidId)).thenReturn(Optional.empty());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskToUpdate));
        when(taskRepository.save(same(taskToUpdate))).thenReturn(taskToUpdate);

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> taskService.mergeById(invalidId, request))
                .satisfies(e -> assertThat(e.getStatus()).isSameAs(HttpStatus.NOT_FOUND));

        verify(taskRepository).findById(invalidId);

        taskService.mergeById(taskId, request);

        assertThat(taskToUpdate.getText()).isEqualTo(newText);
        assertThat(taskToUpdate.getDeadline()).isEqualTo(TaskService.parseStringToLocalDateTime(newDeadline));
        assertThat(taskToUpdate.getExecutor().getName()).isEqualTo(newExecutorName);
        assertThat(taskToUpdate.getReviewer().getName()).isEqualTo(newReviewerName);
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(same(taskToUpdate));

        verifyNoMoreInteractions(taskRepository);



    }

    @Test
    void deleteByIdMethodTest() {
        long validId = 1l;
        long invalidId = 2l;

        when(taskRepository.existsById(invalidId)).thenReturn(false);
        when(taskRepository.existsById(validId)).thenReturn(true);

        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(() -> taskService.deleteById(invalidId))
                .satisfies(e -> assertThat(e.getStatus()).isSameAs(HttpStatus.NOT_FOUND));

        verify(taskRepository).existsById(invalidId);

        taskService.deleteById(validId);
        verify(taskRepository).existsById(validId);
        verify(taskRepository).purgeById(validId);

        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    void changeStatusByIdMethodTest() {
        Task task = new Task();
        long taskId = 1l;
        task.setId(taskId);
        task.setDeadline(LocalDateTime.now());
        task.setText("test");
        task.setStatus(TaskStatus.NOT_STARTED);
        String executorName = "Test Executor";
        User testExecutor = createUserWithName(executorName);
        task.setExecutor(testExecutor);
        String reviewerName = "Test Reviewer";
        User testReviewer = createUserWithName(reviewerName);
        task.setReviewer(testReviewer);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(same(task))).thenReturn(task);

        TaskStatus willNotCauseChanges = TaskStatus.NOT_STARTED;
        taskService.changeStatusById(taskId, willNotCauseChanges);
        verify(taskRepository, times(0)).save(same(task));


        TaskStatus willCauseChanges = TaskStatus.IN_PROGRESS;
        taskService.changeStatusById(taskId, willCauseChanges);

        assertThat(task.getStatus()).isEqualTo(willCauseChanges);
        verify(taskRepository).save(same(task));


    }

    @Test
    void searchMethodTest() {
        Task first = new Task();
        String firstTaskText = "test first";
        first.setText(firstTaskText);
        Task second = new Task();
        String secondTaskText = "second";
        second.setText(secondTaskText);
        Task third = new Task();
        String thirdTaskText = "test third";
        third.setText(thirdTaskText);

        User user = new User();
        user.setName("User");
        first.setExecutor(user);
        first.setReviewer(user);
        second.setExecutor(user);
        second.setReviewer(user);
        third.setExecutor(user);
        third.setReviewer(user);

        List<Task> taskList = List.of(first, third);
        Page<Task> page = new PageImpl<>(taskList);

        when(taskRepository.search("test", null)).thenReturn(page);
        when(taskRepository.search("hello", null)).thenReturn(Page.<Task>empty());

        Page<TaskResponse> mustHaveZeroSize = taskService.search("hello", null);
        assertThat(mustHaveZeroSize.getSize()).isEqualTo(0);

        Page<TaskResponse> mustHaveTwoElements = taskService.search("test", null);
        assertThat(mustHaveTwoElements.getSize()).isEqualTo(2);
        String textOfFirstElement = mustHaveTwoElements.getContent().get(0).getText();
        assertThat(textOfFirstElement).isEqualTo(firstTaskText);
        String textOfSecondElement = mustHaveTwoElements.getContent().get(1).getText();
        assertThat(textOfSecondElement).isEqualTo(thirdTaskText);
    }

    @Test
    void listMethodTest() {
        Task first = new Task();
        String firstTaskText = "test first";
        first.setText(firstTaskText);
        Task second = new Task();
        String secondTaskText = "test second";
        second.setText(secondTaskText);
        Task third = new Task();
        String thirdTaskText = "test third";
        third.setText(thirdTaskText);

        User user = new User();
        user.setName("User");
        first.setExecutor(user);
        first.setReviewer(user);
        second.setExecutor(user);
        second.setReviewer(user);
        third.setExecutor(user);
        third.setReviewer(user);

        List<Task> taskList = List.of(first, second);
        Page<Task> page = new PageImpl<>(taskList);

        List<Task> allTasksList = List.of(first, second, third);
        Page<Task> pageWithAllTasks = new PageImpl<>(allTasksList);

        Pageable firstPageWithTwoElements = PageRequest.of(0, 2);
        Pageable withoutPageable = Pageable.unpaged();

        when(taskRepository.findAll(firstPageWithTwoElements)).thenReturn(page);
        when(taskRepository.findAll(withoutPageable)).thenReturn(pageWithAllTasks);

        Page<TaskResponse> result = taskService.list(firstPageWithTwoElements);
        assertThat(result.getSize()).isEqualTo(2);
        String textOfFirstElement = result.getContent().get(0).getText();
        assertThat(textOfFirstElement).isEqualTo(firstTaskText);
        String textOfSecondElement = result.getContent().get(1).getText();
        assertThat(textOfSecondElement).isEqualTo(secondTaskText);

        Page<TaskResponse> result2 = taskService.list(withoutPageable);
        assertThat(result2.getSize()).isEqualTo(3);



    }


    private User createUserWithName(String name) {
        User user = new User();
        user.setName(name);
        return user;
    }

    private static void assertTaskMatchesResponse(Task task, TaskResponse taskResponse) {
        assertThat(taskResponse.getId()).isEqualTo(task.getId());
        assertThat(taskResponse.getText()).isEqualTo(task.getText());
        assertThat(taskResponse.getTaskStatus()).isEqualTo(task.getStatus());
    }


}
