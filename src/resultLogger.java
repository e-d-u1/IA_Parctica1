import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultLogger {

    /**
     * Guarda los resultados de la simulación en un archivo de texto.
     * Formato simplificado para fácil lectura desde Python.
     */
    public static void guardarResultado(String algoritmo, String configuracion, 
                                        GasolinaEstado estadoInicial, GasolinaEstado estadoFinal, 
                                        long tiempoEjecucion) {
        try {
            // Generar nombre de archivo con timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nombreArchivo = "resultados/resultados_" + algoritmo + "_" + timestamp + ".txt";

            // Asegurar que la carpeta resultados/ existe
            new File("resultados").mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {

                // Encabezado explicativo
                bw.write("# RESULTADOS SIMULACION GASOLINA\n");
                bw.write("# Línea 1: Algoritmo\n");
                bw.write("# Línea 2: Configuración\n");
                bw.write("# Línea 3: Tiempo de ejecución (ms)\n");
                bw.write("# Línea 4: Número total de peticiones iniciales\n");
                bw.write("# Línea 5: Beneficio final\n");
                bw.write("# Línea 6: Distancia total recorrida\n");
                bw.write("# Líneas 7+: Detalle por camión (camión, #peticiones, distancia, viajes)\n");
                bw.write("# Última línea: Total de peticiones asignadas\n");
                bw.write("----------\n");

                // Datos principales
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
            }

            System.out.println("Resultados guardados en " + nombreArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
}
