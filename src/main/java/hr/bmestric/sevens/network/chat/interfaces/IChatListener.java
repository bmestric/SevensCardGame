package hr.bmestric.sevens.network.chat.interfaces;

import java.rmi.Remote;

public interface IChatListener extends Remote {
    void onMessageReceived(String fromPlayerName, String message, String timestamp) throws Exception;
}
