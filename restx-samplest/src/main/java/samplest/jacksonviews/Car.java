package samplest.jacksonviews;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * User: eoriou
 * Date: 04/12/2013
 * Time: 11:16
 */

public class Car {
    private String brand;
    private String model;

    @JsonSerialize(using = CustomJacksonSerializer.class)
    private String status = "";

    @JsonView(Views.Details.class)
    private String details;

    public Car setBrand(final String brand) {
        this.brand = brand;
        return this;
    }

    public Car setModel(final String model) {
        this.model = model;
        return this;
    }

    public Car setStatus(final String status) {
        this.status = status;
        return this;
    }

    public Car setDetails(final String details) {
        this.details = details;
        return this;
    }


    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Car{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", status='" + status + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
