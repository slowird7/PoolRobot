package tstracker;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
//import org.opencv.core.Mat;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import poolrobot.PoolRobot;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion. Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.0 (2016-09-17)
 * @since 1.0
 *
 */
public final class Utils {

//    private final static Logger logger = LogManager.getLogger(Utils.class);

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame) {
	try {
            BufferedImage image = matToBufferedImage(frame);
            if (image == null) {
                return null;
            }
	    return SwingFXUtils.toFXImage(image, null);
	} catch (Exception e) {
	    PoolRobot.logger.warning("mat2Image>");
	    return null;
	}
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the JavaFX thread, to properly update the UI
     *
     * @param property a {@link ObjectProperty}
     * @param value the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
	Platform.runLater(() -> {
	    property.set(value);
	});
    }

    /**
     * Support for the {@link mat2image()} method
     *
     * @param original the {@link Mat} object in BGR or grayscale
     * @return the corresponding {@link BufferedImage}
     */
    private static BufferedImage matToBufferedImage(Mat original) {
	// init
        if (original == null) {
            return null;
        }
	BufferedImage image = null;
	int width = original.width(), height = original.height(), channels = original.channels();
	byte[] sourcePixels = new byte[width * height * channels];
	original.get(0, 0, sourcePixels);
        if (width > 0 && height > 0) {
            if (original.channels() > 1) {
                image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            } else {
                image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            }
            final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        }
	return image;
    }
}
