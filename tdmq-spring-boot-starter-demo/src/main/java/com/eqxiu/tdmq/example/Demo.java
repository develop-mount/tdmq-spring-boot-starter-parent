package com.eqxiu.tdmq.example;

public class Demo {

    private String name;
    private String demo;

    public Demo(String name, String demo) {
        this.name = name;
        this.demo = demo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "name='" + name + '\'' +
                ", demo='" + demo + '\'' +
                '}';
    }
}
