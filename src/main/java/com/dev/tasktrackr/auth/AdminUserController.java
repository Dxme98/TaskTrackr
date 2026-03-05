package com.dev.tasktrackr.auth;

import com.dev.tasktrackr.user.domain.UserEntity;
import com.dev.tasktrackr.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncUserFromKeycloak(@RequestBody UserSyncDto dto) {

        if (userRepository.existsById(dto.id())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        UserEntity newUser = UserEntity.builder()
                .id(dto.id())
                .username(dto.username())
                .build();

        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}