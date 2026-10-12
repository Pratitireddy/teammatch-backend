package com.teammatch.repository;

import com.teammatch.entity.Application;
import com.teammatch.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByStudentIdAndOpportunityIdAndRoleId(Long studentId, Long opportunityId, Long roleId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"student", "opportunity", "opportunity.creator", "role"})
    List<Application> findByStudentId(Long studentId);

    List<Application> findByOpportunityId(Long opportunityId);
    List<Application> findByStudentIdAndOpportunityIdAndStatus(Long studentId, Long opportunityId, ApplicationStatus status);
}
