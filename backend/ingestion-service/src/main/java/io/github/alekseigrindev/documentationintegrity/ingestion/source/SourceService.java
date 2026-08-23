package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.Publisher;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Source source = new Source(
                UUID.randomUUID(),
                publisher,
                registration.connectorType(),
                sourceKey,
                registration.name(),
                registration.description(),
                registration.sourceUrl(),
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
}
