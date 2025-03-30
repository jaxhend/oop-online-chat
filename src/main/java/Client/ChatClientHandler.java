
package Client;

import org.snf4j.core.EndingAction;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.DefaultSessionConfig;
import org.snf4j.core.session.ISessionConfig;

public class ChatClientHandler extends AbstractStreamHandler {

    @Override
    public void read(Object msg) {
        System.err.println(new String((byte[])msg));
    }

    @Override
    public void event(SessionEvent event) {
        if (event == SessionEvent.CLOSED) {

            // Notify if the closing initiated by the server
            if (!getSession().getAttributes().containsKey(ChatClient.BYE_TYPED)) {
                System.err.println("Connection closed. Type \"bye\" to exit");
            }
        }
    }

    @Override
    public ISessionConfig getConfig() {

        // Gently stop the selector loop if session associated
        // with this handler ends
        return new DefaultSessionConfig()
                .setEndingAction(EndingAction.STOP);
    }

}