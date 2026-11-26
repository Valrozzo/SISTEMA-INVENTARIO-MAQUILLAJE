package sisim;

import java.math.BigDecimal;
import java.sql.*;

/** Coordina la venta y la salida de inventario en una sola transacción. */
public class RegistroVentaFacade {
    public static void registrar(int usuarioId, int productoId, int cantidad) throws Exception {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        try (Connection c = ConexionBD.abrir()) {
            c.setAutoCommit(false);
            try {
                BigDecimal precio;
                int disponible;
                String nombre;
                try (PreparedStatement ps = c.prepareStatement("SELECT nombre, precio, cantidad FROM productos WHERE id=? FOR UPDATE")) {
                    ps.setInt(1, productoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Producto no encontrado.");
                        nombre = rs.getString("nombre");
                        precio = rs.getBigDecimal("precio");
                        disponible = rs.getInt("cantidad");
                    }
                }
                if (disponible < cantidad) throw new IllegalArgumentException("No hay stock suficiente. Disponible: " + disponible);
                BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
                int ventaId;
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO ventas(usuario_id,total) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, usuarioId);
                    ps.setBigDecimal(2, total);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("No se pudo obtener el número de venta.");
                        ventaId = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO detalle_ventas(venta_id,producto_id,cantidad,precio_unitario,subtotal) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, ventaId); ps.setInt(2, productoId); ps.setInt(3, cantidad);
                    ps.setBigDecimal(4, precio); ps.setBigDecimal(5, total); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE productos SET cantidad=cantidad-? WHERE id=? AND cantidad>=?")) {
                    ps.setInt(1, cantidad); ps.setInt(2, productoId); ps.setInt(3, cantidad);
                    if (ps.executeUpdate() != 1) throw new SQLException("No se pudo actualizar el inventario.");
                }
                c.commit();
                System.out.println("Venta #" + ventaId + " registrada. Producto: " + nombre + ", total: $" + total + ", stock restante: " + (disponible - cantidad));
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
}
