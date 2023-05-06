package com.nancy.isigns.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Switch;

import com.nancy.isigns.R;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.label.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SignsDetection {

    private static String LOGTAG = "centers_log";

    public static Mat Red_thresholding(Mat m) {

        Mat hsv_m = new Mat();
        Imgproc.cvtColor(m, hsv_m, Imgproc.COLOR_RGB2HSV);
        Mat threshold_img = new Mat();
        Mat threshold_img1 = new Mat();
        Mat threshold_img2 = new Mat();
        Core.inRange(hsv_m, new Scalar(0,100,100), new Scalar(10,255,255), threshold_img1);
        Core.inRange(hsv_m, new Scalar(160,100,100), new Scalar(179,255,255), threshold_img2);
        Core.bitwise_or(threshold_img1, threshold_img2, threshold_img);
        Imgproc.GaussianBlur(threshold_img, threshold_img, new Size(9,9),2,2);
        return threshold_img;
    }

    public static Mat canny(Mat hsv_m) { // on thresholded hsv_img

        Mat canny_img = new Mat();
        int thresh = 100;
        Imgproc.Canny(hsv_m, canny_img, thresh, thresh*2);

        return canny_img;
    }

    public static Mat GreenC(Mat m) {

        Mat m2 = m;

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();

        Imgproc.findContours(canny(Red_thresholding(m)), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        float[] radius = new float[1];
        Point center = new Point();

        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint contour = contours.get(i);

            double contourArea = Imgproc.contourArea(contour);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);

            if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {

                Imgproc.circle(m2, center, (int)radius[0], new Scalar(0,255,0), 3);
            }

        }
        return m2;
    }



    public static ArrayList<CircularRedSign> greenCircles (Mat m) {

        ArrayList<CircularRedSign> signsList= new ArrayList<CircularRedSign>();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();

        Imgproc.findContours(canny(Red_thresholding(m)), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        float[] radius = new float[1];
        Point center = new Point();

        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint contour = contours.get(i);


            double contourArea = Imgproc.contourArea(contour);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);


            if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {


                Log.d(LOGTAG, "center Value: " + Double.toString(center.x)); //debugage multiple green circles

                signsList.add(new CircularRedSign(center, radius[0]));



            }

        }
        return signsList;

    }

    public static Mat signsExtraction(Mat m) {

        Mat m2 = m;
        Mat pannel = null;

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();

        Imgproc.findContours(canny(Red_thresholding(m)), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        float[] radius = new float[1];
        Point center = new Point();

        for (int i = 0; i < contours.size(); i++) {

            MatOfPoint contour = contours.get(i);

            double contourArea = Imgproc.contourArea(contour);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);

            if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {


                Imgproc.circle(m, center, (int)radius[0], new Scalar(0,255,0), 2);
                Rect rect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(m, new Point(rect.x, rect.y),
                        new Point(rect.x+rect.width, rect.y+rect.height),
                        new Scalar(0, 255, 0), 2);
                Mat tmp = m2.submat(rect.y, rect.y+rect.height, rect.x, rect.x+rect.width);
                pannel = Mat.zeros(tmp.size(), tmp.type());
                tmp.copyTo(pannel);


            }

        }
        return pannel;
    }

    public static Category categoryRecognizer(List<Category> probability){

        Category max = probability.get(0);

        for(int i = 1; i < probability.size(); i++){

            if (probability.get(i).getScore() > max.getScore()){

                max = probability.get(i);
            }

        }
        return max;
    }

    public static Bitmap refDisplay(Category cat, Context ctx){

        Bitmap bitmap;

        switch(cat.getLabel()){

            case "30sign":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ref30);
                break;
            case "50sign":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ref50);
                break;
            case "70sign":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ref70);
                break;
            case "90sign":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ref90);
                break;
            case "110sign":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ref110);
            case "double":
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.refdouble);
                break;
            default:
                bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.unrecog);

        }

        return bitmap;
    }

}
