# Introducing the Copilot SDK for Java

Ever wanted to call GitHub Copilot from your Java code? Now you can.

The **Copilot SDK for Java** is a community-built SDK that gives you programmatic access to GitHub Copilot via the **GitHub Copilot CLI**, so you can build AI-powered tools, assistants, and agentic workflows—entirely in Java.

> **Important:** This is a community project, not an official GitHub product.

At a high level, your Java app talks to this SDK, and the SDK drives the Copilot CLI for you. That means you can embed Copilot-style experiences inside your own tools while staying close to the official Copilot ecosystem.

## Quick start

### Prerequisites

Before you write a line of code, make sure your machine is ready.

You’ll need **Java 17+**, plus the **GitHub Copilot CLI** installed and accessible on your `PATH` (or you can point the SDK at it using a custom `cliPath`). You’ll also need an active Copilot entitlement, because the CLI won’t run without one.

#### Step 1: Install Node.js

The Copilot CLI is distributed via npm, so you'll need **Node.js 22+** installed.

- **Windows:** Download and install from [nodejs.org](https://nodejs.org/) or use `winget install OpenJS.NodeJS`
- **macOS:** Use Homebrew: `brew install node`
- **Linux:** Use your package manager or [NodeSource](https://github.com/nodesource/distributions)

Verify Node.js is installed:

```bash
node --version
# Should output v22.x.x or higher
```

#### Step 2: Install the Copilot CLI

Install or upgrade the Copilot CLI globally via npm:

```bash
npm install -g @github/copilot
# or to upgrade
npm update -g @github/copilot
```

Verify:

```bash
copilot --version
```

#### Step 3: Authenticate with GitHub

On first use, the Copilot CLI will prompt you to authenticate. Run any Copilot command to trigger the login flow:

```bash
copilot --help
```

If you're not authenticated, it will open a browser window for GitHub OAuth. Complete the authentication, and you're ready to go.

> **Tip:** If you're behind a corporate proxy or firewall, ensure `copilot` can reach GitHub's APIs. You may need to configure `HTTP_PROXY` or `HTTPS_PROXY` environment variables.

### Add the dependency

If you’re using Maven:

```xml
<dependency>
  <groupId>io.github.copilot-community-sdk</groupId>
  <artifactId>copilot-sdk</artifactId>
  <version>1.0.5</version>
</dependency>
```

Or Gradle:

```groovy
implementation 'io.github.copilot-community-sdk:copilot-sdk:1.0.5'
```

### Your first “Copilot from Java” call

This example starts a client, opens a session, streams assistant messages via events, and exits once the session goes idle.

```java
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
```

Run it:

```bash
mvn compile exec:java -Dexec.mainClass="Example"
```

Or, if you prefer using the Maven wrapper (which doesn't require Maven to be installed globally):

```bash
# Add the wrapper to your project (one-time setup)
mvn wrapper:wrapper

# Run with the wrapper
./mvnw compile exec:java -Dexec.mainClass="Example"
```

And you should see output like:

```text
2+2 equals 4.
```

That’s it—you just called Copilot from Java.

## Prefer “zero project setup”? Use JBang

If you don’t want to create a Maven or Gradle project just to try the SDK, you can run the repo’s example using [JBang](https://www.jbang.dev/). It’s the fastest “kick the tires” option when you’re experimenting or demoing.

```bash
jbang jbang_example.java
```

JBang handles dependencies automatically, so you can go from copy/paste to running in a single command.

## What can you build?

If you can describe it as “Copilot, but inside my product,” it’s a good fit.

You can build internal coding assistants that understand your team’s conventions, automated PR reviewers that summarize changes and suggest improvements, test generators that align with your project patterns, documentation bots that stay in sync with the codebase, or agent-style workflows that can reason, plan, and execute across tools you define.

Already in production: the **JMeter Copilot Plugin** for AI-assisted load testing.  
https://github.com/brunoborges/jmeter-copilot-plugin

## Learn more

Documentation: https://copilot-community-sdk.github.io/copilot-sdk-java/  
API reference (Javadoc): https://copilot-community-sdk.github.io/copilot-sdk-java/apidocs/  
Source code: https://github.com/copilot-community-sdk/copilot-sdk-java

## Contributing

This is a community project, and contributions are welcome—PRs, bug reports, and feedback all help.

If you build something cool with the SDK, consider adding your project as an example so other Java developers can copy, learn, and ship faster.
