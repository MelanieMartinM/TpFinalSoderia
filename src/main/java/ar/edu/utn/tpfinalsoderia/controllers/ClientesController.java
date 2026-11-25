package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cliente;
import ar.edu.utn.tpfinalsoderia.services.ServicioClientes;
import ar.edu.utn.tpfinalsoderia.utils.SesionUsuario;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ClientesController {

    @FXML
    private TextField campoNombre;

    @FXML
    private TextField campoTelefono;

    @FXML
    private TextField campoDireccion;

    @FXML
    private Label mensaje;

    @FXML
    private Button botonGuardar;

    @FXML
    private Button botonActualizar;

    @FXML
    private Button botonDesactivar;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> columnaNombre;

    @FXML
    private TableColumn<Cliente, String> columnaTelefono;

    @FXML
    private TableColumn<Cliente, String> columnaDireccion;

    private final ServicioClientes servicio =
            new ServicioClientes();

    @FXML
    public void initialize() {

        columnaNombre.setCellValueFactory(
                fila -> new SimpleStringProperty(
                        fila.getValue().getNombre()
                )
        );

        columnaTelefono.setCellValueFactory(
                fila -> new SimpleStringProperty(
                        fila.getValue().getTelefono()
                )
        );

        columnaDireccion.setCellValueFactory(
                fila -> new SimpleStringProperty(
                        fila.getValue().getDireccion()
                )
        );

        tablaClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        mostrarEnCampos(seleccionado);
                    }
                });

        aplicarPermisos();

        cargarClientes();
    }

    private void aplicarPermisos() {

        boolean administrador =
                SesionUsuario.esAdministrador();

        campoNombre.setEditable(administrador);
        campoTelefono.setEditable(administrador);
        campoDireccion.setEditable(administrador);

        if (botonGuardar != null) {
            botonGuardar.setDisable(!administrador);
        }

        if (botonActualizar != null) {
            botonActualizar.setDisable(!administrador);
        }

        if (botonDesactivar != null) {
            botonDesactivar.setDisable(!administrador);
        }

        if (!administrador) {
            mensaje.setText(
                    "Modo consulta: no tenés permisos para modificar clientes."
            );
        }
    }

    private boolean verificarPermisos() {

        if (!SesionUsuario.esAdministrador()) {
            mensaje.setText(
                    "No tenés permisos para realizar esta operación."
            );
            return false;
        }

        return true;
    }

    private void mostrarEnCampos(Cliente cliente) {

        campoNombre.setText(cliente.getNombre());
        campoTelefono.setText(cliente.getTelefono());
        campoDireccion.setText(cliente.getDireccion());
    }

    private void cargarClientes() {

        try {
            tablaClientes.setItems(
                    FXCollections.observableArrayList(
                            servicio.listar()
                    )
            );

        } catch (PersistenciaException | IllegalStateException e) {
            mensaje.setText(
                    "No se pudieron cargar los clientes."
            );
        }
    }

    @FXML
    private void guardarCliente() {

        if (!verificarPermisos()) {
            return;
        }

        try {
            Cliente cliente = crearClienteDesdeCampos();

            servicio.guardar(cliente);
            cargarClientes();
            limpiarCampos();

            mensaje.setText(
                    "Cliente guardado correctamente."
            );

        } catch (IllegalArgumentException e) {
            mensaje.setText(e.getMessage());

        } catch (PersistenciaException | IllegalStateException e) {
            mensaje.setText(
                    "No se pudo guardar el cliente."
            );
        }
    }

    @FXML
    private void actualizarCliente() {

        if (!verificarPermisos()) {
            return;
        }

        Cliente seleccionado =
                tablaClientes.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            mensaje.setText(
                    "Seleccioná un cliente de la tabla."
            );
            return;
        }

        try {
            Cliente cliente = crearClienteDesdeCampos();

            cliente.setId(seleccionado.getId());
            cliente.setActivo(seleccionado.isActivo());

            servicio.actualizar(cliente);
            cargarClientes();
            limpiarCampos();

            mensaje.setText(
                    "Cliente actualizado correctamente."
            );

        } catch (IllegalArgumentException e) {
            mensaje.setText(e.getMessage());

        } catch (PersistenciaException | IllegalStateException e) {
            mensaje.setText(
                    "No se pudo actualizar el cliente."
            );
        }
    }

    @FXML
    private void desactivarCliente() {

        if (!verificarPermisos()) {
            return;
        }

        Cliente seleccionado =
                tablaClientes.getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            mensaje.setText(
                    "Seleccioná un cliente de la tabla."
            );
            return;
        }

        try {
            servicio.desactivar(seleccionado.getId());
            cargarClientes();
            limpiarCampos();

            mensaje.setText(
                    "Cliente desactivado correctamente."
            );

        } catch (PersistenciaException | IllegalStateException e) {
            mensaje.setText(
                    "No se pudo desactivar el cliente."
            );
        }
    }

    @FXML
    private void limpiarFormulario() {

        limpiarCampos();

        if (SesionUsuario.esAdministrador()) {
            mensaje.setText("");
        } else {
            mensaje.setText("Modo consulta.");
        }
    }

    private void limpiarCampos() {

        tablaClientes.getSelectionModel().clearSelection();

        campoNombre.clear();
        campoTelefono.clear();
        campoDireccion.clear();
    }

    private Cliente crearClienteDesdeCampos() {

        Cliente cliente = new Cliente();

        cliente.setNombre(campoNombre.getText().trim());
        cliente.setTelefono(campoTelefono.getText().trim());
        cliente.setDireccion(campoDireccion.getText().trim());

        return cliente;
    }
}