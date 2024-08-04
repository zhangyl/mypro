package com.zyl.mypro.test;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
        Animal a = new Dog();
        Feeder feeder = new Feeder();
        feeder.feed(a);//print: feed Animal.

//        Method method = feeder.getClass().getMethod();
    }
}
