package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import hr.bmestric.sevens.session.interfaces.IChatSession;
import hr.bmestric.sevens.session.interfaces.IGameSession;

public interface ISessionFactory {
    IGameSession createLocalGameSession(IGameEngine engine);

    IGameSession createRmiGameSession(IRemoteGameEngine remoteEngine, String clientId);

    IChatSession createRmiChatSession(IChatService chatService, Player player);
}
