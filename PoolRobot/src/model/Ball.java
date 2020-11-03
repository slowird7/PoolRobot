/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.opencv.core.Mat;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 *
 * @author otsuka
 */
public class Ball {
    public int ballNo;
    public double sx, sy, sr;
    public Mat imageMat;
    public Circle contour;
    public Line horz;
    public Line vert;
    public Label a;
    
    public static Ball[] balls;
    public static Ball cueBall;
    public static final int NO_OF_BALLS = 11;
    public static int noOfBalls = 6;
    
    public static final double BallSize = 11;
    
    public Properties prop;
    
    public static void initBalls(String game) {
        balls = new Ball[noOfBalls];
        Ball newBall;
        for (int ballNo=0; ballNo < noOfBalls; ballNo++) {
            newBall = new Ball(game, ballNo, 0., 0., 0., Integer.toString(ballNo), String.format("image/b%02d.png",ballNo));
            balls[ballNo] = newBall;
        }
        cueBall = balls[0];
//        System.out.println("mask   :channels=" + ballMask.channels());
//        System.out.println("cueBall:channels=" + cueBall.imageMat.channels());
    }
    
    public Ball(String game, int ballNo, double x, double y, double r, String label, String imageFile) {
        this.ballNo = ballNo;
        sx = x;
        sy = y;
        sr = r;
//        imageMat = imread(imageFile, 1);
        a = new Label(label);
        a.setTextFill(Color.BLACK);
        contour = new Circle(sx, sy, sr);
        contour.setLayoutX(0);
        contour.setLayoutY(0);
        contour.setStroke( Color.BLUE );
        contour.setStrokeWidth( 1 );
        contour.setFill(null);
        horz = new Line(-25, 0, 25, 0);
        horz.setLayoutX(0);
        horz.setLayoutY(0);
        horz.setStroke( Color.BLUE );
        horz.setStrokeWidth( 1 );
        vert = new Line(0, -400, 0, 400);
        vert.setLayoutX(0);
        vert.setLayoutY(0);
        vert.setStroke( Color.BLUE );
        vert.setStrokeWidth( 1 );
        prop = new Properties();
        try {
            prop.load(new BufferedReader(new FileReader(String.format("parameter/ball/%s/%d.txt", game, ballNo))));
        } catch (FileNotFoundException ex) {
            System.err.println("parameter file not found.");
        } catch (IOException ex) {
            System.err.println("Failed to read parameter file.");
        }

        //System.out.println(String.format("image:type=%08X depth=%d channels=%d width=%d height=%d", imageMat.type(), imageMat.depth(), imageMat.channels(), imageMat.width(), imageMat.height()));
    }
    
    public void hide() {
        contour.setVisible(false);
        horz.setVisible(false);
        vert.setVisible(false);
        a.setVisible(false);
    }
    
    public void showInTopView() {
        contour.setTranslateX(sx);
        contour.setTranslateY(sy);
        contour.setRadius(sr);
        contour.setVisible(true);
        horz.setStartX(-25);
        horz.setEndX(25);
        horz.setTranslateX(sx);
        horz.setTranslateY(sy);
        horz.setVisible(true);
        vert.setStartY(-25);
        vert.setEndY(25);
        vert.setTranslateX(sx);
        vert.setTranslateY(sy);
        vert.setVisible(true);
        a.setTranslateX(sx);
        a.setTranslateY(sy);
        a.setVisible(true);
    }
    
    public void showInPerseView() {
        contour.setTranslateX(sx);
        contour.setTranslateY(sy);
        contour.setRadius(sr);
        contour.setVisible(true);
        horz.setStartX(-sr);
        horz.setEndX(sr);
        horz.setTranslateX(sx);
        horz.setTranslateY(sy);
        horz.setVisible(true);
        vert.setStartY(0);
        vert.setEndY(800);
        vert.setTranslateX(sx);
        vert.setTranslateY(sy);
        vert.setVisible(true);
        a.setTranslateX(sx);
        a.setTranslateY(sy);
        a.setVisible(true);
    }

}
