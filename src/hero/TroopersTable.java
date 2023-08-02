package hero;

import java.util.*;
import java.util.function.*;

import javax.swing.*;
import javax.swing.table.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.utils.swing.*;

import core.*;
import datasource.*;
import gui.*;

public class TroopersTable extends TUIListPanel {

	public TroopersTable() {
		setColumns("trooper", "client", "tau", "alpha", "strictPreflop", "reconnBase", "reconnBand", "takeOpportunity",
				"phi", "phi4");

		Function<String, List<Model>> function = (par -> TrooperParameter.findAll());
		setDBParameters(function, TrooperParameter.getMetaModel().getColumnMetadata());

		// trooper client
		TableColumn column = getTableColumn("client");
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.addItem("BasicBot");
		comboBox.addItem("ChenBasicBot");
		comboBox.addItem("DummyBot");
		comboBox.addItem("HeroBot");
		column.setCellEditor(new WebDefaultCellEditor<>(comboBox));

		setEditable(true);
//	getTable().	setPreferredWidth(700);
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}
}
