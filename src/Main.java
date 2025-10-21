import IA.Gasolina.*;
import aima.search.framework.*;
import aima.search.informed.*;

import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        // Configuración inicial
        Config cfg = new Config();
        inicializaExperimento(cfg, args);

        Gasolineras gasolineras = new Gasolineras(cfg.numGasolineras, cfg.seedGasolineras);
        CentrosDistribucion centros = new CentrosDistribucion(cfg.numCentros, cfg.numCamionesPorCentro, cfg.seedCentros);
        
        // Estado inicial (Creadora solo usada para el main)
        GasolinaEstado estadoInicial = new GasolinaEstado(gasolineras, centros, cfg);

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
        if (cfg.printInfo) printInstrumentation(agent.getInstrumentation());

        GasolinaEstado estadoFinal = (GasolinaEstado) algSA.getGoalState();
        mostrarResultadosFinales(estadoFinal);

        // Pasamos todo al Logger para hacer tablas y gráficos
        if (cfg.guardarResultados) {
            String configuracion = "steps=" + cfg.steps +
                    ", stiter=" + cfg.stiter +
                    ", k=" + cfg.k +
                    ", lambda=" + cfg.lambda;
            ResultLogger.guardarResultado("SA", configuracion, estadoInicial, estadoFinal, endTime - startTime, cfg);
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
        if (cfg.printInfo) { 
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        }

        GasolinaEstado estadoFinal = (GasolinaEstado) algHC.getGoalState();
        mostrarResultadosFinales(estadoFinal);

        if (cfg.guardarResultados) ResultLogger.guardarResultado("HC", "-", estadoInicial, estadoFinal, endTime - startTime, cfg);
    }

    private static void mostrarResultadosFinales(GasolinaEstado estadoFinal) {
        System.out.println("\nBeneficio final: " + estadoFinal.getBeneficio());
        System.out.println("Distancia total recorrida: " + estadoFinal.getDistanciaTotal());
        estadoFinal.imprimirResumenCamiones();
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

    private static void inicializaExperimento(Config cfg, String[] args) {
        // Si la llamada tiene parámetros es para hacer algún expermiento
        if (args.length < 1) return;

        int exp = Integer.parseInt(args[0]);
        cfg.numExperimento = exp;
        cfg.usarSimulatedAnnealing = args[1].equalsIgnoreCase("SA");

        // En todos los exp los args empiezan en el 2
        switch (exp) {
            case 2:
                if (cfg.usarSimulatedAnnealing) return;
                cfg.numCentros = 10;
                cfg.numGasolineras = 100;
                cfg.numCamionesPorCentro = 1;
                cfg.numSolucion = Integer.parseInt(args[2]);
                break;
            case 3:
                // Si usamos SA, cargamos parámetros adicionales
                if (cfg.usarSimulatedAnnealing) {
                    cfg.steps = Integer.parseInt(args[2]);   // steps
                    cfg.stiter = Integer.parseInt(args[3]);  // stiter
                    cfg.k = Integer.parseInt(args[4]);       // K
                    cfg.lambda = Double.parseDouble(args[5]); // lambda
                }
                break;
            case 4:
                cfg.numCentros = Integer.parseInt(args[2]);
                cfg.numGasolineras = cfg.numCentros * 10; // relación 1:10

                if (cfg.usarSimulatedAnnealing) {
                    cfg.steps = Integer.parseInt(args[3]);
                    cfg.stiter = Integer.parseInt(args[4]);
                    cfg.k = Integer.parseInt(args[5]);
                    cfg.lambda = Double.parseDouble(args[6]);
                }
                break;
            case 5:
                // Escenario del primer apartado + HC
                if (cfg.usarSimulatedAnnealing) return;
                cfg.numCentros = Integer.parseInt(args[2]);
                cfg.numGasolineras = 100;
                cfg.numCamionesPorCentro = Integer.parseInt(args[3]);;
                break;
            case 6:
                // Escenario del primer apartado + HC
                if (cfg.usarSimulatedAnnealing) return;
                cfg.numCentros = 10;
                cfg.numGasolineras = 100;
                cfg.numCamionesPorCentro = 1;
                cfg.costeKm = Integer.parseInt(args[2]);
                break;
            case 7: 
                // Escenario del primer apartado + HC
                if (cfg.usarSimulatedAnnealing) return;
                cfg.numCentros = 10;
                cfg.numGasolineras = 100;
                cfg.numCamionesPorCentro = 1;
                cfg.maxDistancia = Integer.parseInt(args[2]);
                break;

            default:
                return;
        }
    }

}
