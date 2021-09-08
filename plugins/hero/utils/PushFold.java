package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

public class PushFold {
	// User selections
	private String selectedHand;

	// Other declarations
	private boolean callMode;
	private CardArray cardArray;
	private ArrayList<String> rangeArray = new ArrayList<String>();
	private JLabel lblXx;
	private JLabel lblPushRange;
	private JLabel lblPushRangeText;
	private JLabel lblPushFold;
	private JFrame frmPushFoldCalculator;
	private JRadioButton rdbtnPushModehu;
	private JRadioButton rdbtnCallModehu;
	private JRadioButton radioButtonFullRing;
	private JRadioButton radioButtonMax;
	private JRadioButton radioButtonHeadsUp;
	private JRadioButton radioButton_players_1;
	private JRadioButton radioButton_players_2;
	private JRadioButton radioButton_players_3;
	private JRadioButton radioButton_players_4;
	private JRadioButton radioButton_players_5;
	private JRadioButton radioButton_players_6;
	private JRadioButton radioButton_players_7;
	private JRadioButton radioButton_players_8;
	private JCheckBox checkboxAntes;
	private JCheckBox checkboxHighlightRange;
	private JSlider slider;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();

	private JButton btnAa;
	private JButton btnAks;
	private JButton btnAjo_1;
	private JButton btnAqs;
	private JButton btnAto_1;
	private JButton btnAo_10;
	private JButton btnAo_9;
	private JButton btnAo_8;
	private JButton btnAo_15;
	private JButton btnAo_14;
	private JButton btnAo_13;
	private JButton btnAo_12;
	private JButton btnAo_11;
	private JButton btnAko;
	private JButton btnKk;
	private JButton btnKjs;
	private JButton button_15;
	private JButton btnKts;
	private JButton btnKs_2;
	private JButton btnKs_1;
	private JButton btnKs;
	private JButton btnKs_7;
	private JButton btnKs_6;
	private JButton btnKs_5;
	private JButton btnKs_4;
	private JButton btnKs_3;
	private JButton btnAqo;
	private JButton btnKqo;
	private JButton btnQjs;
	private JButton btnQq;
	private JButton btnQts;
	private JButton btnQs_2;
	private JButton btnQs_1;
	private JButton btnQs;
	private JButton btnQs_7;
	private JButton btnQs_6;
	private JButton btnQs_5;
	private JButton btnQs_4;
	private JButton btnQs_3;
	private JButton btnAjo;
	private JButton btnKjo;
	private JButton btnJj;
	private JButton btnQjo;
	private JButton btnJts;
	private JButton btnJs_2;
	private JButton btnJs_1;
	private JButton btnJs;
	private JButton btnJs_7;
	private JButton btnJs_6;
	private JButton btnJs_5;
	private JButton btnJs_4;
	private JButton btnJs_3;
	private JButton btnAto;
	private JButton btnKto;
	private JButton btnJto;
	private JButton btnQto;
	private JButton btnTt;
	private JButton btnTs_2;
	private JButton btnTs_1;
	private JButton btnTs;
	private JButton btnTs_7;
	private JButton btnTs_6;
	private JButton btnTs_5;
	private JButton btnTs_4;
	private JButton btnTs_3;
	private JButton btnAo;
	private JButton btnKo;
	private JButton btnJo;
	private JButton btnQo;
	private JButton btnTo;
	private JButton btns_1;
	private JButton btns;
	private JButton button_71;
	private JButton btns_6;
	private JButton btns_5;
	private JButton btns_4;
	private JButton btns_3;
	private JButton btns_2;
	private JButton btnAo_1;
	private JButton btnKo_1;
	private JButton btnJo_1;
	private JButton btnQo_1;
	private JButton btnTto;
	private JButton btns_7;
	private JButton button_83;
	private JButton btno;
	private JButton btns_12;
	private JButton btns_11;
	private JButton btns_10;
	private JButton btns_9;
	private JButton btns_8;
	private JButton btnAo_2;
	private JButton btnKo_2;
	private JButton btnJo_2;
	private JButton btnQo_2;
	private JButton btnTo_1;
	private JButton button_95;
	private JButton btno_7;
	private JButton btno_1;
	private JButton btns_17;
	private JButton btns_16;
	private JButton btns_15;
	private JButton btns_14;
	private JButton btns_13;
	private JButton btnAo_3;
	private JButton btnKo_3;
	private JButton btnJo_3;
	private JButton btnQo_3;
	private JButton btnTo_2;
	private JButton btno_13;
	private JButton btno_8;
	private JButton btno_2;
	private JButton btns_21;
	private JButton btns_20;
	private JButton btns_19;
	private JButton btns_18;
	private JButton button_115;
	private JButton btnAo_4;
	private JButton btnKo_4;
	private JButton btnJo_4;
	private JButton btnQo_4;
	private JButton btnTo_3;
	private JButton btno_14;
	private JButton btno_9;
	private JButton btno_3;
	private JButton btns_24;
	private JButton btns_23;
	private JButton btns_22;
	private JButton button_127;
	private JButton btno_18;
	private JButton btnAo_5;
	private JButton btnKo_5;
	private JButton btnJo_5;
	private JButton btnQo_5;
	private JButton btnTo_4;
	private JButton btno_15;
	private JButton btno_10;
	private JButton btno_4;
	private JButton btns_26;
	private JButton btns_25;
	private JButton button_139;
	private JButton btno_22;
	private JButton btno_19;
	private JButton btnAo_6;
	private JButton btnKo_6;
	private JButton btnJo_6;
	private JButton btnQo_6;
	private JButton btnTo_5;
	private JButton btno_16;
	private JButton btno_11;
	private JButton btno_5;
	private JButton btns_27;
	private JButton button_151;
	private JButton btno_25;
	private JButton btno_23;
	private JButton btno_20;
	private JButton btnAo_7;
	private JButton btnKo_7;
	private JButton btnJo_7;
	private JButton btnQo_7;
	private JButton btnTo_6;
	private JButton btno_17;
	private JButton btno_12;
	private JButton btno_6;
	private JButton button_163;
	private JButton btno_27;
	private JButton btno_26;
	private JButton btno_24;
	private JButton btno_21;

	private ArrayList<JButton> handButtonArray = new ArrayList<JButton>();

	public static void main(String[] args) {
		PushFold window = new PushFold();
		window.frmPushFoldCalculator.setVisible(true);
	}

	public PushFold() {
		initialize();
	}

	private void initialize() {
		selectedHand = "AA";

		cardArray = new CardArray();
		frmPushFoldCalculator = new JFrame();
		frmPushFoldCalculator.setTitle("Push Fold Calculator");
		frmPushFoldCalculator.getContentPane().setBackground(new Color(250, 240, 230));
		frmPushFoldCalculator.setBounds(100, 100, 894, 733);
		frmPushFoldCalculator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPushFoldCalculator.setLocationRelativeTo(null);
		frmPushFoldCalculator.getContentPane().setLayout(null);

		btnAa = new JButton("AA");
		btnAa.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAa.setBackground(new Color(50, 205, 50));
		btnAa.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAa.setBounds(10, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAa);

		btnAks = new JButton("AKs");
		btnAks.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAks.setBackground(new Color(220, 20, 60));
		btnAks.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAks.setBounds(76, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAks);

		btnAjo_1 = new JButton("AJs");
		btnAjo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAjo_1.setBackground(new Color(220, 20, 60));
		btnAjo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAjo_1.setBounds(208, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAjo_1);

		btnAqs = new JButton("AQs");
		btnAqs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAqs.setBackground(new Color(220, 20, 60));
		btnAqs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAqs.setBounds(142, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAqs);

		btnAto_1 = new JButton("ATs");
		btnAto_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAto_1.setBackground(new Color(220, 20, 60));
		btnAto_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAto_1.setBounds(274, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAto_1);

		btnAo_10 = new JButton("A7s");
		btnAo_10.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_10.setBackground(new Color(220, 20, 60));
		btnAo_10.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_10.setBounds(472, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_10);

		btnAo_9 = new JButton("A8s");
		btnAo_9.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_9.setBackground(new Color(220, 20, 60));
		btnAo_9.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_9.setBounds(407, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_9);

		btnAo_8 = new JButton("A9s");
		btnAo_8.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_8.setBackground(new Color(220, 20, 60));
		btnAo_8.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_8.setBounds(341, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_8);

		btnAo_15 = new JButton("A2s");
		btnAo_15.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_15.setBackground(new Color(220, 20, 60));
		btnAo_15.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_15.setBounds(802, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_15);

		btnAo_14 = new JButton("A3s");
		btnAo_14.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_14.setBackground(new Color(220, 20, 60));
		btnAo_14.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_14.setBounds(737, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_14);

		btnAo_13 = new JButton("A4s");
		btnAo_13.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_13.setBackground(new Color(220, 20, 60));
		btnAo_13.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_13.setBounds(671, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_13);

		btnAo_12 = new JButton("A5s");
		btnAo_12.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_12.setBackground(new Color(220, 20, 60));
		btnAo_12.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_12.setBounds(604, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_12);

		btnAo_11 = new JButton("A6s");
		btnAo_11.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_11.setBackground(new Color(220, 20, 60));
		btnAo_11.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_11.setBounds(538, 39, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_11);

		btnAko = new JButton("AKo");
		btnAko.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAko.setBackground(new Color(100, 149, 237));
		btnAko.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAko.setBounds(10, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAko);

		btnKk = new JButton("KK");
		btnKk.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKk.setBackground(new Color(50, 205, 50));
		btnKk.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKk.setBounds(76, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKk);

		btnKjs = new JButton("KJs");
		btnKjs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKjs.setBackground(new Color(220, 20, 60));
		btnKjs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKjs.setBounds(208, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKjs);

		button_15 = new JButton("KQs");
		button_15.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_15.setBackground(new Color(220, 20, 60));
		button_15.setFont(new Font("Calibri", Font.BOLD, 12));
		button_15.setBounds(142, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_15);

		btnKts = new JButton("KTs");
		btnKts.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKts.setBackground(new Color(220, 20, 60));
		btnKts.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKts.setBounds(274, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKts);

		btnKs_2 = new JButton("K7s");
		btnKs_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_2.setBackground(new Color(220, 20, 60));
		btnKs_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_2.setBounds(472, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_2);

		btnKs_1 = new JButton("K8s");
		btnKs_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_1.setBackground(new Color(220, 20, 60));
		btnKs_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_1.setBounds(407, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_1);

		btnKs = new JButton("K9s");
		btnKs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs.setBackground(new Color(220, 20, 60));
		btnKs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs.setBounds(341, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs);

		btnKs_7 = new JButton("K2s");
		btnKs_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_7.setBackground(new Color(220, 20, 60));
		btnKs_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_7.setBounds(802, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_7);

		btnKs_6 = new JButton("K3s");
		btnKs_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_6.setBackground(new Color(220, 20, 60));
		btnKs_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_6.setBounds(737, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_6);

		btnKs_5 = new JButton("K4s");
		btnKs_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_5.setBackground(new Color(220, 20, 60));
		btnKs_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_5.setBounds(671, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_5);

		btnKs_4 = new JButton("K5s");
		btnKs_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_4.setBackground(new Color(220, 20, 60));
		btnKs_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_4.setBounds(604, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_4);

		btnKs_3 = new JButton("K6s");
		btnKs_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKs_3.setBackground(new Color(220, 20, 60));
		btnKs_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKs_3.setBounds(538, 76, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKs_3);

		btnAqo = new JButton("AQo");
		btnAqo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAqo.setBackground(new Color(100, 149, 237));
		btnAqo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAqo.setBounds(10, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAqo);

		btnKqo = new JButton("KQo");
		btnKqo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKqo.setBackground(new Color(100, 149, 237));
		btnKqo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKqo.setBounds(76, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKqo);

		btnQjs = new JButton("QJs");
		btnQjs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQjs.setBackground(new Color(220, 20, 60));
		btnQjs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQjs.setBounds(208, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQjs);

		btnQq = new JButton("QQ");
		btnQq.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQq.setBackground(new Color(50, 205, 50));
		btnQq.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQq.setBounds(142, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQq);

		btnQts = new JButton("QTs");
		btnQts.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQts.setBackground(new Color(220, 20, 60));
		btnQts.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQts.setBounds(274, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQts);

		btnQs_2 = new JButton("Q7s");
		btnQs_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_2.setBackground(new Color(220, 20, 60));
		btnQs_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_2.setBounds(472, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_2);

		btnQs_1 = new JButton("Q8s");
		btnQs_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_1.setBackground(new Color(220, 20, 60));
		btnQs_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_1.setBounds(407, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_1);

		btnQs = new JButton("Q9s");
		btnQs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs.setBackground(new Color(220, 20, 60));
		btnQs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs.setBounds(341, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs);

		btnQs_7 = new JButton("Q2s");
		btnQs_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_7.setBackground(new Color(220, 20, 60));
		btnQs_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_7.setBounds(802, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_7);

		btnQs_6 = new JButton("Q3s");
		btnQs_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_6.setBackground(new Color(220, 20, 60));
		btnQs_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_6.setBounds(737, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_6);

		btnQs_5 = new JButton("Q4s");
		btnQs_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_5.setBackground(new Color(220, 20, 60));
		btnQs_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_5.setBounds(671, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_5);

		btnQs_4 = new JButton("Q5s");
		btnQs_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_4.setBackground(new Color(220, 20, 60));
		btnQs_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_4.setBounds(604, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_4);

		btnQs_3 = new JButton("Q6s");
		btnQs_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQs_3.setBackground(new Color(220, 20, 60));
		btnQs_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQs_3.setBounds(538, 114, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQs_3);

		btnAjo = new JButton("AJo");
		btnAjo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAjo.setBackground(new Color(100, 149, 237));
		btnAjo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAjo.setBounds(10, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAjo);

		btnKjo = new JButton("KJo");
		btnKjo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKjo.setBackground(new Color(100, 149, 237));
		btnKjo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKjo.setBounds(76, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKjo);

		btnJj = new JButton("JJ");
		btnJj.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJj.setBackground(new Color(50, 205, 50));
		btnJj.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJj.setBounds(208, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJj);

		btnQjo = new JButton("QJo");
		btnQjo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQjo.setBackground(new Color(100, 149, 237));
		btnQjo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQjo.setBounds(142, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQjo);

		btnJts = new JButton("JTs");
		btnJts.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJts.setBackground(new Color(220, 20, 60));
		btnJts.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJts.setBounds(274, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJts);

		btnJs_2 = new JButton("J7s");
		btnJs_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_2.setBackground(new Color(220, 20, 60));
		btnJs_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_2.setBounds(472, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_2);

		btnJs_1 = new JButton("J8s");
		btnJs_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_1.setBackground(new Color(220, 20, 60));
		btnJs_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_1.setBounds(407, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_1);

		btnJs = new JButton("J9s");
		btnJs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs.setBackground(new Color(220, 20, 60));
		btnJs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs.setBounds(341, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs);

		btnJs_7 = new JButton("J2s");
		btnJs_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_7.setBackground(new Color(220, 20, 60));
		btnJs_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_7.setBounds(802, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_7);

		btnJs_6 = new JButton("J3s");
		btnJs_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_6.setBackground(new Color(220, 20, 60));
		btnJs_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_6.setBounds(737, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_6);

		btnJs_5 = new JButton("J4s");
		btnJs_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_5.setBackground(new Color(220, 20, 60));
		btnJs_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_5.setBounds(671, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_5);

		btnJs_4 = new JButton("J5s");
		btnJs_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_4.setBackground(new Color(220, 20, 60));
		btnJs_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_4.setBounds(604, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_4);

		btnJs_3 = new JButton("J6s");
		btnJs_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJs_3.setBackground(new Color(220, 20, 60));
		btnJs_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJs_3.setBounds(538, 151, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJs_3);

		btnAto = new JButton("ATo");
		btnAto.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAto.setBackground(new Color(100, 149, 237));
		btnAto.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAto.setBounds(10, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAto);

		btnKto = new JButton("KTo");
		btnKto.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKto.setBackground(new Color(100, 149, 237));
		btnKto.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKto.setBounds(76, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKto);

		btnJto = new JButton("JTo");
		btnJto.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJto.setBackground(new Color(100, 149, 237));
		btnJto.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJto.setBounds(208, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJto);

		btnQto = new JButton("QTo");
		btnQto.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQto.setBackground(new Color(100, 149, 237));
		btnQto.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQto.setBounds(142, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQto);

		btnTt = new JButton("TT");
		btnTt.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTt.setBackground(new Color(50, 205, 50));
		btnTt.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTt.setBounds(274, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTt);

		btnTs_2 = new JButton("T7s");
		btnTs_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_2.setBackground(new Color(220, 20, 60));
		btnTs_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_2.setBounds(472, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_2);

		btnTs_1 = new JButton("T8s");
		btnTs_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_1.setBackground(new Color(220, 20, 60));
		btnTs_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_1.setBounds(407, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_1);

		btnTs = new JButton("T9s");
		btnTs.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs.setBackground(new Color(220, 20, 60));
		btnTs.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs.setBounds(341, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs);

		btnTs_7 = new JButton("T2s");
		btnTs_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_7.setBackground(new Color(220, 20, 60));
		btnTs_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_7.setBounds(802, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_7);

		btnTs_6 = new JButton("T3s");
		btnTs_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_6.setBackground(new Color(220, 20, 60));
		btnTs_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_6.setBounds(737, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_6);

		btnTs_5 = new JButton("T4s");
		btnTs_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_5.setBackground(new Color(220, 20, 60));
		btnTs_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_5.setBounds(671, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_5);

		btnTs_4 = new JButton("T5s");
		btnTs_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_4.setBackground(new Color(220, 20, 60));
		btnTs_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_4.setBounds(604, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_4);

		btnTs_3 = new JButton("T6s");
		btnTs_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTs_3.setBackground(new Color(220, 20, 60));
		btnTs_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTs_3.setBounds(538, 188, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTs_3);

		btnAo = new JButton("A9o");
		btnAo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo.setBackground(new Color(100, 149, 237));
		btnAo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo.setBounds(10, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo);

		btnKo = new JButton("K9o");
		btnKo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo.setBackground(new Color(100, 149, 237));
		btnKo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo.setBounds(76, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo);

		btnJo = new JButton("J9o");
		btnJo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo.setBackground(new Color(100, 149, 237));
		btnJo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo.setBounds(208, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo);

		btnQo = new JButton("Q9o");
		btnQo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo.setBackground(new Color(100, 149, 237));
		btnQo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo.setBounds(142, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo);

		btnTo = new JButton("T9o");
		btnTo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo.setBackground(new Color(100, 149, 237));
		btnTo.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo.setBounds(274, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo);

		btns_1 = new JButton("97s");
		btns_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_1.setBackground(new Color(220, 20, 60));
		btns_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_1.setBounds(472, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_1);

		btns = new JButton("98s");
		btns.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns.setBackground(new Color(220, 20, 60));
		btns.setFont(new Font("Calibri", Font.BOLD, 12));
		btns.setBounds(407, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns);

		button_71 = new JButton("99");
		button_71.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_71.setBackground(new Color(50, 205, 50));
		button_71.setFont(new Font("Calibri", Font.BOLD, 12));
		button_71.setBounds(341, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_71);

		btns_6 = new JButton("92s");
		btns_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_6.setBackground(new Color(220, 20, 60));
		btns_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_6.setBounds(802, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_6);

		btns_5 = new JButton("93s");
		btns_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_5.setBackground(new Color(220, 20, 60));
		btns_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_5.setBounds(737, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_5);

		btns_4 = new JButton("94s");
		btns_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_4.setBackground(new Color(220, 20, 60));
		btns_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_4.setBounds(671, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_4);

		btns_3 = new JButton("95s");
		btns_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_3.setBackground(new Color(220, 20, 60));
		btns_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_3.setBounds(604, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_3);

		btns_2 = new JButton("96s");
		btns_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_2.setBackground(new Color(220, 20, 60));
		btns_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_2.setBounds(538, 225, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_2);

		btnAo_1 = new JButton("A8o");
		btnAo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_1.setBackground(new Color(100, 149, 237));
		btnAo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_1.setBounds(10, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_1);

		btnKo_1 = new JButton("K8o");
		btnKo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_1.setBackground(new Color(100, 149, 237));
		btnKo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_1.setBounds(76, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_1);

		btnJo_1 = new JButton("J8o");
		btnJo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_1.setBackground(new Color(100, 149, 237));
		btnJo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_1.setBounds(208, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_1);

		btnQo_1 = new JButton("Q8o");
		btnQo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_1.setBackground(new Color(100, 149, 237));
		btnQo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_1.setBounds(142, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_1);

		btnTto = new JButton("T8o");
		btnTto.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTto.setBackground(new Color(100, 149, 237));
		btnTto.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTto.setBounds(274, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTto);

		btns_7 = new JButton("87s");
		btns_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_7.setBackground(new Color(220, 20, 60));
		btns_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_7.setBounds(472, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_7);

		button_83 = new JButton("88");
		button_83.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_83.setBackground(new Color(50, 205, 50));
		button_83.setFont(new Font("Calibri", Font.BOLD, 12));
		button_83.setBounds(407, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_83);

		btno = new JButton("98o");
		btno.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno.setBackground(new Color(100, 149, 237));
		btno.setFont(new Font("Calibri", Font.BOLD, 12));
		btno.setBounds(341, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno);

		btns_12 = new JButton("82s");
		btns_12.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_12.setBackground(new Color(220, 20, 60));
		btns_12.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_12.setBounds(802, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_12);

		btns_11 = new JButton("83s");
		btns_11.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_11.setBackground(new Color(220, 20, 60));
		btns_11.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_11.setBounds(737, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_11);

		btns_10 = new JButton("84s");
		btns_10.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_10.setBackground(new Color(220, 20, 60));
		btns_10.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_10.setBounds(671, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_10);

		btns_9 = new JButton("85s");
		btns_9.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_9.setBackground(new Color(220, 20, 60));
		btns_9.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_9.setBounds(604, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_9);

		btns_8 = new JButton("86s");
		btns_8.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_8.setBackground(new Color(220, 20, 60));
		btns_8.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_8.setBounds(538, 263, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_8);

		btnAo_2 = new JButton("A7o");
		btnAo_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_2.setBackground(new Color(100, 149, 237));
		btnAo_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_2.setBounds(10, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_2);

		btnKo_2 = new JButton("K7o");
		btnKo_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_2.setBackground(new Color(100, 149, 237));
		btnKo_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_2.setBounds(76, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_2);

		btnJo_2 = new JButton("J7o");
		btnJo_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_2.setBackground(new Color(100, 149, 237));
		btnJo_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_2.setBounds(208, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_2);

		btnQo_2 = new JButton("Q7o");
		btnQo_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_2.setBackground(new Color(100, 149, 237));
		btnQo_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_2.setBounds(142, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_2);

		btnTo_1 = new JButton("T7o");
		btnTo_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_1.setBackground(new Color(100, 149, 237));
		btnTo_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_1.setBounds(274, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_1);

		button_95 = new JButton("77");
		button_95.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_95.setBackground(new Color(50, 205, 50));
		button_95.setFont(new Font("Calibri", Font.BOLD, 12));
		button_95.setBounds(472, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_95);

		btno_7 = new JButton("87o");
		btno_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_7.setBackground(new Color(100, 149, 237));
		btno_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_7.setBounds(407, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_7);

		btno_1 = new JButton("97o");
		btno_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_1.setBackground(new Color(100, 149, 237));
		btno_1.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_1.setBounds(341, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_1);

		btns_17 = new JButton("72s");
		btns_17.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_17.setBackground(new Color(220, 20, 60));
		btns_17.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_17.setBounds(802, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_17);

		btns_16 = new JButton("73s");
		btns_16.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_16.setBackground(new Color(220, 20, 60));
		btns_16.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_16.setBounds(737, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_16);

		btns_15 = new JButton("74s");
		btns_15.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_15.setBackground(new Color(220, 20, 60));
		btns_15.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_15.setBounds(671, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_15);

		btns_14 = new JButton("75s");
		btns_14.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_14.setBackground(new Color(220, 20, 60));
		btns_14.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_14.setBounds(604, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_14);

		btns_13 = new JButton("76s");
		btns_13.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_13.setBackground(new Color(220, 20, 60));
		btns_13.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_13.setBounds(538, 300, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_13);

		btnAo_3 = new JButton("A6o");
		btnAo_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_3.setBackground(new Color(100, 149, 237));
		btnAo_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_3.setBounds(10, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_3);

		btnKo_3 = new JButton("K6o");
		btnKo_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_3.setBackground(new Color(100, 149, 237));
		btnKo_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_3.setBounds(76, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_3);

		btnJo_3 = new JButton("J6o");
		btnJo_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_3.setBackground(new Color(100, 149, 237));
		btnJo_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_3.setBounds(208, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_3);

		btnQo_3 = new JButton("Q6o");
		btnQo_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_3.setBackground(new Color(100, 149, 237));
		btnQo_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_3.setBounds(142, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_3);

		btnTo_2 = new JButton("T6o");
		btnTo_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_2.setBackground(new Color(100, 149, 237));
		btnTo_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_2.setBounds(274, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_2);

		btno_13 = new JButton("76o");
		btno_13.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_13.setBackground(new Color(100, 149, 237));
		btno_13.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_13.setBounds(472, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_13);

		btno_8 = new JButton("86o");
		btno_8.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_8.setBackground(new Color(100, 149, 237));
		btno_8.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_8.setBounds(407, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_8);

		btno_2 = new JButton("96o");
		btno_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_2.setBackground(new Color(100, 149, 237));
		btno_2.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_2.setBounds(341, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_2);

		btns_21 = new JButton("62s");
		btns_21.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_21.setBackground(new Color(220, 20, 60));
		btns_21.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_21.setBounds(802, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_21);

		btns_20 = new JButton("63s");
		btns_20.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_20.setBackground(new Color(220, 20, 60));
		btns_20.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_20.setBounds(737, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_20);

		btns_19 = new JButton("64s");
		btns_19.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_19.setBackground(new Color(220, 20, 60));
		btns_19.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_19.setBounds(671, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_19);

		btns_18 = new JButton("65s");
		btns_18.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_18.setBackground(new Color(220, 20, 60));
		btns_18.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_18.setBounds(604, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_18);

		button_115 = new JButton("66");
		button_115.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_115.setBackground(new Color(50, 205, 50));
		button_115.setFont(new Font("Calibri", Font.BOLD, 12));
		button_115.setBounds(538, 337, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_115);

		btnAo_4 = new JButton("A5o");
		btnAo_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_4.setBackground(new Color(100, 149, 237));
		btnAo_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_4.setBounds(10, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_4);

		btnKo_4 = new JButton("K5o");
		btnKo_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_4.setBackground(new Color(100, 149, 237));
		btnKo_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_4.setBounds(76, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_4);

		btnJo_4 = new JButton("J5o");
		btnJo_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_4.setBackground(new Color(100, 149, 237));
		btnJo_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_4.setBounds(208, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_4);

		btnQo_4 = new JButton("Q5o");
		btnQo_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_4.setBackground(new Color(100, 149, 237));
		btnQo_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_4.setBounds(142, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_4);

		btnTo_3 = new JButton("T5o");
		btnTo_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_3.setBackground(new Color(100, 149, 237));
		btnTo_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_3.setBounds(274, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_3);

		btno_14 = new JButton("75o");
		btno_14.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_14.setBackground(new Color(100, 149, 237));
		btno_14.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_14.setBounds(472, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_14);

		btno_9 = new JButton("85o");
		btno_9.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_9.setBackground(new Color(100, 149, 237));
		btno_9.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_9.setBounds(407, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_9);

		btno_3 = new JButton("95o");
		btno_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_3.setBackground(new Color(100, 149, 237));
		btno_3.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_3.setBounds(341, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_3);

		btns_24 = new JButton("52s");
		btns_24.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_24.setBackground(new Color(220, 20, 60));
		btns_24.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_24.setBounds(802, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_24);

		btns_23 = new JButton("53s");
		btns_23.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_23.setBackground(new Color(220, 20, 60));
		btns_23.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_23.setBounds(737, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_23);

		btns_22 = new JButton("54s");
		btns_22.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_22.setBackground(new Color(220, 20, 60));
		btns_22.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_22.setBounds(671, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_22);

		button_127 = new JButton("55");
		button_127.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_127.setBackground(new Color(50, 205, 50));
		button_127.setFont(new Font("Calibri", Font.BOLD, 12));
		button_127.setBounds(604, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_127);

		btno_18 = new JButton("65o");
		btno_18.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_18.setBackground(new Color(100, 149, 237));
		btno_18.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_18.setBounds(538, 374, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_18);

		btnAo_5 = new JButton("A4o");
		btnAo_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_5.setBackground(new Color(100, 149, 237));
		btnAo_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_5.setBounds(10, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_5);

		btnKo_5 = new JButton("K4o");
		btnKo_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_5.setBackground(new Color(100, 149, 237));
		btnKo_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_5.setBounds(76, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_5);

		btnJo_5 = new JButton("J4o");
		btnJo_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_5.setBackground(new Color(100, 149, 237));
		btnJo_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_5.setBounds(208, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_5);

		btnQo_5 = new JButton("Q4o");
		btnQo_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_5.setBackground(new Color(100, 149, 237));
		btnQo_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_5.setBounds(142, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_5);

		btnTo_4 = new JButton("T4o");
		btnTo_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_4.setBackground(new Color(100, 149, 237));
		btnTo_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_4.setBounds(274, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_4);

		btno_15 = new JButton("74o");
		btno_15.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_15.setBackground(new Color(100, 149, 237));
		btno_15.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_15.setBounds(472, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_15);

		btno_10 = new JButton("84o");
		btno_10.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_10.setBackground(new Color(100, 149, 237));
		btno_10.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_10.setBounds(407, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_10);

		btno_4 = new JButton("94o");
		btno_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_4.setBackground(new Color(100, 149, 237));
		btno_4.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_4.setBounds(341, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_4);

		btns_26 = new JButton("42s");
		btns_26.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_26.setBackground(new Color(220, 20, 60));
		btns_26.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_26.setBounds(802, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_26);

		btns_25 = new JButton("43s");
		btns_25.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_25.setBackground(new Color(220, 20, 60));
		btns_25.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_25.setBounds(737, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_25);

		button_139 = new JButton("44");
		button_139.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_139.setBackground(new Color(50, 205, 50));
		button_139.setFont(new Font("Calibri", Font.BOLD, 12));
		button_139.setBounds(671, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_139);

		btno_22 = new JButton("54o");
		btno_22.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_22.setBackground(new Color(100, 149, 237));
		btno_22.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_22.setBounds(604, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_22);

		btno_19 = new JButton("64o");
		btno_19.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_19.setBackground(new Color(100, 149, 237));
		btno_19.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_19.setBounds(538, 412, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_19);

		btnAo_6 = new JButton("A3o");
		btnAo_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_6.setBackground(new Color(100, 149, 237));
		btnAo_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_6.setBounds(10, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_6);

		btnKo_6 = new JButton("K3o");
		btnKo_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_6.setBackground(new Color(100, 149, 237));
		btnKo_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_6.setBounds(76, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_6);

		btnJo_6 = new JButton("J3o");
		btnJo_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_6.setBackground(new Color(100, 149, 237));
		btnJo_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_6.setBounds(208, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_6);

		btnQo_6 = new JButton("Q3o");
		btnQo_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_6.setBackground(new Color(100, 149, 237));
		btnQo_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_6.setBounds(142, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_6);

		btnTo_5 = new JButton("T3o");
		btnTo_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_5.setBackground(new Color(100, 149, 237));
		btnTo_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_5.setBounds(274, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_5);

		btno_16 = new JButton("73o");
		btno_16.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_16.setBackground(new Color(100, 149, 237));
		btno_16.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_16.setBounds(472, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_16);

		btno_11 = new JButton("83o");
		btno_11.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_11.setBackground(new Color(100, 149, 237));
		btno_11.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_11.setBounds(407, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_11);

		btno_5 = new JButton("93o");
		btno_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_5.setBackground(new Color(100, 149, 237));
		btno_5.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_5.setBounds(341, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_5);

		btns_27 = new JButton("32s");
		btns_27.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btns_27.setBackground(new Color(220, 20, 60));
		btns_27.setFont(new Font("Calibri", Font.BOLD, 12));
		btns_27.setBounds(802, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btns_27);

		button_151 = new JButton("33");
		button_151.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_151.setBackground(new Color(50, 205, 50));
		button_151.setFont(new Font("Calibri", Font.BOLD, 12));
		button_151.setBounds(737, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_151);

		btno_25 = new JButton("43o");
		btno_25.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_25.setBackground(new Color(100, 149, 237));
		btno_25.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_25.setBounds(671, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_25);

		btno_23 = new JButton("53o");
		btno_23.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_23.setBackground(new Color(100, 149, 237));
		btno_23.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_23.setBounds(604, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_23);

		btno_20 = new JButton("63o");
		btno_20.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_20.setBackground(new Color(100, 149, 237));
		btno_20.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_20.setBounds(538, 449, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_20);

		btnAo_7 = new JButton("A2o");
		btnAo_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnAo_7.setBackground(new Color(100, 149, 237));
		btnAo_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnAo_7.setBounds(10, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnAo_7);

		btnKo_7 = new JButton("K2o");
		btnKo_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnKo_7.setBackground(new Color(100, 149, 237));
		btnKo_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnKo_7.setBounds(76, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnKo_7);

		btnJo_7 = new JButton("J2o");
		btnJo_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnJo_7.setBackground(new Color(100, 149, 237));
		btnJo_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnJo_7.setBounds(208, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnJo_7);

		btnQo_7 = new JButton("Q2o");
		btnQo_7.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnQo_7.setBackground(new Color(100, 149, 237));
		btnQo_7.setFont(new Font("Calibri", Font.BOLD, 12));
		btnQo_7.setBounds(142, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnQo_7);

		btnTo_6 = new JButton("T2o");
		btnTo_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btnTo_6.setBackground(new Color(100, 149, 237));
		btnTo_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btnTo_6.setBounds(274, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btnTo_6);

		btno_17 = new JButton("72o");
		btno_17.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_17.setBackground(new Color(100, 149, 237));
		btno_17.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_17.setBounds(472, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_17);

		btno_12 = new JButton("82o");
		btno_12.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_12.setBackground(new Color(100, 149, 237));
		btno_12.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_12.setBounds(407, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_12);

		btno_6 = new JButton("92o");
		btno_6.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_6.setBackground(new Color(100, 149, 237));
		btno_6.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_6.setBounds(341, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_6);

		button_163 = new JButton("22");
		button_163.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		button_163.setBackground(new Color(50, 205, 50));
		button_163.setFont(new Font("Calibri", Font.BOLD, 12));
		button_163.setBounds(802, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(button_163);

		btno_27 = new JButton("32o");
		btno_27.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_27.setBackground(new Color(100, 149, 237));
		btno_27.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_27.setBounds(737, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_27);

		btno_26 = new JButton("42o");
		btno_26.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_26.setBackground(new Color(100, 149, 237));
		btno_26.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_26.setBounds(671, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_26);

		btno_24 = new JButton("52o");
		btno_24.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_24.setBackground(new Color(100, 149, 237));
		btno_24.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_24.setBounds(604, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_24);

		btno_21 = new JButton("62o");
		btno_21.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				selectedHand = getHandFromButtonEvent(e);
				lblXx.setText(selectedHand);
				evaluate();
			}
		});
		btno_21.setBackground(new Color(100, 149, 237));
		btno_21.setFont(new Font("Calibri", Font.BOLD, 12));
		btno_21.setBounds(538, 486, 64, 34);
		frmPushFoldCalculator.getContentPane().add(btno_21);

		handButtonArray.add(btnAa);
		handButtonArray.add(btnAks);
		handButtonArray.add(btnAjo_1);
		handButtonArray.add(btnAqs);
		handButtonArray.add(btnAto_1);
		handButtonArray.add(btnAo_10);
		handButtonArray.add(btnAo_9);
		handButtonArray.add(btnAo_8);
		handButtonArray.add(btnAo_15);
		handButtonArray.add(btnAo_14);
		handButtonArray.add(btnAo_13);
		handButtonArray.add(btnAo_12);
		handButtonArray.add(btnAo_11);
		handButtonArray.add(btnAko);
		handButtonArray.add(btnKk);
		handButtonArray.add(btnKjs);
		handButtonArray.add(button_15);
		handButtonArray.add(btnKts);
		handButtonArray.add(btnKs_2);
		handButtonArray.add(btnKs_1);
		handButtonArray.add(btnKs);
		handButtonArray.add(btnKs_7);
		handButtonArray.add(btnKs_6);
		handButtonArray.add(btnKs_5);
		handButtonArray.add(btnKs_4);
		handButtonArray.add(btnKs_3);
		handButtonArray.add(btnAqo);
		handButtonArray.add(btnKqo);
		handButtonArray.add(btnQjs);
		handButtonArray.add(btnQq);
		handButtonArray.add(btnQts);
		handButtonArray.add(btnQs_2);
		handButtonArray.add(btnQs_1);
		handButtonArray.add(btnQs);
		handButtonArray.add(btnQs_7);
		handButtonArray.add(btnQs_6);
		handButtonArray.add(btnQs_5);
		handButtonArray.add(btnQs_4);
		handButtonArray.add(btnQs_3);
		handButtonArray.add(btnAjo);
		handButtonArray.add(btnKjo);
		handButtonArray.add(btnJj);
		handButtonArray.add(btnQjo);
		handButtonArray.add(btnJts);
		handButtonArray.add(btnJs_2);
		handButtonArray.add(btnJs_1);
		handButtonArray.add(btnJs);
		handButtonArray.add(btnJs_7);
		handButtonArray.add(btnJs_6);
		handButtonArray.add(btnJs_5);
		handButtonArray.add(btnJs_4);
		handButtonArray.add(btnJs_3);
		handButtonArray.add(btnAto);
		handButtonArray.add(btnKto);
		handButtonArray.add(btnJto);
		handButtonArray.add(btnQto);
		handButtonArray.add(btnTt);
		handButtonArray.add(btnTs_2);
		handButtonArray.add(btnTs_1);
		handButtonArray.add(btnTs);
		handButtonArray.add(btnTs_7);
		handButtonArray.add(btnTs_6);
		handButtonArray.add(btnTs_5);
		handButtonArray.add(btnTs_4);
		handButtonArray.add(btnTs_3);
		handButtonArray.add(btnAo);
		handButtonArray.add(btnKo);
		handButtonArray.add(btnJo);
		handButtonArray.add(btnQo);
		handButtonArray.add(btnTo);
		handButtonArray.add(btns_1);
		handButtonArray.add(btns);
		handButtonArray.add(button_71);
		handButtonArray.add(btns_6);
		handButtonArray.add(btns_5);
		handButtonArray.add(btns_4);
		handButtonArray.add(btns_3);
		handButtonArray.add(btns_2);
		handButtonArray.add(btnAo_1);
		handButtonArray.add(btnKo_1);
		handButtonArray.add(btnJo_1);
		handButtonArray.add(btnQo_1);
		handButtonArray.add(btnTto);
		handButtonArray.add(btns_7);
		handButtonArray.add(button_83);
		handButtonArray.add(btno);
		handButtonArray.add(btns_12);
		handButtonArray.add(btns_11);
		handButtonArray.add(btns_10);
		handButtonArray.add(btns_9);
		handButtonArray.add(btns_8);
		handButtonArray.add(btnAo_2);
		handButtonArray.add(btnKo_2);
		handButtonArray.add(btnJo_2);
		handButtonArray.add(btnQo_2);
		handButtonArray.add(btnTo_1);
		handButtonArray.add(button_95);
		handButtonArray.add(btno_7);
		handButtonArray.add(btno_1);
		handButtonArray.add(btns_17);
		handButtonArray.add(btns_16);
		handButtonArray.add(btns_15);
		handButtonArray.add(btns_14);
		handButtonArray.add(btns_13);
		handButtonArray.add(btnAo_3);
		handButtonArray.add(btnKo_3);
		handButtonArray.add(btnJo_3);
		handButtonArray.add(btnQo_3);
		handButtonArray.add(btnTo_2);
		handButtonArray.add(btno_13);
		handButtonArray.add(btno_8);
		handButtonArray.add(btno_2);
		handButtonArray.add(btns_21);
		handButtonArray.add(btns_20);
		handButtonArray.add(btns_19);
		handButtonArray.add(btns_18);
		handButtonArray.add(button_115);
		handButtonArray.add(btnAo_4);
		handButtonArray.add(btnKo_4);
		handButtonArray.add(btnJo_4);
		handButtonArray.add(btnQo_4);
		handButtonArray.add(btnTo_3);
		handButtonArray.add(btno_14);
		handButtonArray.add(btno_9);
		handButtonArray.add(btno_3);
		handButtonArray.add(btns_24);
		handButtonArray.add(btns_23);
		handButtonArray.add(btns_22);
		handButtonArray.add(button_127);
		handButtonArray.add(btno_18);
		handButtonArray.add(btnAo_5);
		handButtonArray.add(btnKo_5);
		handButtonArray.add(btnJo_5);
		handButtonArray.add(btnQo_5);
		handButtonArray.add(btnTo_4);
		handButtonArray.add(btno_15);
		handButtonArray.add(btno_10);
		handButtonArray.add(btno_4);
		handButtonArray.add(btns_26);
		handButtonArray.add(btns_25);
		handButtonArray.add(button_139);
		handButtonArray.add(btno_22);
		handButtonArray.add(btno_19);
		handButtonArray.add(btnAo_6);
		handButtonArray.add(btnKo_6);
		handButtonArray.add(btnJo_6);
		handButtonArray.add(btnQo_6);
		handButtonArray.add(btnTo_5);
		handButtonArray.add(btno_16);
		handButtonArray.add(btno_11);
		handButtonArray.add(btno_5);
		handButtonArray.add(btns_27);
		handButtonArray.add(button_151);
		handButtonArray.add(btno_25);
		handButtonArray.add(btno_23);
		handButtonArray.add(btno_20);
		handButtonArray.add(btnAo_7);
		handButtonArray.add(btnKo_7);
		handButtonArray.add(btnJo_7);
		handButtonArray.add(btnQo_7);
		handButtonArray.add(btnTo_6);
		handButtonArray.add(btno_17);
		handButtonArray.add(btno_12);
		handButtonArray.add(btno_6);
		handButtonArray.add(button_163);
		handButtonArray.add(btno_27);
		handButtonArray.add(btno_26);
		handButtonArray.add(btno_24);
		handButtonArray.add(btno_21);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 531, 858, 2);
		frmPushFoldCalculator.getContentPane().add(separator);

		JLabel lblPlayersAtTable = new JLabel("Table type:");
		lblPlayersAtTable.setFont(new Font("Calibri", Font.BOLD, 14));
		lblPlayersAtTable.setBounds(10, 543, 104, 34);
		frmPushFoldCalculator.getContentPane().add(lblPlayersAtTable);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(10, 585, 858, 2);
		frmPushFoldCalculator.getContentPane().add(separator_2);

		JLabel lblPlayersBehind = new JLabel("Players behind:");
		lblPlayersBehind.setToolTipText(
				"The number of players left to act after you (e.g. If  you are in SB, there is 1 player behind you (BB))");
		lblPlayersBehind.setFont(new Font("Calibri", Font.BOLD, 14));
		lblPlayersBehind.setBounds(410, 544, 138, 34);
		frmPushFoldCalculator.getContentPane().add(lblPlayersBehind);

		radioButtonFullRing = new JRadioButton("Full Ring");
		radioButtonFullRing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButton_players_1.setSelected(true);
				radioButton_players_2.setEnabled(true);
				radioButton_players_3.setEnabled(true);
				radioButton_players_4.setEnabled(true);
				radioButton_players_5.setEnabled(true);
				radioButton_players_6.setEnabled(true);
				radioButton_players_7.setEnabled(true);
				radioButton_players_8.setEnabled(true);
				slider.setMinimum(2);
				slider.setMaximum(10);
				rdbtnPushModehu.setVisible(false);
				rdbtnCallModehu.setVisible(false);
				callMode = false;
				evaluate();
			}
		});
		buttonGroup.add(radioButtonFullRing);
		radioButtonFullRing.setSelected(true);
		radioButtonFullRing.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButtonFullRing.setBackground(new Color(250, 240, 230));
		radioButtonFullRing.setBounds(120, 550, 89, 23);
		frmPushFoldCalculator.getContentPane().add(radioButtonFullRing);

		radioButtonMax = new JRadioButton("6 Max");
		radioButtonMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButton_players_1.setSelected(true);
				radioButton_players_2.setEnabled(true);
				radioButton_players_3.setEnabled(true);
				radioButton_players_4.setEnabled(true);
				radioButton_players_5.setEnabled(true);
				radioButton_players_6.setEnabled(false);
				radioButton_players_7.setEnabled(false);
				radioButton_players_8.setEnabled(false);
				slider.setMinimum(2);
				slider.setMaximum(10);
				rdbtnPushModehu.setVisible(false);
				rdbtnCallModehu.setVisible(false);
				callMode = false;
				evaluate();
			}
		});
		buttonGroup.add(radioButtonMax);
		radioButtonMax.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButtonMax.setBackground(new Color(250, 240, 230));
		radioButtonMax.setBounds(208, 550, 74, 23);
		frmPushFoldCalculator.getContentPane().add(radioButtonMax);

		radioButtonHeadsUp = new JRadioButton("Heads Up");
		radioButtonHeadsUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				radioButton_players_1.setSelected(true);
				radioButton_players_2.setEnabled(false);
				radioButton_players_3.setEnabled(false);
				radioButton_players_4.setEnabled(false);
				radioButton_players_5.setEnabled(false);
				radioButton_players_6.setEnabled(false);
				radioButton_players_7.setEnabled(false);
				radioButton_players_8.setEnabled(false);
				slider.setMinimum(2);
				slider.setMaximum(16);
				rdbtnPushModehu.setVisible(true);
				rdbtnCallModehu.setVisible(true);
				if (rdbtnPushModehu.isSelected()) {
					callMode = false;
				}

				else if (rdbtnCallModehu.isSelected()) {
					callMode = true;
				}
				evaluate();
			}
		});
		buttonGroup.add(radioButtonHeadsUp);
		radioButtonHeadsUp.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButtonHeadsUp.setBackground(new Color(250, 240, 230));
		radioButtonHeadsUp.setBounds(284, 550, 109, 23);
		frmPushFoldCalculator.getContentPane().add(radioButtonHeadsUp);

		radioButton_players_8 = new JRadioButton("8");
		radioButton_players_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_8);
		radioButton_players_8.setSelected(true);
		radioButton_players_8.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_8.setBackground(new Color(250, 240, 230));
		radioButton_players_8.setBounds(547, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_8);

		radioButton_players_7 = new JRadioButton("7");
		radioButton_players_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_7);
		radioButton_players_7.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_7.setBackground(new Color(250, 240, 230));
		radioButton_players_7.setBounds(587, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_7);

		radioButton_players_5 = new JRadioButton("5");
		radioButton_players_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_5);
		radioButton_players_5.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_5.setBackground(new Color(250, 240, 230));
		radioButton_players_5.setBounds(667, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_5);

		radioButton_players_6 = new JRadioButton("6");
		radioButton_players_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_6);
		radioButton_players_6.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_6.setBackground(new Color(250, 240, 230));
		radioButton_players_6.setBounds(627, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_6);

		radioButton_players_1 = new JRadioButton("1");
		radioButton_players_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_1);
		radioButton_players_1.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_1.setBackground(new Color(250, 240, 230));
		radioButton_players_1.setBounds(828, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_1);

		radioButton_players_4 = new JRadioButton("4");
		radioButton_players_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_4);
		radioButton_players_4.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_4.setBackground(new Color(250, 240, 230));
		radioButton_players_4.setBounds(708, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_4);

		radioButton_players_3 = new JRadioButton("3");
		radioButton_players_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_3);
		radioButton_players_3.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_3.setBackground(new Color(250, 240, 230));
		radioButton_players_3.setBounds(748, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_3);

		radioButton_players_2 = new JRadioButton("2");
		radioButton_players_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		buttonGroup_1.add(radioButton_players_2);
		radioButton_players_2.setFont(new Font("Calibri", Font.PLAIN, 16));
		radioButton_players_2.setBackground(new Color(250, 240, 230));
		radioButton_players_2.setBounds(788, 548, 38, 23);
		frmPushFoldCalculator.getContentPane().add(radioButton_players_2);

		slider = new JSlider();
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				evaluate();
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				evaluate();
			}
		});
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing(1);
		slider.setValue(10);
		slider.setMinimum(2);
		slider.setMaximum(10);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setBackground(new Color(250, 240, 230));
		slider.setForeground(new Color(0, 0, 0));
		slider.setBounds(142, 598, 645, 43);
		frmPushFoldCalculator.getContentPane().add(slider);

		JLabel lblBigBlinds = new JLabel("Big Blinds left:");
		lblBigBlinds.setToolTipText("Your stack size");
		lblBigBlinds.setFont(new Font("Calibri", Font.BOLD, 14));
		lblBigBlinds.setBounds(10, 599, 131, 34);
		frmPushFoldCalculator.getContentPane().add(lblBigBlinds);

		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 652, 858, 2);
		frmPushFoldCalculator.getContentPane().add(separator_3);

		checkboxAntes = new JCheckBox("Antes");
		checkboxAntes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				evaluate();
			}
		});
		checkboxAntes.setFont(new Font("Calibri", Font.PLAIN, 16));
		checkboxAntes.setBackground(new Color(250, 240, 230));
		checkboxAntes.setBounds(800, 609, 70, 23);
		frmPushFoldCalculator.getContentPane().add(checkboxAntes);

		JLabel lblHand = new JLabel("Hand:");
		lblHand.setFont(new Font("Calibri", Font.BOLD, 16));
		lblHand.setBounds(10, 5, 58, 34);
		frmPushFoldCalculator.getContentPane().add(lblHand);

		lblXx = new JLabel("AA");
		lblXx.setFont(new Font("Calibri", Font.BOLD, 16));
		lblXx.setBounds(68, 5, 37, 34);
		frmPushFoldCalculator.getContentPane().add(lblXx);

		lblPushRange = new JLabel("");
		lblPushRange.setFont(new Font("Calibri", Font.BOLD, 13));
		lblPushRange.setBounds(120, 5, 104, 34);
		frmPushFoldCalculator.getContentPane().add(lblPushRange);

		lblPushFold = new JLabel("PUSH");
		lblPushFold.setBounds(790, 1, 124, 43);
		frmPushFoldCalculator.getContentPane().add(lblPushFold);
		lblPushFold.setForeground(new Color(50, 205, 50));
		lblPushFold.setFont(new Font("Calibri", Font.BOLD, 24));

		lblPushRangeText = new JLabel("");
		lblPushRangeText.setFont(new Font("Calibri", Font.BOLD, 13));
		lblPushRangeText.setBounds(218, 6, 509, 34);
		frmPushFoldCalculator.getContentPane().add(lblPushRangeText);

		rdbtnPushModehu = new JRadioButton("Push mode");
		rdbtnPushModehu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		rdbtnPushModehu.setVisible(false);
		rdbtnPushModehu.setSelected(true);
		buttonGroup_2.add(rdbtnPushModehu);
		rdbtnPushModehu.setFont(new Font("Calibri", Font.PLAIN, 12));
		rdbtnPushModehu.setBackground(new Color(250, 240, 230));
		rdbtnPushModehu.setBounds(10, 661, 97, 23);
		frmPushFoldCalculator.getContentPane().add(rdbtnPushModehu);

		rdbtnCallModehu = new JRadioButton("Call mode");
		rdbtnCallModehu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});
		rdbtnCallModehu.setToolTipText("Shows whether to call an opponent's shove");
		rdbtnCallModehu.setVisible(false);
		buttonGroup_2.add(rdbtnCallModehu);
		rdbtnCallModehu.setFont(new Font("Calibri", Font.PLAIN, 12));
		rdbtnCallModehu.setBackground(new Color(250, 240, 230));
		rdbtnCallModehu.setBounds(109, 661, 97, 23);
		frmPushFoldCalculator.getContentPane().add(rdbtnCallModehu);

		checkboxHighlightRange = new JCheckBox("Highlight range");
		checkboxHighlightRange.setSelected(true);
		checkboxHighlightRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkboxHighlightRange.isSelected()) {
					for (JButton b : handButtonArray) {
						for (String c : rangeArray) {
							if (b.getText().equalsIgnoreCase(c)) {
								highlightButton(b);
							}
						}
					}
				}

				else {
					for (JButton b : handButtonArray) {
						unHighlightButton(b);
					}
				}

				evaluate();
			}
		});
		checkboxHighlightRange.setFont(new Font("Calibri", Font.PLAIN, 14));
		checkboxHighlightRange.setBackground(new Color(250, 240, 230));
		checkboxHighlightRange.setBounds(735, 661, 131, 23);
		frmPushFoldCalculator.getContentPane().add(checkboxHighlightRange);

		evaluate();
	}

	public void highlightButton(JButton button) {
		button.setBackground(Color.ORANGE);
	}

	public void highlightButtonForeground(JButton button) {
		button.setForeground(Color.WHITE);
	}

	public void unHighlightButtonForeground(JButton button) {
		button.setForeground(Color.BLACK);
	}

	public void unHighlightButton(JButton button) {
		String hand = button.getText();
		String[] handStringArray = hand.split("");

		if (handStringArray[0].equalsIgnoreCase(handStringArray[1])) {
			button.setBackground(new Color(50, 205, 50)); // green for pair
		}

		else if (handStringArray[2].equalsIgnoreCase("s")) {
			button.setBackground(new Color(220, 20, 60)); // red for suited
		}

		else if (handStringArray[2].equalsIgnoreCase("o")) {
			button.setBackground(new Color(100, 149, 237)); // blue for offsuit
		}

	}

	public String getHandFromButtonEvent(MouseEvent e) {
		for (JButton bu : handButtonArray) {
			unHighlightButtonForeground(bu);
		}

		Object o = e.getSource();
		JButton b = null;

		if (o instanceof JButton) {
			b = (JButton) o;
		}

		if (b != null) {
			highlightButtonForeground(b);
			return b.getText();
		}

		return "Err";
	}

	public String getHandFromButton(JButton b) {
		if (b != null) {
			return b.getText();
		}

		return "Err";
	}

	public void evaluate() {
		for (JButton b : handButtonArray) {
			unHighlightButton(b);
		}
		callMode = false;
		String gameType = "FR";
		String playersLeft = "1";
		int bigBlindsValue = slider.getValue();
		if (bigBlindsValue == 7 && !radioButtonHeadsUp.isSelected()) {
			bigBlindsValue = 6;
		} else if (bigBlindsValue == 9 && !radioButtonHeadsUp.isSelected()) {
			bigBlindsValue = 8;
		}
		String bigBlinds = String.valueOf(bigBlindsValue);
		String antes = "NA";

		if (radioButtonFullRing.isSelected()) {
			gameType = "FR";
		}

		else if (radioButtonMax.isSelected()) {
			gameType = "SM";
		}

		else {
			gameType = "HU";
		}

		if (radioButton_players_1.isSelected()) {
			playersLeft = "1";
		}

		else if (radioButton_players_2.isSelected()) {
			playersLeft = "2";
		}

		else if (radioButton_players_3.isSelected()) {
			playersLeft = "3";
		}

		else if (radioButton_players_4.isSelected()) {
			playersLeft = "4";
		}

		else if (radioButton_players_5.isSelected()) {
			playersLeft = "5";
		}

		else if (radioButton_players_6.isSelected()) {
			playersLeft = "6";
		}

		else if (radioButton_players_7.isSelected()) {
			playersLeft = "7";
		}

		else {
			playersLeft = "8";
		}

		if (checkboxAntes.isSelected()) {
			antes = "A";
		}

		else {
			antes = "NA";
		}

		if (gameType.equalsIgnoreCase("HU") && rdbtnCallModehu.isSelected()) {
			callMode = true;
		}

		if (cardArray.push(gameType, playersLeft, bigBlinds, antes, selectedHand, callMode)) {
			if (callMode) {
				lblPushFold.setText("CALL");
			}

			else {
				lblPushFold.setText("PUSH");
			}
			lblPushFold.setForeground(new Color(50, 205, 50)); // Green
		}

		else {
			lblPushFold.setText("FOLD");
			lblPushFold.setForeground(new Color(220, 20, 60)); // Red
		}

		if (callMode) {
			lblPushRange.setText("Call range:");
		} else {
			lblPushRange.setText("Push range:");
		}
		lblPushRangeText.setText(cardArray.getPushRangeString(gameType, playersLeft, bigBlinds, antes, callMode));

		rangeArray = cardArray.getRangeArray(gameType, playersLeft, bigBlinds, antes, callMode);

		if (checkboxHighlightRange.isSelected()) {
			for (JButton b : handButtonArray) {
				for (String c : rangeArray) {
					if (b.getText().equalsIgnoreCase(c)) {
						highlightButton(b);
					}
				}
			}
		}
	}
}