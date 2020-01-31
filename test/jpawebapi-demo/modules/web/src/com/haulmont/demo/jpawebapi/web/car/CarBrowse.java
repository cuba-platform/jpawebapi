package com.haulmont.demo.jpawebapi.web.car;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.demo.jpawebapi.core.entity.Car;


@UiController("jpademo_Car.browse")
@UiDescriptor("car-browse.xml")
@LookupComponent("carsTable")
@LoadDataBeforeShow
public class CarBrowse extends StandardLookup<Car> {
}