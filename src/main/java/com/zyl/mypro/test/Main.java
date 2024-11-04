package com.zyl.mypro.test;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Animal a = new Dog();
        Feeder feeder = new Feeder();
        feeder.feed(a);//print: feed Animal.

//        Method method = feeder.getClass().getMethod();

        List<User> userList = new ArrayList<>();
        {
            User user = new User();
            user.setName("a");
            user.setAge(20);
            userList.add(user);
        }
        {
            User user = new User();
            user.setName("b");
            user.setAge(21);
            userList.add(user);
        }
        {
            User user = new User();
            user.setName("");
            user.setAge(21);
            userList.add(user);
        }
        List<String> nameList = userList.stream().filter(item->item != null  && StringUtils.isNotBlank(item.getName())).map(User::getName).collect(Collectors.toList());
        System.out.println(nameList);

    }
}

class User {
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}