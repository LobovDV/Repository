package com.bookshop.BookShopApp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdditionalMethod {

    public static String createMD5Hash(String input)  {

        try {
            String hashtext = null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Compute message digest of the input
            byte[] messageDigest = md.digest(input.getBytes());
            hashtext = convertToHex(messageDigest);
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static  String convertToHex(byte[] messageDigest) {
        StringBuilder builder = new StringBuilder();
        for (Byte b : messageDigest) {
            builder.append(String.format("%02x", b & 0xff));
        }
        return builder.toString();
    }
}
