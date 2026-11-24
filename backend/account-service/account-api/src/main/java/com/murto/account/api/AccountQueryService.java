package com.murto.account.api;

import com.murto.account.api.dto.AccountInfo;
import com.murto.account.api.dto.UpdateAccountRequest;
import com.murto.account.api.exception.AccountNotFoundException;

public interface AccountQueryService {

    AccountInfo queryAccount(Long userId) throws AccountNotFoundException;

    AccountInfo updateAccount(Long userId, UpdateAccountRequest request) throws AccountNotFoundException;

    // 删除成功返回 true；账号不存在时抛出 AccountNotFoundException。
    boolean deleteAccount(Long userId) throws AccountNotFoundException;
}
