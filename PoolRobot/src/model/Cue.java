/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 *
 * @author otsuka
 */
public class Cue extends Line {
    
    public Cue() {
        setEndX(0);
        setEndY(0);
        setStartX(-100);
        setStartY(0);
        setStroke(Color.RED);
        setStrokeWidth(2);
        setTranslateX(0);
        setTranslateY(0);
        setRotate(Math.PI / 2);
    }
    
    public void setDirection(double dir_RAD) {
        Platform.runLater(()-> {
            setRotate(dir_RAD);
        });
    }

    public void setPosition(double x, double y) {
        Platform.runLater(()-> {
            setTranslateX(x);
            setTranslateY(y);
        });
    }

    public void setPosition(double x, double y, double dir_RAD) {
        Platform.runLater(()-> {
            setTranslateX(x);
            setTranslateY(y);
            setRotate(dir_RAD);
        });
    }
}
