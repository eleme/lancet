package me.ele.lancet.testsample.playground;

public class CoffeeMaker {
    private final Heater heater;
    private final Pump pump;
    private final CoffeeBox coffeeBox;

    CoffeeMaker(Heater heater, Pump pump,CoffeeBox coffeeBox) {
        this.heater = heater;
        this.pump = pump;
        this.coffeeBox = coffeeBox;
    }

    public Cup brew(Cup cup) {
        cup.putCoffee(coffeeBox.getLatte());
        SugarBox.addSugar(cup,10);
        heater.on();
        pump.pump();
        cup.full();
        heater.off();
        return cup;
    }
}