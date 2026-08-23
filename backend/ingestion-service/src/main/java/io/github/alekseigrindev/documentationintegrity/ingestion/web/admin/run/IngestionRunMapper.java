package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.run;

import io.github.alekseigrindev.documentationintegrity.ingestion.run.IngestionRun;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngestionRunMapper {

    IngestionRunResponse toResponse(IngestionRun run);

    List<IngestionRunResponse> toResponses(List<IngestionRun> runs);
}
