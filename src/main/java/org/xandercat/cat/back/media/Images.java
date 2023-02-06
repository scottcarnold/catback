package org.xandercat.cat.back.media;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xandercat.swing.cache.WeakReferenceCache;

/**
 * This class serves as a common location for all Cat application images.  Images
 * are loaded as they are needed.
 * 
 * @author Scott Arnold
 */
public class Images extends WeakReferenceCache<String, Image> {

	public static final String CATBACK = "image/catback_icon.png";
	public static final String GLACIER = "image/Glacier.jpg";

	private static final Logger log = LogManager.getLogger(Images.class);
	private static final Images instance = new Images();
	
	/**
	 * Gets the image of given name.  Image name should be a resource path relative
	 * to this class.  Common image names are available as constants defined in this class.
	 * 
	 * @param imageName		relative resource path for image
	 * 
	 * @return				image
	 */
	public static synchronized Image getImage(String imageName) {
		return instance.get(imageName);
	}

	@Override
	protected Image loadValue(String key) {
		try {
			return ImageIO.read(getClass().getResource(key));
		} catch (IOException e) {
			log.error("Unable to load image " + key, e);
		}
		return null;
	}
}
