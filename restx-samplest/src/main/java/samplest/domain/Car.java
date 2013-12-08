package samplest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import samplest.jacksonviews.CustomJacksonSerializer;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 11:16
 */

@JsonSerialize(using = CustomJacksonSerializer.class)
public class Car {

    private String brand;

    private String model;

    public Car setBrand(final String brand) {
        this.brand = brand;
        return this;
    }

    public Car setModel(final String model) {
        this.model = model;
        return this;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "Car{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}
