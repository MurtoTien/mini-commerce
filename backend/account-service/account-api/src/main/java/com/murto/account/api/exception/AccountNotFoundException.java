package com.murto.account.api.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("账号不存在");
    }
}
