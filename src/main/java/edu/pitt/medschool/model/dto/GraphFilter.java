package edu.pitt.medschool.model.dto;

public class GraphFilter {
    private int  age;
    private String id;
    
    GraphFilter(int age, String id) {
        this.age = age;
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    
}
