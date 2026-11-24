//Este controlador recibe los datos escritos en la pantalla. Si falta alguno, muestra un mensaje;
// si ambos están completos, avisa que todavía no está implementada la verificación.


package ar.edu.utn.tpfinalsoderia.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Label mensaje;

    @FXML
    private void iniciarSesion() {
        String nombreUsuario = campoUsuario.getText().trim();
        String contrasena = campoContrasena.getText();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mensaje.setText("Completá el usuario y la contraseña.");
            return;
        }

        mensaje.setText("La verificación de acceso todavía está pendiente.");
    }
}