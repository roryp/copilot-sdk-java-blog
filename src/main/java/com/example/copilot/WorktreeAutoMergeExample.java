package com.example.copilot;

import com.github.copilot.sdk.*;
import com.github.copilot.sdk.events.*;
import com.github.copilot.sdk.json.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Worktree Auto-Merge Workflow
 *
 * Demonstrates using Git worktrees + Copilot SDK to:
 *   1. Create a Git worktree for a feature branch
 *   2. Use Copilot to generate code in the worktree
 *   3. Use Copilot to review the generated code
 *   4. Commit, merge back into main, and clean up
 */
public class WorktreeAutoMergeExample {

    private static final Path REPO_ROOT = Path.of(".").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        var featureBranch = "feature/copilot-generated-" + System.currentTimeMillis();
        var worktreePath = REPO_ROOT.resolve("../wt-" + featureBranch.replace("/", "-"));

        System.out.println("=== Worktree Auto-Merge Workflow ===\n");
        System.out.println("Repo root:      " + REPO_ROOT);
        System.out.println("Feature branch: " + featureBranch);
        System.out.println("Worktree path:  " + worktreePath);
        System.out.println();

        try (var client = new CopilotClient()) {
            client.start().get();

            // Step 1: Create feature branch and worktree
            System.out.println("[1/6] Creating feature branch and worktree...");
            git("branch", featureBranch);
            git("worktree", "add", worktreePath.toString(), featureBranch);

            // Step 2: Ask Copilot to generate code
            System.out.println("[2/6] Asking Copilot to generate code...");
            var generatedCode = generateCodeWithCopilot(client, """
                Generate a Java utility class called StringUtils in package com.example.copilot
                with these static methods:
                - reverse(String s) — reverses a string, handles null by returning null
                - isPalindrome(String s) — checks if string is a palindrome (case-insensitive)
                - truncate(String s, int maxLen) — truncates with "..." if longer than maxLen
                Use Java 21 features. No external dependencies.
                Output ONLY the raw Java source code. No markdown fences, no explanation.
                """);

            // Step 3: Write generated code into the worktree
            System.out.println("[3/6] Writing generated code to worktree...");
            var targetFile = worktreePath.resolve(
                "src/main/java/com/example/copilot/StringUtils.java");
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, generatedCode);
            System.out.println("       Wrote " + generatedCode.length() + " chars to " + targetFile.getFileName());

            // Step 4: Ask Copilot to review the code
            System.out.println("[4/6] Asking Copilot to review the generated code...");
            var review = reviewCodeWithCopilot(client, generatedCode);
            System.out.println("\n--- Code Review ---");
            System.out.println(review);
            System.out.println("--- End Review ---\n");

            // Step 5: Commit in the worktree
            System.out.println("[5/6] Committing changes in worktree...");
            gitInDir(worktreePath, "add", "-A");
            gitInDir(worktreePath, "commit", "-m",
                "feat: add Copilot-generated StringUtils utility class");

            // Step 6: Merge feature branch into main
            System.out.println("[6/6] Merging " + featureBranch + " into main...");
            git("merge", "--no-ff", featureBranch, "-m",
                "Merge " + featureBranch + " (auto-merge via Copilot workflow)");

            System.out.println("\n=== Merge Complete ===");
        } finally {
            // Clean up: remove worktree and delete feature branch
            System.out.println("\nCleaning up worktree and branch...");
            try {
                git("worktree", "remove", worktreePath.toString(), "--force");
            } catch (RuntimeException e) {
                System.err.println("Warning: could not remove worktree: " + e.getMessage());
            }
            try {
                git("branch", "-d", featureBranch);
            } catch (RuntimeException e) {
                System.err.println("Warning: could not delete branch: " + e.getMessage());
            }
            System.out.println("Done.");
        }
    }

    /**
     * Uses Copilot to generate code, returning the full streamed response as a String.
     */
    private static String generateCodeWithCopilot(CopilotClient client, String prompt)
            throws Exception {
        var session = client.createSession(
            new SessionConfig()
                .setModel("claude-sonnet-4.5")
                .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                .setSystemMessage(new SystemMessageConfig()
                    .setMode(SystemMessageMode.APPEND)
                    .setContent("""
                        <rules>
                        - You are a code generator. Output ONLY valid Java source code.
                        - No explanations, no markdown fences, no commentary.
                        </rules>
                        """))
        ).get();

        return collectResponse(session, prompt);
    }

    /**
     * Uses Copilot to review code and return a concise verdict.
     */
    private static String reviewCodeWithCopilot(CopilotClient client, String code)
            throws Exception {
        var session = client.createSession(
            new SessionConfig()
                .setModel("claude-sonnet-4.5")
                .setOnPermissionRequest(PermissionHandler.APPROVE_ALL)
                .setSystemMessage(new SystemMessageConfig()
                    .setMode(SystemMessageMode.APPEND)
                    .setContent("""
                        <rules>
                        - You are a concise code reviewer.
                        - Reply LGTM if the code is correct, or list issues briefly.
                        - Focus on null safety, edge cases, and correctness.
                        </rules>
                        """))
        ).get();

        return collectResponse(session,
            "Review this Java code for correctness and edge cases:\n```java\n" + code + "\n```");
    }

    /**
     * Streams a Copilot session response and collects it into a single String.
     */
    private static String collectResponse(CopilotSession session, String prompt)
            throws Exception {
        var result = new StringBuilder();
        var done = new CompletableFuture<Void>();

        session.on(evt -> {
            if (evt instanceof AssistantMessageEvent msg) {
                result.append(msg.getData().content());
            } else if (evt instanceof SessionErrorEvent err) {
                done.completeExceptionally(new RuntimeException(err.getData().message()));
            } else if (evt instanceof SessionIdleEvent) {
                done.complete(null);
            }
        });

        session.send(new MessageOptions().setPrompt(prompt)).get();
        done.get();
        return result.toString();
    }

    // ── Git helpers ──────────────────────────────────────────────

    private static void git(String... args) throws IOException, InterruptedException {
        gitInDir(REPO_ROOT, args);
    }

    private static void gitInDir(Path dir, String... args)
            throws IOException, InterruptedException {
        var cmd = new String[args.length + 1];
        cmd[0] = "git";
        System.arraycopy(args, 0, cmd, 1, args.length);

        var process = new ProcessBuilder(cmd)
            .directory(dir.toFile())
            .inheritIO()
            .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                "git command failed (exit " + exitCode + "): " + String.join(" ", cmd));
        }
    }
}
