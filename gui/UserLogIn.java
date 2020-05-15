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
package gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.alee.laf.checkbox.*;
import com.alee.laf.label.*;
import com.alee.laf.text.*;
import com.alee.managers.settings.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;

public class UserLogIn extends TUIFormPanel {

	/**
	 * nueva instancia
	 * 
	 * @param usr - registro de usuario
	 * 
	 */
	public UserLogIn() {
		showAditionalInformation(true);
		setTitle("UserLogIn.title");
		setDescription("UserLogIn.description");
		WebTextField usertf = TUIUtils.getWebTextField("UserLogIn.user", "", 30);
		WebPasswordField passf = TUIUtils.getWebPasswordField("UserLogIn.password", "", 30);
		WebCheckBox jcb = TUIUtils.getWebCheckBox("UserLogIn.rememberUser");
		// passf.setText("");
		addInputComponent(usertf, true, true);
		addInputComponent(passf, true, true);
		addInputComponent(jcb);

		registreSettings("UserLogIn.rememberUser");
		// recordar usuario
		if (jcb.isSelected()) {
			usertf.setText(SettingsManager.get("rememberUser", ""));
		}
		FormLayout layout = new FormLayout("left:max(40dlu;pref), 3dlu, 90dlu", // 1st major colum
				""); // add rows dynamically
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("UserLogIn.user"), getInputComponent("UserLogIn.user"));
		builder.nextLine();
		builder.append(TStringUtils.getString("UserLogIn.password"), getInputComponent("UserLogIn.password"));
		builder.nextLine();
		builder.append(jcb, 3);

		JPanel jp = new JPanel(new BorderLayout());
		jp.setBackground(Color.white);
		WebLabel wl = new WebLabel(TResources.getIcon("user_sponge_bob", 48));
		wl.setVerticalAlignment(JLabel.TOP);
		wl.setBorder(Borders.DIALOG);
		jp.add(wl, BorderLayout.WEST);
		jp.add(builder.getPanel(), BorderLayout.CENTER);
		setBodyComponent(jp);
		setFooterActions("acept", "cancel");
		preValidate();
	}

	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable<String, Object> ht = super.getValues();
		if (ht.get("UserLogIn.rememberUser").equals(Boolean.TRUE))
			SettingsManager.set("rememberUser", ht.get("UserLogIn.user"));
		return ht;
	}
}
