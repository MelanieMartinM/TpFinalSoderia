module ar.edu.utn.tpfinalsoderia {
    requires javafx.controls;
    requires javafx.fxml;


    opens ar.edu.utn.tpfinalsoderia to javafx.fxml;
    exports ar.edu.utn.tpfinalsoderia;
}