package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.enums.MedioDePago;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.exceptions.ReglaNegocioException;
import ar.edu.utn.tpfinalsoderia.exceptions.StockInsuficienteException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.DetalleRetiro;
import ar.edu.utn.tpfinalsoderia.models.Presentacion;
import ar.edu.utn.tpfinalsoderia.models.Retiro;
import ar.edu.utn.tpfinalsoderia.models.TipoCajon;
import ar.edu.utn.tpfinalsoderia.services.ServicioRetirosCobros;
import javafx.beans.property.SimpleObjectProperty;
import java.math.BigDecimal;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class RetirosController {

    @FXML private CheckBox chkEsFason;
    @FXML private TextField txtClienteId;
    @FXML private TextField txtPedidoId;
    @FXML private TextField txtDetallePedidoId;
    @FXML private TextField txtPresentacionId;
    @FXML private TextField txtCantidadPedida;
    @FXML private TextField txtCantidadRetirada;
    @FXML private TextField txtPrecioAplicado;
    @FXML private TextField txtTipoCajonId;
    @FXML private TextField txtCantidadCajones;

    @FXML private TableView<DetalleRetiro> tblDetalles;
    @FXML private TableColumn<DetalleRetiro, String> colDetPres;
    @FXML private TableColumn<DetalleRetiro, Number> colDetPedido;
    @FXML private TableColumn<DetalleRetiro, Number> colDetRetirado;
    @FXML private TableColumn<DetalleRetiro, Number> colDetPrecio;
    @FXML private TableColumn<DetalleRetiro, Number> colDetCajones;
    @FXML private TableColumn<DetalleRetiro, Number> colDetSubtotal;

    @FXML private Label lblTotalRetiro;
    @FXML private ComboBox<MedioDePago> cmbMedioPago;
    @FXML private TextField txtMontoCobro;
    @FXML private CheckBox chkSinCobro;
    @FXML private TextField txtResponsable;
    @FXML private TextField txtMotivo;
    @FXML private TextField txtRetiroPagoId;
    @FXML private Label lblEstado;

    @FXML private TableView<Cobro> tblCobros;
    @FXML private TableColumn<Cobro, String> colCobFecha;
    @FXML private TableColumn<Cobro, Number> colCobMonto;
    @FXML private TableColumn<Cobro, String> colCobMedio;
    @FXML private TableColumn<Cobro, String> colCobAut;

    private final ObservableList<DetalleRetiro> detallesCargados = FXCollections.observableArrayList();
    private ServicioRetirosCobros servicioRetirosCobros;

    @FXML
    public void initialize() {
        cmbMedioPago.setItems(FXCollections.observableArrayList(MedioDePago.values()));
        cmbMedioPago.getSelectionModel().selectFirst();
        tblDetalles.setItems(detallesCargados);
        chkEsFason.setSelected(true);
        chkEsFason.setDisable(true);
        txtClienteId.setDisable(false);
        configurarColumnas();
        inicializarServicio();
    }

    private void inicializarServicio() {
        try {
            Connection conexion = ConexionFactory.abrirConexion();
            RetiroCobroRepositoryJdbc retiroCobroRepo = new RetiroCobroRepositoryJdbc(conexion);
            InventarioRepositoryJdbc inventarioRepo = new InventarioRepositoryJdbc(conexion);
            this.servicioRetirosCobros = new ServicioRetirosCobros(
                    conexion,
                    retiroCobroRepo,
                    inventarioRepo
            );
            cargarCobros();
        } catch (SQLException | IllegalStateException e) {
            mostrarError("Sin conexión a BD: " + e.getMessage());
        }
    }

    private void configurarColumnas() {
        colDetPres.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentacion() != null ? data.getValue().getPresentacion().getId() : "-"
        ));
        colDetPedido.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidadPedida()));
        colDetRetirado.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidadRetirada()));
        colDetPrecio.setCellValueFactory(data -> new SimpleObjectProperty<Number>(data.getValue().getPrecioAplicado()));
        colDetCajones.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCantidadCajones()));
        colDetSubtotal.setCellValueFactory(data -> new SimpleObjectProperty<Number>(data.getValue().calcularSubtotal()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colCobFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colCobMonto.setCellValueFactory(data -> new SimpleObjectProperty<Number>(data.getValue().getMonto()));
        colCobMedio.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMedioPago() != null ? data.getValue().getMedioPago().name() : "-"
        ));
        colCobAut.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isSinCobroAutorizado()
                        ? "SIN COBRO (" + data.getValue().getResponsableAutorizacion() + ": " + data.getValue().getMotivoAutorizacion() + ")"
                        : "COBRADO"
        ));
    }

    @FXML
    private void onToggleFason() {
        txtClienteId.setDisable(!chkEsFason.isSelected());
        if (!chkEsFason.isSelected()) {
            txtClienteId.clear();
        }
    }

    @FXML
    private void onToggleSinCobro() {
        boolean sinCobro = chkSinCobro.isSelected();
        txtResponsable.setDisable(true);
        txtMotivo.setDisable(true);
        txtMontoCobro.setDisable(sinCobro);
        cmbMedioPago.setDisable(sinCobro);
        if (sinCobro) {
            txtMontoCobro.setText("0");
        }
    }

    @FXML
    private void onCobrarPosterior() {
        if (servicioRetirosCobros == null) {
            mostrarError("Sin conexión a la base de datos.");
            return;
        }
        try {
            BigDecimal monto = new BigDecimal(txtMontoCobro.getText().trim());
            Cobro cobro = new Cobro(null, monto, cmbMedioPago.getValue(),
                    LocalDateTime.now(), false, null, null);
            servicioRetirosCobros.registrarCobroPosterior(txtRetiroPagoId.getText().trim(), cobro);
            mostrarExito("Pago posterior registrado.");
            cargarCobros();
        } catch (NumberFormatException e) {
            mostrarError("El monto del cobro debe ser numérico.");
        } catch (ReglaNegocioException | PersistenciaException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onAgregarDetalle() {
        try {
            String presId = txtPresentacionId.getText().trim();
            if (presId.isEmpty()) {
                mostrarError("Ingresá el ID de presentación.");
                return;
            }
            int cantRetirada = Integer.parseInt(txtCantidadRetirada.getText().trim());
            BigDecimal precio = new BigDecimal(txtPrecioAplicado.getText().trim());
            int cantPedida = txtCantidadPedida.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtCantidadPedida.getText().trim());
            int cantCajones = txtCantidadCajones.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtCantidadCajones.getText().trim());

            Presentacion pres = new Presentacion();
            pres.setId(presId);

            TipoCajon cajon = null;
            String cajonId = txtTipoCajonId.getText().trim();
            if (!cajonId.isEmpty()) {
                cajon = new TipoCajon();
                cajon.setId(cajonId);
            }

            DetalleRetiro det = new DetalleRetiro(
                    null,
                    txtPedidoId.getText().trim(),
                    txtDetallePedidoId.getText().trim(),
                    pres,
                    cantPedida,
                    cantRetirada,
                    precio,
                    cajon,
                    cantCajones
            );
            detallesCargados.add(det);
            actualizarTotal();

            txtPresentacionId.clear();
            txtDetallePedidoId.clear();
            txtCantidadPedida.clear();
            txtCantidadRetirada.clear();
            txtPrecioAplicado.clear();
            txtTipoCajonId.clear();
            txtCantidadCajones.clear();
        } catch (NumberFormatException e) {
            mostrarError("Revisá los valores numéricos del detalle.");
        }
    }

    private void actualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleRetiro d : detallesCargados) {
            total = total.add(d.calcularSubtotal());
        }
        lblTotalRetiro.setText("Total a retirar: $" + total);
        if (!chkSinCobro.isSelected()) {
            txtMontoCobro.setText(String.valueOf(total));
        }
    }

    @FXML
    private void onConfirmarRetiro() {
        if (servicioRetirosCobros == null) {
            mostrarError("Sin conexión a la base de datos.");
            return;
        }
        try {
            Cliente cliente = null;
            if (chkEsFason.isSelected()) {
                String cliId = txtClienteId.getText().trim();
                if (cliId.isEmpty()) {
                    throw new ReglaNegocioException("Debe ingresar el ID del cliente fasón.");
                }
                cliente = new Cliente();
                cliente.setId(cliId);
            }

            Retiro retiro = new Retiro(
                    null,
                    txtPedidoId.getText().trim(),
                    cliente,
                    LocalDateTime.now(),
                    new ArrayList<>(detallesCargados),
                    null
            );

            BigDecimal monto = chkSinCobro.isSelected() ? BigDecimal.ZERO : new BigDecimal(txtMontoCobro.getText().trim());
            Cobro cobro = new Cobro(
                    null,
                    monto,
                    cmbMedioPago.getValue(),
                    LocalDateTime.now(),
                    chkSinCobro.isSelected(),
                    txtResponsable.getText().trim(),
                    txtMotivo.getText().trim()
            );

            servicioRetirosCobros.registrarRetiroYCobro(retiro, cobro);
            mostrarExito("Retiro y cobro registrados. " + retiro.generarComprobante());

            detallesCargados.clear();
            actualizarTotal();
            cargarCobros();
        } catch (NumberFormatException e) {
            mostrarError("El monto del cobro debe ser numérico.");
        } catch (ReglaNegocioException | StockInsuficienteException | PersistenciaException e) {
            mostrarError(e.getMessage());
        }
    }

    private void cargarCobros() {
        if (servicioRetirosCobros != null) {
            tblCobros.setItems(FXCollections.observableArrayList(servicioRetirosCobros.listarCobros()));
        }
    }

    private void mostrarExito(String msg) {
        lblEstado.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        lblEstado.setText(msg);
    }

    private void mostrarError(String msg) {
        lblEstado.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        lblEstado.setText(msg);
    }
}
