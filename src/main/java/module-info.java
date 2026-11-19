module ar.edu.utn.tpfinalsoderia
{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ar.edu.utn.tpfinalsoderia to javafx.fxml;
    opens ar.edu.utn.tpfinalsoderia.controllers to javafx.fxml;
    opens ar.edu.utn.tpfinalsoderia.models to javafx.base;

    exports ar.edu.utn.tpfinalsoderia;
    exports ar.edu.utn.tpfinalsoderia.controllers;
    exports ar.edu.utn.tpfinalsoderia.models;
    exports ar.edu.utn.tpfinalsoderia.services;
    exports ar.edu.utn.tpfinalsoderia.DAO;
    exports ar.edu.utn.tpfinalsoderia.enums;
    exports ar.edu.utn.tpfinalsoderia.exceptions;
    exports ar.edu.utn.tpfinalsoderia.interfaces;
}