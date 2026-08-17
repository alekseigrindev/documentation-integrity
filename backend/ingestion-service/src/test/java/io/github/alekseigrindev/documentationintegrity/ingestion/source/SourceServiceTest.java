package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
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

    @InjectMocks
    private SourceService sourceService;

    @Test
    void registerReturnsExistingSourceWhenConcurrentInsertWins() {
        URI authorityUrl = URI.create("https://docs.github.com");

        SourceRegistration registration = new SourceRegistration(
                "GitHub Docs",
                authorityUrl,
                "CC BY 4.0",
                URI.create("https://creativecommons.org/licenses/by/4.0/"),
                URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
        );

        Source existingSource = new Source(
                UUID.randomUUID(),
                "GitHub Docs",
                authorityUrl,
                "CC BY 4.0",
                URI.create("https://creativecommons.org/licenses/by/4.0/"),
                URI.create("https://docs.github.com/en/site-policy/github-terms/github-terms-of-service")
        );

        when(sourceRepository.findByAuthorityUrl(authorityUrl.toString()))
                .thenReturn(Optional.empty(), Optional.of(existingSource));
        when(sourceRepository.insertIfAbsent(any(Source.class))).thenReturn(0);

        SourceRegistrationResult result = sourceService.register(registration);

        assertThat(result.source()).isEqualTo(existingSource);
        assertThat(result.created()).isFalse();
        verify(sourceRepository, times(2)).findByAuthorityUrl(authorityUrl.toString());
    }

}