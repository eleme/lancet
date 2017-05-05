package me.ele.lancet.testsample.playground;

public class Cup{
    boolean isEmpty;
    String coffee;

    public boolean isEmpty() {
        return isEmpty;
    }

    public void full() {
        isEmpty = false;
    }

    public void putCoffee(String coffee) {
        this.coffee = coffee;
    }

    public String getCoffee() {
        return coffee;
    }
}