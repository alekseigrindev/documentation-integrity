package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.DocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document.SourceRevisionDocumentImportRequest;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source.revision.document.SourceRevisionDocumentImportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentImportMapper {

    DocumentImportCommand toCommand(
            SourceRevisionDocumentImportRequest request
    );

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "sourceRevisionId", source = "document.sourceRevisionId")
    @Mapping(target = "sourceLocator", source = "document.sourceLocator")
    @Mapping(target = "canonicalUrl", source = "document.canonicalUrl")
    @Mapping(target = "productVariant", source = "document.productVariant")
    @Mapping(target = "contentHash", source = "document.contentHash")
    @Mapping(target = "chunkCount", source = "chunkCount")
    SourceRevisionDocumentImportResponse toResponse(
            DocumentImportResult result
    );

}
