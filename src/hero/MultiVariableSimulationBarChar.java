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
import tech.tablesaw.aggregate.*;
import tech.tablesaw.api.*;
import tech.tablesaw.io.json.*;

public class MultiVariableSimulationBarChar extends JDialog {

	public MultiVariableSimulationBarChar(String simulationName) {
		super(Alesia.getMainFrame());
		JFreeChart jFreeChart = createChart(createDataset(simulationName));
		JPanel jPanel = new ChartPanel(jFreeChart);
		setContentPane(jPanel);
	}

	private static DefaultCategoryDataset createDataset(String resultName) {
		SimulationParameters parameters = SimulationParameters.findFirst("simulationName = ?", resultName);
		LazyList<SimulationResult> statistics = parameters.get(SimulationResult.class, "tableId = ?", -1);
		String json = statistics.toJson(false);
		JsonReader reader = new JsonReader();
		Table table = reader.read(JsonReadOptions.builderFromString(json).build());
		table = table.summarize("hands", "wins", "ratio", AggregateFunctions.sum).by("variables");
		table = table.sortOn("Sum [ratio]");
//		System.out.println(table);

		// only the # best results
		table = table.rowCount() > 20 ? table.last(20) : table;
		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		for (int i = 0; i < table.rowCount(); i++) {
			Row row = table.row(i);
			categoryDataset.addValue(row.getDouble("Sum [ratio]"), "", row.getString("variables"));
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
		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(1D));

		chart.getLegend().setVisible(false);
		chart.setBackgroundPaint(Color.WHITE);

		// StandardChartTheme sct = new StandardChartTheme("Legacy");
		// sct.apply(chart);
		// ChartUtils.applyCurrentTheme(jFreeChart);
		return chart;
	}
}
