package com.murto.account.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

// 对外模型不包含 password_hash。
public record AccountInfo(
        Long id,
        String loginName,
        String nickname,
        String avatarUrl,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
