package JpegHelpers;

import java.awt.image.BufferedImage;

public class RootMeanSquareError {

    public float[][] rgbValueMerge(BufferedImage imageInput){

        int width = imageInput.getWidth();
        int height = imageInput.getHeight();

        float[][] rgbValues = new float[width][height];

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                int rawValue = imageInput.getRGB(i, j);
                rgbValues[i][j] = (float) (
                        (rawValue & 0xff) + ((rawValue & 0xff00) >> 8) + ((rawValue & 0xff0000) >> 16)
                ) / 3;
            }
        }
        return rgbValues;
    }

    public void rmseCalculate(float[][] observed, float[][] forecast){
        int runcount = 0;
        // also do this for all 3 channels, avg those values and then do sqrt
        // rmse can be summarized as sqrt(mean((forecast - observed)^2))
        double addSubResults = 0.0;
        for(int i = 0; i < observed.length; i++)
        {
            for(int j = 0; j < observed[0].length; j++)
            {
                runcount++;
                addSubResults += Math.pow((forecast[i][j] - observed[i][j]), 2);
            }
        }
        addSubResults = addSubResults / (observed.length * observed[0].length);
        System.out.println("Calculations based on " + runcount + " values.");
        System.out.println("Final RMSE is calculated to be " + Math.sqrt(addSubResults));
    }

    public void mseCalculate(float[][] observed, float[][] forecast){
        // similar to rmse, but without the sqrt
        double addSubResults = 0.0;
        int runcount = 0;
        for(int i = 0; i < observed.length; i++)
        {
            for(int j = 0; j < observed[0].length; j++)
            {
                runcount++;
                addSubResults += Math.pow((forecast[i][j] - observed[i][j]), 2);
            }
        }

        addSubResults = addSubResults / (observed.length * observed[0].length);
        System.out.println("Calculations based on " + runcount + " values.");
        System.out.println("Final MSE is calculated to be " + addSubResults);
    }
}
