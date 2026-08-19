package io.github.alekseigrindev.documentationintegrity.ingestion.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.SourceRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.SourceResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SourceMapper {

    SourceRegistration toSourceRegistration(SourceRegistrationRequest request);

    SourceResponse toResponse(Source source);

    List<SourceResponse> toResponseList(List<Source> sourceList);
}
