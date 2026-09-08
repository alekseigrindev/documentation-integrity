package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import org.springframework.stereotype.Service;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LocalDirectoryPicker {

    public Optional<URI> choose() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Choosing a directory requires a local desktop session."
            );
        }

        AtomicReference<File> selectedDirectory = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Choose documentation directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedDirectory.set(chooser.getSelectedFile());
                }
            });
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Directory selection was interrupted.",
                    exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Unable to open the local directory picker.",
                    exception.getCause()
            );
        }

        return Optional.ofNullable(selectedDirectory.get())
                .map(File::toPath)
                .map(path -> path.toAbsolutePath().normalize().toUri());
    }
}
