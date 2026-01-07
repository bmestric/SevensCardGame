package hr.bmestric.sevens.network.chat.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChatService extends Remote {
    void sendMessage(String fromPlayerId, String fromPlayerName, String message) throws RemoteException;
    void registerChatListener(String playerId, IChatListener listener) throws RemoteException;
    void unregisterChatListener(String playerId) throws RemoteException;
}
