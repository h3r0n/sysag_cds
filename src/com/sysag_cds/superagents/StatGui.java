package com.sysag_cds.superagents;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;

import javax.swing.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;


public class StatGui {

    JFrame frame = new JFrame("Statistics");
    XYChart chart;
    List<Integer> datax = new LinkedList<>();
    List<Integer> datay = new LinkedList<>();
    JPanel chartPanel;

    StatGui() {
        buildGui();
    }

    private void buildGui() {
        datax.add(0);
        datay.add(0);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chart = new XYChartBuilder().width(600).height(400).title("Andamento contagio").xAxisTitle("Tempo").yAxisTitle("Numero").build();
        chart.addSeries("Infected", datax, datay);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);

        chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    void addData(int x, int y) {
        datax.add(x);
        datay.add(y);
        update();
    }

    void update() {
        chart.updateXYSeries("Infected", datax,datay,null);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}