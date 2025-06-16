package com.example.whiplash.delivery.email;

import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class EmailSendingService {
    private final EmailSender emailSender;
    private final UserRepository userRepository;

    public void sendSummarizedArticlesToUser(Long userId, List<String> summarizedArticleIds) {
        String email = getEmail(userId);
        emailSender.sendSummarizedArticlesToUser(email, summarizedArticleIds);
    }

    private String getEmail(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("there is no user with id: " + userId))
                .getEmail();
    }


}
