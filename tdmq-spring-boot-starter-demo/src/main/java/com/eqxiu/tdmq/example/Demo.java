package com.eqxiu.tdmq.example;

import java.io.Serializable;
import java.util.List;

public class Demo implements Serializable {

    private String name;
    private String demo;
    private List<String> content;

    public Demo() {
    }

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

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Demo{" +
                "name='" + name + '\'' +
                ", demo='" + demo + '\'' +
                ", content=" + content +
                '}';
    }
}
