import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ResultLogger {

    public static void guardarResultado(String algoritmo, String configuracion,
                                        GasolinaEstado estadoInicial, GasolinaEstado estadoFinal,
                                        long tiempoEjecucion, Config cfg) {
        try {
            // Generar un ID simple y único (timestamp + número aleatorio)
            String idUnico = new SimpleDateFormat("HHmmss").format(new Date());

            // Crear nombre del archivo con el formato deseado
            String nombreArchivo = String.format("resultados/exp%d_%s.txt",
                    cfg.numExperimento,idUnico);

            // Asegurar que exista la carpeta resultados/
            new File("resultados").mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {

                // Encabezado informativo
                /*
                    bw.write("# RESULTADOS SIMULACION GASOLINA\n");
                    bw.write("# Línea 1: Algoritmo\n");
                    bw.write("# Línea 2: Parámetros de configuración\n");
                    bw.write("# Línea 3: Tiempo de ejecución (ms)\n");
                    bw.write("# Línea 4: Número total de peticiones iniciales\n");
                    bw.write("# Línea 5: Beneficio final\n");
                    bw.write("# Línea 6: Distancia total recorrida\n");
                    bw.write("# Líneas siguientes: Detalle por camión (camión, #peticiones, distancia, viajes)\n");
                    bw.write("# Última línea: Total de peticiones asignadas\n\n");

                */
                
                // Datos generales
                bw.write(algoritmo + "\n");
                bw.write(configuracion + "\n");
                bw.write(tiempoEjecucion + "\n");
                bw.write(estadoInicial.getNumPeticiones() + "\n");
                bw.write(estadoFinal.getBeneficio() + "\n");
                bw.write(estadoFinal.getDistanciaTotal() + "\n");

                // Detalle por camión
                for (int i = 0; i < estadoFinal.getAsignacionCamionPeticiones().size(); i++) {
                    bw.write(i + "," +
                            estadoFinal.getAsignacionCamionPeticiones().get(i).size() + "," +
                            estadoFinal.getDistanciaCamion(i) + "," +
                            estadoFinal.getViajesCamion(i) + "\n");
                }

                // Total de peticiones asignadas
                bw.write(estadoFinal.getTotalPeticionesAsignadas() + "\n");
                bw.write(cfg.costeKm + "\n");
            }

            System.out.println("Resultados guardados en " + nombreArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
}
