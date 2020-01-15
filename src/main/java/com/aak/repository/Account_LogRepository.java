package com.aak.repository;

import com.aak.domain.Account_Log;
import com.aak.domain.CredentialsAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Account_LogRepository extends JpaRepository<Account_Log,String> {
    List<Account_Log> findAccount_LogsByUsername(String username);

    @Query("select u from Account_Log u where u.timestamp>=?1 and u.timestamp<=?2")
    List<Account_Log> findAccount_LogsByTimestampRange(String start, String end);
}
