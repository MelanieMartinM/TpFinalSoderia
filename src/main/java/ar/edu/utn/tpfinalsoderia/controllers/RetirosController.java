package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc.DetalleOpcion;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc.Opcion;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc.PedidoOpcion;
import ar.edu.utn.tpfinalsoderia.DAO.OpcionesJdbc.RetiroOpcion;
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
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class RetirosController {

    @FXML private ComboBox<Opcion<Cliente>> cmbCliente;
    @FXML private ComboBox<Opcion<PedidoOpcion>> cmbPedido;
    @FXML private ComboBox<Opcion<DetalleOpcion>> cmbDetallePedido;
    @FXML private ComboBox<Opcion<Presentacion>> cmbPresentacion;
    @FXML private ComboBox<Opcion<TipoCajon>> cmbTipoCajon;
    @FXML private ComboBox<Opcion<RetiroOpcion>> cmbRetiroPendiente;

    @FXML private TextField txtCantidadPedida;
    @FXML private TextField txtCantidadRetirada;
    @FXML private TextField txtPrecioAplicado;
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

    // Nuevos campos para la autorización.
    @FXML private TextField txtMotivoAutorizacion;
    @FXML private Label lblResponsableAutorizacion;

    @FXML private ComboBox<MedioDePago> cmbMedioPosterior;
    @FXML private TextField txtMontoPosterior;

    @FXML private Label lblEstado;

    @FXML private TableView<Cobro> tblCobros;

    @FXML private TableColumn<Cobro, String> colCobFecha;
    @FXML private TableColumn<Cobro, Number> colCobMonto;
    @FXML private TableColumn<Cobro, String> colCobMedio;
    @FXML private TableColumn<Cobro, String> colCobAut;

    private final ObservableList<DetalleRetiro> detallesCargados =
            FXCollections.observableArrayList();

    private ServicioRetirosCobros servicio;
    private OpcionesJdbc opciones;

    @FXML
    public void initialize() {

        cmbMedioPago.setItems(
                FXCollections.observableArrayList(
                        MedioDePago.values()
                )
        );

        cmbMedioPosterior.setItems(
                FXCollections.observableArrayList(
                        MedioDePago.values()
                )
        );

        cmbMedioPago.getSelectionModel().selectFirst();
        cmbMedioPosterior.getSelectionModel().selectFirst();

        cmbDetallePedido.setDisable(true);
        txtCantidadPedida.setEditable(false);
        txtPrecioAplicado.setEditable(false);
        txtMontoCobro.setEditable(false);

        tblDetalles.setItems(detallesCargados);

        configurarColumnas();
        aplicarPermisos();
        inicializarServicio();
        actualizarTotal();
    }

    // =====================================================
    // PERMISOS
    // =====================================================

    private void aplicarPermisos() {

        boolean administrador =
                SesionUsuario.esAdministrador();

        chkSinCobro.setSelected(false);
        chkSinCobro.setDisable(!administrador);

        if (txtMotivoAutorizacion != null) {
            txtMotivoAutorizacion.setDisable(true);
            txtMotivoAutorizacion.clear();
        }

        if (lblResponsableAutorizacion != null) {

            lblResponsableAutorizacion.setText(
                    administrador
                            ? "Responsable: "
                            + SesionUsuario.getUsuarioActual()
                            .getNombreUsuario()
                            : "Solo un administrador puede autorizar."
            );
        }
    }

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    private void inicializarServicio() {

        try {

            Connection conexion =
                    ConexionFactory.abrirConexion();

            servicio = new ServicioRetirosCobros(
                    conexion,
                    new RetiroCobroRepositoryJdbc(conexion),
                    new InventarioRepositoryJdbc(conexion)
            );

            opciones = new OpcionesJdbc(conexion);

            cargarOpciones();
            cargarCobros();

        } catch (SQLException |
                 IllegalStateException |
                 PersistenciaException e) {

            mostrarError(
                    "No se pudo conectar o cargar la información: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // CARGAR OPCIONES
    // =====================================================

    @FXML
    private void cargarOpciones() {

        if (opciones == null) {
            return;
        }

        if (!detallesCargados.isEmpty()) {

            mostrarError(
                    "Vaciá la lista de productos antes de actualizar las opciones."
            );

            return;
        }

        try {

            cmbCliente.setItems(
                    FXCollections.observableArrayList(
                            opciones.clientes()
                    )
            );

            cmbPresentacion.setItems(
                    FXCollections.observableArrayList(
                            opciones.presentaciones()
                    )
            );

            cmbTipoCajon.setItems(
                    FXCollections.observableArrayList(
                            opciones.tiposCajon()
                    )
            );

            onSeleccionCliente();

        } catch (PersistenciaException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onSeleccionCliente() {

        cmbPedido.getSelectionModel().clearSelection();
        cmbPedido.getItems().clear();

        cmbDetallePedido.getSelectionModel().clearSelection();
        cmbDetallePedido.getItems().clear();

        cmbRetiroPendiente.getSelectionModel().clearSelection();
        cmbRetiroPendiente.getItems().clear();

        onSeleccionPedido();

        if (opciones == null || cmbCliente.getValue() == null) {
            return;
        }

        try {

            String id =
                    cmbCliente.getValue().getValor().getId();

            cmbPedido.setItems(
                    FXCollections.observableArrayList(
                            opciones.pedidos(id)
                    )
            );

            cmbRetiroPendiente.setItems(
                    FXCollections.observableArrayList(
                            opciones.retirosPendientes(id)
                    )
            );

        } catch (PersistenciaException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onSeleccionPedido() {

        cmbDetallePedido.getItems().clear();

        txtCantidadPedida.clear();
        txtPrecioAplicado.clear();

        Opcion<PedidoOpcion> pedido = cmbPedido.getValue();

        boolean conPedido = pedido != null;

        cmbDetallePedido.setDisable(!conPedido);
        cmbPresentacion.setDisable(conPedido);

        txtPrecioAplicado.setEditable(false);

        if (conPedido && opciones != null) {

            cmbPresentacion.getSelectionModel().clearSelection();

            try {

                cmbDetallePedido.setItems(
                        FXCollections.observableArrayList(
                                opciones.detalles(
                                        pedido.getValor().id
                                )
                        )
                );

            } catch (PersistenciaException e) {

                mostrarError(e.getMessage());
            }

        } else {

            onSeleccionPresentacion();
        }
    }

    @FXML
    private void onQuitarPedido() {

        if (!detallesCargados.isEmpty()) {

            mostrarError(
                    "Vaciá la lista de productos antes de cambiar el pedido."
            );

            return;
        }

        cmbPedido.getSelectionModel().clearSelection();

        onSeleccionPedido();
    }

    @FXML
    private void onSeleccionDetalle() {

        Opcion<DetalleOpcion> opcion =
                cmbDetallePedido.getValue();

        if (opcion == null) {

            txtCantidadPedida.clear();

            if (cmbPedido.getValue() != null) {
                txtPrecioAplicado.clear();
            }

            return;
        }

        DetalleOpcion detalle = opcion.getValor();

        txtCantidadPedida.setText(
                String.valueOf(detalle.pendiente)
        );

        txtPrecioAplicado.setText(
                detalle.precio.toPlainString()
        );
    }

    @FXML
    private void onSeleccionPresentacion() {

        if (cmbPedido.getValue() != null) {
            return;
        }

        Opcion<Presentacion> opcion =
                cmbPresentacion.getValue();

        if (opcion != null
                && opcion.getValor().getPrecioLlenado() != null) {

            txtPrecioAplicado.setText(
                    opcion.getValor()
                            .getPrecioLlenado()
                            .toPlainString()
            );

        } else {

            txtPrecioAplicado.clear();
        }
    }

    // =====================================================
    // TABLAS
    // =====================================================

    private void configurarColumnas() {

        colDetPres.setCellValueFactory(f ->
                new SimpleStringProperty(
                        f.getValue().getPresentacion().getNombre()
                )
        );

        colDetPedido.setCellValueFactory(f ->
                new SimpleIntegerProperty(
                        f.getValue().getCantidadPedida()
                )
        );

        colDetRetirado.setCellValueFactory(f ->
                new SimpleIntegerProperty(
                        f.getValue().getCantidadRetirada()
                )
        );

        colDetPrecio.setCellValueFactory(f ->
                new SimpleObjectProperty<Number>(
                        f.getValue().getPrecioAplicado()
                )
        );

        colDetCajones.setCellValueFactory(f ->
                new SimpleIntegerProperty(
                        f.getValue().getCantidadCajones()
                )
        );

        colDetSubtotal.setCellValueFactory(f ->
                new SimpleObjectProperty<Number>(
                        f.getValue().calcularSubtotal()
                )
        );

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colCobFecha.setCellValueFactory(f ->
                new SimpleStringProperty(
                        f.getValue().getFecha() == null
                                ? "-"
                                : f.getValue().getFecha().format(formato)
                )
        );

        colCobMonto.setCellValueFactory(f ->
                new SimpleObjectProperty<Number>(
                        f.getValue().getMonto()
                )
        );

        colCobMedio.setCellValueFactory(f ->
                new SimpleStringProperty(
                        f.getValue().getMedioPago() == null
                                ? "-"
                                : f.getValue().getMedioPago().name()
                )
        );

        colCobAut.setCellValueFactory(f ->
                new SimpleStringProperty(
                        f.getValue().isSinCobroAutorizado()
                                ? "Sin cobro autorizado"
                                : "Cobrado"
                )
        );
    }

    // =====================================================
    // AUTORIZACIÓN SIN PAGO
    // =====================================================

    @FXML
    private void onToggleSinCobro() {

        if (chkSinCobro.isSelected()
                && !SesionUsuario.esAdministrador()) {

            chkSinCobro.setSelected(false);

            mostrarError(
                    "Solo un administrador puede autorizar retiros sin pago."
            );
        }

        boolean pendiente = chkSinCobro.isSelected();

        txtMontoCobro.setDisable(pendiente);
        cmbMedioPago.setDisable(pendiente);

        if (txtMotivoAutorizacion != null) {

            txtMotivoAutorizacion.setDisable(!pendiente);

            if (!pendiente) {
                txtMotivoAutorizacion.clear();
            }
        }

        actualizarTotal();
    }

    // =====================================================
    // AGREGAR PRODUCTOS AL RETIRO
    // =====================================================

    @FXML
    private void onAgregarDetalle() {

        if (servicio == null) {

            mostrarError(
                    "No hay conexión activa con la base de datos."
            );

            return;
        }

        try {

            if (cmbCliente.getValue() == null) {

                throw new ReglaNegocioException(
                        "Seleccioná un repartidor."
                );
            }

            int cantidad = Integer.parseInt(
                    txtCantidadRetirada.getText().trim()
            );

            int cajones =
                    txtCantidadCajones.getText().isBlank()
                            ? 0
                            : Integer.parseInt(
                            txtCantidadCajones.getText().trim()
                    );

            if (cantidad <= 0 || cajones < 0) {

                throw new ReglaNegocioException(
                        "Revisá las cantidades ingresadas."
                );
            }

            TipoCajon tipo =
                    cmbTipoCajon.getValue() == null
                            ? null
                            : cmbTipoCajon.getValue().getValor();

            if (cajones > 0 && tipo == null) {

                throw new ReglaNegocioException(
                        "Seleccioná el tipo de cajón."
                );
            }

            PedidoOpcion pedido =
                    cmbPedido.getValue() == null
                            ? null
                            : cmbPedido.getValue().getValor();

            DetalleOpcion detalle =
                    cmbDetallePedido.getValue() == null
                            ? null
                            : cmbDetallePedido.getValue().getValor();

            Presentacion presentacion;
            BigDecimal precio;

            int pedida = 0;

            if (pedido != null) {

                if (detalle == null) {

                    throw new ReglaNegocioException(
                            "Seleccioná un producto pendiente del pedido."
                    );
                }

                if (cantidad > detalle.pendiente) {

                    throw new ReglaNegocioException(
                            "La cantidad supera lo pendiente del pedido."
                    );
                }

                for (DetalleRetiro existente : detallesCargados) {

                    if (detalle.id.equals(
                            existente.getDetallePedidoId())) {

                        throw new ReglaNegocioException(
                                "Ese producto del pedido ya fue agregado."
                        );
                    }
                }

                presentacion = detalle.presentacion;
                pedida = detalle.pedida;
                precio = detalle.precio;

            } else {

                if (cmbPresentacion.getValue() == null) {

                    throw new ReglaNegocioException(
                            "Seleccioná una presentación."
                    );
                }

                presentacion =
                        cmbPresentacion.getValue().getValor();

                precio = presentacion.getPrecioLlenado();
            }

            if (precio == null || precio.signum() < 0) {

                throw new ReglaNegocioException(
                        "La presentación no tiene un precio válido."
                );
            }

            detallesCargados.add(
                    new DetalleRetiro(
                            null,
                            pedido == null ? null : pedido.id,
                            detalle == null ? null : detalle.id,
                            presentacion,
                            pedida,
                            cantidad,
                            precio,
                            tipo,
                            cajones
                    )
            );

            cmbCliente.setDisable(true);
            cmbPedido.setDisable(true);

            limpiarCamposDetalle();
            actualizarTotal();

            mostrarExito("Producto agregado.");

        } catch (NumberFormatException e) {

            mostrarError(
                    "Revisá las cantidades ingresadas."
            );

        } catch (ReglaNegocioException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onVaciarDetalles() {

        detallesCargados.clear();

        cmbCliente.setDisable(false);
        cmbPedido.setDisable(false);

        limpiarCamposDetalle();

        actualizarTotal();
    }

    private void limpiarCamposDetalle() {

        cmbTipoCajon.getSelectionModel().clearSelection();
        cmbDetallePedido.getSelectionModel().clearSelection();
        cmbPresentacion.getSelectionModel().clearSelection();

        txtCantidadPedida.clear();
        txtCantidadRetirada.clear();
        txtCantidadCajones.clear();
        txtPrecioAplicado.clear();
    }

    private void actualizarTotal() {

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleRetiro detalle : detallesCargados) {

            total = total.add(
                    detalle.calcularSubtotal()
            );
        }

        lblTotalRetiro.setText(
                "Total a retirar: $" + total.toPlainString()
        );

        txtMontoCobro.setText(
                chkSinCobro.isSelected()
                        ? "0.00"
                        : total.toPlainString()
        );
    }

    // =====================================================
    // CONFIRMAR RETIRO
    // =====================================================

    @FXML
    private void onConfirmarRetiro() {

        if (servicio == null) {

            mostrarError(
                    "No hay conexión activa con la base de datos."
            );

            return;
        }

        try {

            if (cmbCliente.getValue() == null) {

                throw new ReglaNegocioException(
                        "Seleccioná un repartidor."
                );
            }

            if (detallesCargados.isEmpty()) {

                throw new ReglaNegocioException(
                        "Agregá al menos un producto."
                );
            }

            Cliente cliente =
                    cmbCliente.getValue().getValor();

            PedidoOpcion pedido =
                    cmbPedido.getValue() == null
                            ? null
                            : cmbPedido.getValue().getValor();

            Retiro retiro = new Retiro(
                    null,
                    pedido == null ? null : pedido.id,
                    cliente,
                    LocalDateTime.now(),
                    new ArrayList<>(detallesCargados),
                    null
            );

            Cobro cobro;

            if (chkSinCobro.isSelected()) {

                if (!SesionUsuario.esAdministrador()) {

                    throw new ReglaNegocioException(
                            "No tenés permisos para autorizar retiros sin pago."
                    );
                }

                String motivo =
                        txtMotivoAutorizacion == null
                                ? ""
                                : txtMotivoAutorizacion.getText().trim();

                if (motivo.isBlank()) {

                    throw new ReglaNegocioException(
                            "Ingresá el motivo de la autorización."
                    );
                }

                cobro = new Cobro(
                        null,
                        BigDecimal.ZERO,
                        null,
                        LocalDateTime.now(),
                        true,
                        SesionUsuario.getUsuarioActual()
                                .getNombreUsuario(),
                        motivo
                );

            } else {

                BigDecimal monto = new BigDecimal(
                        txtMontoCobro.getText()
                                .trim()
                                .replace(',', '.')
                );

                cobro = new Cobro(
                        null,
                        monto,
                        cmbMedioPago.getValue(),
                        LocalDateTime.now(),
                        false,
                        null,
                        null
                );
            }

            servicio.registrarRetiroYCobro(retiro, cobro);

            try {

                limpiarFormularioRetiro();
                onSeleccionCliente();
                cargarCobros();

                mostrarExito(
                        "Retiro registrado para "
                                + cliente.getNombre() + "."
                );

            } catch (RuntimeException e) {

                mostrarError(
                        "El retiro fue registrado, pero ocurrió "
                                + "un error al actualizar la pantalla."
                );

                e.printStackTrace();
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Ingresá un monto numérico válido."
            );

        } catch (ReglaNegocioException |
                 StockInsuficienteException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    private void limpiarFormularioRetiro() {

        detallesCargados.clear();

        cmbCliente.setDisable(false);
        cmbPedido.setDisable(false);

        cmbPedido.getSelectionModel().clearSelection();

        limpiarCamposDetalle();

        chkSinCobro.setSelected(false);

        cmbMedioPago.getSelectionModel().selectFirst();

        txtMontoPosterior.clear();

        onSeleccionPedido();
        onToggleSinCobro();
    }

    // =====================================================
    // PAGOS POSTERIORES
    // =====================================================

    @FXML
    private void onCobrarPosterior() {

        if (servicio == null) {

            mostrarError(
                    "No hay conexión activa con la base de datos."
            );

            return;
        }

        try {

            if (cmbRetiroPendiente.getValue() == null) {

                throw new ReglaNegocioException(
                        "Seleccioná un retiro pendiente."
                );
            }

            BigDecimal monto = new BigDecimal(
                    txtMontoPosterior.getText()
                            .trim()
                            .replace(',', '.')
            );

            Cobro cobro = new Cobro(
                    null,
                    monto,
                    cmbMedioPosterior.getValue(),
                    LocalDateTime.now(),
                    false,
                    null,
                    null
            );

            servicio.registrarCobroPosterior(
                    cmbRetiroPendiente.getValue().getValor().id,
                    cobro
            );

            try {

                txtMontoPosterior.clear();

                cmbMedioPosterior.getSelectionModel().selectFirst();

                if (detallesCargados.isEmpty()) {
                    onSeleccionCliente();
                } else {
                    String clienteId =
                            cmbCliente.getValue().getValor().getId();

                    cmbRetiroPendiente.setItems(
                            FXCollections.observableArrayList(
                                    opciones.retirosPendientes(clienteId)
                            )
                    );
                }

                cargarCobros();

                mostrarExito("Pago posterior registrado.");

            } catch (RuntimeException e) {

                mostrarError(
                        "El pago fue registrado, pero no se pudo "
                                + "actualizar la pantalla."
                );

                e.printStackTrace();
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "Ingresá un monto numérico válido."
            );

        } catch (ReglaNegocioException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    private void cargarCobros() {

        if (servicio == null) {
            return;
        }

        tblCobros.setItems(
                FXCollections.observableArrayList(
                        servicio.listarCobros()
                )
        );
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarExito(String mensaje) {

        lblEstado.setStyle("-fx-text-fill: #2e7d32;");
        lblEstado.setText(mensaje);
    }

    private void mostrarError(String mensaje) {

        lblEstado.setStyle("-fx-text-fill: #c62828;");
        lblEstado.setText(mensaje);
    }
}