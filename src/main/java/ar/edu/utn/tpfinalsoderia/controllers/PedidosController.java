package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.CatalogoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.PedidoRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.DetallePedido;
import ar.edu.utn.tpfinalsoderia.models.Pedido;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;
import ar.edu.utn.tpfinalsoderia.services.ServicioPedidos;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PedidosController {

    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<Presentacion> cmbPresentacion;
    @FXML private ComboBox<TipoCajon> cmbTipoCajon;
    @FXML private TextField txtCantidadUnidades;
    @FXML private TextField txtCantidadCajones;

    @FXML private TableView<DetallePedido> tblDetalles;
    @FXML private TableColumn<DetallePedido, String> colPresentacion;
    @FXML private TableColumn<DetallePedido, Number> colUnidades;
    @FXML private TableColumn<DetallePedido, Number> colCajones;
    @FXML private TableColumn<DetallePedido, String> colSubtotal;

    @FXML private Label lblTotal;
    @FXML private Label lblEstado;

    private ServicioPedidos servicioPedidos;
    private Pedido pedidoActual;
    private final ObservableList<DetallePedido> detalles = FXCollections.observableArrayList();
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    @FXML
    public void initialize() {
        configurarColumnas();
        tblDetalles.setItems(detalles);
        inicializarServicio();
        actualizarTotal();
    }

    private void inicializarServicio() {
        try {

            PedidoRepositoryJdbc pedidoRepo = new PedidoRepositoryJdbc();
            CatalogoRepositoryJdbc catalogoRepo = new CatalogoRepositoryJdbc();
            this.servicioPedidos = new ServicioPedidos(pedidoRepo, catalogoRepo);
            cargarPresentaciones();
        } catch ( PersistenciaException | IllegalStateException e) {
            mostrarError("Sin conexión a BD: " + e.getMessage());
        }
    }

    public void cargarDatosIniciales(List<Cliente> clientes, List<Presentacion> presentaciones, List<TipoCajon> tiposCajon) {
        if (clientes != null) {
            cmbCliente.setItems(FXCollections.observableArrayList(clientes));
        }
        if (presentaciones != null) {
            cmbPresentacion.setItems(FXCollections.observableArrayList(presentaciones));
        }
        if (tiposCajon != null) {
            cmbTipoCajon.setItems(FXCollections.observableArrayList(tiposCajon));
        }
    }

    private void cargarPresentaciones() {
        if (servicioPedidos == null) return;
        try {
            cmbPresentacion.setItems(FXCollections.observableArrayList(servicioPedidos.listarPresentacionesDisponibles()));
        } catch (PersistenciaException e) {
            mostrarError("Error al cargar presentaciones: " + e.getMessage());
        }
    }

    private void configurarColumnas() {
        colPresentacion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentacion() != null && data.getValue().getPresentacion().getNombre() != null
                        ? data.getValue().getPresentacion().getNombre()
                        : "-"
        ));
        colUnidades.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidadUnidades()));
        colCajones.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidadCajones()));
        colSubtotal.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().calcularSubtotal() != null
                        ? formatoMoneda.format(data.getValue().calcularSubtotal())
                        : formatoMoneda.format(BigDecimal.ZERO)
        ));
    }

    @FXML
    private void onAgregarDetalle() {
        if (servicioPedidos == null) {
            mostrarError("No hay conexión activa con la base de datos.");
            return;
        }
        try {
            Cliente cliente = cmbCliente.getValue();
            Presentacion presentacion = cmbPresentacion.getValue();
            TipoCajon tipoCajon = cmbTipoCajon.getValue();

            int unidades = Integer.parseInt(txtCantidadUnidades.getText().trim());
            int cajones = txtCantidadCajones.getText().trim().isEmpty()
                    ? 0
                    : Integer.parseInt(txtCantidadCajones.getText().trim());

            if (pedidoActual == null) {
                pedidoActual = servicioPedidos.crearPedido(cliente);
            }

            servicioPedidos.agregarItem(
                    pedidoActual,
                    presentacion,
                    unidades,
                    cajones,
                    tipoCajon,
                    null
            );

            detalles.setAll(pedidoActual.getDetalles());
            limpiarCamposItem();
            actualizarTotal();
            mostrarExito("Ítem agregado al pedido.");
        } catch (NumberFormatException e) {
            mostrarError("Ingresá números enteros válidos en unidades y cajones.");
        } catch (ReglaNegocioException | PersistenciaException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onConfirmarPedido() {
        if (servicioPedidos == null) {
            mostrarError("No hay conexión activa con la base de datos.");
            return;
        }
        if (pedidoActual == null) {
            mostrarError("Primero creá un pedido y agregá al menos un ítem.");
            return;
        }
        try {
            servicioPedidos.confirmarPedido(pedidoActual);
            mostrarExito("Pedido confirmado correctamente (ID: " + pedidoActual.getId() + ").");
            pedidoActual = null;
            detalles.clear();
            actualizarTotal();
        } catch (ReglaNegocioException | PersistenciaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizarTotal() {
        BigDecimal total = detalles.stream()
                .map(DetallePedido::calcularSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (lblTotal != null) {
            lblTotal.setText("Total: " + formatoMoneda.format(total));
        }
    }

    private void limpiarCamposItem() {
        txtCantidadUnidades.clear();
        txtCantidadCajones.clear();
        cmbPresentacion.getSelectionModel().clearSelection();
        cmbTipoCajon.getSelectionModel().clearSelection();
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