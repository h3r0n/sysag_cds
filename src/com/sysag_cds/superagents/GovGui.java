package com.sysag_cds.superagents;

import com.sysag_cds.utility.Decree;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import javafx.scene.control.CheckBox;

import java.awt.*;
import java.util.Hashtable;
import javax.swing.*;
import static javax.swing.GroupLayout.Alignment.*;

public class GovGui {
    GuiAgent myAgent;
    JLabel maskLabel = new JLabel("Obbligo DPI: ");
    JLabel nonEssentialLabel = new JLabel("Attività non essenziali:");
    JLabel parkLabel = new JLabel("Parchi:");
    JLabel eventLabel = new JLabel("Eventi pubblici:");
    JLabel densityLabel = new JLabel("Distanziamento: ");
    JLabel travelLabel = new JLabel("Limite spostamenti: ");
    JLabel walkLabel = new JLabel("Distanza passeggiata: ");

    String[] maskOptions = {"Mai", "Al chiuso", "Sempre"};
    JComboBox<String> maskCombo = new JComboBox<>(maskOptions);
    JCheckBox nonEssentialBox = new JCheckBox("Aperti", true);
    JCheckBox parkBox = new JCheckBox("Aperti", true);
    JCheckBox eventBox = new JCheckBox("Consentiti", true);
    JSlider densitySlider =  new JSlider (JSlider.HORIZONTAL, 0, 100, 100);
    JTextField travelInput = new JTextField("100");
    JTextField walkInput = new JTextField("10");
    JButton sendButton = new JButton("Emana decreto");

    JFrame frame = new JFrame("Governo");

    public GovGui(GuiAgent a) {
        myAgent = a;
        buildGui();
    }

    private void buildGui() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        sendButton.addActionListener(e -> myAgent.postGuiEvent(createEvent()));

        // slider ticks
        densitySlider.setMajorTickSpacing(10);
        densitySlider.setPaintTicks(true);
        Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0.0") );
        labelTable.put(100, new JLabel("1.0") );
        densitySlider.setLabelTable( labelTable );
        densitySlider.setPaintLabels(true);

        JPanel decreePane = new JPanel();
        GroupLayout decreeLayout = new GroupLayout(decreePane);
        decreePane.setLayout(decreeLayout);
        decreeLayout.setAutoCreateGaps(true);
        decreeLayout.setAutoCreateContainerGaps(true);

        decreeLayout.setHorizontalGroup(decreeLayout.createSequentialGroup()
                .addGroup(decreeLayout.createParallelGroup(LEADING)
                        .addComponent(maskLabel)
                        .addComponent(nonEssentialLabel)
                        .addComponent(parkLabel)
                        .addComponent(eventLabel)
                        .addComponent(densityLabel)
                        .addComponent(travelLabel)
                        .addComponent(walkLabel)
                )
                .addGroup(decreeLayout.createParallelGroup(LEADING)
                        .addComponent(maskCombo)
                        .addComponent(nonEssentialBox)
                        .addComponent(parkBox)
                        .addComponent(eventBox)
                        .addComponent(densitySlider)
                        .addComponent(travelInput)
                        .addComponent(walkInput)
                )
        );

        decreeLayout.setVerticalGroup(decreeLayout.createSequentialGroup()
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(maskLabel)
                        .addComponent(maskCombo)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(nonEssentialLabel)
                        .addComponent(nonEssentialBox)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(parkLabel)
                        .addComponent(parkBox)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(eventLabel)
                        .addComponent(eventBox)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(densityLabel)
                        .addComponent(densitySlider)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(travelLabel)
                        .addComponent(travelInput)
                )
                .addGroup(decreeLayout.createParallelGroup(CENTER)
                        .addComponent(walkLabel)
                        .addComponent(walkInput)
                )
        );

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(sendButton);

        frame.getContentPane().add(new JLabel("Nuovo decreto:"), BorderLayout.PAGE_START);
        frame.getContentPane().add(decreePane, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);

        frame.pack();
        frame.setVisible(true);
    }

    private GuiEvent createEvent() {
        GuiEvent e = new GuiEvent(this,0);
        e.addParameter(maskCombo.getSelectedIndex());
        e.addParameter(nonEssentialBox.isSelected());
        e.addParameter(parkBox.isSelected());
        e.addParameter(eventBox.isSelected());
        e.addParameter((double) densitySlider.getValue()/100);
        try {
            e.addParameter(Integer.parseInt(travelInput.getText()));
        } catch (NumberFormatException ex) {
            e.addParameter(100);
        }
        try {
            e.addParameter(Integer.parseInt(walkInput.getText()));
        } catch (NumberFormatException ex) {
            e.addParameter(10);
        }
        return e;
    }
}