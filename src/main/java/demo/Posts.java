package demo;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

//Mikroserviso Lombok anotacija skirta suristi modeli su duomenu baze (kaip Spring'e @Entity)
@Data
public class Posts {

    private UUID post_uuid;
    private String title;
    private String content;
    private Date publish_date;
    // kadangi categories table yra tik string'as, tai lista atvaizduosim, o ne categories objekta
    private List<String> categories;
    private List<Comment> comments;

    public Posts() {
    }

    public Posts(String title, String content, List<String> categories) {
        this.title = title;
        this.content = content;
        this.categories = categories;
    }

    public UUID getPost_uuid() {
        return post_uuid;
    }

    public void setPost_uuid(UUID post_uuid) {
        this.post_uuid = post_uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(Date publish_date) {
        this.publish_date = publish_date;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
