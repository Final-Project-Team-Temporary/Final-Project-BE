package com.example.whiplash.domain.entity.history.email;

import com.example.whiplash.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailSendHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String subject;

    @Enumerated(EnumType.STRING)
    private SummaryLevel summaryLevel;

    private LocalDateTime sendAt;

    @Enumerated(EnumType.STRING)
    private EmailSendStatus status;

    // 단방향: EmailSendHistory → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
