package plugins.hero.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardArray {
	private static Map<String, ArrayList<String>> arrayMap;
	private static Map<String, String> arrayRangeMap;

	public CardArray() {
		initArrays();
	}

	public void initArrays() {
		arrayMap = new HashMap<String, ArrayList<String>>();
		arrayRangeMap = new HashMap<String, String>();
		// Code: GAMETYPE_PLAYERS_BB_ANTE
		addArrayFromParsedRangeToMap("33+ A8s+ A5s AJo+ K9s+ KQo QTs+ JTs T9s", "FR_8_10_A");
		addArrayFromParsedRangeToMap("22+ A8s+ A5s ATo+ K9s+ KQo Q9s+ J9s+ T9s", "FR_7_10_A");
		addArrayFromParsedRangeToMap("22+ A8s+ A5s A4s ATo+ K9s+ KJo+ Q9s+ QJo J9s+ T9s", "FR_6_10_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A9o+ K8s+ KJo+ Q9s+ QJo J8s+ JTo T8s+ 98s", "FR_5_10_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A5o+ K7s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "FR_4_10_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q8s+ QTo+ J8s+ JTo T7s+ 97s+ 87s 76s", "FR_3_10_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K8o+ Q6s+ Q9o+ J7s+ J9o+ T7s+ T9o 96s+ 86s+ 75s+ 65s", "FR_2_10_A");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J6o+ T2s+ T7o+ 94s+ 97o+ 84s+ 86o+ 74s+ 76o 63s+ 53s+ 43s",
				"FR_1_10_A");

		addArrayFromParsedRangeToMap("99+ ATs+ A5s AJo+ KTs+ QTs+", "FR_8_10_NA");
		addArrayFromParsedRangeToMap("88+ A9s+ A5s AJo+ KTs+ KQo QTs+ JTs", "FR_7_10_NA");
		addArrayFromParsedRangeToMap("44+ A9s+ A5s AJo+ K9s+ KQo QTs+ JTs", "FR_6_10_NA");
		addArrayFromParsedRangeToMap("22+ A8s+ A5s ATo+ K9s+ KQo Q9s+ J9s+ T9s", "FR_5_10_NA");
		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s A3s ATo+ K8s+ KJo+ Q8s+ QJo J8s+ T8s+ 98s", "FR_4_10_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K7s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "FR_3_10_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s 76s", "FR_2_10_NA");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q8o+ J3s+ J8o+ T4s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 76o 64s+ 53s+",
				"FR_1_10_NA");

		addArrayFromParsedRangeToMap("22+ A8s+ A5s A4s ATo+ K9s+ KJo+ Q9s+ J9s+ T9s", "FR_8_8_A");
		addArrayFromParsedRangeToMap("22+ A3s+ A9o+ K9s+ KJo+ Q9s+ QJo J9s+ T9s 98s", "FR_7_8_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K9s+ KTo+ Q9s+ QJo J8s+ T8s+ 98s", "FR_6_8_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K7s+ KTo+ Q8s+ QJo J8s+ JTo T8s+ 98s 87s", "FR_5_8_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q9s+ QTo+ J8s+ JTo T8s+ 98s 87s", "FR_4_8_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K3s+ K9o+ Q6s+ QTo+ J7s+ JTo T7s+ 97s+ 86s+ 76s 65s", "FR_3_8_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q4s+ Q9o+ J7s+ J9o+ T7s+ T9o 96s+ 86s+ 76s 65s", "FR_2_8_A");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T4o+ 92s+ 96o+ 83s+ 86o+ 73s+ 75o+ 63s+ 65o 53s+ 43s", "FR_1_8_A");

		addArrayFromParsedRangeToMap("55+ A9s+ A5s AJo+ KTs+ KQo QTs+ JTs", "FR_8_8_NA");
		addArrayFromParsedRangeToMap("33+ A8s+ A5s A4s AJo+ K9s+ KQo QTs+ JTs", "FR_7_8_NA");
		addArrayFromParsedRangeToMap("22+ A8s+ A5s ATo+ K9s+ KQo Q9s+ J9s+ T9s", "FR_6_8_NA");
		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s A3s ATo+ K8s+ KJo+ Q9s+ QJo J9s+ T9s 98s", "FR_5_8_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s", "FR_4_8_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K7s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s 87s", "FR_3_8_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T7s+ 97s+ 86s+ 76s 65s", "FR_2_8_NA");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q5o+ J2s+ J7o+ T4s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 76o 64s+ 53s+",
				"FR_1_8_NA");

		addArrayFromParsedRangeToMap("22+ A3s+ A9o+ K9s+ KJo+ Q9s+ QJo J9s+ T9s 98s", "FR_8_6_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J9s+ T8s+ 98s", "FR_7_6_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K7s+ KTo+ Q9s+ QJo J8s+ T8s+ 98s 87s", "FR_6_6_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K6s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "FR_5_6_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s 76s", "FR_4_6_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K7o+ Q6s+ Q9o+ J8s+ JTo T7s+ 97s+ 87s 76s", "FR_3_6_A");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q8o+ J6s+ J8o+ T7s+ T9o 97s+ 86s+ 76s", "FR_2_6_A");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 94o+ 82s+ 85o+ 73s+ 75o+ 62s+ 65o 52s+ 54o 43s", "FR_1_6_A");

		addArrayFromParsedRangeToMap("22+ A9s+ A5s ATo+ K9s+ KQo QTs+ JTs T9s", "FR_8_6_NA");
		addArrayFromParsedRangeToMap("22+ A8s+ A5s A4s ATo+ K9s+ KJo+ Q9s+ J9s+ T9s", "FR_7_6_NA");
		addArrayFromParsedRangeToMap("22+ A3s+ A9o+ K9s+ KJo+ Q9s+ QJo J9s+ T9s 98s", "FR_6_6_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J9s+ T8s+ 98s", "FR_5_6_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K8s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s 87s", "FR_4_6_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q9s+ QTo+ J8s+ JTo T8s+ 97s+ 87s", "FR_3_6_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K7o+ Q8s+ QTo+ J8s+ JTo T7s+ 97s+ 86s+ 76s 65s", "FR_2_6_NA");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J4o+ T2s+ T6o+ 93s+ 96o+ 84s+ 86o+ 74s+ 76o 63s+ 53s+ 43s",
				"FR_1_6_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K9s+ KTo+ Q9s+ QJo J9s+ T9s", "FR_8_5_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ K6s+ KTo+ Q9s+ QTo+ J9s+ T8s+ 98s", "FR_7_5_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A5o+ K6s+ KTo+ Q9s+ QTo+ J9s+ T9s 98s", "FR_6_5_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K5s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 98s", "FR_5_5_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q7s+ Q9o+ J8s+ JTo T8s+ 98s", "FR_4_5_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q6s+ Q9o+ J7s+ J9o+ T8s+ 98s 87s", "FR_3_5_A");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q6o+ J5s+ J8o+ T6s+ T8o+ 97s+ 86s+ 76s", "FR_2_5_A");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 94o+ 82s+ 84o+ 73s+ 75o+ 63s+ 65o 52s+ 54o 43s", "FR_1_5_A");

		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s ATo+ K9s+ KJo+ Q9s+ J9s+ T9s", "FR_8_5_NA");
		addArrayFromParsedRangeToMap("22+ A3s+ ATo+ K9s+ KJo+ Q9s+ J9s+ T9s", "FR_7_5_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A9o+ K9s+ KJo+ Q9s+ QJo J9s+ T9s", "FR_6_5_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K8s+ KTo+ Q9s+ QJo J9s+ T8s+ 98s", "FR_5_5_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K7s+ KTo+ Q9s+ QTo+ J9s+ T8s+ 98s", "FR_4_5_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s", "FR_3_5_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q7s+ Q9o+ J8s+ JTo T8s+ 97s+ 87s 76s", "FR_2_5_NA");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J3o+ T2s+ T6o+ 94s+ 96o+ 84s+ 86o+ 74s+ 76o 64s+ 53s+", "FR_1_5_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K6s+ KTo+ Q8s+ QTo+ J9s+ T9s", "FR_8_4_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ K6s+ KTo+ Q8s+ QTo+ J9s+ JTo T9s", "FR_7_4_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A5o+ K5s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+", "FR_6_4_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K4s+ K9o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "FR_5_4_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K3s+ K8o+ Q6s+ Q9o+ J8s+ J9o+ T8s+ 98s", "FR_4_4_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K5o+ Q4s+ Q8o+ J7s+ J9o+ T7s+ T9o 97s+ 87s", "FR_3_4_A");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q6o+ J4s+ J8o+ T6s+ T8o+ 97s+ 87s", "FR_2_4_A");
		addArrayFromParsedRangeToMap("22+ 9x+ 82s+ 83o+ 72s+ 74o+ 62s+ 64o+ 52s+ 54o 43s", "FR_1_4_A");

		addArrayFromParsedRangeToMap("22+ A4s+ A9o+ K9s+ KJo+ QTs+ JTs T9s", "FR_8_4_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K9s+ KJo+ Q9s+ J9s+ T9s", "FR_7_4_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ K7s+ KTo+ Q9s+ QJo J9s+ T9s 98s", "FR_6_4_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K7s+ KTo+ Q9s+ QJo J9s+ T9s", "FR_5_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ K9o+ Q8s+ QTo+ J9s+ T9s", "FR_4_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K8o+ Q8s+ QTo+ J8s+ JTo T9s 98s", "FR_3_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K5o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "FR_2_4_NA");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T3o+ 92s+ 95o+ 84s+ 85o+ 74s+ 75o+ 64s+ 53s+", "FR_1_4_NA");

		addArrayFromParsedRangeToMap("44+ A2s+ A8o+ K6s+ KTo+ Q8s+ QTo+ J9s+ JTo T9s", "FR_8_3_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A7o+ K5s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+", "FR_7_3_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A5o+ K3s+ K9o+ Q6s+ Q9o+ J7s+ J9o+ T7s+ 97s+ 87s", "FR_6_3_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A4o+ K3s+ K9o+ Q6s+ Q9o+ J7s+ J9o+ T8s+ 98s", "FR_5_3_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K2s+ K7o+ Q5s+ Q8o+ J7s+ J9o+ T7s+ T9o 97s+ 87s", "FR_4_3_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K5o+ Q3s+ Q8o+ J5s+ J8o+ T6s+ T8o+ 97s+ 87s", "FR_3_3_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K3o+ Q2s+ Q6o+ J5s+ J8o+ T6s+ T8o+ 97s+ 98o 87s", "FR_2_3_A");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 63o+ 52s+ 53o+ 42s+ 32s", "FR_1_3_A");

		addArrayFromParsedRangeToMap("44+ A3s+ A9o+ K7s+ KTo+ Q9s+ QJo J9s+", "FR_8_3_NA");
		addArrayFromParsedRangeToMap("44+ A3s+ A9o+ K7s+ KTo+ Q9s+ QJo J9s+", "FR_7_3_NA");
		addArrayFromParsedRangeToMap("44+ A2s+ A7o+ K6s+ KTo+ Q9s+ QTo+ J9s+ T9s", "FR_6_3_NA");
		addArrayFromParsedRangeToMap("33+ A2s+ A7o+ A5o K6s+ K9o+ Q8s+ QTo+ J9s+ T9s", "FR_5_3_NA");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T9s", "FR_4_3_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K4s+ K8o+ Q7s+ Q9o+ J8s+ JTo T8s+", "FR_3_3_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q5s+ Q8o+ J7s+ J9o+ T8s+", "FR_2_3_NA");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 93o+ 82s+ 84o+ 73s+ 75o+ 63s+ 65o 53s+", "FR_1_3_NA");

		addArrayFromParsedRangeToMap("33+ A2s+ A4o+ K2s+ K7o+ Q4s+ Q8o+ J6s+ J9o+ T7s+ T9o 97s+ 86s+ 76s", "FR_8_2_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A4o+ K2s+ K7o+ Q4s+ Q8o+ J7s+ J9o+ T7s+ T9o 97s+ 87s 76s", "FR_7_2_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K2s+ K6o+ Q3s+ Q8o+ J6s+ J8o+ T6s+ T9o 97s+ 86s+ 76s", "FR_6_2_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K2s+ K6o+ Q3s+ Q8o+ J5s+ J8o+ T6s+ T8o+ 97s+ 86s+ 76s", "FR_5_2_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q2s+ Q7o+ J3s+ J8o+ T6s+ T8o+ 96s+ 98o 86s+ 76s 65s",
				"FR_4_2_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q2s+ Q6o+ J4s+ J8o+ T6s+ T8o+ 96s+ 98o 86s+ 76s", "FR_3_2_A");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q3o+ J2s+ J5o+ T2s+ T6o+ 94s+ 97o+ 85s+ 87o 75s+ 64s+ 54s",
				"FR_2_2_A");
		addArrayFromParsedRangeToMap("2x+", "FR_1_2_A");

		addArrayFromParsedRangeToMap("44+ A3s+ A9o+ K7s+ KTo+ Q9s+ QJo J9s+", "FR_8_2_NA");
		addArrayFromParsedRangeToMap("55+ A2s+ A9o+ K7s+ KTo+ Q9s+ QTo+ J9s+", "FR_7_2_NA");
		addArrayFromParsedRangeToMap("44+ A2s+ A7o+ K5s+ K9o+ Q8s+ QTo+ J9s+ JTo T9s", "FR_6_2_NA");
		addArrayFromParsedRangeToMap("44+ A2s+ A7o+ K6s+ K9o+ Q8s+ QTo+ J9s+ JTo T9s", "FR_5_2_NA");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K3s+ K8o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "FR_4_2_NA");
		addArrayFromParsedRangeToMap("44+ A2s+ A3o+ K3s+ K7o+ Q7s+ Q9o+ J8s+ JTo T8s+", "FR_3_2_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q5s+ Q8o+ J8s+ J9o+ T8s+ 98s", "FR_2_2_NA");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 64o+ 52s+ 53o+ 42s+", "FR_1_2_NA");

		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s A3s ATo+ K8s+ KJo+ Q8s+ QJo J8s+ T8s+ 98s", "SM_5_10_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K7s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s", "SM_4_10_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "SM_3_10_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K9o+ Q6s+ QTo+ J7s+ JTo T7s+ T9o 96s+ 86s+ 75s+ 65s 54s",
				"SM_2_10_A");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J7o+ T3s+ T7o+ 94s+ 97o+ 84s+ 86o+ 74s+ 76o 63s+ 53s+ 43s",
				"SM_1_10_A");

		addArrayFromParsedRangeToMap("22+ A8s+ A5s ATo+ K9s+ KQo Q9s+ J9s+ T9s", "SM_5_10_NA");
		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s A3s ATo+ K8s+ KJo+ Q8s+ QJo J8s+ T8s+ 98s", "SM_4_10_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K7s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "SM_3_10_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s 76s", "SM_2_10_NA");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q8o+ J3s+ J8o+ T4s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 76o 64s+ 53s+",
				"SM_1_10_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s", "SM_5_8_A");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K7s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s 87s", "SM_4_8_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s 76s", "SM_3_8_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K7o+ Q5s+ Q9o+ J7s+ JTo T7s+ T9o 96s+ 86s+ 76s 65s", "SM_2_8_A");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J4o+ T2s+ T6o+ 94s+ 96o+ 84s+ 86o+ 74s+ 76o 63s+ 53s+ 43s",
				"SM_1_8_A");

		addArrayFromParsedRangeToMap("22+ A7s+ A5s A4s A3s ATo+ K8s+ KJo+ Q9s+ QJo J9s+ T9s 98s", "SM_5_8_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s", "SM_4_8_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K7s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s 87s", "SM_3_8_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T7s+ 97s+ 86s+ 76s 65s", "SM_2_8_NA");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q5o+ J2s+ J7o+ T4s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 76o 64s+ 53s+",
				"SM_1_8_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K7s+ KTo+ Q9s+ QJo J8s+ T8s+ 98s 87s", "SM_5_6_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "SM_4_6_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K3s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 86s+ 76s 65s", "SM_3_6_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q5s+ Q8o+ J7s+ J9o+ T7s+ 97s+ 87s 76s", "SM_2_6_A");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T4o+ 92s+ 95o+ 83s+ 86o+ 73s+ 75o+ 63s+ 65o 53s+ 43s", "SM_1_6_A");

		addArrayFromParsedRangeToMap("22+ A2s+ A8o+ K8s+ KTo+ Q9s+ QJo J9s+ T8s+ 98s", "SM_5_6_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K8s+ KTo+ Q9s+ QJo J8s+ JTo T8s+ 98s 87s", "SM_4_6_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ KTo+ Q9s+ QTo+ J8s+ JTo T8s+ 97s+ 87s", "SM_3_6_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K7o+ Q8s+ QTo+ J8s+ JTo T7s+ 97s+ 86s+ 76s 65s", "SM_2_6_NA");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J4o+ T2s+ T6o+ 93s+ 96o+ 84s+ 86o+ 74s+ 76o 63s+ 53s+ 43s",
				"SM_1_6_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K6s+ KTo+ Q9s+ QTo+ J9s+ JTo T9s 98s", "SM_5_5_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 98s", "SM_4_5_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K3s+ K8o+ Q8s+ QTo+ J8s+ JTo T8s+ 98s 87s", "SM_3_5_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K3o+ Q4s+ Q8o+ J7s+ J9o+ T7s+ T9o 97s+ 87s", "SM_2_5_A");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 94o+ 82s+ 85o+ 73s+ 75o+ 62s+ 65o 52s+ 54o 43s", "SM_1_5_A");

		addArrayFromParsedRangeToMap("22+ A2s+ A7o+ A5o K8s+ KTo+ Q9s+ QJo J9s+ T8s+ 98s", "SM_5_5_NA");
		addArrayFromParsedRangeToMap("22+ A2s+ A3o+ K7s+ KTo+ Q9s+ QTo+ J9s+ T8s+ 98s", "SM_4_5_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T8s+ 97s+ 87s", "SM_3_5_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q7s+ Q9o+ J8s+ JTo T8s+ 97s+ 87s 76s", "SM_2_5_NA");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J3o+ T2s+ T6o+ 94s+ 96o+ 84s+ 86o+ 74s+ 76o 64s+ 53s+", "SM_1_5_NA");

		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K6s+ K9o+ Q8s+ QTo+ J9s+ JTo T9s", "SM_5_4_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K9o+ Q8s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_4_4_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K3s+ K7o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_3_4_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q3s+ Q7o+ J6s+ J8o+ T7s+ T9o 97s+ 87s", "SM_2_4_A");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 93o+ 82s+ 84o+ 73s+ 75o+ 63s+ 65o 53s+ 43s", "SM_1_4_A");

		addArrayFromParsedRangeToMap("22+ A2s+ A4o+ K7s+ KTo+ Q9s+ QJo J9s+ T9s", "SM_5_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ K9o+ Q8s+ QTo+ J9s+ T9s", "SM_4_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K8o+ Q8s+ QTo+ J8s+ JTo T9s 98s", "SM_3_4_NA");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K5o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_2_4_NA");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T3o+ 92s+ 95o+ 84s+ 85o+ 74s+ 75o+ 64s+ 53s+", "SM_1_4_NA");

		addArrayFromParsedRangeToMap("33+ A2s+ A4o+ K4s+ K9o+ Q7s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_5_3_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K4s+ K8o+ Q7s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_4_3_A");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q5s+ Q9o+ J7s+ J9o+ T8s+ T9o 98s", "SM_3_3_A");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q3s+ Q7o+ J6s+ J8o+ T7s+ T9o 97s+ 87s", "SM_2_3_A");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 64o+ 52s+ 53o+ 42s+", "SM_1_3_A");

		addArrayFromParsedRangeToMap("33+ A2s+ A7o+ A5o K6s+ K9o+ Q8s+ QTo+ J9s+ T9s", "SM_5_3_NA");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K4s+ K9o+ Q8s+ QTo+ J8s+ JTo T9s", "SM_4_3_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K4s+ K8o+ Q7s+ Q9o+ J8s+ JTo T8s+", "SM_3_3_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q5s+ Q8o+ J7s+ J9o+ T8s+", "SM_2_3_NA");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 93o+ 82s+ 84o+ 73s+ 75o+ 63s+ 65o 53s+", "SM_1_3_NA");

		addArrayFromParsedRangeToMap("44+ A2s+ A4o+ K3s+ K8o+ Q6s+ Q9o+ J8s+ J9o+ T8s+ 98s", "SM_5_2_A");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K2s+ K7o+ Q5s+ Q8o+ J7s+ J9o+ T7s+ T9o 97s+", "SM_4_2_A");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q3s+ Q8o+ J6s+ J8o+ T7s+ T9o 97s+ 87s", "SM_3_2_A");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K3o+ Q2s+ Q6o+ J4s+ J8o+ T6s+ T8o+ 96s+ 98o 87s", "SM_2_2_A");
		addArrayFromParsedRangeToMap("22+ 4x+ 32s", "SM_1_2_A");

		addArrayFromParsedRangeToMap("44+ A2s+ A7o+ K6s+ K9o+ Q8s+ QTo+ J9s+ JTo T9s", "SM_5_2_NA");
		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K3s+ K8o+ Q6s+ Q9o+ J8s+ JTo T8s+ 98s", "SM_4_2_NA");
		addArrayFromParsedRangeToMap("44+ A2s+ A3o+ K3s+ K7o+ Q7s+ Q9o+ J8s+ JTo T8s+", "SM_3_2_NA");
		addArrayFromParsedRangeToMap("33+ Ax+ K2s+ K6o+ Q5s+ Q8o+ J8s+ J9o+ T8s+ 98s", "SM_2_2_NA");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 64o+ 52s+ 53o+ 42s+", "SM_1_2_NA");

		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K7o+ Q5s+ Q9o+ J6s+ J9o+ T6s+ T9o 96s+ 98o 85s+ 75s+ 65s 54s",
				"HU_1_16_NC");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q4s+ Q9o+ J6s+ J9o+ T6s+ T8o+ 96s+ 98o 85s+ 75s+ 64s+ 54s",
				"HU_1_15_NC");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K5o+ Q4s+ Q9o+ J5s+ J8o+ T6s+ T8o+ 95s+ 98o 85s+ 87o 75s+ 64s+ 54s",
				"HU_1_14_NC");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q3s+ Q8o+ J5s+ J9o+ T6s+ T8o+ 96s+ 98o 85s+ 75s+ 64s+ 54s",
				"HU_1_13_NC");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K3o+ Q2s+ Q8o+ J5s+ J9o+ T6s+ T8o+ 95s+ 98o 85s+ 87o 75s+ 64s+ 54s",
				"HU_1_12_NC");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q8o+ J4s+ J8o+ T5s+ T8o+ 95s+ 98o 85s+ 87o 74s+ 64s+ 53s+",
				"HU_1_11_NC");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q7o+ J3s+ J8o+ T5s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 64s+ 53s+",
				"HU_1_10_NC");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q5o+ J2s+ J8o+ T5s+ T8o+ 95s+ 97o+ 85s+ 87o 74s+ 64s+ 53s+",
				"HU_1_9_NC");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q4o+ J2s+ J7o+ T4s+ T7o+ 95s+ 97o+ 85s+ 87o 74s+ 64s+ 53s+",
				"HU_1_8_NC");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J6o+ T3s+ T7o+ 95s+ 97o+ 84s+ 87o 74s+ 76o 64s+ 53s+ 43s",
				"HU_1_7_NC");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J4o+ T2s+ T6o+ 94s+ 97o+ 84s+ 86o+ 74s+ 76o 64s+ 53s+", "HU_1_6_NC");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T5o+ 93s+ 96o+ 84s+ 86o+ 74s+ 76o 64s+ 53s+", "HU_1_5_NC");
		addArrayFromParsedRangeToMap("22+ Jx+ T2s+ T3o+ 92s+ 95o+ 83s+ 85o+ 74s+ 76o 64s+ 53s+", "HU_1_4_NC");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 93o+ 82s+ 84o+ 73s+ 75o+ 63s+ 65o 53s+", "HU_1_3_NC");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 63o+ 52s+ 53o+ 42s+ 32s", "HU_1_2_NC");

		addArrayFromParsedRangeToMap("33+ A2s+ A3o+ K8s+ K9o+ Q9s+ QTo+ JTs", "HU_1_16_C");
		addArrayFromParsedRangeToMap("33+ Ax+ K7s+ K9o+ Q9s+ QTo+ JTs", "HU_1_15_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K6s+ K8o+ Q9s+ QTo+ JTs", "HU_1_14_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K5s+ K8o+ Q8s+ QTo+ J9s+ JTo", "HU_1_13_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K4s+ K8o+ Q8s+ Q9o+ J9s+ JTo", "HU_1_12_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q7s+ Q9o+ J8s+ JTo T9s", "HU_1_11_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K6o+ Q6s+ Q8o+ J8s+ J9o+ T9s", "HU_1_10_C");
		addArrayFromParsedRangeToMap("22+ Ax+ K2s+ K4o+ Q4s+ Q8o+ J7s+ J9o+ T8s+ T9o 98s", "HU_1_9_C");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q7o+ J6s+ J8o+ T7s+ T9o 98s", "HU_1_8_C");
		addArrayFromParsedRangeToMap("22+ Kx+ Q2s+ Q5o+ J5s+ J8o+ T7s+ T8o+ 97s+", "HU_1_7_C");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J6o+ T5s+ T7o+ 96s+ 98o 86s+", "HU_1_6_C");
		addArrayFromParsedRangeToMap("22+ Qx+ J2s+ J3o+ T2s+ T6o+ 94s+ 96o+ 85s+ 87o 75s+ 65s", "HU_1_5_C");
		addArrayFromParsedRangeToMap("22+ Tx+ 92s+ 95o+ 82s+ 85o+ 73s+ 75o+ 63s+ 65o 53s+ 43s", "HU_1_4_C");
		addArrayFromParsedRangeToMap("22+ 8x+ 72s+ 73o+ 62s+ 63o+ 5x+ 42s+ 43o 32s", "HU_1_3_C");
		addArrayFromParsedRangeToMap("2x+", "HU_1_2_C");

	}

	public void addArrayFromParsedRangeToMap(String range, String name) {
		ArrayList<String> array = new ArrayList<String>();
		String[] splitRange = range.split(" ");
		int index = 1;
		for (String s : splitRange) {
			String[] charSplit = s.split("");

			if (charSplit[2].equalsIgnoreCase("s") || charSplit[2].equalsIgnoreCase("o")) {
				if (charSplit.length == 4) // AQs+ etc
				{
					array = addHandsAboveAndIncludingIndexToRangeArray(array, charSplit[0], charSplit[1], charSplit[2]);
				}

				else // AQs
				{
					array = addSingleHandToRangeArray(array, charSplit[0], charSplit[1], charSplit[2]);
				}
			}

			else if (charSplit[0].equalsIgnoreCase(charSplit[1])) // QQ
			{
				array = addPairsAboveAndIncludingIndexToRangeArray(array, charSplit[1]);
			}

			else if (charSplit[1].equalsIgnoreCase("x")) // Ax+ etc
			{
				array = addHandsAboveAndIncludingIndexToRangeArrayNoSecondCard(array, charSplit[0]);
			}

			index++;
		}

		/*
		 * System.out.println("Printing array:");
		 * 
		 * for(String s : array) { System.out.println(s); }
		 */

		arrayMap.put(name, array);
		arrayRangeMap.put(name, range);
	}

	public ArrayList<String> addHandsAboveAndIncludingIndexToRangeArrayNoSecondCard(ArrayList<String> array,
			String index1) {
		int minCardIndex = 0;

		if (index1.equalsIgnoreCase("T")) {
			minCardIndex = 10;
		} else if (index1.equalsIgnoreCase("J")) {
			minCardIndex = 11;
		} else if (index1.equalsIgnoreCase("Q")) {
			minCardIndex = 12;
		} else if (index1.equalsIgnoreCase("K")) {
			minCardIndex = 13;
		} else if (index1.equalsIgnoreCase("A")) {
			minCardIndex = 14;
		} else {
			minCardIndex = Integer.parseInt(index1);
		}

		int finalCardOneIndex = minCardIndex - 2;

		String[] secondCardArray = new String[13];
		secondCardArray[0] = "2";
		secondCardArray[1] = "3";
		secondCardArray[2] = "4";
		secondCardArray[3] = "5";
		secondCardArray[4] = "6";
		secondCardArray[5] = "7";
		secondCardArray[6] = "8";
		secondCardArray[7] = "9";
		secondCardArray[8] = "T";
		secondCardArray[9] = "J";
		secondCardArray[10] = "Q";
		secondCardArray[11] = "K";
		secondCardArray[12] = "A";

		for (int j = finalCardOneIndex; j < 13; j++) {
			index1 = secondCardArray[j];

			for (int i = 0; i < 13; i++) {
				String index2 = secondCardArray[i];

				if (j < i) // If card two is bigger than card one, switch positions
				{
					String tempOne = index1;
					String tempTwo = index2;

					index1 = tempTwo;
					index2 = tempOne;
				}

				String finalHandString = index1 + index2 + "o";

				String finalHandStringV2 = index1 + index2 + "s";

				if (index1.equalsIgnoreCase(index2)) {
					finalHandString = index1 + index2;
				}

				if (!array.contains(finalHandString)) {
					array.add(finalHandString);
				}

				if (!array.contains(finalHandStringV2) && !index1.equalsIgnoreCase(index2)) {
					array.add(finalHandStringV2);
				}
			}
		}

		/*
		 * for(String s : array) { System.out.println(s); }
		 */

		return array;
	}

	public ArrayList<String> addHandsAboveAndIncludingIndexToRangeArray(ArrayList<String> array, String index1,
			String index2, String index3) {
		int cardTwoIndex = 0;

		if (index2.equalsIgnoreCase("T")) {
			cardTwoIndex = 10;
		} else if (index2.equalsIgnoreCase("J")) {
			cardTwoIndex = 11;
		} else if (index2.equalsIgnoreCase("Q")) {
			cardTwoIndex = 12;
		} else if (index2.equalsIgnoreCase("K")) {
			cardTwoIndex = 13;
		} else if (index2.equalsIgnoreCase("A")) {
			cardTwoIndex = 14;
		} else {
			cardTwoIndex = Integer.parseInt(index2);
		}

		int finalCardOneIndex = cardTwoIndex - 2;

		String[] secondCardArray = new String[13];
		secondCardArray[0] = "2";
		secondCardArray[1] = "3";
		secondCardArray[2] = "4";
		secondCardArray[3] = "5";
		secondCardArray[4] = "6";
		secondCardArray[5] = "7";
		secondCardArray[6] = "8";
		secondCardArray[7] = "9";
		secondCardArray[8] = "T";
		secondCardArray[9] = "J";
		secondCardArray[10] = "Q";
		secondCardArray[11] = "K";
		secondCardArray[12] = "A";

		for (int i = finalCardOneIndex; i < 13; i++) {
			String finalHandString = index1 + secondCardArray[i] + index3;
			String finalHandString2 = secondCardArray[i] + index1 + index3;

			if (index1.equalsIgnoreCase(secondCardArray[i])) {
				finalHandString = index1 + secondCardArray[i];
			}

			if (!array.contains(finalHandString) && !array.contains(finalHandString2)) {
				array.add(finalHandString);
			}
		}

		return array;
	}

	private ArrayList<String> addSingleHandToRangeArray(ArrayList<String> array, String index1, String index2,
			String index3) {
		int cardOneIndex = 0;

		if (index1.equalsIgnoreCase("T")) {
			cardOneIndex = 10;
		} else if (index1.equalsIgnoreCase("J")) {
			cardOneIndex = 11;
		} else if (index1.equalsIgnoreCase("Q")) {
			cardOneIndex = 12;
		} else if (index1.equalsIgnoreCase("K")) {
			cardOneIndex = 13;
		} else if (index1.equalsIgnoreCase("A")) {
			cardOneIndex = 14;
		} else {
			cardOneIndex = Integer.parseInt(index1);
		}

		int cardTwoIndex = 0;

		if (index2.equalsIgnoreCase("T")) {
			cardTwoIndex = 10;
		} else if (index2.equalsIgnoreCase("J")) {
			cardTwoIndex = 11;
		} else if (index2.equalsIgnoreCase("Q")) {
			cardTwoIndex = 12;
		} else if (index2.equalsIgnoreCase("K")) {
			cardTwoIndex = 13;
		} else if (index2.equalsIgnoreCase("A")) {
			cardTwoIndex = 14;
		} else {
			cardTwoIndex = Integer.parseInt(index2);
		}

		if (cardOneIndex < cardTwoIndex) // If card two is bigger than card one, switch positions
		{
			String tempOne = index1;
			String tempTwo = index2;

			index1 = tempTwo;
			index2 = tempOne;
		}

		String hand = index1 + index2 + index3;

		if (index1.equalsIgnoreCase(index2)) {
			hand = index1 + index2;
		}

		if (!array.contains(hand)) {
			array.add(hand);
		}

		return array;
	}

	public ArrayList<String> addPairsAboveAndIncludingIndexToRangeArray(ArrayList<String> array, String index) {
		int stdIndex = 0;

		if (index.equalsIgnoreCase("T")) {
			stdIndex = 10;
		} else if (index.equalsIgnoreCase("J")) {
			stdIndex = 11;
		} else if (index.equalsIgnoreCase("Q")) {
			stdIndex = 12;
		} else if (index.equalsIgnoreCase("K")) {
			stdIndex = 13;
		} else if (index.equalsIgnoreCase("A")) {
			stdIndex = 14;
		} else {
			stdIndex = Integer.parseInt(index);
		}

		int editedIndex = stdIndex - 2;

		String[] pairArray = new String[13];
		pairArray[0] = "22";
		pairArray[1] = "33";
		pairArray[2] = "44";
		pairArray[3] = "55";
		pairArray[4] = "66";
		pairArray[5] = "77";
		pairArray[6] = "88";
		pairArray[7] = "99";
		pairArray[8] = "TT";
		pairArray[9] = "JJ";
		pairArray[10] = "QQ";
		pairArray[11] = "KK";
		pairArray[12] = "AA";

		for (int i = editedIndex; i < 13; i++) {
			if (!array.contains(pairArray[i])) {
				array.add(pairArray[i]);
			}
		}

		return array;
	}

	public ArrayList<String> getRangeArray(String gameType, String playersLeft, String bigBlinds, String antes,
			boolean callMode) {
		if (gameType.equalsIgnoreCase("HU")) {
			if (callMode) {
				return arrayMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "C");
			}

			return arrayMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "NC");
		}

		return arrayMap.get(gameType + "_" + playersLeft + "_" + bigBlinds + "_" + antes);
	}

	public String getPushRangeString(String gameType, String playersLeft, String bigBlinds, String antes,
			boolean callMode) {
		if (gameType.equalsIgnoreCase("HU")) {
			if (callMode) {
				return arrayRangeMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "C");
			}

			return arrayRangeMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "NC");
		}

		return arrayRangeMap.get(gameType + "_" + playersLeft + "_" + bigBlinds + "_" + antes);
	}

	public boolean push(String gameType, String playersLeft, String bigBlinds, String antes, String hand,
			boolean callMode) {
		if (gameType.equalsIgnoreCase("HU")) {
			if (callMode) {
				return isHandInArray(arrayMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "C"), hand);
			}

			return isHandInArray(arrayMap.get(gameType + "_" + "1" + "_" + bigBlinds + "_" + "NC"), hand);
		}

		return isHandInArray(arrayMap.get(gameType + "_" + playersLeft + "_" + bigBlinds + "_" + antes), hand);
	}

	private boolean isHandInArray(ArrayList<String> array, String hand) {
		for (String h : array) {
			if (hand.equalsIgnoreCase(h)) {
				return true;
			}
		}

		return false;
	}
}
