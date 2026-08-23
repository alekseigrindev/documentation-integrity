package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;

    @Transactional
    public PublisherRegistrationResult register(PublisherRegistration publisherRegistration) {
        String normalizedName = publisherRegistration.name().strip();

        return publisherRepository.findByName(normalizedName)
                .map(publisher -> new PublisherRegistrationResult(publisher, false))
                .orElseGet(() -> createPublisher(normalizedName));
    }

    private PublisherRegistrationResult createPublisher(String name) {
        Publisher publisher = new Publisher(
                UUID.randomUUID(),
                name
        );

        int inserted = publisherRepository.insertIfAbsent(publisher);

        if (inserted == 1) {
            return new PublisherRegistrationResult(publisher, true);
        }

        Publisher existingPublisher = publisherRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Publisher not found"));

        return new PublisherRegistrationResult(existingPublisher, false);
    }

    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        return publisherRepository.findAllByOrderByNameAscIdAsc();
    }


}
