package plugins.hero.pokerEnlighter.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

import javax.swing.*;

import org.javafling.pokerenlighter.combination.*;
import org.javafling.pokerenlighter.simulation.*;
import org.jfree.chart.*;

import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;

/**
 *
 * @author Radu Murzea
 * 
 * @version 1.1
 */
public final class ResultChartDialog extends JDialog {
	private ResultChartDialog rcd;

	private static final String XAxis = "Players";
	private static final String YAxis = "Percentage";

	private JFreeChart chart;
	private ChartPanel chartPanel;
	private String title;
	private SimulationFinalResult result;

	public ResultChartDialog(JFrame parent, String title, SimulationFinalResult result) {
		super(parent, title, true);

		this.title = title;

		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		this.result = result;

		OptionsContainer options = OptionsContainer.getOptionsContainer();
		buildChart(options.getGraph3D(), options.getGraphStacked());
		add(chartPanel, BorderLayout.CENTER);

		add(createButtonPanel(), BorderLayout.SOUTH);

		pack();

		rcd = this;
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton exportButton = new JButton("Export as PNG");
		exportButton.addActionListener(new ExportListener());

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		panel.add(exportButton);
		panel.add(closeButton);

		return panel;
	}

	private class ExportListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog(rcd);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					ChartUtils.saveChartAsPNG(fc.getSelectedFile(), chart, chartPanel.getWidth(),
							chartPanel.getHeight());
				} catch (IOException ex) {
					GUIUtilities.showErrorDialog(rcd, "Could not save the image: " + ex.getMessage(), "Save Error");
				}
			}
		}
	}

	// set width
	// for 6 players: 7 x 30 pixels space zone and 6 x 130 pixels bar zone
	// so formula will be:
	// width = 130 x nr_players + 30 x (nr_players + 1)
	// additionally, 60 pixels should be added for accomodating metadata on the side
	//
	// final formula: 160 x nr_players + 90
	private int getDynamicWidth() {
		return 160 * result.getNrOfPlayers() + 90;
	}

	// for now, there is no formula for the height, its fixed.
	// but it's provided by a method in case a formula will be implemented in the future
	private int getDynamicHeight() {
		return 450;
	}

	private void buildChart(boolean is3D, boolean isStacked) {
		DefaultCategoryDataset dataset = createDataset();

		if (isStacked) {
			createStackedChart(dataset);
		} else {
			createClassicChart(dataset);
		}

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		setBarColors(plot);

		if (OptionsContainer.getOptionsContainer().getShowGraphLabels()) {
			setBarLabels(plot);
		}

		chartPanel = new ChartPanel(chart);

		chartPanel.setPreferredSize(new Dimension(getDynamicWidth(), getDynamicHeight()));
	}

	private void createClassicChart(CategoryDataset dataset) {
		chart = ChartFactory.createBarChart(title, XAxis, YAxis, dataset, PlotOrientation.VERTICAL, // orientation
																									// (vert/horiz)
				true, // whether or not legend is required
				true, // generate tooltips ?
				false // generate URLs ?
		);
	}

	private void createStackedChart(CategoryDataset dataset) {
		chart = ChartFactory.createStackedBarChart(title, XAxis, YAxis, dataset, PlotOrientation.VERTICAL, // orientation
																											// (vert/horiz)
				true, // whether or not legend is required
				true, // generate tooltips ?
				false // generate URLs ?
		);

		CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
		StackedBarRenderer stackedbarrenderer = (StackedBarRenderer) categoryplot.getRenderer();
		stackedbarrenderer.setDrawBarOutline(false);
//		stackedbarrenderer.setBaseItemLabelsVisible(true);
//		stackedbarrenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
	}

	// sets the colors of the bars according to the values found in the options file
	private void setBarColors(CategoryPlot plot) {
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		OptionsContainer options = OptionsContainer.getOptionsContainer();

		Color winsColor = new Color(options.getWinsRedValue(), options.getWinsGreenValue(), options.getWinsBlueValue());
		Color losesColor = new Color(options.getLosesRedValue(), options.getLosesGreenValue(),
				options.getLosesBlueValue());
		Color tieColor = new Color(options.getTiesRedValue(), options.getTiesGreenValue(), options.getTiesBlueValue());

		renderer.setSeriesPaint(0, winsColor);
		renderer.setSeriesPaint(1, losesColor);
		renderer.setSeriesPaint(2, tieColor);
	}

	// display the values of the bars as a label on top of them
	private void setBarLabels(CategoryPlot plot) {
		CategoryItemRenderer categoryRenderer = plot.getRenderer();

		CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator("{2} %",
				new DecimalFormat("0.0"));

		categoryRenderer.setSeriesItemLabelGenerator(0, generator);
		categoryRenderer.setSeriesItemLabelGenerator(1, generator);
		categoryRenderer.setSeriesItemLabelGenerator(2, generator);

		categoryRenderer.setSeriesItemLabelsVisible(0, true);
		categoryRenderer.setSeriesItemLabelsVisible(1, true);
		categoryRenderer.setSeriesItemLabelsVisible(2, true);
	}

	private DefaultCategoryDataset createDataset() {
		DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();

		DecimalFormat formatter = new DecimalFormat();
		formatter.setMaximumFractionDigits(1);

		for (int i = 0; i < result.getNrOfPlayers(); i++) {
			StringBuilder playerLabel = new StringBuilder("Player ");
			playerLabel.append(Integer.toString(i + 1));
			playerLabel.append(" (");

			HandType handType = result.getPlayer(i).getHandType();
			if (handType == HandType.EXACTCARDS) {
				Card[] playerCards = result.getPlayer(i).getCards();

				for (Card playerCard : playerCards) {
					playerLabel.append(playerCard.toString());
				}
			} else if (handType == HandType.RANDOM) {
				playerLabel.append("Random");
			} else if (handType == HandType.RANGE) {
				playerLabel.append(Integer.toString(result.getPlayer(i).getRange().getPercentage()));
				playerLabel.append("% Range");
			}

			playerLabel.append(")");

			categoryDataSet.addValue(result.getWinPercentage(i), "Wins",
					playerLabel.toString());

			categoryDataSet.addValue(result.getLosePercentage(i), "Loses",
					playerLabel.toString());

			categoryDataSet.addValue(result.getTiePercentage(i), "Ties",
					playerLabel.toString());
//
//			categoryDataSet.addValue(Double.parseDouble(formatter.format(result.getWinPercentage(i))), "Wins",
//					playerLabel.toString());
//
//			categoryDataSet.addValue(Double.parseDouble(formatter.format(result.getLosePercentage(i))), "Loses",
//					playerLabel.toString());
//
//			categoryDataSet.addValue(Double.parseDouble(formatter.format(result.getTiePercentage(i))), "Ties",
//					playerLabel.toString());
		}

		return categoryDataSet;
	}
}
