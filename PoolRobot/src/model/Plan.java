/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javafx.geometry.Point2D;

/**
 *
 * @author otsuka
 */
public class Plan {

    public Ball ball;         // target ball No.
    public Pocket pocket;       // target pocket No.
    public Point2D aimPoint;   // aim point.
    public double deviation;   //
    public double offset;
    public double incident;
    public double distance;
    public double score;
    public boolean available;
    public String cause;

    public Plan() {
        this.ball = null;
        this.pocket = null;
        this.aimPoint = null;
        this.deviation = 0.;
        this.offset = 0.;
        this.incident = 0.;
        this.distance = 0.;
        this.score = 0.;
        this.available = false;
        this.cause = "";
    }
    
    @Override
    public String toString() {
        return String.format("score=%.2f ball=%d pocket=%d offset=%.3f inc.=%.2f dis.=%.0f\n", this.score, this.ball.ballNo, this.pocket.pocketNo, this.offset, this.incident, this.distance);
    }

}
