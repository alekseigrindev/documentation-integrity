package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;

    @Transactional
    public SourceRegistrationResult register(SourceRegistration registration) {
        return sourceRepository.findByAuthorityUrl(registration.authorityUrl().toString())
                .map(source -> new SourceRegistrationResult(source, false))
                .orElseGet(() -> createSource(registration));
    }

    private SourceRegistrationResult createSource(SourceRegistration registration) {
        Source source = new Source(
                UUID.randomUUID(),
                registration.name(),
                registration.authorityUrl(),
                registration.licenseName(),
                registration.licenseUrl(),
                registration.accessPolicyUrl()
        );

        int inserted = sourceRepository.insertIfAbsent(source);

        if (inserted == 1) {
            return new SourceRegistrationResult(source, true);
        }

        Source existingSource = sourceRepository.findByAuthorityUrl(registration.authorityUrl().toString())
                .orElseThrow(() -> new IllegalStateException("Source was not found after registration."));

        return new SourceRegistrationResult(existingSource, false);
    }
}
