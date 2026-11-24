package com.murto.account.dao;

import com.murto.account.api.dto.AccountInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountDAO {

    private final JdbcTemplate jdbcTemplate;

    public AccountDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AccountInfo> findById(Long id) {
        return jdbcTemplate.query(
                """
                SELECT id, login_name, nickname, avatar_url, status, created_at, updated_at
                FROM user_account WHERE id = ?
                """,
                (rs, row) -> new AccountInfo(
                        rs.getLong("id"),
                        rs.getString("login_name"),
                        rs.getString("nickname"),
                        rs.getString("avatar_url"),
                        rs.getInt("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                id
        ).stream().findFirst();
    }

    public void updateProfile(Long id, String nickname, String avatarUrl) {
        jdbcTemplate.update(
                "UPDATE user_account SET nickname = ?, avatar_url = ? WHERE id = ?",
                nickname,
                avatarUrl,
                id
        );
    }

    public boolean deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM user_account WHERE id = ?", id) == 1;
    }
}
