import java.nio.ByteBuffer;

public class Helper {
    /*************************************************************
     *	Helper method used to write data into output stream from type Long
     *************************************************************/

    public static void writeLongIntoStream(int IdLength, int id, int MeasurementLength, Long measurement, FilterFramework filterFramework) {

        byte[] idForBuffer = ByteBuffer.allocate(IdLength).putInt(id).array();
        for (byte b : idForBuffer) {
            filterFramework.WriteFilterOutputPort(b);
        }

        byte[] result = ByteBuffer.allocate(MeasurementLength).putLong(measurement).array();
        for (byte b : result) {
            filterFramework.WriteFilterOutputPort(b);
        }
    }

    /*************************************************************
     *	Helper method used to write data into output stream from type Double
     *************************************************************/
    public static void writeDoubleIntoStream(int IdLength, int id, int MeasurementLength, Double measurement, FilterFramework filterFramework) {


        byte[] idForBuffer = ByteBuffer.allocate(IdLength).putInt(id).array();
        for (byte b : idForBuffer) {
            filterFramework.WriteFilterOutputPort(b);
        }

        byte[] result = ByteBuffer.allocate(MeasurementLength).putDouble(measurement).array();
        for (byte b : result) {
            filterFramework.WriteFilterOutputPort(b);
        }
    }

}
