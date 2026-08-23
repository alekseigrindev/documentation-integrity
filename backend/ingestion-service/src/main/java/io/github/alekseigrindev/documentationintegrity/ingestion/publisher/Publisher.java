package io.github.alekseigrindev.documentationintegrity.ingestion.publisher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(schema = "knowledge", name = "publishers")
public class Publisher {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;
}
