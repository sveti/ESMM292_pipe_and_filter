import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PressureFilter extends FilterFramework{

    int maxWildPoint = 80;
    int minWildPoint = 50;
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
        Double pressure;
        Boolean asterisk;

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

        public void writeIntoFile()
                throws IOException
        {


            BufferedWriter writer = new BufferedWriter(new FileWriter("WildPoints.dat", true));
            TimeStamp.setTimeInMillis(time);
            String str = TimeStampFormat.format(TimeStamp.getTime()) + " ";
            writer.write(str);
            String doubleAsString = String.valueOf(pressure);
            int indexOfDecimal = doubleAsString.indexOf(".");
            str = doubleAsString.substring(0, indexOfDecimal) + ":"
                    + doubleAsString.substring(indexOfDecimal+1,indexOfDecimal+1+5) + "\n";
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

        public Input(Long time, Long velocity, Long temperature, Long altitude, Double pressure, Boolean asterix) {
            this.time = time;
            this.velocity = velocity;
            this.temperature = temperature;
            this.altitude = altitude;
            this.pressure = pressure;
            this.asterisk = asterix;
        }

        public Input() {
            asterisk = false;
        }

        public void putIntoStream(){
            //pass one object into the stream
            for (i = 0; i < 5; i++) {

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
                        byte[] pressureArray = ByteBuffer.allocate(MeasurementLength).putDouble(pressure).array();
                        measurement = ByteBuffer.wrap(pressureArray).getLong();
                        break;
                    }
                    case 4:
                        measurement = temperature;
                        break;
                }

                byte[] result = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
                for (byte b : result) {
                    WriteFilterOutputPort(b);
                    byteswritten++;
                }


            }
            ///ID 6 -> asterix
            byte[] idForAstrerix = ByteBuffer.allocate(IdLength).putInt(6).array();
            for (byte b : idForAstrerix) {
                WriteFilterOutputPort(b);
                byteswritten++;
            }
            ///1.5 -> true
            ///0.5 -> false
            byte[] result = ByteBuffer.allocate(MeasurementLength).putDouble(asterisk ?1.5:0.5).array();
            for (byte b : result) {
                WriteFilterOutputPort(b);
                byteswritten++;
            }


        }
    }

    ArrayList<Input> wildPointInputs = new ArrayList<>();

    public void run()
    {


        Input input = new Input();


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
                    case 3 : input.setPressure(Double.longBitsToDouble(measurement));break;
                    case 4 : input.setTemperature(measurement);break;
                }

                if(id == 4) {
                    //whole input object
                    if (input.getPressure() < minWildPoint || input.getPressure() > maxWildPoint) {
                        ///wildcard add to list
                        Input inputForArray = new Input(input.getTime(), input.getVelocity(),input.getTemperature(),input.getAltitude(),input.getPressure(),input.getAsterisk());
                        wildPointInputs.add(inputForArray);

                    } else {
                        ///it's okay
                        lastGoodPressure = currentGoodPressure;
                        currentGoodPressure = input.getPressure();

                        for(Input wildPoint: wildPointInputs){

                            ///beginning or middle
                            ///end -> to be handled in catch
                            wildPoint.writeIntoFile();
                            wildPoint.setPressure(lastGoodPressure == null? currentGoodPressure : (lastGoodPressure+currentGoodPressure)/2.0);
                            wildPoint.setAsterisk(true);
                            wildPoint.putIntoStream();
                        }
                        wildPointInputs.clear();
                        input.setAsterisk(false);
                        input.putIntoStream();
                    }
//                    input.putIntoStream();

                }




            } // try

            catch (EndOfStreamException | IOException e)
            {
                for(Input wildPoint: wildPointInputs){
                    ///beginning or middle
                    ///end -> to be handled in catch
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
