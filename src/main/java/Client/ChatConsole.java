package Client;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.DumbTerminal;

public class ChatConsole {
    private final Terminal terminal;
    private final LineReader reader;

    public ChatConsole() throws Exception {
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter("/quit", "/leave", "/room", "/direct", "/members", "/chatrooms", "/help"));
        terminal = TerminalBuilder.builder()
                .name("Online Chat Console")
                .system(true)
                .build();
        reader = LineReaderBuilder
                .builder()
                .terminal(terminal)
                .completer(completer)
                .build();

        System.out.println();
        if (terminal instanceof DumbTerminal) {
            System.out.println("\033[0;31mKasutad IDE terminali. Kõik funktsioonid ei pruugi toimida. => Kasuta OS käsurida.\033[0m");
        }
        System.out.println("===== Online Chat Console =====");
    }

    public LineReader getReader() {
        return reader;
    }
}