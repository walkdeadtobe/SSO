package com.aak.repository;

import com.aak.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority,Long> {
    Authority findAuthorityById(Long id);
    List<Authority> findAuthoritiesById(Long id);

}
