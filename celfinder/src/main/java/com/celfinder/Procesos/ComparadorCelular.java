package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComparadorCelular {
    private Celular celular1;
    private Celular celular2;
    private List<String> resultadosComparacion = new ArrayList<>();

    public ComparadorCelular() {
        crearTablaSiNoExiste();
    }

    public void registrarCelulares(Celular c1, Celular c2) {
        this.celular1 = c1;
        this.celular2 = c2;

        compararCelulares();
        guardarCelulares();
    }

    private void crearTablaSiNoExiste() {
        String sql = "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Celular]') AND type in (N'U')) " +
                     "BEGIN " +
                     "CREATE TABLE Celular (" +
                     "Nombre NVARCHAR(100)," +
                     "Ghz FLOAT," +
                     "Camara INT," +
                     "Ram INT," +
                     "Bateria INT," +
                     "Almacenamiento INT," +
                     "Seleccion INT DEFAULT 1," +
                     "Nombre_Cpu NVARCHAR(100)" +
                     ") " +
                     "END";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void compararCelulares() {
        compararCaracteristicas("Frecuencia (GHz)", celular1.getGhz(), celular2.getGhz());
        compararCaracteristicas("Cámaras", celular1.getCamara(), celular2.getCamara());
        compararCaracteristicas("RAM (GB)", celular1.getRam(), celular2.getRam());
        compararCaracteristicas("Batería (mAh)", celular1.getBateria(), celular2.getBateria());
        compararCaracteristicas("Almacenamiento (GB)", celular1.getAlmacenamiento(), celular2.getAlmacenamiento());

        determinarMejorDispositivo();
    }

    private void compararCaracteristicas(String nombre, double valor1, double valor2) {
        if (valor1 < valor2) {
            resultadosComparacion.add("La característica de " + nombre + " del dispositivo 1 es inferior al dispositivo 2.");
        } else if (valor1 > valor2) {
            resultadosComparacion.add("La característica de " + nombre + " del dispositivo 1 es superior al dispositivo 2.");
        } else {
            resultadosComparacion.add("Ambos dispositivos son iguales en " + nombre + ".");
        }
    }

    private void determinarMejorDispositivo() {
        int contador1 = 0;
        int contador2 = 0;

        if (celular1.getGhz() > celular2.getGhz()) contador1++;
        else if (celular1.getGhz() < celular2.getGhz()) contador2++;

        if (celular1.getCamara() > celular2.getCamara()) contador1++;
        else if (celular1.getCamara() < celular2.getCamara()) contador2++;

        if (celular1.getRam() > celular2.getRam()) contador1++;
        else if (celular1.getRam() < celular2.getRam()) contador2++;

        if (celular1.getBateria() > celular2.getBateria()) contador1++;
        else if (celular1.getBateria() < celular2.getBateria()) contador2++;

        if (celular1.getAlmacenamiento() > celular2.getAlmacenamiento()) contador1++;
        else if (celular1.getAlmacenamiento() < celular2.getAlmacenamiento()) contador2++;

        if (contador1 > contador2) {
            resultadosComparacion.add("El dispositivo 1 es mejor en " + contador1 + " características.");
        } else if (contador2 > contador1) {
            resultadosComparacion.add("El dispositivo 2 es mejor en " + contador2 + " características.");
        } else {
            resultadosComparacion.add("Ambos dispositivos son igualmente buenos.");
        }
    }

    private void guardarCelulares() {
        try (Connection con = conexion.getConexion()) {
            String sql = "INSERT INTO Celular (Nombre, Ghz, Camara, Ram, Bateria, Almacenamiento, Seleccion, Nombre_Cpu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                // Guardar celular 1
                pstmt.setString(1, celular1.getNombre());
                pstmt.setDouble(2, celular1.getGhz());
                pstmt.setInt(3, celular1.getCamara());
                pstmt.setInt(4, celular1.getRam());
                pstmt.setInt(5, celular1.getBateria());
                pstmt.setInt(6, celular1.getAlmacenamiento());
                pstmt.setInt(7, 1); // Inicializar Seleccion en 1
                pstmt.setString(8, celular1.getNombreCpu());
                pstmt.executeUpdate();

                // Guardar celular 2
                pstmt.setString(1, celular2.getNombre());
                pstmt.setDouble(2, celular2.getGhz());
                pstmt.setInt(3, celular2.getCamara());
                pstmt.setInt(4, celular2.getRam());
                pstmt.setInt(5, celular2.getBateria());
                pstmt.setInt(6, celular2.getAlmacenamiento());
                pstmt.setInt(7, 1); // Inicializar Seleccion en 1
                pstmt.setString(8, celular2.getNombreCpu());
                pstmt.executeUpdate();
            }
            System.out.println("Celulares guardados en la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
