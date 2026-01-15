package hr.bmestric.sevens.session.interfaces;

import java.io.Closeable;
import java.io.IOException;

public interface IChatSession extends Closeable {

    void sendMessage(String fromPlayerId, String fromPlayerName, String message) throws IOException;

    void setMessageListener(MessageListener listener);

    @Override
    void close();

    @FunctionalInterface
    interface MessageListener {
        void onMessage(String fromPlayerName, String message, String timestamp);
    }
}
