package ar.edu.utn.tpfinalsoderia.controllers;

import ar.edu.utn.tpfinalsoderia.DAO.ConexionFactory;
import ar.edu.utn.tpfinalsoderia.DAO.InventarioRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.DAO.RetiroCobroRepositoryJdbc;
import ar.edu.utn.tpfinalsoderia.exceptions.PersistenciaException;
import ar.edu.utn.tpfinalsoderia.models.Cobro;
import ar.edu.utn.tpfinalsoderia.models.StockEnvase;
import ar.edu.utn.tpfinalsoderia.services.ServicioReportes;
import ar.edu.utn.tpfinalsoderia.services.ServicioReportes.FilaSaldoCajonCliente;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ReportesController {

    @FXML private Label lblTotalCobrado;
    @FXML private Label lblEstado;

    @FXML private TableView<StockEnvase> tblRepEnvases;
    @FXML private TableColumn<StockEnvase, String> colRepEnvTitular;
    @FXML private TableColumn<StockEnvase, String> colRepEnvDesc;
    @FXML private TableColumn<StockEnvase, Number> colRepEnvAptos;
    @FXML private TableColumn<StockEnvase, Number> colRepEnvSucios;

    @FXML private TableView<FilaSaldoCajonCliente> tblRepCajones;
    @FXML private TableColumn<FilaSaldoCajonCliente, String> colRepCajCliente;
    @FXML private TableColumn<FilaSaldoCajonCliente, String> colRepCajTipo;
    @FXML private TableColumn<FilaSaldoCajonCliente, Number> colRepCajSaldo;

    @FXML private TableView<Cobro> tblRepCobros;
    @FXML private TableColumn<Cobro, String> colRepCobFecha;
    @FXML private TableColumn<Cobro, String> colRepCobMedio;
    @FXML private TableColumn<Cobro, Number> colRepCobMonto;
    @FXML private TableColumn<Cobro, String> colRepCobEstado;

    private ServicioReportes servicioReportes;

    @FXML
    public void initialize() {
        configurarColumnas();
        inicializarServicio();
    }

    private void inicializarServicio() {
        try {
            Connection conexion = ConexionFactory.abrirConexion();
            InventarioRepositoryJdbc inventarioRepo = new InventarioRepositoryJdbc(conexion);
            RetiroCobroRepositoryJdbc retiroCobroRepo = new RetiroCobroRepositoryJdbc(conexion);
            this.servicioReportes = new ServicioReportes(
                    conexion,
                    inventarioRepo,
                    retiroCobroRepo
            );
            onRefrescarReportes();
        } catch (SQLException | IllegalStateException e) {
            mostrarError("Sin conexión a BD: " + e.getMessage());
        }
    }

    private void configurarColumnas() {
        colRepEnvTitular.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTitular() != null ? data.getValue().getTitular().getNombre() : "Sin titular"
        ));
        colRepEnvDesc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEnvase() != null ? data.getValue().getEnvase().getDescripcion() : "-"
        ));
        colRepEnvAptos.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAptos()));
        colRepEnvSucios.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSucios()));

        colRepCajCliente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClienteNombre()));
        colRepCajTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoCajonDesc()));
        colRepCajSaldo.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSaldoActual()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colRepCobFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFecha() != null ? data.getValue().getFecha().format(fmt) : "-"
        ));
        colRepCobMedio.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMedioPago() != null ? data.getValue().getMedioPago().name() : "-"
        ));
        colRepCobMonto.setCellValueFactory(data -> new SimpleObjectProperty<Number>(data.getValue().getMonto()));
        colRepCobEstado.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isSinCobroAutorizado()
                        ? "Autorizado sin cobro (" + data.getValue().getResponsableAutorizacion() + ")"
                        : "Cobrado"
        ));
    }

    @FXML
    public void onRefrescarReportes() {
        if (servicioReportes == null) return;
        try {
            tblRepEnvases.setItems(FXCollections.observableArrayList(servicioReportes.obtenerReporteStockEnvases()));
            tblRepCajones.setItems(FXCollections.observableArrayList(servicioReportes.obtenerReporteSaldosCajonesPorCliente()));
            tblRepCobros.setItems(FXCollections.observableArrayList(servicioReportes.obtenerReporteCobros()));
            lblTotalCobrado.setText("Total cobrado: $" + servicioReportes.calcularTotalCobrado());
            lblEstado.setText("");
        } catch (PersistenciaException e) {
            mostrarError("Error al generar reportes: " + e.getMessage());
        }
    }

    private void mostrarError(String msg) {
        lblEstado.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        lblEstado.setText(msg);
    }
}