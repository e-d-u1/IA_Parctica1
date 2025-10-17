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
    private ArrayList<Integer> peticionesPendientes; 
    
    // asignacionCamionPeticiones[i] = lista de IDs de peticiones que atiende el camión i

    private double beneficioTotal;
    private double distanciaTotal;

    public static final int MAX_VIAJES = 5;
    public static final int MAX_DISTANCIA = 640;
    public static final int MAX_PETICIONES_GAS = 3; 
    public static final double COSTE_KM = 2.0;
    public static final int VALOR_DEPOSITO = 1000;
    
    // Integracion Solucion 2

    // cada índice corresponde al mismo ID de petición que usas en asignacionCamionPeticiones
    // ejemplo: petId = gasId * 10 + index
    // valor = días pendientes    
    private ArrayList<Integer> peticionesTotalesDias; 

    private void inicializaArrayPeticiones() {
        int numGasolineras = this.gasolineras.size();
        peticionesTotalesDias = new ArrayList<>();

        // Rellenamos con null o -1
        for (int i = 0; i < numGasolineras * 10 + 10; i++) peticionesTotalesDias.add(null); 

        // Llenamos los días reales
        for (int gasId = 0; gasId < numGasolineras; gasId++) {
            Gasolinera gas = this.gasolineras.get(gasId);
            int index = 0;
            for (int dias : gas.getPeticiones()) {
                int petId = gasId * 10 + index;
                peticionesTotalesDias.set(petId, dias);
                index++;
            }
        }
    }

    // CONSTRUCTOR
    public GasolinaEstado(Gasolineras g, CentrosDistribucion c) {
        this.gasolineras = g;
        this.centros = c;
        this.camionesPorCentro = 1; // HARDCODEADO
        System.out.println("Peticiones totales Ini: " + getNumPeticiones());

        // Inicializa la estructura de asignación
        int numCamiones = c.size() * this.camionesPorCentro;
        asignacionCamionPeticiones = new ArrayList<>();
        for (int i = 0; i < numCamiones; i++) {
            asignacionCamionPeticiones.add(new ArrayList<>());
        }

        peticionesPendientes = new ArrayList<>(); 

        inicializaArrayPeticiones();

        generarSolucionInicialSimple();

        calcularBeneficioYDistancia();

        
    }

    // Constructor privado sin inicializar
    private GasolinaEstado(Gasolineras g, CentrosDistribucion c, boolean sinInicializar) {
        this.gasolineras = g;
        this.centros = c;
        this.camionesPorCentro = 1;

        int numCamiones = c.size() * this.camionesPorCentro;
        asignacionCamionPeticiones = new ArrayList<>();
        for (int i = 0; i < numCamiones; i++) {
            asignacionCamionPeticiones.add(new ArrayList<>());
        }
    }

    private void generarSolucionInicialSimple() {
        // Crear lista de peticiones pendientes

        for (int i = 0; i < gasolineras.size(); i++) {
            Gasolinera g = gasolineras.get(i);
            for (int j = 0; j < g.getPeticiones().size(); j++) {
                peticionesPendientes.add(i * 10 + j); // ID único: gasolinera*10 + deposito
            }
        }

        // Inicializar métricas de cada camión
        int numCamiones = centros.size();
        int[] viajesRealizados = new int[numCamiones];
        double[] distanciaRecorrida = new double[numCamiones];

        Random rnd = new Random();

        // Mientras queden peticiones pendientes
        while (!peticionesPendientes.isEmpty()) {
            // Tomamos una petición al azar
            int index = rnd.nextInt(peticionesPendientes.size());
            int petId = peticionesPendientes.get(index);

            int gasolineraId = petId / 10;
            Gasolinera g = gasolineras.get(gasolineraId);

            // Buscar el camión más cercano que pueda atenderla
            int mejorCamion = -1;
            double menorDistancia = Double.MAX_VALUE;

            for (int i = 0; i < numCamiones; i++) {
                if (viajesRealizados[i] >= MAX_VIAJES)
                    continue;

                Distribucion centro = centros.get(i);
                double distanciaViaje = Math.abs(centro.getCoordX() - g.getCoordX()) +
                        Math.abs(centro.getCoordY() - g.getCoordY());
                distanciaViaje *= 2; // ida y vuelta

                if (distanciaRecorrida[i] + distanciaViaje > MAX_DISTANCIA)
                    continue;

                if (distanciaViaje < menorDistancia) {
                    menorDistancia = distanciaViaje;
                    mejorCamion = i;
                }
            }

            // Si encontramos camión válido, asignamos
            if (mejorCamion != -1) {
                asignacionCamionPeticiones.get(mejorCamion).add(petId);
                viajesRealizados[mejorCamion]++;
                distanciaRecorrida[mejorCamion] += menorDistancia;
            }
            // La petición se elimina de pendientes aunque no se haya asignado
            peticionesPendientes.remove(index);
        }

    }


    /* Por cada Centro de Dsitribucion, al primero de sus camiones le sera asignada la gasolinera mas cercana que tenga peticion,
       se recorreran todas las gasolineras hasta que se complete el primer viaje de ese camion(2 gasolineras asignadas). 
       Luego pasa al primer camion del segundo centro, asi hasta visitar todos los camiones de todos los centros */

    private void generarSolucionInicialOrdenada() {
        precalcularMatrizDistancias(); // ya genera gasolinerasOrdenadas[centroId]

        System.out.println("GENERAMOS SOLUCION INICIAL ORDENADA");
                        
        int numCentros = centros.size();
        boolean quedanPeticiones = true;
        while(quedanPeticiones){
            quedanPeticiones = false;

            for (int centroId = 0; centroId < numCentros; centroId++) {
                // Cada camión del centro
                for (int k = 0; k < camionesPorCentro; k++) {
                    int camionId = centroId * camionesPorCentro + k;
                    ArrayList<Integer> asignacionesCamion = asignacionCamionPeticiones.get(camionId);

                  
                    if (asignacionesCamion.size() / 2 >= MAX_VIAJES) continue;

                    // Primea gasolinera con peticiones
                    boolean asignado = false;
                    
                    int atendidas = 0;
                    for (int gasId : gasolinerasOrdenadas[centroId]) {
                        Gasolinera g = gasolineras.get(gasId);
                        if(g.getPeticiones().isEmpty()) continue;
                        //System.out.println("Para el camion " + camionId + " Y su asignacion " + asignacionesCamion.size()  + " puede usar gas?? " + gasId);
                        

                        if (!cumpleRestriccionesEdu(asignacionesCamion, g, centroId)) continue;
                        
                        //System.out.println("Para el camion " + camionId + " Y su asignacion " + asignacionesCamion.size()  + " puede usar gas " + gasId);
                        
                        for (int idx = 0; idx < g.getPeticiones().size() && atendidas < 2; ) {
                            int dias = g.getPeticiones().get(idx); // días pendientes
                            int petId = gasId * 10 + idx; // ID correcto

                            asignacionesCamion.add(petId);
                            g.getPeticiones().remove(idx); // eliminas la petición usada
                            atendidas++;
                            asignado = true;
                        }

                        if (atendidas == 2) break; 
                    }

                    if (asignado) quedanPeticiones = true;
                }
            }    
        }
    }
    private int getNumPeticiones(){
        int total = 0;
        for (Gasolinera g : gasolineras) {
            total += g.getPeticiones().size();
        }
        return total;
    }

    public int calcDistanciaTotalviaje(Distribucion d, Gasolinera g1, Gasolinera g2) {
        if (g1 != null && g2 != null) return calcDistancia(d, g1) + calcDistancia(g1, g2) + calcDistancia(g2, d);
        else if (g1 != null) return calcDistancia(d, g1) + calcDistancia(g1, d);
        else return 0;     
    }
    /* Comprueba si al añadir a las asignaciones del camion en cuestion la gasolinera "a", no se pasa del MAX_KM */
    private boolean cumpleRestriccionesEdu(ArrayList<Integer> asignacionesCamion, Gasolinera nueva, Integer centroId){
        
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

            Distribucion centro = centros.get(i);

            for (int j = 0; j < peticiones.size(); j += 2) {
                // Peticiones del viaje actual
                
                int petId1 = peticiones.get(j);
                //System.out.println("La peticion " + petId1 + " tiene de longitud: " + peticionesTotalesDias.size());
                Gasolinera g1 = gasolineras.get(petId1 / 10);

                Gasolinera g2 = null;
                Integer petId2 = null;
                if (j + 1 < peticiones.size()) {
                    petId2 = peticiones.get(j + 1);
                    g2 = gasolineras.get(petId2 / 10);
                }

                // Distancia del viaje completo
                totalDistancia += calcDistanciaTotalviaje(centro, g1, g2);

                // Beneficio de la primera petición
                Integer dias1 = peticionesTotalesDias.get(petId1);
                if (dias1 != null) {
                    double porcentajePrecio = (dias1 == 0) ? 1.02 : (1 - 0.02 * dias1);
                    totalBeneficio += VALOR_DEPOSITO * porcentajePrecio;
                }

                // Beneficio de la segunda petición (si existe)
                if (petId2 != null) {
                    Integer dias2 = peticionesTotalesDias.get(petId2);
                    if (dias2 != null) {
                        double porcentajePrecio = (dias2 == 0) ? 1.02 : (1 - 0.02 * dias2);
                        totalBeneficio += VALOR_DEPOSITO * porcentajePrecio;
                    }
                }
            }
        }

        //System.out.println("Tenemos beneficio de "  + totalBeneficio + " con distandia de " + totalDistancia);
        // Restar coste de combustible
        totalBeneficio =  totalBeneficio  - COSTE_KM * totalDistancia;

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
        GasolinaEstado nuevo = new GasolinaEstado(this.gasolineras, this.centros, true);

        // Copiar la asignación de camiones
        for (int i = 0; i < this.asignacionCamionPeticiones.size(); i++) {
            nuevo.asignacionCamionPeticiones.set(i, new ArrayList<>(this.asignacionCamionPeticiones.get(i)));
        }

        // Copiar métricas
        nuevo.peticionesTotalesDias = this.peticionesTotalesDias;
        nuevo.beneficioTotal = this.beneficioTotal;
        nuevo.distanciaTotal = this.distanciaTotal;
        
        nuevo.peticionesTotalesDias = new ArrayList<>(this.peticionesTotalesDias);
        if (this.peticionesPendientes != null)
            nuevo.peticionesPendientes = new ArrayList<>(this.peticionesPendientes);

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
    
    public int getViajesCamion(int camionId) {
        ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(camionId);
        if (peticiones.isEmpty()) return 0;
        // Cada viaje puede llevar hasta 2 peticiones
        return (peticiones.size() + 1) / 2;
    }

    public double getDistanciaCamion(int camionId) {
        double distTotal = 0;
        ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(camionId);
        if (peticiones.isEmpty()) return 0;

        Distribucion centro = centros.get(camionId);
        int cx = centro.getCoordX();
        int cy = centro.getCoordY();

        for (int j = 0; j < peticiones.size(); j += 2) {
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

            double distViaje = Math.abs(cx - gx1) + Math.abs(cy - gy1);
            if (j + 1 < peticiones.size()) {
                distViaje += Math.abs(gx1 - gx2) + Math.abs(gy1 - gy2);
                distViaje += Math.abs(gx2 - cx) + Math.abs(gy2 - cy);
            } else {
                distViaje += Math.abs(gx1 - cx) + Math.abs(gy1 - cy);
            }

            distTotal += distViaje;
        }
        return distTotal;
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

    public ArrayList<Integer> getPeticionesPendientes() {
        return peticionesPendientes; 
    }



}
