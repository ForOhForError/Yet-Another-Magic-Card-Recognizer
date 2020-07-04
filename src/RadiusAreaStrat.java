import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import java.util.List;

import boofcv.abst.feature.detect.line.DetectLine;
import boofcv.abst.feature.detect.line.DetectLineSegment;
import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.feature.detect.line.ConnectLinesGrid;
import boofcv.alg.feature.detect.line.GridRansacLineDetector;
import boofcv.alg.feature.detect.line.gridline.*;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.factory.feature.detect.line.ConfigHoughGradient;
import boofcv.factory.feature.detect.line.ConfigLineRansac;
import boofcv.factory.feature.detect.line.FactoryDetectLine;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageGray;
import georegression.fitting.line.ModelManagerLinePolar2D_F32;
import georegression.struct.line.LineParametric2D_F32;
import georegression.struct.line.LinePolar2D_F32;
import georegression.struct.line.LineSegment2D_F32;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_I32;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;


public class RadiusAreaStrat extends AreaRecognitionStrategy{

    static final int maxLines = 10;
    private Point2D_F32 center = new Point2D_F32(320,240);
    private double radius = 200;
    private List<LineSegment2D_F32> found = new ArrayList<>();

    private boolean configChanged = true;
    private ConfigLineRansac config = new ConfigLineRansac();
    private DetectLineSegment<GrayU8> detector;

    private Point2D_I32[] points = new Point2D_I32[2];
    private int offx, offy = 0;
    private ContourBoundingBox bound;
    private int draggingPoint = -1;
    private int width;
    private int height;

    private ArrayList<Point2D_I32> inters = new ArrayList<>();

    private ArrayList<MatchResult> result = new ArrayList<>();

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat) {
        result.clear();
        if(draggingPoint == -1) {
            // convert the line into a single band image
            GrayU8 input = ConvertBufferedImage.convertFromSingle(in, null, GrayU8.class);
            GrayU8 blurred = input.createSameShape();

            // Blur smooths out gradient and improves results
            int blurRad = Math.max(1, (int) (radius / 20));
            GBlurImageOps.gaussian(input, blurred, 0, blurRad, null);

            if(configChanged) {
                detector = this.lineRansac(config, 600, 1);
                configChanged = false;
            }

            found = detector.detect(blurred);
            processSegments(strat);
        }
        return result;
    }

    private void processSegments(RecognitionStrategy strat)
    {
        //eliminate segments outside of the recognition area
        int i = 0;
        while (i < found.size()) {
            LineSegment2D_F32 line = found.get(i);
            if (!isInRadius(line)) {
                found.remove(i);
            } else {
                i++;
            }
        }

        ArrayList<Double> angles = new ArrayList<>(found.size());
        for(LineSegment2D_F32 segment:found)
        {
            double angle = Math.toDegrees(Math.atan2(segment.slopeY(),segment.slopeX()));
            if(angle < 0)
            {
                angle = angle+180;
            }
            angles.add(angle%180);
        }
        doAngleHistogram(angles);
    }

    public void doAngleHistogram(List<Double> angles)
    {
        int[] count = new int[36];
        for(double a:angles)
        {
            int i = (int)(a+2.5)%5;
            count[i] += 1;
        }
        int max = 0;
        int maxix = -1;
        for(int i=0;i<36;i++)
        {
            if(count[i]>max)
            {
                max = count[i];
                maxix = i;
            }
        }
        System.out.println(maxix*5);
    }

    private boolean isInRadius(LineSegment2D_F32 line)
    {
        double effectiveRadius = radius*1.1;
        return line.a.distance(center)<=effectiveRadius &&
                line.b.distance(center)<=effectiveRadius;
    }

    @Override
    public String getStratName() {
        return "radius";
    }

    @Override
    public String getStratDisplayName() {
        return "Radius Recognition";
    }

    Point2D_F32 collide(LineParametric2D_F32 l1, LineParametric2D_F32 l2, float low, float high)
    {
        float m1, b1, m2, b2;
        double a1, a2;
        m1 = l1.slope.y / l1.slope.x;
        b1 = l1.p.y - (l1.p.x * m1);
        a1 = Math.atan2(l1.slope.y, l1.slope.x);
        m2 = l2.slope.y / l2.slope.x;
        b2 = l2.p.y - (l2.p.x * m2);
        a2 = Math.atan2(l2.slope.y, l2.slope.x);

        double diff = Math.abs(a1-a2)%Math.PI;
        if(diff<low || diff>high)
        {
            return null;
        }

        if (m1 == m2)
        {
            return null;
        }

        float x = (b2 - b1) / (m1 - m2);
        float y = m1 * x + b1;
        return new Point2D_F32(x,y);
    }

    @Override
    public void draw(Graphics g) {
        for(int i=0;i<points.length;i++){
            Point2D_I32 p = points[i];
            if(draggingPoint == i)
            {
                g.setColor(Color.RED);
            }
            else
            {
                g.setColor(Color.WHITE);
            }
            g.fillOval(p.x-3, p.y-3, 7, 7);
        }
        if(draggingPoint != -1) {
            g.setColor(Color.RED);
        }
        else
        {
            g.setColor(Color.WHITE);
        }
        g.drawOval((int)(center.x-radius),(int)(center.y-radius),(int)(radius*2),(int)(radius*2));

        for(LineSegment2D_F32 line : found)
        {
            g.drawLine((int)line.a.x, (int)line.a.y, (int)line.b.x, (int)line.b.y);
        }
    }

    private void updateCircle()
    {
        center.x = points[0].x;
        center.y = points[0].y;
        radius = points[0].distance(points[1]);
        config.connectLines = true;
        config.regionSize = (int) (radius/2);
        config.thresholdAngle = 0.5;
        config.thresholdEdge = 50;
        configChanged = true;
    }

    @Override
    public void init(int width, int height) {
        this.width = width;
        this.height = height;
        points[0] = new Point2D_I32(width/2,height/2);
        int dist = (int)(height/2*0.85);
        points[1] = new Point2D_I32(width/2+dist,height/2);
        updateCircle();
    }

    @Override
    public SettingsEntry getSettingsEntry() {
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        for(int i=0;i<points.length;i++){
            Point2D_I32 pt = points[i];
            if(Math.abs(p.x-pt.x)<=3 && Math.abs(p.y-pt.y)<=3)
            {
                draggingPoint = i;
                if(i == 0)
                {
                    offx = points[1].x-points[0].x;
                    offy = points[1].y-points[0].y;
                }
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingPoint = -1;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(draggingPoint != -1)
        {
            Point p = e.getPoint();
            if(p.x >= 0 && p.x <= this.width)
            {
                points[draggingPoint].x = p.x;
            }
            if(p.y >= 0 && p.y <= this.height)
            {
                points[draggingPoint].y = p.y;
            }
            if(draggingPoint == 0)
            {
                points[1].x = points[0].x + offx;
                points[1].y = points[0].y + offy;
            }
        }
        updateCircle();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private DetectLineSegmentsGridRansac<GrayU8,GrayS16> lineRansac(ConfigLineRansac config, int maxIter, int maxLines) {

        if( config == null )
            config = new ConfigLineRansac();

        ImageGradient<GrayU8,GrayS16> gradient = FactoryDerivative.sobel(GrayU8.class,GrayS16.class);

        ModelManagerLinePolar2D_F32 manager = new ModelManagerLinePolar2D_F32();
        GridLineModelDistance distance = new GridLineModelDistance((float)config.thresholdAngle);
        GridLineModelFitter fitter = new GridLineModelFitter((float)config.thresholdAngle);

        ModelMatcher<LinePolar2D_F32, Edgel> matcher =
                new Ransac<>(123123, manager, fitter, distance, maxIter, 1);

        GridRansacLineDetector<GrayS16> alg =
                (GridRansacLineDetector)new ImplGridRansacLineDetector_S16(config.regionSize,maxLines,matcher);


        ConnectLinesGrid connect = null;
        if( config.connectLines )
            connect = new ConnectLinesGrid(Math.PI*0.01,1,8);

        return new DetectLineSegmentsGridRansac<>(alg, connect, gradient, config.thresholdEdge, GrayU8.class, GrayS16.class);
    }
}
