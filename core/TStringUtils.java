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

import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.javalite.activejdbc.*;

import com.alee.utils.*;

/**
 * Utils class for String and {@link TEntry} Manipulations
 * 
 */
public class TStringUtils {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat();
	private static Random random = new Random();
	/**
	 * contains all the .propertie files. (Alesia core) and detected plugins.
	 */
	private static Properties allProperties = new Properties();
	// private static ArrayList<Properties> properties = new ArrayList<>();

	/**
	 * Return a formatted string representing the speed or time of the input argument. The incomming argument represent
	 * an elapsed amound of time.
	 * <p>
	 * This method is not a presition method. is intended only for rougly format an amount of time. e.g. millis=1456
	 * will be 1456 Ms but millis=2456 will be formatted as 2 Sec
	 * 
	 * @param millis
	 * @return
	 */
	public static String formatSpeed(long millis) {
		String rs = millis + " Ms";
		millis = millis / 1000;
		rs = millis > 1 ? millis + " Sec" : rs;
		millis = millis / 60;
		rs = millis > 1 ? millis + " Min" : rs;
		millis = millis / 60;
		rs = millis > 1 ? millis + " Hour" : rs;
		millis = millis / 24;
		rs = millis > 1 ? millis + " Day" : rs;
		millis = millis / 365;
		rs = millis > 1 ? millis + " Year" : rs;
		return rs;
	}
	/**
	 * return a {@link TreeMap} of string with all properties from all .properties files registres in Alesia) that
	 * beging with the given argument. the key for the returned list will be the property and the value, the property
	 * value.
	 * <p>
	 * NOTE: the list is sort accordint to the key argument from the property file.
	 * 
	 * @param group - prefix of the property to look.
	 * 
	 * @return list for property values
	 */
	public static TreeMap<String, String> getProperties(String group) {
		ArrayList<String> keys = new ArrayList(allProperties.keySet());
		TreeMap<String, String> list = new TreeMap<>();
		for (String key : keys) {
			if (key.startsWith(group)) {
				list.put(key, allProperties.getProperty(key));
			}
		}
		// Hashtable<String, String> l2 = new Hashtable<>(list);
		// return l2;
		return list;
	}

	/**
	 * same as {@link #getTEntryGroup(String)} but in {@link Hashtable} format
	 * 
	 * @param group - prefix for a valid tentry group
	 * 
	 * @return list of elements
	 */
	public static Hashtable<String, String> getHashtableGroup(String group) {
		TEntry[] telist = getTEntryGroup(group);
		Hashtable<String, String> hashtable = new Hashtable<>();
		for (TEntry tEntry : telist) {
			hashtable.put(tEntry.getKey().toString(), tEntry.getValue().toString());
		}
		return hashtable;
	}

	/**
	 * look for the constants from all .properties files load in the system and retunr a array of {@link TEntry}. the
	 * <code>group</code> argument must point to a valid standar key;value porperty. for examples <br>
	 * <code>
	 * <br>prefix01 = key1;value1
	 * <br>prefix02 = key2;value2
	 * <br>...
	 * <br>prefix99 = key99;value99
	 * </code>
	 * 
	 * @param group - prefix for the group of constant.
	 * @return array of entries
	 */
	public static TEntry[] getTEntryGroup(String group) {
		TreeMap<String, String> prps = getProperties(group);
		// 210213: jajaja ayer fue 12022021. palindrome y ambigram: se puede leer izq->der, der->izq y de arriba a
		// abajo. mi ex profe Maria me mando ese meme
		TEntry[] rntarray = new TEntry[prps.size()];
		ArrayList<String> vals = new ArrayList(prps.values());
		for (int i = 0; i < rntarray.length; i++) {
			String[] kv = vals.get(i).split(";");
			rntarray[i] = new TEntry<>(kv[0], kv[1]);
		}
		return rntarray;
	}

	public static TEntry getTEntry(String tid) {
		TEntry[] te = getTEntryGroup(tid);
		return (te == null) ? new TEntry(tid, tid) : te[0];
	}

	/**
	 * return a hex representation of digested string that has been cypher by MD5 algorithm.
	 * 
	 * @param srcs - source string to digest
	 * @return digested string in hex
	 */
	public static String getDigestString(String srcs) {
		String dmsg = "";
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// terry in numeric keys 53446
			messageDigest.update((srcs + "Terry").getBytes());
			byte[] bytes = messageDigest.digest();
			BigInteger bi = new BigInteger(1, bytes);
			dmsg = String.format("%0" + (bytes.length << 1) + "X", bi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dmsg;
	}

	/**
	 * crea una instanica de <code>Date</code> que representa el monemto actual y lo retorna como una cadena de
	 * caracteres segun el formato pasado como argumento
	 * 
	 * @param f formato <code>(ver SimpleDateFormat)</code>
	 * @return
	 */
	public static String getStringDate(String f) {
		return getStringDate(new Date(), f);
	}

	/**
	 * retorna la instancia de <code>Date</code> pasada como argumento en una String con el formato
	 * <code>f (ver SimpleDateFormat)</code>
	 * 
	 * @param d - instancia de <code>Date</code>
	 * @param f - formato
	 * @return String
	 */
	public static String getStringDate(Date d, String f) {
		dateFormat.applyPattern(f);
		return dateFormat.format(d);
	}

	/**
	 * @since 2.3
	 * 
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		String txt = allProperties.getProperty(key);
		return (txt == null) ? key : txt;
	}

	/**
	 * compara las 2 instancias de <code>Date</code> pasadas como argumento y retorna un valor < 0 si d1 < d2, 0 si d1 =
	 * d2 o un valor > 0 si d1 > d2. La comparacion es establecida con la presicion del argumento <code>fmt</code>
	 * 
	 * @param d1 - instancia de <code>Date</code> menor
	 * @param d2 - instancia de <code>Date</code> mayor
	 * @param fmt - precision o formato (ver SimpleDateFormat)
	 * @return valor segun comparacion
	 */
	public static int compare(Date d1, Date d2, String fmt) {
		dateFormat.applyPattern(fmt);
		String d_1 = dateFormat.format(d1);
		String d_2 = dateFormat.format(d2);
		return d_1.compareTo(d_2);
	}

	/**
	 * retorna un indentificador unico en formato estandar xx-xxx-xxx
	 * 
	 * @return <code>String</code> con identificador unico
	 */
	public static String getUniqueID() {
		String cern = "000";
		String f1 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f2 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String f3 = cern + Integer.toHexString(random.nextInt()).toUpperCase();
		String cern1 = f1.substring(f1.length() - 2) + "-" + f2.substring(f2.length() - 3) + "-"
				+ f3.substring(f3.length() - 3);
		// System.out.println(cern1);
		return cern1;
	}

	/**
	 * generate a OTP for the given user. the password lenght MUST BE 6 char long (this is necesary because this system
	 * use the length of store passgord to determine if that field value is a OTP.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #verifyOneTimePassword(String, String)
	 * @param usid - user to generate the otp
	 * 
	 * @return password to store
	 */
	public static String getOneTimePassword(String uid) {
		String nextp = null;
		try {
			nextp = getPasscodeGenerator(uid).generateTimeoutCode();
		} catch (Exception e) {

		}
		return nextp;
	}
	/**
	 * create a standar {@link PasscodeGenerator}
	 * 
	 * @param uid - uid to generate/verfy OTP
	 * 
	 * @return PasscodeGenerator
	 */
	private static PasscodeGenerator getPasscodeGenerator(String uid) {
		try {
			int to = Integer.parseInt(System.getProperty("Alesia.OTPTimeout"));
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(new SecretKeySpec(uid.toLowerCase().getBytes(), ""));
			PasscodeGenerator pcg = new PasscodeGenerator(mac, 6, to * 60);
			return pcg;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * verify if the given OTP is still valid for the user pass as argument.
	 * 
	 * @see #getPasscodeGenerator(String)
	 * @see #getOneTimePassword(String)
	 * 
	 * @param uid - user to verify
	 * @param p - OTP to check
	 * 
	 * @return <code>true</code> if otp still valid
	 */
	public static boolean verifyOneTimePassword(String uid, String p) {
		boolean ok = false;
		try {
			ok = getPasscodeGenerator(uid).verifyTimeoutCode(p, 0, 0);
		} catch (Exception e) {

		}
		return ok;
	}

	/**
	 * Parse the properties stored in String <code>prplis</code> and append in the <code>prps</code> properti objet
	 * 
	 * @param pl - String of properties stores in standar format (key;value;key;value...)
	 * @param prps - {@code Properties} to append the parsed string
	 */
	public static void parseProperties(String pl, Properties prps) {
		if (!pl.equals("")) {
			String[] kv = pl.split(";");
			for (int j = 0; j < kv.length; j = j + 2) {
				prps.put(kv[j], kv[j + 1]);
			}
		}
	}

	/**
	 * retorna texto identificador de aplicacion y version. generalmente usado par grabar en archivos externos
	 * 
	 * @return String en formato <b>Clio Version: 1.36 Update: 0</b> o similar
	 */
	public static String getAboutAppShort() {
		return TStringUtils.getString("name") + " " + TStringUtils.getString("version") + " "
				+ TStringUtils.getString("vendor");

		// return TStringUtils.getString("about.app.name") + " " + TStringUtils.getString("about.version") + " "
		// + SystemVariables.getStringVar("versionID") + " " + TStringUtils.getString("about.update") + " "
		// + SystemVariables.getStringVar("updateID");
	}

	public static String getRecordId() {
		long mill = System.currentTimeMillis();
		// long nanos = System.nanoTime();
		// until better solution, ensure unleast 1 mill from previous method call
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		return Long.toHexString(mill);
	}

	/**
	 * Given a text and a wildcard pattern, implement wildcard pattern matching algorithm that finds if wildcard pattern
	 * is matched with text. The matching should cover the entire text (not partial text).
	 * <p>
	 * The wildcard pattern can include the characters �?� and �*�
	 * <li>�?� � matches any single character
	 * <li>�*� � Matches any sequence of characters (including the empty sequence)
	 * <p>
	 * examples
	 * 
	 * <br>
	 * String pattern = "ba*****ab"; <br>
	 * String pattern = "ba*ab"; <br>
	 * String pattern = "a*ab"; <br>
	 * String pattern = "a*****ab"; <br>
	 * String pattern = "*a*****ab"; <br>
	 * String pattern = "ba*ab****"; <br>
	 * String pattern = "****"; <br>
	 * String pattern = "*"; <br>
	 * String pattern = "aa?ab"; <br>
	 * String pattern = "b*b"; <br>
	 * String pattern = "a*a"; <br>
	 * String pattern = "baaabab"; <br>
	 * String pattern = "?baaabab"; <br>
	 * String pattern = "*baaaba*";
	 * 
	 * @param string - string to compare
	 * @param pattern - pattern with wildcard caracters
	 * @since 2.3
	 * @return <code>true</code> if the string pass the pattern
	 */
	public static boolean wildCardMacher(String string, String pattern) {

		// static boolean wlidCardFilter(String str, String pattern, int n, int m) {
		// terry: get m and n from the length of the strings
		String str = string == null ? "" : string;
		String patt = pattern == null ? "" : pattern;

		int n = str.length();
		int m = patt.length();

		// empty pattern can only match with
		// empty string
		if (m == 0)
			return (n == 0);

		// lookup table for storing results of
		// subproblems
		boolean[][] lookup = new boolean[n + 1][m + 1];

		// initailze lookup table to false
		for (int i = 0; i < n + 1; i++)
			Arrays.fill(lookup[i], false);

		// empty pattern can match with empty string
		lookup[0][0] = true;

		// Only '*' can match with empty string
		for (int j = 1; j <= m; j++)
			if (patt.charAt(j - 1) == '*')
				lookup[0][j] = lookup[0][j - 1];

		// fill the table in bottom-up fashion
		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				// Two cases if we see a '*'
				// a) We ignore '*'' character and move
				// to next character in the pattern,
				// i.e., '*' indicates an empty sequence.
				// b) '*' character matches with ith
				// character in input
				if (patt.charAt(j - 1) == '*')
					lookup[i][j] = lookup[i][j - 1] || lookup[i - 1][j];

				// Current characters are considered as
				// matching in two cases
				// (a) current character of pattern is '?'
				// (b) characters actually match
				else if (patt.charAt(j - 1) == '?' || str.charAt(i - 1) == patt.charAt(j - 1))
					lookup[i][j] = lookup[i - 1][j - 1];

				// If characters don't match
				else
					lookup[i][j] = false;
			}
		}

		return lookup[n][m];
	}

	/**
	 * Return a standar decimal format pattern when is necesary display all decimal places. this implementation show
	 * until 10 decimal places. this method return the standar 2 places decimal digits if dp < 3.
	 * 
	 * @param dp - decimal pattern that show until 10 decimal places
	 * 
	 * @return format
	 */
	public static String getFormattForDecimalPlaces(int dp) {
		String fmt = "#,###,###,##0.00;-#,###,###,##0.00";
		if (dp > 2) {
			// String doublefmt = "#,###,###,##0.?;-#,###,###,##0.?";
			String doublefmt = "#,###,###,##0.?;-#,###,###,##0.?";
			String zeros = "0000000000";
			String t = zeros.substring(0, Math.min(10, dp));
			fmt = doublefmt.replace("?", t);
		}
		return fmt;
	}

	/**
	 * retun a list of {@link TEntry} constructed with the fields values passes as argument.
	 * 
	 * @param list - list of row from database
	 * @param key - field name for key
	 * @param value - field name for value
	 * @return list of {@link TEntry}
	 * @since 2.3
	 */
	public static ArrayList<TEntry> getTEntryGroupFrom(LazyList<Model> list, String key, String value) {
		ArrayList<TEntry> entrys = new ArrayList<>();
		for (Model ele : list) {
			entrys.add(new TEntry(ele.get(key), ele.get(value)));
		}
		return entrys;
	}

	/**
	 * find recursively all <code>.properties</code> files starting from the give directoy and merge all properties
	 * files found whit the main property list
	 * 
	 * @since 2.3
	 * @param directory to look at
	 */
	public static void addProperties(String dir) {
		try {
			List<File> files = FileUtils.findFilesRecursively(dir, f -> f.getName().endsWith(".properties"));
			for (File file : files) {
				Properties prp = new Properties();
				prp.load(new FileInputStream(file));
				allProperties.putAll(prp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
