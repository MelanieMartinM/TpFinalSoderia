package ar.edu.utn.tpfinalsoderia.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/** Gestiona una transacción propia, usando la conexión compartida por los DAO. */
public final class TransaccionJdbc implements AutoCloseable {
    private final Connection conexion;
    private boolean confirmada;

    public TransaccionJdbc(Connection conexion) throws SQLException {
        this.conexion = Objects.requireNonNull(conexion, "Falta la conexión.");
        if (!conexion.getAutoCommit()) {
            throw new SQLException("La conexión ya tiene una transacción activa.");
        }
        conexion.setAutoCommit(false);
    }

    public void confirmar() throws SQLException {
        conexion.commit();
        confirmada = true;
    }

    @Override
    public void close() throws SQLException {
        if (!confirmada) {
            try {
                conexion.rollback();
            } catch (SQLException error) {
                // No restaurar autoCommit después de fallar el rollback: eso podría confirmar datos.
                try { conexion.close(); } catch (SQLException cierre) { error.addSuppressed(cierre); }
                throw error;
            }
        }
        conexion.setAutoCommit(true);
    }
}
