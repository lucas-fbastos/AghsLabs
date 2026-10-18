package io.luck.AghsLabs.repository;

import io.luck.AghsLabs.model.entity.ProPlayer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProPlayerRepository extends JpaRepository<ProPlayer,Long> {

    List<ProPlayer> findByPlayerRoleOrderByLastLoginDescAccountIdAsc(
            Integer playerRole,
            Pageable pageable
    );

    @Query("""
            SELECT pro
            FROM ProPlayer pro
            WHERE LOCATE(:query, LOWER(pro.personaName)) > 0
               OR LOCATE(:query, LOWER(pro.name)) > 0
               OR LOCATE(:query, LOWER(pro.teamName)) > 0
               OR LOCATE(:query, LOWER(pro.teamTag)) > 0
            ORDER BY CASE
                         WHEN LOWER(COALESCE(pro.personaName, '')) = :query THEN 0
                         WHEN LOCATE(:query, LOWER(COALESCE(pro.personaName, ''))) = 1 THEN 1
                         ELSE 2
                     END,
                     LOWER(COALESCE(pro.personaName, '')),
                     pro.accountId
            """)
    List<ProPlayer> search(@Param("query") String normalizedQuery, Pageable pageable);
}
