/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 *
 * @author otsuka
 */
public class Pocket {
    public int pocketNo;
    public boolean active;  // 有効
    public double sx, sy;   // ポケットの狙い点座標
    public double selectx;  // 仮想ポケットの場合、対応する実ポケット番号 ／実ポケットの場合、クリック点
    public double noOfBanks;  // バンク回数。／実ポケットの場合、クリック点
    public double faceAngle;// ポケットに正対する方位角 
    public Rectangle rect;  // ポケットの狙い点を表示するマーカー
    public Label a;         // ポケット番号

    public static final int NoOfImaginaryPockets = 34;
    public static Pocket[] realPockets;
//    public static Point[] selectPoint;
    public static Pocket[] imaginaryPockets;
    public static final double TOP = 126;
    public static final double BOTTOM = 526;
    public static final double LEFT = 80;
    public static final double RIGHT = 894;
    public static final double CENTER = (LEFT + RIGHT) / 2;
    public static final double YCENTER = (TOP + BOTTOM) / 2;
    private static final double spanX = (RIGHT - LEFT) / 2; // ポケットの間隔(X方向)
    private static final double spanY = BOTTOM - TOP; // ポケットの間隔(Y方向)
    
    public Pocket(int no, boolean act, double x, double y, double p, double q, double fa_DEG) {
        pocketNo = no;
        active = act;
        sx = x;
        sy = y;
        selectx = p;
        noOfBanks = q;
        faceAngle = fa_DEG / 180 * Math.PI;
        rect = new Rectangle(0, 0, 8, 8);
        rect.setLayoutX(-4);
        rect.setLayoutY(-4);
        rect.setStroke( Color.LIME );
        rect.setStrokeWidth( 2 );
        rect.setFill(Color.LIME);
        rect.setTranslateX(sx);
        rect.setTranslateY(sy);
    }
    public static void initPockets() {
        realPockets = new Pocket[7];
        realPockets[0] = new Pocket(0, true, 0, 0, 0, 0, 0);
        realPockets[1] = new Pocket(1, true, LEFT,   TOP,    LEFT - 18,  TOP - 18, 225);
        realPockets[2] = new Pocket(2, true, CENTER, TOP,    CENTER,     TOP - 18, 270);
        realPockets[3] = new Pocket(3, true, RIGHT,  TOP,    RIGHT + 18, TOP - 18, 315);
        realPockets[4] = new Pocket(4, true, LEFT,   BOTTOM, LEFT - 18,  BOTTOM + 18, 135);
        realPockets[5] = new Pocket(5, true, CENTER, BOTTOM, CENTER,     BOTTOM + 18, 90);
        realPockets[6] = new Pocket(6, true, RIGHT,  BOTTOM, RIGHT + 18, BOTTOM + 18, 45);

        imaginaryPockets = new Pocket[NoOfImaginaryPockets];
        imaginaryPockets[ 0] = new Pocket( 0, true , CENTER + spanX * -3, TOP + spanY * -1.1  , 6, 2, 225);
        imaginaryPockets[ 1] = new Pocket( 1, true , CENTER + spanX * -2, TOP + spanY * -0.9  , 5, 2, 270);
        imaginaryPockets[ 2] = new Pocket( 2, true , CENTER + spanX *  1, TOP + spanY * -1.1, 4, 1, 225);
        imaginaryPockets[ 3] = new Pocket( 3, true , CENTER + spanX *  0, TOP + spanY * -1.1, 5, 1, 270);
        imaginaryPockets[ 4] = new Pocket( 4, true , CENTER + spanX *  1, TOP + spanY * -1.1, 6, 1, 315);
        imaginaryPockets[ 5] = new Pocket( 5, true , CENTER + spanX *  2, TOP + spanY * -0.9, 5, 2, 270);
        imaginaryPockets[ 6] = new Pocket( 6, true , CENTER + spanX *  3, TOP + spanY * -1.1, 4, 2, 315);

        imaginaryPockets[ 7] = new Pocket( 7, true , CENTER + spanX * -3.2, TOP, 3, 1, 225);
        imaginaryPockets[ 8] = new Pocket( 8, true , CENTER + spanX * -2, TOP, 2, 1, 270);
        imaginaryPockets[ 9] = new Pocket( 9, true , CENTER + spanX * -1, TOP, 1, 0, 225);
        imaginaryPockets[10] = new Pocket(10, true , CENTER + spanX *  0, TOP, 2, 0, 270);
        imaginaryPockets[11] = new Pocket(11, true , CENTER + spanX *  1, TOP, 3, 0, 315);
        imaginaryPockets[12] = new Pocket(12, true , CENTER + spanX *  2, TOP, 2, 1, 270);
        imaginaryPockets[13] = new Pocket(13, true , CENTER + spanX *  3.2, TOP, 1, 1, 315);
        
        imaginaryPockets[14] = new Pocket(14, true , CENTER + spanX * -3.2, BOTTOM, 6, 1, 135);
        imaginaryPockets[15] = new Pocket(15, true , CENTER + spanX * -2, BOTTOM, 5, 1,  90);
        imaginaryPockets[16] = new Pocket(16, true , CENTER + spanX * -1, BOTTOM, 4, 0, 135);
        imaginaryPockets[17] = new Pocket(17, true , CENTER + spanX *  0, BOTTOM, 5, 0,  90);
        imaginaryPockets[18] = new Pocket(18, true , CENTER + spanX *  1, BOTTOM, 6, 0,  45);
        imaginaryPockets[19] = new Pocket(19, true , CENTER + spanX *  2, BOTTOM, 5, 1,  90);
        imaginaryPockets[20] = new Pocket(20, true , CENTER + spanX *  3.2, BOTTOM, 4, 1,  45);

        imaginaryPockets[21] = new Pocket(21, true , CENTER + spanX * -3, BOTTOM + spanY *  1.1, 3, 2, 135);
        imaginaryPockets[22] = new Pocket(22, true , CENTER + spanX * -2, BOTTOM + spanY *  0.9, 2, 2,  90);
        imaginaryPockets[23] = new Pocket(23, true , CENTER + spanX * -1, BOTTOM + spanY *  1.1, 1, 1, 135);
        imaginaryPockets[24] = new Pocket(24, true , CENTER + spanX *  0, BOTTOM + spanY *  1.1, 2, 1,  90);
        imaginaryPockets[25] = new Pocket(25, true , CENTER + spanX *  1, BOTTOM + spanY *  1.1, 3, 1,  45);
        imaginaryPockets[26] = new Pocket(26, true , CENTER + spanX *  2, BOTTOM + spanY *  0.9, 2, 2,  90);
        imaginaryPockets[27] = new Pocket(27, true , CENTER + spanX *  3, BOTTOM + spanY *  1.1, 1, 2,  45);

        imaginaryPockets[28] = new Pocket(28, true , CENTER + spanX * -1, TOP + spanY * -2.2, 1, 2, 225);
        imaginaryPockets[29] = new Pocket(29, true , CENTER + spanX *  0, TOP + spanY * -2.2, 2, 2, 270);
        imaginaryPockets[30] = new Pocket(30, true , CENTER + spanX *  1, TOP + spanY * -2.2, 3, 2, 315);
        imaginaryPockets[31] = new Pocket(31, true , CENTER + spanX * -1, BOTTOM + spanY * 2.2, 4, 2, 135);
        imaginaryPockets[32] = new Pocket(32, true , CENTER + spanX *  0, BOTTOM + spanY * 2.2, 5, 2,  90);
        imaginaryPockets[33] = new Pocket(33, true , CENTER + spanX *  1, BOTTOM + spanY * 2.2, 6, 2,  45);
    }
}
