package com.teammatch.repository;

import com.teammatch.entity.Otp;
import com.teammatch.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByEmailAndPurpose(String email, OtpPurpose purpose);
}
