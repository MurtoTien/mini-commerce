package com.murto.account.api.dto;

import java.io.Serializable;

// 完整替换资料字段，avatarUrl=null 表示清空头像。
public record UpdateAccountRequest(
        String nickname,
        String avatarUrl
) implements Serializable {
}
