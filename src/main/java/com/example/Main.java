package com.example;

import com.example.entity.User;
import com.example.service.UserService;
import com.example.service.UserServiceImpl;
import com.example.util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static final UserService userService = new UserServiceImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== User Service ===");

        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": create(); break;
                    case "2": findById(); break;
                    case "3": findByEmail(); break;
                    case "4": findAll(); break;
                    case "5": update(); break;
                    case "6": delete(); break;
                    case "0": running = false; break;
                    default: System.out.println("Выберете от 1 до 0");
                }
            }
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
        }
    }

    private static void printMenu() {
        System.out.println("\n1. Создать пользователя");
        System.out.println("2. Поиск по ID");
        System.out.println("3. Поиск по email");
        System.out.println("4. Показать пользователей");
        System.out.println("5. Изменить пользователя");
        System.out.println("6. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private static void create() {
        try {
            System.out.print("Имя: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Введите имя");
                return;
            }

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty() || !email.contains("@")) {
                System.out.println("Введите корректный email");
                return;
            }

            System.out.print("Возраст: ");
            String ageInput = scanner.nextLine().trim();
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            User user = userService.createUser(name, email, age);
            System.out.println("Создан пользователь с ID: " + user.getId());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void findById() {
        try {
            System.out.print("ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());

            userService.getUserById(id)
                    .ifPresentOrElse(
                            user -> System.out.println("Найдено: " + user),
                            () -> System.out.println("Пользователь не найден")
                    );

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void findByEmail() {
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            userService.getUserByEmail(email)
                    .ifPresentOrElse(
                            user -> System.out.println("Найдено: " + user),
                            () -> System.out.println("Пользователь не найден")
                    );

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void findAll() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены");
            } else {
                System.out.println("Всего пользователей: " + users.size());
                users.forEach(user ->
                        System.out.printf("ID: %d, Имя: %s, Email: %s%n",
                                user.getId(), user.getName(), user.getEmail())
                );
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void update() {
        try {
            System.out.print("Введите ID для изменения: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Новое имя: ");
            String name = scanner.nextLine().trim();
            name = name.isEmpty() ? null : name;

            System.out.print("Новый email: ");
            String email = scanner.nextLine().trim();
            email = email.isEmpty() ? null : email;

            System.out.print("Новый возраст: ");
            String ageInput = scanner.nextLine().trim();
            Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

            User updated = userService.updateUser(id, name, email, age);
            System.out.println("Изменено: " + updated);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void delete() {
        try {
            System.out.print("Введите ID для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Подтверждаете? (да/нет): ");
            if ("да".equalsIgnoreCase(scanner.nextLine())) {
                userService.deleteUser(id);
                System.out.println("Пользователь удален");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}