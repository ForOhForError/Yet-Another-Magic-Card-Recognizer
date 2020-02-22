import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.transform.homography.HomographyPointOps_F64;

class HomographyCoverage
{
    public static double calculateCoverage(Point2D_F64[] points, Homography2D_F64 H)
    {
        Point2D_F64[] results = new Point2D_F64[points.length];
        for(int i=0; i<points.length; i++)
        {
            results[i] = HomographyPointOps_F64.transform(H, points[i], null);
        }
        double[] a = calculateBounds(points);
        double[] b = calculateBounds(results);
        double c = calculateCoverage(a,b);
        return c;
    }

    public static double calculateCoverage(double[] a, double[] b)
    {
        if(!overlaps(a,b))
        {
            return 0;
        }

        double x_min = Math.max(a[0], b[0]);
        double x_max = Math.min(a[1], b[1]);
        double y_min = Math.max(a[2], b[2]);
        double y_max = Math.min(a[3],b[3]);

        double overlap = (x_max-x_min)*(y_max-y_min);
        double aa = (a[1]-a[0])*(a[3]-a[2]);
        double ba = (b[1]-b[0])*(b[3]-b[2]);
        double union = aa+ba-overlap;
        return overlap/union;
    }

    public static boolean overlaps(double[] a, double[] b)
    {
        for(int i=0; i<=2; i+=2)
        {
            int min = i;
            int max = i+1;
            if(a[min] > b[max] || a[max] < b[min])
            {
                return false;
            }
        }
        return true;
    }

    public static double[] calculateBounds(Point2D_F64[] points)
    {
        double[] aabb = new double[4];
        aabb[0] = aabb[2] = Integer.MAX_VALUE;
        aabb[1] = aabb[3] = Integer.MIN_VALUE;
        for(Point2D_F64 p : points)
        {
            if(p.x < aabb[0])
            {
                aabb[0] = p.x;
            }
            if(p.x > aabb[1])
            {
                aabb[1] = p.x;
            }
            if(p.y < aabb[2])
            {
                aabb[2] = p.y;
            }
            if(p.y > aabb[3])
            {
                aabb[3] = p.y;
            }
        }
        return aabb;
    }
}