package com.junting.gulimall.member.exception;

/**
 * @author mini_zeng
 * @create 2022-01-13 19:16
 */

public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号存在");
    }
}
