import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.github.sarxos.webcam.Webcam;

import boofcv.alg.InputSanityCheck;
import boofcv.alg.background.BackgroundModelStationary;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.factory.background.ConfigBackgroundBasic;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConfigLength;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;

class CardBoundingBoxFinder
{
    static ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);
    static BackgroundModelStationary<GrayU8> background =
		FactoryBackgroundModel.stationaryBasic(new ConfigBackgroundBasic(35, 0.01f), imageType);


    public static ArrayList<ContourBoundingBox> process(BufferedImage in, boolean removeBackground) {
        ArrayList<ContourBoundingBox> bounds = new ArrayList<ContourBoundingBox>();
        GrayU8 img = ConvertBufferedImage.convertFromSingle(in, null, GrayU8.class);
        GrayU8 binary = img.createSameShape();
        if(removeBackground)
        {
            background.segment(img, binary);
            binary=BinaryImageOps.dilate8(binary, 5, null);
            //mask(img,binary,img);
        }
        else
        {
            GThresholdImageOps.localMean(img, binary, ConfigLength.fixed(20), 1.0, true, null, null,null);
        }

        //GThresholdImageOps.localMean(img, binary, ConfigLength.fixed(20), 1.0, true, null, null,null);

        GrayU8 filtered = BinaryImageOps.erode8(binary, 2, null);
        GrayS32 label = new GrayS32(img.width,img.height);

        double imgArea = img.height*img.width;

        List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);
        bounds.clear();
        for(Contour contour:contours)
        {
            ContourBoundingBox bb = new ContourBoundingBox(contour.external);
            double ratio = bb.area()/imgArea;
            if( ratio > 0.005 && ratio < 0.5 && bb.isRoughlyRecttangular())
            {
                bounds.add(bb);
            }
        }
        return bounds;
    }

    /**
     * Adapted from BoofCV source
     */
    public static void mask(GrayU8 source, GrayU8 mask, GrayU8 output)
	{
        InputSanityCheck.checkSameShape(source,mask);
		output = InputSanityCheck.checkDeclare(source, output);
		for( int y = 0; y < source.height; y++ ) {
			int indexA = source.startIndex + y*source.stride;
			int indexB = mask.startIndex + y*mask.stride;
			int indexOut = output.startIndex + y*output.stride;

			int end = indexA + source.width;
			for( ; indexA < end; indexA++,indexB++,indexOut++) {
                byte srcval = source.data[indexA];
                byte mskval = mask.data[indexB];
				output.data[indexOut] = mskval == (byte)1 ? srcval:(byte)0;
			}
		}
	}

    public static void adaptBackground(Webcam w)
    {
        background.reset();
        BufferedImage frame = w.getImage();
        GrayU8 img = null;
        for(int i=0;i<10;i++)
        {
            img = ConvertBufferedImage.convertFromSingle(frame, img, GrayU8.class);
            background.updateBackground(img);
            frame = w.getImage();
        }
    }
}