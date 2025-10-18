import IA.Gasolina.*;
import aima.search.framework.*;
import aima.search.informed.*;

import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        // Cargar configuración
        Config cfg = new Config();

        // Escenario
        Gasolineras gasolineras = new Gasolineras(cfg.numGasolineras, cfg.seedGasolineras);
        CentrosDistribucion centros = new CentrosDistribucion(cfg.numCentros, cfg.numCamionesPorCentro, cfg.seedCentros);
        
        // Estado inicial
        GasolinaEstado estadoInicial = new GasolinaEstado(gasolineras, centros, cfg.numSolucion);

        if (cfg.usarSimulatedAnnealing) ejecutarBusquedaSimulatedAnnealing(estadoInicial, cfg);
        else ejecutarBusquedaHillClimbing(estadoInicial, cfg);
        
    }

    private static void ejecutarBusquedaSimulatedAnnealing(GasolinaEstado estadoInicial, Config cfg) throws Exception {
        Problem problemaSA = new Problem(
                estadoInicial,
                new GasolinaSuccesorFunctionSA(),
                new GasolinaGoalTest(),
                new GasolinaHeuristicFunction()
        );

        SimulatedAnnealingSearch algSA = new SimulatedAnnealingSearch(cfg.steps, cfg.stiter, cfg.k, cfg.lambda);

        long startTime = System.currentTimeMillis();
        SearchAgent agent = new SearchAgent(problemaSA, algSA);
        long endTime = System.currentTimeMillis();

        System.out.println("---------- SIMULATED ANNEALING ----------");
        if (cfg.verbose) {
            printInstrumentation(agent.getInstrumentation());
        }

        GasolinaEstado estadoFinal = (GasolinaEstado) algSA.getGoalState();

        mostrarResultadosFinales(estadoFinal);

        if (cfg.guardarResultados) {
            String configuracion = "steps=" + cfg.steps +
                    ", stiter=" + cfg.stiter +
                    ", k=" + cfg.k +
                    ", lambda=" + cfg.lambda;
            ResultLogger.guardarResultado("SA", configuracion, estadoInicial, estadoFinal, endTime - startTime);
        }
    }

    private static void ejecutarBusquedaHillClimbing(GasolinaEstado estadoInicial, Config cfg) throws Exception {
        Problem problemaHC = new Problem(
                estadoInicial,
                new GasolinaSuccesorFunction(),
                new GasolinaGoalTest(),
                new GasolinaHeuristicFunction()
        );

        HillClimbingSearch algHC = new HillClimbingSearch();

        long startTime = System.currentTimeMillis();
        SearchAgent agent = new SearchAgent(problemaHC, algHC);
        long endTime = System.currentTimeMillis();

        System.out.println("----------- HILL CLIMBING -----------");
        if (cfg.verbose) {
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        }

        GasolinaEstado estadoFinal = (GasolinaEstado) algHC.getGoalState();

        mostrarResultadosFinales(estadoFinal);

        if (cfg.guardarResultados) {
            ResultLogger.guardarResultado("HC", "-", estadoInicial, estadoFinal, endTime - startTime);
        }
    }

    private static void mostrarResultadosFinales(GasolinaEstado estadoFinal) {
        System.out.println("\nBeneficio final: " + estadoFinal.getBeneficio());
        System.out.println("Distancia total recorrida: " + estadoFinal.getDistanciaTotal());
        estadoFinal.imprimirResumenCamiones();

        // --- COMPROBACION EXTRA: Edu ---
        estadoFinal.calcularBeneficioYDistancia();
        System.out.println("Total peticiones asignadas: " + estadoFinal.getTotalPeticionesAsignadas());
    }

    private static void printInstrumentation(Properties properties) {
        for (Object key : properties.keySet()) {
            System.out.println(key + " : " + properties.getProperty((String) key));
        }
    }

    private static void printActions(List<?> actions) {
        for (Object action : actions) {
            System.out.println((String) action);
        }
    }
}
