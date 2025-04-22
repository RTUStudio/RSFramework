package kr.rtuserver.framework.bukkit.api.storage.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import kr.rtuserver.framework.bukkit.api.storage.config.MongoDBConfig;
import kr.rtuserver.protoweaver.api.protocol.internal.StorageSync;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MongoDB implements Storage {

    private final RSPlugin plugin;
    private final MongoDBConfig config;

    private final Gson gson = new Gson();
    private final MongoClient client;
    private final MongoDatabase database;

    private final String prefix;

    public MongoDB(RSPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigurations().getMongodb();
        this.prefix = config.getTablePrefix();
        String serverHost = config.getHost() + ":" + config.getPort();
        // Replace the placeholder with your Atlas connection string
        String uri = "mongodb://";
        String username = config.getUsername();
        String password = config.getPassword();
        if (!username.isEmpty()) {
            uri += username;
            if (!password.isEmpty()) uri += ":" + password;
            uri += "@";
        }
        uri += serverHost;
        // Construct a ServerApi instance using the ServerApi.builder() method
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        this.client = MongoClients.create(settings);
        this.database = client.getDatabase(config.getDatabase());
    }

    private void debug(String type, String collection, String json) {
        plugin.verbose("[Storage] " + type + ": " + collection + " - " + json);
    }

    @Override
    public CompletableFuture<Boolean> add(@NotNull String collectionName, @NotNull JsonObject data) {
        return CompletableFuture.supplyAsync(() -> {
            if (data.isJsonNull()) return false;
            MongoCollection<Document> collection = database.getCollection(prefix + collectionName);
            Document document = Document.parse(gson.toJson(data));
            debug("ADD", collectionName, document.toJson());
            if (!collection.insertOne(document).wasAcknowledged()) return false;
            sync(collectionName, data);
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> set(@NotNull String collectionName, @Nullable Pair<String, Object> find, @Nullable JsonObject data) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(prefix + collectionName);
            if (find != null) {
                Bson filter;
                if (find.getValue() instanceof JsonObject jsonElement) {
                    filter = Filters.eq(find.getKey(), Document.parse(jsonElement.toString()));
                } else filter = Filters.eq(find.getKey(), (String) find.getValue());
                if (data == null || data.isJsonNull()) {
                    debug("SET", collectionName, filter.toBsonDocument().toJson());
                    DeleteResult result = collection.deleteMany(filter);
                    if (!result.wasAcknowledged()) return false;
                } else {
                    UpdateOptions options = new UpdateOptions().upsert(true);
                    BsonDocument update = new BsonDocument("$set", BsonDocument.parse(data.toString()));
                    BsonDocument debug = new BsonDocument()
                            .append("filter", filter.toBsonDocument())
                            .append("update", update.toBsonDocument());
                    debug("SET", collectionName, debug.toJson());
                    UpdateResult result = collection.updateMany(filter, update, options);
                    if (!result.wasAcknowledged()) return false;
                }
                sync(collectionName, find);
            } else {
                debug("SET", collectionName, Filters.empty().toBsonDocument().toJson());
                DeleteResult result = collection.deleteMany(Filters.empty());
                if (!result.wasAcknowledged()) return false;
                sync(collectionName, (@Nullable Pair<String, Object>) null);
            }
            return true;
        });
    }


    @Override
    public CompletableFuture<List<JsonObject>> get(@NotNull String collectionName, Pair<String, Object> find) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Document> collection = database.getCollection(prefix + collectionName);
            Bson filter = find != null ? Filters.eq(find.getKey(), find.getValue()) : Filters.empty();
            debug("GET", collectionName, filter.toBsonDocument().toJson());
            FindIterable<Document> documents = collection.find(filter);
            List<JsonObject> result = new ArrayList<>();
            for (Document document : documents) {
                if (document != null && !document.isEmpty()) {
                    result.add(JsonParser.parseString(document.toJson()).getAsJsonObject());
                }
            }
            return result;
        });
    }

    private void sync(@NotNull String table, @Nullable JsonObject find) {
        StorageSync sync = new StorageSync(plugin.getName(), table, find);
        plugin.getFramework().getProtoWeaver().sendPacket(sync);
    }

    private void sync(@NotNull String table, @Nullable Pair<String, Object> find) {
        StorageSync sync;
        if (find == null) sync = new StorageSync(plugin.getName(), table, null);
        else {
            String key = find.getKey();
            Object value = find.getValue();
            JsonObject json = new JsonObject();
            switch (value) {
                case JsonElement element -> json.add(key, element);
                case Number number -> json.addProperty(key, number);
                case Boolean bool -> json.addProperty(key, bool);
                case String str -> json.addProperty(key, str);
                case Character character -> json.addProperty(key, character);
                case null -> json.add(key, null);
                default -> throw new IllegalStateException("Unexpected value: " + value);
            }
            sync = new StorageSync(plugin.getName(), table, json);
        }
        plugin.getFramework().getProtoWeaver().sendPacket(sync);
    }

    @Override
    public void close() {
        client.close();
    }
}
