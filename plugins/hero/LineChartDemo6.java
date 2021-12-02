package plugins.hero;

import java.awt.*;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

public class LineChartDemo6 extends JDialog {
	public LineChartDemo6() {
		JPanel jPanel = createDemoPanel();
		jPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(jPanel);
	}

	private static XYDataset createDataset() {
		XYSeries xYSeries1 = new XYSeries("First");
		xYSeries1.add(1.0D, 1.0D);
		xYSeries1.add(2.0D, 4.0D);
		xYSeries1.add(3.0D, 3.0D);
		xYSeries1.add(4.0D, 5.0D);
		xYSeries1.add(5.0D, 5.0D);
		xYSeries1.add(6.0D, 7.0D);
		xYSeries1.add(7.0D, 7.0D);
		xYSeries1.add(8.0D, 8.0D);
		XYSeries xYSeries2 = new XYSeries("Second");
		xYSeries2.add(1.0D, 5.0D);
		xYSeries2.add(2.0D, 7.0D);
		xYSeries2.add(3.0D, 6.0D);
		xYSeries2.add(4.0D, 8.0D);
		xYSeries2.add(5.0D, 4.0D);
		xYSeries2.add(6.0D, 4.0D);
		xYSeries2.add(7.0D, 2.0D);
		xYSeries2.add(8.0D, 1.0D);
		XYSeries xYSeries3 = new XYSeries("Third");
		xYSeries3.add(3.0D, 4.0D);
		xYSeries3.add(4.0D, 3.0D);
		xYSeries3.add(5.0D, 2.0D);
		xYSeries3.add(6.0D, 3.0D);
		xYSeries3.add(7.0D, 6.0D);
		xYSeries3.add(8.0D, 3.0D);
		xYSeries3.add(9.0D, 4.0D);
		xYSeries3.add(10.0D, 3.0D);
		XYSeriesCollection xYSeriesCollection = new XYSeriesCollection();
		xYSeriesCollection.addSeries(xYSeries1);
		xYSeriesCollection.addSeries(xYSeries2);
		xYSeriesCollection.addSeries(xYSeries3);
		return xYSeriesCollection;
	}

	private static JFreeChart createChart(XYDataset paramXYDataset) {
		JFreeChart jFreeChart = ChartFactory.createXYLineChart("Line Chart Demo 6", "Simulated Hands",
				"Average Banckroll", paramXYDataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xYPlot = (XYPlot) jFreeChart.getPlot();
		xYPlot.setBackgroundPaint(null);
		XYLineAndShapeRenderer xYLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xYLineAndShapeRenderer.setSeriesLinesVisible(0, false);
		xYLineAndShapeRenderer.setSeriesShapesVisible(1, false);
		xYLineAndShapeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		xYPlot.setRenderer(xYLineAndShapeRenderer);
		// NumberAxis numberAxis = new NumberAxis();
		// TickUnitSource source = NumberAxis.createIntegerTickUnits();
		// numberAxis.setStandardTickUnits((numberAxis = (NumberAxis)xYPlot.getRangeAxis()).createIntegerTickUnits());
		return jFreeChart;
	}

	public static JPanel createDemoPanel() {
		JFreeChart jFreeChart = createChart(createDataset());
		return new ChartPanel(jFreeChart);
	}

	public static void main(String[] paramArrayOfString) {
		LineChartDemo6 lineChartDemo8 = new LineChartDemo6();
		lineChartDemo8.pack();
		lineChartDemo8.setVisible(true);
	}
}