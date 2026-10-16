
package cajero;
 
import java.util.HashMap;
 

public class LogicaCajero {
 
    public static final int[] DENOMINACIONES = {100000, 50000, 20000, 10000};
    private static final int BILLETES_INICIALES = 100;
    private final HashMap<Integer, Integer> billetes = new HashMap<>();
    public LogicaCajero() {
        for (int denominacion : DENOMINACIONES) {
            billetes.put(denominacion, BILLETES_INICIALES);
        }
    }
