package com.savemyseat.repository;

import com.savemyseat.entity.User;
import com.savemyseat.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

  @Container
  static PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired UserRepository userRepository;

  @Test
  void savesAndReadsBackUser(){
    User u = new User("Alice", "Smith",
            "123@example.com", "hash", Role.ATTENDEE);
    User saved = userRepository.save(u);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();

    User fetched = userRepository.findById(saved.getId()).orElseThrow();
    assertThat(fetched.getEmail()).isEqualTo("123@example.com");
    assertThat(fetched.getRole()).isEqualTo(Role.ATTENDEE);

  }

  @Test
  void findUserByEmail(){
    userRepository.save(new User("Bob", "b", "321@example.com", "hash",
            Role.ADMIN));

    Optional<User> found = userRepository.findByEmail("321@example.com");

    assertThat(found).isPresent();
    assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);

  }

}
