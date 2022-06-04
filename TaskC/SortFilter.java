import java.util.ArrayList;
import java.util.Comparator;

public class SortFilter extends FilterFramework {
    public SortFilter(int inPorts) {
        super(inPorts);
    }

    int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
    int IdLength = 4;                // This is the length of IDs in the byte stream

    byte databyte = 0;                // This is the data byte read from the stream
    int bytesread = 0;                // This is the number of bytes read from the stream

    long measurement;                // This is the word used to store all measurements - conversions are illustrated.
    int id;                            // This is the measurement id
    int i;                            // This is a loop counter

    /*************************************************************
     *	Comparator to compare two ByteInputs
     *************************************************************/
    class byteInputComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            ByteInput s1 = (ByteInput) o1;
            ByteInput s2 = (ByteInput) o2;
            return (s1.getTimeFromLong().compareTo(s2.getTimeFromLong()));
        }
    }

    /*************************************************************
     *	Queue for Inputs
     *************************************************************/
    ArrayList<ByteInput> queue = new ArrayList<>();

    public void run() {


        ByteInput input = new ByteInput(this);


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
                    input.setPressure(measurement);
                } else if (id == 4) {
                    input.setTemperature(measurement);
                } else if (id == 5) {
                    input.setAttitude(measurement);
                }

                /*************************************************************
                 *	Whole object has been read
                 *************************************************************/
                if (id == 5) {
                    /*************************************************************
                     *	Minimum queue size is 10. If less than 10 -add to queue
                     *************************************************************/
                    if (queue.size() < 10) {
                        ///queue has less than 2 elements
                        ByteInput inputForArray = new ByteInput(input.getTime(), input.getVelocity(), input.getTemperature(), input.getAltitude(), input.getPressure(), input.getAttitude(), this);
                        queue.add(inputForArray);
                    } else {
                        /*************************************************************
                         *	Get smallest of the queue and pass it on to the stream
                         *************************************************************/
                        ByteInput inputForArray = new ByteInput(input.getTime(), input.getVelocity(), input.getTemperature(), input.getAltitude(), input.getPressure(), input.getAttitude(), this);
                        queue.add(inputForArray);
                        queue.sort(new byteInputComparator());
                        ByteInput passOn = queue.get(0);
                        queue.remove(0);
                        passOn.putIntoStream();

                    }

                }


            } // try

            catch (EndOfStreamException e) {
                /*************************************************************
                 *	End of file - dequeue all
                 *************************************************************/
                queue.sort(new byteInputComparator());
                for (ByteInput passOn : queue) {
                    passOn.putIntoStream();
                }
                ClosePorts();
                ///   System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
                break;

            } // catch

        } // while

    } // run
}
