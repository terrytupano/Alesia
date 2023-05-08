package core;

/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

import org.apache.commons.text.*;
import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.extended.button.*;
import com.alee.extended.date.*;
import com.alee.extended.filechooser.*;
import com.alee.extended.image.*;
import com.alee.extended.layout.*;
import com.alee.extended.list.*;
import com.alee.extended.panel.*;
import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.laf.checkbox.*;
import com.alee.laf.combobox.*;
import com.alee.laf.grouping.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.scroll.*;
import com.alee.laf.spinner.*;
import com.alee.laf.text.*;
import com.alee.laf.toolbar.*;
import com.alee.managers.settings.Configuration;
import com.alee.managers.style.*;
import com.alee.managers.tooltip.*;
import com.alee.utils.*;
import com.alee.utils.swing.*;
import com.jgoodies.common.base.*;

import gui.*;
import gui.jgoodies.*;
import gui.wlaf.*;

/**
 * static methods for grapichal user interfaces utils
 * 
 * @author terry
 * 
 */
public class TUIUtils {

	public static final int H_GAP = 4;
	public static final int V_GAP = 4;
	public static final Font H1_Font = UIManager.getFont("Label.font").deriveFont(20l);
	public static final Font H2_Font = UIManager.getFont("Label.font").deriveFont(16l);
//	public static final Color ACCENT_COLOR = Color.BLUE.darker();
	public static final Color ACCENT_COLOR = new Color(63, 90, 116);
	public static final int TOOL_BAR_ICON_SIZE = 20;
	public static final int STANDAR_GAP = 10;
	public static final Border STANDAR_EMPTY_BORDER = new EmptyBorder(TUIUtils.STANDAR_GAP, TUIUtils.STANDAR_GAP,
			TUIUtils.STANDAR_GAP, TUIUtils.STANDAR_GAP);


	public static Color brighter(Color c) {
		double FACTOR = 0.92;
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		int i = (int) (1.0 / (1.0 - FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			return new Color(i, i, i);
		}
		if (r > 0 && r < i)
			r = i;
		if (g > 0 && g < i)
			g = i;
		if (b > 0 && b < i)
			b = i;

		return new Color(Math.min((int) (r / FACTOR), 255), Math.min((int) (g / FACTOR), 255),
				Math.min((int) (b / FACTOR), 255));
	}

	/**
	 * draw a image icon form the icon from <code>Material Icons</code> font.
	 * 
	 * @param unicode - the unicode caracter for the icont
	 * @param size    - target size
	 * @param color   - foreground color
	 * 
	 * @return image
	 */
	public static Image buildImage(char unicode, float size, Color color) {
		Font font = new Font("Material Icons", Font.PLAIN, 1);
		font = font.deriveFont(size);
		String text = Character.toString(unicode);
		return buildImage(text, font, color);
	}

	/**
	 * build an Icon based on the unicode caracter.
	 * 
	 * @param text  - the icon caracter
	 * @param font  - source font where the icon font is.
	 * @param color - foreground color for the image
	 * 
	 * @return image
	 */
	private static BufferedImage buildImage(String text, Font font, Color color) {
		JLabel label = new JLabel(text);
		label.setForeground(color);
		label.setFont(font);
		Dimension dim = label.getPreferredSize();
		int width = dim.width;
		int height = dim.height;
		// int width = dim.width + 1;
		// int height = dim.height + 1;
		label.setSize(width, height);
		BufferedImage bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufImage.createGraphics();
		// g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		// RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		label.print(g2d);
		g2d.dispose();
		return bufImage;
	}

	public static ArrayList<WebButton> createNavButtons(Color toColor, String style, Font font, Action... actions) {
		int size = 20;
		ArrayList<WebButton> list = new ArrayList<>();
		overRideIcons(size, toColor, actions);
		for (Action action : actions) {
			WebButton wb = new WebButton(StyleId.of(style), action);
			if (font != null) {
				wb.setFont(font);
			}
			// TODO: incorporate security
			list.add(wb);
		}
		return list;
	}

	/**
	 * establece el ancho de las columnas de la tabla pasada como primer argumento
	 * al valor especificado en el segundo.
	 * 
	 * @param jt - tabla
	 * @param w  - arreglo de enteros con el ancho de la columna segun su posicion.
	 *           si alguno de ellos es < 1, se omite.
	 */
	public static void fixTableColumn(JTable jt, int[] w) {
		TableColumnModel cm = jt.getColumnModel();
		TableColumn tc;
		for (int i = 0; i < w.length; i++) {
			tc = cm.getColumn(i);
			if (w[i] > 0) {
				tc.setPreferredWidth(w[i]);
			}
		}
	}

	/**
	 * este metodo da formato estandar a una instancia de <code>JLabel</code> segun
	 * los argumentos
	 * 
	 * @param jl  - instancia a dar formato
	 * @param req - requerido
	 * @param ena - habilitado
	 */
	public static void formatJLabel(JLabel jl, boolean req, boolean ena) {
		jl.setEnabled(ena);
		// requerido
		String txt = jl.getText();
		Font f = jl.getFont();
		// obligatorio
		if (req) {
			txt += "*";
		} else {
			txt = txt.trim();
			txt = (txt.endsWith("*") && f.isBold()) ? txt.substring(0, txt.length() - 1) : txt;
		}
		jl.setText(txt);
		jl.setFont((req ? f.deriveFont(Font.BOLD) : f.deriveFont(Font.PLAIN)));
	}

	public static JComponent getBackgroundPanel() {
		WebImage wi = new WebImage(TResources.getIcon("text")) {
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(new LinearGradientPaint(0, 0, 0, getHeight(), new float[] { 0f, 0.4f, 0.6f, 1f },
						new Color[] { Color.gray, Color.WHITE, Color.WHITE, Color.gray }));
				g2d.fill(g2d.getClip() != null ? g2d.getClip() : getVisibleRect());

				super.paintComponent(g);
			}
		};
		wi.setDisplayType(DisplayType.preferred);
		wi.setHorizontalAlignment(SwingConstants.CENTER);
		wi.setVerticalAlignment(SwingConstants.CENTER);
		return wi;
	}

	public static Box getBoxForButtons(JComponent jc, boolean eb) {
		Box b1 = Box.createVerticalBox();
		b1.add(Box.createVerticalStrut(V_GAP));
		b1.add(new JSeparator(JSeparator.HORIZONTAL));
		b1.add(Box.createVerticalStrut(V_GAP));
		b1.add(jc);
		if (eb) {
			setEmptyBorder(b1);
		}
		return b1;
	}

	public static GroupPanel getButtonGroup() {
		GroupPanel bg = new GroupPanel();
		CompoundBorder cb = new CompoundBorder(new EmptyBorder(2, 2, 2, 2), bg.getBorder());
		bg.setBorder(cb);
		return bg;
	}

	public static WebPanel getConfigLinePanel(String field, JComponent rightComponent) {
		Border empty = new EmptyBorder(10, 10, 10, 10);
		LineBorder line = new LineBorder(Color.LIGHT_GRAY);
		CompoundBorder border = new CompoundBorder(line, empty);

		WebPanel panel = new WebPanel(new BorderLayout(10, 10));
		// Color color =UIManager.getColor("Panel.background");
		// color = ColorUtils.intermediate(color, Color.WHITE, 0.5f);
		Color color = ColorUtils.intermediate(panel.getBackground(), Color.WHITE, 0.7f);
		// panel.setBorder(new WebBorder(10));
		panel.setBorder(border);
		panel.setBackground(color);
		WebPanel panel2 = new WebPanel(new VerticalFlowLayout(VerticalFlowLayout.MIDDLE));
		panel2.setBackground(color);
		panel2.add(rightComponent);
		panel.add(getTitleTextLabel(field), BorderLayout.CENTER);
		panel.add(panel2, BorderLayout.EAST);
		return panel;
	}

	/**
	 * return a console style {@link WebEditorPane}
	 * 
	 * @return compoent
	 */
	public static WebEditorPane getConsoleEditorPane() {
		WebEditorPane editorPane = new WebEditorPane();
		editorPane.setEditable(false);
		editorPane.setEditorKit(new StyledEditorKit());
		Font f = new Font("courier new", Font.PLAIN, 12);
		editorPane.setFont(f);
		return editorPane;
	}

	public static Icon getFontIcon(char unicode, float size, Color color) {
		return new ImageIcon(buildImage(unicode, size, color));
	}

	public static GroupPane getGroupPane(Action... actions) {
		return getGroupPane(Arrays.asList(actions));
	}

	public static GroupPane getGroupPane(JComponent... components) {
		GroupPane groupPane = new GroupPane();
		for (JComponent jComponent : components) {
			groupPane.add(jComponent);
		}
		return groupPane;
	}

	public static GroupPane getGroupPane(List<Action> actions) {
		GroupPane groupPane = new GroupPane();
		for (Action action : actions) {
			WebButton b = getWebButtonForToolBar(action);
			groupPane.add(b);
		}
		return groupPane;
	}

	public static JCheckBox getJCheckBox(String field, boolean selected) {
		JCheckBox jcb = new JCheckBox(TStringUtils.getString(field), selected);
		jcb.setName(field);
		return jcb;
	}


	/**
	 * create and return a {@link WebCheckBox}. this implementation assume that the
	 * data type from the model is boolean
	 * 
	 * @param field - the field name
	 * @param model - the Model
	 * 
	 * @return {@link JCheckBox}
	 */
	public static JCheckBox getJCheckBox(String field, Model model) {
		JCheckBox jcb = getJCheckBox(field, model.getBoolean(field));
		return jcb;
	}

	/**
	 * crea y retorna una instancia de <code>JEditorPane</code> con configuracion
	 * estandar para presentacion de texto en formato HTML
	 * 
	 * @param txt - texto a presentar en el componente
	 * 
	 * @return instancia de <code>JEditorPane</code>
	 */
	public static JEditorPane getJEditorPane(String txt) {
		JEditorPane jep = new JEditorPane();
		jep.setEditable(false);
		StyleSheet shee = new StyleSheet();
		try {
			// shee.loadRules(new FileReader(TResources.getFile("HtmlEditor.css")), null);
		} catch (Exception e) {

		}
		HTMLEditorKit kit = new HTMLEditorKit();
		kit.setStyleSheet(shee);
		jep.setEditorKit(kit);
		jep.setText(txt);
		return jep;
	}

	/**
	 * return and {@link JEditorPane} for information read only.
	 * 
	 * @param textId            - text id. may be <code>null</code>
	 * @param hyperlinkListener - may be <code>null</code>
	 * 
	 * @return eidtor pane for read only
	 */
	public static JEditorPane getJEditorPane(String textId, HyperlinkListener hyperlinkListener) {
		String txt = textId == null ? null : TStringUtils.getString(textId);
		WebEditorPane editorPane = new WebEditorPane("text/html", txt);
		editorPane.setEditable(false);
		// editorPane.setOpaque(false);
		editorPane.setFocusable(false);
		HTMLUtils.addDefaultStyleSheetRule(editorPane);
		if (hyperlinkListener != null) {
			editorPane.addHyperlinkListener(hyperlinkListener);
		}
		return editorPane;
	}

	/**
	 * construye y retorna una instancia de JLabel con los atributos establecidos
	 * segun los argumentos de entrada.
	 * 
	 * @param field - id de resource bundle
	 * @param req   - true si el campo es de entrada obligatoria.
	 * @param ena   - abilitado o no.
	 * @return instancia con atributos
	 */
	public static JLabel getJLabel(String field, boolean req, boolean ena) {
		JLabel jl = new JLabel(TStringUtils.getString(field));
		jl.setName(field);
		formatJLabel(jl, req, ena);
		return jl;
	}

	/**
	 * <code>Jt_uspasswordField</code> con formato estandar
	 * 
	 * @param rcd - datos
	 * @param fld - nombre del campo
	 * @return JTextField
	 */
	public static JPasswordField getJPasswordField(Model model, String fld) {
		@SuppressWarnings("static-access")
		int len = model.getMetaModel().getColumnMetadata().get(fld).getColumnSize();
		String val = model.getString(fld);
		JPasswordField jpf = getJPasswordField("tt" + fld, val, len);
		// jpf.setName(fld);
		return jpf;
	}

	/**
	 * <code>Jt_uspasswordField</code> con formato estandar
	 * 
	 * @param ttn - id de tooltip
	 * @param val - valor para el componente
	 * @param cw  - longitud del componente medido en caracteres
	 * @return <code>Jt_uspasswordField</code> con formato estandar
	 */
	public static JPasswordField getJPasswordField(String ttn, String val, int cw) {
		JPasswordField jpf = new JPasswordField(cw);
		jpf.setDocument(new TPlainDocument(val, cw));
		jpf.setText(val);
		// jtf.setColumns(rcd.getFieldLength(fld));
		setDimensionForTextComponent(jpf, cw);
		setToolTip(ttn, jpf);
		return jpf;
	}

	/**
	 * retorna un <code>JRadioButton</code> con valores standar
	 * 
	 * @param ti  - id de tooltip
	 * @param idt - identificador en resourcebundle para el texto
	 * @param sel - estado: seleccionado o no
	 * @return JRadioButton
	 */
	public static JRadioButton getJRadioButton(String ti, String idt, boolean sel) {
		JRadioButton jrb = new JRadioButton(TStringUtils.getString(idt), sel);
		setToolTip(ti, jrb);
		return jrb;
	}

	public static JScrollPane getJTextArea(Model model, String field) {
		@SuppressWarnings("static-access")
		int len = model.getMetaModel().getColumnMetadata().get(field).getColumnSize();
		String val = model.getString(field);
		JScrollPane jsp = getJTextArea("tt" + field, val, len, 2);
		// jsp.setName(f);
		return jsp;
	}

	public static JScrollPane getJTextArea(Model model, String field, int lin) {
		@SuppressWarnings("static-access")
		int len = model.getMetaModel().getColumnMetadata().get(field).getColumnSize();
		String val = model.getString(field);
		JScrollPane jsp = getJTextArea("tt" + field, val, len, lin);
		// jsp.setName(f);
		return jsp;
	}

	public static JScrollPane getJTextArea(String tt, String val, int col, int lin) {
		int cl = (col / lin);
		JTextArea jta = new JTextArea(val, lin, cl);
		jta.setDocument(new TPlainDocument(val, col));
		jta.setLineWrap(true);
		setToolTip(tt, jta);
		setDimensionForTextComponent(jta, cl);
		JScrollPane jsp = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return jsp;
	}

	/**
	 * <code>JTextField</code> con formato estandar
	 * 
	 * @param ttn - id de tooltip
	 * @param val - valor para el componente
	 * @param cw  - longitud del componente medido en caracteres
	 * @return <code>JTextField</code> scon formato estandar
	 */
	public static JTextField getJTextField(String ttn, String val, int cw) {
		JTextField jtf = new JTextField(cw);
		jtf.setDocument(new TPlainDocument(val, cw));
		jtf.setText(val);
		// jtf.setColumns(rcd.getFieldLength(fld));
		setDimensionForTextComponent(jtf, cw);
		setToolTip(ttn, jtf);
		return jtf;
	}

	public static WebPanel getListItems(int height, JComponent... rightComponents) {
		WebPanel panel = new WebPanel(StyleId.panelTransparent, new FormLayout(false, true, STANDAR_GAP, STANDAR_GAP));

		for (JComponent jComponent : rightComponents) {
			String name = jComponent.getName();
			Preconditions.checkNotNull(name, "The component hast no name.");
			ListItem item = ListItem.getItemForField(name, jComponent);
			// the with is forced by the layout
			item.setPreferredSize(new Dimension(0, height));
			panel.add(item, FormLayout.LINE);
		}
		panel.setBorder(STANDAR_EMPTY_BORDER);
		return panel;
	}

	public static WebPanel getListItems(JComponent... rightComponents) {
		return getListItems(70, rightComponents);
	}

	public static JList<ListItem> getListItems2(JComponent... components) {
		Vector<ListItem> items = new Vector<>();
		for (JComponent jComponent : components) {
			String name = jComponent.getName();
			Preconditions.checkNotNull(name, "The component hast no name.");
			ListItem item = ListItem.getItemForField(name, jComponent);
			items.add(item);
		}
		JList<ListItem> jList = new JList<>(items);
		return jList;
	}

	/**
	 * return the ImageIcon <code>src</code> with a mark which is a scaled instance
	 * of the icon file name <code>mfn</code> draw over the source image.
	 * 
	 * @param src - original imagen
	 * @param mfn - icon file name used as mark
	 * @param h   - Horizontal position of the mark. any of
	 *            {@link SwingConstants#LEFT} or {@link SwingConstants#RIGHT}
	 * @param h   - Vertical position of the mark. any of {@link SwingConstants#TOP}
	 *            or {@link SwingConstants#BOTTOM}
	 * @return the image icon with the mark
	 */
	public static ImageIcon getMarkIcon(ImageIcon src, String mfn, int h, int v) {
		int size = src.getIconHeight();
		BufferedImage bi = ImageUtils.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(src.getImage(), 0, 0, null);
		ImageIcon ii = TResources.getIcon(mfn, (int) (size * .6));
		// draw position
		int hpos = h == SwingConstants.LEFT ? 0 : size - ii.getIconWidth();
		int vpos = v == SwingConstants.TOP ? 0 : size - ii.getIconHeight();
		g2d.drawImage(ii.getImage(), hpos, vpos, null);
		return new ImageIcon(bi);
	}

	/**
	 * create and return a especial instace of {@link WebButton}
	 * 
	 * @param action - action
	 * 
	 * @return especial webbuton
	 */
	public static WebButton getMosaicWebButton(Action action) {
		overRideIcons(32, ACCENT_COLOR, action);
		WebButton btn = new WebButton(StyleId.buttonHover, action);
		// btn.onMouseEnter(me -> btn.setBorder(new LineBorder(Color.BLUE));
		String html = TStringUtils.getTitleText(action.getValue(javax.swing.Action.NAME).toString(),
				action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
		btn.setText(html);
		btn.setIconTextGap(8);
		btn.setVerticalAlignment(SwingConstants.TOP);
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		// btn.setVerticalTextPosition(SwingConstants.TOP);
		return btn;
	}

	public static NumericTextField getNumericTextField(String field, Model model, Map<String, ColumnMetadata> columns) {
		return getNumericTextField(field, model, columns, null);
	}

	public static NumericTextField getNumericTextField(String field, Model model, Map<String, ColumnMetadata> columns,
			String mask) {
		ColumnMetadata column = columns.get(field);
		int len = column.getColumnSize();
		String val = model.getString(field);
		return getNumericTextField(field, val, len, mask);
	}

	public static NumericTextField getNumericTextField(String name, String text, int cols, String mask) {
		NumericTextField ntf = new NumericTextField(text, cols);
		if (mask != null) {
			DecimalFormat fmt = new DecimalFormat(mask);
			fmt.setGroupingUsed(true);
			fmt.setGroupingSize(3);
			fmt.setParseIntegerOnly(false);
			ntf = new NumericTextField(text, cols, fmt);
		}

		ntf.setName(name);
		ntf.putClientProperty("settingsProcessor", new Configuration<TextComponentState>(name));
		setToolTip(name, ntf);
		return ntf;
	}

	public static Icon getSmallFontIcon(char unicode) {
		return new ImageIcon(buildImage(unicode, TOOL_BAR_ICON_SIZE, Color.BLACK));
	}

	/**
	 * create and return a {@link JScrollPane} setted with an instance of
	 * {@link SmartScroller}. this is intended for console style components
	 * 
	 * @see SmartScroller
	 * @param component - component to scroll
	 * @return {@link JScrollPane} with standard {@link SmartScroller}
	 */
	public static JScrollPane getSmartScroller(JComponent component) {
		WebScrollPane pane = new WebScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		WebScrollPane pane = getWebScrollPane(component);
//		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		new SmartScroller(pane);
		return pane;
	}

	public static WebToggleButton getStartPauseToggleButton(Action action, ActionListener listener) {
		WebToggleButton startPause = new WebToggleButton();
		startPause.setSelectedIcon(TUIUtils.getSmallFontIcon('\ue037'));
		if (action != null) {
			startPause.setAction(action);
			// overRideToolBarButton(startPause);
		} else {
			startPause.setIcon(TUIUtils.getSmallFontIcon('\ue034'));
		}
		if (listener != null)
			startPause.addActionListener(listener);
		return startPause;

	}

	public static WebToggleButton getStartPauseToggleButton(ActionListener actionListener) {
		return getStartPauseToggleButton(null, actionListener);
	}

	public static int getStringPixelHeight(String str, Font font) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(str, null);
		return (int) bounds.getHeight();
	}

	static int getStringPixelWidth(String str, Font font) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(str, null);
		return (int) bounds.getWidth();
	}

	/**
	 * TODO: temp move to laf xml file
	 * 
	 * @param field
	 * @param stlId
	 * @return
	 */
	public static JLabel getStyledJLabel(String field, String stlId) {
		JLabel jl = new JLabel("<html><" + stlId + " style='font-family:Segoe UI light; color:gray'>"
				+ TStringUtils.getString(field) + "</" + stlId + "></html>");
		return jl;
	}

	/**
	 * crea y retorna un separador horizontal con un texto colocado hacia la
	 * izquierda
	 * 
	 * 20161123.04:25 NAAAA GUEBONAAA DE VIEJOOOOO !!! ESTE METODO DEBE TENER +10
	 * A�OS !!!! FUE DE LOS PRIMEROS PARA CLIO
	 * 
	 * @param idl - id para texto
	 * @return componente
	 */
	public static JComponent getTitledSeparator(String idl) {
		Box tb1 = Box.createVerticalBox();
		tb1.add(Box.createVerticalGlue());
		tb1.add(new JSeparator());
		Box tb = Box.createHorizontalBox();
		JLabel jl = new JLabel(TStringUtils.getString(idl));
		jl.setFont(jl.getFont().deriveFont(Font.BOLD));
		tb.add(jl);
		tb.add(Box.createHorizontalStrut(H_GAP));
		tb.add(tb1);
		return tb;
	}

	public static WebPanel getTitlePanel(String constID) {
		WebLabel tit = new WebLabel(StyleId.labelShadow, TStringUtils.getString(constID));
		tit.changeFontSize(2);
		WebLabel text = new WebLabel(TStringUtils.getString(constID + ".tt"));
		WebPanel panel = new WebPanel(StyleId.panelTransparent, new FormLayout(false, false, STANDAR_GAP, 0));
		panel.add(tit, FormLayout.LINE);
		panel.add(text, FormLayout.LINE);
		return panel;
	}

	/**
	 * return a {@link WebLabel} with html formated title/message elements. the
	 * title element is the text found in property file and the message will be the
	 * field.tt text
	 * 
	 * @see TStringUtils#getTitleText(String, String)
	 * 
	 * @param field   - property key for title
	 * @param message - property key for message
	 * 
	 * @return {@link WebLabel}
	 */
	public static WebLabel getTitleTextLabel(String field) {
		String tit = TStringUtils.getString(field);
		String msg = TStringUtils.getString(field + ".tt");
		String html = TStringUtils.getConfigTitleText(tit, msg);
		return new WebLabel(html);
	}

	public static WebPanel getTitleTextPanel(String title, String description) {
		WebLabel titleLabel = new WebLabel(StyleId.labelShadow, title, WebLabel.LEFT);
		Font font = titleLabel.getFont();
		titleLabel.setFont(font.deriveFont(font.getSize() + 2F));

		WebLabel descriptionLabel = new WebLabel(description, WebLabel.LEFT);
		descriptionLabel.setForeground(Color.GRAY);

		WebPanel vertical = new WebPanel(new VerticalFlowLayout(VerticalFlowLayout.MIDDLE, 0, 0, true, false));
		vertical.setOpaque(false);
		vertical.add(titleLabel);
		vertical.add(descriptionLabel);

		return vertical;

	}

	/**
	 * return a {@link JScrollPane} with a {@link TPropertyJTable} inside
	 * 
	 * @param rcd - record to obtain properties
	 * @param fld - field name
	 * @return Component
	 * 
	 */
	public static JScrollPane getTPropertyJTable(Model model, String fld) {
		return getTPropertyJTable("tt" + fld, model.getString(fld).toString());
	}

	public static JScrollPane getTPropertyJTable(String tid, String prpl) {
		TPropertyJTable tpjt = new TPropertyJTable(prpl);
		setToolTip(tid, tpjt);
		JScrollPane js = new JScrollPane(tpjt);
		js.getViewport().setBackground(Color.WHITE);
		return js;
	}

	public static WebComboBox getTWebComboBox(String fieldName, String group, String selectedKey) {
		List<TSEntry> entries = TStringUtils.getEntriesFrom(group);
		return getWebComboBox(fieldName, entries, selectedKey);
	}

	public static WebToolBar getUndecoradetToolBar(Component... components) {
		WebToolBar toolBar = new WebToolBar(StyleId.toolbarUndecorated);
		toolBar.add(components);
		return toolBar;
	}

	/**
	 * create and return and {@link WebButton} with all settings established for
	 * toolbar
	 * 
	 * @param action - action to set in the button
	 * @return button ready to set as toolbar button
	 * @since 2.3
	 */
	public static WebButton getWebButtonForToolBar(Action action) {
		overRideIcons(TOOL_BAR_ICON_SIZE, action);
//		WebButton button = new WebButton(StyleId.buttonHover, action);
		WebButton button = new WebButton(action);
		button.setText(null);
		return button;
	}

	public static WebButton getWebButtonForToolBar(Object actionSource, String action) {
		 ApplicationContext ac = Alesia.getInstance().getContext();
		 ActionMap actionMap = ac.getActionMap(actionSource.getClass(), actionSource);
		 return getWebButtonForToolBar(actionMap.get(action));
	}

	public static WebCheckBox getWebCheckBox(String name) {
		return getWebCheckBox(name, false);
	}

	public static WebCheckBox getWebCheckBox(String name, boolean selected) {
		WebCheckBox jcb = new WebCheckBox(TStringUtils.getString(name));
		jcb.setSelected(selected);
		jcb.setName(name);
		setToolTip(name, jcb);
		jcb.putClientProperty("settingsProcessor", new Configuration<ButtonState>(name));
		return jcb;
	}

	public static WebCheckBoxList<TSEntry> getWebCheckBoxList(String fieldName, String group) {
		List<TSEntry> entries = TStringUtils.getEntriesFrom(group);
		CheckBoxListModel<TSEntry> model = new CheckBoxListModel<>();
		entries.forEach(e -> model.add(new CheckBoxCellData<TSEntry>(e)));
		WebCheckBoxList<TSEntry> boxList = new WebCheckBoxList<>(model);
		return boxList;
	}

	public static WebComboBox getWebComboBox(String fieldName, List<TSEntry> entries, String selectedKey) {
		TSEntry entry = TStringUtils.getEntryFromList(entries, selectedKey);
		WebComboBox comboBox = new WebComboBox(entries, entry);
		comboBox.setName(fieldName);
		comboBox.putClientProperty("settingsProcessor", new Configuration<ComboBoxState>(fieldName));
		setToolTip(fieldName, comboBox);
		return comboBox;
	}

	public static WebComboBox getWebComboBox(String fieldName, String group) {
		List<TSEntry> entries = TStringUtils.getEntriesFrom(group);
		return getWebComboBox(fieldName, entries, null);
	}

	/**
	 * create and return {@link WebDateField} according to parameters
	 * 
	 * @param rcd - Record to obtain data
	 * @param fn  - record field name
	 * @return {@link WebDateField}
	 * 
	 */
	public static WebDateField getWebDateField(Model model, ColumnMetadata column) {
		String name = column.getColumnName();
		WebDateField wdf = getWebDateField("tt" + name, model.getDate(name));
		wdf.setName(name);
		return wdf;
	}

	/**
	 * create and return {@link WebDateField} according to parameters. the date
	 * format is dd/MM/yyy
	 * 
	 * @param tt  - id for tooltips
	 * @param val - date
	 * @return {@link WebDateField}
	 */
	public static WebDateField getWebDateField(String tt, Date val) {
		WebDateField wdf = val == null ? new WebDateField() : new WebDateField(val);
		wdf.setDateFormat(new SimpleDateFormat("dd/MM/yyy"));
		setDimensionForTextComponent(wdf, 10);
		setToolTip(tt, wdf);
		return wdf;
	}

	public static TWebFileChooserField getWebDirectoryChooserField(Model model, String fn) {
		final TWebFileChooserField fcf = getWebFileChooserField("tt" + fn, model.getString(fn));
		WebButton wb = fcf.getChooseButton();
		wb.removeActionListener(wb.getActionListeners()[0]);
		wb.addActionListener(new ActionListener() {
			private WebDirectoryChooser directoryChooser = null;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (directoryChooser == null) {
					directoryChooser = new WebDirectoryChooser(Alesia.getInstance().getMainFrame());
				}
				directoryChooser.setVisible(true);
				if (directoryChooser.getResult() == DialogOptions.OK_OPTION) {
					fcf.setSelectedFile(directoryChooser.getSelectedDirectory());
				}
			}
		});
		fcf.getWebFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return fcf;
	}

	public static TWebFileChooserField getWebFileChooserField(Model model, String fn) {
		return getWebFileChooserField("tt" + fn, model.getString(fn));
	}

	public static TWebFileChooserField getWebFileChooserField(String ttn, String file) {
		TWebFileChooserField wfcf = new TWebFileChooserField(file);
		wfcf.setPreferredWidth(200);
		wfcf.setMultiSelectionEnabled(false);
		wfcf.setShowFileShortName(false);
		wfcf.getWebFileChooser().setFileSelectionMode(JFileChooser.FILES_ONLY);
		setToolTip(ttn, wfcf);
		return wfcf;
	}

	/**
	 * return a {@link WebTextField} with a trailing cancel button. The cancel
	 * button reroute the action performed to set a empty text on text component and
	 * notify the actionlister.
	 * 
	 * @param alist - action listener. listerner to notify when a change on the text
	 *              component ocurr.
	 * 
	 * @return text field for search or filter
	 */
	public static WebTextField getWebFindField(final ActionListener alist) {
		final WebTextField findf = new WebTextField(20);
		// WebButton cancelbt =
		// WebButton.createIconWebButton(TResourceUtils.getIcon("cancelAction", 14), 0);
		WebButton cancelbt = new WebButton(TResources.getIcon("cancelAction", 14));
		cancelbt.setName("Cancel");
		cancelbt.setFocusable(false);
		// cancelbt.setShadeWidth(0);
		// cancelbt.setMoveIconOnPress(false);
		// cancelbt.setRolloverDecoratedOnly(true);
		cancelbt.setCursor(Cursor.getDefaultCursor());

		findf.setMargin(0, 0, 0, 0);
		findf.setTrailingComponent(cancelbt);
		// findf.setRound(WebDateFieldStyle.round);
		// findf.setShadeWidth(WebDateFieldStyle.shadeWidth);

		// 180403: old find button replaced by cancel button

		// button change source to WebTextField for simplicity
		cancelbt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findf.setText("");
				alist.actionPerformed(new ActionEvent(findf, ActionEvent.ACTION_PERFORMED, ""));
			}
		});

		findf.addActionListener(alist);
		setDimensionForTextComponent(findf, 20);
		setToolTip("ttsearch.textfield", findf);
		setToolTip("ttsearch.button", cancelbt);

		return findf;
	}

	public static WebFormattedTextField getWebFormattedTextField(Model model, ColumnMetadata column) {
		return getWebFormattedTextField(model, column, null);
	}

	public static WebFormattedTextField getWebFormattedTextField(Model model, ColumnMetadata column, String mask) {
		int len = column.getColumnSize();
		String field = column.getColumnName();
		Object val = model.get(field);
		return getWebFormattedTextField(field, val, len, mask);
	}

	public static WebFormattedTextField getWebFormattedTextField(String field, Model model,
			Map<String, ColumnMetadata> columns) {
		return getWebFormattedTextField(field, model, columns, null);
	}

	public static WebFormattedTextField getWebFormattedTextField(String field, Model model,
			Map<String, ColumnMetadata> columns, String mask) {
		ColumnMetadata metadata = columns.get(field);
		return getWebFormattedTextField(model, metadata, mask);
	}

	public static WebFormattedTextField getWebFormattedTextField(String name, Object val, int cols) {
		return getWebFormattedTextField(name, val, cols, null);
	}

	public static WebFormattedTextField getWebFormattedTextField(String name, Object val, int cols, String mask) {
		WebFormattedTextField jftf = new WebFormattedTextField();
		if (mask != null) {
			try {
				MaskFormatter fmt = new MaskFormatter(mask);
				fmt.setAllowsInvalid(false);
				jftf = new WebFormattedTextField(fmt);
				jftf.setInputPrompt(mask);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		jftf.setValue(val);
		jftf.setColumns(cols);
		jftf.setName(name);
		// if (val instanceof java.lang.Number) {
		// jftf.setHorizontalAlignment(JTextField.RIGHT);
		// }
		setDimensionForTextComponent(jftf, cols);
		// setToolTip(name, jftf);
		// 180614: implement focus listener
		FocusAdapter fa = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				final JFormattedTextField jc = (JFormattedTextField) e.getSource();
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						jc.selectAll();
					}
				});
			}
		};
		jftf.addFocusListener(fa);

		jftf.putClientProperty("settingsProcessor", new Configuration<TextComponentState>(name));
		return jftf;
	}

	public static WebPasswordField getWebPasswordField(String field, String val, int cw) {
		WebPasswordField wpf = new WebPasswordField(cw);
		wpf.setDocument(new TPlainDocument(val, cw));
		wpf.setText(val);

		wpf.setName(field);
		return wpf;
	}

	public static WebScrollPane getWebScrollPane(JComponent component) {
		WebScrollPane scrollPane = new WebScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setStyleId(StyleId.scrollpaneTransparentHovering);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	public static WebSpinner getWebSpinner(String name, int val, int min, int max, int step) {
		SpinnerNumberModel sModel = new SpinnerNumberModel(val, min, max, step);
		WebSpinner spinner = new WebSpinner(sModel);
		// spinner.setValue(val);
		spinner.setName(name);
		spinner.putClientProperty("settingsProcessor", new Configuration<TextComponentState>(name));
		setToolTip(name, spinner);
		return spinner;
	}

	public static WebSwitch getWebSwitch(String name, boolean selected) {
		final WebSwitch wswitch = new WebSwitch(selected);
		wswitch.setSwitchComponents("On", "Off");
		wswitch.setName(name);
		wswitch.putClientProperty("settingsProcessor", new Configuration<ButtonState>(name));
		return wswitch;
	}

	public static WebTextField getWebTextField(Model model, ColumnMetadata column) {
		int len = column.getColumnSize();
		String field = column.getColumnName();
		String val = model.getString(field);
		WebTextField jftf = getWebTextField(field, val, len);
		return jftf;
	}

	public static WebTextField getWebTextField(String field, Model model, Map<String, ColumnMetadata> columns) {
		ColumnMetadata metadata = columns.get(field);
		return getWebTextField(model, metadata);
	}

	public static WebTextField getWebTextField(String name, String val, int colums) {
		WebTextField textField = new WebTextField(colums);
		textField.setDocument(new TPlainDocument(val, colums));
		textField.setText(val);
		textField.setName(name);
		textField.putClientProperty("settingsProcessor", new Configuration<TextComponentState>(name));
		setToolTip(name, textField);
		return textField;
	}

	/**
	 * create and return and {@link WebToggleButton} with all standard settings for
	 * toolbar
	 * 
	 * @param action - action to set in the button
	 * @return button ready to set as toolbar button
	 * @since 2.3
	 */
	public static WebToggleButton getWebToggleButton(Action action) {
		overRideIcons(TOOL_BAR_ICON_SIZE, action);
		WebToggleButton jb = new WebToggleButton(StyleId.togglebutton, action);
		// test: for toglebuttons, perform action performed over the action
		// jb.addItemListener(
		// evt -> ((AbstractButton) evt.getSource()).getAction().actionPerformed(new
		// ActionEvent(jb, -1, "")));
		jb.setText(null);
		jb.setPreferredWidth(46);
		return jb;
	}

	public static WebToggleButton getWebToggleButtonForToolBar(Action action) {
		overRideIcons(TOOL_BAR_ICON_SIZE, Color.BLACK, action);
		WebToggleButton button = new WebToggleButton(StyleId.buttonHover, action);
		button.setText(null);
		// button.setPreferredSize(new Dimension(46, 26));
		return button;
	}

	public static WebToolBar getWebToolBar() {
		WebToolBar toolBar = new WebToolBar(StyleId.toolbarAttachedNorth);
		toolBar.setFloatable(false);
		return toolBar;
	}

	public static WebToolBar getWebToolBar(Action... actions) {
		WebToolBar toolBar = getWebToolBar();
		for (Action action : actions) {
			WebButton b = getWebButtonForToolBar(action);
			toolBar.add(b);
		}
		return toolBar;
	}

	/**
	 * Perform the initialization method
	 * 
	 * @since 2.3
	 */
	public static void init() {
		try {
			List<File> fonts = FileUtils.findFilesRecursively(TResources.USER_DIR,
					file -> file.getName().endsWith(".ttf") || file.getName().endsWith(".otf"));
			for (File fontNam : fonts) {
				Font fo = Font.createFont(Font.TRUETYPE_FONT, fontNam);
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void overRideIcons(int size, Action... actions) {
		overRideIcons(size, Color.BLACK, actions);
	}

	/**
	 * Utility method for override icons properties setted in the
	 * {@link ApplicationAction} by bsf framework. This method
	 * <ul>
	 * <li>check the attribute <code>[action name].Action.iconFont</code>. if this
	 * attribute is present, set the {@link Action#SMALL_ICON} property for the
	 * action to this icon font
	 * <li>if iconFont atributte is not present, take the icon from the
	 * {@link Action#SMALL_ICON} and create an scaled instance using the size
	 * parameter. Repaint the icon to the target color toColor argument
	 * </ul>
	 * 
	 * @param size    - new icon size
	 * @param toColor - new icon color
	 * @param actions - list of actions to override
	 * 
	 * @since 2.3
	 */
	public static void overRideIcons(int size, Color toColor, Action... actions) {
		for (Action action : actions) {
			org.jdesktop.application.ApplicationAction aa = (org.jdesktop.application.ApplicationAction) action;
			org.jdesktop.application.ResourceMap rm = aa.getResourceMap();
			String ifon = rm.getString(aa.getName() + ".Action.iconFont");
			if (ifon != null) {
				action.putValue(Action.LARGE_ICON_KEY, TUIUtils.getFontIcon(ifon.toCharArray()[0], size, toColor));
			} else {
				ImageIcon ii = (ImageIcon) action.getValue(Action.SMALL_ICON);
				// maybe the action has no icon
				if (ii != null) {
					if (toColor != null) {
						// ii = TColorUtils.changeColor(ii, toColor);
					}
					Image i = ii.getImage().getScaledInstance(size, size, Image.SCALE_FAST);
					action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(i));
				}
			}
		}
	}

	/**
	 * establece dimenciones para los componentes. Si una instancia de
	 * <code>JTextField</code> sobrepasa las 30 columnas, no se modifica el ancho ya
	 * que se asume que se ve mejor. ademas, si componente de texto es menor a las 5
	 * colummas, se redondea a 5
	 * 
	 * @param jtc - componente de texto
	 * @param col - columnas
	 */
	public static void setDimensionForTextComponent(JComponent jtc, int col) {
		col = (col < 5) ? 5 : col;
		col = (col > 50) ? 50 : col;
		if (jtc instanceof JTextField) {
			((JTextField) jtc).setColumns(col);
		}
		if (jtc instanceof JTextArea) {
			((JTextArea) jtc).setColumns(col);
		}
		Dimension d = jtc.getPreferredSize();
		d.width = (int) ((col * 10) * 0.80); // % del tama�o
		jtc.setPreferredSize(d);
		jtc.setMaximumSize(d);
	}

	public static void setEmptyBorder(JComponent comp) {
		comp.setBorder(new EmptyBorder(H_GAP, V_GAP, H_GAP, V_GAP));
	}

	public static void setEnabledRecursively(Component component, boolean enabled) {
		Component[] cmps = (component instanceof Box || component instanceof JPanel) ? cmps = ((Container) component).getComponents()
				: new Component[] { component };

		for (int e = 0; e < cmps.length; e++) {
			cmps[e].setEnabled(enabled);
			if (cmps[e] instanceof Box || cmps[e] instanceof JPanel) {
				setEnabledRecursively(cmps[e], enabled);
			}
			if (cmps[e] instanceof JScrollPane) {
				setEnabledRecursively(((JScrollPane) cmps[e]).getViewport().getView(), enabled);
			}
		}
	}

	/**
	 * set the tooltip for the component. If the component is a instance of
	 * {@link ToolTipMethods}, the tooltip will be a {@link WebLookAndFeel} tooltip,
	 * else the tooltip will be a standar swing tooltip
	 * 
	 * @param name - the name for the resorurce bundle. this method will be append
	 *             <code>.tt</code> to this name to look for the tooltip
	 * @param cmp  - the component
	 */
	public static void setToolTip(String name, JComponent cmp) {
		if (name != null) {
			String n1 = name + ".tt";
			String tooltip = TStringUtils.getString(n1);
			if (!n1.equals(tooltip)) {
				tooltip = tooltip.length() > 80 ? WordUtils.wrap(tooltip, 80) : tooltip;
				if (cmp instanceof ToolTipMethods)
					((ToolTipMethods) cmp).setToolTip(tooltip);
				else
					cmp.setToolTipText(tooltip);
			}
		}
	}

}
