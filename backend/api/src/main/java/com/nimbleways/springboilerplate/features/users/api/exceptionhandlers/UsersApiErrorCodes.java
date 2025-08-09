package com.nimbleways.springboilerplate.features.users.api.exceptionhandlers;

import com.nimbleways.springboilerplate.common.api.exceptionhandling.base.ErrorCode;
import lombok.Getter;

@Getter
public enum UsersApiErrorCodes implements ErrorCode {
    USERNAME_ALREADY_EXISTS_ERROR("errors.username_already_exists");

    private final String code;

    UsersApiErrorCodes(String code) {
        this.code = code;
    }
}
