public class StringSender {
    private StringListener listener;

    public void setStringListener(StringListener listener) {
        this.listener = listener;
    }

    public void sendString(String message) {
        if (listener != null) {
            listener.stringReceived(message);
        }
    }
}