package com.teammatch.repository;

import com.teammatch.entity.Opportunity;
import com.teammatch.entity.OpportunityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Opportunity o WHERE o.id = :id")
    Optional<Opportunity> findByIdWithPessimisticLock(@Param("id") Long id);

    // All opportunities created by a specific user
    List<Opportunity> findByCreatorId(Long creatorId);

    // Public listing: open opportunities, optionally filtered by type, with pagination
    @Query("SELECT o FROM Opportunity o WHERE o.status = :status " +
           "AND (:type IS NULL OR o.type = :type) " +
           "AND (:keyword IS NULL OR LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Opportunity> findOpenOpportunities(
            @Param("status") OpportunityStatus status,
            @Param("type") String type,
            @Param("keyword") String keyword,
            Pageable pageable);
}
