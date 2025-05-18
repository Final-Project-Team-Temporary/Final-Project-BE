package com.example.whiplash.user.service;


import com.example.whiplash.user.User;
import com.example.whiplash.user.UserModifyRequestDTO;
import com.example.whiplash.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;

    /** 새 사용자 생성 */
    @Transactional
    public Long create(User user) {
        return userRepository.save(user).getId();
    }

    /** 단일 사용자 조회 */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    /** 모든 사용자 조회 */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** 사용자 정보 업데이트 */
    @Transactional
    public User update(UserModifyRequestDTO dto, Long userId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        findUser.update(dto);

        return userRepository.save(findUser);
    }

    /** 사용자 삭제 */
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
