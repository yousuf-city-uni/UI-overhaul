// File: Venue.java (Updated Class)
public class Venue {
    private int venueID;
    private String name;
    private String type;
    private int capacity;
    private String seatingConfiguration;
    // New fields for numeric rates:
    private double hourlyRate;
    private double allDayRate;

    // Getters and Setters
    public int getVenueID() {
        return venueID;
    }
    public void setVenueID(int venueID) {
        this.venueID = venueID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSeatingConfiguration() {
        return seatingConfiguration;
    }
    public void setSeatingConfiguration(String seatingConfiguration) {
        this.seatingConfiguration = seatingConfiguration;
    }

    // New getters and setters for rates
    public double getHourlyRate() {
        return hourlyRate;
    }
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getAllDayRate() {
        return allDayRate;
    }
    public void setAllDayRate(double allDayRate) {
        this.allDayRate = allDayRate;
    }
}

