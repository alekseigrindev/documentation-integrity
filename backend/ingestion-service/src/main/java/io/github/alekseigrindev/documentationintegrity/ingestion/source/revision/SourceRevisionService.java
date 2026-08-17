package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRevisionRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceRevisionService {

    private final SourceRevisionRepository sourceRevisionRepository;

    @Transactional
    public SourceRevisionRegistrationResult register(UUID sourceId, SourceRevisionRegistration registration) {
        return sourceRevisionRepository.findBySourceIdAndVersionIdentifier(sourceId, registration.versionIdentifier())
                .map(revision -> new SourceRevisionRegistrationResult(revision, false))
                .orElseGet(() -> createSourceRevision(sourceId, registration));
    }

    private SourceRevisionRegistrationResult createSourceRevision(UUID sourceId, SourceRevisionRegistration registration) {
        SourceRevision sourceRevision = new SourceRevision(
                UUID.randomUUID(),
                sourceId,
                registration.versionIdentifier(),
                registration.acquisitionMethod(),
                registration.acquiredAt(),
                registration.integrityHash()
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
