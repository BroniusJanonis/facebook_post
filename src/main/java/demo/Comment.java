package demo;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Comment {
    private UUID comments_uuid;
    private UUID post_uuid;
    private String author;
    private String content;
    private Date submission_date;
    private boolean approved_com;

    public Comment() {
    }

    public Comment(UUID post_uuid, String author, String content) {
        this.post_uuid = post_uuid;
        this.author = author;
        this.content = content;
    }

    public UUID getComments_uuid() {
        return comments_uuid;
    }

    public void setComments_uuid(UUID comments_uuid) {
        this.comments_uuid = comments_uuid;
    }

    public UUID getPost_uuid() {
        return post_uuid;
    }

    public void setPost_uuid(UUID post_uuid) {
        this.post_uuid = post_uuid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSubmission_date() {
        return submission_date;
    }

    public void setSubmission_date(Date submission_date) {
        this.submission_date = submission_date;
    }

    public boolean isApproved_com() {
        return approved_com;
    }

    public void setApproved_com(boolean approved_com) {
        this.approved_com = approved_com;
    }
}
