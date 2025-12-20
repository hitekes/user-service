package com.example;

import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;
import com.example.exception.DaoException;
import com.example.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserDao userDao = new UserDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        logger.info("Запуск User Service приложения");

        try {
            testDatabaseConnection();

            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1": createUser(); break;
                    case "2": findUserById(); break;
                    case "3": findUserByEmail(); break;
                    case "4": findAllUsers(); break;
                    case "5": updateUser(); break;
                    case "6": deleteUser(); break;
                    case "0": running = false; break;
                    default: System.out.println("Неверный выбор");
                }
            }
        } catch (Exception e) {
            logger.error("Критическая ошибка", e);
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
        }
    }

    private static void testDatabaseConnection() {
        try {
            List<User> users = userDao.findAll();
            logger.info("Подключение успешно. Пользователей: {}", users.size());
        } catch (DaoException e) {
            logger.error("Ошибка подключения", e);
            System.exit(1);
        }
    }

    private static void printMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти по ID");
        System.out.println("3. Найти по email");
        System.out.println("4. Все пользователи");
        System.out.println("5. Обновить");
        System.out.println("6. Удалить");
        System.out.println("0. Выход");
        System.out.print("Выбор: ");
    }

    private static void createUser() {
        try {
            System.out.println("\n--- Создание нового пользователя ---");

            String name;
            while (true) {
                System.out.print("Введите имя: ");
                name = scanner.nextLine().trim();
                if (!name.isEmpty()) {
                    break;
                }
                System.out.println("Имя не может быть пустым!");
            }

            String email;
            while (true) {
                System.out.print("Введите email: ");
                email = scanner.nextLine().trim();
                if (!email.isEmpty()) {
                    if (email.contains("@")) {
                        try {
                            Optional<User> existing = userDao.findByEmail(email);
                            if (existing.isPresent()) {
                                System.out.println("Ошибка: Пользователь с email '" + email + "' уже существует!");
                                System.out.print("Продолжить? (yes/no): ");
                                if (!scanner.nextLine().equalsIgnoreCase("yes")) {
                                    return;
                                }
                            }
                            break;
                        } catch (DaoException e) {
                            break;
                        }
                    } else {
                        System.out.println("Email должен содержать '@' символ!");
                    }
                } else {
                    System.out.println("Email не может быть пустым!");
                }
            }

            Integer age = null;
            while (true) {
                System.out.print("Введите возраст : ");
                String ageInput = scanner.nextLine().trim();
                if (ageInput.isEmpty()) {
                    break;
                }
                try {
                    age = Integer.parseInt(ageInput);
                    if (age >= 0 && age <= 150) {
                        break;
                    } else {
                        System.out.println("Возраст должен быть от 0 до 150 лет!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Пожалуйста, введите целое число!");
                }
            }

            User user = new User(name, email, age);

            logger.info("Попытка сохранить пользователя: {}", user);
            User savedUser = userDao.save(user);

            System.out.println("Пользователь успешно создан!");
            System.out.println("ID: " + savedUser.getId());
            System.out.println("Имя: " + savedUser.getName());
            System.out.println("Email: " + savedUser.getEmail());
            if (savedUser.getAge() != null) {
                System.out.println("Возраст: " + savedUser.getAge());
            }
            System.out.println("Дата создания: " + savedUser.getCreatedAt());

        } catch (DaoException e) {
            System.err.println("Ошибка при создании пользователя!");
            System.err.println("Сообщение: " + e.getMessage());

            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Причина: " + cause.getMessage());

                if (cause.getMessage().contains("unique constraint") ||
                        cause.getMessage().contains("duplicate key")) {
                    System.err.println("Этот email уже используется другим пользователем!");
                }

                if (cause.getMessage().contains("not-null") ||
                        cause.getMessage().contains("null value")) {
                    System.err.println("Обязательные поля не могут быть пустыми");
                }
            }

            logger.error("Ошибка при создании пользователя", e);
        } catch (Exception e) {
            System.err.println("ошибка: " + e.getMessage());
            logger.error("ошибка при создании пользователя", e);
        }
    }

    private static void findUserById() {
        try {
            System.out.print("ID: ");
            Long id = Long.parseLong(scanner.nextLine());
            Optional<User> user = userDao.findById(id);

            if (user.isPresent()) {
                System.out.println("Найден: " + user.get());
            } else {
                System.out.println("Не найден");
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void findAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("Нет пользователей");
            } else {
                users.forEach(System.out::println);
            }
        } catch (DaoException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("ID для обновления: ");
            Long id = Long.parseLong(scanner.nextLine());
            Optional<User> userOpt = userDao.findById(id);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                System.out.print("Новое имя (" + user.getName() + "): ");
                String name = scanner.nextLine();
                if (!name.isEmpty()) user.setName(name);

                System.out.print("Новый email (" + user.getEmail() + "): ");
                String email = scanner.nextLine();
                if (!email.isEmpty()) user.setEmail(email);

                System.out.print("Новый возраст (" + user.getAge() + "): ");
                String ageStr = scanner.nextLine();
                if (!ageStr.isEmpty()) {
                    try {
                        user.setAge(Integer.parseInt(ageStr));
                    } catch (NumberFormatException e) {
                    }
                }

                userDao.update(user);
                System.out.println("Обновлен: " + user);
            } else {
                System.out.println("Не найден");
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("ID для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Уверены? (Да/Нет): ");
            if ("Да".equalsIgnoreCase(scanner.nextLine())) {
                userDao.delete(id);
                System.out.println("Удален");
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static void findUserByEmail() {
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            Optional<User> user = userDao.findByEmail(email);

            if (user.isPresent()) {
                System.out.println("Найден: " + user.get());
            } else {
                System.out.println("Не найден");
            }
        } catch (DaoException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}