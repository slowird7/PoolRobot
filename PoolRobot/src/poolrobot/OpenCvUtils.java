/*
 * https://stackoverflow.com/questions/34771380/how-to-convert-a-javafx-image-to-a-opencv-matrix/41444430
 * "How to convert a JavaFX image to a OpenCV matrix?"
 * http://www.codeproject.com/Tips/752511/How-to-Convert-Mat-to-BufferedImage-Vice-Versa
 * "How to Convert Mat to BufferedImage & Vice Versa"
 */
package poolrobot;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2HSV;

public class OpenCvUtils {

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    public static Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static Mat image2Mat(Image image) {

        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);

        return bufferedImage2Mat(bImage);

    }

    // http://www.codeproject.com/Tips/752511/How-to-Convert-Mat-to-BufferedImage-Vice-Versa
    public static Mat bufferedImage2Mat(BufferedImage in) {
        Mat out;
        byte[] data;
        int r, g, b;
        int height = in.getHeight();
        int width = in.getWidth();
        if (in.getType() == BufferedImage.TYPE_INT_RGB || in.getType() == BufferedImage.TYPE_INT_ARGB) {
            out = new Mat(height, width, CvType.CV_8UC3);
            data = new byte[height * width * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
            for (int i = 0; i < dataBuff.length; i++) {
                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
            }
        } else {
            out = new Mat(height, width, CvType.CV_8UC1);
            data = new byte[height * width * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, width, height, null, 0, width);
            for (int i = 0; i < dataBuff.length; i++) {
                r = (byte) ((dataBuff[i] >> 16) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
            }
        }
        out.put(0, 0, data);
        return out;
    }

    public static String getOpenCvResource(Class<?> clazz, String path) {
        try {
            return Paths.get(clazz.getResource(path).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Convert image to Mat
    // alternate version http://stackoverflow.com/questions/21740729/converting-bufferedimage-to-mat-opencv-in-java
    public static Mat bufferedImage2Mat_v2(BufferedImage im) {

        im = toBufferedImageOfType(im, BufferedImage.TYPE_3BYTE_BGR);

        // Convert INT to BYTE
        //im = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        // Convert bufferedimage to byte array
        byte[] pixels = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();

        // Create a Matrix the same size of image
        Mat image = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
        // Fill Matrix with image values
        image.put(0, 0, pixels);

        return image;

    }

    private static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }

        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        } finally {
            g.dispose();
        }

        return image;
    }

    
}
