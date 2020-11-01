//pocket: 30,0,0,178,143,162,0
//f2jd75
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poolrobot;

import model.Ball;
import model.Cue;
import com.sun.jna.Pointer;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_SPACE;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.Plan;
import model.Pocket;
import model.Screen;
import org.opencv.core.Core;
import static org.opencv.core.Core.minMaxLoc;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.THRESH_TOZERO;
import static org.opencv.imgproc.Imgproc.THRESH_TOZERO_INV;
import static poolrobot.OpenCvUtils.bufferedImage2Mat;
import user32.JnaUtil;
import user32.JnaUtilException;

/**
 *
 * @author otsuka
 */
public class Tool extends Robot {

    private static Tool INSTANCE;
    private static final double MaxAngleOfIncidence = Math.PI / 4;  // 許容最大入射角
    //private static final double MinAngleOfIncidence = Math.PI / 16;  // 許容最小入射角
    private static final double offsetTorelance = 0.5;
    private static final double INITIALMOVE = 3;
    private static final double InitialMouseMovementInPixel = 100;
    private static final double offsetCorrectionFactor = 1.0;
    private Pointer hWndPool;
    public int cpx, cpy;

    Mat imageHSV = new Mat();
    Mat imageResult = new Mat();

    private Tool() throws AWTException {
        setAutoWaitForIdle(true);
//    @Override
//    public synchronized void mouseMove(int x, int y) { 
//        super.mouseMove(x, y);
//        
//    }
    }

    public static Tool getInstance() throws AWTException {
        if (INSTANCE == null) {
            INSTANCE = new Tool();
        }
        return INSTANCE;
    }

    public Rectangle findPoolWindow() {
        // find window titled "9-Ball Pool - Google Chrome".
        Rectangle poolWindow = null;
        try {
            hWndPool = JnaUtil.getWinHwnd("Bank Pool");
            if (hWndPool != null) {
                poolWindow = JnaUtil.getWindowRect(hWndPool);
            }
        } catch (JnaUtilException ex) {
            ex.printStackTrace();
        }
        return poolWindow;
    }

//    public void focusPoolWindow() {
//        clickScreen(Screen.rectPoolWindow.x + 100, Screen.rectPoolWindow.y + 200);
//    }
//
    public void focusPoolWindow() {
        System.out.println("setForegroundWindow>");
        if (hWndPool == null) {
            return;
        }
//        if (!JnaUtil.isForegroundWindow(hWndPool)) {
            if (JnaUtil.setForegroundWindow(hWndPool)) {
                System.out.println("setForegroundWindow -> OK");
            } else {
                System.out.println("setForegroundWindow -> NG");
            };
//        }

    }

    public void clickScreen(double mx, double my) {
        focusPoolWindow();
        mouseMove((int) mx, (int) my);
        delay(100);
        mousePress(InputEvent.BUTTON1_DOWN_MASK);
        delay(100);
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        delay(500);
    }
    
    public void clickButton(Point2D button) {
        clickScreen(button.getX(), button.getY());
    }

    private void dragMouseRel(int mx, int my, int pixelPerMsec) {
        double fmx, fmy;
//        System.out.println("dragMouseRel>>>");
        if (mx == 0 && my == 0) {
            return;
        }
        PointerInfo pI = MouseInfo.getPointerInfo();
        java.awt.Point point = pI.getLocation();
        double travelDistance = Math.sqrt(my * my + mx * mx) + INITIALMOVE;
        double travelDir = Math.atan2(my, mx);
        fmx = travelDistance * Math.cos(travelDir);
        fmy = travelDistance * Math.sin(travelDir);
        int timeToTravel_msec = (int) travelDistance / pixelPerMsec;
        double stepX = (int) Math.round(fmx / timeToTravel_msec);
        double stepY = (int) (fmy / timeToTravel_msec);
        System.err.println(String.format("timeToTravel_msec=%d stepX=%f stepY=%f", timeToTravel_msec, stepX, stepY));

        for (int i = 1; i <= timeToTravel_msec; i++) {
            mouseMove((int) Math.round(point.x + stepX * i), (int) Math.round(point.y + stepY * i));
            delay(5);
        }
//        System.out.println("dragMouseRel<<<");

    }

    public void keyPress(int delay, int... keys) {
        for (int key : keys) {
            keyPress(key);
        }
        for (int i = keys.length - 1; i >= 0; i--) {
            keyRelease(keys[i]);
        }
        delay(delay);
    }

    /**
     * https://stackoverrun.com/ja/q/10232161 "Detecting Hough circles JAVA
     * OpenCV"
     *
     * @param imgGray
     * @param rayon
     * @return
     */
    private void colorFilter(Mat matScreen, double hueBegin, double hueEnd, double satMin, double valMin, double satMax, double valMax) {
        Imgproc.cvtColor(matScreen, imageHSV, Imgproc.COLOR_BGR2HSV);
        if (hueBegin <= hueEnd) {
            Core.inRange(imageHSV, new Scalar(hueBegin, satMin, valMin), new Scalar(hueEnd, satMax, valMax), imageResult);
        } else {
            Mat upper = new Mat();
            Mat lower = new Mat();
            Core.inRange(imageHSV, new Scalar(hueBegin, satMin, valMin), new Scalar(180, satMax, valMax), lower);
            Core.inRange(imageHSV, new Scalar(0, satMin, valMin), new Scalar(hueEnd, satMax, valMax), upper);
            Core.add(lower, upper, imageResult);
        }
    }

    public Ball recognizeBallInTopView(Ball ball, Mat matScreen, Mat result) {
        System.out.println("recognizeBallInTopView>" + ball.ballNo);
        Screen.switchToTopView();

        // Extracvt ball contour;
        double hueBegin = Double.valueOf(ball.prop.getProperty("top.hueBegin"));
        double hueEnd = Double.valueOf(ball.prop.getProperty("top.hueEnd"));
        double satMin = Double.valueOf(ball.prop.getProperty("top.satMin"));
        double satMax = Double.valueOf(ball.prop.getProperty("top.satMax"));
        double valMin = Double.valueOf(ball.prop.getProperty("top.valMin"));
        double valMax = Double.valueOf(ball.prop.getProperty("top.valMax"));

        colorFilter(matScreen, hueBegin, hueEnd, satMin, valMin, satMax, valMax);

        // detect circle
        recognizeBallByFindContour(ball);

/*
        Mat circles = new Mat();
        Imgproc.HoughCircles(imageResult, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 200, 8, 8, 12);
        for (int i = 0; i < circles.cols(); i++) {
            double[] vCircle = circles.get(0, i);
            Point3D pt = new Point3D(vCircle[0], vCircle[1], vCircle[2]);
        }
        System.out.println("recognizeBallInTopView>plot");
        if (circles.cols() > 0) {
            double[] vCircle = circles.get(0, 0);
            ball.sx = vCircle[0];
            ball.sy = vCircle[1];
            ball.sr = Ball.BallSize;
            ball.showInTopView();
        } else {
            ball.sx = 0;
            ball.sy = 0;
            ball.sr = Ball.BallSize;
            ball.hide();
        }
*/
        // erase the ball from mat;
        Imgproc.circle​(matScreen, new Point(ball.sx, ball.sy), (int)ball.sr+2, new Scalar(0, 0, 0), -1);
        if (ball.sx > 0) {
            ball.showInTopView();
        } else {
            ball.hide();
        }
        return ball;
    }

    public Ball recognizeBallInParseView(Ball ball, Mat result) {

        Screen.switchToPerseView();

        // Extracvt ball contour;
        double hueBegin = Double.valueOf(ball.prop.getProperty("parse.hueBegin"));
        double hueEnd = Double.valueOf(ball.prop.getProperty("parse.hueEnd"));
        double satMin = Double.valueOf(ball.prop.getProperty("parse.satMin"));
        double satMax = Double.valueOf(ball.prop.getProperty("parse.satMax"));
        double valMin = Double.valueOf(ball.prop.getProperty("parse.valMin"));
        double valMax = Double.valueOf(ball.prop.getProperty("parse.valMax"));

        Mat matScreen = Screen.getPoolMat();
        colorFilter(matScreen, hueBegin, hueEnd, satMin, valMin, satMax, valMax);
        // for debug
        //Imgproc.cvtColor(imageResult, result, Imgproc.COLOR_HSV2BGR);
        imageResult.copyTo(result);

//        detectBallByHoughcircles(ball);
        recognizeBallByFindContour(ball);
        Imgproc.circle​(matScreen, new Point(ball.sx, ball.sy), (int)ball.sr, new Scalar(0, 0, 0));
        if (ball.sx > 0) {
            ball.showInTopView();
        } else {
            ball.hide();
        }
        return ball;
    }

    private void recognizeBallByHoughcircles(Ball ball) {
        // detect circle
        Mat circles = new Mat();
        Imgproc.HoughCircles(imageResult, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 200, 8, 12, 60);
        if (circles.cols() > 0) {
            double[] vCircle = circles.get(0, 0);
            ball.sx = vCircle[0];
            ball.sy = vCircle[1];
            ball.sr = vCircle[2];
            ball.showInPerseView();
        } else {
            ball.sx = 0;
            ball.sy = 0;
        }
    }

    private void recognizeBallByFindContour(Ball ball) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(imageResult, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
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
        if (maxId >= 0) {
            ball.sx = center.x;
            ball.sy = center.y;
            ball.sr = radius[0];
//            ball.showInPerseView();
        } else {
            ball.sx = 0;
            ball.sy = 0;
            ball.sr = 0;
            ball.hide();
        }
    }

    /**
     * findCue cue: 14,0,89,166,213,255,0
     */
    public Cue detectCue(Mat screenMat, Mat resultMat, Cue cue) {
        // HSV 指定してキューを抽出
        Mat matHsv = new Mat();
        Imgproc.cvtColor(screenMat, matHsv, Imgproc.COLOR_BGR2HSV); // 画像をHSVに変換
        Mat maskHsv = new Mat();
        Core.inRange(matHsv, new Scalar(14, 84, 213), new Scalar(100, 121, 255), maskHsv);
//        toolViewer.showImage(matHsv);
        //
        //
        // HoughLines(Mat image, Mat lines, double rho, double theta, int threshold)
        Mat lines = new Mat();
        Imgproc.HoughLines(maskHsv, lines, 1, Math.PI / 180, 150);
        Imgproc.cvtColor(maskHsv, maskHsv, Imgproc.COLOR_GRAY2BGR);
        System.out.println("lines=" + lines.rows());
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 500 * (-b)), Math.round(y0 + 500 * (a)));
            Point pt2 = new Point(Math.round(x0 - 500 * (-b)), Math.round(y0 - 500 * (a)));
            System.out.println(String.format("line(%5.0f, %5.0f)-(%5.0f, %5.0f)", pt1.x, pt1.y, pt2.x, pt2.y));
//            Imgproc.line(resultMat, pt1, pt2, new Scalar(255, 0, 0), 1, Imgproc.LINE_AA, 0);
            cue.setPosition(pt1.x, pt1.x, Math.atan2(pt2.y - pt2.x, pt2.x - pt1.x));
        }
        return cue;
    }

    public Point2D calcAimPoint(Ball cueBall, Ball target, Pocket pocket) {
        double dirToFly = Math.atan2(pocket.sy - target.sy, pocket.sx - target.sx);
        double tx = target.sx - 2 * Ball.BallSize * Math.cos(dirToFly);
        double ty = target.sy - 2 * Ball.BallSize * Math.sin(dirToFly);
        return new Point2D(tx, ty);
    }

    public void callBallAndPocket(Plan thePlan) {
        // click 'Call'
        clickButton(Screen.btnCall);

        // select target pocket
        Pocket imaginaryPocket = thePlan.pocket;
        Pocket realPocket = Pocket.realPockets[(int)imaginaryPocket.selectx];
        clickScreen(realPocket.selectx, realPocket.noOfBanks);
        
        // set number of cushion.
        for (int ii = 1; ii < imaginaryPocket.noOfBanks; ii++) {
            keyPress(100, KeyEvent.VK_PAGE_UP);
        }

        // click 'Call' again
        clickButton(Screen.btnCall);

        // select target ball
        clickScreen(thePlan.ball.sx, thePlan.ball.sy);

    }

    public void aim(Plan thePlan, Point cueEndPoint) {
        thePlan.aimPoint = calcAimPoint(Ball.cueBall, thePlan.ball, thePlan.pocket);
        Point2D p = thePlan.aimPoint;
        double dir = Math.atan2(p.getY() - Ball.cueBall.sy, p.getX() - Ball.cueBall.sx);
        cpx = (int) (Ball.cueBall.sx - 200 * Math.cos(dir));
        cpy = (int) (Ball.cueBall.sy - 200 * Math.sin(dir));
        mousePress(InputEvent.BUTTON1_DOWN_MASK);
        mouseMove(cpx, cpy);
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        cueEndPoint.x = cpx;
        cueEndPoint.y = cpy;

        System.out.println(String.format("aim> at (%d, %d) dir=%f",
                (int) (p.getX()), (int) (p.getY()), dir / Math.PI * 180));

    }

    private double currentDeviation(Ball target) {
        if ((target.sx - Ball.cueBall.sx) > target.sr) {
            return Math.PI / 2;
        } else if ((target.sx - Ball.cueBall.sx) < -target.sr) {
            return -Math.PI / 2;
        }
        return Math.asin((target.sx - Ball.cueBall.sx) / target.sr / 2);
    }

    /*
    *  
     */
    public void aimInParseView(Ball target, double targetOffset, double spinPower, TextArea console) {
        double targetOffsetInPixel, currentOffsetInPixel, prevOffsetInPixel, offsetErrorInPixel, controlValue, prevControlGain;
        Ball cueBall = Ball.balls[0];

        focusPoolWindow();
        Screen.switchToPerseView();

        clickButton(Screen.btnFineAim);
        recognizeBallInParseView(target, imageResult);
        recognizeBallInParseView(cueBall, imageResult);
        currentOffsetInPixel = -(target.sx - cueBall.sx);
        targetOffsetInPixel = targetOffset * target.sr;

        offsetErrorInPixel = targetOffsetInPixel - currentOffsetInPixel;  // targetDev error
        controlValue = InitialMouseMovementInPixel; //offsetError / (target.sy - cueBall.sy);
        prevControlGain = 0;

        String log = String.format("DEV.target.sr=%f target=%f current=%f error=%f controlGain=%f --> controlValue=%f",
                target.sr, targetOffsetInPixel, currentOffsetInPixel, offsetErrorInPixel, prevControlGain, controlValue);
        System.out.println(log);
        console.setText(console.getText() + log);

        int loopCount;
        for (loopCount = 0; loopCount < 10; loopCount++) {
            System.out.println("------------------------------------------------");
            if (Math.abs(offsetErrorInPixel) <= offsetTorelance) {
                System.out.println("Got it !");
                break;
            }
            controlValue = Math.min(Math.abs(controlValue), 300) * Math.signum(controlValue);
            mouseMove(600, 500);
            mousePress(InputEvent.BUTTON1_DOWN_MASK);
            dragMouseRel((int) Math.round(controlValue), 0, 3);
            mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            delay(500);

            prevOffsetInPixel = currentOffsetInPixel;
            recognizeBallInParseView(target, imageResult);
            recognizeBallInParseView(cueBall, imageResult);
            currentOffsetInPixel = -(target.sx - cueBall.sx);
            if (currentOffsetInPixel == prevOffsetInPixel) {
                System.out.println("... screen not moved.");
                continue;
            }
            prevControlGain = (currentOffsetInPixel - prevOffsetInPixel) / controlValue;
            offsetErrorInPixel = targetOffsetInPixel - currentOffsetInPixel;  // targetDev error
            controlValue = offsetErrorInPixel / prevControlGain;
            log = String.format("target=(%.1f, %.1f) cueBall=(%.1f, %.1f) error=%.1f",
                    target.sx, target.sy, cueBall.sx, cueBall.sy, offsetErrorInPixel);
            System.out.println(log);
            console.setText(console.getText() + log);
            log = String.format("Dev.sr=%f target=%f current=%f error=%f controlGain=%f --> controlValue=%f",
                    target.sr, targetOffsetInPixel, currentOffsetInPixel, offsetErrorInPixel, prevControlGain, controlValue);
            System.out.println(log);
            console.setText(console.getText() + log);
            console.setPrefRowCount(100);

        }
        if (spinPower != 0) {
            setSpin(spinPower);
        }

        // click 'Shoot'
//        mousePress(InputEvent.BUTTON1_DOWN_MASK);
//        mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    
    /**
     * @parameter spinPower   -10 ~  10
     */
    public void setSpin(double spinPower) {
        focusPoolWindow();

        clickButton(Screen.btnSpin);
        clickButton(Screen.btnSpin);
        
        mouseMove(600, 500);
        mousePress(InputEvent.BUTTON1_DOWN_MASK);
        dragMouseRel(0, (int)spinPower * -10, 8);
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void shoot(double power) {
        focusPoolWindow();
        // click 'Shoot'

        clickButton(Screen.btnShoot);
        clickButton(Screen.btnShoot);

        mouseMove(600, 500);
        mousePress(InputEvent.BUTTON1_DOWN_MASK);
        dragMouseRel(0, +200, 1);
        dragMouseRel(0, -600, (int) power);
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private Point2D calcMirrorPointOf(double x, double y, int bankSide) {
        double mx = x, my = y;
        if ((bankSide & 1) == 1) {
            my = Pocket.TOP * 2 - y;
        } else if ((bankSide & 2) == 2) {
            my = Pocket.BOTTOM * 2 - y;
        }
        if ((bankSide & 4) == 4) {
            mx = Pocket.LEFT * 2 - x;
        } else if ((bankSide & 8) == 8) {
            mx = Pocket.RIGHT * 2 - x;
        }
        return new Point2D(mx, my);
    }

    private boolean xxx(Point2D from, Point2D to, Point2D center, double rr) {
        // vec2Target : 手玉から狙い点に向かうベクトル
        Point2D vec2AimPoint = new Point2D(to.getX() - from.getX(), to.getY() - from.getY());
        // 手玉から他の球に向かうベクトル
        Point2D vec2Ball = new Point2D(center.getX() - from.getX(), center.getY() - from.getY());
        double t = vec2AimPoint.dotProduct(vec2Ball) / vec2AimPoint.dotProduct(vec2AimPoint);
        // t が負ならばその球は狙い方向とは反対側にあるか的玉より向うにある
        if (0. < t && t < 1.) {
            // 垂線のベクトルを求めて．．．
            Point2D leg = vec2Ball.subtract(vec2AimPoint.multiply(t));
            // その長さが球の半径の２倍未満ならば衝突する
            if (leg.dotProduct(leg) < 4 * Ball.BallSize * Ball.BallSize) {
                return true;
            }
        }
        // 狙い点のすぐ先に的玉以外の球がないか
        double ll = to.distance(center);
//            System.out.println(String.format("aimPoint:(%f, %f) ballNo[%d]:(%f, %f) ll=%f", 
//                    aimPoint.getX(), aimPoint.getY(), ballNo, ball.sx, ball.sy, ll));
        if (ll < 2 * Ball.BallSize - 1) {
            return true;
        }
        return false;
    }

    /**
     * 手玉と的玉の間の障害球を見つける
     *
     * @param targetBallNo
     * @return ballNo 障害となる球の番号
     */
    public int detectObstacleBallOnTheWayToTargetPoint(Point2D aimPoint, int targetBallNo) {
        for (int ballNo = 1; ballNo < Ball.NO_OF_BALLS; ballNo++) {
// 2020.09.30 手玉から見て的玉の向こう側の面が狙い点となるケースがあるので、的玉との干渉もチェックしないといけない
//            if (ballNo == targetBallNo) continue;
            Ball ball = Ball.balls[ballNo];
            if (ball.sx == 0 || ball.sy == 0) {
                continue;
            }
            if (xxx(new Point2D(Ball.cueBall.sx, Ball.cueBall.sy), aimPoint, new Point2D(ball.sx, ball.sy), ball.sr)) {
                return ballNo;
            }
        }
        return 0;
    }

    /**
     * 的玉とポケットの間の障害球を見つける（バンク指定あり）
     *
     * @param targetBallNo 的玉 pocketNo ポケット bankNo 1=TOP / 2=BOTTOM / 4=LEFT
     * /8=RIGHT
     * @return ballNo 障害となる球の番号
     */
    public int detectObstacleBallOnTheWayToPocket(Ball target, Pocket pocket, int bankNo) {
        int result = 0;
//        Pocket pocket = Pocket.imaginaryPockets[pocketNo];   // 仮想ポケット
//        Ball target = Ball.balls[targetBallNo];     // 的玉
        // vec2Target : 的玉からポケットに向かうベクトル
        Point2D vec2Pocket = new Point2D(pocket.sx - target.sx, pocket.sy - target.sy);

        for (int ballNo = 1; ballNo < Ball.NO_OF_BALLS; ballNo++) {
            // ボールの鏡像を計算
//            if (ballNo == targetBallNo) {
//                continue;
//            }
            Ball ball = Ball.balls[ballNo];
            if (ball == target) {
                continue;
            }
            if (ball.sx == 0 && ball.sy == 0) {
                continue;
            }
            Point2D mirror = calcMirrorPointOf(ball.sx, ball.sy, bankNo);
            Point2D vec2Ball = mirror.subtract(target.sx, target.sy);
            double t = vec2Pocket.dotProduct(vec2Ball) / vec2Pocket.dotProduct(vec2Pocket);
            // t が負ならばその球は狙い方向とは反対側にあるか的玉より向うにある
            if (t <= 0. || t > 1.) {
                continue;
            }
            // 垂線のベクトルを求めて．．．
            Point2D leg = vec2Ball.subtract(vec2Pocket.multiply(t));
            // その長さが球の半径の２倍未満ならば衝突する
            if (leg.dotProduct(leg) < 4 * Ball.BallSize * Ball.BallSize) {
                result = ballNo;
                break;
            }
        }
        return result;
    }

    /**
     * 的玉とポケットの間の障害球を見つける
     *
     * @param targetBallNo 的玉 pocketNo ポケット
     * @return ballNo 障害となる球の番号
     */
    public int detectObstacleBallOnTheWayToPocket(Ball targetBall, Pocket pocket) {
        int result = 0;

        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 0)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 1)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 2)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 3)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 4)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 8)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 5)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 9)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 6)) != 0) {
            return result;
        }
        if ((result = detectObstacleBallOnTheWayToPocket(targetBall, pocket, 10)) != 0) {
            return result;
        }
        return result;
    }

    private boolean isPocketPitfall(Ball targetBall, Pocket targetPocket, Pocket pitfall) {
        Point2D from = new Point2D(targetBall.sx, targetBall.sy);
        Point2D to = new Point2D(targetPocket.sx, targetPocket.sy);
        Point2D center = new Point2D(pitfall.sx, pitfall.sy);
        return xxx(from, to, center, Ball.BallSize);
    }

    public int detectPitfallOnTheWayToPocket(Ball targetBall, Pocket targetPocket) {
        int result = -1;
//        Ball targetBall = Ball.balls[targetBallNo];
//        Pocket targetPocket = Pocket.imaginaryPockets[targetPocketNo];
        for (int pocketNo : Arrays.asList(1, 2, 3, 4, 5, 6)) {
            Pocket pitfall = Pocket.realPockets[pocketNo];
            if (isPocketPitfall(targetBall, targetPocket, pitfall)) {
                return pocketNo;
            }
        }
        return -1;
    }

    public List<Plan> findExecutablePlans() {
        List<Plan> plans = new ArrayList<Plan>();
        double deviationMin = 999;
        int theBestBallNo = -1;
        int theBestPocketNo = -1;

        for (int ballNo = 1; ballNo < Ball.NO_OF_BALLS; ballNo++) {
            Ball ball = Ball.balls[ballNo];
            if (ball.sx == 0 || ball.sy == 0) {
                continue;
            }
            for (int pocketNo = 0; pocketNo < Pocket.NoOfImaginaryPockets; pocketNo++) {
                Pocket pocket = Pocket.imaginaryPockets[pocketNo];
                Plan thePlan = new Plan();
                thePlan.ball = ball;
                thePlan.pocket = pocket;
                
                if (isExecutablePlan(thePlan)) {
                    evaluatePlan(thePlan);
                    plans.add(thePlan);
                }
            }
        }
        plans.sort(new PlanComparator());
        return plans;
    }

    public boolean isExecutablePlan(Plan thePlan) {
        Pocket pocket = thePlan.pocket;
        Ball ball = thePlan.ball;
        double deviationMin;
        
        // check if the pocket is not imaginary one.
        if (pocket.noOfBanks == 0) {
            thePlan.available = false;
            thePlan.cause = "no bank shot.";
            return false;
        }
        if (!pocket.active) {
            thePlan.available = false;
            thePlan.cause = "not active.";
            return false;
        }
        double pocketDir = Math.atan2(pocket.sy - ball.sy, pocket.sx - ball.sx);
        // 狙い点と方向角を計算
        Point2D aimPoint = calcAimPoint(Ball.cueBall, ball, pocket);
        double aimDir = Math.atan2(aimPoint.getY() - Ball.cueBall.sy, aimPoint.getX() - Ball.cueBall.sx);
        // ショットの偏向角
        double deviation = pocketDir - aimDir;
        while (deviation > Math.PI) {
            deviation = deviation - 2 * Math.PI;
        }
        while (deviation <= -Math.PI) {
            deviation = deviation + 2 * Math.PI;
        }
        if (Math.abs(deviation) >= Math.PI / 2) {
            thePlan.available = false;
            thePlan.cause = "OppositeSide";
            return false;
        }
        
        double offset = -  2 * Math.sin(deviation) * offsetCorrectionFactor;
        // ポケットを正面から狙えるか
        double incidenceAngle = pocketDir - pocket.faceAngle;
        while (incidenceAngle > Math.PI) {
            incidenceAngle -= Math.PI * 2;
        }
        while (incidenceAngle <= -Math.PI) {
            incidenceAngle += Math.PI * 2;
        }
        // ポケットに対する入射角チェック。真正面はダメ。斜めもダメ。
        if (Math.abs(incidenceAngle) > MaxAngleOfIncidence) {
            thePlan.available = false;
            thePlan.cause = "IncidentAngle";
            return false;
        }
        // 狙い点までの間に障害となる球はあるか？
        int obstacleBallNo = detectObstacleBallOnTheWayToTargetPoint(aimPoint, ball.ballNo);
        if (obstacleBallNo > 0) {
            thePlan.available = false;
            thePlan.cause = "ObstacleBall#1";
            return false;
        }
        // 的玉からポケットまでの間に障害となる球はあるか？
        obstacleBallNo = Tool.this.detectObstacleBallOnTheWayToPocket(ball, pocket);
        if (obstacleBallNo > 0 && obstacleBallNo != ball.ballNo) {
            thePlan.available = false;
            thePlan.cause = "ObstacleBall#2";
            return false;
        }
        int pitfallNo = detectPitfallOnTheWayToPocket(ball, pocket);
        if (pitfallNo > 0) {
            thePlan.available = false;
            thePlan.cause = "Pitfall";
            return false;
        }
        
        
        
        System.out.println(String.format("ball=%d pocket=%d dev.=%.2f", ball.ballNo, pocket.pocketNo, deviation));
        deviationMin = Math.abs(deviation);
        thePlan.aimPoint = aimPoint;
        thePlan.deviation = deviation;
        thePlan.offset = offset;
        thePlan.incident = incidenceAngle;
        Point2D d = new Point2D(pocket.sx - ball.sx, pocket.sy - ball.sy);
        thePlan.distance = d.distance(Point2D.ZERO);
        return true;
    }
    
    public void evaluatePlan(Plan thePlan) {
        thePlan.score = 1000 * Math.sqrt(1-Math.abs(thePlan.offset)/2) / thePlan.distance;
    }

    private class PlanComparator implements Comparator<Plan> {

        @Override
        public int compare(Plan p1, Plan p2) {
            return p1.score > p2.score ? -1 : 1;
        }
    }

}
