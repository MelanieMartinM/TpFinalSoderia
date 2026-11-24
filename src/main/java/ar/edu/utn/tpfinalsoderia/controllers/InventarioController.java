package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
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

public class InventarioController
{

    private static final String OP_RECEPCION = "1. Recepción de Envases Vacíos (Sucios)";
    private static final String OP_DEVOLUCION = "2. Devolución de Envases Vacíos (Retorno)";
    private static final String OP_LAVADO = "3. Lavado y Sanitizado (Sucios -> Aptos)";
    private static final String OP_PRODUCCION = "4. Orden de Producción (Aptos -> Producto Lleno)";

    private static final String CAJON_INGRESO_DEVOLUCION = "Devolución / Ingreso a Fábrica";
    private static final String CAJON_ENTREGA_SALIDA = "Entrega / Salida a Repartidor";

    @FXML private ComboBox<String> cmbOperacion;
    @FXML private CheckBox chkEsFason;
    @FXML private TextField txtClienteId;
    @FXML private TextField txtEnvaseId;
    @FXML private TextField txtPresentacionId;
    @FXML private TextField txtCantidad;
    @FXML private Label lblEstado;

    @FXML private ComboBox<String> cmbOpCajon;
    @FXML private TextField txtCajonTipoId;
    @FXML private TextField txtCajonClienteId;
    @FXML private TextField txtCajonCantidad;
    @FXML private Label lblEstadoCajon;

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

    @FXML private TableView<StockCajon> tblStockCajones;
    @FXML private TableColumn<StockCajon, String> colCajonTipo;
    @FXML private TableColumn<StockCajon, Number> colCajonCapacidad;
    @FXML private TableColumn<StockCajon, Number> colCajonDisponibles;

    @FXML private TableView<MovimientoCajon> tblMovCajones;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonFecha;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonTipoOp;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonCliente;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonTipo;
    @FXML private TableColumn<MovimientoCajon, Number> colMovCajonCant;

    private InventarioService inventarioService;

    @FXML
    public void initialize() {
        cmbOperacion.setItems(FXCollections.observableArrayList(OP_RECEPCION, OP_DEVOLUCION, OP_LAVADO, OP_PRODUCCION));
        cmbOperacion.getSelectionModel().selectFirst();
        cmbOperacion.setOnAction(e -> actualizarCamposSegunOperacion());

        cmbOpCajon.setItems(FXCollections.observableArrayList(CAJON_INGRESO_DEVOLUCION, CAJON_ENTREGA_SALIDA));
        cmbOpCajon.getSelectionModel().selectFirst();

        configurarColumnas();
        inicializarServicio();
    }

    private void inicializarServicio() {
        try {
            Connection conexion = ConexionFactory.abrirConexion();
            this.inventarioService = new InventarioService(
                    conexion,
                    new StockEnvaseDAO(conexion),
                    new StockProductoDAO(conexion),
            );
            cargarDatosTablas();
        } catch (SQLException | IllegalStateException e) {
            mostrarError(lblEstado, "Sin conexión a BD: " + e.getMessage());
        }
    }

    private String formatearTitular(Cliente titular) {
        if (titular == null) return "Fábrica";
        String nombre = (titular.getNombre() != null && !titular.getNombre().isBlank())
                ? titular.getNombre()
                : titular.getId();
        return "Fasón - " + nombre;
    }

    private void configurarColumnas() {
        colEnvTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colEnvCodigo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null
                        ? (data.getValue().getEnvase().getDescripcion() != null ? data.getValue().getEnvase().getDescripcion() : data.getValue().getEnvase().getId())
                        : "-"
        ));
        colEnvAptos.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAptos()));
        colEnvSucios.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSucios()));

        colProdTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colProdPresentacion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentacion() != null
                        ? (data.getValue().getPresentacion().getNombre() != null ? data.getValue().getPresentacion().getNombre() : data.getValue().getPresentacion().getId())
                        : "-"
        ));
        colProdDisponibles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDisponibles()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colMovFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colMovTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().name() : "-"
        ));
        colMovTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colMovEnvase.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null
                        ? (data.getValue().getEnvase().getDescripcion() != null ? data.getValue().getEnvase().getDescripcion() : data.getValue().getEnvase().getId())
                        : "-"
        ));
        colMovCantidad.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidad()));

        colCajonTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().getDescripcion() : "-"
        ));
        colCajonCapacidad.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().getCapacidad() : 0
        ));
        colCajonDisponibles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDisponiblesFabrica()));

        colMovCajonFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colMovCajonTipoOp.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isEsRetorno() ? "DEVOLUCIÓN / INGRESO" : "ENTREGA / SALIDA"
        ));
        colMovCajonCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCliente() != null ? data.getValue().getCliente().getNombre() : "Fábrica"
        ));
        colMovCajonTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipoCajon() != null ? data.getValue().getTipoCajon().getDescripcion() : "-"
        ));
        colMovCajonCant.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidad()));
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
        txtEnvaseId.setDisable(esProduccion);
        if (esProduccion) {
            txtEnvaseId.clear();
        } else {
            txtPresentacionId.clear();
        }
    }

    @FXML
    private void onConfirmarOperacion() {
        if (inventarioService == null) {
            mostrarError(lblEstado, "No hay conexión activa con la base de datos.");
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

            String op = cmbOperacion.getValue();
            if (OP_PRODUCCION.equals(op)) {
                String presId = txtPresentacionId.getText().trim();
                if (presId.isEmpty()) {
                    throw new ReglaNegocioException("Debe ingresar el ID de la presentación.");
                }
                Presentacion presentacion = new Presentacion();
                presentacion.setId(presId);

                inventarioService.registrarProduccion(titular, presentacion, cantidad);
                mostrarExito(lblEstado, "Producción de " + cantidad + " unidades registrada.");
            } else {
                String envaseId = txtEnvaseId.getText().trim();
                if (envaseId.isEmpty()) {
                    throw new ReglaNegocioException("Debe ingresar el ID del envase.");
                }
                Envase envase = new Envase();
                envase.setId(envaseId);

                if (OP_RECEPCION.equals(op)) {
                    inventarioService.registrarRecepcionVacios(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Recepción de " + cantidad + " envases vacíos registrada.");
                } else if (OP_DEVOLUCION.equals(op)) {
                    inventarioService.registrarDevolucionEnvases(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Devolución de " + cantidad + " envases registrada.");
                } else if (OP_LAVADO.equals(op)) {
                    inventarioService.registrarLavado(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Lavado de " + cantidad + " envases registrado (pasaron a APTOS).");
                }
            }

            onLimpiarFormulario();
            cargarDatosTablas();

        } catch (NumberFormatException e) {
            mostrarError(lblEstado, "La cantidad debe ser un número entero válido.");
        } catch (ReglaNegocioException | StockInsuficienteException | PersistenciaException e) {
            mostrarError(lblEstado, e.getMessage());
        }
    }

    @FXML
    private void onConfirmarCajon() {
        if (inventarioService == null) {
            mostrarError(lblEstadoCajon, "No hay conexión activa con la base de datos.");
            return;
        }
        try {
            String tipoCajonId = txtCajonTipoId.getText().trim();
            int cantidad = Integer.parseInt(txtCajonCantidad.getText().trim());
            boolean esRetorno = CAJON_INGRESO_DEVOLUCION.equals(cmbOpCajon.getValue());

            String clienteId = txtCajonClienteId.getText().trim();
            Cliente cliente = clienteId.isEmpty() ? null : new Cliente("", clienteId, "", "", true);

            inventarioService.registrarMovimientoCajon(cliente, tipoCajonId, cantidad, esRetorno);
            mostrarExito(lblEstadoCajon, "Movimiento de " + cantidad + " cajones registrado.");
            txtCajonTipoId.clear();
            txtCajonClienteId.clear();
            txtCajonCantidad.clear();
            cargarDatosTablas();
        } catch (NumberFormatException e) {
            mostrarError(lblEstadoCajon, "La cantidad de cajones debe ser un número entero.");
        } catch (ReglaNegocioException | StockInsuficienteException | PersistenciaException e) {
            mostrarError(lblEstadoCajon, e.getMessage());
        }
    }

    @FXML
    public void cargarDatosTablas() {
        if (inventarioService == null) return;
        try {
            tblStockEnvases.setItems(FXCollections.observableArrayList(inventarioService.listarStockEnvases()));
            tblStockProductos.setItems(FXCollections.observableArrayList(inventarioService.listarStockProductos()));
            tblMovimientos.setItems(FXCollections.observableArrayList(inventarioService.listarMovimientosEnvases()));
            tblStockCajones.setItems(FXCollections.observableArrayList(inventarioService.listarStockCajones()));
            tblMovCajones.setItems(FXCollections.observableArrayList(inventarioService.listarMovimientosCajones()));
        } catch (PersistenciaException e) {
            mostrarError(lblEstado, "Error al refrescar tablas: " + e.getMessage());
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
        actualizarCamposSegunOperacion();
    }

    private void mostrarExito(Label target, String mensaje) {
        target.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        target.setText(mensaje);
    }

    private void mostrarError(Label target, String mensaje) {
        target.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        target.setText(mensaje);
    }
}