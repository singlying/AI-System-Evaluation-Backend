package com.ai.backend.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";


    private static final String PREFIX_KAPTCHA = "kaptcha";




    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }



}