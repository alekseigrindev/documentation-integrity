package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.Publisher;
import io.github.alekseigrindev.documentationintegrity.ingestion.publisher.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceServiceTest {

    @Mock
    private SourceRepository sourceRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private SourceService sourceService;

    @Test
    void registerReturnsExistingSourceWhenConcurrentInsertWins() {
        String sourceKey = "github-docs";
        URI sourceUrl = URI.create("https://docs.github.com");
        Publisher publisher = new Publisher(
                UUID.randomUUID(),
                "GitHub Inc."
        );

        SourceRegistration registration = new SourceRegistration(
                publisher.getId(),
                ConnectorType.GITHUB,
                sourceKey,
                "GitHub Docs",
                "GitHub documentation source.",
                sourceUrl,
                "CC BY 4.0",
                URI.create("https://creativecommons.org/licenses/by/4.0/"),
                URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
        );

        Source existingSource = new Source(
                UUID.randomUUID(),
                publisher,
                ConnectorType.GITHUB,
                sourceKey,
                "GitHub Docs",
                "GitHub documentation source.",
                sourceUrl,
                "CC BY 4.0",
                URI.create("https://creativecommons.org/licenses/by/4.0/"),
                URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
        );

        when(publisherRepository.findById(publisher.getId()))
                .thenReturn(Optional.of(publisher));
        when(sourceRepository.findBySourceKey(sourceKey))
                .thenReturn(Optional.empty(), Optional.of(existingSource));
        when(sourceRepository.insertIfAbsent(any(Source.class))).thenReturn(0);

        SourceRegistrationResult result = sourceService.register(registration);

        assertThat(result.source()).isEqualTo(existingSource);
        assertThat(result.created()).isFalse();
        verify(sourceRepository, times(2)).findBySourceKey(sourceKey);
    }
}
