package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.CatalogoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.MarcaRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ProductoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.services.ServicioCatalogo;
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CatalogoController {

    @FXML private TableView<Presentacion> tblPresentaciones;

    @FXML private TableColumn<Presentacion, String> colId;
    @FXML private TableColumn<Presentacion, String> colNombre;
    @FXML private TableColumn<Presentacion, String> colProducto;
    @FXML private TableColumn<Presentacion, String> colPrecio;
    @FXML private TableColumn<Presentacion, String> colEstado;

    @FXML private TextField txtNuevoPrecio;
    @FXML private Label lblEstado;

    @FXML private Button botonActualizarPrecio;
    @FXML private Button botonDesactivarPresentacion;

    private ServicioCatalogo servicioCatalogo;

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-AR")
            );

    @FXML
    public void initialize() {

        configurarColumnas();
        inicializarServicio();
        aplicarPermisos();
    }

    private void aplicarPermisos() {

        boolean administrador =
                SesionUsuario.esAdministrador();

        txtNuevoPrecio.setDisable(!administrador);

        if (botonActualizarPrecio != null) {
            botonActualizarPrecio.setDisable(!administrador);
        }

        if (botonDesactivarPresentacion != null) {
            botonDesactivarPresentacion.setDisable(!administrador);
        }
    }

    private boolean verificarPermisos() {

        if (!SesionUsuario.esAdministrador()) {
            mostrarError(
                    "No tenés permisos para modificar el catálogo."
            );
            return false;
        }

        return true;
    }

    private void inicializarServicio() {

        try {

            ProductoRepositoryJdbc productoRepo =
                    new ProductoRepositoryJdbc();

            MarcaRepositoryJdbc marcaRepo =
                    new MarcaRepositoryJdbc();

            CatalogoRepositoryJdbc catalogoRepo =
                    new CatalogoRepositoryJdbc();

            this.servicioCatalogo = new ServicioCatalogo(
                    productoRepo,
                    marcaRepo,
                    catalogoRepo
            );

            cargarCatalogo();

        } catch (PersistenciaException | IllegalStateException e) {
            mostrarError(
                    "Sin conexión a BD: " + e.getMessage()
            );
        }
    }

    private void configurarColumnas() {

        colId.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getId() != null
                                ? data.getValue().getId()
                                : "-"
                )
        );

        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre() != null
                                ? data.getValue().getNombre()
                                : "-"
                )
        );

        colProducto.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getProducto() != null
                                && data.getValue()
                                .getProducto().getNombre() != null

                                ? data.getValue()
                                .getProducto().getNombre()

                                : "-"
                )
        );

        colPrecio.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPrecioLlenado() != null

                                ? formatoMoneda.format(
                                data.getValue().getPrecioLlenado()
                        )

                                : formatoMoneda.format(BigDecimal.ZERO)
                )
        );

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().isActiva()
                                ? "Activa"
                                : "Inactiva"
                )
        );
    }

    @FXML
    public void cargarCatalogo() {

        if (servicioCatalogo == null) {
            return;
        }

        try {

            refrescarDatos();

            if (SesionUsuario.esAdministrador()) {
                lblEstado.setText("");
            } else {
                mostrarModoConsulta();
            }

        } catch (PersistenciaException | IllegalStateException e) {
            mostrarError(
                    "Error al cargar catálogo: " + e.getMessage()
            );
        }
    }

    private void refrescarDatos() {

        tblPresentaciones.setItems(
                FXCollections.observableArrayList(
                        servicioCatalogo.listarPresentaciones()
                )
        );
    }

    @FXML
    private void onActualizarPrecio() {

        if (!verificarPermisos()) {
            return;
        }

        if (servicioCatalogo == null) {
            mostrarError(
                    "No hay conexión activa con la base de datos."
            );
            return;
        }

        Presentacion seleccionada =
                tblPresentaciones.getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            mostrarError(
                    "Seleccioná una presentación de la tabla."
            );
            return;
        }

        try {

            String textoPrecio = txtNuevoPrecio.getText()
                    .trim()
                    .replace(',', '.');

            BigDecimal nuevoPrecio =
                    new BigDecimal(textoPrecio);

            servicioCatalogo.actualizarPrecio(
                    seleccionada,
                    nuevoPrecio
            );

            txtNuevoPrecio.clear();

            try {
                refrescarDatos();

                mostrarExito(
                        "Precio actualizado para "
                                + seleccionada.getNombre() + "."
                );

            } catch (PersistenciaException | IllegalStateException e) {
                mostrarError(
                        "El precio fue actualizado, pero no se pudo "
                                + "refrescar el catálogo."
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Ingresá un precio numérico válido."
            );

        } catch (IllegalArgumentException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onDesactivarPresentacion() {

        if (!verificarPermisos()) {
            return;
        }

        if (servicioCatalogo == null) {
            mostrarError(
                    "No hay conexión activa con la base de datos."
            );
            return;
        }

        Presentacion seleccionada =
                tblPresentaciones.getSelectionModel()
                        .getSelectedItem();

        if (seleccionada == null) {
            mostrarError(
                    "Seleccioná una presentación para desactivar."
            );
            return;
        }

        try {

            servicioCatalogo.desactivarPresentacion(
                    seleccionada
            );

            txtNuevoPrecio.clear();

            try {
                refrescarDatos();
                mostrarExito("Presentación desactivada.");

            } catch (PersistenciaException | IllegalStateException e) {
                mostrarError(
                        "La presentación fue desactivada, pero no "
                                + "se pudo refrescar el catálogo."
                );
            }

        } catch (IllegalArgumentException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    private void mostrarModoConsulta() {

        lblEstado.setStyle("-fx-text-fill: #283783;");

        lblEstado.setText(
                "Modo consulta: no tenés permisos para modificar el catálogo."
        );
    }

    private void mostrarExito(String mensaje) {

        lblEstado.setStyle(
                "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
        );

        lblEstado.setText(mensaje);
    }

    private void mostrarError(String mensaje) {

        lblEstado.setStyle(
                "-fx-text-fill: #c62828; -fx-font-weight: bold;"
        );

        lblEstado.setText(mensaje);
    }
}