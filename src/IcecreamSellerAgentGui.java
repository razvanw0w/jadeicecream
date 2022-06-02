import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class IcecreamSellerAgentGui extends JFrame {
    private IcecreamSellerAgent icecreamSellerAgent;

    private JTextField flavourField;
    private JTextField priceField;
    private JTextField quantityField;

    IcecreamSellerAgentGui(IcecreamSellerAgent agent) {
        super("Agent " + agent.getLocalName());
        icecreamSellerAgent = agent;
        addGuiWidgets();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                icecreamSellerAgent.doDelete();
            }
        });
    }

    private void addGuiWidgets() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.add(new JLabel("Flavour: "));
        flavourField = new JTextField(15);
        panel.add(flavourField);
        panel.add(new JLabel("Quantity: "));
        quantityField = new JTextField(5);
        panel.add(quantityField);
        panel.add(new JLabel("Price:"));
        priceField = new JTextField(5);
        panel.add(priceField);
        getContentPane().add(panel, BorderLayout.CENTER);

        JButton addButton = new JButton("Update flavour details");
        addButton.addActionListener(event -> {
            try {
                String flavour = flavourField.getText();
                int price = Integer.parseInt(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                icecreamSellerAgent.updateStock(flavour, quantity, price);
                flavourField.setText("");
                quantityField.setText("");
                priceField.setText("");
            } catch (Exception e) {

            }
        });
        panel = new JPanel();
        panel.add(addButton);
        getContentPane().add(panel, BorderLayout.SOUTH);
    }

    public void showGui() {
        pack();
        setSize(400, 200);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.width, screenSize.height + 50);
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
