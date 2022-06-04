import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PressureFilter extends FilterFramework {

    int maxWildPoint = 80;
    int minWildPoint = 50;
    int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    int IdLength = 4;                // This is the length of IDs in the byte stream

    byte databyte = 0;                // This is the data byte read from the stream
    int bytesread = 0;                // This is the number of bytes read from the stream

    long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    int id;                            // This is the measurement id
    int i;                            // This is a loop counter

    Double lastGoodPressure = null;
    Double currentGoodPressure = null;

    ArrayList<Input> wildPointInputs = new ArrayList<>();

    public void run() {


        Input input = new Input(this);


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
                }


                /*************************************************************
                 *	The whole object has been read
                 *************************************************************/
                if (id == 4) {

                    /*************************************************************
                     *	Check if the pressure data falls into wild point category
                     *************************************************************/
                    if (input.getPressure() < minWildPoint || input.getPressure() > maxWildPoint) {

                        /*************************************************************
                         *	It is a wild point, add it to the designated array for wild points and await further "good"point
                         *************************************************************/
                        Input inputForArray = new Input(input.getTime(), input.getVelocity(), input.getTemperature(), input.getAltitude(), input.getPressure(), input.getAsterisk(), this);
                        wildPointInputs.add(inputForArray);

                    } else {

                        /*************************************************************
                         *	The passed input is not a wild point update the markers pointing to previous acceptable pressures
                         *************************************************************/
                        lastGoodPressure = currentGoodPressure;
                        currentGoodPressure = input.getPressure();


                        /*************************************************************
                         *	Empty the list of wild points by averaging the pressure
                         *************************************************************/

                        for (Input wildPoint : wildPointInputs) {

                            /*************************************************************
                             *	Add wild point before transformation into the wildpoints.dat file
                             *************************************************************/
                            wildPoint.writeIntoFile();

                            wildPoint.setPressure(lastGoodPressure == null ? currentGoodPressure : (lastGoodPressure + currentGoodPressure) / 2.0);
                            wildPoint.setAsterisk(true);
                            wildPoint.putIntoStream();
                        }
                        wildPointInputs.clear();

                        /*************************************************************
                         *	The list of previous wild points has been emptied, pass the current input on
                         *************************************************************/
                        input.setAsterisk(false);
                        input.putIntoStream();
                    }

                }


            } // try

            catch (EndOfStreamException | IOException e) {


                /*************************************************************
                 *	End of stream, check if there are any wild points left unpassed
                 *************************************************************/

                for (Input wildPoint : wildPointInputs) {
                    try {
                        wildPoint.writeIntoFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    wildPoint.setPressure(currentGoodPressure);
                    wildPoint.setAsterisk(true);
                    wildPoint.putIntoStream();
                }
                ClosePorts();
                break;

            } // catch

        } // while

    } // run
}
