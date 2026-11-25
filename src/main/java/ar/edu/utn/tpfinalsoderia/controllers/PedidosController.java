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
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    private final ObservableList<DetallePedido> detalles =
            FXCollections.observableArrayList();

    private final NumberFormat formatoMoneda =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-AR")
            );

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        configurarCombos();
        configurarColumnas();

        tblDetalles.setItems(detalles);

        actualizarTotal();

        if (!SesionUsuario.haySesionActiva()) {

            mostrarError("No hay una sesión activa.");

            return;
        }

        inicializarServicio();
    }

    // =====================================================
    // CONFIGURAR COMBOBOX
    // =====================================================

    private void configurarCombos() {

        cmbCliente.setConverter(new StringConverter<>() {

            @Override
            public String toString(Cliente cliente) {

                if (cliente == null) {
                    return "";
                }

                int posicion =
                        cmbCliente.getItems().indexOf(cliente) + 1;

                String contacto =
                        cliente.getTelefono() == null
                                || cliente.getTelefono().isBlank()
                                ? cliente.getDireccion()
                                : cliente.getTelefono();

                return (posicion > 0 ? posicion + ". " : "")
                        + cliente.getNombre()
                        + (contacto == null || contacto.isBlank()
                        ? ""
                        : " — " + contacto);
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });

        cmbPresentacion.setConverter(new StringConverter<>() {

            @Override
            public String toString(Presentacion presentacion) {

                return presentacion == null
                        ? ""
                        : presentacion.getNombre();
            }

            @Override
            public Presentacion fromString(String texto) {
                return null;
            }
        });

        cmbTipoCajon.setConverter(new StringConverter<>() {

            @Override
            public String toString(TipoCajon cajon) {

                return cajon == null
                        ? ""
                        : cajon.getDescripcion()
                        + " — capacidad "
                        + cajon.getCapacidad();
            }

            @Override
            public TipoCajon fromString(String texto) {
                return null;
            }
        });
    }

    // =====================================================
    // INICIALIZAR SERVICIO
    // =====================================================

    private void inicializarServicio() {

        try {

            PedidoRepositoryJdbc pedidoRepo =
                    new PedidoRepositoryJdbc();

            CatalogoRepositoryJdbc catalogoRepo =
                    new CatalogoRepositoryJdbc();

            this.servicioPedidos =
                    new ServicioPedidos(
                            pedidoRepo,
                            catalogoRepo
                    );

            cargarPresentaciones();
            cargarRepartidoresYTipos();

        } catch (PersistenciaException |
                 IllegalStateException |
                 SQLException e) {

            servicioPedidos = null;

            mostrarError(
                    "No se pudo inicializar el módulo: "
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // CARGAR REPARTIDORES Y TIPOS DE CAJÓN
    // =====================================================

    private void cargarRepartidoresYTipos()
            throws SQLException {

        cmbCliente.getItems().clear();
        cmbTipoCajon.getItems().clear();

        String sqlClientes =
                "SELECT id, nombre, telefono, direccion "
                        + "FROM clientes "
                        + "WHERE activo = TRUE "
                        + "ORDER BY nombre, id";

        String sqlTipos =
                "SELECT id, codigo, descripcion, capacidad "
                        + "FROM tipos_cajon "
                        + "ORDER BY descripcion";

        try (Connection conexion =
                     ConexionFactory.abrirConexion()) {

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlClientes);

                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Cliente cliente = new Cliente();

                    cliente.setId(rs.getString("id"));
                    cliente.setNombre(rs.getString("nombre"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setDireccion(rs.getString("direccion"));

                    cmbCliente.getItems().add(cliente);
                }
            }

            try (PreparedStatement ps =
                         conexion.prepareStatement(sqlTipos);

                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TipoCajon tipoCajon = new TipoCajon(
                            rs.getString("id"),
                            rs.getString("codigo"),
                            rs.getString("descripcion"),
                            rs.getInt("capacidad")
                    );

                    cmbTipoCajon.getItems().add(tipoCajon);
                }
            }
        }
    }

    public void cargarDatosIniciales(
            List<Cliente> clientes,
            List<Presentacion> presentaciones,
            List<TipoCajon> tiposCajon) {

        if (clientes != null) {

            cmbCliente.setItems(
                    FXCollections.observableArrayList(clientes)
            );
        }

        if (presentaciones != null) {

            cmbPresentacion.setItems(
                    FXCollections.observableArrayList(
                            presentaciones
                    )
            );
        }

        if (tiposCajon != null) {

            cmbTipoCajon.setItems(
                    FXCollections.observableArrayList(
                            tiposCajon
                    )
            );
        }
    }

    private void cargarPresentaciones() {

        if (servicioPedidos == null) {
            return;
        }

        cmbPresentacion.setItems(
                FXCollections.observableArrayList(
                        servicioPedidos
                                .listarPresentacionesDisponibles()
                )
        );
    }

    // =====================================================
    // CONFIGURAR TABLA
    // =====================================================

    private void configurarColumnas() {

        colPresentacion.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPresentacion() != null
                                && data.getValue()
                                .getPresentacion()
                                .getNombre() != null

                                ? data.getValue()
                                .getPresentacion()
                                .getNombre()

                                : "-"
                )
        );

        colUnidades.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getCantidadUnidades()
                )
        );

        colCajones.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getCantidadCajones()
                )
        );

        colSubtotal.setCellValueFactory(data ->
                new SimpleStringProperty(
                        formatoMoneda.format(
                                data.getValue().calcularSubtotal()
                        )
                )
        );
    }

    // =====================================================
    // AGREGAR DETALLE
    // =====================================================

    @FXML
    private void onAgregarDetalle() {

        if (!verificarDisponibilidad()) {
            return;
        }

        try {

            Cliente cliente = cmbCliente.getValue();

            Presentacion presentacion =
                    cmbPresentacion.getValue();

            TipoCajon tipoCajon =
                    cmbTipoCajon.getValue();

            if (cliente == null) {

                throw new ReglaNegocioException(
                        "Seleccioná un repartidor."
                );
            }

            if (presentacion == null) {

                throw new ReglaNegocioException(
                        "Seleccioná una presentación."
                );
            }

            int unidades = Integer.parseInt(
                    txtCantidadUnidades.getText().trim()
            );

            int cajones =
                    txtCantidadCajones.getText().trim().isEmpty()
                            ? 0
                            : Integer.parseInt(
                            txtCantidadCajones
                                    .getText().trim()
                    );

            if (unidades <= 0) {

                throw new ReglaNegocioException(
                        "La cantidad de unidades debe ser mayor a cero."
                );
            }

            if (cajones < 0) {

                throw new ReglaNegocioException(
                        "La cantidad de cajones no puede ser negativa."
                );
            }



            System.out.println("Unidades ingresadas: " + unidades);
            System.out.println("Cajones ingresados: " + cajones);
            System.out.println("Tipo de cajón: " + tipoCajon);

            if (cajones > 0 && tipoCajon == null) {

                throw new ReglaNegocioException(
                        "Seleccioná el tipo de cajón."
                );
            }







            if (cajones == 0) {

                tipoCajon = null;
            }

            if (pedidoActual == null) {

                // Crear el pedido y su primer detalle
                // dentro de la misma transacción.

                pedidoActual =
                        servicioPedidos.crearPedidoConItem(
                                cliente,
                                presentacion,
                                unidades,
                                cajones,
                                tipoCajon
                        );

                // Bloquear el repartidor solamente
                // después de guardar correctamente.

                cmbCliente.setDisable(true);

            } else {

                // Agregar los productos siguientes
                // al pedido existente.

                servicioPedidos.agregarItem(
                        pedidoActual,
                        presentacion,
                        unidades,
                        cajones,
                        tipoCajon,
                        null
                );
            }

            detalles.setAll(
                    pedidoActual.getDetalles()
            );

            limpiarCamposItem();

            actualizarTotal();

            mostrarExito(
                    "Ítem agregado al pedido correctamente."
            );

        } catch (NumberFormatException e) {

            mostrarError(
                    "Ingresá números enteros válidos "
                            + "en unidades y cajones."
            );

        } catch (ReglaNegocioException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    // =====================================================
    // CONFIRMAR PEDIDO
    // =====================================================

    @FXML
    private void onConfirmarPedido() {

        if (!verificarDisponibilidad()) {
            return;
        }

        if (pedidoActual == null
                || pedidoActual.getDetalles().isEmpty()) {

            mostrarError(
                    "Primero agregá al menos un producto al pedido."
            );

            return;
        }

        try {

            String idPedido = pedidoActual.getId();

            BigDecimal totalPedido =
                    pedidoActual.calcularTotal();

            String nombreCliente =
                    pedidoActual.getCliente().getNombre();

            servicioPedidos.confirmarPedido(pedidoActual);

            limpiarFormulario();

            mostrarExito(
                    "Pedido confirmado correctamente para "
                            + nombreCliente
                            + ". Total: "
                            + formatoMoneda.format(totalPedido)
                            + ". ID: "
                            + idPedido
            );

        } catch (ReglaNegocioException |
                 PersistenciaException |
                 IllegalStateException e) {

            mostrarError(e.getMessage());
        }
    }

    // =====================================================
    // CALCULAR TOTAL
    // =====================================================

    private void actualizarTotal() {

        BigDecimal total =
                pedidoActual == null
                        ? BigDecimal.ZERO
                        : pedidoActual.calcularTotal();

        lblTotal.setText(
                "Total: " + formatoMoneda.format(total)
        );
    }

    // =====================================================
    // LIMPIAR FORMULARIO
    // =====================================================

    private void limpiarCamposItem() {

        txtCantidadUnidades.clear();
        txtCantidadCajones.clear();

        cmbPresentacion.getSelectionModel().clearSelection();
        cmbTipoCajon.getSelectionModel().clearSelection();
    }

    private void limpiarFormulario() {

        pedidoActual = null;

        detalles.clear();

        cmbCliente.setDisable(false);

        limpiarCamposItem();

        actualizarTotal();
    }

    // =====================================================
    // VERIFICAR DISPONIBILIDAD
    // =====================================================

    private boolean verificarDisponibilidad() {

        if (!SesionUsuario.haySesionActiva()) {

            mostrarError("No hay una sesión activa.");

            return false;
        }

        if (servicioPedidos == null) {

            mostrarError(
                    "No hay conexión activa con la base de datos."
            );

            return false;
        }

        return true;
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarExito(String mensaje) {

        lblEstado.setStyle(
                "-fx-text-fill: #2e7d32; "
                        + "-fx-font-weight: bold;"
        );

        lblEstado.setText(mensaje);
    }

    private void mostrarError(String mensaje) {

        lblEstado.setStyle(
                "-fx-text-fill: #c62828; "
                        + "-fx-font-weight: bold;"
        );

        lblEstado.setText(mensaje);
    }
}