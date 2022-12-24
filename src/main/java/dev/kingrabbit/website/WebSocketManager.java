package dev.kingrabbit.website;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class WebSocketManager {
    private static WebSocketManager instance;
    private final HashMap<String, WebSocketConnection> connections = new HashMap<>();

    public WebSocketManager() {
        instance = this;
    }

    public static WebSocketManager getInstance() {
        return instance;
    }

    public WebSocketConnection getConnection(WebSocketSession session) {
        String id = session.getId();
        if (connections.containsKey(id)) return connections.get(id);
        WebSocketConnection connection = new WebSocketConnection(session, id);
        connections.put(id, connection);
        return connection;
    }

    public Collection<WebSocketConnection> getConnections() {
        return connections.values();
    }

    public void removeConnection(WebSocketConnection webSocketConnection) {
        connections.remove(webSocketConnection.id);
    }

    /**
     * Send packets to all authenticated players with a list of all players using the BunnyUtils mod
     */
    public void updateConnectedPlayers() {
        System.out.println("Updating connected players");

        List<WebSocketConnection> connectedPlayers = new ArrayList<>();
        JsonArray uuids = new JsonArray();
        connections.values().forEach(webSocketConnection -> {
            if (webSocketConnection.uuid != null) {
                connectedPlayers.add(webSocketConnection);
                uuids.add(webSocketConnection.uuid);
            }
        });
        JsonObject object = new JsonObject();
        object.addProperty("type", "connected_players");
        object.add("players", uuids);
        String packet = WebSocketConnection.GSON.toJson(object);
        for (WebSocketConnection connection : connectedPlayers) {
            connection.sendRaw(packet);
        }
    }

}
