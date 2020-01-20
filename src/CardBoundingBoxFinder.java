import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConfigLength;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;

class CardBoundingBoxFinder
{
    public static ArrayList<ContourBoundingBox> process(BufferedImage in) {
        ArrayList<ContourBoundingBox> bounds = new ArrayList<ContourBoundingBox>();
        GrayU8 img = ConvertBufferedImage.convertFromSingle(in, null, GrayU8.class);
        GrayU8 binary = img.createSameShape();
        GThresholdImageOps.localMean(img, binary, ConfigLength.fixed(57), 1.0, true, null, null,null);

        GrayU8 filtered = BinaryImageOps.erode8(binary, 3, null);
        GrayS32 label = new GrayS32(img.width,img.height);

        List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);
        bounds.clear();
        for(Contour contour:contours)
        {
            int size = contour.internal.size();
            if(size > 0 && size < 3)
            {
                ContourBoundingBox bb = new ContourBoundingBox(contour.external);
                if(bb.area()>1000 && bb.isRoughlyRecttangular())
                {
                    bounds.add(bb);
                    bb.draw(RecogApp.INSTANCE.getCanvasGraphics());
                }
            }
        }
        return bounds;
    }
}