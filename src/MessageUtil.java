public class MessageUtil {
    public static int getMessageType(byte[] message) {
        return Integer.parseInt(FileUtil.convertByteToString(message).substring(4, 5));
    }

    public static String getPayload(byte[] byteMessage) {
        return FileUtil.convertByteToString(byteMessage).substring(5);
    }

    public static String convertStringToFourBytes(String s) {
        String sLength;
        if (s.length() == 1) {
            sLength = "000" + s;
        } else if (s.length() == 2) {
            sLength = "00" + s;
        } else if (s.length() == 3) {
            sLength = "0" + s;
        } else {
            sLength = s;
        }
        return sLength;
    }

    public static int getPieceIndexFromPieceMessage(byte[] byteMessage) {

        System.out.println("Payload: " + getPayload(byteMessage) + ", " + getPayload(byteMessage).substring(0, 4));

        return Integer.parseInt(getPayload(byteMessage).substring(0, 4));
    }
}