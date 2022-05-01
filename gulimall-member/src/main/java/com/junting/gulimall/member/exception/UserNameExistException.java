package com.junting.gulimall.member.exception;

/**
 * @author mini_zeng
 * @create 2022-01-13 19:16
 */

public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名存在");
    }
}
