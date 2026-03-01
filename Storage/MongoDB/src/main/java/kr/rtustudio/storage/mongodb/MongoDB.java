package kr.rtustudio.storage.mongodb;

import kr.rtustudio.storage.Storage;
import kr.rtustudio.storage.StorageLogger;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import com.google.gson.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

@Slf4j
public class MongoDB implements Storage {

    private final Pool connection;
    private final Gson gson = new Gson();
    private final String collectionName;

    public MongoDB(Pool connection, Config config, String name) {
        this.connection = connection;
        this.collectionName = config.getCollectionPrefix() + name;
    }

    @Override
    public @NonNull CompletableFuture<Result> add(@NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    try {
                        MongoCollection<Document> collection =
                                connection.getDatabase().getCollection(collectionName);
                        Document document = Document.parse(gson.toJson(data));
                        StorageLogger.logAdd(log, this.collectionName, document.toJson());
                        return collection.insertOne(document).wasAcknowledged()
                                ? Result.UPDATED
                                : Result.FAILED;
                    } catch (Exception e) {
                        StorageLogger.logError(log, "ADD", this.collectionName, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        MongoCollection<Document> collection =
                                connection.getDatabase().getCollection(collectionName);
                        Bson filter = filter(find);

                        if (isNull(data)) {
                            StorageLogger.logSet(
                                    log,
                                    this.collectionName,
                                    filter.toBsonDocument().toJson() + " (DELETE)");
                            DeleteResult result = collection.deleteMany(filter);
                            if (result.wasAcknowledged()) {
                                return result.getDeletedCount() > 0
                                        ? Result.UPDATED
                                        : Result.UNCHANGED;
                            }
                            return Result.FAILED;
                        } else {
                            UpdateOptions options = new UpdateOptions().upsert(true);
                            BsonDocument update =
                                    new BsonDocument("$set", BsonDocument.parse(data.toString()));
                            StorageLogger.logSet(
                                    log,
                                    this.collectionName,
                                    "filter: "
                                            + filter.toBsonDocument().toJson()
                                            + ", update: "
                                            + update.toJson());

                            UpdateResult result = collection.updateMany(filter, update, options);
                            if (result.wasAcknowledged()) {
                                return result.getModifiedCount() > 0
                                                || result.getUpsertedId() != null
                                        ? Result.UPDATED
                                        : Result.UNCHANGED;
                            }
                            return Result.FAILED;
                        }
                    } catch (Exception e) {
                        StorageLogger.logError(log, "SET", this.collectionName, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(@NotNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    try {
                        MongoCollection<Document> collection =
                                connection.getDatabase().getCollection(collectionName);
                        Bson filter = filter(find);
                        StorageLogger.logGet(
                                log, this.collectionName, filter.toBsonDocument().toJson());

                        for (Document document : collection.find(filter)) {
                            if (document != null && !document.isEmpty()) {
                                result.add(
                                        JsonParser.parseString(document.toJson())
                                                .getAsJsonObject());
                            }
                        }
                    } catch (Exception e) {
                        StorageLogger.logError(log, "GET", this.collectionName, e);
                    }
                    return result;
                });
    }

    private Bson filter(JsonObject find) {
        if (isNull(find)) return Filters.empty();

        List<Bson> filters = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : find.entrySet()) {
            String key = entry.getKey();
            JsonElement el = entry.getValue();

            if (el.isJsonNull()) {
                filters.add(Filters.eq(key, null));
            } else if (el instanceof JsonPrimitive primitive) {
                if (primitive.isBoolean()) {
                    filters.add(Filters.eq(key, primitive.getAsBoolean()));
                } else if (primitive.isNumber()) {
                    filters.add(Filters.eq(key, primitive.getAsNumber()));
                } else if (primitive.isString()) {
                    filters.add(Filters.eq(key, primitive.getAsString()));
                }
            } else {
                filters.add(Filters.eq(key, Document.parse(el.toString())));
            }
        }

        return filters.isEmpty() ? Filters.empty() : Filters.and(filters);
    }

    private boolean isNull(JsonObject json) {
        return json == null || json.size() == 0 || json.isJsonNull();
    }

    @Override
    public void close() {}

    @Slf4j
    public static class Pool implements AutoCloseable {

        private final MongoClient client;
        private final MongoDatabase database;

        public Pool(Config config) {
            String serverHost = config.getHost() + ":" + config.getPort();
            StringBuilder uriBuilder = new StringBuilder("mongodb://");
            String username = config.getUsername();
            String password = config.getPassword();

            if (username != null && !username.isEmpty()) {
                uriBuilder.append(username);
                if (password != null && !password.isEmpty()) {
                    uriBuilder.append(":").append(password);
                }
                uriBuilder.append("@");
            }
            uriBuilder.append(serverHost);

            ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
            MongoClientSettings settings =
                    MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(uriBuilder.toString()))
                            .serverApi(serverApi)
                            .build();

            this.client = MongoClients.create(settings);
            this.database = client.getDatabase(config.getDatabase());
            log.info("MongoDB connected ({})", serverHost);
        }

        public MongoDatabase getDatabase() {
            return database;
        }

        @Override
        public void close() {
            if (client != null) {
                client.close();
                log.info("MongoDB disconnected");
            }
        }
    }

    public interface Config {

        String getHost();

        int getPort();

        String getDatabase();

        String getUsername();

        String getPassword();

        String getCollectionPrefix();
    }
}
