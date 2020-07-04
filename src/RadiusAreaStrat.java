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

    private ArrayList<ContourBoundingBox> candidates = new ArrayList<>();
    private ArrayList<MatchResult> result = new ArrayList<>();

    @Override
    public ArrayList<MatchResult> recognize(BufferedImage in, RecognitionStrategy strat) {
        result.clear();
        candidates.clear();
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

            for (ContourBoundingBox bound : candidates) {
                BufferedImage norm = ImageUtil.getScaledImage(bound.getTransformedImage(in,false));
                BufferedImage flip = ImageUtil.getScaledImage(bound.getTransformedImage(in,true));
                ImageDesc i = new ImageDesc(norm,flip);
                MatchResult mr = strat.getMatch(i, SettingsPanel.RECOG_THRESH/100.0);
                if (mr != null) {
                    result.add(mr);
                }
            }
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

        doAngleHistogram();
    }

    private double getAngle(LineSegment2D_F32 segment)
    {
        double angle = Math.toDegrees(Math.atan2(segment.slopeY(),segment.slopeX()));
        if(angle < 0)
        {
            angle = angle+360;
        }
        return angle;
    }

    public void doAngleHistogram()
    {
        ArrayList<Double> angles = new ArrayList<>(found.size());
        for(LineSegment2D_F32 segment:found)
        {
            angles.add(getAngle(segment));
        }
        int[] count = new int[36];
        for(double a:angles)
        {
            double angle = (a+2.5)%180;
            int i = (int)(angle)/5;
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
        if(maxix!=-1) {
            int correspix = (maxix+18)%36;
            if(count[maxix]>=2 && count[correspix] >= 2) {
                int ix=0;
                ArrayList<LineSegment2D_F32> seg1 = new ArrayList<>(count[maxix]);
                ArrayList<LineSegment2D_F32> seg2 = new ArrayList<>(count[correspix]);
                while(ix<found.size())
                {
                    double a = angles.get(ix);
                    double angle = (a+2.5)%180;
                    int i = (int)(angle)/5;
                    if(i == maxix)
                    {
                        seg1.add(found.get(ix));
                        ix++;
                    }
                    else if(i == correspix)
                    {
                        seg2.add(found.get(ix));
                        ix++;
                    }
                    else
                    {
                        found.remove(ix);
                        angles.remove(ix);
                    }
                }
                doConnectSegments(seg1,seg2);
            }
        }
    }

    private void doConnectSegments(List<LineSegment2D_F32> seg1, List<LineSegment2D_F32> seg2)
    {
        ArrayList<Point2D_I32> corners = new ArrayList<>(4);
        LineSegment2D_F32 start = seg1.remove(0);
        LineSegment2D_F32 currentSegment = start;
        Point2D_F32 currentPoint = currentSegment.a;
        boolean parallelToStart = true;
        for(;;)
        {
            System.out.println(corners.size());
            System.out.println(seg1.size()+" "+seg2.size());
            List<LineSegment2D_F32> samples = parallelToStart ? seg2 : seg1;
            double minDist = Double.POSITIVE_INFINITY;
            LineSegment2D_F32 next = null;
            Point2D_F32 nextPoint = null;

            if(corners.size() < 3) {
                for (LineSegment2D_F32 seg : samples) {
                    double distA, distB;
                    distA = seg.a.distance(currentPoint);
                    distB = seg.b.distance(currentPoint);
                    if (distA < minDist) {
                        minDist = distA;
                        next = seg;
                        nextPoint = seg.b;
                    }
                    if (distB < minDist) {
                        minDist = distB;
                        next = seg;
                        nextPoint = seg.a;
                    }
                }
            }
            else
            {
                next = start;
            }
            corners.add(extrapolateAndCollide(currentSegment,next));
            if(corners.size() == 4)
            {
                candidates.add(new ContourBoundingBox(corners));
                return;
            }
            samples.remove(next);
            currentSegment = next;
            currentPoint = nextPoint;
            parallelToStart = !parallelToStart;
        }
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

    /**
     * Extrapolate 2 line segments, and get their collision point, or null
     * if the segments are perfectly parallel
     */
    Point2D_I32 extrapolateAndCollide(LineSegment2D_F32 s1, LineSegment2D_F32 s2)
    {
        float m1, b1, m2, b2;
        double a1, a2;

        m1 = s1.slopeY() / s1.slopeX();
        b1 = s1.a.y - (s1.a.x * m1);

        m2 = s2.slopeY() / s2.slopeX();
        b2 = s2.a.y - (s2.a.x * m2);

        if (m1 == m2)
        {
            return null;
        }

        float x = (b2 - b1) / (m1 - m2);
        float y = m1 * x + b1;
        return new Point2D_I32((int)x, (int)y);
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
            g.drawString((int)getAngle(line)+"",(int)line.a.x,(int)line.a.y);
        }

        for(ContourBoundingBox bound : candidates)
        {
            bound.draw(g);
        }
    }

    private void updateCircle()
    {
        center.x = points[0].x;
        center.y = points[0].y;
        radius = points[0].distance(points[1]);
        config.connectLines = true;
        config.regionSize = (int) (radius/3);
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
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        double dist = p.distance(points[0].x,points[0].y);
        System.out.println(dist-radius);
        if(Math.abs(radius-dist) <= 3)
        {
            draggingPoint = 1;
            return;
        }

        dist = p.distance(points[0].x,points[0].y);
        if(dist<=3)
        {
            draggingPoint = 0;
            offx = points[1].x-points[0].x;
            offy = points[1].y-points[0].y;
            return;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggingPoint = -1;
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

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Derived from the BoofCV factory source code, but exposes
     * the RANSAC iterations and the max lines per grid region
     */
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
