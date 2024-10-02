package com.example.demo.seed;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepository; // Ensure this import exists
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired; // Import the Autowired annotation

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class UserDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository; // Inject the UserRepository

    @Override
    public void run(String... args) {
        try {
            List<User> users = loadUsers();
            userRepository.saveAll(users); // Save all users to the database
            users.forEach(user -> System.out.println("Saved user: " + user.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<User> loadUsers() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getResourceAsStream("/user.json")) {
            return objectMapper.readValue(inputStream, new TypeReference<List<User>>() {});
        }
    }
}
