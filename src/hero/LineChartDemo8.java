package hero;

import java.awt.*;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.util.*;

public class LineChartDemo8 extends JDialog {
	public LineChartDemo8() {
		JPanel jPanel = createDemoPanel();
		jPanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(jPanel);
	}

	private static CategoryDataset createDataset() {
		DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
		defaultCategoryDataset.addValue(0.0D, "Series 1", "Category 1");
		defaultCategoryDataset.addValue(2.0D, "Series 1", "Category 2");
		defaultCategoryDataset.addValue(1.0D, "Series 1", "Category 3");
		defaultCategoryDataset.addValue(3.0D, "Series 1", "Category 4");
		defaultCategoryDataset.addValue(5.0D, "Series 1", "Category 5");
		defaultCategoryDataset.addValue(2.0D, "Series 2", "Category 1");
		defaultCategoryDataset.addValue(4.0D, "Series 2", "Category 2");
		defaultCategoryDataset.addValue(4.0D, "Series 2", "Category 3");
		defaultCategoryDataset.addValue(5.0D, "Series 2", "Category 4");
		defaultCategoryDataset.addValue(2.0D, "Series 2", "Category 5");
		defaultCategoryDataset.addValue(1.0D, "Series 3", "Category 1");
		defaultCategoryDataset.addValue(3.0D, "Series 3", "Category 2");
		defaultCategoryDataset.addValue(5.0D, "Series 3", "Category 3");
		defaultCategoryDataset.addValue(2.0D, "Series 3", "Category 4");
		defaultCategoryDataset.addValue(0.0D, "Series 3", "Category 5");
		return defaultCategoryDataset;
	}

	private static JFreeChart createChart(CategoryDataset paramCategoryDataset) {
		JFreeChart jFreeChart = ChartFactory.createLineChart("Line Chart Demo 8", "Category", "Count",
				paramCategoryDataset, PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot categoryPlot = (CategoryPlot) jFreeChart.getPlot();
		SymbolAxis symbolAxis = new SymbolAxis("Average Backroll", new String[] { "A", "B", "C", "D", "E", "F" });
		categoryPlot.setRangeAxis(symbolAxis);
		ChartUtilities.applyCurrentTheme(jFreeChart);
		LineAndShapeRenderer lineAndShapeRenderer = (LineAndShapeRenderer) categoryPlot.getRenderer();
		lineAndShapeRenderer.setSeriesShapesVisible(0, true);
		lineAndShapeRenderer.setSeriesShapesVisible(1, false);
		lineAndShapeRenderer.setSeriesShapesVisible(2, true);
		lineAndShapeRenderer.setSeriesLinesVisible(2, false);
		lineAndShapeRenderer.setSeriesShape(2, ShapeUtilities.createDiamond(4.0F));
		lineAndShapeRenderer.setDrawOutlines(true);
		lineAndShapeRenderer.setUseFillPaint(true);
//    lineAndShapeRenderer.setBaseFillPaint(Color.white);

		return jFreeChart;
	}

	public static JPanel createDemoPanel() {
		JFreeChart jFreeChart = createChart(createDataset());
		return new ChartPanel(jFreeChart);
	}

	public static void main(String[] paramArrayOfString) {
		LineChartDemo8 lineChartDemo8 = new LineChartDemo8();
		lineChartDemo8.pack();
		lineChartDemo8.setVisible(true);
	}
}