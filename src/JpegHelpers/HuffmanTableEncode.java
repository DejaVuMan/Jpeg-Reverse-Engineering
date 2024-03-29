package JpegHelpers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.LongBuffer;
import java.util.Vector;

public class HuffmanTableEncode { // based on huffman table implementation from jpeg-6a
    int bufferPutBits, bufferInsertionBuffer;
    public int imageHeight, imageWidth;

    public int[][] dcMatrix0;
    public int[][] dcMatrix1;
    public int[][] acMatrix0;
    public int[][] acMatrix1;

    public Object[] dcMatrix;
    public Object[] acMatrix;

    public int code;
    public int dcTableCount;
    public int acTableCount;

    public int[] bitsDcLuminance = {0x00, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
    public int[] valDcLuminance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    public int[] bitsDcChrominance = {0x01, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    public int[] valDcChrominance = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    public int[] bitsAcLuminance = {0x10, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d};
    public int[] valAcLuminance = {0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41,
            0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42,
            0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17,
            0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
            0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77,
            0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
            0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2,
            0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8,
            0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4,
            0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9,
            0xfa};

    public int[] bitsAcChrominance = {0x11, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77};
    public int[] valAcChrominance = {0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06,
            0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1,
            0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34,
            0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37,
            0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56,
            0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
            0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92,
            0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8,
            0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
            0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2,
            0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
            0xf9, 0xfa};

    public Vector bits;
    public Vector val;

    public static int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
            63, };

    public HuffmanTableEncode(int width, int height){ // constructor
        bits = new Vector();
        bits.addElement(bitsDcLuminance); // unchecked call - probably shouldnt be done
        bits.addElement(bitsAcLuminance);
        bits.addElement(bitsDcChrominance);
        bits.addElement(bitsAcChrominance);

        val = new Vector();
        val.addElement(valDcLuminance);
        val.addElement(valAcLuminance);
        val.addElement(valDcChrominance);
        val.addElement(valAcChrominance);

        InitializeHuffmanEncoder();

        imageHeight = height;
        imageWidth = width;
    }

    public void BlockEncoder(BufferedOutputStream output, int[] zigzagTable, int precision, int dcCode, int acCode){
        int temp0, temp1, bits, i, j, k;

        dcTableCount = 2;
        acTableCount = 2;

        // DC
        temp0 = temp1 = zigzagTable[0] - precision;

        if(temp0 < 0){
            temp0 = -temp0;
            temp1--;
        }

        bits = 0;
        while(temp0 != 0){
            bits++;
            temp0 >>= 1; // >>= same as temp0 = temp0 >> 1
        }

        IntBuffer(output, ((int[][]) dcMatrix[dcCode])[bits][0], ((int[][]) dcMatrix[dcCode])[bits][1]);
        if(bits != 0){
            IntBuffer(output, temp1, bits);
        }
        // AC
        j = 0;
        for(i = 1; i < 64; i++){ // Sequence is always 1 DC value followed by 63 AC values
            temp0 = zigzagTable[jpegNaturalOrder[i]];
            if(temp0 == 0)
            {
                j++;
            } else {
                while(j > 15){
                    IntBuffer(output, ((int[][]) acMatrix[acCode])[0xF0][0], ((int[][]) acMatrix[acCode])[0xF0][1]);
                    j -= 16;
                }
                temp1 = temp0;
                if(temp0 < 0){
                    temp0 = -temp0;
                    temp1--;
                }
                bits = 1;
                while((temp0 >>= 1) != 0){
                    bits++;
                }
                k = (j << 4) + bits;
                IntBuffer(output, ((int[][]) acMatrix[acCode])[k][0], ((int[][]) acMatrix[acCode])[k][1]);
                IntBuffer(output, temp1, bits);
                j = 0;
            }
        }
        if(j > 0){
            IntBuffer(output, ((int[][]) acMatrix[acCode])[0][0], ((int[][]) acMatrix[acCode])[0][1]);
        }
    }

    void IntBuffer(BufferedOutputStream output, int code, int size){ // 32 bits used to write huffman bits to output
        int putBuffer = code;
        int putBits = bufferPutBits;

        putBuffer &= (1 << size) - 1; // &= same as putBuffer = putBuffer & ((1 << size) - 1)
        putBits += size;
        putBuffer <<= 24 - putBits;
        putBuffer |= bufferInsertionBuffer;
        //IOWriter(putBuffer, putBits, output);
        while(putBits >= 8){
            int c = ((putBuffer >> 16) & 0xFF);
            try{
                output.write(c);
            } catch(IOException e){
                System.out.println("Error writing to file");
                System.out.println(e.getMessage());
            }
            if(c == 0xFF){
                try {
                    output.write(0);
                } catch (IOException e) {
                    System.out.println("Error writing to file");
                    System.out.println(e.getMessage());
                }
            }
            putBuffer <<= 8;
            putBits -= 8;
        }
        bufferInsertionBuffer = putBuffer;
        bufferPutBits = putBits;
    }

    void FlushBuffer(BufferedOutputStream output){
        int putBuffer = bufferInsertionBuffer;
        int putBits = bufferPutBits;
        //IOWriter(putBuffer, putBits, output);

        while(putBits >= 8){
            int c = ((putBuffer >> 16) & 0xFF);
            try{
                output.write(c);
            } catch(IOException e){
                System.out.println("Error writing to file");
                System.out.println(e.getMessage());
            }
            if(c == 0xFF){
                try {
                    output.write(0);
                } catch (IOException e) {
                    System.out.println("Error writing to file");
                    System.out.println(e.getMessage());
                }
            }
            putBuffer <<= 8;
            putBits -= 8;
        }

        if(putBits > 0){
            int c = (putBuffer >> 16) & 0xFF;
            try {
                output.write(c);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    void IOWriter(int putBuffer, int putBits, BufferedOutputStream output) {
        while(putBits >= 8){
            int c = (putBuffer >> 16) & 0xFF;
            try {
                output.write(c);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            if(c == 0xFF){
                try {
                    output.write(0);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
            putBuffer <<= 8;
            putBits -= 8;
        }
    }

    public void InitializeHuffmanEncoder(){
        dcMatrix0 = new int[12][2];
        dcMatrix1 = new int[12][2];
        acMatrix0 = new int[255][2];
        acMatrix1 = new int[255][2];
        dcMatrix = new Object[2];
        acMatrix = new Object[2];

        int position, l, i, lastPosition, si, code;
        int[] huffmanSize = new int[257];
        int[] huffmanCode = new int[257];

        // As described in the work done by James R. Weeks and BioElectroMech, DC values for chrominance are
        // [][0] for the code itself and [][1] for the number of the bit

        position = 0;
        for(l = 1; l <= 16; l++){
            for(i = 1; i <= bitsDcChrominance[l]; i++){
                huffmanSize[position++] = l;
            }
        }
        huffmanSize[position] = 0;
        lastPosition = position;

        code = 0;
        si = huffmanSize[0];
        position = 0;
        while(huffmanSize[position] != 0){
            while(huffmanSize[position] == si){
                huffmanCode[position++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for(position = 0; position < lastPosition; position++){
            dcMatrix1[valDcChrominance[position]][0] = huffmanCode[position];
            dcMatrix1[valDcChrominance[position]][1] = huffmanSize[position];
        }

        // like above, AC values for chrominance are
        // [][0] for the code itself and [][1] for the number of the bit

        position = 0;
        for(l = 1; l <= 16; l++){
            for(i = 1; i <= bitsAcChrominance[l]; i++){
                huffmanSize[position++] = l;
            }
        }
        huffmanSize[position] = 0;
        lastPosition = position;

        code = 0;
        si = huffmanSize[0];
        position = 0;
        while(huffmanSize[position] != 0){
            while(huffmanSize[position] == si){
                huffmanCode[position++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for(position = 0; position < lastPosition; position++){
            acMatrix1[valAcChrominance[position]][0] = huffmanCode[position];
            acMatrix1[valAcChrominance[position]][1] = huffmanSize[position];
        }

        // Init of DC Values for luminance

        position = 0;
        for(l = 1; l <= 16; l++){
            for(i = 1; i <= bitsDcLuminance[l]; i++){
                huffmanSize[position++] = l;
            }
        }
        huffmanSize[position] = 0;
        lastPosition = position;

        code = 0;
        si = huffmanSize[0];
        position = 0;
        while(huffmanSize[position] != 0){
            while(huffmanSize[position] == si){
                huffmanCode[position++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for(position = 0; position < lastPosition; position++){
            dcMatrix0[valDcLuminance[position]][0] = huffmanCode[position];
            dcMatrix0[valDcLuminance[position]][1] = huffmanSize[position];
        }

        // Init of AC Values for luminance

        position = 0;
        for(l = 1; l <= 16; l++){
            for(i = 1; i <= bitsAcLuminance[l]; i++){
                huffmanSize[position++] = l;
            }
        }
        huffmanSize[position] = 0;
        lastPosition = position;

        code = 0;
        si = huffmanSize[0];
        position = 0;
        while(huffmanSize[position] != 0){
            while(huffmanSize[position] == si){
                huffmanCode[position++] = code;
                code++;
            }
            code <<= 1;
            si++;
        }

        for(int q = 0; q < lastPosition; q++){
            acMatrix0[valAcLuminance[q]][0] = huffmanCode[q];
            acMatrix0[valAcLuminance[q]][1] = huffmanSize[q];
        }

        dcMatrix[0] = dcMatrix0;
        dcMatrix[1] = dcMatrix1;
        acMatrix[0] = acMatrix0;
        acMatrix[1] = acMatrix1;
    }
}
