package com.aak.repository;

import com.aak.domain.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<Credentials,Long> {
    Credentials findByName(String name);
    //Credentials findByName (String name);
}
