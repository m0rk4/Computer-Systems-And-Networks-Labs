module core {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires spring.jcl;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.module.jaxb;

    opens by.bsuir.m0rk4.csan.task.third.dto to com.fasterxml.jackson.databind;
    exports by.bsuir.m0rk4.csan.task.third;
}