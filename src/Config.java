
import java.util.Random;
public class Config {
    // Experimento - valor por defecto(no hace falta tocarlo)
    public int numExperimento = 1;

    // Configuración general
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

    public boolean usarSimulatedAnnealing = false; // true = SA, false = HC

    // Parámetros SA (Establecidos a partir del experimento 3/4)
    public int steps = 1000;   // iteraciones totales
    public int stiter = 100;    // iteraciones por temperatura
    public int k = 25;          // temperatura 
    public double lambda = 0.01; // velocidad enfriamiento

     
    public boolean printInfo = true; // Imprimr por consola 
    public boolean guardarResultados = true; // Loggearlo en carpeta /resultados
}