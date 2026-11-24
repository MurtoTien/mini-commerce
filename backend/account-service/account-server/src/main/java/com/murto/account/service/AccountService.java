package com.murto.account.service;

import com.murto.account.api.exception.AccountNotFoundException;
import com.murto.account.dao.AccountDAO;
import com.murto.account.api.dto.AccountInfo;
import com.murto.account.api.dto.UpdateAccountRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public AccountInfo getAccount(Long userId) {
        validateId(userId);

        return accountDAO.findById(userId).orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public AccountInfo updateAccount(Long userId, UpdateAccountRequest request) {
        validateId(userId);

        if (request == null || request.nickname() == null || request.nickname().isBlank()) {
            throw new IllegalArgumentException("昵称不能为空");
        }

        String nickname = request.nickname().strip();

        if (nickname.codePointCount(0, nickname.length()) > 100) {
            throw new IllegalArgumentException("昵称不能超过100个字符");
        }

        String avatarUrl = request.avatarUrl();

        if (avatarUrl != null && avatarUrl.codePointCount(0, avatarUrl.length()) > 1000) {
            throw new IllegalArgumentException("头像地址不能超过1000个字符");
        }

        accountDAO.updateProfile(userId, nickname, avatarUrl);

        // 相同内容的更新也正常返回，不用影响行数判断是否存在。
        return getAccount(userId);
    }

    public void deleteAccount(Long userId) {
        validateId(userId);

        if (!accountDAO.deleteById(userId)) {
            throw new AccountNotFoundException();
        }
    }

    private void validateId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须为正数");
        }
    }
}
