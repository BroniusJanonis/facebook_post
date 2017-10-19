package demo;

import javafx.geometry.Pos;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.*;

public class SqlToModel implements IModel{

    // cia microservisuose vietoj JDBCTemplate, skirta CRUD su serveriu
    Sql2o sql2o;
    // random UUID generuos
    UuidGenerator uuidGenerator;

    public SqlToModel() {
    }
    // kai tik uuidGenerator iskvieciame, tai default konstruktoriuje uzpildome duomenis + kuriant objekta jau bus implementuotas sql2o
    public SqlToModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        uuidGenerator = new RandomUUIDGenerator();
    }

    @Override
    public UUID createPost(String title, String content, List<String> categories) {

        // sql2o connection => siuntimas vieno ar keliu selektu. Turi visa transakcija suveikti, nes kitu atveju neisiraso
        // tai pvz sistejome norim irasyti per viena teransakcija kelis updates, jei vienus sufeilina, tai neisiraso i serveri ir serveris atsistato i pradine (pries tai buvusia) padeti
        // try () duoda tai, jog, jei kazkas negerai su transakcija, tai is karto uzdaro transakcija
        try(Connection conn = sql2o.beginTransaction()) {
            UUID postUuid = uuidGenerator.generator();
            // :post_uuid naudojam, nes mes negeneruosim ID automatistai, todel ir pasikurem UuidGenerator uuidGenerator;
            conn.createQuery("INSERT INTO posts (post_uuid, title, content, publish_date) VALUES (:post_uuid, :title, :content, :publish_date)")
                    .addParameter("post_uuid", postUuid)
                    .addParameter("title", title)
                    .addParameter("content", content)
                    .addParameter("publish_date", new Date())
                    .executeUpdate();

            for (String cat : categories) {
                conn.createQuery("INSERT INTO post_category (post_uuid, category) VALUES (:post_uuid, :category)")
                        .addParameter("post_uuid", postUuid)
                        .addParameter("category", cat)
                        .executeUpdate();
            }
            // arba galima taip (cia yra lembda klase)/ Lembda yra "->" regis
//        categories.forEach(s -> {
//            conn.createQuery("INSERT INTO post_category (post_uuid, category) VALUES (:post_uuid, category)")
//                    .addParameter("post_uuid", postUuid)
//                    .addParameter("category", s)
//                    .executeUpdate();
//        });
            // arba dar Stream pridet, jei norim papildomu filtru :]
//        categories.stream().forEach(g-> {
//            conn.createQuery("INSERT INTO post_category (post_uuid, category) VALUES (:post_uuid, category)")
//                    .addParameter("post_uuid", postUuid)
//                    .addParameter("category", g)
//                    .executeUpdate();
//        });
            // arba per Iterator()
//        Iterator<String> iterator = categories.iterator();
//        while(iterator.hasNext()){
//            conn.createQuery("INSERT INTO post_category (post_uuid, category) VALUES (:post_uuid, category)")
//                    .addParameter("post_uuid", postUuid)
//                    .addParameter("category", iterator.next())
//                    .executeUpdate();
//        }
            // commitinam transakcija
            conn.commit();

            return postUuid;
        }
    }

    @Override
    public UUID createComment(UUID postuuid, String author, String content) {
        // vietoj sql2o.beginTransaction() galim naudoti sql2o.open(), tuomet nebus transakcijos, o sius is karto
        // skirtumas tarp transaction() ir open() > i transakcija() gali deti kelis selektus, o open() tik viena
        // tad su transakcija reikia commit(), nes parodom, kada transakcija surinkta ir siunciam, o su open() is karto siunciam, nes yra tik vienas selektas, daugiau nelaukiam
        try(Connection conn = sql2o.open()) {
            UUID randomCommentUUID = uuidGenerator.generator();
            conn.createQuery("INSERT INTO comments (comments_uuid, post_uuid, author, content, submission_date, approved_com) VALUES (:comm_uuid, :pst_uuid, :auth, :content, :submiss_date, :approv_com)")
                    .addParameter("comm_uuid", randomCommentUUID)
                    .addParameter("pst_uuid", postuuid)
                    .addParameter("auth", author)
                    .addParameter("content", content)
                    .addParameter("submiss_date", new Date())
                    .addParameter("approv_com", true)
                    .executeUpdate();

            return randomCommentUUID;
        }
    }

    @Override
    public List<Posts> getAllPosts() {
        try(Connection conn = sql2o.beginTransaction()) {
            // tuscias listas, i kuri desiu visus selektus
            List<Posts> allListPost = new ArrayList<>();
//            SELECT * FROM posts INNER JOIN post_category on posts.post_uuid=post_category.post_uuid INNER JOIN comments ON posts.post_uuid = comments.post_uuid
            // gaunu lista postu
            List<Posts> postsList = conn.createQuery("SELECT post_uuid, title, content, publish_date FROM posts")
                    .executeAndFetch(Posts.class);
            // kol yra listas postu, tol, pagal jo id, selektinsiu lista komentaru ir stringu
            for(Posts post: postsList){
                // listas komentaru
                List<Comment> commentList = conn.createQuery("SELECT post_uuid, author, content, submission_date, approved_com FROM comments WHERE post_uuid='"+post.getPost_uuid()+"'")
                        .executeAndFetch(Comment.class);
                // listas stringu
                List<String> categoryList = conn.createQuery("SELECT category FROM post_category WHERE post_uuid='"+post.getPost_uuid()+"'")
                        .executeAndFetch(String.class);
                // dedu i post objekta lista komenratu ir listu
                post.setComments(commentList);
                post.setCategories(categoryList);
                // keliu objekta su listais i nauja bendra lista
                allListPost.add(post);
            }
            conn.commit();

            return allListPost;
        }
    }

    @Override
    public List<Comment> getAllComments(UUID postuuid) {
        try(Connection conn = sql2o.open()) {
            List<Comment> commentList = conn.createQuery("SELECT * FROM comments WHERE post_uuid=:pstuuid")
                    .addParameter("pstuuid", postuuid)
                    .executeAndFetch(Comment.class);
            return commentList;
        }
    }

    @Override
    public boolean existPost(UUID postuuid) {
        try(Connection conn = sql2o.open()) {
            List<Posts> postsList = conn.createQuery("SELECT * FROM posts WHERE post_uuid=:post")
                    .addParameter("post", postuuid)
                    .executeAndFetch(Posts.class);
            // true, jei listas daugiau uz 0 (nera tuscias)
            return postsList.size()>0;
        }
    }

    // reikia Post modelyje Date pakeist i stringa arba date konvertuot i stringini formata (kuri gaunam is Postman), nes isparsinus neisiraso ir atmetamas irasymas
    @Override
    public void updatePost(Posts posts) {
        try(Connection conn = sql2o.beginTransaction()){
            conn.createQuery("UPDATE posts SET title=:tlt, content=:cntnt, publish_date=: pub_dt WHERE post_uuid=:pst_uuid")
                    .addParameter("tlt",posts.getTitle())
                    .addParameter("cntnt",posts.getContent())
                    .addParameter("pub_dt",posts.getPublish_date())
                    .addParameter("pst_uuid",posts.getPost_uuid())
                    .executeUpdate();
            conn.commit();
        }
    }

    @Override
    public void deletePost(UUID uuid) {

    }

    @Override
    public Optional<Posts> getPost(UUID postuuid) {
        return null;
    }
}
