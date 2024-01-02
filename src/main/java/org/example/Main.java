package org.example;

import org.example.framework.Context;
import org.example.model.Airplane;
import org.example.model.Car;

public class Main {
    public static void main(String[] args) {
        Context context = Context.load("org.example.model");
        System.out.println(context.getLoadedClasses());
        try {
            Car car = (Car) context.get("Car");
            System.out.println(car.toString());
            Airplane airplane = (Airplane) context.get("Airplane");
            System.out.println(airplane.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
