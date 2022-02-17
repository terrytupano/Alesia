package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.html.*;

import com.alee.api.resource.*;
import com.alee.utils.*;

import core.*;

public class HTMLUtils {
	private static Font cachedTextFont;
	private static String cachedDefaultRule;

	public static void addDefaultStyleSheetRule(JEditorPane editorPane) {
		addStyleSheetRule(editorPane, getDefaultRule());
	}

	public static void addDefaultStyleSheetRule(JEditorPane editorPane, Font textFont) {
		addStyleSheetRule(editorPane, loadDefaultRule(textFont));
	}

	public static void addStyleSheetRule(JEditorPane editorPane, String rule) {
		HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule(rule);
	}

	public static Font getDefaultTextFont() {
		String fontKey = "Label.font";
		return UIManager.getFont(fontKey);
	}

	private static String getDefaultRule() {
		Font textFont = getDefaultTextFont();
		if (textFont != cachedTextFont) {
			cachedDefaultRule = loadDefaultRule(textFont);
			cachedTextFont = textFont;
		}
		return cachedDefaultRule;
	}

	private static String loadDefaultRule(Font font) {
		if (cachedDefaultRule == null) {
			cachedDefaultRule = FileUtils.readToString(TResources.getFile("Default.css"));
			cachedDefaultRule = cachedDefaultRule.replace("<default.font-family>", font.getFontName());
			cachedDefaultRule = cachedDefaultRule.replace("<default.font-size>", "" + font.getSize());
		}
		return cachedDefaultRule;
	}
}