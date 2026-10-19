package io.luck.AghsLabs.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Tracks persistence state independently of an externally assigned identifier.
 * New instances are inserted; load an existing entity before updating it.
 */
@MappedSuperclass
public abstract class AssignedIdEntity<ID> implements Persistable<ID> {

    @Transient
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
