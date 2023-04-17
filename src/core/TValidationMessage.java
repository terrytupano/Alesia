package core;

import java.awt.*;
import java.text.*;

import javax.swing.*;

import com.jgoodies.validation.*;

public class TValidationMessage implements ValidationMessage {

	public static final Color ACTION_COLOR = new Color(190, 220, 250);
	public static final Color ERROR_COLOR = new Color(255, 190, 230);
	public static final Color INFORMATION_COLOR = new Color(230, 230, 255);
	public static final Color WARNING_COLOR = new Color(255, 255, 220);

	public static final int SHORT = 2000;
	public static final int A_WHILE = 10000;
	public static final int FOR_EVER = 999000;

	private Color color;
	private Severity severity;
	private String message;
	private Icon icon;
	private int miliSeconds;

	public TValidationMessage(String messageId) {
		String[] a_m = TStringUtils.getString(messageId).split(";");
		if (a_m.length < 2) {
			throw new IllegalArgumentException("Error trying get exception ID " + messageId);
		}
		this.message = a_m[1];

		if (a_m[0].equals("a")) {
			this.color = ACTION_COLOR;
			this.miliSeconds = FOR_EVER;
			severity = Severity.INFO;
			this.icon = TUIUtils.getFontIcon('\ue87f', 14, ACTION_COLOR.darker());
		}
		if (a_m[0].equals("e")) {
			this.color = ERROR_COLOR;
			this.miliSeconds = FOR_EVER;
			severity = Severity.ERROR;
			this.icon = TUIUtils.getFontIcon('\ue001', 14, ERROR_COLOR.darker());
		}
		if (a_m[0].equals("i")) {
			this.color = INFORMATION_COLOR;
			this.miliSeconds = SHORT;
			severity = Severity.OK;
			this.icon = TUIUtils.getFontIcon('\ue88e', 14, INFORMATION_COLOR.darker());
		}
		if (a_m[0].equals("w")) {
			this.color = WARNING_COLOR;
			this.miliSeconds = A_WHILE;
			severity = Severity.WARNING;
			this.icon = TUIUtils.getFontIcon('\ue8b2', 14, WARNING_COLOR.darker());
		}
	}

	public TValidationMessage(String messageId, Object... msgData) {
		this(messageId);
		message = MessageFormat.format(message, msgData);
	}

	@Override
	public String formattedText() {
		return message;
	}

	@Override
	public Object key() {
		return null;
	}

	@Override
	public Severity severity() {
		return severity;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	/**
	 * Retorna el color asociado al tipo de excepcion
	 * 
	 * @return color
	 */
	public Color getColor() {
		return color;
	}
	/**
	 * retorna el tiempo que se espera se muestre esta exepcion.
	 * 
	 * @return tiempo en seg.
	 */
	public int getMiliSeconds() {
		return miliSeconds;
	}
}
