package com.aak.repository;

import com.aak.domain.Account_Log;
import com.aak.domain.CredentialsAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Account_LogRepository extends JpaRepository<Account_Log,String> {
    Account_Log findAccount_LogsByUsername(String username);
}
