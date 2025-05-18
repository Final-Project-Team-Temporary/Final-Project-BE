package com.example.whiplash.domain;

import com.example.whiplash.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_alarm_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAlarmInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean emailEnabled;
    private Boolean reportEnabled;
    private LocalDateTime preferredTime;

    // 단방향: UserAlarmInfo → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
