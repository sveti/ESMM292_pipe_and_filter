import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AttitudeFilter extends FilterFramework {
    int maxPressurePoint;
    int maxAttitudePoint;
    int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    int IdLength = 4;                // This is the length of IDs in the byte stream

    byte databyte = 0;                // This is the data byte read from the stream
    int bytesread = 0;                // This is the number of bytes read from the stream

    long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    int id;                            // This is the measurement id
    int i;                            // This is a loop counter


    int byteswritten = 0;                // Number of bytes written to the stream.

    Double lastGoodPressure = null;
    Double currentGoodPressure = null;
    Double lastGoodAttitude = null;
    Double currentGoodAttitute = null;
    Calendar TimeStamp = Calendar.getInstance();
    SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");

    public AttitudeFilter(int inPorts, int maxPressurePoint, int maxAttitudePoint) {
        super(inPorts);
        this.maxPressurePoint = maxPressurePoint;
        this.maxAttitudePoint = maxAttitudePoint;

    }


    ArrayList<AttitudeFilterInput> wildPointInputs = new ArrayList<>();

    public void run() {


        AttitudeFilterInput input = new AttitudeFilterInput(this);


        // Next we write a message to the terminal to let the world know we are alive...

        ///  System.out.print( "\n" + this.getName() + "::Temperature Reading ");

        while (true) {
            /*************************************************************
             *	Here we read a byte and write a byte
             *************************************************************/

            try {


                id = 0;

                for (i = 0; i < IdLength; i++) {
                    databyte = ReadFilterInputPort();    // This is where we read the byte from the stream...

                    id = id | (databyte & 0xFF);        // We append the byte on to ID...

                    if (i != IdLength - 1)                // If this is not the last byte, then slide the
                    {                                    // previously appended byte to the left by one byte
                        id = id << 8;                    // to make room for the next byte we append to the ID

                    } // if

                    bytesread++;                        // Increment the byte count

                } // for
                ///id -> 4
                ///

                measurement = 0;

                for (i = 0; i < MeasurementLength; i++) {
                    databyte = ReadFilterInputPort();
                    measurement = measurement | (databyte & 0xFF);    // We append the byte on to measurement...

                    if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
                    {                                                // previously appended byte to the left by one byte
                        measurement = measurement << 8;                // to make room for the next byte we append to the
                        // measurement
                    } // if

                    bytesread++;                                    // Increment the byte count

                }


                if (id == 0) {
                    input.setTime(measurement);
                } else if (id == 1) {
                    input.setVelocity(measurement);
                } else if (id == 2) {
                    input.setAltitude(measurement);
                } else if (id == 3) {
                    input.setPressure(Double.longBitsToDouble(measurement));
                } else if (id == 4) {
                    input.setTemperature(measurement);
                } else if (id == 5) {
                    input.setAttitude(Double.longBitsToDouble(measurement));
                } else if (id == 6) {
                    input.setAsterisk(Double.longBitsToDouble(measurement));
                }

                if (id == 6) {
                    //whole input object
                    if (input.getPressure() > maxPressurePoint && input.getAttitude() > maxAttitudePoint) {
                        ///wildcard add to list
                        AttitudeFilterInput inputForArray = new AttitudeFilterInput(input.getTime(),
                                input.getVelocity(), input.getTemperature(), input.getAltitude(), input.getPressure(), input.getAttitude(), input.getAsterisk(),this);
                        wildPointInputs.add(inputForArray);

                    } else {
                        ///it's okay
                        lastGoodPressure = currentGoodPressure;
                        currentGoodPressure = input.getPressure();
                        lastGoodAttitude = currentGoodAttitute;
                        currentGoodAttitute = input.getAttitude();


                        for (AttitudeFilterInput wildPoint : wildPointInputs) {

                            ///beginning or middle
                            ///end -> to be handled in catch
                            wildPoint.writeIntoFile();
                            wildPoint.setPressure(lastGoodPressure == null ? currentGoodPressure : (lastGoodPressure + currentGoodPressure) / 2.0);
                            wildPoint.setAttitude(lastGoodAttitude == null ? currentGoodAttitute : (lastGoodAttitude + currentGoodAttitute) / 2.0);

                            wildPoint.setAsterisk(1.5);
                            wildPoint.putIntoStream();
                        }
                        wildPointInputs.clear();
                        input.putIntoStream();
                    }
//                    input.putIntoStream();

                }


            } // try

            catch (EndOfStreamException | IOException e) {
                for (AttitudeFilterInput wildPoint : wildPointInputs) {
                    ///beginning or middle
                    ///end -> to be handled in catch
                    try {
                        wildPoint.writeIntoFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    wildPoint.setPressure(lastGoodPressure == null ? currentGoodPressure : (lastGoodPressure + currentGoodPressure) / 2.0);
                    wildPoint.setAttitude(lastGoodAttitude == null ? currentGoodAttitute : (lastGoodAttitude + currentGoodAttitute) / 2.0);

                    wildPoint.setAsterisk(1.5);
                    wildPoint.putIntoStream();
                }
                ClosePorts();
                ///   System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
                break;

            } // catch

        } // while

    } // run

}
