package com.haulmont.demo.jpawebapi.web.car;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.demo.jpawebapi.core.entity.Car;


@UiController("jpademo_Car.edit")
@UiDescriptor("car-edit.xml")
@EditedEntityContainer("carDc")
@LoadDataBeforeShow
public class CarEdit extends StandardEditor<Car> {
}