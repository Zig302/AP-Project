package graph;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        // General constructor for byte array input
        double asDouble1;
        this.data = data;
        this.asText = new String(data);
        try { // Attempt to parse the byte array as a double
            asDouble1 = Double.parseDouble(this.asText);
        } catch (NumberFormatException e) {
            asDouble1 = Double.NaN;
        }
        this.asDouble = asDouble1;
        this.date = new Date();
    }

    public Message(String text) {
        // Constructor for String input
        this(text.getBytes());
    }

    public Message(double text) {
        // Constructor for double input
        this(Double.toString(text).getBytes());
    }

}
