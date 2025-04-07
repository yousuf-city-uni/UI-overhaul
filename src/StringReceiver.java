public class StringReceiver implements StringListener {
    public String getString() {
        return string;
    }

    private String string;
    @Override
    public void stringReceived(String message) {
        this.string = message;
        System.out.println("Received: " + message);
        // Do something with the string
    }
}