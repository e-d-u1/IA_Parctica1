
import java.util.Random;
public class Config {
    // Experimento
    public int numExperimento = 2;

    // Escenario general
    public int numGasolineras = 100;
    public int numCentros = 10;
    public int numCamionesPorCentro = 1;
    public int seed = (int)(Math.random() * 9999) + 1;
    public int seedGasolineras = this.seed;
    public int seedCentros = this.seed;

    // Restricciones
    public int maxViajes = 5;
    public int maxDistancia = 640; 
    public int maxPeticionesGas =  3;
    public double costeKm = 2.0;
    public int valorDeposito = 1000;

    // Solucion a usar: 1 = Simple, 2 = Ordenada
    public int numSolucion = 2;

    // Algoritmo a usar
    public boolean usarSimulatedAnnealing = false; // true = SA, false = HC

    // Parámetros SA
    public int steps = 1000;   // iteraciones totales
    public int stiter = 100;    // iteraciones por cambio de temperatura
    public int k = 20;          // parámetro k de aceptación 
    public double lambda = 0.01; // tasa de enfriamiento

    // Otros parámetros opcionales
    public boolean verbose = false; // si se quieren imprimir resultados detallados
    public boolean guardarResultados = true; // activar logging
}