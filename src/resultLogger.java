import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultLogger {

    public static void guardarResultado(String algoritmo, String configuracion, GasolinaEstado estadoFinal, long tiempoEjecucion) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "resultados/resultados_" + algoritmo + "_" + timestamp + ".txt";

            // Asegura que exista la carpeta resultados/
            new File("resultados").mkdirs();

            FileWriter fw = new FileWriter(nombreArchivo);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("==== RESULTADOS ====\n");
            bw.write("Algoritmo: " + algoritmo + "\n");
            bw.write("Configuración: " + configuracion + "\n");
            bw.write("Tiempo de ejecución: " + tiempoEjecucion + " ms\n\n");

            bw.write("==== ESTADO FINAL ====\n");
            bw.write("Beneficio total: " + estadoFinal.getBeneficioTotal() + "\n");
            bw.write("Distancia total: " + estadoFinal.getDistanciaTotal() + "\n");
            bw.write("Peticiones no asignadas: " + estadoFinal.getPeticionesPendientes().size() + "\n");
            bw.write("---- Detalle por camión ----\n");

            for (int i = 0; i < estadoFinal.getAsignacionCamionPeticiones().size(); i++) {
                bw.write("Camión " + i + ": " +
                        estadoFinal.getAsignacionCamionPeticiones().get(i).size() + " peticiones, " +
                        "distancia " + estadoFinal.getDistanciaCamion(i) + ", " +
                        "viajes " + estadoFinal.getViajesCamion(i) + "\n");
            }

            bw.close();
            fw.close();
            System.out.println("Resultados guardados en " + nombreArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
}