package es.dwes.socios.dao;

import es.dwes.socios.model.Socio;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SocioDAO {
    private static final String URL = setting("DB_URL", "jdbc:mysql://localhost:3306/baloncesto");
    private static final String USER = setting("DB_USER", "root");
    private static final String PASSWORD = setting("DB_PASSWORD", "root");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Socio> listar() throws SQLException {
        String sql = "SELECT socioID, nombre, estatura, edad, localidad FROM socio ORDER BY socioID";
        List<Socio> socios = new ArrayList<>();
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                socios.add(new Socio(
                        resultado.getInt("socioID"),
                        resultado.getString("nombre"),
                        (Integer) resultado.getObject("estatura"),
                        (Integer) resultado.getObject("edad"),
                        resultado.getString("localidad")));
            }
        }
        return socios;
    }

    public void insertar(Socio socio) throws SQLException {
        String sql = "INSERT INTO socio (socioID, nombre, estatura, edad, localidad) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, socio.numero());
            sentencia.setString(2, socio.nombre());
            sentencia.setObject(3, socio.estatura());
            sentencia.setObject(4, socio.edad());
            sentencia.setString(5, socio.localidad());
            sentencia.executeUpdate();
        }
    }

    public void borrar(int numero) throws SQLException {
        try (Connection conexion = conectar();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM socio WHERE socioID = ?")) {
            sentencia.setInt(1, numero);
            sentencia.executeUpdate();
        }
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}