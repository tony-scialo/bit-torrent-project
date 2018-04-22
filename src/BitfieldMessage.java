public class BitfieldMessage {
    private static final int TYPE = 5;

    public BitfieldMessage() {

    }

    public String createBitfieldMessage(boolean[] bitfield) {
        String length = "9999";
        String payload = "";

        for (boolean b : bitfield) {
            if (b) {
                payload += "1";
            } else {
                payload += "0";
            }
        }

        return length + TYPE + payload;
    }
}