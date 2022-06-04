import java.io.IOException;
import java.util.ArrayList;

public class PressureFilter extends FilterFramework {

    int maxWildPoint;
    int minWildPoint;
    int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    int IdLength = 4;                // This is the length of IDs in the byte stream

    byte databyte = 0;                // This is the data byte read from the stream
    int bytesread = 0;                // This is the number of bytes read from the stream

    long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    int id;                            // This is the measurement id
    int i;                            // This is a loop counter

    Double lastGoodPressure = null;
    Double currentGoodPressure = null;

    /***********************************************************************************
     *	Constuctor with parametrized filter values
     ***********************************************************************************/
    public PressureFilter(int inPorts, int minWildPoint, int maxWildPoint) {
        super(inPorts);
        this.minWildPoint = minWildPoint;
        this.maxWildPoint = maxWildPoint;

    }

    ArrayList<Input> wildPointInputs = new ArrayList<>();

    public void run() {
        Input input = new Input(this);


        // Next we write a message to the terminal to let the world know we are alive...

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
                    input.setAttitude(measurement);
                }


                /*************************************************************
                 *	The whole object has been read
                 *************************************************************/
                if (id == 5) {
                    /*************************************************************
                     *	Check if the pressure data falls into wild point category
                     *************************************************************/
                    if (input.getPressure() < minWildPoint || input.getPressure() > maxWildPoint) {
                        /*************************************************************
                         *	It is a wild point, add it to the designated array for wild points and await further "good"point
                         *************************************************************/
                        Input inputForArray = new Input(input.getTime(), input.getVelocity(), input.getTemperature(), input.getAltitude(), input.getPressure(), input.getAttitude(), input.getAsterisk(), this);
                        wildPointInputs.add(inputForArray);

                    } else {
                        /*************************************************************
                         *	It is a wild point, add it to the designated array for wild points and await further "good"point
                         *************************************************************/
                        lastGoodPressure = currentGoodPressure;
                        currentGoodPressure = input.getPressure();

                        for (Input wildPoint : wildPointInputs) {

                            /*************************************************************
                             *	Add wild point before transformation into the WildPointPressure.dat file
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
                for (Input wildPoint : wildPointInputs) {
                    /*************************************************************
                     *	End of stream, check if there are any wild points left unpassed
                     *************************************************************/
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
                ///   System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
                break;

            } // catch

        } // while

    } // run
}
