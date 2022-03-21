package JpegHelpers;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JpegDecoder { // JPG and JPEG are the same thing :)

    /*
    0xFFD8 - Start of Image
    0xFFE0 - App specific JFIF
    0xFFE1 - EXIF
    0xFFDB - Define Quant Tables
    0xFFC0 - Frame Start
    0xFFDD - Define Restart Interval
    0xFFC4 - Define Huffman Tables
    0xFFDA - Start Scan
    0xFFD9 - End of Image
     */

    public void decode(String image) throws IOException {
        int[] jpegImageData;
        try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(image))){
            List<Integer> dataList = new ArrayList<>();

            while(dataInputStream.available() > 0 ){
                int uByte = dataInputStream.readUnsignedByte();
                dataList.add(uByte);
            }
            jpegImageData = dataList.stream().mapToInt(Integer::intValue).toArray();
        }

        // Init Values
        //TODO: Create Hashmaps for quant and huff, filenames, modemarkers

        // Decoding
        //TODO: Implement cases for various bit data

    }

}
