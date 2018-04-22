public class BitfieldMessage {
    private static final int TYPE = 5;

    public BitfieldMessage() {

    }

    public String createBitfieldMessage(char[] bitfield) {
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
        for (char b : bitfield) {
            if (b == '1') {
                payload += "1";
            } else {
                payload += "0";
            }
        }
        return sLength + TYPE + payload;
    }

    public static char[] setUpBitfield(Common commonFile, boolean hasFile) {
        char[] bitfield = new char[commonFile.calcNumPieces()];
        char hasFileChar;
        if (hasFile) {
            hasFileChar = '1';
        } else {
            hasFileChar = '0';
        }
        for (int x = 0; x < bitfield.length; x++) {
            bitfield[x] = hasFileChar;
        }

        return bitfield;
    }

    public static char[] convertPayloadToBitfield(String payload) {
        return payload.toCharArray();
    }
}