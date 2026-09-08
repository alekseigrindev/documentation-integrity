package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.connector;

import io.github.alekseigrindev.documentationintegrity.ingestion.connector.ConnectorType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/connectors")
public class ConnectorAdminController {

    @GetMapping
    public ResponseEntity<List<ConnectorResponse>> getConnectorTypes() {
        List<ConnectorResponse>  connectors = Arrays.stream(ConnectorType.values())
                .filter(ConnectorType::isActive)
                .map(type -> new ConnectorResponse(
                        type.getAlias(),
                        type.getDescription()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(connectors);
    }

}
