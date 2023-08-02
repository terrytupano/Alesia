package hero;

import java.awt.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

import core.*;
import datasource.*;

import hero.ozsoft.*;

public class SingeVariableSimulationLineChart extends JDialog {

	public SingeVariableSimulationLineChart(String resultName) {
		super(Alesia.getMainFrame());
		JFreeChart jFreeChart = createChart(createDataset(resultName));
		JPanel jPanel = new ChartPanel(jFreeChart);
		setContentPane(jPanel);
	}

	private static XYDataset createDataset(String resultName) {
		XYSeriesCollection xYSeriesCollection = new XYSeriesCollection();
		Alesia.openDB();
		LazyList<TrooperParameter> troopers = TrooperParameter.findAll();
		for (TrooperParameter client : troopers) {
			String pName = client.getString("trooper");
			LazyList<SimulationResult> statistics = SimulationResult.find("name = ? AND trooper = ?", resultName,
					pName);
			// Retrieve the first element of the statistical series and append the additional value field
			String av = statistics.size() > 0 ? statistics.get(0).getString("variables") : "";
			XYSeries xYSeries = new XYSeries(pName + "\n(" + av + ")");
			xYSeriesCollection.addSeries(xYSeries);
			for (SimulationResult sts : statistics) {
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