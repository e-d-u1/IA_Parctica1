import IA.Gasolina.*;


import aima.search.framework.SuccessorFunction;
import aima.search.framework.Successor;
import java.util.ArrayList;
import java.util.List;

public class GasolinaSuccesorFunction implements SuccessorFunction {

    @Override
    public List getSuccessors(Object state) {
        ArrayList<Successor> retval = new ArrayList<>();
        GasolinaEstado board = (GasolinaEstado) state;

        ArrayList<ArrayList<Integer>> asignaciones = board.getAsignacionCamionPeticiones();
        int numCamiones = asignaciones.size();

        // 1. Mover una petición de un camión a otro ---

        // Recorremos cada camión y sus peticiones
        for (int i = 0; i < numCamiones; i++) {
            ArrayList<Integer> camionI = asignaciones.get(i);

            // Recorremos las peticiones del camión i
            for (int j = 0; j < camionI.size(); j++) {
                int peticion = camionI.get(j);

                // Intentamos mover la petición a otro camión k
                for (int k = 0; k < numCamiones; k++) {
                    if (k == i)
                        continue; // no mover al mismo camión

                    // Crear copia del estado
                    GasolinaEstado newBoard = board.copia();

                    // Mover la petición al otro camión

                    // se hace remove de la primera aparación del objeto petición, para esto sirve
                    // el casteo
                    // porque si hacemos el remove directo con el int, lo interpreta como índice
                    // y puede haber errores por el desplazamiento de índices al eliminar elementos
                    newBoard.getAsignacionCamionPeticiones().get(i).remove((Integer) peticion);
                    newBoard.getAsignacionCamionPeticiones().get(k).add(peticion);

                    // Recalcular beneficio y distancia
                    newBoard.calcularBeneficioYDistancia();

                    // Verificar restricciones
                    if (cumpleRestricciones(newBoard, k) && cumpleRestricciones(newBoard, i)) {
                        retval.add(new Successor(
                                "Mover peticion " + peticion + " de camión " + i + " a " + k,
                                newBoard));
                    }
                }
            }
        }

        // 2. Intercambiar peticiones entre camiones ---
        // iteramos sobre todos los pares de camiones (i, j)
        for (int i = 0; i < numCamiones; i++) {
            for (int j = i + 1; j < numCamiones; j++) {
                ArrayList<Integer> camionI = asignaciones.get(i);
                ArrayList<Integer> camionJ = asignaciones.get(j);

                // Iteramos sobre todas las peticiones de los dos camiones
                for (int pi = 0; pi < camionI.size(); pi++) {
                    for (int pj = 0; pj < camionJ.size(); pj++) {
                        int petI = camionI.get(pi);
                        int petJ = camionJ.get(pj);

                        // copia del estado
                        GasolinaEstado newBoard = board.copia();

                        // hacemos el swap
                        // ses hace set en las posiciones correspondientes con las peticiones
                        // intercambiadas
                        newBoard.getAsignacionCamionPeticiones().get(i).set(pi, petJ);
                        newBoard.getAsignacionCamionPeticiones().get(j).set(pj, petI);

                        newBoard.calcularBeneficioYDistancia();

                        // Verificar restricciones
                        if (cumpleRestricciones(newBoard, i) && cumpleRestricciones(newBoard, j)) {
                            retval.add(new Successor(
                                    "Swap peticion " + petI + " (camion " + i + ") con " + petJ + " (camion " + j + ")",
                                    newBoard));
                        }
                    }
                }
            }
        }

        // 3. Desasignar una petición de un camión (devolver a pendientes)

        // recorremos tdos los camiones y sus peticiones
        for (int i = 0; i < numCamiones; i++) {
            ArrayList<Integer> camionI = asignaciones.get(i);
            for (int j = 0; j < camionI.size(); j++) {
                int peticion = camionI.get(j);

                // creamos copia del estado
                GasolinaEstado newBoard = board.copia();

                // Quitamos la petición del camión i 
                // casteandolo a integer por la misma razon que en 1. 
                newBoard.getAsignacionCamionPeticiones().get(i).remove((Integer) peticion);

                // Añadirla a la lista de pendientes
                newBoard.getPeticionesPendientes().add(peticion);

                // Recalcular métricas
                newBoard.calcularBeneficioYDistancia();

                retval.add(new Successor(
                        "Desasignar petición " + peticion + " del camión " + i,
                        newBoard));
            }
        }

        // 4. Asignar una petición pendiente a un camión
        ArrayList<Integer> pendientes = new ArrayList<>(board.getPeticionesPendientes()); // ← copia segura
        
        // recorremos tdos los camiones y sus peticiones
        for (int pet : pendientes) {
            for (int i = 0; i < numCamiones; i++) {

                // Crear copia del estado
                GasolinaEstado newBoard = board.copia();

                // Comprobar restricciones antes de añadir
                if (!cumpleRestricciones(newBoard, i))
                    continue;

                // Añadir petición al camión
                newBoard.getAsignacionCamionPeticiones().get(i).add(pet);

                // Quitarla de pendientes
                newBoard.getPeticionesPendientes().remove((Integer) pet);

                // Recalcular métricas
                newBoard.calcularBeneficioYDistancia();

                // Verificar que sigue cumpliendo restricciones
                if (cumpleRestricciones(newBoard, i)) {
                    retval.add(new Successor(
                            "Añadir petición " + pet + " al camión " + i,
                            newBoard));
                }
            }
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