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
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

@Slf4j
public class MongoDB implements Storage {

    public interface Config {
        String getHost();

        int getPort();

        String getDatabase();

        String getUsername();

        String getPassword();

        String getCollectionPrefix();
    }

    private final Gson gson = new Gson();
    private final MongoClient client;
    private final MongoDatabase database;
    private final String prefix;

    public MongoDB(Config config) {
        this.prefix = config.getCollectionPrefix();
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

    private boolean isNull(JsonObject json) {
        return json == null || json.isEmpty() || json.isJsonNull();
    }

    @Override
    public @NonNull CompletableFuture<Result> add(
            @NotNull String collectionName, @NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    if (isNull(data)) return Result.FAILED;
                    try {
                        MongoCollection<Document> collection =
                                database.getCollection(prefix + collectionName);
                        Document document = Document.parse(gson.toJson(data));
                        StorageLogger.logAdd(log, collectionName, document.toJson());
                        return collection.insertOne(document).wasAcknowledged()
                                ? Result.UPDATED
                                : Result.FAILED;
                    } catch (Exception e) {
                        StorageLogger.logError(log, "ADD", collectionName, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<Result> set(
            @NotNull String collectionName, @NonNull JsonObject find, @NonNull JsonObject data) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        MongoCollection<Document> collection =
                                database.getCollection(prefix + collectionName);
                        Bson filter = filter(find);

                        if (isNull(data)) {
                            StorageLogger.logSet(
                                    log,
                                    collectionName,
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
                                    collectionName,
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
                        StorageLogger.logError(log, "SET", collectionName, e);
                        return Result.FAILED;
                    }
                });
    }

    @Override
    public @NonNull CompletableFuture<List<JsonObject>> get(
            @NotNull String collectionName, @NotNull JsonObject find) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<JsonObject> result = new ArrayList<>();
                    try {
                        MongoCollection<Document> collection =
                                database.getCollection(prefix + collectionName);
                        Bson filter = filter(find);
                        StorageLogger.logGet(log, collectionName, filter.toBsonDocument().toJson());

                        for (Document document : collection.find(filter)) {
                            if (document != null && !document.isEmpty()) {
                                result.add(
                                        JsonParser.parseString(document.toJson())
                                                .getAsJsonObject());
                            }
                        }
                    } catch (Exception e) {
                        StorageLogger.logError(log, "GET", collectionName, e);
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

    @Override
    public void close() {
        if (client != null) {
            client.close();
            log.info("MongoDB disconnected");
        }
    }
}
