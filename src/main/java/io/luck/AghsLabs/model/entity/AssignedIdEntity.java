package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

/**
 * Tracks persistence state independently of an externally assigned identifier.
 * New instances are inserted; load an existing entity before updating it.
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class AssignedIdEntity<ID> implements Persistable<ID> {

    @Transient
    @Builder.Default
    private boolean newEntity = true;

    @Override
    @JsonIgnore
    public boolean isNew() {
        return newEntity;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        newEntity = false;
    }
}
