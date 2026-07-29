package com.example.dental.entity;

import com.example.dental.enums.LogActionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * システムの操作ログを保持するエンティティ
 */
@Entity
@Table(name = "system_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private LogActionType actionType;

    // 操作を行ったユーザーのログインID（ログイン失敗時など存在しないユーザーの入力値も保持できるようにStringとする）
    @Column(name = "login_id")
    private String loginId;

    // どの医院に関連する操作か（管理者の操作など、特定の医院に紐付かない場合はnullを許容）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dental_id")
    private DentalClinic dentalClinic;

    // 操作の詳細内容（対象の予約IDや変更内容などを柔軟に格納）
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 操作元のIPアドレス
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters removed in favor of Lombok @Data
}
