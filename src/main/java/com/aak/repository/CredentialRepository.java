package com.aak.repository;

import com.aak.domain.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CredentialRepository extends JpaRepository<Credentials,Long> {
    Credentials findByName(String name);
    @Transactional
    int deleteCredentialsByName(String name);

    //Credentials findByName (String name);

}
