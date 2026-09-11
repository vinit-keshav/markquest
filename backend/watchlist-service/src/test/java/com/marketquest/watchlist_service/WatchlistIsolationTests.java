package com.marketquest.watchlist_service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.marketquest.watchlist_service.repository.WatchlistRepository;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class WatchlistIsolationTests {
    @Autowired WebApplicationContext context;
    @Autowired WatchlistRepository repository;
    MockMvc mvc;
    @BeforeEach void setup() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    String token(String subject, Instant expiration, String secret) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
            .subject(subject).expirationTime(Date.from(expiration)).build());
        jwt.sign(new MACSigner(secret));
        return "Bearer " + jwt.serialize();
    }
    String token(String subject) throws Exception {
        return token(subject, Instant.now().plusSeconds(600), "test-only-secret-with-at-least-thirty-two-bytes");
    }
    @Test void rejectsMissingForgedAndExpiredTokens() throws Exception {
        mvc.perform(get("/api/watchlist")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/watchlist").header("Authorization", token("alice", Instant.now().plusSeconds(600), "a-different-secret-with-at-least-thirty-two-bytes"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/watchlist").header("Authorization", token("alice", Instant.now().minusSeconds(120), "test-only-secret-with-at-least-thirty-two-bytes"))).andExpect(status().isUnauthorized());
    }
    @Test void usersCannotReadOrEditEachOthersStocks() throws Exception {
        mvc.perform(post("/api/watchlist").header("Authorization", token("alice"))
            .contentType("application/json").content("{\"symbol\":\"aapl\",\"companyName\":\"Apple\",\"userId\":\"bob\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value("alice"));
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(get("/api/watchlist").header("Authorization", token("bob")))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(put("/api/watchlist/" + id).header("Authorization", token("bob"))
            .contentType("application/json").content("{\"note\":\"changed\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/watchlist").header("Authorization", token("alice")))
            .andExpect(jsonPath("$[0].symbol").value("AAPL"));
    }
}
