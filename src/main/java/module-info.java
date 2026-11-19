module ar.edu.utn.tpfinalsoderia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens ar.edu.utn.tpfinalsoderia to javafx.fxml;
    opens ar.edu.utn.tpfinalsoderia.controllers to javafx.fxml;

    exports ar.edu.utn.tpfinalsoderia;
}