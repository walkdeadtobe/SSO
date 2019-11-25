package com.aak.repository;

import com.aak.domain.Account_Log;
import com.aak.domain.CredentialsAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Account_LogRepository extends JpaRepository<Account_Log,String> {
    List<Account_Log> findAccount_LogsByUsername(String username);

}
