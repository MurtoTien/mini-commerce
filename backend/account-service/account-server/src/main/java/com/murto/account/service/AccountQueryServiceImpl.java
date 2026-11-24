package com.murto.account.service;

import com.murto.account.api.AccountQueryService;
import com.murto.account.api.dto.AccountInfo;
import com.murto.account.api.dto.UpdateAccountRequest;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountService accountService;

    public AccountQueryServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public AccountInfo queryAccount(Long userId) {
        return accountService.getAccount(userId);
    }

    @Override
    public AccountInfo updateAccount(Long userId, UpdateAccountRequest request) {
        return accountService.updateAccount(userId, request);
    }

    @Override
    public boolean deleteAccount(Long userId) {
        accountService.deleteAccount(userId);

        return true;
    }
}
