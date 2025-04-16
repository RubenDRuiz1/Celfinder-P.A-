package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.Celularmedia;
import com.celfinder.Model.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ComparadorMedia {
    private Celular celular;
    private List<String> resultadosComparacion = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);

    public ComparadorMedia() {
        crearTablaSiNoExiste();
    }

    public void registrarCelular() {
        celular = new Celular();

        System.out.print("Ingrese el nombre del celular: ");
        celular.setNombre(scanner.nextLine());

        System.out.print("Ingrese el nombre del CPU: ");
        celular.setNombreCpu(scanner.nextLine());

        System.out.print("Ingrese la frecuencia en GHz: ");
        celular.setGhz(scanner.nextDouble());

        System.out.print("Ingrese la cantidad de cámaras: ");
        celular.setCamara(scanner.nextInt());

        System.out.print("Ingrese la cantidad de RAM: ");
        celular.setRam(scanner.nextInt());

        System.out.print("Ingrese la capacidad de batería: ");
        celular.setBateria(scanner.nextInt());

        System.out.print("Ingrese la capacidad de almacenamiento: ");
        celular.setAlmacenamiento(scanner.nextInt());

        // Comparar con Celularmedia
        Celularmedia celularMedia = new Celularmedia(); // Inicializar con valores predeterminados
        compararConMedia(celular, celularMedia);

        // Guardar o actualizar celular en la base de datos
        guardarCelular(celular);
    }

    private void crearTablaSiNoExiste() {
        String sql = "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Celular]') AND type in (N'U')) " +
                     "BEGIN " +
                     "CREATE TABLE Celular (" +
                     "Nombre NVARCHAR(100)," +
                     "Nombre_Cpu NVARCHAR(100)," +
                     "Ghz FLOAT," +
                     "Camara INT," +
                     "Ram INT," +
                     "Bateria INT," +
                     "Almacenamiento INT," +
                     "Seleccion INT DEFAULT 1" +
                     ") " +
                     "END";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void compararConMedia(Celular celular, Celularmedia celularMedia) {
        compararCaracteristicas("Frecuencia (GHz)", celular.getGhz(), celularMedia.getGhz());
        compararCaracteristicas("Cámaras", celular.getCamara(), celularMedia.getCamara());
        compararCaracteristicas("RAM (GB)", celular.getRam(), celularMedia.getRam());
        compararCaracteristicas("Batería (mAh)", celular.getBateria(), celularMedia.getBateria());
        compararCaracteristicas("Almacenamiento (GB)", celular.getAlmacenamiento(), celularMedia.getAlmacenamiento());
    }

    private void compararCaracteristicas(String nombre, double valor1, double valor2) {
        if (valor1 < valor2) {
            resultadosComparacion.add("La característica de " + nombre + " es inferior a la media.");
        } else if (valor1 > valor2) {
            resultadosComparacion.add("La característica de " + nombre + " es superior a la media.");
        } else {
            resultadosComparacion.add("La característica de " + nombre + " es igual a la media.");
        }
    }

    public void guardarCelular(Celular celular) {
        // Verificar si el celular ya existe en la base de datos
        Celular celularExistente = obtenerCelularExistente(celular);
        if (celularExistente != null) {
            // Si existe, incrementar la selección y actualizar
            int nuevaSeleccion = celularExistente.getSeleccion() + 1;
            actualizarSeleccion(celularExistente, nuevaSeleccion);
        } else {
            // Si no existe, guardar como nuevo
            try (Connection con = conexion.getConexion()) {
                String sql = "INSERT INTO Celular (Nombre, Ghz, Camara, Ram, Bateria, Almacenamiento, Seleccion, Nombre_Cpu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, celular.getNombre());
                    pstmt.setDouble(2, celular.getGhz());
                    pstmt.setInt(3, celular.getCamara());
                    pstmt.setInt(4, celular.getRam());
                    pstmt.setInt(5, celular.getBateria());
                    pstmt.setInt(6, celular.getAlmacenamiento());
                    pstmt.setInt(7, 1); // Inicializar Seleccion en 1
                    pstmt.setString(8, celular.getNombreCpu());
                    pstmt.executeUpdate();
                }
                System.out.println("Celular guardado en la base de datos.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Celular obtenerCelularExistente(Celular celular) {
        Celular celularExistente = null;
        String sql = "SELECT * FROM Celular WHERE Nombre = ? AND Nombre_Cpu = ? AND Ghz = ? AND Camara = ? AND Ram = ? AND Bateria = ? AND Almacenamiento = ?";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, celular.getNombre());
            pstmt.setString(2, celular.getNombreCpu());
            pstmt.setDouble(3, celular.getGhz());
            pstmt.setInt(4, celular.getCamara());
            pstmt.setInt(5, celular.getRam());
            pstmt.setInt(6, celular.getBateria());
            pstmt.setInt(7, celular.getAlmacenamiento());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    celularExistente = new Celular();
                    celularExistente.setNombre(rs.getString("Nombre"));
                    celularExistente.setGhz(rs.getDouble("Ghz"));
                    celularExistente.setCamara(rs.getInt("Camara"));
                    celularExistente.setRam(rs.getInt("Ram"));
                    celularExistente.setBateria(rs.getInt("Bateria"));
                    celularExistente.setAlmacenamiento(rs.getInt("Almacenamiento"));
                    celularExistente.setSeleccion(rs.getInt("Seleccion"));
                    celularExistente.setNombreCpu(rs.getString("Nombre_Cpu"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return celularExistente;
    }

    public List<Celular> obtenerCelulares() {
        List<Celular> celulares = new ArrayList<>();
        String sql = "SELECT * FROM Celular";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Celular celular = new Celular();
                celular.setNombre(rs.getString("Nombre"));
                celular.setGhz(rs.getDouble("Ghz"));
                celular.setCamara(rs.getInt("Camara"));
                celular.setRam(rs.getInt("Ram"));
                celular.setBateria(rs.getInt("Bateria"));
                celular.setAlmacenamiento(rs.getInt("Almacenamiento"));
                celular.setSeleccion(rs.getInt("Seleccion"));
                celular.setNombreCpu(rs.getString("Nombre_Cpu"));
                celulares.add(celular);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return celulares;
    }

    public List<String> getResultadosComparacion() {
        return resultadosComparacion;
    }

    public void actualizarSeleccion(Celular celular, int nuevaSeleccion) {
        String sql = "UPDATE Celular SET Seleccion = ? WHERE Nombre = ? AND Nombre_Cpu = ? AND Ghz = ? AND Camara = ? AND Ram = ? AND Bateria = ? AND Almacenamiento = ?";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, nuevaSeleccion);
            pstmt.setString(2, celular.getNombre());
            pstmt.setString(3, celular.getNombreCpu());
            pstmt.setDouble(4, celular.getGhz());
            pstmt.setInt(5, celular.getCamara());
            pstmt.setInt(6, celular.getRam());
            pstmt.setInt(7, celular.getBateria());
            pstmt.setInt(8, celular.getAlmacenamiento());
            
            int filasActualizadas = pstmt.executeUpdate();
            
            if (filasActualizadas > 0) {
                System.out.println("Seleccion actualizada para el celular " + celular.getNombre());
            } else {
                System.out.println("No se encontró un celular con las características especificadas.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
