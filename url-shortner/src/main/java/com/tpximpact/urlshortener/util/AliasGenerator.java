package com.tpximpact.urlshortener.util;


public class AliasGenerator {

    private static final String CHARACHTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generate(int length) {

        StringBuilder sb = new StringBuilder();

        for(int i=0; i<length; i++) {
            int index = (int) (Math.random() * CHARACHTERS.length());
            sb.append(CHARACHTERS.charAt(index));
        }
        return sb.toString();
    }
}
