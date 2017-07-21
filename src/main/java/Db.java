import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

public class Db {
    // could have just used MongoAuth
    public static final String COLLECTION = "vlogin";
    private MongoClient client;
    public Db(Vertx vertx, JsonObject config){
        JsonObject params = new JsonObject()
                .put("connection_string", config.getString("db_connectionString"));
        this.client = MongoClient.createShared(vertx, params);
        System.out.println("Db initialized.");

        // setup test accounts
        CompositeFuture
        .all(
            this.upsert("foo@foo.com", "foo"),
            this.upsert("bar@bar.com", "bar"))
        .setHandler((res)->{
            System.out.println("Db test accounts created.");
        });

    }

    // find user by email
    public Future<JsonObject> find(String email) {
        JsonObject query = new JsonObject()
                .put("email", email);
        Future<JsonObject> future = Future.future();
        this.client.find(COLLECTION, query, res -> {
            if (res.succeeded()) {
                if(res.result().size()==0) {
                    future.fail("No docs found matching email.");
                }
                future.complete(res.result().get(0)); //JsonObject
            } else {
                res.cause().printStackTrace();
                future.fail("find query failed.");
            }
        });
        return future;
    }

    // performs mongo upsert operation
    public Future<JsonObject> upsert(String email, String password){
        JsonObject query = new JsonObject()
                .put("email", email)
                .put("password", password);
        JsonObject update = new JsonObject().put("$set",
                new JsonObject().put("email", email)
                .put("password", password));
        Future<JsonObject> future = Future.future();
        UpdateOptions options = new UpdateOptions().setMulti(false).setUpsert(true);

        this.client.updateWithOptions(COLLECTION, query, update, options, res -> {
            if (res.succeeded()) {
                future.complete(update);
            } else {
                res.cause().printStackTrace();
                future.fail("Upsert failed.");
            }

        });
        return future;
    }
}
