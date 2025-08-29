package com.dev.tasktrackr.user;


import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException(userId));
    }
}
