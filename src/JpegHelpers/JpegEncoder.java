package JpegHelpers;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class JpegEncoder {

    void Encode(String image) throws IOException{
        BufferedImage imageBuff = ImageIO.read(new File(image));
        System.out.println(imageBuff.getWidth());
        System.out.println(imageBuff.getHeight());
    }

//    Integer[] RgbToYcbcr(Integer[] rawData)
//    {
//    }
}
