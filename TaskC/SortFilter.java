import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class SortFilter extends FilterFramework{
    public SortFilter(int inPorts) {
        super(inPorts);
    }
    int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
    int IdLength = 4;				// This is the length of IDs in the byte stream

    byte databyte = 0;				// This is the data byte read from the stream
    int bytesread = 0;				// This is the number of bytes read from the stream

    long measurement;				// This is the word used to store all measurements - conversions are illustrated.
    int id;							// This is the measurement id
    int i;							// This is a loop counter


    int byteswritten = 0;				// Number of bytes written to the stream.

    Double lastGoodPressure = null;
    Double currentGoodPressure = null;
    Calendar TimeStamp = Calendar.getInstance();
    SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");

    public class Input{

        Long time;
        Long velocity;
        Long temperature;
        Long altitude;
        Long pressure;
        Long attitude;

        public Calendar getTimeFromLong(){
            Calendar timeStamp = Calendar.getInstance();
            timeStamp.setTimeInMillis(time);
            return  timeStamp;

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
            return "Input{" +
                    "time=" + time +
                    ", velocity=" + velocity +
                    ", temperature=" + temperature +
                    ", altitude=" + altitude +
                    ", pressure=" + pressure +
                    '}';
        }

        public Input(Long time, Long velocity, Long temperature, Long altitude, Long pressure, Long attitude) {
            this.time = time;
            this.velocity = velocity;
            this.temperature = temperature;
            this.altitude = altitude;
            this.pressure = pressure;
            this.attitude = attitude;
        }

        public Input() {

        }



        public void putIntoStream(){
            //pass one object into the stream
            for (i = 0; i < 6; i++) {

                byte[] idForBuffer = ByteBuffer.allocate(IdLength).putInt(i).array();
                for (byte b : idForBuffer) {
                    WriteFilterOutputPort(b);
                    byteswritten++;
                }

                switch (i) {
                    case 0:
                        measurement = time;
                        break;
                    case 1:
                        measurement = velocity;
                        break;
                    case 2:
                        measurement = altitude;
                        break;
                    case 3: {
                        measurement = pressure;
                        break;
                    }
                    case 4:
                        measurement = temperature;
                        break;
                    case 5:
                        measurement = attitude;
                        break;
                }

                byte[] result = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
                for (byte b : result) {
                    WriteFilterOutputPort(b);
                    byteswritten++;
                }


            }

        }
    }

    class InputComaparator implements Comparator {
        public int compare(Object o1,Object o2){
            Input s1=(Input)o1;
            Input s2=(Input)o2;
            return (s1.getTimeFromLong().compareTo(s2.getTimeFromLong()));
        }
    }

    ArrayList<SortFilter.Input> queue = new ArrayList<>();

    public void run()
    {


        SortFilter.Input input = new SortFilter.Input();


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


                switch (id) {
                    case 0 : input.setTime(measurement); break;
                    case 1 : input.setVelocity(measurement);break;
                    case 2 : input.setAltitude(measurement);break;
                    case 3 : input.setPressure(measurement);break;
                    case 4 : input.setTemperature(measurement);break;
                    case 5 : input.setAttitude(measurement);break;
                }

                if(id == 5) {
                    ///object fully read
                    if(queue.size() < 10){
                        ///queue has less than 2 elements
                        SortFilter.Input inputForArray = new SortFilter.Input(input.getTime(), input.getVelocity(),input.getTemperature(),input.getAltitude(),input.getPressure(),input.getAttitude());
                        queue.add(inputForArray);
                    }
                    else{
                        SortFilter.Input inputForArray = new SortFilter.Input(input.getTime(), input.getVelocity(),input.getTemperature(),input.getAltitude(),input.getPressure(),input.getAttitude());
                        queue.add(inputForArray);
                        queue.sort(new InputComaparator());
                        Input passOn = queue.get(0);
                        queue.remove(0);
                        passOn.putIntoStream();

                    }

                }




            } // try

            catch (EndOfStreamException e)
            {
                queue.sort(new InputComaparator());
                for(Input passOn : queue){
                    passOn.putIntoStream();
                }
                ClosePorts();
                ///   System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
                break;

            } // catch

        } // while

    } // run
}
