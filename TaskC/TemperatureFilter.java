import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TemperatureFilter extends FilterFramework{

    public TemperatureFilter(int inPorts) {
        super(inPorts);
    }

    public void run()
    {

        int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
        int IdLength = 4;				// This is the length of IDs in the byte stream

        byte databyte = 0;				// This is the data byte read from the stream
        int bytesread = 0;				// This is the number of bytes read from the stream

        long measurement;				// This is the word used to store all measurements - conversions are illustrated.
        int id;							// This is the measurement id
        int i;							// This is a loop counter


        int byteswritten = 0;				// Number of bytes written to the stream.


        // Next we write a message to the terminal to let the world know we are alive...

      ///  System.out.print( "\n" + this.getName() + "::Temperature Reading ");

        while (true)
        {
            /*************************************************************
             *	Here we read a byte and write a byte
             *************************************************************/

            try
            {


                id = 0;

                for (i=0; i<IdLength; i++ )
                {
                    databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

                    id = id | (databyte & 0xFF);		// We append the byte on to ID...

                    if (i != IdLength-1)				// If this is not the last byte, then slide the
                    {									// previously appended byte to the left by one byte
                        id = id << 8;					// to make room for the next byte we append to the ID

                    } // if

                    bytesread++;						// Increment the byte count

                } // for
            ///id -> 4
                ///

                measurement = 0;

                for (i=0; i<MeasurementLength; i++ )
                {
                    databyte = ReadFilterInputPort();
                    measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

                    if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
                    {												// previously appended byte to the left by one byte
                        measurement = measurement << 8;				// to make room for the next byte we append to the
                        // measurement
                    } // if

                    bytesread++;									// Increment the byte count

                }


                if ( id == 4 )
                {
                    byte[] celsiusArray = ByteBuffer.allocate(MeasurementLength).putDouble((Double.longBitsToDouble(measurement) -32) * 5 / 9).array();
                    measurement = ByteBuffer.wrap(celsiusArray).getLong();

                } // if

                byte[] idForBuffer = ByteBuffer.allocate(IdLength).putInt(id).array();
                for (byte b : idForBuffer) {
                    WriteFilterOutputPort(b);
                    byteswritten++;
                }

                byte[] result = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
                for (byte b : result) {
                    WriteFilterOutputPort(b);
                    byteswritten++;
                }

            } // try

            catch (EndOfStreamException e)
            {
                ClosePorts();
             ///   System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
                break;

            } // catch

        } // while

    } // run
    }
