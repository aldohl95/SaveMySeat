package com.savemyseat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@TestConfiguration
@EnableJpaAuditing
public class JpaTestConfig {
    @Bean
    public DateTimeProvider auditingDateTimeProvider(){
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
