package com.consignment.auth.repository;

import com.consignment.auth.model.User;
import com.consignment.auth.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserProfileRepository}.
 * Validates Requirements 2.1, 2.2.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private User createAndPersistUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");
        user.setEmail(username + "@test.com");
        user.setRoles(Set.of("ROLE_CONSIGNEE"));
        user.setEnabled(true);
        return entityManager.persist(user);
    }

    @Test
    void findByUserId_whenUserProfileExists_shouldReturnUserProfile() {
        // given
        User user = createAndPersistUser("consignee_user");

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setConsigneeStore("STORE_A");
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        entityManager.persist(profile);
        entityManager.flush();

        // when
        Optional<UserProfile> result = userProfileRepository.findByUserId(user.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getConsigneeStore()).isEqualTo("STORE_A");
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_whenUserProfileDoesNotExist_shouldReturnEmpty() {
        // given
        User user = createAndPersistUser("user_without_profile");
        entityManager.flush();

        // when
        Optional<UserProfile> result = userProfileRepository.findByUserId(user.getId());

        // then
        assertThat(result).isEmpty();
    }
}
