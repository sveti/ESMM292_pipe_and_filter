public class BaseInput {

    Long time;
    Double temperature;
    Double altitude;
    Double pressure;

    public BaseInput() {
    }

    public BaseInput(Long time, Double temperature, Double altitude, Double pressure) {
        this.time = time;
        this.temperature = temperature;
        this.altitude = altitude;
        this.pressure = pressure;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return "BaseInput{" +
                "time=" + time +
                ", temperature=" + temperature +
                ", altitude=" + altitude +
                ", pressure=" + pressure +
                '}';
    }
}
