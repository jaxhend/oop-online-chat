package Client;

import org.snf4j.core.EndingAction;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.DefaultSessionConfig;
import org.snf4j.core.session.ISessionConfig;

import java.nio.charset.StandardCharsets;

public class ChatClientHandler extends AbstractStreamHandler {

    @Override
    public void read(Object msg) {
        System.out.println(new String((byte[])msg, StandardCharsets.UTF_8));
    }

    @Override
    public void event(SessionEvent event) {
        if (event == SessionEvent.CLOSED) {
            // Ühendus sulgub.
            if (!getSession().getAttributes().containsKey(ChatClient.BYE_TYPED)) {
                System.out.println("Connection closed. Type \"bye\" to exit");
            }
        }
    }

    @Override
    public ISessionConfig getConfig() {
        // Selleks, et SelectorLoop oleks turvaliselt suletud.
        return new DefaultSessionConfig()
                .setEndingAction(EndingAction.STOP);
    }

}