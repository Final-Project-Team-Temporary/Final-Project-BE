package com.example.whiplash.auth.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.example.whiplash.converter.AuthConverter;
import com.example.whiplash.converter.UserConverter;
import com.example.whiplash.user.User;
import com.example.whiplash.user.dto.AuthResponse;
import com.example.whiplash.user.dto.UserCreateDTO;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse joinUser(UserCreateDTO userCreateDTO) {
        Optional<User> user = userRepository.findByEmail(userCreateDTO.getEmail());

        if (user.isPresent()) {
            throw new WhiplashException(ErrorStatus.DUPLICATE_EMAIL);
        }

        User newUser = UserConverter.toUser(userCreateDTO);

        newUser.encodePassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        userRepository.save(newUser);

        String tempToken = jwtTokenProvider.generateTempToken(newUser);

        return AuthConverter.toAuthResponse(tempToken);
    }
}
