package local.nix.task.management.system.rest.model.task.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import local.nix.task.management.system.rest.model.task.Task;
import local.nix.task.management.system.rest.model.task.TaskStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class TaskResponse {

    private Long id;
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime deadline;

    private String executor;
    private String reviewer;
    private TaskStatus taskStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    public static TaskResponse fromTask(Task task) {
        TaskResponse response = new TaskResponse();
        response.id = task.getId();
        response.text = task.getText();
        response.deadline = task.getDeadline();
        if(!Objects.isNull(task.getExecutor())) {
            response.executor = task.getExecutor().getName();
        }
        response.reviewer = task.getReviewer().getName();
        response.taskStatus = task.getStatus();
        response.createdAt = task.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResponse that = (TaskResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(text, that.text) &&
                Objects.equals(deadline, that.deadline) &&
                Objects.equals(executor, that.executor) &&
                Objects.equals(reviewer, that.reviewer) &&
                taskStatus == that.taskStatus &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, deadline, executor, reviewer, taskStatus, createdAt);
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", deadline=" + deadline +
                ", executor='" + executor + '\'' +
                ", reviewer='" + reviewer + '\'' +
                ", status=" + taskStatus +
                ", createdAt=" + createdAt +
                '}';
    }
}
