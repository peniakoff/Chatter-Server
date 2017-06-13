package pl.tomaszmiller.websocket;

import org.springframework.web.socket.WebSocketSession;

/**
 * Created by Peniakoff on 13.06.2017.
 */
public class User {

    private WebSocketSession session;
    private String nick;

    public User(WebSocketSession session) {
        this.session = session;
        this.nick = "";
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

}
