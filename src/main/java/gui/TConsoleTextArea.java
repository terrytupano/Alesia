package gui;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.alee.laf.text.*;

import core.*;

/**
 * Console stile {@link WebTextArea}.
 * <p>
 * NOTE: to correct control the scrolling, this method DONT set the preferedSize
 * 
 * @see #getSmartScroller(JComponent)
 * 
 */
public class TConsoleTextArea extends WebTextArea {

	private String pattern;

	public TConsoleTextArea() {
		this("%-20s %-50s");
	}

	public TConsoleTextArea(String pattern) {
		this.pattern = pattern;
		Font f = new Font("courier new", Font.PLAIN, 12);
		setFont(f);
		setLineWrap(false);
		setEditable(false);
	}

	public void print(Map<String, Object> map) {
		clear();
		map.forEach((key, value) -> print(key, value));
	}

	private List<String> headers = new ArrayList<>();

	public void setHeader(List<String> headers) {
		this.headers = headers;
	}

	public void print(String key, Object value) {
		String value2 = value.toString();
//		if (value instanceof Boolean) {
//			boolean boolVal = ((Boolean) value).booleanValue();
//			value2 = !boolVal ? "<b>" + boolVal + "</b>" : ""+ boolVal;
//		}
		if (value instanceof Double)
			value2 = TResources.fourDigitFormat.format((Double) value);

//		if (value instanceof List<?>) {
//			List<?> list = (List<?>) value;
//			value2 = StringUtils.join(list, ", ");
//		}

		// remove headers
		String key2 = key;
		for (String header : headers) {
			if (key.startsWith(header))
				key2 = key.replace(header, "");
		}
		append("\n" + String.format(pattern, key2, value2));
	}

	public void print(Properties properties) {
		TreeMap<String, Object> treeMap = new TreeMap<>();
		properties.forEach((k, v) -> treeMap.put(k.toString(), v));
		print(treeMap);
	}
}
