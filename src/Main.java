
import IA.Gasolina.*;
import aima.search.framework.*;
import aima.search.informed.*;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {

        // Parameters
        int numGasolineras = 50;
        int numCentros = 5;
        int numCamionesPorCentro = 2;
        int seedGasolineras = 1234;
        int seedCentros = 5678;

        // se crea el escenario
        Gasolineras gasolineras = new Gasolineras(numGasolineras, seedGasolineras);
        CentrosDistribucion centros = new CentrosDistribucion(numCentros, numCamionesPorCentro, seedCentros);

        // Estado inicial
        GasolinaEstado estadoInicial = new GasolinaEstado(gasolineras, centros);

        // ELEGIR ALGORITMO DE BUSQUEDA
        boolean usarSimulatedAnnealing = true; // Cambia a false para usar Hill Climbing

        if (usarSimulatedAnnealing) {
            //SIMULATED ANNEALING
            //parameters
            int steps = 10000;   // iteraciones totales
            int stiter = 100;    // iteraciones por cambio de temperatura
            int k = 20;          // parametro k de aceptacion 
            double lambda = 0.01; //tasa de enfriamiento

        
            Problem problemaSA = new Problem(
                    estadoInicial,
                    new GasolinaSuccesorFunctionSA(), 
                    new GasolinaGoalTest(),
                    new GasolinaHeuristicFunction()
            );
            // Instantiate the search algorithm
            Search alg = new SimulatedAnnealingSearch(steps, stiter, k, lambda);
            // Instantiate the SearchAgent object (ejecutar la busqueda)
            SearchAgent agent = new SearchAgent(problemaSA, alg);

            // We print the results of the search
            System.out.println("----------SIMULATED ANNEALING ----------");
            printInstrumentation(agent.getInstrumentation());

            GasolinaEstado estadoFinal = (GasolinaEstado) ((SimulatedAnnealingSearch) alg).getGoalState();
            System.out.println("\n beneficio final:  " + estadoFinal.getBeneficio());
            System.out.println(" distancia total recorrida:  " + estadoFinal.getDistanciaTotal());
            estadoFinal.imprimirResumenCamiones();

            // COMPROBACION EXTRA - Edu
            estadoFinal.calcularBeneficioYDistancia();
            System.out.println("Total peticiones asignadas: " + estadoFinal.getTotalPeticionesAsignadas());

        } else {
            // --- HILL CLIMBING ---
            Problem problemaHC = new Problem(
                    estadoInicial,
                    new GasolinaSuccesorFunction(), 
                    new GasolinaGoalTest(),
                    new GasolinaHeuristicFunction()
            );
            // Instantiate the search algorithm
            Search alg = new HillClimbingSearch();
            // Instantiate the SearchAgent object (ejecutar la busqueda)
            SearchAgent agent = new SearchAgent(problemaHC, alg);

            System.out.println("-----------HILL CLIMBING -----------");
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());

            GasolinaEstado estadoFinal = (GasolinaEstado) ((HillClimbingSearch) alg).getGoalState();
            System.out.println("\nBeneficio final: " + estadoFinal.getBeneficio());
            System.out.println("Distancia total recorrida: " + estadoFinal.getDistanciaTotal());
            estadoFinal.imprimirResumenCamiones();


            // COMPROBACION EXTRA - Edu
            estadoFinal.calcularBeneficioYDistancia();
            System.out.println("Total peticiones asignadas: " + estadoFinal.getTotalPeticionesAsignadas());
        }
                    

        
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }

    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }
}