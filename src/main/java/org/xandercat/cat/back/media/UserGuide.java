package org.xandercat.cat.back.media;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for extracting user guide from archive and displaying it for user.
 * 
 * @author Scott Arnold
 */
public class UserGuide {

	private static final String externalPath = "catback-userguide/";
	private static final String resourcePath = "/userguide/";
	private static final String[] resources = new String[] {
			"catback-userguide.html", "catback.png", "catback-thumb.png", "catback-backup-settings.png", "catback-backup-settings-thumb.png", 
			"catback-included-files.png", "catback-included-files-thumb.png", "catback-name-location.png", "catback-name-location-thumb.png",
			"checked.png", "partialcheck.png", "unchecked.png", "Symbols-Forbidden-16x16.png", "Symbols-Warning-16x16.png"
	};
	private static final File externalDirectory = new File(externalPath);
	private static final File externalHtmlFile = new File(externalDirectory, resources[0]);
	
	public static void extract() throws IOException {
		if (!externalDirectory.exists()) {
			externalDirectory.mkdir();
		}
		for (String resource : resources) {
			File externalFile = new File(externalDirectory, resource);
			try(InputStream inputStream = UserGuide.class.getResourceAsStream(resourcePath + resource); 
					OutputStream outputStream = new FileOutputStream(externalFile)) {
				final byte[] buffer = new byte[8192];
				int length;
				while ((length = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, length);
				}
			};
			
		}
	}
	
	public static void display() throws IOException {
		if (!verify()) {
			extract();
		}
		Desktop.getDesktop().browse(externalHtmlFile.toURI());
	}
	
	public static boolean verify() {
		return externalHtmlFile.isFile();
	}
}
