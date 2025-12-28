package com.example.dao;

import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.TestHibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserDao userDao;

    @BeforeAll
    static void setUpAll() {
        postgres.start();
    }

    @BeforeEach
    void setUp() {
        var sessionFactory = TestHibernateUtil.getSessionFactory(postgres);
        try {
            var hibernateUtilClass = Class.forName("com.example.util.HibernateUtil");
            var sessionFactoryField = hibernateUtilClass.getDeclaredField("sessionFactory");
            sessionFactoryField.setAccessible(true);
            sessionFactoryField.set(null, sessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set session factory", e);
        }

        userDao = new UserDaoImpl();
    }

    @AfterEach
    void tearDown() {
        try (var session = TestHibernateUtil.getSessionFactory(postgres).openSession()) {
            var transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
        }
    }
    @AfterAll
    static void tearDownAll() {
        TestHibernateUtil.closeSessionFactory();
        if (postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Test
    @DisplayName("Должен сохранить пользователя и вернуть его с ID")
    void save_ShouldSaveUserAndReturnWithId() {
        User user = createTestUser("test@example.com");
        User savedUser = userDao.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Должен найти пользователя по существующему ID")
    void findById_ShouldReturnUser_WhenUserExists() {
        User savedUser = userDao.save(createTestUser("find@example.com"));
        Long userId = savedUser.getId();
        Optional<User> foundUser = userDao.findById(userId);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("find@example.com");
    }
    @Test
    @DisplayName("Должен вернуть пустой Optional при поиске по несуществующему ID")
    void findById_ShouldReturnEmptyOptional_WhenUserNotExists() {
        Optional<User> foundUser = userDao.findById(999L);
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть всех пользователей")
    void findAll_ShouldReturnAllUsers() {
        userDao.save(createTestUser("user1@example.com"));
        userDao.save(createTestUser("user2@example.com"));
        userDao.save(createTestUser("user3@example.com"));
        List<User> users = userDao.findAll();
        assertThat(users).hasSize(3);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "user1@example.com",
                        "user2@example.com",
                        "user3@example.com"
                );
    }
    @Test
    @DisplayName("Должен обновить данные пользователя")
    void update_ShouldUpdateUser() {
        User savedUser = userDao.save(createTestUser("update@example.com"));
        savedUser.setName("Updated Name");
        savedUser.setAge(30);
        User updatedUser = userDao.update(savedUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getAge()).isEqualTo(30);
        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Должен удалить пользователя по ID")
    void delete_ShouldDeleteUser() {
        User savedUser = userDao.save(createTestUser("delete@example.com"));
        Long userId = savedUser.getId();
        userDao.delete(userId);
        Optional<User> foundUser = userDao.findById(userId);
        assertThat(foundUser).isEmpty();
    }
    @Test
    @DisplayName("Должен найти пользователя по email")
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        String email = "findbyemail@example.com";
        userDao.save(createTestUser(email));
        Optional<User> foundUser = userDao.findByEmail(email);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
    }
    @Test
    @DisplayName("Должен вернуть пустой Optional при поиске по несуществующему email")
    void findByEmail_ShouldReturnEmptyOptional_WhenEmailNotExists() {
        Optional<User> foundUser = userDao.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пробелы в email")
    void findByEmail_ShouldTrimSpaces() {
        String email = "trim@example.com";
        userDao.save(createTestUser(email));
        Optional<User> foundUser = userDao.findByEmail("  trim@example.com  ");
        assertThat(foundUser).isPresent();
    }

    private User createTestUser(String email) {
        return User.builder()
                .name("Test User")
                .email(email)
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }
}