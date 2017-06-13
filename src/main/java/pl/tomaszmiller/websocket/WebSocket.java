package pl.tomaszmiller.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import javax.sound.midi.SysexMessage;
import java.util.*;

/**
 * Created by Peniakoff on 12.06.2017.
 */
@Configuration
@EnableWebSocket
public class WebSocket extends BinaryWebSocketHandler implements WebSocketConfigurer { //wiadomości przesyłane binarnie

    private Map<String, User> sessionMap = Collections.synchronizedMap(new HashMap<String, User>());

    private List<String> badWords = Collections.synchronizedList(new ArrayList<String>(Arrays.asList("chuj", "kurwa", "spierdalaj", "wypierdalaj")));

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this, "/chat").setAllowedOrigins("*"); //w tym miejscu rejestrujemy end-pointy, do tego miejsca połączamy się z klienta, odniesienie do WebSocketHandlera
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {

        User userSending = sessionMap.get(session.getId());
        String messageConverted = censure(new String(message.getPayload().array()));

        if (messageConverted.startsWith("/")) {
            String[] command = messageConverted.split(" ");
            System.out.println(command[0]);
            switch (command[0].substring(1, command[0].length())) {
                case "addword": {
                    badWords.add(command[1]);
                    userSending.getSession().sendMessage(new BinaryMessage("Dodałeś nowe słowo do słownika.".getBytes("UTF-8")));
                    break;
                }
                case "changenick": {
                    if (command.length != 2) {
                        userSending.getSession().sendMessage(new BinaryMessage("Złe argumenty!".getBytes()));
                        break;
                    }
                    userSending.setNick(command[1]);
                    break;
                }
                default: {
                    userSending.getSession().sendMessage(new BinaryMessage("Nie znaleziono takiej komendy!.".getBytes()));
                    break;
                }
            }
            return;
        }

        if (userSending.getNick().isEmpty()) {
            userSending.setNick(messageConverted);
            userSending.getSession().sendMessage(new BinaryMessage(("Twój nick został ustawiony na: " + messageConverted).getBytes()));
        } else {
            for (User user : sessionMap.values()) {
                user.getSession().sendMessage(new BinaryMessage((userSending.getNick() + ": " + messageConverted).getBytes()));
            }
        }
    }

    private String censure(String message) {
        String changedMessage = message;
        for (String word : badWords) {
            if (message.contains(word)) {
                changedMessage = message.substring(0, 1);
                changedMessage += "***";
            }
        }
        return changedMessage;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionMap.put(session.getId(), new User(session));
        session.sendMessage(new BinaryMessage("Witaj!".getBytes()));
        session.sendMessage(new BinaryMessage("Twoja pierwsza wiadomość zostanie Twoim nickiem!".getBytes()));
        System.out.println("Zarejestrowano nowego użytkownika.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionMap.remove(session.getId());
        System.out.println("Wyrejestrowano użytkownika.");
    }
}
