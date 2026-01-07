package hr.bmestric.sevens.network.rmi.interfaces;

import hr.bmestric.sevens.model.GameState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IGameStateCallback  extends Remote {
    void onStateChanged(GameState gameState) throws RemoteException;
}
