package com.zyl.mypro.lambda;

import java.util.ArrayList;
import java.util.List;

public class TestForEach {

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(0);
        list.add(2);

        list.forEach(item -> {
            System.out.println(10/item);
        });
    }
}
