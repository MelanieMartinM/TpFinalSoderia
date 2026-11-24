package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc.Opcion;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Envase;
import ar.edu.utn.tpfinalsoderia.models.MovimientoCajon;
import ar.edu.utn.tpfinalsoderia.models.MovimientoEnvase;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.StockCajon;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;
import ar.edu.utn.tpfinalsoderia.models.StockProducto;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;
import ar.edu.utn.tpfinalsoderia.services.ServicioInventario;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class InventarioController {

    private static final String OP_RECEPCION = "1. Recepción de Envases Vacíos (Sucios)";
    private static final String OP_DEVOLUCION = "2. Devolución de Envases Vacíos (Retorno)";
    private static final String OP_LAVADO = "3. Lavado y Sanitizado (Sucios -> Aptos)";
    private static final String OP_PRODUCCION = "4. Orden de Producción (Aptos -> Producto Lleno)";

    private static final String CAJON_INGRESO_DEVOLUCION = "Devolución / Ingreso a Fábrica";
    private static final String CAJON_ENTREGA_SALIDA = "Entrega / Salida a Repartidor";

    @FXML private ComboBox<String> cmbOperacion;
    @FXML private ComboBox<Opcion<Cliente>> cmbCliente;
    @FXML private ComboBox<Opcion<Envase>> cmbEnvase;
    @FXML private ComboBox<Opcion<Presentacion>> cmbPresentacion;
    @FXML private TextField txtCantidad;
    @FXML private Label lblEstado;

    @FXML private ComboBox<String> cmbOpCajon;
    @FXML private ComboBox<Opcion<TipoCajon>> cmbCajonTipo;
    @FXML private ComboBox<Opcion<Cliente>> cmbCajonCliente;
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
    @FXML private TableColumn<StockCajon, String> colCajonTitular;
    @FXML private TableColumn<StockCajon, Number> colCajonCapacidad;
    @FXML private TableColumn<StockCajon, Number> colCajonDisponibles;

    @FXML private TableView<MovimientoCajon> tblMovCajones;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonFecha;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonTipoOp;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonCliente;
    @FXML private TableColumn<MovimientoCajon, String> colMovCajonTipo;
    @FXML private TableColumn<MovimientoCajon, Number> colMovCajonCant;

    private ServicioInventario servicioInventario;
    private OpcionesJdbc opciones;

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
            InventarioRepositoryJdbc inventarioRepo = new InventarioRepositoryJdbc(conexion);
            this.servicioInventario = new ServicioInventario(conexion, inventarioRepo);
            this.opciones = new OpcionesJdbc(conexion);
            cargarOpciones();
            actualizarCamposSegunOperacion();
            cargarDatosTablas();
        } catch (SQLException | IllegalStateException | PersistenciaException e) {
            mostrarError(lblEstado, "Sin conexión a BD: " + e.getMessage());
        }
    }

    private String formatearTitular(Cliente titular) {
        if (titular == null) return "Sin titular";
        String nombre = (titular.getNombre() != null && !titular.getNombre().isBlank())
                ? titular.getNombre()
                : "Repartidor sin nombre";
        return nombre;
    }

    private void configurarColumnas() {
        colEnvTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colEnvCodigo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null
                        ? (data.getValue().getEnvase().getDescripcion() != null ? data.getValue().getEnvase().getDescripcion() : "Envase sin descripción")
                        : "-"
        ));
        colEnvAptos.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAptos()));
        colEnvSucios.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSucios()));

        colProdTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colProdPresentacion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentacion() != null
                        ? (data.getValue().getPresentacion().getNombre() != null ? data.getValue().getPresentacion().getNombre() : "Presentación sin nombre")
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
                        ? (data.getValue().getEnvase().getDescripcion() != null ? data.getValue().getEnvase().getDescripcion() : "Envase sin descripción")
                        : "-"
        ));
        colMovCantidad.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidad()));

        colCajonTitular.setCellValueFactory(data -> new SimpleStringProperty(formatearTitular(data.getValue().getTitular())));
        colCajonTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().getDescripcion() : "-"
        ));
        colCajonCapacidad.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().getTipo() != null ? data.getValue().getTipo().getCapacidad() : 0
        ));
        colCajonDisponibles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDisponibles()));

        colMovCajonFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colMovCajonTipoOp.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isEsRetorno() ? "DEVOLUCIÓN / INGRESO" : "ENTREGA / SALIDA"
        ));
        colMovCajonCliente.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCliente() != null ? data.getValue().getCliente().getNombre() : "Sin titular"
        ));
        colMovCajonTipo.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipoCajon() != null ? data.getValue().getTipoCajon().getDescripcion() : "-"
        ));
        colMovCajonCant.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidad()));
    }

    @FXML
    private void cargarOpciones() {
        if (opciones == null) return;
        try {
            java.util.List<Opcion<Cliente>> clientes = opciones.clientes();
            cmbCliente.setItems(FXCollections.observableArrayList(clientes));
            cmbCajonCliente.setItems(FXCollections.observableArrayList(clientes));
            cmbEnvase.setItems(FXCollections.observableArrayList(opciones.envases()));
            cmbPresentacion.setItems(FXCollections.observableArrayList(opciones.presentaciones()));
            cmbCajonTipo.setItems(FXCollections.observableArrayList(opciones.tiposCajon()));
        } catch (PersistenciaException e) {
            mostrarError(lblEstado, e.getMessage());
        }
    }

    private void actualizarCamposSegunOperacion() {
        boolean esProduccion = OP_PRODUCCION.equals(cmbOperacion.getValue());
        cmbPresentacion.setDisable(!esProduccion);
        cmbEnvase.setDisable(esProduccion);
        if (esProduccion) cmbEnvase.getSelectionModel().clearSelection();
        else cmbPresentacion.getSelectionModel().clearSelection();
    }

    private <T> T valorSeleccionado(ComboBox<Opcion<T>> combo, String mensaje) throws ReglaNegocioException {
        Opcion<T> opcion = combo.getValue();
        if (opcion == null) throw new ReglaNegocioException(mensaje);
        return opcion.getValor();
    }

    @FXML
    private void onConfirmarOperacion() {
        if (servicioInventario == null) {
            mostrarError(lblEstado, "No hay conexión activa con la base de datos.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            Cliente titular = valorSeleccionado(cmbCliente, "Seleccioná el repartidor titular.");
            String op = cmbOperacion.getValue();
            if (OP_PRODUCCION.equals(op)) {
                Presentacion presentacion = valorSeleccionado(cmbPresentacion, "Seleccioná una presentación.");
                servicioInventario.registrarProduccion(titular, presentacion, cantidad);
                mostrarExito(lblEstado, "Producción de " + cantidad + " unidades registrada.");
            } else {
                Envase envase = valorSeleccionado(cmbEnvase, "Seleccioná un tipo de envase.");
                if (OP_RECEPCION.equals(op)) {
                    servicioInventario.registrarRecepcionVacios(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Recepción de " + cantidad + " envases registrada.");
                } else if (OP_DEVOLUCION.equals(op)) {
                    servicioInventario.registrarDevolucionEnvases(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Devolución de " + cantidad + " envases registrada.");
                } else if (OP_LAVADO.equals(op)) {
                    servicioInventario.registrarLavado(titular, envase, cantidad);
                    mostrarExito(lblEstado, "Lavado de " + cantidad + " envases registrado.");
                } else {
                    throw new ReglaNegocioException("Seleccioná una operación.");
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
        if (servicioInventario == null) {
            mostrarError(lblEstadoCajon, "No hay conexión activa con la base de datos.");
            return;
        }
        try {
            TipoCajon tipo = valorSeleccionado(cmbCajonTipo, "Seleccioná un tipo de cajón.");
            Cliente cliente = valorSeleccionado(cmbCajonCliente, "Seleccioná el repartidor titular.");
            int cantidad = Integer.parseInt(txtCajonCantidad.getText().trim());
            boolean esRetorno = CAJON_INGRESO_DEVOLUCION.equals(cmbOpCajon.getValue());
            servicioInventario.registrarMovimientoCajon(cliente, tipo.getId(), cantidad, esRetorno);
            mostrarExito(lblEstadoCajon, "Movimiento de " + cantidad + " cajones registrado.");
            cmbCajonTipo.getSelectionModel().clearSelection();
            cmbCajonCliente.getSelectionModel().clearSelection();
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
        if (servicioInventario == null) return;
        try {
            tblStockEnvases.setItems(FXCollections.observableArrayList(servicioInventario.listarStockEnvases()));
            tblStockProductos.setItems(FXCollections.observableArrayList(servicioInventario.listarStockProductos()));
            tblMovimientos.setItems(FXCollections.observableArrayList(servicioInventario.listarMovimientosEnvases()));
            tblStockCajones.setItems(FXCollections.observableArrayList(servicioInventario.listarStockCajones()));
            tblMovCajones.setItems(FXCollections.observableArrayList(servicioInventario.listarMovimientosCajones()));
        } catch (PersistenciaException e) {
            mostrarError(lblEstado, "Error al refrescar tablas: " + e.getMessage());
        }
    }

    @FXML
    private void onLimpiarFormulario() {
        cmbEnvase.getSelectionModel().clearSelection();
        cmbPresentacion.getSelectionModel().clearSelection();
        cmbCliente.getSelectionModel().clearSelection();
        txtCantidad.clear();
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
