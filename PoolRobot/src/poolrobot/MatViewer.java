/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolrobot;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.core.Mat;
import static poolrobot.OpenCvUtils.mat2Image;

/**
 * FXML Controller class
 *
 * @author otsuka
 */
public class MatViewer implements Initializable {

    @FXML
    private AnchorPane ap;
    @FXML
    public Rectangle aimPoint;
    @FXML
    public Rectangle cueEndPoint;
    @FXML
    public Line pathToAimPoint;
    @FXML
    public Line pathToCushion;
    @FXML
    public Line pathToPocket;
//    @FXML
//    private ImageView imageView;
//    @FXML
//    private ScrollPane scroller;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void add(Node child) {
        ap.getChildren().add(child);        
    }
    
    public void remove(Node child) {
        ap.getChildren().remove(child);        
    }
    
    public void showImage(Mat mat) {
        Image image = mat2Image(mat);
//        m.view.setWidth()
//        imageView.setImage(image);
    }
}
