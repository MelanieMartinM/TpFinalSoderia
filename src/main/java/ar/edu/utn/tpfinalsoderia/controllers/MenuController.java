package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.HelloApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;

public class MenuController {

    @FXML
    private BorderPane contenidoPrincipal;

    @FXML
    private Label mensaje;

    @FXML
    private void abrirClientes() {
        abrirPantalla("clientes-view.fxml");
    }

    @FXML
    private void abrirCatalogo() {
        abrirPantalla("catalogo-view.fxml");
    }

    @FXML
    private void abrirPedidos() {
        abrirPantalla("pedidos-view.fxml");
    }

    @FXML
    private void abrirInventario() {
        abrirPantalla("inventario-view.fxml");
    }

    @FXML
    private void abrirRetiros() {
        abrirPantalla("retiros-view.fxml");
    }

    @FXML
    private void abrirReportes() {
        abrirPantalla("reportes-view.fxml");
    }

    private void abrirPantalla(String archivo) {
        URL recurso = HelloApplication.class.getResource(archivo);

        if (recurso == null) {
            mensaje.setText("Todavía no se encontró la pantalla: " + archivo);
            return;
        }

        try {
            Parent pantalla = FXMLLoader.load(recurso);

            Button botonVolver = new Button("Volver al menú");
            botonVolver.setOnAction(evento -> mostrarMenu());

            HBox barra = new HBox(botonVolver);
            barra.setPadding(new Insets(10));

            contenidoPrincipal.setTop(barra);
            contenidoPrincipal.setCenter(pantalla);

        } catch (IOException e) {
            mensaje.setText("No se pudo abrir la pantalla: " + archivo);
            e.printStackTrace();
        }
    }

    private void mostrarMenu() {
        try {
            URL recurso = HelloApplication.class.getResource("menu-view.fxml");

            if (recurso == null) {
                mensaje.setText("No se encontró la pantalla del menú.");
                return;
            }

            Parent menu = FXMLLoader.load(recurso);

            contenidoPrincipal.getScene().setRoot(menu);

        } catch (IOException e) {
            mensaje.setText("No se pudo volver al menú.");
            e.printStackTrace();
        }
    }
}
