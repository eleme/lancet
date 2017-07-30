package com.sample.playground;

public class Main{

    public static void main(String[] args) {
        Heater heater = new SuperHeater();
        Pump punm = new Thermosiphon(heater);
        CoffeeBox box = new CoffeeBox();
        CoffeeMaker maker = new CoffeeMaker(heater,punm,box);
        Cup cup = maker.brew(new Cup());
        System.out.println("a "+(cup.isEmpty()?"empty":"full") + " cup of "+cup.getCoffee());
    }
}