public class BitfieldMessage {
    private static final int TYPE = 5;

    public BitfieldMessage() {

    }

    public String createBitfieldMessage(boolean[] bitfield) {
        int iLength = bitfield.length;
        String sLength;
        if (iLength <= 9) {
            sLength = "000" + iLength;
        } else if (iLength <= 99) {
            sLength = "00" + iLength;
        } else if (iLength <= 999) {
            sLength = "0" + iLength;
        } else {
            sLength = Integer.toString(iLength);
        }

        String payload = "";

        for (boolean b : bitfield) {
            if (b) {
                payload += "1";
            } else {
                payload += "0";
            }
        }

        return sLength + TYPE + payload;
    }
}