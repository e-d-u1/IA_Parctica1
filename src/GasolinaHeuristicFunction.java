import IA.Gasolina.*;


import aima.search.framework.HeuristicFunction;

public class GasolinaHeuristicFunction implements HeuristicFunction {

    public double getHeuristicValue(Object n){

        return ((GasolinaEstado) n).heuristic();
    }
}
