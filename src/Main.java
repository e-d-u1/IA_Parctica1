
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

        // Escenario aleatorio
        Gasolineras gasolineras = new Gasolineras(numGasolineras, seedGasolineras);
        CentrosDistribucion centros = new CentrosDistribucion(numCentros, numCamionesPorCentro, seedCentros);

        // Estado inicial
        GasolinaEstado estadoInicial = new GasolinaEstado(gasolineras, centros);

        // Creamos problema AIMA
        Problem problema = new Problem(
                estadoInicial,
                new GasolinaSuccesorFunction(), // función sucesora (operadores)
                new GasolinaGoalTest(), // condición de parada (si aplica)
                new GasolinaHeuristicFunction() // función heurística (evaluación)
        );

        /* 
                    // Instantiate the search algorithm
            Search alg = new HillClimbingSearch(); // o new SimulatedAnnealingSearch( parametros)

            // Instantiate the SearchAgent object (ejecutar la busqueda)
            SearchAgent agent = new SearchAgent(problema, alg);

            // We print the results of the search
            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());

            // You can access also to the goal state using the
            // method getGoalState of class Search
            
            GasolinaEstado estadoFinal = (GasolinaEstado) ((HillClimbingSearch) alg).getGoalState();
            System.out.println("\nBeneficio final: " + estadoFinal.getBeneficio());
            System.out.println("Distancia total recorrida: " + estadoFinal.getDistanciaTotal());
            estadoFinal.imprimirResumenCamiones();

        
        */

       
     

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