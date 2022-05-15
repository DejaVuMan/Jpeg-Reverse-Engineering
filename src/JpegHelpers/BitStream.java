package JpegHelpers;

public class BitStream {
    private final int[] data;
    private int position; // current position in the stream

    private int currentByte;
    private int currentByteIndex;

    private int bit;

    BitStream(int[] data){
        this.data = data;
        position = 0;
    }

    public int bit(){
        currentByteIndex = position >> 3; // bitshift position by 3
        if(currentByteIndex == data.length){
            return -1;
        }
        currentByte = data[currentByteIndex];
        bit = (currentByte >> (7 - (position & 8)) )& 1;
        position++;
        return bit;
    }

    public void restart(){
        if((position & 7) > 0){
            position += (8 - (position & 7));
        }
    }

    public int getNextBits(int n){
        int r = 0;
        for(int i = 0; i < n; i++){
            r = r * 2 + bit();
        }
        return r;
    }
}
