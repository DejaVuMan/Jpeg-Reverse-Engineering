package JpegHelpers;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JpegEncoder {

    void Encode(String image) throws IOException{
        int[] rawImageData;
        try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(image))){
            List<Integer> dataList = new ArrayList<>();

            while(dataInputStream.available() > 0)
            {
                int uByte = dataInputStream.readUnsignedByte();
                dataList.add(uByte);
            }
            rawImageData = dataList.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
