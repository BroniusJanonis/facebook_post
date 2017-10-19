package demo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// metodu interface apsirasom
public interface IModel {
    UUID createPost(String title, String content, List<String> categories);
    // kadangi pirma eis postas sukurtas, o pakuriant comentara mums reikes sugeneruoto automatiskai posto id
    UUID createComment(UUID postuuid, String author, String content);
    List<Posts> getAllPosts();
    // gauti visus komentarus priklausancius vienam postui
    List<Comment> getAllComments(UUID postuuid);
    // ar egzistuoja postas
    boolean existPost(UUID postuuid);
    void updatePost(Posts posts);
    void deletePost(UUID uuid);
    // Optional yra Java klase, kuri duoda verte, nors ir tuscia, jei vdrug getPost pagrazintu null
    // tuomet neduos not null exception, gausim tuscia, bet ne klaida, kad sugriuna
    Optional<Posts> getPost(UUID postuuid);
}
