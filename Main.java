package sisim;

import java.io.Console;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner entrada = new Scanner(System.in);
    private static int usuarioId;
    private static String rol;

    public static void main(String[] args) {
        try {
            try (Connection c = ConexionBD.abrir()) { System.out.println("Conexión a MySQL correcta."); }
            prepararPrimerAdministrador();
            if (!iniciarSesion()) return;
            menu();
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }
    private static int leerEntero(String mensaje) {
        while (true) {
            try { return Integer.parseInt(leer(mensaje)); }
            catch (NumberFormatException e) { System.out.println("Escribe un número entero válido."); }
        }
    }
    private static BigDecimal leerDecimal(String mensaje) {
        while (true) {
            try { return new BigDecimal(leer(mensaje).replace(',', '.')); }
            catch (NumberFormatException e) { System.out.println("Escribe un valor numérico válido."); }
        }
    }
    private static String leerClave(String mensaje) {
        Console consola = System.console();
        if (consola != null) {
            char[] clave = consola.readPassword("%s", mensaje);
            return clave == null ? "" : new String(clave);
        }
        return leer(mensaje + " (visible en esta terminal): ");
    }
    private static void prepararPrimerAdministrador() throws Exception {
        try (Connection c = ConexionBD.abrir(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            r.next();
            if (r.getInt(1) > 0) return;
        }
        System.out.println("\nCONFIGURACIÓN INICIAL: crea la cuenta del administrador.");
        String nombre = leer("Nombre: ");
        String usuario = leer("Usuario: ");
        String clave = leerClave("Contraseña (mínimo 8 caracteres)");
        if (nombre.isBlank() || usuario.isBlank() || clave.length() < 8) throw new IllegalArgumentException("Datos incompletos o contraseña demasiado corta.");
        crearUsuario(nombre, usuario, clave, "ADMINISTRADOR");
        System.out.println("Administrador creado. Ahora inicia sesión.");
    }
    private static void crearUsuario(String nombre, String usuario, String clave, String tipo) throws Exception {
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO usuarios(nombre,usuario,clave_hash,rol) VALUES(?,?,?,?)")) {
            ps.setString(1, nombre); ps.setString(2, usuario); ps.setString(3, Seguridad.generarHash(clave)); ps.setString(4, tipo);
            ps.executeUpdate();
        }
    }
    private static boolean iniciarSesion() throws Exception {
        for (int i = 0; i < 3; i++) {
            String usuario = leer("\nUsuario: ");
            String clave = leerClave("Contraseña");
            try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("SELECT id,clave_hash,rol FROM usuarios WHERE usuario=?")) {
                ps.setString(1, usuario);
                try (ResultSet r = ps.executeQuery()) {
                    if (r.next() && Seguridad.verificar(clave, r.getString("clave_hash"))) {
                        usuarioId = r.getInt("id"); rol = r.getString("rol");
                        System.out.println("Bienvenido/a. Rol: " + rol);
                        return true;
                    }
                }
            }
            System.out.println("Credenciales incorrectas.");
        }
        return false;
    }
    private static boolean esAdmin() { return "ADMINISTRADOR".equals(rol); }
    private static void menu() {
        while (true) {
            System.out.println("\n========= SISIM =========");
            System.out.println("1. Consultar inventario");
            System.out.println("2. Registrar venta");
            if (esAdmin()) {
                System.out.println("3. Registrar producto");
                System.out.println("4. Agregar existencias");
                System.out.println("5. Registrar proveedor");
                System.out.println("6. Crear empleado");
                System.out.println("7. Consultar ventas");
                System.out.println("8. Productos con bajo stock");
            }
            System.out.println("0. Salir");
            int opcion = leerEntero("Opción: ");
            if (opcion == 0) { System.out.println("Hasta luego."); return; }
            try {
                switch (opcion) {
                    case 1 -> listarProductos(false);
                    case 2 -> RegistroVentaFacade.registrar(usuarioId, leerEntero("ID del producto: "), leerEntero("Cantidad a vender: "));
                    case 3 -> { if (esAdmin()) registrarProducto(); else accesoDenegado(); }
                    case 4 -> { if (esAdmin()) agregarStock(); else accesoDenegado(); }
                    case 5 -> { if (esAdmin()) registrarProveedor(); else accesoDenegado(); }
                    case 6 -> { if (esAdmin()) registrarEmpleado(); else accesoDenegado(); }
                    case 7 -> { if (esAdmin()) listarVentas(); else accesoDenegado(); }
                    case 8 -> { if (esAdmin()) listarProductos(true); else accesoDenegado(); }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception e) { System.out.println("No se pudo completar la operación: " + e.getMessage()); }
        }
    }
    private static void accesoDenegado() { System.out.println("Acceso exclusivo para el administrador."); }
    private static void listarProductos(boolean bajoStock) throws SQLException {
        String sql = "SELECT id,nombre,marca,precio,cantidad,stock_minimo FROM productos" + (bajoStock ? " WHERE cantidad<=stock_minimo" : "") + " ORDER BY id";
        try (Connection c = ConexionBD.abrir(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            System.out.println("\nID | PRODUCTO | MARCA | PRECIO | CANTIDAD | MÍNIMO");
            while (r.next()) System.out.printf("%d | %s | %s | $%s | %d | %d%n", r.getInt("id"), r.getString("nombre"), r.getString("marca"), r.getBigDecimal("precio"), r.getInt("cantidad"), r.getInt("stock_minimo"));
        }
    }
    private static void registrarProducto() throws SQLException {
        String nombre = leer("Nombre: "), marca = leer("Marca: ");
        BigDecimal precio = leerDecimal("Precio: ");
        int cantidad = leerEntero("Cantidad inicial: "), minimo = leerEntero("Stock mínimo: ");
        int proveedor = leerEntero("ID proveedor (0 si no tiene): ");
        if (nombre.isBlank() || marca.isBlank() || precio.signum() < 0 || cantidad < 0 || minimo < 0) throw new IllegalArgumentException("Datos de producto inválidos.");
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO productos(nombre,marca,precio,cantidad,stock_minimo,proveedor_id) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1,nombre); ps.setString(2,marca); ps.setBigDecimal(3,precio); ps.setInt(4,cantidad); ps.setInt(5,minimo);
            if (proveedor == 0) ps.setNull(6, Types.INTEGER); else ps.setInt(6,proveedor);
            ps.executeUpdate(); System.out.println("Producto registrado.");
        }
    }
    private static void agregarStock() throws SQLException {
        int id = leerEntero("ID del producto: "), cantidad = leerEntero("Cantidad a ingresar: ");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE productos SET cantidad=cantidad+? WHERE id=?")) {
            ps.setInt(1,cantidad); ps.setInt(2,id);
            System.out.println(ps.executeUpdate() == 1 ? "Existencias actualizadas." : "Producto no encontrado.");
        }
    }
    private static void registrarProveedor() throws SQLException {
        String nombre = leer("Nombre del proveedor: "), telefono = leer("Teléfono: ");
        if (nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("INSERT INTO proveedores(nombre,telefono) VALUES(?,?)")) {
            ps.setString(1,nombre); ps.setString(2,telefono); ps.executeUpdate();
            System.out.println("Proveedor registrado.");
        }
    }
    private static void registrarEmpleado() throws Exception {
        String nombre = leer("Nombre empleado: "), usuario = leer("Usuario: "), clave = leerClave("Contraseña (mínimo 8 caracteres)");
        if (nombre.isBlank() || usuario.isBlank() || clave.length() < 8) throw new IllegalArgumentException("Datos inválidos o contraseña corta.");
        crearUsuario(nombre,usuario,clave,"EMPLEADO");
        System.out.println("Empleado creado.");
    }
    private static void listarVentas() throws SQLException {
        try (Connection c = ConexionBD.abrir(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT v.id,v.fecha,u.nombre,v.total FROM ventas v JOIN usuarios u ON v.usuario_id=u.id ORDER BY v.id DESC")) {
            System.out.println("\nID | FECHA | EMPLEADO | TOTAL");
            while (r.next()) System.out.printf("%d | %s | %s | $%s%n", r.getInt(1),r.getTimestamp(2),r.getString(3),r.getBigDecimal(4));
        }
    }
}
