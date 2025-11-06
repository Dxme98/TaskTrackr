package com.dev.tasktrackr.basicdetails.repository;

import com.dev.tasktrackr.basicdetails.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Set<Link> findAllByBasicDetailsId(Long basicDetailsId);

    boolean existsByBasicDetailsIdAndTitle(Long basicDetailsId, String title);
}
