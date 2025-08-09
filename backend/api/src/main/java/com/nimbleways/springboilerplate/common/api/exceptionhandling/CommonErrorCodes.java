package com.nimbleways.springboilerplate.common.api.exceptionhandling;

import com.nimbleways.springboilerplate.common.api.exceptionhandling.base.ErrorCode;
import lombok.Getter;

@Getter
public enum CommonErrorCodes implements ErrorCode {
    UNAUTHORIZED_ERROR("errors.unauthorized");

    private final String code;

    CommonErrorCodes(String code) {
        this.code = code;
    }
}
