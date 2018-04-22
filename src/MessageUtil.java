public class MessageUtil {
    public static int getMessageType(String message) {
        return Integer.parseInt(message.substring(4, 5));
    }

    public static String getPayload(String message) {
        return message.substring(5);
    }
}