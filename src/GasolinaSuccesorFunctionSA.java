import IA.Gasolina.*;
import aima.search.framework.*;
import aima.search.informed.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GasolinaSuccesorFunctionSA implements SuccessorFunction {

    private Random rand = new Random();

    @Override
    public List<Successor> getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>();
        GasolinaEstado board = (GasolinaEstado) state;

        ArrayList<ArrayList<Integer>> asignaciones = board.getAsignacionCamionPeticiones();
        int numCamiones = asignaciones.size();

        // Elegir aleatoriamente el tipo de operación: 0=mover, 1=swap, 2=desasignar,
        // 3=asignar pendiente
        int operador = rand.nextInt(4);

        GasolinaEstado newBoard = board.copia();
        String action = "";

        switch (operador) {
            case 0:
                // Mover una petición de un camión a otro
                // se elige un camion aleatoriamente y una petición aleatoria de ese camión
                int fromCamion = rand.nextInt(numCamiones);
                ArrayList<Integer> peticionesFrom = asignaciones.get(fromCamion);
                if (!peticionesFrom.isEmpty()) {
                    // obtenemos una petición aleatoria
                    int peticion = peticionesFrom.get(rand.nextInt(peticionesFrom.size()));

                    // añadimos todos los camiones diferentes al fromCamion a una lista
                    ArrayList<Integer> otrosCamiones = new ArrayList<>();
                    for (int i = 0; i < numCamiones; i++) {
                        if (i != fromCamion)
                            otrosCamiones.add(i);
                    }
                    // escogemos un camión aleatoriamente de los otros camiones
                    int toCamion = otrosCamiones.get(rand.nextInt(otrosCamiones.size()));

                    // Realizamos el movimiento
                    newBoard.getAsignacionCamionPeticiones().get(fromCamion).remove((Integer) peticion);
                    newBoard.getAsignacionCamionPeticiones().get(toCamion).add(peticion);

                    newBoard.calcularBeneficioYDistancia();
                    if (cumpleRestricciones(newBoard, fromCamion) && cumpleRestricciones(newBoard, toCamion)) {
                        action = "Mover peticion " + peticion + " de camión " + fromCamion + " a " + toCamion;
                        retval.add(new Successor(action, newBoard));
                    }
                }
                break;

            case 1:
                // Swap entre dos camiones
                int camion1 = rand.nextInt(numCamiones);

                // lo mismo que en case 0 
                ArrayList<Integer> otrosCamiones = new ArrayList<>();
                for (int i = 0; i < numCamiones; i++) {
                    if (i != camion1)
                        otrosCamiones.add(i);
                }

                int camion2 = otrosCamiones.get(rand.nextInt(otrosCamiones.size()));

                ArrayList<Integer> list1 = asignaciones.get(camion1);
                ArrayList<Integer> list2 = asignaciones.get(camion2);

                if (!list1.isEmpty() && !list2.isEmpty()) {

                    // hacemos el swap 
                    int idx1 = rand.nextInt(list1.size());
                    int idx2 = rand.nextInt(list2.size());

                    int temp = list1.get(idx1);
                    newBoard.getAsignacionCamionPeticiones().get(camion1).set(idx1, list2.get(idx2));
                    newBoard.getAsignacionCamionPeticiones().get(camion2).set(idx2, temp);


                    newBoard.calcularBeneficioYDistancia();

                    if (cumpleRestricciones(newBoard, camion1) && cumpleRestricciones(newBoard, camion2)) {
                        action = "Swap peticion " + temp + " (camion " + camion1 + ") con " + list2.get(idx2)
                                + " (camion " + camion2 + ")";
                        retval.add(new Successor(action, newBoard));
                    }
                }
                break;

            case 2:
                // Desasignar una petición de un camión
                // indice del camion a desasignar
                int camDesasignar = rand.nextInt(numCamiones);
                // peticones del camion a desasignar
                ArrayList<Integer> petCam = asignaciones.get(camDesasignar);

                if (!petCam.isEmpty()) {
                    int peticion = petCam.get(rand.nextInt(petCam.size()));
                    newBoard.getPeticionesPendientes().add(peticion);
                    newBoard.getAsignacionCamionPeticiones().get(camDesasignar).remove((Integer) peticion);

                    newBoard.calcularBeneficioYDistancia();

                    action = "Desasignar petición " + peticion + " del camión " + camDesasignar;
                }
                break;

            case 3:
                // Asignar una petición pendiente a un camión
                // obtenemos la lista de pendientes
                ArrayList<Integer> pendientes = new ArrayList<>(board.getPeticionesPendientes());
                if (!pendientes.isEmpty()) {
                    // seleccionamos una petición aleatoria y un camión aleatorio
                    int pet = pendientes.get(rand.nextInt(pendientes.size()));
                    int camAsignar = rand.nextInt(numCamiones);

                    // se realiza la asignación
                    newBoard.getAsignacionCamionPeticiones().get(camAsignar).add(pet);
                    newBoard.getPeticionesPendientes().remove((Integer) pet);

                    newBoard.calcularBeneficioYDistancia();
                    if (cumpleRestricciones(newBoard, camAsignar)) {
                        action = "Añadir petición " + pet + " al camión " + camAsignar;
                        retval.add(new Successor(action, newBoard));
                    }

                }
                break;
        }
        return retval;
    }

    // Función auxiliar para verificar restricciones de un camión
    private boolean cumpleRestricciones(GasolinaEstado estado, int camionId) {
        // Calcular viajes y distancia del camión
        double distancia = estado.getDistanciaCamion(camionId);
        int viajes = estado.getViajesCamion(camionId);

        return distancia <= GasolinaEstado.MAX_DISTANCIA && viajes <= GasolinaEstado.MAX_VIAJES;
    }
}


