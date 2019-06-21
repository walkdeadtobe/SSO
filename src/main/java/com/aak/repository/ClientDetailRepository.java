package com.aak.repository;

import com.aak.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.web.savedrequest.SavedRequest;

public interface ClientDetailRepository extends JpaRepository<ClientDetail,Long> {
    ClientDetail findByClientId(String client_id );
}