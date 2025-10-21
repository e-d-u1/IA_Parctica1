import IA.Gasolina.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class GasolinaEstado {

    // Datos estáticos 
    private Gasolineras gasolineras;
    private CentrosDistribucion centros;

    public int MAX_VIAJES;
    public int MAX_DISTANCIA;
    public int MAX_PETICIONES_GAS; 
    public double COSTE_KM;
    public int VALOR_DEPOSITO;    
    
    private int[][] distancias; // distancias[centro][gasolinera]
    private ArrayList<Integer>[] gasolinerasOrdenadas; 
    
    private ArrayList<Integer> peticionesTotalesDias; 
    
    // Datos dinámicos
    /* 
        peticionId: Hemos creado un ID ficticio para cada una, se calcula de la siguiente manera:
            petId = gasId * 10 + index
        Evidentemente aquí suponemos que las gasolineras no van a llegar a más de 10 peticiones(o 10 depósitos)
        si fuera el caso se debería de usar otro multiplicador 
    */
    private ArrayList<ArrayList<Integer>> asignacionCamionPeticiones; // asignacionCamionPeticiones[i] = lista de petId
    private ArrayList<Integer> peticionesPendientes; 
    private ArrayList<Boolean> peticionesUsadas;
    
    private double beneficioTotal;
    private double distanciaTotal;    

    /* --------------------  CREADORAS -------------------- */
    // Constructor único del MAIN
    public GasolinaEstado(Gasolineras g, CentrosDistribucion c, Config cfg) {
        this.gasolineras = g;
        this.centros = c;
        ////System.out.println("Peticiones totales Ini: " + getNumPeticiones());

        // Restricciones del Config
        this.MAX_VIAJES = cfg.maxViajes;
        this.MAX_DISTANCIA = cfg.maxDistancia;
        this.MAX_PETICIONES_GAS = cfg.maxPeticionesGas;
        this.COSTE_KM = cfg.costeKm;
        this.VALOR_DEPOSITO = cfg.valorDeposito;
        

        // Inicializa la estructura de asignación
        int numCamiones = c.size();
        asignacionCamionPeticiones = new ArrayList<>();
        for (int i = 0; i < numCamiones; i++) asignacionCamionPeticiones.add(new ArrayList<>());
        

        peticionesPendientes = new ArrayList<>(); 

        inicializaArrayPeticiones();

        if(cfg.numSolucion == 1) generarSolucionInicialSimple();
        else generarSolucionInicialOrdenada();
        actualizarPeticionesPendientes();
    
        calcularBeneficioYDistancia();
    }

    // Constructor privado para copias
    private GasolinaEstado(Gasolineras g, CentrosDistribucion c, boolean sinInicializar) {
        this.gasolineras = g;
        this.centros = c;

        int numCamiones = c.size();
        asignacionCamionPeticiones = new ArrayList<>();
        for (int i = 0; i < numCamiones; i++) {
            asignacionCamionPeticiones.add(new ArrayList<>());
        }
    }

    public GasolinaEstado copia() {
        GasolinaEstado nuevo = new GasolinaEstado(this.gasolineras, this.centros, true);
        // Todo y ser estático
        nuevo.MAX_VIAJES = this.MAX_VIAJES;
        nuevo.MAX_DISTANCIA = this.MAX_DISTANCIA;
        nuevo.MAX_PETICIONES_GAS = this.MAX_PETICIONES_GAS;
        nuevo.COSTE_KM = this.COSTE_KM;
        nuevo.VALOR_DEPOSITO = this.VALOR_DEPOSITO;

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

    /* ------------------ INICIALIZACIONES -------------------- */
    private void inicializaArrayPeticiones() {
        int numGasolineras = this.gasolineras.size();
        peticionesTotalesDias = new ArrayList<>();
        peticionesUsadas = new ArrayList<>();

        // Inicializamos con null y false respetando petId
        for (int i = 0; i < numGasolineras * 10 + 10; i++) {
            peticionesTotalesDias.add(null);
            peticionesUsadas.add(false);
        }

        // Llenamos solo los que existen realmente
        for (int gasId = 0; gasId < numGasolineras; gasId++) {
            Gasolinera gas = this.gasolineras.get(gasId);
            int index = 0;
            for (int dias : gas.getPeticiones()) {
                int petId = gasId * 10 + index;
                peticionesTotalesDias.set(petId, dias);
                peticionesUsadas.set(petId, false); // inicialmente ninguna está usada
                index++;
            }
        }
    }
    private void actualizarPeticionesPendientes() {
        peticionesPendientes.clear();
        for (int petId = 0; petId < peticionesTotalesDias.size(); petId++) {
            Integer dias = peticionesTotalesDias.get(petId);
            if (dias != null && !peticionesUsadas.get(petId)) {
                peticionesPendientes.add(petId);
            }
        }
    }
    
    /* ------------------ GETTERS -------------------- */
    public ArrayList<ArrayList<Integer>> getAsignacionCamionPeticiones() {return asignacionCamionPeticiones;}
    public ArrayList<Integer> getPeticionesPendientes() {return peticionesPendientes;}
    public double getBeneficio() {return beneficioTotal;}
    public double getDistanciaTotal() {return distanciaTotal;}
    
    public int getNumPeticiones(){
        int total = 0;
        for (Gasolinera g : gasolineras) total += g.getPeticiones().size();
        return total;
    }
    public int getViajesCamion(int camionId) {
        ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(camionId);
        if (peticiones.isEmpty()) return 0;
        return (peticiones.size() + 1) / 2;
    }
    public double getDistanciaCamion(int camionId) {
        double distTotal = 0;
        ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(camionId);
        if (peticiones.isEmpty()) return 0;

        Distribucion centro = centros.get(camionId );
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
   
    public int getTotalPeticionesAsignadas() {
        int total = 0;
        for (ArrayList<Integer> camion : asignacionCamionPeticiones) {
            total += camion.size();
        }
        return total;
    }

    

    /* --------------------  Sols. Iniciales -------------------- */
    private void generarSolucionInicialSimple() {
        // Crear lista de peticiones pendientes temporal
        ArrayList<Integer> pendientes = new ArrayList<>();

        for (int i = 0; i < gasolineras.size(); i++) {
            Gasolinera g = gasolineras.get(i);
            for (int j = 0; j < g.getPeticiones().size(); j++) {
                pendientes.add(i * 10 + j); // ID único: gasolinera*10 + depósito
            }
        }

        // Inicializar métricas de cada camión
        int numCamiones = centros.size();
        int[] viajesRealizados = new int[numCamiones];
        double[] distanciaRecorrida = new double[numCamiones];

        Random rnd = new Random();
        ArrayList<Integer> noAsignadas = new ArrayList<>();

        // Mientras queden peticiones pendientes
        while (!pendientes.isEmpty()) {
            // Tomamos una petición al azar
            int index = rnd.nextInt(pendientes.size());
            int petId = pendientes.get(index);

            int gasolineraId = petId / 10;
            Gasolinera g = gasolineras.get(gasolineraId);

            // Buscar el camión más cercano que pueda atenderla
            int mejorCamion = -1;
            double menorDistancia = Double.MAX_VALUE;

            for (int i = 0; i < numCamiones; i++) {
                if (viajesRealizados[i] >= MAX_VIAJES) continue;

                Distribucion centro = centros.get(i);
                double distanciaViaje = Math.abs(centro.getCoordX() - g.getCoordX()) +
                                        Math.abs(centro.getCoordY() - g.getCoordY());
                distanciaViaje *= 2; // ida y vuelta

                if (distanciaRecorrida[i] + distanciaViaje > MAX_DISTANCIA) continue;

                if (distanciaViaje < menorDistancia) {
                    menorDistancia = distanciaViaje;
                    mejorCamion = i;
                }
            }

            // Si encontramos camión válido, asignamos
            if (mejorCamion != -1) {
                asignacionCamionPeticiones.get(mejorCamion).add(petId);
                peticionesUsadas.set(petId, true);
                viajesRealizados[mejorCamion]++;
                distanciaRecorrida[mejorCamion] += menorDistancia;
            } else {
                // Si no se puede asignar, queda pendiente
                noAsignadas.add(petId);
            }

            // Quitamos la petición temporal de la lista
            pendientes.remove(index);
        }

        // Guardar las que no pudieron asignarse en peticionesPendientes
        peticionesPendientes.addAll(noAsignadas);
    }

    /* Por cada Centro de Dsitribucion, al primero de sus camiones le sera asignada la gasolinera mas cercana que tenga peticion,
       se recorreran todas las gasolineras hasta que se complete el primer viaje de ese camion(2 gasolineras asignadas). 
       Luego pasa al primer camion del segundo centro, asi hasta visitar todos los camiones de todos los centros */

    private void generarSolucionInicialOrdenada() {
        precalcularMatrizDistancias(); // Genera gasolinerasOrdenadas[centroId][]

        int numCamiones = centros.size();    
        boolean quedanPeticiones = true;

        while (quedanPeticiones) {
            quedanPeticiones = false;

            // Cada camión del centro( === a número de centros)
            for (int camionId = 0; camionId < numCamiones; camionId++) {
                ArrayList<Integer> asignacionesCamion = asignacionCamionPeticiones.get(camionId);

                if (asignacionesCamion.size() / 2 >= MAX_VIAJES) continue;

                boolean asignado = false;
                int atendidas = 0;

                for (int gasId : gasolinerasOrdenadas[camionId]) {
                    Gasolinera g = gasolineras.get(gasId);
                    if (g.getPeticiones().isEmpty()) continue;

                    if (!cumpleRestricciones(asignacionesCamion, g, camionId)) continue;

                    for (int idx = 0; idx < g.getPeticiones().size() && atendidas < 2; idx++) {
                        int petId = gasId * 10 + idx;

                        // Si ya está usada, saltamos
                        if (peticionesUsadas.get(petId)) continue;

                        // Asignamos al camión
                        asignacionesCamion.add(petId);
                        peticionesUsadas.set(petId, true);
                        atendidas++;
                        asignado = true;
                    }

                    if (atendidas == 2) break;
                }

                if (asignado) quedanPeticiones = true;
            
            }
        }
    }

    /* -------------------- Heurística -------------------- */
    
    public double heuristic() {
        return -beneficioTotal; // AIMA minimiza, así que usamos el negativo
    }

    /* -------------------- Cálculos -------------------- */

    /* Comprueba si al añadir a las asignaciones del camion en cuestion la gasolinera "nueva", no se pasa del MAX_KM */
    private boolean cumpleRestricciones(ArrayList<Integer> asignacionesCamion, Gasolinera nueva, Integer camionId){
        
        double distTotal = 0;
        // Como hacemos un array dinámico hay que iterar en pares para saber los viajes
        for (int i = 0; i < asignacionesCamion.size(); i += 2) {
            Gasolinera g1 = gasolineras.get(asignacionesCamion.get(i) / 10);
            Gasolinera g2 = null;
            if (i + 1 < asignacionesCamion.size()) {
                g2 = gasolineras.get(asignacionesCamion.get(i + 1) / 10);
            }
            distTotal += calcDistanciaTotalviaje(centros.get(camionId), g1, g2);
        }

        // Si el vector es par, en realidad estamos hablando de un nuevo viaje(g2 = null)
        distTotal += calcDistanciaTotalviaje(centros.get(camionId), nueva, null);

        return distTotal <= MAX_DISTANCIA;
    }

    // Post: Actualiza las variables de la instancia del beneficio y distancia total de la solución
    public void calcularBeneficioYDistancia() {
        double totalDistancia = 0;
        double totalBeneficio = 0;

        for (int i = 0; i < asignacionCamionPeticiones.size(); i++) {
            ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(i);
            if (peticiones.isEmpty())
                continue;

            Distribucion centro = centros.get(i );

            for (int j = 0; j < peticiones.size(); j += 2) {
                // Peticiones del viaje actual
                
                int petId1 = peticiones.get(j);
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

        totalBeneficio =  totalBeneficio  - COSTE_KM * totalDistancia;

        this.beneficioTotal = totalBeneficio;
        this.distanciaTotal = totalDistancia;
    }


    // SOLO USADO PARA EXPERIMENTO 6
    public ArrayList<Integer> getDiasPeticiones() {
        ArrayList<Integer> diasPeticiones = new ArrayList<>();

        int atendidasDia0 = 0;
        int atendidasDia1 = 0;
        int atendidasDia2 = 0;
        int atendidasDia3 = 0;

        for (ArrayList<Integer> camion : this.getAsignacionCamionPeticiones()) {
            for (Integer petId : camion) {
                Integer dias = this.peticionesTotalesDias.get(petId);  // accede directo a la variable
                if (dias == 0) atendidasDia0++;
                else if (dias == 1) atendidasDia1++;
                else if (dias == 2) atendidasDia2++;
                else if (dias == 3) atendidasDia3++;
            }
        }

        diasPeticiones.add(atendidasDia0);
        diasPeticiones.add(atendidasDia1);
        diasPeticiones.add(atendidasDia2);
        diasPeticiones.add(atendidasDia3);

        return diasPeticiones;
    }

    /* ----------------- Estado Cálculos/Utils -----------------  */

    private static int compararDistancias(int[][] distancias, int fila, int a, int b) {
        return Integer.compare(distancias[fila][a], distancias[fila][b]);
    }
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
    }
    public int calcDistanciaTotalviaje(Distribucion d, Gasolinera g1, Gasolinera g2) {
        if (g1 != null && g2 != null) return calcDistancia(d, g1) + calcDistancia(g1, g2) + calcDistancia(g2, d);
        else if (g1 != null) return calcDistancia(d, g1) + calcDistancia(g1, d);
        else return 0;     
    }
    
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

    /* Prints de información */
    public void imprimirResumenCamiones() {
        System.out.println("===== Resumen de camiones =====");

        for (int i = 0; i < asignacionCamionPeticiones.size(); i++) {
            ArrayList<Integer> peticiones = asignacionCamionPeticiones.get(i);
            if (peticiones.isEmpty()) {
                //System.out.println("Camión " + i + " no tiene peticiones asignadas.\n");
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
            Distribucion centro = centros.get(i );
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
