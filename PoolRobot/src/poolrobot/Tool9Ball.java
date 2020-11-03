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
public class Tool9Ball extends Tool {

    protected Tool9Ball() throws AWTException {
        super();
        INSTANCE = this;
    }

    private Ball checkNextBall() {
        Ball nextTarget = null;
        for (int ballNo = 1; ballNo < Ball.noOfBalls; ballNo++) {
            Ball ball = Ball.balls[ballNo];
            if (ball.sx != 0 && ball.sy != 0) {
                nextTarget = ball;
                break;
            }
        }
        return nextTarget;
    }
    
    @Override
    public List<Plan> findExecutablePlans() {
        List<Plan> plans = new ArrayList<Plan>();
        double deviationMin = 999;
        int theBestBallNo = -1;
        int theBestPocketNo = -1;

        Ball ball = checkNextBall();
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
        plans.sort(new PlanComparator());
        return plans;
    }

    @Override
    public boolean isExecutablePlan(Plan thePlan) {
        Pocket pocket = thePlan.pocket;
        Ball ball = thePlan.ball;
        double deviationMin;
        
        // check if the pocket is not imaginary one.
//        if (pocket.noOfBanks == 0) {
//            thePlan.available = false;
//            thePlan.cause = "no bank shot.";
//            return false;
//        }
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
        obstacleBallNo = Tool9Ball.this.detectObstacleBallOnTheWayToPocket(ball, pocket);
        if (obstacleBallNo > 0 && obstacleBallNo != ball.ballNo) {
            thePlan.available = false;
            thePlan.cause = "ObstacleBall#2";
            return false;
        }
//        int pitfallNo = detectPitfallOnTheWayToPocket(ball, pocket);
//        if (pitfallNo > 0) {
//            thePlan.available = false;
//            thePlan.cause = "Pitfall";
//            return false;
//        }
        
        
        
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
    
}
