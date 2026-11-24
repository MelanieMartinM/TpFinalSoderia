package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.HelloApplication;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Usuario;
import ar.edu.utn.tpfinalsoderia.services.ServicioLogin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoContrasena;

    @FXML
    private Label mensaje;

    private final ServicioLogin servicioLogin = new ServicioLogin();

    @FXML
    private void iniciarSesion() {
        String nombreUsuario = campoUsuario.getText().trim();
        String contrasena = campoContrasena.getText();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mensaje.setText("Completá el usuario y la contraseña.");
            return;
        }

        try {
            Usuario usuario = servicioLogin.iniciarSesion(
                    nombreUsuario,
                    contrasena
            );

            if (usuario == null) {
                mensaje.setText("Usuario o contraseña incorrectos.");
                return;
            }

            abrirMenu();

        } catch (PersistenciaException | IllegalStateException e) {
            mensaje.setText("No se pudo conectar con la base de datos.");
        }
    }

    private void abrirMenu() {
        URL recurso = HelloApplication.class.getResource("menu-view.fxml");

        if (recurso == null) {
            mensaje.setText("No se encontró la pantalla del menú.");
            return;
        }

        try {
            Parent menu = FXMLLoader.load(recurso);
            campoUsuario.getScene().setRoot(menu);

        } catch (IOException e) {
            mensaje.setText("No se pudo abrir el menú.");
            e.printStackTrace();
        }
    }
}