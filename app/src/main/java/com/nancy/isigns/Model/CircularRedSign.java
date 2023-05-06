package com.nancy.isigns.Model;

import org.opencv.core.Point;

public class CircularRedSign {

    private Point center;
    private float radius;


    CircularRedSign(Point center, float radius){

        this.center = center;
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }
}
