import java.util.Calendar;

public class ByteInput {
    private Long time;
    private Long velocity;
    private Long temperature;
    private Long altitude;
    private Long pressure;
    private Long attitude;
    private FilterFramework filterFramework;
    int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    int IdLength = 4;                // This is the length of IDs in the byte stream
    long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    int id;                            // This is the measurement id

    public Calendar getTimeFromLong() {
        Calendar timeStamp = Calendar.getInstance();
        timeStamp.setTimeInMillis(time);
        return timeStamp;

    }

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

    public Long getPressure() {
        return pressure;
    }

    public void setPressure(Long pressure) {
        this.pressure = pressure;
    }

    public Long getAttitude() {
        return attitude;
    }

    public void setAttitude(Long attitude) {
        this.attitude = attitude;
    }

    @Override
    public String toString() {
        return "ByteInput{" +
                "time=" + time +
                ", velocity=" + velocity +
                ", temperature=" + temperature +
                ", altitude=" + altitude +
                ", pressure=" + pressure +
                '}';
    }

    public ByteInput(Long time, Long velocity, Long temperature, Long altitude, Long pressure, Long attitude, FilterFramework filterFramework) {
        this.time = time;
        this.velocity = velocity;
        this.temperature = temperature;
        this.altitude = altitude;
        this.pressure = pressure;
        this.attitude = attitude;
        this.filterFramework = filterFramework;
    }

    public ByteInput(FilterFramework filterFramework) {
        this.filterFramework = filterFramework;
    }


    public void putIntoStream() {
        //pass one object into the stream
        for (int i = 0; i < 6; i++) {


            if (i == 0) {
                measurement = time;
            } else if (i == 1) {
                measurement = velocity;
            } else if (i == 2) {
                measurement = altitude;
            } else if (i == 3) {
                measurement = pressure;
            } else if (i == 4) {
                measurement = temperature;
            } else {
                measurement = attitude;
            }

            Helper.writeLongIntoStream(IdLength, i, MeasurementLength, measurement, filterFramework);

        }

    }
}
