import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;


public class ResultLogger {

    public static void guardarResultado(String algoritmo, String configuracion,
                                        GasolinaEstado estadoInicial, GasolinaEstado estadoFinal,
                                        long tiempoEjecucion, Config cfg) {
        try {
            // Para generar un ID por segundo(en los experimentos se usa time.sleep(X) para no sobreescribir)
            String idUnico = new SimpleDateFormat("HHmmss").format(new Date());

            String nombreArchivo = String.format("resultados/exp%d_%s.txt",
                    cfg.numExperimento,idUnico);

            // Dónde almacenamos los datos
            new File("resultados").mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {

                /*
                    Formato:
                    Línea 1: Algoritmo
                    Línea 2: Parámetros de configuración
                    Línea 3: Tiempo de ejecución (ms)
                    Línea 4: Número total de peticiones iniciales
                    Línea 5: Beneficio final
                    Línea 6: Distancia total recorrida
                    Líneas siguientes: Detalle por camión (camión, #peticiones, distancia, viajes)
                    Línea -3: Total de peticiones asignadas
                    Línea -2: Coste por KM
                    Línea -1: Diás que llevan pendiente las peticiones (0,1,2,3)
                */
                
                bw.write(algoritmo + "\n");
                bw.write(configuracion + "\n");
                bw.write(tiempoEjecucion + "\n");
                bw.write(estadoInicial.getNumPeticiones() + "\n");
                bw.write(estadoFinal.getBeneficio() + "\n");
                bw.write(estadoFinal.getDistanciaTotal() + "\n");

                // Info camión
                for (int i = 0; i < estadoFinal.getAsignacionCamionPeticiones().size(); i++) {
                    bw.write(i + "," +
                            estadoFinal.getAsignacionCamionPeticiones().get(i).size() + "," +
                            estadoFinal.getDistanciaCamion(i) + "," +
                            estadoFinal.getViajesCamion(i) + "\n");
                }

                bw.write(estadoFinal.getTotalPeticionesAsignadas() + "\n");
                bw.write(cfg.costeKm + "\n");
                ArrayList<Integer> dias = estadoFinal.getDiasPeticiones();
                bw.write(dias.get(0) + "," + dias.get(1) + "," + dias.get(2) + "," + dias.get(3) + "\n");
            }

            System.out.println("Resultados guardados: " + nombreArchivo);

        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
}
