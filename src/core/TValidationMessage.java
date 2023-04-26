package core;

import java.awt.*;
import java.io.*;
import java.text.*;

import javax.sound.sampled.*;
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

	private Color color, iconColor;
	private Severity severity;
	private String message;
	private char icon;
	private int miliSeconds;

	// private static final AudioClip newMsg = Applet.newAudioClip(TResources.getFile("newMsg.wav").toURL());
	// private static final AudioClip errMsg = Applet.newAudioClip(TResources.getFile("errMsg.wav").toURL());
	private static final File newMsg = TResources.getFile("newMsg.wav");
	private static final File errMsg = TResources.getFile("errMsg.wav");

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
			icon = '\ue87f';
			iconColor = color.darker();
		}
		if (a_m[0].equals("e")) {
			this.color = ERROR_COLOR;
			this.miliSeconds = FOR_EVER;
			severity = Severity.ERROR;
			icon = '\ue001';
			iconColor = color.darker();
		}
		if (a_m[0].equals("i")) {
			this.color = INFORMATION_COLOR;
			this.miliSeconds = SHORT;
			severity = Severity.OK;
			icon = '\ue88e';
			iconColor = color.darker();
		}
		if (a_m[0].equals("w")) {
			this.color = WARNING_COLOR;
			this.miliSeconds = A_WHILE;
			severity = Severity.WARNING;
			icon = '\ue8b2';
			iconColor = color.darker();
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
		return getIcon(14);
	}

	public Icon getIcon(int size) {
		return TUIUtils.getFontIcon(icon, size, iconColor);
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

	public Severity getSeverity() {
		return severity;
	}

	public void playSound() {
		try {
			File clipFile = (getSeverity() == Severity.INFO || getSeverity() == Severity.OK) ? newMsg : errMsg;
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(clipFile));
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
