package io.github.alekseigrindev.documentationintegrity.ingestion.source.revision;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.SourceRevisionRegistration;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.SourceRevisionRegistrationRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.SourceRevisionRegistrationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SourceRevisionMapper {
    SourceRevisionRegistration toRegistration(SourceRevisionRegistrationRequest request);

    SourceRevisionRegistrationResponse toResponse(SourceRevision registration);
}
