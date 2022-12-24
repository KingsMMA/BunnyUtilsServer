package dev.kingrabbit.website;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println(message.getPayload() + " was received");
        WebSocketConnection connection = WebSocketManager.getInstance().getConnection(session);
        try {
            JsonObject data = (JsonObject) JsonParser.parseString(message.getPayload());
            switch (data.get("type").getAsString()) {
                case "ping" -> connection.pinged();
                case "handshake" -> connection.handshake(data.get("key").getAsString());
                case "data" -> connection.data(data);
                default -> System.out.println("Unknown payload from " + connection.uuid + " (ID: " + connection.id + ")");
            }
        } catch (Exception exception) {
            System.out.println("Exception occurred parsing json string");
            exception.printStackTrace();
            connection.send("error");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        WebSocketManager.getInstance().getConnection(session).close();
    }

}
