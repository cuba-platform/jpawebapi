package com.haulmont.demo.jpawebapi.core.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.*;

@Table(name = "JPADEMO_CAR")
@Entity(name = "jpademo_Car")
public class Car extends StandardEntity {
    @Column(name = "MODEL")
    protected String model;

    @Column(name = "COLOR")
    protected String color;

    @Column(name = "PRODUCED_YEAR")
    protected Integer producedYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID")
    protected Driver owner;

    public Driver getOwner() {
        return owner;
    }

    public void setOwner(Driver owner) {
        this.owner = owner;
    }

    public Integer getProducedYear() {
        return producedYear;
    }

    public void setProducedYear(Integer producedYear) {
        this.producedYear = producedYear;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}