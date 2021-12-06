package plugins.hero;

import java.awt.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.ozsoft.*;

public class LineChartDemo6 extends JDialog {

	public LineChartDemo6() {
		super(Alesia.getInstance().mainFrame);
		JFreeChart jFreeChart = createChart(createDataset());
		JPanel jPanel = new ChartPanel(jFreeChart);
		jPanel.setPreferredSize(new Dimension(1000, 600));
		setContentPane(jPanel);
	}

	private static XYDataset createDataset() {
		XYSeriesCollection xYSeriesCollection = new XYSeriesCollection();
		Alesia.getInstance().openDB("hero");
		LazyList<SimulatorClient> clients = SimulatorClient.findAll();
		for (SimulatorClient client : clients) {
			String pName = client.getString("playerName");
			XYSeries xYSeries = new XYSeries(pName);
			LazyList<SimulatorStatistic> statistics = SimulatorStatistic.find("name = ? AND player = ?", "Bankroll",
					pName);
			xYSeriesCollection.addSeries(xYSeries);
			for (SimulatorStatistic sts : statistics) {
				xYSeries.add(sts.getInteger("hands"), sts.getDouble("wins"));
			}
		}
		return xYSeriesCollection;
	}

	private static JFreeChart createChart(XYDataset paramXYDataset) {
		JFreeChart jFreeChart = ChartFactory.createXYLineChart("Average Banckroll History", "Simulated Hands",
				"Average Banckroll", paramXYDataset, PlotOrientation.VERTICAL, true, true, false);
		XYPlot xYPlot = (XYPlot) jFreeChart.getPlot();
		xYPlot.setRangeZeroBaselineVisible(true);
		xYPlot.setBackgroundPaint(null);
		XYLineAndShapeRenderer xYLineAndShapeRenderer = (XYLineAndShapeRenderer) xYPlot.getRenderer();
		for (int i = 0; i < Table.CAPACITY; i++) {
			xYLineAndShapeRenderer.setSeriesStroke(i, new BasicStroke(2.0F));
			xYLineAndShapeRenderer.setSeriesLinesVisible(i, true);
			xYLineAndShapeRenderer.setSeriesShapesVisible(i, false);
			// xYLineAndShapeRenderer.setSeriesLinesVisible(0, false);
			// xYLineAndShapeRenderer.setSeriesShapesVisible(1, false);
			// xYLineAndShapeRenderer.setBaseShapesVisible(true);
			// xYLineAndShapeRenderer.setDrawOutlines(true);
			// xYLineAndShapeRenderer.setUseFillPaint(true);
			// xYLineAndShapeRenderer.setBaseFillPaint(Color.white);
		}
		xYLineAndShapeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		xYPlot.setRenderer(xYLineAndShapeRenderer);
		// NumberAxis numberAxis = new NumberAxis();
		// TickUnitSource source = NumberAxis.createIntegerTickUnits();
		// numberAxis.setStandardTickUnits((numberAxis = (NumberAxis)xYPlot.getRangeAxis()).createIntegerTickUnits());
		return jFreeChart;
	}
}