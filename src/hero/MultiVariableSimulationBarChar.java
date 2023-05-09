package hero;

import java.awt.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;

import core.*;
import datasource.*;


public class MultiVariableSimulationBarChar extends JDialog {

	public MultiVariableSimulationBarChar(String resutlName) {
		super(Alesia.getInstance().mainFrame);
		JFreeChart jFreeChart = createChart(createDataset(resutlName));
		JPanel jPanel = new ChartPanel(jFreeChart);
		setContentPane(jPanel);
	}

	private static DefaultCategoryDataset createDataset(String resultName) {
		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		Alesia.getInstance().openDB("hero");
		// AND trooper != 'Hero' avoid the marker (0 element inserted in simulation)
		LazyList<SimulationResult> statistics = SimulationResult.find("name = ? AND trooper != 'Hero'", resultName).orderBy("ratio");
		for (SimulationResult result : statistics) {
			categoryDataset.addValue(result.getInteger("wins"), "", result.getString("multiAditionalValues"));
		}
		return categoryDataset;
	}

	private static JFreeChart createChart(CategoryDataset paramCategoryDataset) {
		JFreeChart chart = ChartFactory.createBarChart(null, null, null, paramCategoryDataset);
		CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
		categoryPlot.setNoDataMessage("NO DATA!");
		categoryPlot.setDomainGridlinesVisible(true);
		categoryPlot.setRangeCrosshairVisible(true);
		categoryPlot.setRangeCrosshairPaint(Color.blue);

		Plot plot = chart.getPlot();
		// xYPlot.setDomainPannable(true);
		// xYPlot.setRangePannable(true);
		plot.setBackgroundPaint(Color.white);
		plot.setForegroundAlpha(0.85F);

		CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
		categoryAxis
				.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.5235987755982988D));

		chart.getLegend().setVisible(false);
		chart.setBackgroundPaint(Color.WHITE);

		// StandardChartTheme sct = new StandardChartTheme("Legacy");
		// sct.apply(chart);
		// ChartUtils.applyCurrentTheme(jFreeChart);
		return chart;
	}
}
