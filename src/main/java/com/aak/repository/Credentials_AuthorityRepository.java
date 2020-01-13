package com.aak.repository;

import com.aak.domain.CredentialsAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Credentials_AuthorityRepository extends JpaRepository<CredentialsAuthority,Long> {
    CredentialsAuthority findByCredentialsid (Long credentialid);
}
