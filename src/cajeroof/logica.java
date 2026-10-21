package cajeroof;
 
import java.util.HashMap;
 
public class logica {
 

    public static final int[]  DENOMINACIONES = {100000, 50000, 20000, 10000};
        private   static final  int BILLETES_INICIALES = 100;
          private    final  HashMap<Integer, Integer> billetes = new HashMap<>();
      public logica() {
        for (int  denominacion : DENOMINACIONES) {
            billetes.put(denominacion, BILLETES_INICIALES);
        }
    }
 
        public int getCantidad(int denominacion) {
           return billetes.get(denominacion);
    }
 
     public long getSaldoTotal() {
         long total = 0;
        for (int denominacion : DENOMINACIONES) {
               total += (long) denominacion * billetes.get(denominacion);
        }
        return total;
    }
 
         public HashMap<Integer, Integer> calcularEntrega(long monto) {
        HashMap<Integer, Integer> entrega = armarEntrega(monto, true);
             if (entrega == null) {
            entrega = armarEntrega(monto, false);
        }
        return entrega;
    }

    private HashMap<Integer, Integer> armarEntrega(long monto, boolean usarReglas) {
        HashMap<Integer, Integer> entrega = new HashMap<>();
        for (int d : DENOMINACIONES) {
            entrega.put(d, 0);
        }
 
        long restante = monto; 
 
        if (usarReglas) {
            if (monto == 50000) {
                entrega.put(20000, 2);
                entrega.put(10000, 1);
                restante = 0;
            } else if (monto == 100000) {
                entrega.put(50000, 1);
                entrega.put(20000, 2);
                entrega.put(10000, 1);
                restante = 0;
            } else if (monto > 100000) {
                entrega.put(50000, 1);
                entrega.put(20000, 2);
                entrega.put(10000, 1);
                restante = monto - 100000;
            }

            for (int d : DENOMINACIONES) {
                if (entrega.get(d) > billetes.get(d)) {
                    return null; 
                }
            }
        }
    
        for (int d : DENOMINACIONES) {
            int necesarios = (int) (restante / d);                
            int disponibles = billetes.get(d) - entrega.get(d);   
            int usar = Math.min(necesarios, disponibles);         
 
            entrega.put(d, entrega.get(d) + usar);
            restante -= (long) usar * d;
        }
 
        if (restante != 0) {
            return null;
        }
        return entrega;
    }
 
    public void descontar(HashMap<Integer, Integer> entrega) {
        for (int d : DENOMINACIONES) {
            billetes.put(d, billetes.get(d) - entrega.get(d));
        }
    }
}