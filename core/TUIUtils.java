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
package core;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.table.TableColumn;
import javax.swing.text.*;
import javax.swing.text.html.*;

import org.apache.commons.text.*;
import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.extended.date.*;
import com.alee.extended.filechooser.*;
import com.alee.extended.image.*;
import com.alee.extended.list.*;
import com.alee.extended.panel.*;
import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.laf.checkbox.*;
import com.alee.laf.combobox.*;
import com.alee.laf.label.*;
import com.alee.laf.scroll.*;
import com.alee.laf.text.*;
import com.alee.laf.toolbar.*;
import com.alee.managers.settings.Configuration;
import com.alee.managers.style.*;
import com.alee.managers.tooltip.*;
import com.alee.utils.*;
import com.alee.utils.swing.*;

import dev.utils.*;
import gui.*;
import gui.wlaf.*;
import javafx.scene.control.*;

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

	/**
	 * copiado de <code>Color.brighter()</code> pero con el factor modificado para obtener un mejor degradado
	 * 
	 * @return color un poco mas brillante
	 */
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
	 * @param size - target size
	 * @param color - foreground color
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
	 * establece el ancho de las columnas de la tabla pasada como primer argumento al valor especificado en el segundo.
	 * 
	 * @param jt - tabla
	 * @param w - arreglo de enteros con el ancho de la columna segun su posicion. si alguno de ellos es < 1, se omite.
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
	 * este metodo da formato estandar a una instancia de <code>JLabel</code> segun los argumentos
	 * 
	 * @param jl - instancia a dar formato
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
				g2d.setPaint(new LinearGradientPaint(0, 0, 0, getHeight(), new float[]{0f, 0.4f, 0.6f, 1f},
						new Color[]{Color.gray, Color.WHITE, Color.WHITE, Color.gray}));
				g2d.fill(g2d.getClip() != null ? g2d.getClip() : getVisibleRect());

				super.paintComponent(g);
			}
		};
		wi.setDisplayType(DisplayType.preferred);
		wi.setHorizontalAlignment(SwingConstants.CENTER);
		wi.setVerticalAlignment(SwingConstants.CENTER);
		return wi;
	}

	/**
	 * retorna un <code>Box</code> con formato preestablecido para los componentes que se encuentran en la parte
	 * inferior de las ventanas de dialogo.
	 * 
	 * @param jc - Generalmente, un contenedor con los botones ya añadidos
	 * @return <code>Box</code> listo para adicionar a la parte inferirior
	 */
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

	/**
	 * return a {@link CheckComboBox} with predefined parameters
	 * 
	 * @param ct - constants group
	 * @param rcd - Record
	 * @param fld - field name of store parameters
	 * 
	 * @return {@link CheckComboBox}
	 */
	public static CheckComboBox getCheckComboBox(String ct, Model model, String fld) {
		TEntry[] val = TStringUtils.getTEntryGroup(ct);
		CheckComboBox jcbox = getCheckComboBox("tt" + fld, val, model.getString(fld));
		return jcbox;
	}

	/**
	 * return a {@link CheckComboBox} with predefined parameters
	 * 
	 * @param tid - id for {@link Tooltip}
	 * @param val - {@link TEntry} array
	 * @param sel - selected key separated by ;
	 * 
	 * @return {@link CheckComboBox}
	 */
	public static CheckComboBox getCheckComboBox(String tid, TEntry[] val, String sel) {
		CheckComboBox jcbox = new CheckComboBox(val, sel);
		setToolTip(tid, jcbox);
		return jcbox;
	}

	/**
	 * crea y retorna una instancia de <code>JComboBox</code> diseñada presentar una paleta de colores DESENTEEEEEEEEEEE
	 * POR FAVOOOOOOORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
	 * 
	 * 
	 * @param tid - id para tooltip
	 * @param col - color en formato <code>Integer.decode(Sttring)</code> (0xHexDigits 0XHexDigits #HexDigits)
	 * 
	 * @return selector de color
	 */
	public static JComboBox getColorJComboBox(String tid, Color col) {
		ColorComboBox jcbox = new ColorComboBox(col);
		setToolTip(tid, jcbox);
		return jcbox;
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

	/**
	 * return a console stily {@link WebTextArea}.
	 * <p>
	 * NOTE: to conrrect control the scrolling, this method DONT set the preferedSize
	 * 
	 * @see #getSmartScroller(JComponent)
	 * 
	 * @return conole stile {@link WebTextArea}
	 */
	public static WebTextArea getConsoleTextArea() {
		WebTextArea console = new WebTextArea();
		Font f = new Font("courier new", Font.PLAIN, 12);
		console.setFont(f);
		console.setLineWrap(false);
		console.setEditable(false);
		// int h = getStringPixelHeight("X", f);
		// console.setPreferredSize(new Dimension(-1, h * 10));
		// console.setMinimumSize(new Dimension(-1, h * 10));
		return console;
	}

	public static Icon getFontIcon(char unicode, float size, Color color) {
		return new ImageIcon(buildImage(unicode, size, color));
	}

	/**
	 * retorna un unico componente dentro de una Box alineado hacia la izquierda
	 * 
	 * @param jcomp - component
	 */
	public static Box getInHoriszontalBox(JComponent jcomp) {
		Box b = Box.createHorizontalBox();
		b.add(jcomp);
		b.add(Box.createHorizontalGlue());
		return b;
	}

	/**
	 * coloca los componentes pasados como argumentos uno junto a oltro en un
	 * <code>new JPanel(new FlowLayout(alg, H_GAP, 0))</code> (alineados hacia alg con un espacio entre componentes de
	 * H_GAP
	 * 
	 * @param jcomps - componentes
	 * @param alg - alineacion de los componente. Puede ser cualquiera <code>FlowLayout.XXX</code>
	 * @return JPanel
	 */
	public static JPanel getInHorizontalBox(Component[] jcomps, int alg) {
		JPanel jp = new JPanel(new FlowLayout(alg, H_GAP, 0));
		for (int t = 0; t < jcomps.length; t++) {
			jp.add(jcomps[t]);
		}

		return jp;
	}

	/**
	 * Retorna el par <code>JLabel(lab) JComponent</code> en un <code>Box</code> con alineacion horizontal con ambos
	 * componentes a los extremos del contenedor
	 * 
	 * @param lab - id en ResourceBundle para <code>JLabel</code>
	 * @param jcom - componente al que refiere la etiqueta lab
	 * @param req - <code>true</code> si el par es de entrada obligatoria
	 * @param ena - <code>true</code> ambos etiqueta y componente habilitados.
	 * @return Box con componentes en su interior
	 */
	public static Box getInHorizontalBox(String lab, JComponent jcom, boolean req, boolean ena) {
		return coupleInBox(lab, jcom, req, ena, true);
	}

	/**
	 * Retorna el par <code>JLabel(lab) JComponent</code> en un <code>Box</code> con alineacion horizontal pero con
	 * <code>Box.CreateHorizontalGlue()</code> con ambos componentes hacia la izquierda del contenedor
	 * 
	 * @param lab - id en ResourceBundle para <code>JLabel</code>
	 * @param jcom - componente al que refiere la etiqueta lab
	 * @param req - <code>true</code> si el par es de entrada obligatoria
	 * @param ena - <code>true</code> ambos etiqueta y componente habilitados.
	 * @return Box con componentes en su interior
	 */
	public static Box getInHorizontalBoxWithGlue(String lab, JComponent jcom, boolean req, boolean ena) {
		return coupleInBox(lab, jcom, req, ena, false);
	}

	/**
	 * retorna los componentes pasados como argumentos en un contenedor, colocados verticalmente y alineados segun el
	 * parametro alg
	 * 
	 * @param jcomps - componentes
	 * @param alg - alineacion (de SwingConstants)
	 * @return Box
	 */
	public static Box getInVerticalBox(JComponent[] jcomps, int alg) {
		Box b = Box.createVerticalBox();
		for (int t = 0; t < jcomps.length; t++) {
			Box bt = Box.createHorizontalBox();
			bt.add(jcomps[t]);
			bt.add(Box.createHorizontalGlue());
			b.add(bt);
			b.add(Box.createHorizontalStrut(V_GAP));
		}
		Box b2 = Box.createHorizontalBox();
		if (alg == SwingConstants.LEFT) {
			b2.add(Box.createHorizontalGlue());
			b2.add(b);
		}
		if (alg == SwingConstants.RIGHT) {
			b2.add(b);
			b2.add(Box.createGlue());
		}
		return b2;

	}

	/**
	 * retorna el par <code>JLabel(lab) JComponent</code> dentro de un <code>Box</code> vertical alineados hacia la
	 * izquierda.
	 * 
	 * @param lab - id en ResourceBundle para <code>JLabel</code>
	 * @param jcom - componente
	 * @param req - si el componente es de entrada obligatoria o no.
	 * @param ena - valor para <code>jcom.setEnabled()</code>
	 * 
	 * @return box vertical
	 */
	public static JPanel getInVerticalBox(String lab, JComponent jcom, boolean req, boolean ena) {
		Box b1 = Box.createHorizontalBox();
		JLabel jl = getJLabel(lab, req, ena);
		b1.add(jl);
		b1.add(Box.createHorizontalGlue());
		jcom.setEnabled(ena);

		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
		jp.add(b1);
		jp.add(jcom);

		/**
		 * Box b2 = Box.createHorizontalBox(); b2.add(jcom); b2.add(Box.createHorizontalGlue()); Box b =
		 * Box.createVerticalBox(); b.add(b1); b.add(b2);
		 */
		return jp;// b;
	}

	/**
	 * retorna un <code>JCheckBox</code> con valores standar
	 * 
	 * @param idt - identificador en resourcebundle para el texto
	 * @param sel - estado: seleccionado o no
	 * 
	 * @return JCheckBox
	 */
	public static JCheckBox getJCheckBox(String idt, boolean sel) {
		JCheckBox jcb = new JCheckBox(TStringUtils.getString(idt), sel);
		jcb.setName(idt);
		return jcb;
	}
	/**
	 * create and return a {@link WebCheckBox}. this implementation assume that the data type from the model is boolean
	 * 
	 * @param field - the field name
	 * @param model - the Model
	 * 
	 * @return {@link WebComboBox}
	 */
	public static JCheckBox getJCheckBox(String field, Model model) {
		JCheckBox jcb = getJCheckBox(field, model.getBoolean(field));
		// jcb.setName(fld);
		return jcb;
	}

	/**
	 * crea y retorna una instancia de <code>JEditorPane</code> con configuracion estandar para presentacion de texto en
	 * formato HTML
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
			shee.loadRules(new FileReader(TResources.getFile("HtmlEditor.css")), null);
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
	 * @param textId - text id. may be <code>null</code>
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
	 * construye y retorna una instancia de JLabel con los atributos establecidos segun los argumentos de entrada.
	 * 
	 * @param field - id de resource bundle
	 * @param req - true si el campo es de entrada obligatoria.
	 * @param ena - abilitado o no.
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
	 * @param cw - longitud del componente medido en caracteres
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
	 * @param ti - id de tooltip
	 * @param idt - identificador en resourcebundle para el texto
	 * @param sel - estado: seleccionado o no
	 * @return JRadioButton
	 */
	public static JRadioButton getJRadioButton(String ti, String idt, boolean sel) {
		JRadioButton jrb = new JRadioButton(TStringUtils.getString(idt), sel);
		setToolTip(ti, jrb);
		return jrb;
	}
	/**
	 * JtextArea estadar para datos de registros
	 * 
	 * @param r - registro
	 * @param f - nombre de la columna
	 * @return JScrollPane
	 */
	public static JScrollPane getJTextArea(Model model, String field) {
		int len = model.getMetaModel().getColumnMetadata().get(field).getColumnSize();
		String val = model.getString(field);
		JScrollPane jsp = getJTextArea("tt" + field, val, len, 2);
		// jsp.setName(f);
		return jsp;
	}

	/**
	 * JtextArea estadar para datos de registros
	 * 
	 * @param r - registro
	 * @param f - nombre de la columna
	 * @param lin - Nro de lineas deseadas para el componente
	 * @return JScrollPane
	 */
	public static JScrollPane getJTextArea(Model model, String field, int lin) {
		int len = model.getMetaModel().getColumnMetadata().get(field).getColumnSize();
		String val = model.getString(field);
		JScrollPane jsp = getJTextArea("tt" + field, val, len, lin);
		// jsp.setName(f);
		return jsp;
	}

	/**
	 * retorna <code>JTextArea</code> con formato estandar
	 * 
	 * @param tt - id para tooltips
	 * @param val - Texto inicial para el componente
	 * @param col - columnas. las columnas seran dividias entre el Nro de lineas
	 * @param lin - Lineas. Nro de lines que se desean para el componentes
	 * @return JScrollPane
	 */
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
	 * @param cw - longitud del componente medido en caracteres
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
	/**
	 * barra de herramientas con formatos estandar
	 * 
	 * @return jtoolbar
	 * 
	 * @see {@link UIComponentPanel} for future toolbar implementation
	 */
	public static WebToolBar getJToolBar() {
		WebToolBar toolBar = new WebToolBar();
		// toolBar.setToolbarStyle(ToolbarStyle.attached);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		return toolBar;

	}

	/**
	 * return the ImageIcon <code>src</code> with a mark which is a scaled instance of the icon file name
	 * <code>mfn</code> draw over the source image.
	 * 
	 * @param src - original imagen
	 * @param mfn - icon file name used as mark
	 * @param h - Horizontal position of the mark. any of {@link SwingConstants#LEFT} or {@link SwingConstants#RIGHT}
	 * @param h - Vertical position of the mark. any of {@link SwingConstants#TOP} or {@link SwingConstants#BOTTOM}
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
	 * create and return an ImageIcon that is result of drawing background icon <code>bi</code> of request size
	 * <code>size</code> and and merging with the fornt icon <code>fi</code> with 0.6 of size
	 * 
	 * @param bi - background icon (big)
	 * @param fi - foreground Icon (small)
	 * @param size - return image size
	 * 
	 * @return merged icon
	 */
	public static ImageIcon getMergedIcon(String bi, String fi, int size) {
		// TODO: draw an oval before small icon to create contrast between images
		ImageIcon ii2 = TResources.getIcon(bi, size);
		ImageIcon ii = TResources.getIcon(fi, (int) (size * 0.6));
		return ImageUtils.mergeIcons(ii2, ii);
	}

	public static NumericTextField getNumericTextField(String field, Model model, Map<String, ColumnMetadata> columns) {
		ColumnMetadata column = columns.get(field);
		int len = column.getColumnSize();
		String val = model.getString(field);
		return getNumericTextField(field, val, len, null);
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
		return new ImageIcon(buildImage(unicode, 16, Color.BLACK));
	}

	/**
	 * create and return a {@link JScrollPane} setted with an instace of {@link SmartScroller}. this is intendet for
	 * console style componentes
	 * 
	 * @see SmartScroller
	 * @param component - component to scroll
	 * @return {@link JScrollPane} with standar {@link SmartScroller}
	 */
	public static JScrollPane getSmartScroller(JComponent component) {
		WebScrollPane pane = new WebScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		new SmartScroller(pane);
		return pane;
	}

	/**
	 * crea y retorna un componente informativo con formato estandar
	 * 
	 * @param rbid - id de resourceBundle
	 * @param inf - componente que contendra la informacion
	 * @return - Box
	 */
	public static JPanel getStandarInfoComponent(String rbid, Component inf) {
		JLabel jl = new JLabel(TStringUtils.getString(rbid) + ":");
		float inc = 2;
		Font fo = jl.getFont();
		fo = fo.deriveFont(fo.getSize() + inc);
		fo = fo.deriveFont(Font.BOLD);
		jl.setFont(fo);

		fo = inf.getFont();
		fo = fo.deriveFont(fo.getSize() + inc);
		inf.setFont(fo);

		JPanel ic = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ic.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		ic.add(jl);
		ic.add(Box.createHorizontalStrut(4));
		ic.add(inf);
		return ic;
	}

	public static WebToggleButton getStartPauseToggleButton(Action action, ActionListener listener) {
		WebToggleButton startPause = new WebToggleButton();
		startPause.setSelectedIcon(TUIUtils.getSmallFontIcon('\ue034'));
		if (action != null) {
			startPause.setAction(action);
			overRideToolBarButton(startPause);
		} else {
			startPause.setIcon(TUIUtils.getSmallFontIcon('\ue037'));
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
	 * crea y retorna un separador horizontal con un texto colocado hacia la izquierda
	 * 
	 * 20161123.04:25 NAAAA GUEBONAAA DE VIEJOOOOO !!! ESTE METODO DEBE TENER +10 AÑOS !!!! FUE DE LOS PRIMEROS PARA
	 * CLIO
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

	/**
	 * return a {@link WebLabel} whit html formated title/message elements
	 * 
	 * @see TStringUtils#getTitleText(String, String) TODO: temp move to look and feel
	 * 
	 * @param title
	 * @param message
	 * @return
	 */
	public static WebLabel getTitleLabel(String title, String message) {
		String msg = TStringUtils.getTitleText(title, message);
		return new WebLabel(msg);
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

	/**
	 * return a {@link JScrollPane} with a {@link TPropertyJTable} inside
	 * 
	 * @param tid - tooltip id
	 * @param prpl - propertis string in standar format
	 * @return Component 170911: MALDITO MABURRO con sus cadenas de mierdaaaa
	 */
	public static JScrollPane getTPropertyJTable(String tid, String prpl) {
		TPropertyJTable tpjt = new TPropertyJTable(prpl);
		setToolTip(tid, tpjt);
		JScrollPane js = new JScrollPane(tpjt);
		js.getViewport().setBackground(Color.WHITE);
		return js;
	}

	public static TWebComboBox getTWebComboBox(String name, Model model, String listId) {
		TEntry[] entries = TStringUtils.getTEntryGroup(listId);
		TWebComboBox jcbox = getTWebComboBox(name, entries, model.get(name));
		return jcbox;
	}

	/**
	 * Return a {@link TWebComboBox} with the standar configuration parameters
	 * 
	 * @param componentName - name of the compoment
	 * @param listId - prefix of the list elements stored in the app or plugins {@link ResourceBundle}
	 * 
	 * @return {@link TWebComboBox}
	 */
	public static TWebComboBox getTWebComboBox(String componentName, String listId) {
		TEntry[] entries = TStringUtils.getTEntryGroup(listId);
		return getTWebComboBox(componentName, entries, null);
	}

	/**
	 * build and return a {@link TWebComboBox} filled with the array of elements and selected element (if apply)
	 * 
	 * @param componentName - tha name for this component
	 * @param entries - array of elements
	 * @param selected - selected element. or <code>null</code>
	 * 
	 * @return ready to use {@link TWebComboBox}
	 */
	public static TWebComboBox getTWebComboBox(String componentName, TEntry[] entries, Object selected) {
		// si no hay datos, no selecciono nada
		int row = entries.length > 0 ? 0 : -1;

		for (int l = 0; l < entries.length; l++) {
			if (entries[l].getKey().equals(selected)) {
				row = l;
			}
		}
		TWebComboBox twcb = new TWebComboBox(entries);
		twcb.setSelectedIndex(row);
		twcb.setName(componentName);
		twcb.putClientProperty("settingsProcessor", new Configuration<ComboBoxState>(componentName));
		setToolTip(componentName, twcb);
		return twcb;
	}

	/**
	 * create and return and {@link WebButton} with all settings stablisehd for toolbar
	 * 
	 * @param taa - action to set in the button
	 * @return button ready to set as toolbar button
	 * @since 2.3
	 */
	public static WebButton getWebButtonForToolBar(Action action) {
		WebButton button = new WebButton(action);
		overRideToolBarButton(button);
		return button;
	}

	public static WebCheckBox getWebCheckBox(String name) {
		WebCheckBox jcb = new WebCheckBox(TStringUtils.getString(name));
		jcb.setName(name);
		setToolTip(name, jcb);
		jcb.putClientProperty("settingsProcessor", new Configuration<ButtonState>(name));
		return jcb;
	}

	public static WebCheckBoxList<TEntry> getWebCheckBox(String name, String group) {
		TEntry[] entries = TStringUtils.getTEntryGroup(group);
		CheckBoxListModel<TEntry> model = new CheckBoxListModel<>();
		for (TEntry tEntry : entries) {
			model.add(new CheckBoxCellData<TEntry>(tEntry));
		}
		WebCheckBoxList<TEntry> boxList = new WebCheckBoxList<>(model);
		boxList.setName(name);
		// TODO: i must write the procesor !!!O.o no tnk, not today
		// boxList.putClientProperty("settingsProcessor", new Configuration<list>(name));
		return boxList;

	}

	/**
	 * create and return {@link WebDateField} according to parameters
	 * 
	 * @param rcd - Record to obtain data
	 * @param fn - record field name
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
	 * create and return {@link WebDateField} according to parameters. the date format is dd/MM/yyy
	 * 
	 * @param tt - id for tooltips
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
	 * return a {@link WebTextField} with a trailing cancel button. The cancel button reroute the action performed to
	 * set a empty text on text component and notify the actionlister.
	 * 
	 * @param alist - action listener. listerner to notify when a change on the text component ocurr.
	 * 
	 * @return text field for search or filter
	 */
	public static WebTextField getWebFindField(final ActionListener alist) {
		final WebTextField findf = new WebTextField(20);
		// WebButton cancelbt = WebButton.createIconWebButton(TResourceUtils.getIcon("cancelAction", 14), 0);
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
		int len = column.getColumnSize();
		String field = column.getColumnName();
		Object val = model.get(field);
		return getWebFormattedTextField(field, val, len, null);
	}

	public static WebFormattedTextField getWebFormattedTextField(String field, Model model,
			Map<String, ColumnMetadata> columns) {
		ColumnMetadata metadata = columns.get(field);
		return getWebFormattedTextField(model, metadata);
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
	 * create and return and {@link WebToggleButton} with all settings stablisehd for toolbar
	 * 
	 * @param action - action to set in the button
	 * @return button ready to set as toolbar button
	 * @since 2.3
	 */
	public static WebToggleButton getWebToggleButton(Action action) {
		overRideIcons(16, null, action);
		WebToggleButton jb = new WebToggleButton(StyleId.togglebutton, action);
		// test: for toglebuttons, perform action performed over the action
		// jb.addItemListener(
		// evt -> ((AbstractButton) evt.getSource()).getAction().actionPerformed(new ActionEvent(jb, -1, "")));
		jb.setText(null);
		jb.setPreferredWidth(46);
		return jb;
	}

	/**
	 * Perform the initialization method p´for thise static class
	 * 
	 * @since 2.3
	 */
	public static void init() {
		try {
			List<File> fonts = FileUtils.findFilesRecursively(TResources.getCoreResourcePath(),
					file -> file.getName().endsWith(".ttf") || file.getName().endsWith(".otf"));
			for (File fontNam : fonts) {
				Font fo = Font.createFont(Font.TRUETYPE_FONT, fontNam);
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Utility method for override icons propertis setted in the {@link ApplicationAction} by bsf framework. This method
	 * <ul>
	 * <li>check the atributte <code>[action name].Action.iconFont</code>. if this attribute is present, set the
	 * {@link Action#SMALL_ICON} property for the action to this icon font
	 * <li>if iconFont atributte is not present, take the icon from the {@link Action#SMALL_ICON} and create an scaled
	 * instance using the size parameter. Repaint the icon to the target color toColor argument
	 * </ul>
	 * 
	 * @param size - new icon size
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
						ii = TColorUtils.changeColor(ii, toColor);
					}
					Image i = ii.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
					action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(i));
				}
			}
		}
	}

	public static void overRideToolBarButton(AbstractButton button) {
		overRideIcons(16, null, button.getAction());
		button.setRequestFocusEnabled(false);
		// TooltipManager.setTooltip(jb, (String) taa.getValue(TAbstractAction.SHORT_DESCRIPTION), TooltipWay.down);
		button.setText(null);
		button.setPreferredSize(new Dimension(46, 26));
	}

	/**
	 * establece dimenciones para los componentes. Si una instancia de <code>JTextField</code> sobrepasa las 30
	 * columnas, no se modifica el ancho ya que se asume que se ve mejor. ademas, si componente de texto es menor a las
	 * 5 colummas, se redondea a 5
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
		d.width = (int) ((col * 10) * 0.80); // % del tamaño
		jtc.setPreferredSize(d);
		jtc.setMaximumSize(d);
	}

	/**
	 * coloca un borde vacio (espacio) al rededor del componente.
	 * 
	 * @param comp - componente a colocar border
	 */
	public static void setEmptyBorder(JComponent comp) {
		comp.setBorder(new EmptyBorder(H_GAP, V_GAP, H_GAP, V_GAP));
	}

	/**
	 * Habilita/inhabilita los componentes cmps. Si alguno de estos es instancia de <code>Box o JPanel</code> se realiza
	 * la operacion a los componentes que contienen en forma recursiva.
	 * 
	 * @param cmps - componentes a habilitar/inhabilitar
	 * @param ena - =true habilitar, inhabilitar si =false
	 */
	public static void setEnabled(Component cnt, boolean ena) {
		Component[] cmps = (cnt instanceof Box || cnt instanceof JPanel)
				? cmps = ((Container) cnt).getComponents()
				: new Component[]{cnt};

		for (int e = 0; e < cmps.length; e++) {
			cmps[e].setEnabled(ena);
			if (cmps[e] instanceof Box || cmps[e] instanceof JPanel) {
				setEnabled(cmps[e], ena);
			}
			if (cmps[e] instanceof JScrollPane) {
				setEnabled(((JScrollPane) cmps[e]).getViewport().getView(), ena);
			}
		}
	}

	/**
	 * set the tooltip for the component. If the component is a instance of {@link ToolTipMethods}, the tooltip will be
	 * a {@link WebLookAndFeel} tooltip, else the tooltip will be a standar swing tooltip
	 * 
	 * @param name - the name for the resorurce bundle. this method will be append <code>.tt</code> to this name to look
	 *        for the tooltip
	 * @param cmp - the component
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

	/**
	 * build an Icon based on the unicode caracter.
	 * 
	 * @param text - the icon caracter
	 * @param font - source font where the icon font is.
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
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		label.print(g2d);
		g2d.dispose();
		return bufImage;
	}

	/**
	 * Construye y retorna <code>Box</code> con el par <code>JLabel(lab) JComponent</code> alineados segun los
	 * argumentos de entrada
	 * 
	 * @param lab - id en ResourceBundle para <code>JLabel</code>
	 * @parm jcomp - Componente de entrada
	 * @parm req - <code>true</code> si el componente es un campo de entrada obligatoria
	 * @parm ena - valor para metodo <code>setEnabled(ena)</code>
	 * @parm glue - si =true coloca Box.createHorizontalGlue() entre la etiqueta y el componente para que ambos esten
	 *       separados. de lo contrario, solo coloca Box.createHorizontalStrut(H_GAP)
	 */
	private static Box coupleInBox(String lab, JComponent jcom, boolean req, boolean ena, boolean glue) {
		Box b = Box.createHorizontalBox();
		JLabel jl = getJLabel(lab, req, ena);
		b.add(jl);
		b.add(Box.createHorizontalStrut(H_GAP));
		if (glue) {
			b.add(Box.createHorizontalGlue());
		}
		jl.setEnabled(ena);
		jcom.setEnabled(ena);
		b.add(jcom);
		if (!glue) {
			b.add(Box.createHorizontalGlue());
		}
		return b;
	}

	static int getStringPixelWidth(String str, Font font) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(str, null);
		return (int) bounds.getWidth();
	}

}
