import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/*************************************************************
 *	Class to encapsulate the input from stream object
 *************************************************************/

public class Input {

    ///variables for input description
    private Long time;
    private Long velocity;
    private Long temperature;
    private Long altitude;
    private Double pressure;
    private Boolean asterisk;

    ///variables for sending the data to the next filter
    private FilterFramework filterFramework;
    private  Calendar TimeStamp = Calendar.getInstance();
    private SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
    private int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    private int IdLength = 4;                // This is the length of IDs in the byte stream
    private long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    private int id;                            // This is the measurement id

    ///getters and setters

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getVelocity() {
        return velocity;
    }

    public void setVelocity(Long velocity) {
        this.velocity = velocity;
    }

    public Long getTemperature() {
        return temperature;
    }

    public void setTemperature(Long temperature) {
        this.temperature = temperature;
    }

    public Long getAltitude() {
        return altitude;
    }

    public void setAltitude(Long altitude) {
        this.altitude = altitude;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Boolean getAsterisk() {
        return asterisk;
    }

    public void setAsterisk(Boolean asterisk) {
        this.asterisk = asterisk;
    }

    public FilterFramework getFilterFramework() {
        return filterFramework;
    }

    public void setFilterFramework(FilterFramework filterFramework) {
        this.filterFramework = filterFramework;
    }


    /*************************************************************
     *	Create wildpoints file and write the points into it
     *************************************************************/
    public void writeIntoFile()
            throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter("WildPoints.dat", true));
        TimeStamp.setTimeInMillis(time);
        String str = TimeStampFormat.format(TimeStamp.getTime()) + " ";
        writer.write(str);
        String doubleAsString = String.valueOf(pressure);
        int indexOfDecimal = doubleAsString.indexOf(".");
        str = doubleAsString.substring(0, indexOfDecimal) + ":"
                + doubleAsString.substring(indexOfDecimal + 1, indexOfDecimal + 1 + 5) + "\n";
        writer.write(str);
        writer.close();
    }


    @Override
    public String toString() {
        return "Input{" +
                "time=" + time +
                ", velocity=" + velocity +
                ", temperature=" + temperature +
                ", altitude=" + altitude +
                ", pressure=" + pressure +
                ", asterix=" + asterisk +
                '}';
    }

    public Input(Long time, Long velocity, Long temperature, Long altitude, Double pressure, Boolean asterix, FilterFramework filterFramework) {
        this.time = time;
        this.velocity = velocity;
        this.temperature = temperature;
        this.altitude = altitude;
        this.pressure = pressure;
        this.asterisk = asterix;
        this.filterFramework = filterFramework;
    }

    public Input() {
        asterisk = false;
    }
    public Input( FilterFramework filterFramework) {
        this.filterFramework = filterFramework;
        asterisk = false;
    }

    public void putIntoStream() {

        /*************************************************************
         *	Pass the whole object into the stream
         *************************************************************/
        for (int i = 0; i < 5; i++) {

            if (i == 0) {
                measurement = time;
            } else if (i == 1) {
                measurement = velocity;
            } else if (i == 2) {
                measurement = altitude;
            } else if (i == 3) {
                byte[] pressureArray = ByteBuffer.allocate(MeasurementLength).putDouble(pressure).array();
                measurement = ByteBuffer.wrap(pressureArray).getLong();
            } else {
                measurement = temperature;
            }

            Helper.writeLongIntoStream(IdLength,i,MeasurementLength,measurement,filterFramework);

        }
        /*************************************************************
         *	Additionally pass the asterisk as a 6th argument
         *************************************************************/
        Helper.writeDoubleIntoStream(IdLength,6,MeasurementLength,asterisk ? 1.5 : 0.5,filterFramework);

    }
}
