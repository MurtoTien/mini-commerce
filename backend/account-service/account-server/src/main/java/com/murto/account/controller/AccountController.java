package com.murto.account.controller;

import com.murto.account.api.dto.AccountInfo;
import com.murto.account.api.dto.UpdateAccountRequest;
import com.murto.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{userId}")
    public AccountInfo getAccount(@PathVariable("userId") Long userId) {
        return accountService.getAccount(userId);
    }

    // 保留原来的演示入口，数据改为来自 MySQL。
    @GetMapping("/info")
    public AccountInfo getAccountInfo(@RequestParam(name = "userId", defaultValue = "1") Long userId) {
        return accountService.getAccount(userId);
    }

    @PutMapping("/{userId}")
    public AccountInfo updateAccount(
            @PathVariable("userId") Long userId,
            @RequestBody UpdateAccountRequest request
    ) {
        return accountService.updateAccount(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable("userId") Long userId) {
        accountService.deleteAccount(userId);
    }
}
