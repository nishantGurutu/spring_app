package com.example.attendence.service;

import com.example.attendence.dto.RegisterRequest;
import com.example.attendence.entity.User;
import com.example.attendence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final String uploadDir = "uploads/";  // Create an uploads/ folder in your project root

    public ResponseEntity<?> register(RegisterRequest request) {

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());

        // Encrypt password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(request.getPassword()));

        // Save image if present
        MultipartFile imageFile = request.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
                String filePath = uploadDir + fileName;
                File saveFile = new File(filePath);
                saveFile.getParentFile().mkdirs();  // Create directories if not exist
                imageFile.transferTo(saveFile);

                user.setImageUrl(filePath);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Failed to upload image");
            }
        }

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
