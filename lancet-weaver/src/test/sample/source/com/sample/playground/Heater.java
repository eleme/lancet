package com.sample.playground;

class Heater {
  boolean heating;

  public void on() {
    System.out.println("~ ~ ~ heating ~ ~ ~");
    this.heating = true;
  }

  public void off() {
    this.heating = false;
  }

  public boolean isHot() {
    return heating;
  }
}