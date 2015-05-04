package com.example.kai.mulitactivityapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.GestureDetector.*;

/**
 * Created by Kai on 01/03/2015.
 */
class PathDrawView extends View {

    Point[] results;

    private static final int LOOP_DISTANCE = 2000;
    private static final double EXP = 3;
    private static final double ANGLE_THRESHOLD = Math.toRadians(0.5f);
    private static final int MAX_POINTS = 0;
    private static final double EPSILON = 3;
    private volatile Stack<Point> points = new Stack<>();
    private Paint pathPaint,walkerPaint;
    private static final int orange = Color.argb(255, 255, 102, 0);
    private static final int yellow = Color.argb(255, 255, 255, 0);
    private TextView textView;
    private static final int MIN_DISTANCE = 30;
    List<String> stringList;

    // private Walker walker = new Walker(300,300,Math.PI/6,3.5,Math.PI/60);


    public PathDrawView(Context context) {
        super(context);
        init(context);
    }

    public PathDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PathDrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(6);
        pathPaint.setColor(orange);
        pathPaint.setShadowLayer(10, 0, 0, Color.BLACK);

        walkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        walkerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        walkerPaint.setColor(Color.argb(255, 0, 0, 0));
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    /*
        Timer t;
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(points.size()>=2)
                    walker.update(points);
                    postInvalidate();
            }
        },20,20);
        */
    }

    private void invalidateScreen(){
        this.invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!points.isEmpty()) {
            Point prev = points.firstElement();
            boolean b = true;
            for (Point p : points) {
                if (b) {
                    pathPaint.setColor(orange);
                    b = false;
                }
                canvas.drawLine(p.x, p.y, prev.x, prev.y, pathPaint);
                if (pathPaint.getColor() == orange) pathPaint.setColor(Color.BLACK);
                else pathPaint.setColor(orange);
                prev = p;
            }
        }
       // drawTriangle(canvas,(int)walker.x,(int)walker.y,walker.theta);
    }

    private void drawTriangle(Canvas canvas,int x, int y, double theta) {
        Point o = new Point(x,y);
        Point a = rotatePoint(new Point(x,y-35),o,theta-Math.PI/2);
        Point b = rotatePoint(new Point(x-25,y+35),o,theta-Math.PI/2);
        Point c = rotatePoint(new Point(x+25,y+35),o,theta-Math.PI/2);

        //rotatePoint(a,o,theta);rotatePoint(b,o,theta);rotatePoint(c,o,theta);

        Path triPath = new Path();
        triPath.moveTo(a.x, a.y);
        triPath.lineTo(b.x,b.y);
        triPath.lineTo(c.x, c.y);
        triPath.lineTo(a.x, a.y);
        canvas.drawPath(triPath, walkerPaint);
    }

    static Point rotatePoint(Point pointToRotate, Point centerPoint, double theta)
    {

        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        return new Point((int)
                            (cosTheta * (pointToRotate.x - centerPoint.x) -
                                    sinTheta * (pointToRotate.y - centerPoint.y) + centerPoint.x),
                    (int)
                                    (sinTheta * (pointToRotate.x - centerPoint.x) +
                                            cosTheta * (pointToRotate.y - centerPoint.y) + centerPoint.y)
        );
    }


    public void addPoint(float x, float y) {
        points.add(new Point((int) x, (int) y));
        this.invalidate();
    }

    public void deletePoints() {
        points.removeAllElements();
        this.invalidate();
    }

    private VelocityTracker mVelocityTracker = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int index = event.getActionIndex();
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_MOVE) {
            this.addPoint(event.getX(), event.getY());
        } else if (action == MotionEvent.ACTION_DOWN) {
            this.deletePoints();
            this.pathPaint.setColor(yellow);
        } else if (action == MotionEvent.ACTION_UP) {
            this.validateLine();
            this.pathPaint.setColor(orange);
        }

        return true;
    }

    private synchronized void validateLine() {


        /*walker.y = points.get(0).y;
        walker.x = points.get(0).x;
        if(points.size()>=2) {
            walker.theta = Walker.calcTheta(points.get(0), points.get(1));
            walker.counter = 0;
        }

        System.out.println(walker.x+","+walker.y);
*/
        int D = getDispSquared();
        if (D < LOOP_DISTANCE) forceLoop(points);


        while(points.size()<MAX_POINTS) {
            points = subdivide(points);
        }

        //<TO DO> Cast point list to array, run simplifyLines, cast back to Stack/List
        int pSize = points.size();

        Point[] tmp = new Point[points.size()];
        for(int i=0;i<tmp.length;i++){
            tmp[i] = points.get(i);
        }

        Point[] newPoints = simplifyLines(tmp);
        List<Point> list = Arrays.asList(newPoints);
        points.removeAllElements();
        points.addAll(list);
        

        for(int i=0;i<points.size()-2;i++){
            if(Distance(points.get(i), points.get(i+1))<MIN_DISTANCE){
                points.remove(i+1);
                i--;
            }


        }


        /*
        points.removeAllElements();
        for(int i=0;i<newPoints.length;i++) {
          points.add(newPoints[i]);
        }
        */
        System.out.println(pSize-points.size()+" points removed");








        if (textView != null) {
            textView.setText("");
            for (String s:toStringList(points)) textView.append(s + "\n");
            stringList = toStringList(points);
            textView.setMovementMethod(new ScrollingMovementMethod());
        }



        
        this.invalidate();
    }


    // <TO DO> Refactor this method to take and return an array of Points.
    private synchronized Point[] simplifyLines(Point[] points) {
        /* IMPLEMENTATION OF DOUGLAS-PEUCKER ALGORTHM, see: http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
        function DouglasPeucker(PointList[], epsilon)
            // Find the point with the maximum distance
            dmax = 0
            index = 0
            end = length(PointList)
            for i = 2 to ( end - 1) {
                d = shortestDistanceToSegment(PointList[i], Line(PointList[1], PointList[end]))
                if ( d > dmax ) {
                    index = i
                    dmax = d
                }
            }
            // If max distance is greater than epsilon, recursively simplify
            if ( dmax > epsilon ) {
                // Recursive call
                recResults1[] = DouglasPeucker(PointList[1...index], epsilon)
                recResults2[] = DouglasPeucker(PointList[index...end], epsilon)

                // Build the result list
                ResultList[] = {recResults1[1...length(recResults1)-1] recResults2[1...length(recResults2)]}
            } else {
                ResultList[] = {PointList[1], PointList[end]}
            }
            // Return the result
            return ResultList[]
        end
         */

        /*
          If the input only consists of 2 points, return the input.
         */
        if(points.length<=2)return points;


        /*
        Check which point is the furthest from the line between the first and last element.
         */
        int dmax = 0, index = 0;
        int end = points.length-1;
        for(int i = 1;i<end;i++){
           // int d = LineToPointDistance2D(points[0],points[end],points[i]);
            int d = (int)Line2D.ptSegDist(points[0].x, points[0].y, points[end].x, points[end].y, points[i].x, points[i].y);
            if(d > dmax){
                index = i;
                dmax = d;
            }
        }

        /*
        If the max distance is larger than a set constant.
         */
        if(dmax > EPSILON){

            /*
            Create new point array including the points from the first point to the point furthest
            from the line.
             */
            Point[] points2 = new Point[index+1];
            for(int i=0;i<points2.length;i++){
                points2[i] = points[i];
            }

            /*
            Recursively simplify this "first half" array.
             */
            Point[] recResult1 = simplifyLines(points2);


            /*
            Cut out the last element to account for duplication the "index" element for the merging
            of the two results.
             */
            if(recResult1.length>0)
                recResult1 = Arrays.copyOf(recResult1, recResult1.length - 1);


            /*
            Create a new point array with all the elements from the index element to the end of the
            original array.
             */
            Point[] points3 = new Point[points.length-points2.length+1];
            for(int i=0;i<points3.length;i++){
                points3[i] = points[i+index];
            }

            /*
            Recursively simplify the "second half" array.
             */
            Point[] recResult2 = simplifyLines(points3);

            /*
            Add the two arrays together...
             */
            results = new Point[recResult1.length+recResult2.length];
            for(int i=0;i<recResult1.length;i++){
                results[i] = recResult1[i];
            }
            for(int i=recResult1.length;i<results.length;i++){
                results[i] = recResult2[i-recResult1.length];
            }

        /*
        If the distance was not further than epsilon, simplify the line to a single segment from
        the first to the last point.
         */
        }else{
            results = new Point[2];
            results[0]= points[0];
            results[1]=points[points.length-1];
        }

        /*
        Return results.
         */
        return results;
    }

    private static int shortestDistToSeg(Point point, Point start, Point end) {
        return -1;
    }

    public Point[] concat(Point[] a, Point[] b) {
        int aLen = a.length;
        int bLen = b.length;
        Point[] c= new Point[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private static Stack<Point> subdivide(Stack<Point> points) {
        Stack<Point> result = new Stack<>();

        //For each line segment, add point in middle, splitting line segment into two
        for(int i = 0;i<points.size()-1;i++){
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            int x = (p1.x+p2.x)/2, y= (p1.y+p2.y)/2;
            result.add(p1);
            result.add(new Point(x,y));
        }
        result.add(points.lastElement());

        //For each point (Excluding final) move to the midpoint between it and the point immediatly after
        for(int i = 1;i<result.size()-1;i++){
            Point p1 = result.get(i);
            Point p2 = result.get(i+1);
            int x = (p1.x+p2.x)/2, y= (p1.y+p2.y)/2;
            result.get(i).x = x;
            result.get(i).y = y;
        }
        return result;
    }

    private void forceLoop(Stack<Point> points) {
        if(!points.empty()) {
            Point totalVector = new Point(points.peek().x - points.get(0).x, points.peek().y - points.get(0).y);
            for (int i = 0; i < points.size(); i++) {
                double ratio = Math.pow((float) (i) / (points.size() - 1), EXP);
                points.get(i).x -= totalVector.x * ratio;
                points.get(i).y -= totalVector.y * ratio;
            }
        }
    }

    public void assignTextView(TextView textView) {
        this.textView = textView;
    }

    public int getDispSquared() {
        if(points.size()>0) {
            int dx = points.get(0).x - points.peek().x;
            int dy = points.get(0).y - points.peek().y;

            return dx * dx + dy * dy;
        }else return 0;
    }

    static List<String> toStringList(List<Point> pointList){
        ArrayList<String> stringList = new ArrayList<String>();
        int instructionCount = (2*pointList.size())-3;
        stringList.add(instructionCount+"!");
        for(int i=0;i<(pointList.size()-1);i++){


            Point firstPoint = pointList.get(i);
            Point nextPoint = pointList.get(i+1);
            Point lastPoint;

            int xTwo = nextPoint.x;
            int xOne = firstPoint.x;
            int xLast;

            int yTwo = nextPoint.y;
            int yOne = firstPoint.y;
            int yLast;

            int dx1 = xTwo-xOne;
            int dy1 = yTwo-yOne;

            double vectorLengthA = Math.sqrt(Math.pow(dx1, 2) + Math.pow(dy1, 2));

            String tmpStr = "goForward "+(int)vectorLengthA+"*";

            stringList.add(tmpStr);

            if(i<pointList.size()-2){
                firstPoint = pointList.get(i+1);
                nextPoint = pointList.get(i+2);
                lastPoint = pointList.get(i);


                xTwo = nextPoint.x;
                xOne = firstPoint.x;
                xLast = lastPoint.x;

                yTwo = nextPoint.y;
                yOne = firstPoint.y;
                yLast = lastPoint.y;

                double rotation;

                double angleOne =(Math.atan2((yOne-yLast), (xOne-xLast)));
                double angleTwo =(Math.atan2((yTwo-yOne), (xTwo-xOne)));


                rotation = (angleTwo-angleOne);
                if(rotation>(Math.PI)) rotation = rotation-(2*(Math.PI));


                if(rotation>0){
                    rotation = (int)Math.toDegrees(rotation);
                    tmpStr = "rotateClockwise "+rotation+"*";
                }else {
                    rotation = -(int)Math.toDegrees(rotation);
                    tmpStr =  "rotateCounterClockwise "+rotation+"*";
                }
                stringList.add(tmpStr);
            }
        }

        return stringList;
    }


    //Compute the dot product AB . AC
    static private double DotProduct(Point A,  Point B, Point C)
    {
        double[] AB = new double[2];
        double[] BC = new double[2];
        AB[0] = B.x - A.x;
        AB[1] = B.y - A.y;
        BC[0] = C.x - B.x;
        BC[1] = C.y - B.y;
        double dot = AB[0] * BC[0] + AB[1] * BC[1];

        return dot;
    }

    //Compute the cross product AB x AC
    static  private double CrossProduct(Point A,  Point B, Point C)
    {
        double[] AB = new double[2];
        double[] AC = new double[2];
        AB[0] = B.x - A.x;
        AB[1] = B.y - A.y;
        AC[0] = C.x - A.x;
        AC[1] = C.y - A.y;
        double cross = AB[0] * AC[1] - AB[1] * AC[0];

        return cross;
    }

    //Compute the distance from A to B
    static double Distance(Point A, Point B)
    {
        double d1 = A.x - B.x;
        double d2 = A.y - B.y;

        return Math.sqrt(d1 * d1 + d2 * d2);
    }
    //Compute the distance from AB to C

    //if isSegment is true, AB is a segment, not a line.
    /*
    static int  LineToPointDistance2D(Point A, Point B, Point C)
    {

        if(A.x==B.x&&A.y==B.y){
            return (int)Distance(A,C);
        }

        double dist = CrossProduct(A, B, C) / Distance(A, B);

        double dot1 = DotProduct(A, B, C);
        if (dot1 > 0)
            return (int)Distance(B, C);

        double dot2 = DotProduct(B, A, C);
        if (dot2 > 0)
            return (int)Distance(A, C);

        return 0;
    }
    */
    public static int LineToPointDistance2D(Point segA, Point segB, Point p){

        if(segA.x==segB.x&&segA.y==segB.y) {
            return (int) Distance(segA, p);
        }
        Point p2 = new Point(segB.x - segA.x, segB.y - segA.y);
        int something = p2.x*p2.x + p2.y*p2.y;
        int u = ((p.x - segA.x) * p2.x + (p.y - segA.y) * p2.y) / something;

        if (u > 1)
            u = 1;
        else if (u < 0)
            u = 0;

        int x = segA.x + u * p2.x;
        int y = segA.y + u * p2.y;

        int dx = x - p.x;
        int dy = y - p.y;

        int dist = (int)Math.sqrt(dx*dx + dy*dy);

        return dist;
    }
}
