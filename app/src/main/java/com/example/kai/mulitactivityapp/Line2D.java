package com.example.kai.mulitactivityapp;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Denis M. Kishenko
 * @version $Revision$
 */

import android.graphics.Point;

        import java.util.NoSuchElementException;
/**
 * The Class Line2D represents a line whose data is given in high-precision
 * values appropriate for graphical operations.
 *
 * @since Android 1.0
 */
public abstract class Line2D implements Cloneable {

    protected Line2D() {
    }
    /**
     * Gets the x coordinate of the starting point.
     *
     * @return the x coordinate of the starting point.
     */
    public abstract double getX1();
    /**
     * Gets the y coordinate of the starting point.
     *
     * @return the y coordinate of the starting point.
     */
    public abstract double getY1();
    /**
     * Gets the x coordinate of the end point.
     *
     * @return the x2.
     */
    public abstract double getX2();
    /**
     * Gets the y coordinate of the end point.
     *
     * @return the y coordinate of the end point.
     */
    public abstract double getY2();
    /**
     * Gets the p the starting point.
     *
     * @return the p the starting point.
     */
    public abstract Point getP1();
    /**
     * Gets the p end point.
     *
     * @return the p end point.
     */
    public abstract Point getP2();
    /**
     * Sets the line's endpoints.
     *
     * @param x1
     *            the x coordinate of the starting point.
     * @param y1
     *            the y coordinate of the starting point.
     * @param x2
     *            the x coordinate of the end point.
     * @param y2
     *            the y coordinate of the end point.
     */
    public abstract void setLine(double x1, double y1, double x2, double y2);
    /**
     * Sets the line's endpoints.
     *
     * @param p1
     *            the starting point.
     * @param p2
     *            the end point.
     */
    public void setLine(Point p1, Point p2) {
        setLine(p1.x, p1.y, p2.x, p2.y);
    }
    /**
     * Sets the line's endpoints by copying the data from another Line2D.
     *
     * @param line
     *            the Line2D to copy the endpoint data from.
     */
    public void setLine(Line2D line) {
        setLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
    }
    /**
     * Tells where the point is with respect to the line segment, given the
     * orientation of the line segment. If the ray found by extending the line
     * segment from its starting point is rotated, this method tells whether the
     * ray should rotate in a clockwise direction or a counter-clockwise
     * direction to hit the point first. The return value is 0 if the point is
     * on the line segment, it's 1 if the point is on the ray or if the ray
     * should rotate in a counter-clockwise direction to get to the point, and
     * it's -1 if the ray should rotate in a clockwise direction to get to the
     * point or if the point is on the line determined by the line segment but
     * not on the ray from the segment's starting point and through its end
     * point.
     *
     * @param x1
     *            the x coordinate of the starting point of the line segment.
     * @param y1
     *            the y coordinate of the starting point of the line segment.
     * @param x2
     *            the x coordinate of the end point of the line segment.
     * @param y2
     *            the y coordinate of the end point of the line segment.
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the p coordinate of the test point.
     * @return the value that describes where the point is with respect to the
     *         line segment, given the orientation of the line segment.
     */
    public static int relativeCCW(double x1, double y1, double x2, double y2, double px, double py) {
        /*
         * A = (x2-x1, y2-y1) P = (px-x1, py-y1)
         */
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double t = px * y2 - py * x2; // PxA
        if (t == 0.0) {
            t = px * x2 + py * y2; // P*A
            if (t > 0.0) {
                px -= x2; // B-A
                py -= y2;
                t = px * x2 + py * y2; // (P-A)*A
                if (t < 0.0) {
                    t = 0.0;
                }
            }
        }
        return t < 0.0 ? -1 : (t > 0.0 ? 1 : 0);
    }
    /**
     * Tells where the point is with respect to this line segment, given the
     * orientation of this line segment. If the ray found by extending the line
     * segment from its starting point is rotated, this method tells whether the
     * ray should rotate in a clockwise direction or a counter-clockwise
     * direction to hit the point first. The return value is 0 if the point is
     * on the line segment, it's 1 if the point is on the ray or if the ray
     * should rotate in a counter-clockwise direction to get to the point, and
     * it's -1 if the ray should rotate in a clockwise direction to get to the
     * point or if the point is on the line determined by the line segment but
     * not on the ray from the segment's starting point and through its end
     * point.
     *
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the p coordinate of the test point.
     * @return the value that describes where the point is with respect to this
     *         line segment, given the orientation of this line segment.
     */
    public int relativeCCW(double px, double py) {
        return relativeCCW(getX1(), getY1(), getX2(), getY2(), px, py);
    }
    /**
     * Tells where the point is with respect to this line segment, given the
     * orientation of this line segment. If the ray found by extending the line
     * segment from its starting point is rotated, this method tells whether the
     * ray should rotate in a clockwise direction or a counter-clockwise
     * direction to hit the point first. The return value is 0 if the point is
     * on the line segment, it's 1 if the point is on the ray or if the ray
     * should rotate in a counter-clockwise direction to get to the point, and
     * it's -1 if the ray should rotate in a clockwise direction to get to the
     * point or if the point is on the line determined by the line segment but
     * not on the ray from the segment's starting point and through its end
     * point.
     *
     * @param p
     *            the test point.
     * @return the value that describes where the point is with respect to this
     *         line segment, given the orientation of this line segment.
     */
    public int relativeCCW(Point p) {
        return relativeCCW(getX1(), getY1(), getX2(), getY2(), p.x, p.y);
    }
    /**
     * Tells whether the two line segments cross.
     *
     * @param x1
     *            the x coordinate of the starting point of the first segment.
     * @param y1
     *            the y coordinate of the starting point of the first segment.
     * @param x2
     *            the x coordinate of the end point of the first segment.
     * @param y2
     *            the y coordinate of the end point of the first segment.
     * @param x3
     *            the x coordinate of the starting point of the second segment.
     * @param y3
     *            the y coordinate of the starting point of the second segment.
     * @param x4
     *            the x coordinate of the end point of the second segment.
     * @param y4
     *            the y coordinate of the end point of the second segment.
     * @return true, if the two line segments cross.
     */
    public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3,
                                         double y3, double x4, double y4) {
        /*
         * A = (x2-x1, y2-y1) B = (x3-x1, y3-y1) C = (x4-x1, y4-y1) D = (x4-x3,
         * y4-y3) = C-B E = (x1-x3, y1-y3) = -B F = (x2-x3, y2-y3) = A-B Result
         * is ((AxB) (AxC) <=0) and ((DxE) (DxF) <= 0) DxE = (C-B)x(-B) =
         * BxB-CxB = BxC DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB = AxB+BxC-AxC
         */
        x2 -= x1; // A
        y2 -= y1;
        x3 -= x1; // B
        y3 -= y1;
        x4 -= x1; // C
        y4 -= y1;
        double AvB = x2 * y3 - x3 * y2;
        double AvC = x2 * y4 - x4 * y2;
        // Online
        if (AvB == 0.0 && AvC == 0.0) {
            if (x2 != 0.0) {
                return (x4 * x3 <= 0.0)
                        || ((x3 * x2 >= 0.0) && (x2 > 0.0 ? x3 <= x2 || x4 <= x2 : x3 >= x2
                        || x4 >= x2));
            }
            if (y2 != 0.0) {
                return (y4 * y3 <= 0.0)
                        || ((y3 * y2 >= 0.0) && (y2 > 0.0 ? y3 <= y2 || y4 <= y2 : y3 >= y2
                        || y4 >= y2));
            }
            return false;
        }
        double BvC = x3 * y4 - x4 * y3;
        return (AvB * AvC <= 0.0) && (BvC * (AvB + BvC - AvC) <= 0.0);
    }
    /**
     * Tells whether the specified line segments crosses this line segment.
     *
     * @param x1
     *            the x coordinate of the starting point of the test segment.
     * @param y1
     *            the y coordinate of the starting point of the test segment.
     * @param x2
     *            the x coordinate of the end point of the test segment.
     * @param y2
     *            the y coordinate of the end point of the test segment.
     * @return true, if the specified line segments crosses this line segment.
     */
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        return linesIntersect(x1, y1, x2, y2, getX1(), getY1(), getX2(), getY2());
    }
    /**
     * Tells whether the specified line segments crosses this line segment.
     *
     * @param l
     *            the test segment.
     * @return true, if the specified line segments crosses this line segment.
     * @throws NullPointerException
     *             if l is null.
     */
    public boolean intersectsLine(Line2D l) {
        return linesIntersect(l.getX1(), l.getY1(), l.getX2(), l.getY2(), getX1(), getY1(),
                getX2(), getY2());
    }
    /**
     * Gives the square of the distance between the point and the line segment.
     *
     * @param x1
     *            the x coordinate of the starting point of the line segment.
     * @param y1
     *            the y coordinate of the starting point of the line segment.
     * @param x2
     *            the x coordinate of the end point of the line segment.
     * @param y2
     *            the y coordinate of the end point of the line segment.
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the the square of the distance between the point and the line
     *         segment.
     */
    public static double ptSegDistSq(double x1, double y1, double x2, double y2, double px,
                                     double py) {
        /*
         * A = (x2 - x1, y2 - y1) P = (px - x1, py - y1)
         */
        x2 -= x1; // A = (x2, y2)
        y2 -= y1;
        px -= x1; // P = (px, py)
        py -= y1;
        double dist;
        if (px * x2 + py * y2 <= 0.0) { // P*A
            dist = px * px + py * py;
        } else {
            px = x2 - px; // P = A - P = (x2 - px, y2 - py)
            py = y2 - py;
            if (px * x2 + py * y2 <= 0.0) { // P*A
                dist = px * px + py * py;
            } else {
                dist = px * y2 - py * x2;
                dist = dist * dist / (x2 * x2 + y2 * y2); // pxA/|A|
            }
        }
        if (dist < 0) {
            dist = 0;
        }
        return dist;
    }
    /**
     * Gives the distance between the point and the line segment.
     *
     * @param x1
     *            the x coordinate of the starting point of the line segment.
     * @param y1
     *            the y coordinate of the starting point of the line segment.
     * @param x2
     *            the x coordinate of the end point of the line segment.
     * @param y2
     *            the y coordinate of the end point of the line segment.
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the the distance between the point and the line segment.
     */
    public static double ptSegDist(double x1, double y1, double x2, double y2, double px, double py) {
        return Math.sqrt(ptSegDistSq(x1, y1, x2, y2, px, py));
    }
    /**
     * Gives the square of the distance between the point and this line segment.
     *
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the the square of the distance between the point and this line
     *         segment.
     */
    public double ptSegDistSq(double px, double py) {
        return ptSegDistSq(getX1(), getY1(), getX2(), getY2(), px, py);
    }
    /**
     * Gives the square of the distance between the point and this line segment.
     *
     * @param p
     *            the test point.
     * @return the square of the distance between the point and this line
     *         segment.
     */
    public double ptSegDistSq(Point p) {
        return ptSegDistSq(getX1(), getY1(), getX2(), getY2(), p.x, p.y);
    }
    /**
     * Gives the distance between the point and this line segment.
     *
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the distance between the point and this line segment.
     */
    public double ptSegDist(double px, double py) {
        return ptSegDist(getX1(), getY1(), getX2(), getY2(), px, py);
    }
    /**
     * Gives the distance between the point and this line segment.
     *
     * @param p
     *            the test point.
     * @return the distance between the point and this line segment.
     */
    public double ptSegDist(Point p) {
        return ptSegDist(getX1(), getY1(), getX2(), getY2(), p.x, p.y);
    }
    /**
     * Gives the square of the distance between the point and the line.
     *
     * @param x1
     *            the x coordinate of the starting point of the line segment.
     * @param y1
     *            the y coordinate of the starting point of the line segment.
     * @param x2
     *            the x coordinate of the end point of the line segment.
     * @param y2
     *            the y coordinate of the end point of the line segment.
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the square of the distance between the point and the line.
     */
    public static double ptLineDistSq(double x1, double y1, double x2, double y2, double px,
                                      double py) {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        double s = px * y2 - py * x2;
        return s * s / (x2 * x2 + y2 * y2);
    }
    /**
     * Gives the square of the distance between the point and the line.
     *
     * @param x1
     *            the x coordinate of the starting point of the line segment.
     * @param y1
     *            the y coordinate of the starting point of the line segment.
     * @param x2
     *            the x coordinate of the end point of the line segment.
     * @param y2
     *            the y coordinate of the end point of the line segment.
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the square of the distance between the point and the line.
     */
    public static double ptLineDist(double x1, double y1, double x2, double y2, double px, double py) {
        return Math.sqrt(ptLineDistSq(x1, y1, x2, y2, px, py));
    }
    /**
     * Gives the square of the distance between the point and the line
     * determined by this Line2D.
     *
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the square of the distance between the point and the line
     *         determined by this Line2D.
     */
    public double ptLineDistSq(double px, double py) {
        return ptLineDistSq(getX1(), getY1(), getX2(), getY2(), px, py);
    }
    /**
     * Gives the square of the distance between the point and the line
     * determined by this Line2D.
     *
     * @param p
     *            the test point.
     * @return the square of the distance between the point and the line
     *         determined by this Line2D.
     */
    public double ptLineDistSq(Point p) {
        return ptLineDistSq(getX1(), getY1(), getX2(), getY2(), p.x, p.y);
    }
    /**
     * Gives the distance between the point and the line determined by this
     * Line2D.
     *
     * @param px
     *            the x coordinate of the test point.
     * @param py
     *            the y coordinate of the test point.
     * @return the distance between the point and the line determined by this
     *         Line2D.
     */
    public double ptLineDist(double px, double py) {
        return ptLineDist(getX1(), getY1(), getX2(), getY2(), px, py);
    }
    /**
     * Gives the distance between the point and the line determined by this
     * Line2D.
     *
     * @param p
     *            the test point.
     * @return the distance between the point and the line determined by this
     *         Line2D.
     */
    public double ptLineDist(Point p) {
        return ptLineDist(getX1(), getY1(), getX2(), getY2(), p.x, p.y);
    }
    public boolean contains(double px, double py) {
        return false;
    }
    public boolean contains(Point p) {
        return false;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}