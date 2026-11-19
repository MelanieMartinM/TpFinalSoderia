package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.MovimientoEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockEnvaseDAO;
import ar.edu.utn.tpfinalsoderia.DAO.StockProductoDAO;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.*;
import ar.edu.utn.tpfinalsoderia.services.InventarioService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class InventarioController {

    private static final String OP_RECEPCION = "1. Recepción de Envases Vacíos (Sucios)";
    private static final String OP_LAVADO = "2. Lavado y Sanitizado (Sucios -> Aptos)";
    private static final String OP_PRODUCCION = "3. Orden de Producción (Aptos -> Producto Lleno)";

    @FXML private ComboBox<String> cmbOperacion;
    @FXML private CheckBox chkEsFason;
    @FXML private TextField txtClienteId;
    @FXML private TextField txtEnvaseId;
    @FXML private TextField txtPresentacionId;
    @FXML private TextField txtCantidad;
    @FXML private Label lblEstado;

    @FXML private TableView<StockEnvase> tblStockEnvases;
    @FXML private TableColumn<StockEnvase, String> colEnvTitular;
    @FXML private TableColumn<StockEnvase, String> colEnvCodigo;
    @FXML private TableColumn<StockEnvase, Number> colEnvAptos;
    @FXML private TableColumn<StockEnvase, Number> colEnvSucios;

    @FXML private TableView<StockProducto> tblStockProductos;
    @FXML private TableColumn<StockProducto, String> colProdTitular;
    @FXML private TableColumn<StockProducto, String> colProdPresentacion;
    @FXML private TableColumn<StockProducto, Number> colProdDisponibles;

    @FXML private TableView<MovimientoEnvase> tblMovimientos;
    @FXML private TableColumn<MovimientoEnvase, String> colMovFecha;
    @FXML private TableColumn<MovimientoEnvase, String> colMovTipo;
    @FXML private TableColumn<MovimientoEnvase, String> colMovTitular;
    @FXML private TableColumn<MovimientoEnvase, String> colMovEnvase;
    @FXML private TableColumn<MovimientoEnvase, Number> colMovCantidad;

    private InventarioService inventarioService;

    @FXML
    public void initialize() {
        cmbOperacion.setItems(FXCollections.observableArrayList(OP_RECEPCION, OP_LAVADO, OP_PRODUCCION));
        cmbOperacion.getSelectionModel().selectFirst();
        cmbOperacion.setOnAction(e -> actualizarCamposSegunOperacion());

        configurarColumnas();
        inicializarServicio();
    }

    private void inicializarServicio() {
        try {
            Connection conexion = ConexionFactory.abrirConexion();
            this.inventarioService = new InventarioService(
                    new StockEnvaseDAO(conexion),
                    new StockProductoDAO(conexion),
                    new MovimientoEnvaseDAO(conexion)
            );
            cargarDatosTablas();
        } catch (SQLException | IllegalStateException e) {
            mostrarError("Sin conexión a BD: " + e.getMessage());
        }
    }

    private void configurarColumnas() {
        colEnvTitular.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTitular() != null ? "Fasón (" + data.getValue().getTitular().getId() + ")" : "Fábrica"
        ));
        colEnvCodigo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null ? data.getValue().getEnvase().getId() : "-"
        ));
        colEnvAptos.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAptos()));
        colEnvSucios.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSucios()));

        colProdTitular.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTitular() != null ? "Fasón (" + data.getValue().getTitular().getId() + ")" : "Fábrica"
        ));
        colProdPresentacion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentacion() != null ? "Presentación #" + data.getValue().getPresentacion().getId() : "-"
        ));
        colProdDisponibles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDisponibles()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colMovFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colMovTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().name() : "-"
        ));
        colMovTitular.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTitular() != null ? "Fasón (" + data.getValue().getTitular().getId() + ")" : "Fábrica"
        ));
        colMovEnvase.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null ? data.getValue().getEnvase().getId() : "-"
        ));
        colMovCantidad.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidad()));
    }

    @FXML
    private void onToggleFason() {
        boolean esFason = chkEsFason.isSelected();
        txtClienteId.setDisable(!esFason);
        if (!esFason) {
            txtClienteId.clear();
        }
    }

    private void actualizarCamposSegunOperacion() {
        String op = cmbOperacion.getValue();
        boolean esProduccion = OP_PRODUCCION.equals(op);
        txtPresentacionId.setDisable(!esProduccion);
        if (!esProduccion) {
            txtPresentacionId.clear();
        }
    }

    @FXML
    private void onConfirmarOperacion() {
        if (inventarioService == null) {
            mostrarError("No hay conexión activa con la base de datos.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            Cliente titular = null;
            if (chkEsFason.isSelected()) {
                String clienteId = txtClienteId.getText().trim();
                if (clienteId.isEmpty()) {
                    throw new ReglaNegocioException("Debe ingresar el ID del cliente fasón.");
                }
                titular = new Cliente("", clienteId, "", "", true);
            }

            String envaseId = txtEnvaseId.getText().trim();
            if (envaseId.isEmpty()) {
                throw new ReglaNegocioException("Debe ingresar el ID del envase.");
            }
            Envase envase = new Envase();
            envase.setId(envaseId);

            String op = cmbOperacion.getValue();
            if (OP_RECEPCION.equals(op)) {
                inventarioService.registrarRecepcionVacios(titular, envase, cantidad);
                mostrarExito("Recepción de " + cantidad + " envases vacíos registrada.");
            } else if (OP_LAVADO.equals(op)) {
                inventarioService.registrarLavado(titular, envase, cantidad);
                mostrarExito("Lavado de " + cantidad + " envases registrado (pasaron a APTOS).");
            } else if (OP_PRODUCCION.equals(op)) {
                int presId = Integer.parseInt(txtPresentacionId.getText().trim());
                Presentacion presentacion = new Presentacion();
                presentacion.setId(presId);
                presentacion.setEnvase(envase);

                inventarioService.registrarProduccion(titular, presentacion, cantidad);
                mostrarExito("Producción de " + cantidad + " unidades registrada.");
            }

            onLimpiarFormulario();
            cargarDatosTablas();

        } catch (NumberFormatException e) {
            mostrarError("Cantidad e ID de presentación deben ser números enteros válidos.");
        } catch (ReglaNegocioException | StockInsuficienteException | PersistenciaException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    public void cargarDatosTablas() {
        if (inventarioService == null) return;
        try {
            tblStockEnvases.setItems(FXCollections.observableArrayList(inventarioService.listarStockEnvases()));
            tblStockProductos.setItems(FXCollections.observableArrayList(inventarioService.listarStockProductos()));
            tblMovimientos.setItems(FXCollections.observableArrayList(inventarioService.listarMovimientosEnvases()));
        } catch (PersistenciaException e) {
            mostrarError("Error al refrescar tablas: " + e.getMessage());
        }
    }

    @FXML
    private void onLimpiarFormulario() {
        txtEnvaseId.clear();
        txtPresentacionId.clear();
        txtCantidad.clear();
        chkEsFason.setSelected(false);
        txtClienteId.clear();
        txtClienteId.setDisable(true);
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        lblEstado.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        lblEstado.setText(mensaje);
    }
}