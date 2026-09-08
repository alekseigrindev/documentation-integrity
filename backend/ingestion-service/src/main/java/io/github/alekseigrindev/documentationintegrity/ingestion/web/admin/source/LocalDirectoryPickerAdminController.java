package io.github.alekseigrindev.documentationintegrity.ingestion.web.admin.source;

import io.github.alekseigrindev.documentationintegrity.ingestion.source.LocalDirectoryPicker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/local-directories")
@RequiredArgsConstructor
public class LocalDirectoryPickerAdminController {

    private final LocalDirectoryPicker localDirectoryPicker;

    @PostMapping("/choose")
    public LocalDirectorySelectionResponse choose() {
        return new LocalDirectorySelectionResponse(
                localDirectoryPicker.choose().orElse(null)
        );
    }
}
