public class Config {

    // Escenario general
    
    public int numGasolineras = 100;
    public int numCentros = 10;
    public int numCamionesPorCentro = 2;
    public int seedGasolineras = 1234;
    public int seedCentros = 5678;

    // Solucion a usar: 1 = Simple, 2 = Ordenada
    public int numSolucion = 2;

    // Algoritmo a usar
    public boolean usarSimulatedAnnealing = false; // true = SA, false = HC

    // Parámetros SA
    public int steps = 10000;   // iteraciones totales
    public int stiter = 100;    // iteraciones por cambio de temperatura
    public int k = 20;          // parámetro k de aceptación 
    public double lambda = 0.01; // tasa de enfriamiento

    // Otros parámetros opcionales
    public boolean verbose = true; // si se quieren imprimir resultados detallados
    public boolean guardarResultados = true; // activar logging
}