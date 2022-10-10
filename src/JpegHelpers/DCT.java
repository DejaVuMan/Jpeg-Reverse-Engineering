package JpegHelpers;

public class DCT {
    int blockSize = 8; // Generally, Discreet Cosine Transforms are done in blocks of 8x8 pixels
    int quality = 100; // default quality value
    public Object[] quantizationValues = new Object[2]; // hold quantization tables for luminance and chrominance
    public Object[] divisorValues = new Object[2]; // hold divisors for luminance and chrominance
    // Theoretically we could change Object[] to Number[], but then we'd also need to type cast everything to Number

    // quantumLuminance and divisorLuminance are the default quantization table for luminance
    public int[] quantumLuminance; //= new int[blockSize * blockSize];
    public double[] divisorLuminance = new double[blockSize * blockSize];

    // quantumChrominance and divisorChrominance are the default quantization table for chrominance
    public int[] quantumChrominance; //= new int[blockSize * blockSize];
    public double[] divisorChrominance = new double[blockSize * blockSize];

    public void setQuality(int num) {
        quality = num;
    }

    // Construct new DCT object - initialize our cosine transform table used for computing DCT, as well as ZigZag table
    // Much of this is the same from DCT3, but because I forgot to do documentation when I was creating it I am
    // not sure how it works now lol

    public DCT() {
        Initialize();
    }

    private void Initialize() {
        double[] scaleFactor = {
                1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100, 0.275899379
        };
        int i;
        int j;
        int index;
        int temp;

        if (quality < 1) { // quality cannot be less than 1
            quality = 1;
        }
        if (quality > 100) {
            quality = 100;
        }

        // Luminance table - redefine so we dont have to enter each index value by hand
        quantumLuminance = new int[]{
                16, 11, 10, 16, 24, 40, 51, 61,
                12, 12, 14, 19, 26, 58, 60, 55,
                14, 13, 16, 24, 40, 57, 69, 56,
                14, 17, 22, 29, 51, 87, 80, 62,
                18, 22, 37, 56, 68, 109, 103, 77,
                24, 35, 55, 64, 81, 104, 113, 92,
                49, 64, 78, 87, 103, 121, 120, 101,
                72, 92, 95, 98, 112, 100, 103, 99
        };

        // jpeg divisors method - This is the AAN method
        for (j = 0; j < 64; j++) {
            temp = (quantumLuminance[j] * quality + 50) / 100;
            if (temp <= 0)
                temp = 1;
            if (temp > 255)
                temp = 255;
            quantumLuminance[j] = temp;
        }
        index = 0;
        for (i = 0; i < blockSize; i++) {
            for (j = 0; j < blockSize; j++) {
                divisorLuminance[index] = 1.0 / (quantumLuminance[index] * scaleFactor[i] * scaleFactor[j] * 8.0);
                index++;
            }
        }

        //Chrominance table
        quantumChrominance = new int[]{
                17, 18, 24, 47, 99, 99, 99, 99,
                18, 21, 26, 66, 99, 99, 99, 99,
                24, 26, 56, 99, 99, 99, 99, 99,
                47, 66, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99
        };

        for (j = 0; j < 64; j++) {
            temp = (quantumChrominance[j] * quality + 50) / 100;
            if (temp <= 0)
                temp = 1;
            if (temp > 255)
                temp = 255;
            quantumChrominance[j] = temp;
        }
        index = 0;
        for (i = 0; i < blockSize; i++) {
            for (j = 0; j < blockSize; j++) {
                divisorChrominance[index] = 1.0 / (quantumChrominance[index] * scaleFactor[i] * scaleFactor[j] * 8.0);
                index++;
            }
        }

        // objects which hold the relevant arrays
        quantizationValues[0] = quantumLuminance;
        quantizationValues[1] = quantumChrominance;
        divisorValues[0] = divisorLuminance;
        divisorValues[1] = divisorChrominance;
    }

    // 1D array of all image data might not have been the smartest idea...
//    public byte[] forwardDCT(byte[] inputData, int width, int height, int blockCount)
//    {
//
//    }
}
