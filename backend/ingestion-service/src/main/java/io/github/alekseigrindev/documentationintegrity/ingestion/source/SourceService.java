package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceUpdate;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.Publisher;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final PublisherRepository publisherRepository;

    @Transactional
    public SourceRegistrationResult register(SourceRegistration registration) {
        String sourceKey = registration.sourceKey().strip();
        Publisher publisher = publisherRepository.findById(registration.publisherId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found: " + registration.publisherId()
                ));

        return sourceRepository.findBySourceKey(sourceKey)
                .map(source -> new SourceRegistrationResult(source, false))
                .orElseGet(() -> createSource(registration, publisher, sourceKey));
    }

    private SourceRegistrationResult createSource(
            SourceRegistration registration,
            Publisher publisher,
            String sourceKey
    ) {
        URI sourceUrl = validateSourceUrl(
                registration.connectorType(),
                registration.sourceUrl()
        );
        Source source = new Source(
                UUID.randomUUID(),
                publisher,
                registration.connectorType(),
                sourceKey,
                registration.name(),
                registration.description(),
                sourceUrl,
                registration.licenseName(),
                registration.licenseUrl(),
                registration.accessPolicyUrl()
        );

        int inserted = sourceRepository.insertIfAbsent(source);

        if (inserted == 1) {
            return new SourceRegistrationResult(source, true);
        }

        Source existingSource = sourceRepository.findBySourceKey(sourceKey)
                .orElseThrow(() -> new IllegalStateException("Source was not found after registration."));

        return new SourceRegistrationResult(existingSource, false);
    }

    @Transactional(readOnly = true)
    public List<Source> findAll() {
        return sourceRepository.findAllByOrderByNameAscIdAsc();
    }

    @Transactional
    public Source update(UUID sourceId, SourceUpdate sourceUpdate) {
        String sourceKey = sourceUpdate.sourceKey().strip();

        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source not found: " + sourceId
                ));

        Publisher publisher = publisherRepository.findById(sourceUpdate.publisherId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found: " + sourceUpdate.publisherId()
                ));

        sourceRepository.findBySourceKey(sourceKey)
                .filter(existing -> !existing.getId().equals(sourceId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Source key is already in use: " + sourceKey
                    );
                });

        source.update(
                publisher,
                sourceUpdate.connectorType(),
                sourceKey,
                sourceUpdate.name(),
                validateSourceUrl(sourceUpdate.connectorType(), sourceUpdate.sourceUrl())
        );

        return source;
    }

    private URI validateSourceUrl(ConnectorType connectorType, URI sourceUrl) {
        if (connectorType != ConnectorType.LOCAL_DIRECTORY) {
            return sourceUrl;
        }

        if (sourceUrl == null || !"file".equalsIgnoreCase(sourceUrl.getScheme())) {
            throw new IllegalArgumentException(
                    "A local-directory Source requires a file URL."
            );
        }

        final Path localDirectory;
        try {
            localDirectory = Path.of(sourceUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Local-directory Source URL is not a valid file path: " + sourceUrl,
                    exception
            );
        }

        if (!Files.isDirectory(localDirectory) || !Files.isReadable(localDirectory)) {
            throw new IllegalArgumentException(
                    "Local-directory Source path is not an accessible directory: " + sourceUrl
            );
        }

        return sourceUrl;
    }
}
