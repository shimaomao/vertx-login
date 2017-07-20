import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

public class Db {
    public static final String COLLECTION = "vlogin";
    private MongoClient client;
    public Db(Vertx vertx){
        JsonObject params = new JsonObject()
                .put("connection_string", vertx.config().getString("db_connectionString"));
        this.client = MongoClient.createShared(vertx, params);
    }
    public void save(){
        JsonObject query = new JsonObject().put("title", "The Hobbit");
        JsonObject update = new JsonObject().put("$set",
                new JsonObject().put("author", "J. R. R. Tolkien"));
        UpdateOptions options = new UpdateOptions().setMulti(false).setUpsert(true);

        this.client.updateWithOptions(COLLECTION, query, update, options, res -> {
            if (res.succeeded()) {

                System.out.println("Book updated !");

            } else {

                res.cause().printStackTrace();
            }

        });
    }
}