package com.utf8;

public class Main {
    public static void main(String[] args) {
        String hello = "Hello \uD83D\uDE00";
        System.out.println("Size of hello is: " + hello.length());
        System.out.println("Size of hello codepoint is: " + hello.codePointCount(0, hello.length()));
    }
}