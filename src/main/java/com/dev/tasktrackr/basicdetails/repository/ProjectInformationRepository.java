package com.dev.tasktrackr.basicdetails.repository;

import com.dev.tasktrackr.basicdetails.domain.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectInformationRepository extends JpaRepository<Information, Long> {

    Optional<Information> findByBasicDetailsId(Long basicDetailsId);
}
