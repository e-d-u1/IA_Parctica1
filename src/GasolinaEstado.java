import IA.Gasolina.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class GasolinaEstado {

    // Datos fijos del problema
    private Gasolineras gasolineras;
    private CentrosDistribucion centros;
    private Integer camionesPorCentro;
    // Asignaciones dinámicas (lo que cambia de estado a estado)
    private ArrayList<ArrayList<Integer>> asignacionCamionPeticiones;
    
    // asignacionCamionPeticiones[i] = lista de IDs de peticiones que atiende el
    // camión i

    private double beneficioTotal;
    private double distanciaTotal;

    // Parámetros del problema (podrías moverlos a una clase Config)
    private static final int MAX_VIAJES = 5;
    private static final int MAX_DISTANCIA = 640;
    private static final double COSTE_KM = 2.0;
    private static final int VALOR_DEPOSITO = 1000;
    
    // Extras
    
    private ArrayList<Integer> petNoAtendidas;


    // -----------------------------------------------------
    // CONSTRUCTOR
    // -----------------------------------------------------
    public GasolinaEstado(Gasolineras g, CentrosDistribucion c) {
        this.gasolineras = g;
        this.centros = c;
        this.camionesPorCentro = 1; // HARDCODEADO

        // Inicializa la estructura de asignación
        int numCamiones = c.size() * this.camionesPorCentro;
        asignacionCamionPeticiones = new ArrayList<>();
        for (int i = 0; i < numCamiones; i++) {
            asignacionCamionPeticiones.add(new ArrayList<>());
        }

         System.out.println("PRUEBA COMPLETADA");
        // Estrategia inicial simple (asignar peticiones aleatoriamente)
        generarSolucionInicialOrdenada();
        imprimirResumenCamiones();
        // Calcular métricas iniciales
        //calcularBeneficioYDistancia();
    }

    private void generarSolucionInicialSimple() {
        Random rnd = new Random();
        int numPeticiones = gasolineras.size() * 3; // Máximo 3 peticiones por gasolinera aprox.

        for (int i = 0; i < gasolineras.size(); i++) {
            Gasolinera g = gasolineras.get(i);
            for (int j = 0; j < g.getPeticiones().size(); j++) {
                int camion = rnd.nextInt(centros.size());
                asignacionCamionPeticiones.get(camion).add(i * 10 + j); // ID ficticio de petición
            }
        }
    }

    /* Por cada Centro de Dsitribucion, al primero de sus camiones le sera asignada la gasolinera mas cercana que tenga peticion,
       se recorreran todas las gasolineras hasta que se complete el primer viaje de ese camion(2 gasolineras asignadas). 
       Luego pasa al primer camion del segundo centro, asi hasta visitar todos los camiones de todos los centros */

    private void generarSolucionInicialOrdenada() {
        precalcularMatrizDistancias(); // ya genera gasolinerasOrdenadas[centroId]

        int numCentros = centros.size();
        boolean quedanPeticiones = true;
        while(quedanPeticiones){
            quedanPeticiones = false;


            for (int centroId = 0; centroId < numCentros; centroId++) {
                // Cada camión del centro
                for (int k = 0; k < camionesPorCentro; k++) {
                    int camionId = centroId * camionesPorCentro + k;
                    ArrayList<Integer> asignacionesCamion = asignacionCamionPeticiones.get(camionId);

                    // Si ya hizo MAX_VIAJES, saltamos
                    if (asignacionesCamion.size() / 2 >= MAX_VIAJES) continue;

                    // Buscamos la primera gasolinera con peticiones
                    boolean asignado = false;
                    int atendidas = 0;
                    for (int gasId : gasolinerasOrdenadas[centroId]) {
                        Gasolinera g = gasolineras.get(gasId);


                        if (!cumpleRestricciones(asignacionesCamion, g, centroId) || g.getPeticiones().isEmpty()) continue;

                        Iterator<Integer> it = g.getPeticiones().iterator();
                        while (it.hasNext() && atendidas < 2) {
                            int petId = gasId * 10 + it.next(); // ID ficticio
                            asignacionesCamion.add(petId);
                            it.remove();
                            atendidas++;
                            asignado = true;
                        }

                        if (atendidas == 2) break; 
                    }

                    if (asignado) quedanPeticiones = true;
                }
            }    
        }
            
        calcularBeneficioYDistancia();

    }

    public int calcDistanciaTotalviaje(Distribucion d, Gasolinera g1, Gasolinera g2) {
        if (g1 != null && g2 != null) return calcDistancia(d, g1) + calcDistancia(g1, g2) + calcDistancia(g2, d);
        else if (g1 != null) return calcDistancia(d, g1) + calcDistancia(g1, d);
        else return 0;     
    }
    /* Comprueba si al añadir a las asignaciones del camion en cuestion la gasolinera "a", no se pasa del MAX_KM */
    private boolean cumpleRestricciones(ArrayList<Integer> asignacionesCamion, Gasolinera nueva, Integer centroId){
        
        double distTotal = 0;
        
        for (int i = 0; i < asignacionesCamion.size(); i += 2) {
            Gasolinera g1 = gasolineras.get(asignacionesCamion.get(i) / 10);
            Gasolinera g2 = null;
            if (i + 1 < asignacionesCamion.size()) {
                g2 = gasolineras.get(asignacionesCamion.get(i + 1) / 10);
            }

            distTotal += calcDistanciaTotalviaje(centros.get(centroId), g1, g2);
        }

        // Añadir la nueva gasolinera como último viaje (si es par, g2 = null)
        distTotal += calcDistanciaTotalviaje(centros.get(centroId), nueva, null);

        return distTotal <= MAX_DISTANCIA;
    }

    private static int compararDistancias(int[][] distancias, int fila, int a, int b) {
        return Integer.compare(distancias[fila][a], distancias[fila][b]);
    }

    private int[][] distancias; // distancias[centro][gasolinera]
    private ArrayList<Integer>[] gasolinerasOrdenadas; 

    private void precalcularMatrizDistancias() {
        int nG = this.gasolineras.size();
        int nC = this.centros.size();

        distancias = new int[nC][nG];
        gasolinerasOrdenadas = new ArrayList[nC];

        // --- Calculamos las distancias ---
        for (int i = 0; i < nC; i++) {
            Distribucion c = this.centros.get(i);
            gasolinerasOrdenadas[i] = new ArrayList<>();

            for (int j = 0; j < nG; j++) {
                Gasolinera g = this.gasolineras.get(j);
                int d = calcDistancia(g, c);
                distancias[i][j] = d;
                gasolinerasOrdenadas[i].add(j);
            }

            // Ordenamos las gasolineras del centro i por distancia
            final int idxCentro = i;
            gasolinerasOrdenadas[i].sort((a, b) -> compararDistancias(distancias, idxCentro, a, b));
        }

        System.out.println("Distancias precalculadas [centro][gasolinera] y gasolineras ordenadas por centro");
    }


    public void calcularBeneficioYDistancia() {
        double totalDistancia = 0;
        double totalBeneficio = 0;

        for (int i = 0; i < asignacionCamionPeticiones.size(); i++) {
            ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(i);
            if (peticiones.isEmpty())
                continue;

            // Centro del camión i
            Distribucion centro = centros.get(i);
            int cx = centro.getCoordX();
            int cy = centro.getCoordY();

            for (int p : peticiones) {
                int gasolineraId = p / 10; // (forma de obtenerla del ID ficticio)
                Gasolinera g = gasolineras.get(gasolineraId);
                int gx = g.getCoordX();
                int gy = g.getCoordY();

                double distancia = Math.abs(cx - gx) + Math.abs(cy - gy);
                totalDistancia += 2 * distancia; // ida y vuelta

                // Cálculo del precio con descuento por días pendientes
                for (int dias : g.getPeticiones()) {
                    double porcentajePrecio = (dias == 0) ? 1.02 : (1 - 0.02 * dias);
                    totalBeneficio += VALOR_DEPOSITO * porcentajePrecio;
                }
            }
        }

        // Restar el coste del combustible recorrido
        totalBeneficio -= COSTE_KM * totalDistancia;

        this.beneficioTotal = totalBeneficio;
        this.distanciaTotal = totalDistancia;
    }

    public double getBeneficio() {
        return beneficioTotal;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public GasolinaEstado copia() {
        GasolinaEstado nuevo = new GasolinaEstado(this.gasolineras, this.centros);
        nuevo.asignacionCamionPeticiones = new ArrayList<>();
        for (ArrayList<Integer> lista : this.asignacionCamionPeticiones) {
            nuevo.asignacionCamionPeticiones.add(new ArrayList<>(lista));
        }
        nuevo.beneficioTotal = this.beneficioTotal;
        nuevo.distanciaTotal = this.distanciaTotal;
        return nuevo;
    }

    public double heuristic() {
        return -beneficioTotal; // AIMA minimiza, así que usamos el negativo
    }

    public ArrayList<ArrayList<Integer>> getAsignacionCamionPeticiones() {
        return asignacionCamionPeticiones;
    }

    /* Estado Utils */
    public int calcDistancia(Gasolinera a, Gasolinera b) {
        return Math.abs(a.getCoordX() - b.getCoordX()) + Math.abs(a.getCoordY() - b.getCoordY()); 
    }
    public int calcDistancia(Gasolinera a, Distribucion b) {
        return Math.abs(a.getCoordX() - b.getCoordX()) + Math.abs(a.getCoordY() - b.getCoordY()); 
    }
    public int calcDistancia(Distribucion a, Gasolinera b) {
        return Math.abs(a.getCoordX() - b.getCoordX()) + Math.abs(a.getCoordY() - b.getCoordY()); 
    }
    public int calcDistancia(Distribucion a, Distribucion b) {
        return Math.abs(a.getCoordX() - b.getCoordX()) + Math.abs(a.getCoordY() - b.getCoordY()); 
    }
    

    public void imprimirResumenCamiones() {
        System.out.println("===== Resumen de camiones =====");

        for (int i = 0; i < asignacionCamionPeticiones.size(); i++) {
            ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(i);
            if (peticiones.isEmpty()) {
                System.out.println("Camión " + i + " no tiene peticiones asignadas.\n");
                continue;
            }

            System.out.print("Camión " + i + " atiende gasolineras: ");
            for (int peticion : peticiones) {
                int gasolineraId = peticion / 10;
                System.out.print(gasolineraId + " ");
            }
            System.out.println();

            // Calcular viajes y distancia
            int viajes = 0;
            double distanciaTotalCamion = 0;

            // Obtener centro del camión (asumimos camión i pertenece al centro i)
            Distribucion centro = centros.get(i);
            int cx = centro.getCoordX();
            int cy = centro.getCoordY();

            for (int j = 0; j < peticiones.size(); j += 2) {
                viajes++;

                int pet1 = peticiones.get(j);
                Gasolinera g1 = gasolineras.get(pet1 / 10);
                int gx1 = g1.getCoordX();
                int gy1 = g1.getCoordY();

                int gx2 = gx1, gy2 = gy1;
                if (j + 1 < peticiones.size()) {
                    int pet2 = peticiones.get(j + 1);
                    Gasolinera g2 = gasolineras.get(pet2 / 10);
                    gx2 = g2.getCoordX();
                    gy2 = g2.getCoordY();
                }

                // Distancia: centro -> primera gasolinera
                double distanciaViaje = Math.abs(cx - gx1) + Math.abs(cy - gy1);

                // Si hay segunda gasolinera en el mismo viaje, ir de 1 a 2
                if (j + 1 < peticiones.size()) {
                    distanciaViaje += Math.abs(gx1 - gx2) + Math.abs(gy1 - gy2);
                    // volver al centro desde la segunda gasolinera
                    distanciaViaje += Math.abs(gx2 - cx) + Math.abs(gy2 - cy);
                } else {
                    // volver al centro desde la primera gasolinera
                    distanciaViaje += Math.abs(gx1 - cx) + Math.abs(gy1 - cy);
                }

                distanciaTotalCamion += distanciaViaje;
            }

            System.out.println("Viajes realizados: " + viajes);
            System.out.println("Distancia total recorrida: " + distanciaTotalCamion + "\n");
        }
    }

}
