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

        // --- 1. Mover una petición de un camión a otro ---
        for (int i = 0; i < numCamiones; i++) {
            ArrayList<Integer> camionI = asignaciones.get(i);
            for (int j = 0; j < camionI.size(); j++) {
                int peticion = camionI.get(j);

                for (int k = 0; k < numCamiones; k++) {
                    if (k == i) continue; // no mover al mismo camión

                    // Crear copia del estado
                    GasolinaEstado newBoard = board.copia();

                    // Mover la petición al otro camión
                    newBoard.getAsignacionCamionPeticiones().get(i).remove((Integer) peticion);
                    newBoard.getAsignacionCamionPeticiones().get(k).add(peticion);

                    // Recalcular beneficio/distancia
                    newBoard.calcularBeneficioYDistancia();

                    // Añadir sucesor
                    retval.add(new Successor("Mover peticion " + peticion + " de camión " + i + " a " + k, newBoard));
                }
            }
        }

        // --- 2. Intercambiar peticiones entre camiones ---
        for (int i = 0; i < numCamiones; i++) {
            for (int j = i + 1; j < numCamiones; j++) {
                ArrayList<Integer> camionI = asignaciones.get(i);
                ArrayList<Integer> camionJ = asignaciones.get(j);

                for (int pi = 0; pi < camionI.size(); pi++) {
                    for (int pj = 0; pj < camionJ.size(); pj++) {
                        int petI = camionI.get(pi);
                        int petJ = camionJ.get(pj);

                        // Crear copia del estado
                        GasolinaEstado newBoard = board.copia();

                        // Hacer swap
                        newBoard.getAsignacionCamionPeticiones().get(i).set(pi, petJ);
                        newBoard.getAsignacionCamionPeticiones().get(j).set(pj, petI);

                        newBoard.calcularBeneficioYDistancia();

                        retval.add(new Successor("Swap peticion " + petI + " (camion " + i + ") con " + petJ + " (camion " + j + ")", newBoard));
                    }
                }
            }
        }

        return retval;
    }
}
