package cajeroof;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;


public class cajeroGUI extends JFrame {

   
    private final logica cajero = new logica();   // se deja el punto de la logica para ir analizando

   
    private final JTextArea pantalla = new JTextArea(9, 28);
    private DefaultTableModel modeloTabla;
    private String entrada = "";
    private String mensaje = "BIENVENIDO\nDigite el monto a retirar.\n(Mínimo $10.000 - Máximo $1.000.000)";   //texto de resultados

    
    public cajeroGUI() {
        setTitle("Cajero Electrónico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));   // ventana dividida: arriba / centro / abajo
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearPantalla(), BorderLayout.NORTH);   // arriba: pantalla
        add(crearBotones(), BorderLayout.CENTER);   // centro: botones
        add(crearTabla(), BorderLayout.SOUTH);      // abajo: tabla de billetes

        actualizarTabla();     // llena la tabla con los valores iniciales
        actualizarPantalla();  // muestra el mensaje de bienvenida

        pack();                        // ajusta el tamaño a los componentes
        setLocationRelativeTo(null);   // centra la ventana en el monitor
    }

  // se va creando la interfaz
    private JScrollPane crearPantalla() {
        pantalla.setEditable(false);                       // el usuario NO escribe aquí con el teclado
        pantalla.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        pantalla.setBackground(new Color(20, 30, 40));
        pantalla.setForeground(new Color(80, 255, 120));
        pantalla.setLineWrap(true);
        pantalla.setWrapStyleWord(true);
        pantalla.setMargin(new java.awt.Insets(8, 8, 8, 8));
        return new JScrollPane(pantalla);
    }

 
    private JPanel crearBotones() {
       
        JPanel teclado = new JPanel(new GridLayout(4, 3, 6, 6));
        for (int i = 1; i <= 9; i++) {
            final String numero = String.valueOf(i);
            teclado.add(crearBoton(numero, e -> escribirNumero(numero)));
        }

       
        teclado.add(crearBoton("←", e -> retroceder()));              // Backspace (punto 3)
        teclado.add(crearBoton("0", e -> escribirNumero("0")));
        teclado.add(crearBoton("Limpiar", e -> limpiar()));           // Limpiar (punto 3)

      
        JButton botonRetirar = crearBoton("RETIRAR", e -> retirar());
        botonRetirar.setBackground(new Color(40, 140, 60));
        botonRetirar.setForeground(Color.WHITE);
        botonRetirar.setOpaque(true);
        botonRetirar.setPreferredSize(new Dimension(100, 45));

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(teclado, BorderLayout.CENTER);
        panel.add(botonRetirar, BorderLayout.SOUTH);
        return panel;
    }

    private JButton crearBoton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 18));
        boton.setFocusPainted(false);
        boton.addActionListener(accion);   // qué hacer cuando lo presionen
        return boton;
    }

    
    private JScrollPane crearTabla() {
        String[] columnas = {"Denominación", "Billetes disponibles", "Dinero total"};

        // Modelo de tabla en el que las celdas NO se pueden editar
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(400, 170));
        return scroll;
    }

  
    private void escribirNumero(String numero) {
        if (entrada.length() >= 9) {
            return; // límite de dígitos para evitar números gigantes
        }
        if (entrada.isEmpty() && numero.equals("0")) {
            return; // evita ceros a la izquierda
        }
        entrada += numero;
        mensaje = "Digite el monto a retirar.";
        actualizarPantalla();
    }


    private void retroceder() {
        if (!entrada.isEmpty()) {
            entrada = entrada.substring(0, entrada.length() - 1);
        }
        actualizarPantalla();
    }

    
    private void limpiar() {
        entrada = "";
        mensaje = "Digite el monto a retirar.";
        actualizarPantalla();
    }


    private void retirar() {

      
        if (entrada.isEmpty()) {
            mostrarError("Debe ingresar un monto.");
            return;
        }

        long monto = Long.parseLong(entrada); 

       
        if (monto % 10 != 0) {
            mostrarError("El monto debe ser múltiplo de 10.");
            return;
        }

        // Validación 3: mínimo 10.000 y máximo 1.000.000 (punto 8)
        if (monto < 10000 || monto > 1000000) {
            mostrarError("El retiro mínimo es $10.000 y el máximo $1.000.000.");
            return;
        }

        // Validación 4: los billetes son de 10.000 en adelante, así que
        // el monto debe poder armarse con ellos
        if (monto % 10000 != 0) {
            mostrarError("Con billetes de 10.000, 20.000, 50.000 y 100.000\n"
                    + "solo se pueden retirar múltiplos de $10.000.");
            return;
        }

        // Validación 5: que el cajero tenga saldo suficiente (punto 12)
        if (monto > cajero.getSaldoTotal()) {
            mostrarError("Saldo insuficiente en el cajero.\nDisponible: $"
                    + formato(cajero.getSaldoTotal()));
            return;
        }

        // Calcular qué billetes entregar (puntos 10 y 11)
        HashMap<Integer, Integer> entrega = cajero.calcularEntrega(monto);
        if (entrega == null) {
            mostrarError("No hay billetes suficientes de cada\n"
                    + "denominación para entregar ese monto.");
            return;
        }

        // Todo correcto: se descuentan los billetes y se actualiza la tabla (punto 5)
        cajero.descontar(entrega);
        actualizarTabla();

        // Armar el mensaje de resultado (punto 9)
        StringBuilder texto = new StringBuilder();
        texto.append("RETIRO SATISFACTORIO\n");
        texto.append("Monto retirado: $").append(formato(monto)).append("\n\n");
        texto.append("Billetes entregados:\n");
        for (int d : logica.DENOMINACIONES) {
            int cantidad = entrega.get(d);
            if (cantidad > 0) {
                texto.append("  ").append(cantidad).append(" x $").append(formato(d)).append("\n");
            }
        }
        mensaje = texto.toString();
        entrada = "";
        actualizarPantalla();
    }

 
    private void mostrarError(String error) {
        mensaje = "ERROR\n" + error + "\n\nIngrese el monto nuevamente.";
        entrada = "";
        actualizarPantalla();
    }

    private void actualizarPantalla() {
        String monto = entrada.isEmpty() ? "" : "$" + formato(Long.parseLong(entrada));
        pantalla.setText(mensaje + "\n\n-----------------------------\nMonto: " + monto);
    }

    private void actualizarTabla() {
        modeloTabla.setRowCount(0); // borra todas las filas

        for (int d : logica.DENOMINACIONES) {
            int cantidad = cajero.getCantidad(d);
            modeloTabla.addRow(new Object[]{
                "$" + formato(d),
                cantidad,
                "$" + formato((long) d * cantidad)
            });
        }
       
        modeloTabla.addRow(new Object[]{"TOTAL", "", "$" + formato(cajero.getSaldoTotal())});
    }

    private String formato(long numero) {
        return String.format(Locale.forLanguageTag("es-CO"), "%,d", numero);
    }

    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(() -> new cajeroGUI().setVisible(true));
    }
}