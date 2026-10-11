package com.teammatch.repository;

import com.teammatch.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByStudentId(Long studentId);
    List<TeamMember> findByTeamId(Long teamId);
    boolean existsByTeamIdAndStudentId(Long teamId, Long studentId);
}
