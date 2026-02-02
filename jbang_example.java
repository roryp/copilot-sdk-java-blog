///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS io.github.copilot-community-sdk:copilot-sdk:1.0.5

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.CompletableFuture;

public class jbang_example {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Copilot SDK via JBang...");
        
        try (var client = new CopilotClient()) {
            client.start().get();

            var session = client.createSession(
                new SessionConfig().setModel("claude-sonnet-4.5")
            ).get();

            var done = new CompletableFuture<Void>();
            session.on(evt -> {
                if (evt instanceof AssistantMessageEvent msg) {
                    System.out.println(msg.getData().getContent());
                } else if (evt instanceof SessionIdleEvent) {
                    done.complete(null);
                }
            });

            session.send(new MessageOptions().setPrompt("What is 2+2?")).get();
            done.get();
        }
        
        System.out.println("\nDone!");
    }
}
