package com.haulmont.demo.jpawebapi.web.driver;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.demo.jpawebapi.core.entity.Driver;


@UiController("jpademo_Driver.edit")
@UiDescriptor("driver-edit.xml")
@EditedEntityContainer("driverDc")
@LoadDataBeforeShow
public class DriverEdit extends StandardEditor<Driver> {
}