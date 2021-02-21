package com.kg.megaregionapp.users;

/**
 * Created by ASUS on 6/29/2017.
 */

public class Users {

    private String id;
    private String name;

    public Users() {
    }

    public Users(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
