package com.example.service;

import com.example.dao.UserDao;
import com.example.entity.User;
import com.example.exception.DaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao);

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Должен успешно создать пользователя")
    void createUser_ShouldCreateUserSuccessfully() {
        User userToSave = User.builder()
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .build();

        when(userDao.save(any(User.class))).thenReturn(testUser);
        User createdUser = userService.createUser("Test User", "test@example.com", 25);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");

        verify(userDao, times(1)).save(argThat(user ->
                user.getName().equals("Test User") &&
                        user.getEmail().equals("test@example.com") &&
                        user.getAge() == 25
        ));
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException при ошибке создания пользователя")
    void createUser_ShouldThrowRuntimeException_WhenDaoFails() {
        when(userDao.save(any(User.class)))
                .thenThrow(new DaoException("Database error"));
        assertThatThrownBy(() -> userService.createUser("Test", "test@example.com", 25))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось создать пользователя");
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional при поиске по несуществующему ID")
    void getUserById_ShouldReturnEmptyOptional_WhenUserNotExists() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException при ошибке получения пользователя")
    void getUserById_ShouldThrowRuntimeException_WhenDaoFails() {
        when(userDao.findById(anyLong()))
                .thenThrow(new DaoException("Database error"));
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось получить пользователя");
    }

    @Test
    @DisplayName("Должен найти пользователя по email")
    void getUserByEmail_ShouldReturnUser() {
        when(userDao.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Optional<User> result = userService.getUserByEmail("test@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Должен вернуть всех пользователей")
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(
                createUser(1L, "user1@example.com"),
                createUser(2L, "user2@example.com")
        );
        when(userDao.findAll()).thenReturn(users);
        List<User> result = userService.getAllUsers();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getEmail)
                .containsExactly("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("Должен обновить только указанные поля")
    void updateUser_ShouldUpdateOnlySpecifiedFields() {
        User existingUser = createUser(1L, "test@example.com");
        existingUser.setAge(25);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = userService.updateUser(1L, "New Name", null, null);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getAge()).isEqualTo(25);

        verify(userDao).update(argThat(user ->
                user.getName().equals("New Name") &&
                        user.getEmail().equals("test@example.com") &&
                        user.getAge() == 25
        ));
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException при обновлении несуществующего пользователя")
    void updateUser_ShouldThrowRuntimeException_WhenUserNotFound() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(999L, "New Name", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userDao, never()).update(any());
    }
    @Test
    @DisplayName("Должен успешно удалить пользователя")
    void deleteUser_ShouldDeleteUserSuccessfully() {
        doNothing().when(userDao).delete(1L);
        userService.deleteUser(1L);
        verify(userDao, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException при ошибке удаления")
    void deleteUser_ShouldThrowRuntimeException_WhenDaoFails() {
        doThrow(new DaoException("Database error")).when(userDao).delete(1L);
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Не удалось удалить пользователя");
    }

    @Test
    @DisplayName("Должен обработать null значения при создании пользователя")
    void createUser_ShouldHandleNullAge() {
        User userToSave = User.builder()
                .name("Test User")
                .email("test@example.com")
                .age(null)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .age(null)
                .build();

        when(userDao.save(any(User.class))).thenReturn(savedUser);
        User result = userService.createUser("Test User", "test@example.com", null);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAge()).isNull();
    }

    private User createUser(Long id, String email) {
        return User.builder()
                .id(id)
                .name("User " + id)
                .email(email)
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }
}