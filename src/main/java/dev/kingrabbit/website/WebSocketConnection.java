package dev.kingrabbit.website;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketConnection extends TimerTask {

    public static final Gson GSON = new Gson();
    public static final Timer TIMER = new Timer("ConnectionsTimer");
    public static final Random RANDOM = new Random();

    @Getter
    final WebSocketSession session;

    @Getter
    final String id;

    @Getter
    @Setter
    long lastPing;

    @Getter
    @Setter
    boolean requestedPing;

    @Getter
    String uuid = null;

    public WebSocketConnection(WebSocketSession session, String id) {
        this.session = session;
        this.id = id;
        this.lastPing = System.currentTimeMillis();
        this.requestedPing = false;

        log("Opened connection, awaiting handshake.");

        TIMER.schedule(this, 0, 1000);
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        if (uuid == null)
            if (currentTimeMillis - 5000 > lastPing)
                close("not authenticating");
        if (!requestedPing) {
            if (currentTimeMillis - 30000 > lastPing)
                ping();
        } else {
            if (currentTimeMillis - 40000 > lastPing) {
                close("missed ping");
            }
        }
    }

    public void sendRaw(String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String type) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        sendRaw(GSON.toJson(object));
    }

    public void send(String type, String key, String property) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.addProperty(key, property);
        sendRaw(GSON.toJson(object));
    }

    public void send(String type, String key, JsonArray array) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.add(key, array);
        sendRaw(GSON.toJson(object));
    }

    public void ping() {
        log("Pinging");
        send("ping");
        requestedPing = true;
    }

    public void pinged() {
        log("Received ping from");
        lastPing = System.currentTimeMillis();
        requestedPing = false;
    }

    public void handshake(String key) {
        log("Received handshake using key '" + key + "'");
        if (key == null || key.isBlank()) {
            close("Invalid authentication key.");
            return;
        }

        HttpUtil.sendGetRequestAsync("https://mcbunnyfarm.org/api/key", new String[]{"Api-Key:" + key}).thenAccept(response -> {
            log("Received response to key lookup " + response);
            JsonElement keyInformationRaw = JsonParser.parseString(response);
            if (!keyInformationRaw.isJsonObject()) {
                close("Error validating authentication key.");
                return;
            }
            JsonObject keyInformation = keyInformationRaw.getAsJsonObject();
            if (!keyInformation.has("success")) {
                close("Error validating authentication key.");
                return;
            }
            if (!keyInformation.get("success").getAsBoolean()) {
                close("Invalid authentication key.");
                return;
            }
            if (!keyInformation.has("uuid")) {
                close("Error validating authentication key.");
                return;
            }
            uuid = keyInformation.get("uuid").getAsString();
            log("User authenticated with uuid " + uuid);
            WebSocketManager.getInstance().updateConnectedPlayers();
            ping();
        });

    }

    public void data(JsonObject json) {
        log("Received data '" + GSON.toJson(json) + "' from");
        JsonObject data = json.get("data").getAsJsonObject();
        String dataType = json.get("dataType").getAsString();
        log("Parsed to '" + GSON.toJson(data) + "' from");

    }

    public void log(String message) {
        System.out.println(message + " connection with id '" + id + "'");
    }

    public void close(String reason) {
        try {
            System.out.println("Closing connection with id '" + id + "' due to " + reason);
            send("disconnect", "reason", reason);
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        log("Closed");
        cancel();
        WebSocketManager.getInstance().removeConnection(this);
        WebSocketManager.getInstance().updateConnectedPlayers();
    }

}
