package sisim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    public static Connection abrir() throws SQLException {
        String url = System.getenv().getOrDefault("SISIM_DB_URL", "jdbc:mysql://localhost:3306/sisim?useSSL=false&serverTimezone=UTC");
        String usuario = System.getenv().getOrDefault("SISIM_DB_USER", "root");
        String clave = System.getenv("SISIM_DB_PASSWORD");
        if (clave == null) clave = "";
        return DriverManager.getConnection(url, usuario, clave);
    }
}
