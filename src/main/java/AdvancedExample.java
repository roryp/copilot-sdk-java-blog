import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced Copilot SDK Example
 * 
 * Demonstrates complex behaviors:
 * - Multi-turn conversations with context retention
 * - Structured output requests (JSON formatting)
 * - Code generation and analysis
 * - Streaming response handling with progress tracking
 * - Error handling
 * - Conversation history management
 */
public class AdvancedExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Advanced Copilot SDK Demo ===\n");
        
        try (var client = new CopilotClient()) {
            client.start().get();
            
            // Demo 1: Code Review Assistant
            runCodeReviewDemo(client);
            
            // Demo 2: Multi-turn Conversation
            runMultiTurnConversation(client);
            
            // Demo 3: Structured Output (JSON)
            runStructuredOutputDemo(client);
            
            // Demo 4: Code Generation with Context
            runCodeGenerationDemo(client);
            
            System.out.println("\n=== All Demos Complete ===");
        }
    }

    /**
     * Demo 1: Code Review with detailed analysis
     */
    private static void runCodeReviewDemo(CopilotClient client) throws Exception {
        System.out.println("\n--- Demo 1: Code Review Assistant ---\n");
        
        var session = client.createSession(
            new SessionConfig().setModel("claude-opus-4.5")
        ).get();

        String codeToReview = """
            public class UserService {
                private Map<String, User> users = new HashMap<>();
                
                public User getUser(String id) {
                    return users.get(id);
                }
                
                public void saveUser(User user) {
                    users.put(user.getId(), user);
                }
                
                public void deleteUser(String id) {
                    users.remove(id);
                }
            }
            """;

        String prompt = String.format("""
            You are an expert Java developer. Review this Java code for:
            1. Thread safety issues
            2. Null safety concerns
            3. Missing validation
            4. Suggested improvements
            
            Code:
            ```java
            %s
            ```
            
            Provide a brief, actionable review.
            """, codeToReview);

        streamResponse(session, prompt);
    }

    /**
     * Demo 2: Multi-turn conversation with context retention
     */
    private static void runMultiTurnConversation(CopilotClient client) throws Exception {
        System.out.println("\n--- Demo 2: Multi-turn Conversation ---\n");
        
        var session = client.createSession(
            new SessionConfig().setModel("claude-opus-4.5")
        ).get();

        // First turn - establish context
        System.out.println("[User] What is the Factory Pattern?");
        streamResponse(session, "Explain the Factory Pattern in Java in 2-3 sentences. Be concise.");
        
        // Second turn - builds on previous answer
        System.out.println("\n[User] Show me a simple example.");
        streamResponse(session, "Show a minimal Java code example of the Factory Pattern. Keep it under 15 lines.");
        
        // Third turn - continues conversation context
        System.out.println("\n[User] When should I use this?");
        streamResponse(session, "Give 3 real-world scenarios where Factory Pattern is useful. One line each.");
    }

    /**
     * Demo 3: Request structured JSON output
     */
    private static void runStructuredOutputDemo(CopilotClient client) throws Exception {
        System.out.println("\n--- Demo 3: Structured JSON Output ---\n");
        
        var session = client.createSession(
            new SessionConfig().setModel("claude-opus-4.5")
        ).get();

        String prompt = """
            Output ONLY raw JSON. No markdown fences, no explanation, no text before or after.
            
            Analyze this tech stack and return exactly this structure:
            {"stack":[...],"strengths":[...],"concerns":[...],"recommendation":"..."}
            
            Tech stack: Spring Boot 3, PostgreSQL, Redis, Docker, Kubernetes
            """;

        System.out.println("[Requesting structured analysis...]\n");
        streamResponse(session, prompt);
    }

    /**
     * Demo 4: Code generation with specific requirements
     */
    private static void runCodeGenerationDemo(CopilotClient client) throws Exception {
        System.out.println("\n--- Demo 4: Code Generation ---\n");
        
        var session = client.createSession(
            new SessionConfig().setModel("claude-opus-4.5")
        ).get();

        String prompt = """
            Generate a complete Java record called ApiResponse<T> for REST APIs with:
            - Generic type T for data payload
            - boolean success field
            - String errorMessage (nullable)
            - Instant timestamp
            - Static factory: success(T data) and error(String message)
            
            Output the full Java code. Use Java 17+ features. Add a 3-line usage example in comments.
            """;

        streamResponse(session, prompt);
    }

    /**
     * Helper method to stream and display responses with progress tracking
     */
    private static void streamResponse(CopilotSession session, String prompt) throws Exception {
        var done = new CompletableFuture<Void>();
        var tokenCount = new AtomicInteger(0);
        
        session.on(evt -> {
            if (evt instanceof AssistantMessageEvent msg) {
                String content = msg.getData().getContent();
                tokenCount.incrementAndGet();
                System.out.print(content);
            } else if (evt instanceof SessionErrorEvent err) {
                System.err.println("\n[Error: " + err.getData().getMessage() + "]");
                done.completeExceptionally(new RuntimeException(err.getData().getMessage()));
            } else if (evt instanceof SessionIdleEvent) {
                done.complete(null);
            }
        });

        session.send(new MessageOptions().setPrompt(prompt)).get();
        done.get();
        
        System.out.println("\n[Chunks received: " + tokenCount.get() + "]");
    }
}
