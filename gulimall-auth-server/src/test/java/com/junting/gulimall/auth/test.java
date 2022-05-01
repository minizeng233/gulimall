package com.junting.gulimall.auth;

import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author mini_zeng
 * @create 2022-02-28 19:02
 */

public class test {
    @Test
    public void test(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String abc = Md5Crypt.md5Crypt("123456".getBytes(), "$1$abc");
        String s = Md5Crypt.md5Crypt("123456".getBytes());
        System.out.println(abc);
        System.out.println(s);
    }
}
