package example.nio;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Main extends JFrame{

    private FlightBoard flightPanel = new FlightBoard();
    private AirplaneList<Airplane> lista = new AirplaneList<>();
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private JButton buttonAdd = new JButton("Add plane");
    private JButton buttonSave = new JButton("Save");
    private JButton buttonLoad = new JButton("Load");
    private JButton buttonExit = new JButton("Exit");

    public Main(){
        initComponents();
    }

    public static void main(String[] args) {
	    new Main().setVisible(true);
    }

    private void initComponents(){
        this.setTitle("Loty");
        this.setBounds(250, 300, 600, 600);

        //airplane panel
        flightPanel.setBackground(Color.GRAY);
        flightPanel.setPreferredSize(new Dimension(600,600));
        this.getContentPane().add(flightPanel, BorderLayout.CENTER);

        //listing airplanes
        lista.setPreferredSize(new Dimension(400, 600));
        this.getContentPane().add(lista, BorderLayout.WEST);

        //add airplane
        buttonAdd.addActionListener(e -> flightPanel.addAirplane());
        buttonPanel.add(buttonAdd);

        //button panel
        buttonSave.addActionListener(e -> {
            System.out.println("Saving planes");
            flightPanel.saveComponents();
        });
        buttonPanel.add(buttonSave);

        buttonLoad.addActionListener(e -> {
            System.out.println("Loading planes");
            flightPanel.loadComponents();
        });
        buttonPanel.add(buttonLoad);

        buttonExit.addActionListener(e -> {
            buttonAdd.setEnabled(false);
            buttonSave.setEnabled(false);
            buttonLoad.setEnabled(false);
            buttonExit.setEnabled(false);
            flightPanel.waitAndExit();
//            System.exit(0);
        });
        buttonPanel.add(buttonExit);

        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();

    }

    public class AirplaneList<N> extends JList<Airplane> implements Runnable {

        DynamicList dynamicModel = new DynamicList();
        Thread thread = new Thread(this);

        AirplaneList(){
            this.setModel(dynamicModel);
            thread.start();
        }

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(2);
                    dynamicModel.update();
                    this.repaint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        class DynamicList extends AbstractListModel<Airplane>{
            ArrayList<Airplane> arrayList = flightPanel.getAirplanesList();

            @Override
            public int getSize() {
                return arrayList.size();
            }

            @Override
            public Airplane getElementAt(int index) {
                return arrayList.get(index);
            }

            void update(){
                this.fireContentsChanged(this, 0, arrayList.size() - 1);
            }

        }
    }

}
