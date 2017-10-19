package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.util.List;
import java.util.UUID;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

public class Main {
    public static void main(String[] args) {

        // Sql2o apsirasom koncekcija mikroservisu su DB
        // new PostgresQuirks() naudojamas konvertuoti UUID formatui, nes Postgresql savyje nesupras UUID formato, tad mes jam pranesam, ka su tokia gauta informacija padaryti
        Sql2o sql2o = new Sql2o("jdbc:postgresql://localhost:5432/postgres", "postgres","CodeAcademy333", new PostgresQuirks()
        {
            {
                converters.put(UUID.class, new UUIDConverter());
            }
        });

        port(8067);

        // Interface = Interfeiso klasei implementuotai, kuri (turi savyje konekcija, pakuriant metodui) < SqlToModel construktoriuje todel ir pasirasem konekcija
        // cia padarom parsinima su Gson
        IModel model = new SqlToModel(sql2o);
        post("/createPost", (request, response) ->
                {
                    /// post modelis is Gson
                    Gson gson = new Gson();
                    // stringa json formatu paverciam i Objekta musu
                    String body = request.body();
                    Posts post = gson.fromJson(body, Posts.class);

                    UUID id = model.createPost(post.getTitle(),post.getContent(), post.getCategories());
                    response.status(200);
                    response.type("application/json");
                    return response;
                }
        );

        // cia padarom parsinima su Jackson'u
        post("/createComment", (request, response) -> {

            // pirma turime mapper'i, kuriame sudesime savo objekta is gautu request duomenu
            ObjectMapper mapper = new ObjectMapper();
            // tuomet turime modeli. Prisilyginam null'ui, jei tarkim dedam i try, tada nematysim post, jei nebusime pasikure sio
            Comment post = null;

            // surenkam stringa per request'a
            String body = request.body();

            //JSON from String to Object
           post = mapper.readValue(body, Comment.class);

           UUID id = model.createComment(post.getPost_uuid(), post.getAuthor(), post.getContent());
           response.status(200);
           response.type("application/json");
            return response;
        });

        get("/getAllPosts", (request, response) -> {
            List<Posts> allPosts = model.getAllPosts();
            response.status(200);
            response.type("application/json");
            // mapper Jackson
            ObjectMapper mapper = new ObjectMapper();
            // Object (cia list'as) to Json in String
            return mapper.writeValueAsString(allPosts);
        });

        // PVZ per Postman.: http://localhost:8067/getAllComments?uuid=d46e313a-44b5-4cd9-8195-e36e2ae5d568
        get("/getAllComments", (request, response) -> {
            List<Comment> allPosts = model.getAllComments(UUID.fromString(request.queryParams("uuid")));
            response.status(200);
            response.type("application/json");
            // mapper Jackson
            ObjectMapper mapper = new ObjectMapper();
            // Object (cia list'as) to Json in String
            return mapper.writeValueAsString(allPosts);
        });

        // PVZ per Postman.: http://localhost:8067/existPost?uuid=d46e313a-44b5-4cd9-8195-e36e2ae5d568
        get("/existPost", (request, response) -> {
            boolean uuid = model.existPost(UUID.fromString(request.queryParams("uuid")));
            response.status(200);
            response.type("application/json");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(uuid);
        });

        post("/updatePost", (request, response) ->
                {
                    Gson gson = new Gson();
                    String body = request.body();
                    Posts post = gson.fromJson(body, Posts.class);

                    model.updatePost(post);
                    response.status(200);
                    response.type("application/json");
                    return response;
                }
        );

    }
}
