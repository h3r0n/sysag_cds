package com.sysag_cds.superagents;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.None;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;


public class StatGui {

    JFrame frame = new JFrame("Statistics");
    XYChart chart;
    List<Double> datax = new LinkedList<>();
    List<Integer> datatp = new LinkedList<>();
    List<Integer> datacp = new LinkedList<>();
    List<Integer> datar = new LinkedList<>();
    List<Integer> datad = new LinkedList<>();
    JPanel chartPanel;

    StatGui() {
        buildGui();
    }

    private void buildGui() {
        datax.add(0.0);
        datatp.add(0);
        datacp.add(0);
        datar.add(0);
        datad.add(0);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chart = new XYChartBuilder().width(600).height(400).title("Andamento contagio").xAxisTitle("Giorni").yAxisTitle("Casi").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setChartBackgroundColor(new Color(247,246,242));
        //chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);

        chart.addSeries("Totale positivi", datax, datatp);
        chart.getSeriesMap().get("Totale positivi").setLineColor(new Color(239,78,99));

        chart.addSeries("Attualmente positivi", datax, datacp);
        chart.getSeriesMap().get("Attualmente positivi").setLineColor(new Color(245,149,143));

        chart.addSeries("Guariti", datax, datar);
        chart.getSeriesMap().get("Guariti").setLineColor(new Color(24,178,144));

        chart.addSeries("Morti", datax, datad);
        chart.getSeriesMap().get("Morti").setLineColor(new Color(10,10,10));

        for (XYSeries s : chart.getSeriesMap().values())
            s.setMarker(SeriesMarkers.NONE);

        chartPanel = new XChartPanel<>(chart);
        frame.add(chartPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    void addData(double x, int tp, int cp, int r, int d) {
        datax.add(x);
        datatp.add(tp);
        datacp.add(cp);
        datar.add(r);
        datad.add(d);
        update();
    }

    void update() {
        chart.updateXYSeries("Totale positivi", datax,datatp,null);
        chart.updateXYSeries("Attualmente positivi", datax, datacp,null);
        chart.updateXYSeries("Guariti", datax, datar,null);
        chart.updateXYSeries("Morti", datax, datad,null);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
}