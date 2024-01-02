package org.example.model;

import org.example.annotations.Autowired;
import org.example.annotations.Component;

@Component("Airplane")
public class Airplane {
    @Autowired
    private Body body;
    @Autowired
    private Engine engine;

    @Override
    public String toString() {
        return "Airplane{" +
                "body=" + body +
                ", engine=" + engine +
                '}';
    }
}
