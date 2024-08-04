package com.zyl.mypro.bean;

import java.util.Date;

public class User {
    private Integer id;
    private String name;

    private MyNameEnum nameEnum;

    private Date createTime;
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public MyNameEnum getNameEnum() {
        return nameEnum;
    }

    public void setNameEnum(MyNameEnum nameEnum) {
        this.nameEnum = nameEnum;
    }
}
