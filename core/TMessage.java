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
import java.text.*;
import java.util.*;

import javax.swing.*;

/**
 * Encapsulate erros in the framework. Any instance of this class represent an error message whit correspondient visual
 * attributes like color of the error, type and icons
 * 
 * @author terry
 *
 */
public class TMessage {
	public static final String ACTION = "action";
	public static final String ERROR = "error";
	public static final String INFORMATION = "information";
	public static final String WARNING = "warning";

	public static final Color ACTION_COLOR = new Color(190, 220, 250);
	public static final Color ERROR_COLOR = new Color(255, 190, 230);
	public static final Color INFORMATION_COLOR = new Color(230, 230, 255);
	public static final Color WARNING_COLOR = new Color(255, 255, 220);

	public static final int SHORT = 2000;
	public static final int A_WHILE = 10000;
	public static final int FOR_EVER = 999000;

	private Color exceptionColor;
	private int milis;
	private Icon exceptionIcon;
	private String message;
	private String eType;

	public TMessage(String mid) {
		super();
		String[] a_m = TStringUtils.getString(mid).split(";");
		if (a_m.length < 2) {
			throw new NoSuchElementException("Error trying get exception ID " + mid);
		}
		if (a_m[0].equals("a")) {
			eType = ACTION;
			this.exceptionColor = ACTION_COLOR;
			this.milis = FOR_EVER;
			this.exceptionIcon = TUIUtils.getFontIcon('\ue87f', 32, ACTION_COLOR);
		}
		if (a_m[0].equals("e")) {
			eType = ERROR;
			this.exceptionColor = ERROR_COLOR;
			this.milis = FOR_EVER;
			this.exceptionIcon = TUIUtils.getFontIcon('\ue001', 32, ERROR_COLOR);
		}
		if (a_m[0].equals("i")) {
			eType = INFORMATION;
			this.exceptionColor = INFORMATION_COLOR;
			this.milis = SHORT;
			this.exceptionIcon = TUIUtils.getFontIcon('\ue88e', 32, INFORMATION_COLOR);
		}
		if (a_m[0].equals("w")) {
			eType = WARNING;
			this.exceptionColor = WARNING_COLOR;
			this.milis = A_WHILE;
			this.exceptionIcon = TUIUtils.getFontIcon('\ue8b2', 32, WARNING_COLOR);
		}
		this.message = a_m[1];
	}

	/**
	 * nueva instancia
	 * 
	 * @param msgID - identificador de mensaje.
	 * @param msgData - datos para ejecutar
	 */
	public TMessage(String msgID, Object... msgData) {
		this(msgID);
		message = MessageFormat.format(message, msgData);
	}

	/**
	 * Retorna el color asociado al tipo de excepcion
	 * 
	 * @return color
	 */
	public Color getExceptionColor() {
		return exceptionColor;
	}

	/**
	 * retorna icono para esta excepcion
	 * 
	 * @return icono
	 */
	public Icon getIcon() {
		return exceptionIcon;
	}

	/**
	 * retorna el tipo de esta exepcion.
	 * 
	 * @return tipo de exepcion
	 */
	public String getExceptionType() {
		return eType;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Establece un nuevo mensaje de texto para esta exception
	 * 
	 * @param msg - mensaje
	 */
	public void setMessage(String msg) {
		this.message = msg;
	}

	/**
	 * retorna el tiempo que se espera se muestre esta exepcion.
	 * 
	 * @return tiempo en seg.
	 */
	public int getMiliSeconds() {
		return milis;
	}
}
