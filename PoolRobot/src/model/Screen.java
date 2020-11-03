/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import static java.lang.Thread.sleep;
import javafx.geometry.Point2D;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import org.opencv.imgproc.Imgproc;
import static poolrobot.OpenCvUtils.bufferedImage2Mat;
import poolrobot.Tool;

/**
 *
 * @author otsuka
 */
public class Screen {
    public static Point2D btnShoot = new Point2D(1148, 400);
    public static Point2D btnAim = new Point2D(996, 400);
    public static Point2D btnFineAim = new Point2D(996, 447);
    public static Point2D btnCall = new Point2D(1148, 494);
    public static Point2D btnSpin = new Point2D(996, 494);
    public static Point2D btnSwitchView = new Point2D(1250, 600);
    public static Point2D btnUndo = new Point2D(1100, 800);
    public static Point2D btnNewFrame = new Point2D(488, 325);
    public static Point2D btnFrameOver = new Point2D(488, 420);
    
    public static Rectangle rectPoolWindow;
    
    
    public static Mat matBtnTopView;
    public static Mat matBtnParseView;
    public static Mat matNewFrame;
    public static Mat matOpenBreakShot;
    public static Mat matFrameOver;
    private static Rectangle bounds;
    private static Tool tool;
    
    static {
        matBtnTopView = imread("image/btnTopView.png");
        matBtnParseView = imread("image/btnParseView.png");
        matNewFrame = imread("image/NewFrame.png");
        matOpenBreakShot = imread("image/OpenBreakShot.png");
        matFrameOver = imread("image/FrameOver.png");
        bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    public static Mat getScreenMat() {
            BufferedImage imageScreen = Tool.getInstance().createScreenCapture(bounds);
            return bufferedImage2Mat(imageScreen);
    }
    
    public static Mat getPoolMat() {
        Mat poolMat = new Mat(getScreenMat(), new Rect(0, 0, 970, 650));
        Imgproc.rectangle(poolMat, new Point(0,0), new Point(poolMat.width(), 94), new Scalar(0, 0, 0), -1);
        Imgproc.rectangle(poolMat, new Point(0,598), new Point(poolMat.width(), poolMat.height()), new Scalar(0, 0, 0), -1);
        return poolMat;
    }
    
    public static void waitStill() {
        System.out.println("waitStill>");
        Mat matPrev, matDiff = new Mat();
        Mat matNow = getScreenMat();
        try { sleep(100); } catch (InterruptedException ex) {};
        int waitAndSeeCount = 0; 
        while (true) {
            matPrev = matNow;
            matNow = getScreenMat();
            Core.absdiff(matNow, matPrev, matDiff);
            if (Core.countNonZero(matDiff.reshape(1)) == 0) {
                waitAndSeeCount++;
                if (waitAndSeeCount > 5) {
                    break;
                }
            }
            try { sleep(100); } catch (InterruptedException ex) {};
        }
        System.out.println("waitStill<");
    }
    
    public static boolean isNewFrame() {
        boolean ret;

        Mat outputImage=new Mat();    
        int machMethod=Imgproc.TM_CCOEFF;
        //Template matching method
        Imgproc.matchTemplate(getScreenMat(), matNewFrame, outputImage, machMethod);
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        System.out.println("isNewFrame>mmr.maxVal=" + mmr.maxVal);
        return (mmr.maxVal > 1.6E7);
    }
    
    public static boolean isOpenBreakShot() {
        boolean ret;

        Mat outputImage=new Mat();    
        int machMethod=Imgproc.TM_CCOEFF;
        //Template matching method
        Imgproc.matchTemplate(getScreenMat(), matOpenBreakShot, outputImage, machMethod);
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        System.out.println("isOpenBreakShot>mmr.maxVal=" + mmr.maxVal);
        return (mmr.maxVal > 3.0E7);
    }
    
    public static boolean isFrameOver() {
        boolean ret;

        Mat outputImage=new Mat();    
        int machMethod=Imgproc.TM_CCOEFF;
        //Template matching method
        Imgproc.matchTemplate(getScreenMat(), matFrameOver, outputImage, machMethod);
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        System.out.println("isFrameOver>mmr.maxVal=" + mmr.maxVal);
        return (mmr.maxVal > 2.5E7);
    }
    
    public static void undo() {
        Tool.getInstance().clickButton(btnUndo);
    }
    
    public static boolean isTopView() {
        boolean ret;

        Mat outputImage=new Mat();    
        int machMethod=Imgproc.TM_CCOEFF;
        //Template matching method
        Imgproc.matchTemplate(getScreenMat(), matBtnTopView, outputImage, machMethod);
 
    
        MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        System.out.println("mmr.maxVal=" + mmr.maxVal);
        return (mmr.maxVal > 3.0E7);
    }
    
    public static void switchToPerseView() {
        if (isTopView()) {
            Tool.getInstance().clickButton(btnSwitchView);
        }
    }
    
    public static void switchToTopView() {
        if (!isTopView()) {
            Tool.getInstance().clickButton(btnSwitchView);
        }
    }
    
}
