package local.nix.task.management.system.rest.model.task.request;

import javax.validation.constraints.NotBlank;

public class SaveTaskRequest {

    @NotBlank(message = "username must not be blank")
    private String text;

    @NotBlank(message = "deadline must not be blank")
    private String deadline;

    private String executor;

    @NotBlank(message = "reviewer must not be blank")
    private String reviewer;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
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
}
