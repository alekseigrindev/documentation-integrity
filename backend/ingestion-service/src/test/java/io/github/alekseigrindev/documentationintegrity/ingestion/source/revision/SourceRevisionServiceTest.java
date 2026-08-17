package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRevisionRegistration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceRevisionServiceTest {

    @Mock
    private SourceRevisionRepository sourceRevisionRepository;

    @InjectMocks
    private SourceRevisionService sourceRevisionService;
    @Test
    void registerReturnsExistingRevisionWhenConcurrentInsertWins() {
        UUID sourceId = UUID.randomUUID();
        String versionIdentifier = "0123456789abcdef0123456789abcdef01234567";

        SourceRevisionRegistration registration = new SourceRevisionRegistration(
                versionIdentifier,
                "PINNED_GIT_CHECKOUT",
                Instant.parse("2026-08-17T00:00:00Z"),
                "a".repeat(64)
        );

        SourceRevision existingRevision = new SourceRevision(
                UUID.randomUUID(),
                sourceId,
                versionIdentifier,
                "PINNED_GIT_CHECKOUT",
                Instant.parse("2026-08-17T00:00:00Z"),
                "a".repeat(64)
        );

        when(sourceRevisionRepository.findBySourceIdAndVersionIdentifier(
                sourceId,
                versionIdentifier
        )).thenReturn(Optional.empty(), Optional.of(existingRevision));

        when(sourceRevisionRepository.insertIfAbsent(any(SourceRevision.class)))
                .thenReturn(0);

        SourceRevisionRegistrationResult result = sourceRevisionService.register(
                sourceId,
                registration
        );

        assertThat(result.sourceRevision()).isEqualTo(existingRevision);
        assertThat(result.created()).isFalse();

        verify(sourceRevisionRepository, times(2))
                .findBySourceIdAndVersionIdentifier(sourceId, versionIdentifier);
    }

}
