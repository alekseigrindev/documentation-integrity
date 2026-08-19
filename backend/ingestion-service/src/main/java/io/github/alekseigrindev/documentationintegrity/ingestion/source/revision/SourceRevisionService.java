package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRevisionRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.LocalCheckoutDocumentReader;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceRevisionService {

    private static final String LOCAL_CHECKOUT = "LOCAL_CHECKOUT";

    private final SourceRevisionRepository sourceRevisionRepository;
    private final SourceRepository sourceRepository;
    private final LocalCheckoutDocumentReader checkoutReader;
    private final Clock clock;

    @Transactional
    public SourceRevisionRegistrationResult register(UUID sourceId, SourceRevisionRegistration registration) {
        return sourceRevisionRepository.findBySourceIdAndVersionIdentifier(sourceId, registration.versionIdentifier())
                .map(revision -> new SourceRevisionRegistrationResult(revision, false))
                .orElseGet(() -> createSourceRevision(sourceId, registration));
    }

    private SourceRevisionRegistrationResult createSourceRevision(UUID sourceId, SourceRevisionRegistration registration) {
        if (!sourceRepository.existsById(sourceId)) {
            throw new EntityNotFoundException("Source was not found: " + sourceId);
        }

        checkoutReader.verifyAccessible();

        SourceRevision sourceRevision = new SourceRevision(
                UUID.randomUUID(),
                sourceId,
                registration.versionIdentifier(),
                LOCAL_CHECKOUT,
                clock.instant()
        );

        int inserted = sourceRevisionRepository.insertIfAbsent(sourceRevision);
        if (inserted == 1) {
            return new SourceRevisionRegistrationResult(sourceRevision, true);
        }

        SourceRevision existingSourceRevision = sourceRevisionRepository
                .findBySourceIdAndVersionIdentifier(sourceId, registration.versionIdentifier())
                .orElseThrow(() -> new IllegalStateException("Source revision was not created"));

        return new SourceRevisionRegistrationResult(existingSourceRevision, false);
    }
}
