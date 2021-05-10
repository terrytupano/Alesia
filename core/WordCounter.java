package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import com.alee.utils.*;

public class WordCounter {

	private static TreeMap<String, String> Dicctionary;
	private static TreeMap<String, Integer> wordCount;

	public static void main(String[] args) {

		Dicctionary = new TreeMap<>();
		Dicctionary.put("com.alee*", "WebLaF");
		Dicctionary.put("com.jgoodies.forms*", "JGoodies Forms");
		Dicctionary.put("java.awt", "Abstract Window Toolkit (AWT)");
		// Dicctionary.put("java.awt.geom*", "Java 2D");
		Dicctionary.put("java.awt.image*", "Java 2D");
		Dicctionary.put("java.io*", "Serialization");
		Dicctionary.put("java.util.logging*", "Logging");
		Dicctionary.put("javax.swing*", "Swing");
		Dicctionary.put("net.sourceforge.tess4j*", "Tesseract for Java");
		Dicctionary.put("org.apache.shiro*", "Apache Shiro");
		Dicctionary.put("org.apache.commons", "Apache Commons");
		Dicctionary.put("org.apache.commons.math3*", "Apache Math");
		Dicctionary.put("org.jdesktop*", "JSR296");
		Dicctionary.put("org.jfree*", "JFree Chart");
		Dicctionary.put("org.apache.poi*", "Apache Poi");
		Dicctionary.put("java.beans*", "Java Beans");

		Dicctionary.put("java.lang.reflect", "Java Reflection");
		
		Dicctionary.put("javax.imageio*", "Image I/O");
		Dicctionary.put("java.util", "Lang and Utils");

		Dicctionary.put("marvin", "Image processing");
		
		Dicctionary.put("marvin", "Text, Dates, Numbers, and Messages");
		
		
		
		Dicctionary.put("java.sql*", "JDBC");
		Dicctionary.put("javax.sql*", "JDBC");
		
		Dicctionary.put("org.javalite.activejdbc*", "ActiveJDBC");
		
		
		String project = "C:\\Users\\terry\\Documents\\java project\\Alesia";

		wordCount = new TreeMap<>();

		ArrayList<String> diccKeys = new ArrayList<>(Dicctionary.keySet());
		ArrayList<File> files = new ArrayList<>(
				FileUtils.findFilesRecursively(project, fn -> fn.getName().endsWith(".java")));
		for (File file : files) {
			// System.out.println("processing " + file);
			// if (file.toString().equals(""))
			// System.out.println("WordCounter.main()");
			List<String> impLin;
			try {
				impLin = Files.lines(file.toPath()).filter(line -> line.startsWith("import"))
						.collect(Collectors.toList());
			} catch (Exception e) {
				System.err.println("Error in file: " + file);
				// e.printStackTrace();
				continue;
			}
			for (String line : impLin) {
				String pack = line.split("\\h")[1].replace(".*;", "").replace(";", "");
				String pName = diccKeys.stream().filter(key -> TStringUtils.wildCardMacher(pack, key)).findFirst()
						.orElse(null);
				String libNam = pName != null ? Dicctionary.get(pName) : "*Unk " + pack;
				int c = wordCount.getOrDefault(libNam, 0);
				wordCount.put(libNam, ++c);
			}
		}

		wordCount.keySet().stream().forEach(key -> System.out.println(wordCount.get(key) + "\t" + key));
	}
}
