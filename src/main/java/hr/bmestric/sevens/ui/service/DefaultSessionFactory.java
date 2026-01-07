package hr.bmestric.sevens.ui.service;

import hr.bmestric.sevens.engine.interfaces.IGameEngine;
import hr.bmestric.sevens.model.Player;
import hr.bmestric.sevens.network.chat.interfaces.IChatService;
import hr.bmestric.sevens.network.rmi.interfaces.IRemoteGameEngine;
import hr.bmestric.sevens.session.interfaces.IChatSession;
import hr.bmestric.sevens.session.interfaces.IGameSession;
import hr.bmestric.sevens.session.*;

public class DefaultSessionFactory implements ISessionFactory {
    @Override
    public IGameSession createLocalGameSession(IGameEngine engine) {
        return new LocalGameSession(engine);
    }

    @Override
    public IGameSession createRmiGameSession(IRemoteGameEngine remoteEngine, String clientId) {
        return new RmiGameSession(remoteEngine, clientId);
    }

    @Override
    public IChatSession createRmiChatSession(IChatService chatService, Player player) {
        return new RmiChatSession(chatService, player.getId());
    }
}
