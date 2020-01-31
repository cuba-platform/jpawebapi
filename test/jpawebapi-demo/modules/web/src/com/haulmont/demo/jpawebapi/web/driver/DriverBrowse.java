package com.haulmont.demo.jpawebapi.web.driver;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.demo.jpawebapi.core.entity.Driver;


@UiController("jpademo_Driver.browse")
@UiDescriptor("driver-browse.xml")
@LookupComponent("driversTable")
@LoadDataBeforeShow
public class DriverBrowse extends StandardLookup<Driver> {
}