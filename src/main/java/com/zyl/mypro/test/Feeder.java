package com.zyl.mypro.test;

public class Feeder {
    public void feed(Animal animal) {
        System.out.println("feed Animal.");
    }
    public void feed(Dog dog) {
        System.out.println("feed Dog: " + dog.eat());
    }
    public void feed(Cat cat) {
        System.out.println("feed Cat: " + cat.eat());
    }
}
