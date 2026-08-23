package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.publisher.PublisherRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.publisher.PublisherResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    PublisherRegistration toPublisherRegistration(PublisherRegistrationRequest request);

    PublisherResponse toResponse(Publisher publisher);

    List<PublisherResponse> toResponseList(List<Publisher> publishers);
}
