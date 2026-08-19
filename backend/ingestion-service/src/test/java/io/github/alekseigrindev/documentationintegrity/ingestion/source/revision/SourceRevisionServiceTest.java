package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRevisionRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.importing.LocalCheckoutDocumentReader;
import io.github.alekseigrindev.documentationintegrity.ingestion.source.SourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceRevisionServiceTest {

    @Mock
    private SourceRevisionRepository sourceRevisionRepository;

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private LocalCheckoutDocumentReader checkoutReader;

    @Mock
    private Clock clock;

    @InjectMocks
    private SourceRevisionService sourceRevisionService;

    @Test
    void registerReturnsExistingRevisionWhenConcurrentInsertWins() {
        UUID sourceId = UUID.randomUUID();
        String versionIdentifier = "0123456789abcdef0123456789abcdef01234567";
        Instant acquiredAt = Instant.parse("2026-08-17T00:00:00Z");

        SourceRevisionRegistration registration = new SourceRevisionRegistration(
                versionIdentifier
        );

        SourceRevision existingRevision = new SourceRevision(
                UUID.randomUUID(),
                sourceId,
                versionIdentifier,
                "LOCAL_CHECKOUT",
                acquiredAt
        );

        when(sourceRevisionRepository.findBySourceIdAndVersionIdentifier(
                sourceId,
                versionIdentifier
        )).thenReturn(Optional.empty(), Optional.of(existingRevision));

        when(sourceRepository.existsById(sourceId)).thenReturn(true);
        when(clock.instant()).thenReturn(acquiredAt);
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
        verify(checkoutReader).verifyAccessible();
    }

    @Test
    void registerGeneratesAcquisitionMetadataOnTheServer() {
        UUID sourceId = UUID.randomUUID();
        String versionIdentifier = "fixture-workflow-permissions-v1";
        Instant acquiredAt = Instant.parse("2026-08-19T12:00:00Z");

        when(sourceRevisionRepository.findBySourceIdAndVersionIdentifier(
                sourceId,
                versionIdentifier
        )).thenReturn(Optional.empty());
        when(sourceRepository.existsById(sourceId)).thenReturn(true);
        when(clock.instant()).thenReturn(acquiredAt);
        when(sourceRevisionRepository.insertIfAbsent(any(SourceRevision.class)))
                .thenReturn(1);

        SourceRevisionRegistrationResult result = sourceRevisionService.register(
                sourceId,
                new SourceRevisionRegistration(versionIdentifier)
        );

        assertThat(result.created()).isTrue();
        assertThat(result.sourceRevision().getAcquisitionMethod())
                .isEqualTo("LOCAL_CHECKOUT");
        assertThat(result.sourceRevision().getAcquiredAt()).isEqualTo(acquiredAt);
        verify(checkoutReader).verifyAccessible();
    }

}
