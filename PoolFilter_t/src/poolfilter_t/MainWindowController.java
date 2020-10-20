/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolfilter_t;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.circle;

/**
 *
 * @author otsuka
 */
public class MainWindowController implements Initializable {
    
    static{
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @FXML
    private AnchorPane ap;
    @FXML
    private Pane sp;
    @FXML
    private TextField txtProjectName;
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnSaveAs;
    
    @FXML
    private TextField txtHueBegin;
    @FXML
    private TextField txtHueEnd;
    @FXML
    private Slider sldHueBegin;
    @FXML
    private Slider sldHueEnd;
    
    @FXML
    private TextField txtSatMin;
    @FXML
    private TextField txtSatMax;
    @FXML
    private Slider sldSatMin;
    @FXML
    private Slider sldSatMax;
    
    @FXML
    private TextField txtValMin;
    @FXML
    private TextField txtValMax;
    @FXML
    private Slider sldValMin;
    @FXML
    private Slider sldValMax;

    @FXML
    private TextField txtParam1;
    @FXML
    private TextField txtParam2;
    @FXML
    private Slider sldParam1;
    @FXML
    private Slider sldParam2;
    
    @FXML
    private ToggleButton btnSwitch;
    
    @FXML
    private ImageView imageView;
    
    Stage me;
    FileChooser fileChooser;
    Mat imageBGR, imageHSV, imageResult;
    File imageFile;
    File parameterFile;
    Properties parameters;
    ArrayList<Circle> circles = new ArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createMonitoredSlider(sldHueBegin, txtHueBegin);
        createMonitoredSlider(sldHueEnd, txtHueEnd);
        createMonitoredSlider(sldSatMin, txtSatMin);
        createMonitoredSlider(sldSatMax, txtSatMax);
        createMonitoredSlider(sldValMin, txtValMin);
        createMonitoredSlider(sldValMax, txtValMax);
        createMonitoredSlider(sldParam1, txtParam1);
        createMonitoredSlider(sldParam2, txtParam2);
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        imageHSV = new Mat();
        imageResult = new Mat();
        parameters = new Properties();
        
    }    

    private void createMonitoredSlider(Slider slider, TextField txtField) {

        txtField.textProperty().bindBidirectional(slider.valueProperty(), new NumberStringConverter());
        slider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(
                    ObservableValue<? extends Boolean> observableValue,
                    Boolean wasChanging,
                    Boolean changing) {
//                String valueString = String.format("%d", slider.getValue());

                if (changing) {
                } else {
                    updateImage();
                }
            }
        });
        
    }
    
    @FXML
    private void onBtLoadImageClicked() {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.bmp"));
        fileChooser.setInitialDirectory(new File("image/9ball"));
        imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            imageBGR = imread(imageFile.getAbsolutePath(), 1);
            Imgproc.cvtColor(imageBGR, imageHSV, Imgproc.COLOR_BGR2HSV);
            txtProjectName.setText(imageFile.getName());
            updateImage();
        }
    }
    
    @FXML
    private void onBtLoadClicked() {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.txt", "*.txt"));
        fileChooser.setInitialDirectory(new File("parameter"));
        parameterFile = fileChooser.showOpenDialog(null);
        if (parameterFile != null && parameterFile.isFile()) {
            loadParameter(parameterFile);
            updateImage();
        }
    }

    @FXML
    private void onBtSaveClicked() {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.txt", "*.txt"));
        parameterFile = fileChooser.showSaveDialog(null);
        if (parameterFile != null) {
            saveParameter(parameterFile);
        }
    }
    
    @FXML
    private void onBtSaveAsClicked() {
        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.txt", "*.txt"));
        parameterFile = fileChooser.showSaveDialog(null);
        if (parameterFile != null) {
            saveParameter(parameterFile);
        }
    }
    
    @FXML
    private void onBtnSwitchClicked(ActionEvent ev) {
        if (btnSwitch.isSelected()) {
            imageView.setImage(OpenCvUtils.mat2Image(imageBGR));
        } else {
            imageView.setImage(OpenCvUtils.mat2Image(imageResult));            
        }
    }
    
    private void loadParameter(File parameterFile) {
        try {
            parameters.load(new BufferedReader(new FileReader(parameterFile)));
            sldHueBegin.setValue(Double.parseDouble( parameters.getProperty("hueBegin", "0")));
            sldHueEnd.setValue(Double.parseDouble( parameters.getProperty("hueEnd", "180")));
            sldSatMin.setValue(Double.parseDouble( parameters.getProperty("satMin", "0")));
            sldSatMax.setValue(Double.parseDouble( parameters.getProperty("satMax", "255")));
            sldValMin.setValue(Double.parseDouble( parameters.getProperty("valMin", "0")));
            sldValMax.setValue(Double.parseDouble( parameters.getProperty("valMax", "255")));
            sldParam1.setValue(Double.parseDouble( parameters.getProperty("param1", "50")));
            sldParam2.setValue(Double.parseDouble( parameters.getProperty("param2", "30")));
        } catch (IOException e) {
            // ファイル読み込みに失敗
            System.out.println(String.format("ファイルの読み込みに失敗しました。ファイル名:%s", parameterFile.getAbsoluteFile()));
        }
    }

    private void saveParameter(File parameterFile) {
        parameters.setProperty("hueBegin", Double.toString(sldHueBegin.getValue()));
        parameters.setProperty("hueEnd", Double.toString(sldHueEnd.getValue()));
        parameters.setProperty("satMin", Double.toString(sldSatMin.getValue()));
        parameters.setProperty("satMax", Double.toString(sldSatMax.getValue()));
        parameters.setProperty("valMin", Double.toString(sldValMin.getValue()));
        parameters.setProperty("valMax", Double.toString(sldValMax.getValue()));
        try(FileOutputStream f = new FileOutputStream(parameterFile);
            BufferedOutputStream b = new BufferedOutputStream(f)){
            parameters.store(b, "");
        } catch (IOException e) {
            // ファイル読み込みに失敗
            System.out.println(String.format("ファイルの書き出しに失敗しました。ファイル名:%s", parameterFile.getAbsoluteFile()));
        }
    }
    
    private void updateImage() {
        double hueBegin = sldHueBegin.getValue();
        double hueEnd = sldHueEnd.getValue();
        double satMin = sldSatMin.getValue();
        double satMax = sldSatMax.getValue();
        double valMin = sldValMin.getValue();
        double valMax = sldValMax.getValue();

        if (hueBegin <= hueEnd) {
            Core.inRange(imageHSV, new Scalar(hueBegin, satMin, valMin), new Scalar(hueEnd, satMax, valMax), imageResult);
        } else {
            Mat upper = new Mat();
            Mat lower = new Mat();
            Core.inRange(imageHSV, new Scalar(hueBegin, satMin, valMin), new Scalar(180, satMax, valMax), lower);
            Core.inRange(imageHSV, new Scalar(0, satMin, valMin), new Scalar(hueEnd, satMax, valMax), upper);
            Core.add(lower, upper, imageResult);
        }
        System.out.println(String.format("chnells:%d depth:%d", imageResult.channels(), imageResult.depth()));

        Image img = OpenCvUtils.mat2Image(imageResult);
        sp.setPrefSize(img.getWidth(),img.getHeight());
        imageView.setImage(img);
//        xxx();
        yyy();
        
        
    
    }
 
    private void xxx() {
        for (Circle c : circles) {
            sp.getChildren().remove(c);
        }
        double param1 = sldParam1.getValue();
        double param2 = sldParam2.getValue();
        Mat circles = new Mat();
        Point3D[] circlesList = null;
//        Imgproc.HoughCircles(imageResult, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, param1, param2, 16, 24);
        Imgproc.HoughCircles(imageResult, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 200, 8, 12, 60);
        System.out.println("circles.cols()=" + circles.cols());
        if (circles.cols() > 0) {
            int maxId = -1; double maxR = 0.;
            for (int i = 0; i < circles.cols(); i++) {
                double[] vCircle = circles.get(0, i);
                if (vCircle[2] > maxR) {
                    maxId = i;
                    maxR = vCircle[2];
                } 
            }
            System.out.println("maxId=" + maxId + " maxR=" + maxR);
            double[] vCircle = circles.get(0, maxId);
            Circle c = new Circle(vCircle[0], vCircle[1], vCircle[2]);
            c.setStroke(Color.RED);
            c.setFill(null);
            
            sp.getChildren().add(c);
        }
    }
    
    private void yyy() {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        for (Circle c : circles) {
            sp.getChildren().remove(c);
        }
        Imgproc.findContours(imageResult, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );
        double maxArea = 0;
        float[] radius = new float[1];
        Point center = new Point();
        int maxId = -1;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint c = contours.get(i);
            if (Imgproc.contourArea(c) > maxArea) {
                maxId = i;
                maxArea = Imgproc.contourArea(c);
                MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
                Imgproc.minEnclosingCircle(c2f, center, radius);
            }
        }
        Circle c = new Circle(center.x, center.y, radius[0]);
        c.setStroke(Color.RED);
        c.setFill(null);
        circles.add(c);
        sp.getChildren().add(c);
    }
}
