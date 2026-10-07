/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.backend.conexion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author mrk-ema
 */
public class Conexion {

    private static final String ARCHIVO_CONFIG = "db.properties";
    private static final String URL_DEFAULT = "jdbc:mysql://localhost:3306/colegio?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String USUARIO_DEFAULT = "mrk";
    private static final String PASSWORD_DEFAULT = "cambiar-en-resource";

    private static String url = URL_DEFAULT;
    private static String usuario = USUARIO_DEFAULT;
    private static String password = PASSWORD_DEFAULT;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println("Error al cargar el driver MySQL");
            ex.printStackTrace();
        }
        Properties propiedades = new Properties();
        boolean leido = false;

        File externo = Paths.get(ARCHIVO_CONFIG).toFile();
        try (InputStream entrada = new FileInputStream(externo)) {
            propiedades.load(entrada);
            leido = true;
        } catch (IOException e) {
            try (InputStream entrada = Conexion.class.getClassLoader()
                    .getResourceAsStream(ARCHIVO_CONFIG)) {
                if (entrada != null) {
                    propiedades.load(entrada);
                    leido = true;
                }
            } catch (IOException ex) {
                System.out.println("No se pudo leer los datos para ingresar a la bd");
                ex.printStackTrace();
            }
        }

        if (leido) {
            url = propiedades.getProperty("url", URL_DEFAULT);
            usuario = propiedades.getProperty("usuario", USUARIO_DEFAULT);
            password = propiedades.getProperty("password", PASSWORD_DEFAULT);
        }
    }

    private static Connection connection;

    public static synchronized Connection obtener() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, usuario, password);
        }
        return connection;
    }

}
