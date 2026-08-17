package com.taskflow.vacation.config;

import com.taskflow.vacation.domain.entity.User;
import com.taskflow.vacation.domain.enums.Role;
import com.taskflow.vacation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@taskflow.com")
                    .role(Role.ADMIN)
                    .password("123456")
                    .build();

            User manager = User.builder()
                    .name("Carlos Manager")
                    .email("carlos.manager@taskflow.com")
                    .role(Role.MANAGER)
                    .password("123456")
                    .build();

            userRepository.saveAll(List.of(admin, manager));

            User dev1 = User.builder()
                    .name("Ana Developer")
                    .email("ana.dev@taskflow.com")
                    .role(Role.COLLABORATOR)
                    .password("123456")
                    .manager(manager)
                    .build();

            User dev2 = User.builder()
                    .name("Bruno Developer")
                    .email("bruno.dev@taskflow.com")
                    .role(Role.COLLABORATOR)
                    .password("123456")
                    .manager(manager)
                    .build();

            userRepository.saveAll(List.of(dev1, dev2));
        }
    }
}