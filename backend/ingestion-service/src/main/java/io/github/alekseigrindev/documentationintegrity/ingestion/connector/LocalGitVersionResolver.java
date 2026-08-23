package io.github.alekseigrindev.documentationintegrity.ingestion.connector;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Returns a commit only when the acquired file exactly matches tracked Git state.
 */
@Component
public class LocalGitVersionResolver {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public Optional<String> resolve(Path configuredRoot, Path documentPath) {
        CommandResult topLevel = run(configuredRoot, "rev-parse", "--show-toplevel");
        if (topLevel.exitCode() != 0) {
            return Optional.empty();
        }

        Path workTree = Path.of(topLevel.output()).toAbsolutePath().normalize();
        Path normalizedDocument = documentPath.toAbsolutePath().normalize();
        if (!normalizedDocument.startsWith(workTree)) {
            return Optional.empty();
        }

        String relativeDocument = workTree.relativize(normalizedDocument).toString();
        if (run(workTree, "ls-files", "--error-unmatch", "--", relativeDocument)
                .exitCode() != 0) {
            return Optional.empty();
        }
        if (run(workTree, "diff", "--quiet", "HEAD", "--", relativeDocument)
                .exitCode() != 0) {
            return Optional.empty();
        }
        if (run(workTree, "diff", "--cached", "--quiet", "HEAD", "--", relativeDocument)
                .exitCode() != 0) {
            return Optional.empty();
        }

        CommandResult head = run(workTree, "rev-parse", "--verify", "HEAD");
        String version = head.output();
        if (head.exitCode() != 0 || !version.matches("[0-9a-fA-F]{40,64}")) {
            throw new IllegalStateException("Git returned an invalid commit identifier.");
        }

        return Optional.of(version.toLowerCase(Locale.ROOT));
    }

    private CommandResult run(Path directory, String... arguments) {
        String[] command = new String[arguments.length + 3];
        command[0] = "git";
        command[1] = "-C";
        command[2] = directory.toString();
        System.arraycopy(arguments, 0, command, 3, arguments.length);

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            if (!process.waitFor(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException(
                        "Timed out while reading local Git provenance."
                );
            }

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();
            return new CommandResult(process.exitValue(), output);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while reading local Git provenance.",
                    exception
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Unable to execute Git for the configured local directory.",
                    exception
            );
        }
    }

    private record CommandResult(int exitCode, String output) {
    }
}
