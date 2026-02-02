package com.example.copilot;

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.CompletableFuture;

public class Example {
    public static void main(String[] args) throws Exception {
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
    }
}
