public class MessageUtil {
    public static int getMessageType(byte[] message) {
        return Integer.parseInt(FileUtil.convertByteToString(message).substring(4, 5));
    }

    public static String getPayload(byte[] byteMessage) {
        return FileUtil.convertByteToString(byteMessage).substring(5);
    }
}