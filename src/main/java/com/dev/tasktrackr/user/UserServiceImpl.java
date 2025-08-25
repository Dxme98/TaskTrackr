package com.dev.tasktrackr.user;


import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity findUserById(UserId userId) {
        return userRepository.findById(userId.value()).orElseThrow(
                () -> new UsernameNotFoundException(userId.value()));
    }
}
