package core;
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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import com.alee.api.resource.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;

public class TResources {

	public static String USER_DIR = System.getProperty("user.dir");
	private static String TEMP_PATH = System.getProperty("java.io.tmpdir") + "Alesia/";
	private static List<File> RESOURCES_FOLDERS;

	public static void init() {
		// scan all resources folder
		RESOURCES_FOLDERS = FileUtils.findFilesRecursively(USER_DIR + "/bin",
				f -> f.getName().contains("resources") && f.isDirectory());

		// I never delete temporal path just in case need look for something
		File td = new File(TEMP_PATH);
		if (!td.exists()) {
			td.mkdir();
		}
	}

	/**
	 * perform a <b>cmdow.exe</b> command.
	 * 
	 * @param winId - window ID
	 * @param cmdowParm - command to perform
	 * @see #getActiveWindows(String)
	 */
	public static void performCMDOWCommand(String winId, String cmdowParm) {
		try {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("cmdow.exe " + winId + " " + cmdowParm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Consult the <b>cmdow.exe</b> program and look for the active windows who match the partialName parameter. The
	 * result is stored in a {@link List} of {@link TEntry} where the key is the window identificator and the value is
	 * the window title bar.
	 * 
	 * <p>
	 * This method use the name parameter and fill the resulting list whit a patter matchin obtained from concating
	 * <code>*partialName*</code>
	 * 
	 * @param partialName - partial name of the window to look for
	 * 
	 * @return list of {@link TEntry} with the id and title bar text.
	 * @see #performCMDOWCommand(String, String)
	 */
	public static ArrayList<TSEntry> getActiveWindows(String partialName) {
		Preconditions.checkState(partialName.length() > 2, "pattern parameter must be of length > 2");
		ArrayList<TSEntry> windows = new ArrayList<>();
		String pattern = "*" + partialName + "*";
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec("cmdow.exe /t /b /f");
			InputStream is = process.getInputStream();
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (TStringUtils.wildCardMacher(line, pattern)) {
					String[] fields = line.split("\\s");
					String titt = "";
					for (int t = 8; t < fields.length; t++)
						titt = (fields[t].trim().equals("")) ? titt : titt + " " + fields[t].trim();
					windows.add(new TSEntry(fields[0].trim(), titt));
				}
			}
			is.close();
			sc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return windows;
	}

	/**
	 * Read and return the content of the file <code>fn</code> in byte array. This method throw and
	 * {@link ApplicationException} if the file length is > 16 Mg
	 * 
	 * @param fn - File to read
	 * @return
	 * @throws Exception - if any error or file length is > 16Mg
	 */
	public static byte[] loadFile(String fn) throws Exception {
		byte[] docData = null;
		File sf = new File(fn);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sf));
		// verifica longitud
		int flen = 0;
		if (flen / 1024 < 16000) {
			docData = new byte[(int) sf.length()];
			bis.read(docData);
			bis.close();
		} else {
			bis.close();
			// throw new TError("resource.msg01");
		}
		return docData;
	}

	public static File createTemporalDirectory(String ldn) {
		File td = new File(TEMP_PATH + ldn + "_" + Long.toHexString(System.currentTimeMillis()));
		td.mkdir();
		return td;
	}

	/**
	 * return a list of files which file name contain the substring {@code subst}. this method look recursibily starting
	 * form the <code>dir</code> argument
	 * 
	 * @param dir - starting dir file
	 * @param extention - file name must end with this extention
	 * 
	 * @return list of files fount or an empy list if no files was found
	 */
	public static List<File> findFiles(File dir, String extention) {
		Preconditions.checkArgument(dir.isDirectory(), "The file is not a file directory");
		return FileUtils.findFilesRecursively(dir, f -> f.getName().endsWith(extention));
	}

	public static FileResource getFile(String fileName) {
		List<File> files = FileUtils.findFilesRecursively(USER_DIR, f -> f.getName().contains(fileName));
		if (!files.isEmpty())
			return new FileResource(files.get(0));
		return null;
	}

	public static ImageIcon getIcon(String iconName) {
		Preconditions.checkNotNull(iconName, "iconName argument can.t be null");
		// to clean the app. only names with extension
		Preconditions.checkArgument(iconName.contains("."), "'"+iconName+"'" + " iconName don.t contain a valid image extention.");
		for (File file : RESOURCES_FOLDERS) {
			File file2 = new File(file.getPath() + "/" + iconName);
			// ImageUtils.SUPPORTED_IMAGES
			if (file2.exists())
				return new ImageIcon(file2.getAbsolutePath());
		}
		return null;
	}

	public static ImageIcon getIcon(String iconName, int size) {
		ImageIcon ii = getIcon(iconName);
		if (ii == null) {
			return null;
		}
		Image i = ii.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(i);
	}

	public static ImageIcon getSmallIcon(String iconName) {
		return getIcon(iconName, 16);
	}

	/**
	 * Utility method to convert byte array to object. the return value is deserialized using
	 * {@link ObjectInputStream#readObject()} method. Designed for store purpose
	 * 
	 * @param bao - byte array of stored object
	 * @return object read form serialized form
	 */
	public static Object getObjectFromByteArray(byte[] bao) {
		Object o = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bao);
			ObjectInputStream in = new ObjectInputStream(bis);
			o = in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * utility to conver from {@link ImageIcon} to array of byte
	 * 
	 * @param ii - image icon
	 * @return array of byte
	 */
	public static byte[] getBytearrayFromImage(ImageIcon ii) {
		byte[] dta = null;
		try {
			BufferedImage bi = new BufferedImage(ii.getIconWidth(), ii.getIconHeight(), BufferedImage.TYPE_INT_RGB);
			bi.getGraphics().drawImage(ii.getImage(), 0, 0, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "JPG", baos);
			dta = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dta;
	}

	/**
	 * Return the <code>val</code> argument in byte array generated by {@link ObjectOutputStream#writeObject(Object)}
	 * methdo execution. this method is used generaly to store data inside a <code>BLOG</code> field
	 * 
	 * @param val - object
	 * @return serialized object
	 */
	public static byte[] getByteArrayFromObject(Object val) {
		byte[] rdta = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(val);
			out.flush();
			rdta = bos.toByteArray();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rdta;
	}
}
