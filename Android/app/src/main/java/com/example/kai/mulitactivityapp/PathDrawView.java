package com.example.kai.mulitactivityapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kai on 01/03/2015.
 */
class PathDrawView extends View {

    private final int DEFAULT_SCALE = this.getWidth();
    private static int LOOP_DISTANCE = 2000;
	Point[] results;
    private int VIEW_DIMENSION = 0;

   
    private static int EDIT_DISTANCE = 30;
    private static final double EXP = 3;
    private static final double ANGLE_THRESHOLD = Math.toRadians(0.5f);
    private static final int MAX_POINTS = 0;
    private static double EPSILON = 3;
    private volatile Stack<Point> points = new Stack<>();
    private Paint pathPaint,errorPaint;
    private static final int darkGray = Color.argb(255,70,70,70);
    private static final int yellow = Color.argb(255, 255, 225, 0);
    private static final int MIN_DISTANCE = 5;
    private int obstacleIndex = -1;
    List<String> stringList;
    private SharedPreferences sharedPreferences;

    private Stack<Point> pointsValidated = new Stack<>();
    private Stack<Point> pointsRaw = new Stack<>();
    boolean validated = false;
    private static Bitmap givenBackgroundBitmap = null;
    private Button swapButton;

    public static void setGivenBackgroundBitmap(Bitmap givenBackgroundBitmap) {
        PathDrawView.givenBackgroundBitmap = givenBackgroundBitmap;
    }

    // private Walker walker = new Walker(300,300,Math.PI/6,3.5,Math.PI/60);

    public void onObstacleDetected(int instructionIndex){
        obstacleIndex = (instructionIndex+1)/2+1;
    }

    public void resetObstacleDetected(){
        obstacleIndex = -1;
    }


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

    @Override
    public void onSizeChanged(int w, int h, int prevW, int prevH){
        super.onSizeChanged(w, h, prevW, prevH);

        VIEW_DIMENSION = this.getWidth();
        System.out.println("the width is: " + VIEW_DIMENSION);
    }


    private void init(Context context) {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(6);
        pathPaint.setShadowLayer(10, 0, 0, Color.BLACK);

        errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setStyle(Paint.Style.STROKE);
        errorPaint.setStrokeWidth(6);
        errorPaint.setShadowLayer(10, 0, 0, Color.BLACK);
        errorPaint.setColor(Color.RED);

        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Timer t;
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(errorPaint.getColor()==Color.RED){
                    errorPaint.setColor(darkGray);
                }else{
                    errorPaint.setColor(Color.RED);
                }
                postInvalidate();
            }
        },400,400);

    }
    private void invalidateScreen(){
        this.invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(givenBackgroundBitmap != null){
            canvas.drawBitmap(givenBackgroundBitmap, 0, 0, null);
        }

        Stack<Point> points;

        if(validated)points = pointsValidated;
        else points = pointsRaw;

        if (!points.isEmpty()) {
            Point prev = points.firstElement();
            int i = 0;
            for (Point p : points) {
                if(i++== obstacleIndex){
                    canvas.drawLine(p.x, p.y, prev.x, prev.y, errorPaint);
                }else{
                    canvas.drawLine(p.x, p.y, prev.x, prev.y, pathPaint);
                }

                prev = p;
            }
        }
       // drawTriangle(canvas,(int)walker.x,(int)walker.y,walker.theta);
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
        pointsRaw.add(new Point((int) x, (int) y));
        this.invalidate();
    }

    public void deletePoints() {
        pointsRaw.removeAllElements();
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
            obstacleIndex = -1;
            String editValue = sharedPreferences.getString("MODIFY_LIST","testString2");

            if(editValue.compareTo("testString1") == 0){
                EDIT_DISTANCE = 0;
            }
            if(editValue.compareTo("testString2") == 0){
                EDIT_DISTANCE = 25;
            }
            if(editValue.compareTo("testString3") == 0){
                EDIT_DISTANCE = 50;
            }

            if(getDistToPath(event.getX(), event.getY(),pointsRaw)<EDIT_DISTANCE){
                this.pathPaint.setColor(yellow);
                Point p = getClosestPointOnPath(event.getX(), event.getY());
                int i = pointsRaw.indexOf(p);
                Stack<Point> temp = new Stack<>();
                temp.addAll(pointsRaw.subList(0,i+1));
                pointsRaw = temp;
            }else{
                this.deletePoints();
                this.pathPaint.setColor(yellow);
            }
            this.validated = false;
        } else if (action == MotionEvent.ACTION_UP) {
            this.validateLine();
            validated = true;
            this.pathPaint.setColor(darkGray);
        }

        return true;
    }

    private Point getClosestPointOnPath(float x, float y) {
        double minDist = Double.MAX_VALUE;
        Point minP = pointsRaw.get(0);
        for(int i = 0;i<pointsRaw.size()-1;i++){
            Point A = pointsRaw.get(i);
            Point B = pointsRaw.get(i+1);
            int d = (int)Line2D.ptSegDist(A.x, A.y, B.x, B.y, x, y);
            if(d<minDist){
                minP = A;
                minDist = d;
            }
        }
        return minP;
    }

    private static int getDistToPath(float x, float y, Stack<Point> points) {
        int minDist = Integer.MAX_VALUE;
        for(int i = 0;i<points.size()-1;i++){
            Point A = points.get(i);
            Point B = points.get(i+1);
            int d = (int)Line2D.ptSegDist(A.x, A.y, B.x, B.y, x, y);
            minDist = Math.min(d,minDist);
        }
        return minDist;
    }

    public synchronized void validateLine() {
        this.validated = false;

 		String loopValue = sharedPreferences.getString("LOOP_LIST","testString2");

        if(loopValue.compareTo("testString1") == 0){
            LOOP_DISTANCE = 0;
        }
        if(loopValue.compareTo("testString2") == 0){
            LOOP_DISTANCE = 1000;
        }
        if(loopValue.compareTo("testString3") == 0){
            LOOP_DISTANCE = 2000;
        }


        int D = getDispSquared(pointsRaw);
             if (D < LOOP_DISTANCE) pointsValidated = forceLoop(pointsRaw);
        else{
            pointsValidated = getPointListClone(pointsRaw);
        }


        while(pointsValidated.size()<MAX_POINTS) {
            pointsValidated = subdivide(pointsValidated);
        }

        //<TO DO> Cast point list to array, run simplifyLines, cast back to Stack/List
        int pSize = pointsValidated.size();

        Point[] tmp = new Point[pointsValidated.size()];
        for(int i=0;i<tmp.length;i++){
            tmp[i] = pointsValidated.get(i);
        }

        Point[] newPoints = simplifyLines(tmp);
        List<Point> list = Arrays.asList(newPoints);
        pointsValidated.removeAllElements();
        pointsValidated.addAll(list);

        for(int i=0;i<pointsValidated.size()-2;i++){
            if(Distance(pointsValidated.get(i), pointsValidated.get(i+1))<MIN_DISTANCE){
                pointsValidated.remove(i+1);
                i--;
            }


        }

        stringList = toStringList(pointsValidated, DEFAULT_SCALE, VIEW_DIMENSION, sharedPreferences);


        this.invalidate();
    }

    private Stack<Point> getPointListClone(Stack<Point> input) {
        Stack<Point> result = new Stack<>();
        for(Point p:input)
            result.add(new Point(p.x,p.y));
        return result;
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
        int epsilon = 3;
        String optionValue = sharedPreferences.getString("PREF_LIST", "Low");

        if(optionValue.compareTo("testValue1") == 0){
            epsilon = 0;
        }else if(optionValue.compareTo("testValue2") == 0){
            epsilon = 3;
        }else if(optionValue.compareTo("testValue3") == 0){
            epsilon = 15;
        }

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


        Point[] results;

        /*
        If the max distance is larger than a set constant.
         */
        if(dmax > epsilon){

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
        if(!points.empty())
            result.add(points.lastElement());

        //For each point (Excluding final) move to the midpoint between it and the point immediately after
        for(int i = 1;i<result.size()-1;i++){
            Point p1 = result.get(i);
            Point p2 = result.get(i+1);
            int x = (p1.x+p2.x)/2, y= (p1.y+p2.y)/2;
            result.get(i).x = x;
            result.get(i).y = y;
        }
        return result;
    }

    private static Stack<Point> forceLoop(Stack<Point> input) {
        Stack<Point> points = new Stack<Point>();
        points.addAll(input);
        if(!points.empty()) {
            Point totalVector = new Point(points.peek().x - points.get(0).x, points.peek().y - points.get(0).y);
            for (int i = 0; i < points.size(); i++) {
                double ratio = Math.pow((float) (i) / (points.size() - 1), EXP);
                points.get(i).x -= totalVector.x * ratio;
                points.get(i).y -= totalVector.y * ratio;
            }
        }
        return points;
    }


    public static int getDispSquared(Stack<Point> points) {
        if(points.size()>0) {
            int dx = points.get(0).x - points.peek().x;
            int dy = points.get(0).y - points.peek().y;

            return dx * dx + dy * dy;
        }else return 0;
    }

    static List<String> toStringList(List<Point> pointList, double scale, int screenSize, SharedPreferences sharedPreferences){
        ArrayList<String> stringList = new ArrayList<String>();
        int instructionCount = (2*pointList.size())-3;
        stringList.add(instructionCount+"!");

        String scaleValue = sharedPreferences.getString("SCALE_EDITTEXT", "450");
        try {
            scale = Double.parseDouble(scaleValue);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
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
            double ratio = scale/screenSize;
            vectorLengthA = ratio*vectorLengthA;

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

    public void addSharedPreferences(SharedPreferences shPref) {
        this.sharedPreferences = shPref;
    }

    public void setswapButton(Button swapButton) {
        this.swapButton = swapButton;
    }

    public Button getSwapButton() {
        return swapButton;
    }

    public void setSwapButton(Button swapButton) {
        if(swapButton == null)System.out.println("OMFG");
        this.swapButton = swapButton;
    }
}
