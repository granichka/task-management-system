package local.nix.task.management.system.rest.model.task;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import local.nix.task.management.system.rest.model.user.User;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "executor_id")
    private User executor;

    @JsonManagedReference
    @ManyToOne
    @JoinColumn(nullable = false, name = "reviewer_id")
    private User reviewer;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private TaskStatus taskStatus = TaskStatus.NOT_STARTED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getExecutor() {
        return executor;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public TaskStatus getStatus() {
        return taskStatus;
    }

    public void setStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
