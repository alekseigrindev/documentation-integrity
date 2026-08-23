package io.github.alekseigrindev.documentationintegrity.ingestion.importing;

import io.github.alekseigrindev.documentationintegrity.ingestion.command.LocalDocumentImportCommand;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document.DocumentImportResponse;
import io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.document.LocalDocumentImportRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentImportMapper {

    LocalDocumentImportCommand toCommand(LocalDocumentImportRequest request);

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "sourceId", source = "document.sourceId")
    @Mapping(target = "sourceLocator", source = "document.sourceLocator")
    @Mapping(target = "canonicalUrl", source = "document.canonicalUrl")
    @Mapping(target = "productVariant", source = "document.productVariant")
    @Mapping(target = "upstreamVersion", source = "document.upstreamVersion")
    @Mapping(target = "mediaType", source = "document.mediaType")
    @Mapping(target = "acquiredAt", source = "document.acquiredAt")
    @Mapping(target = "contentHash", source = "document.contentHash")
    @Mapping(target = "chunkCount", source = "chunkCount")
    DocumentImportResponse toResponse(DocumentImportResult result);

}
