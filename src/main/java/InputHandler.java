import jdk.nashorn.api.scripting.JSObject;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vitaly on 31.10.15.
 */
public class InputHandler implements Runnable {
    SerialPort serialPort;
    OrientationOperator operator;

    public InputHandler(SerialPort serialPort){
        this.serialPort = serialPort;
        operator = new OrientationOperator();
    }
    public static boolean isFirst = true;

    public void run() {
        String str = null;
        int bracketCount = 0;
        StringBuilder sb = new StringBuilder();

        try {
            while (MainFrame.isReceavingMode) {

                str = serialPort.readString();
                if(str != null) {
//                    System.out.print(str);
//                    if (isFirst){
//                        Date date = new Date();
//                        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss.SSS");
//                        String formattedDate = sdf.format(date);
//                        MainFrame.dataList.append(formattedDate + "\n");
//                        isFirst = false;
//                    }

                    for( int i = 0; i < str.length(); i++ ) {
                        if( str.charAt(i) == '{' ) {
                            bracketCount++;
                        }else if( str.charAt(i) == '}') {
                            if (bracketCount > 0) {
                                bracketCount--;
                            } else {
                                bracketCount = 0;
                            }
                        }
                    }

                    if( bracketCount < 1 ){
                        sb.append(str);
                        //Log.i(LOG_TAG, "I RECEIVE IT: " + sb.toString());

//                        sendIncomingData(sb.toString());

                        JSONObject jsonObject = new JSONObject(sb.toString());
                        operator.addData(jsonObject);
                        System.out.println("gone");
//                        System.out.println(String.format("ax %d ay %d az %d gx %d gy %d gz %d", jsonObject.getInt("ax"),
//                                jsonObject.getInt("ay"), jsonObject.getInt("az"), jsonObject.getInt("gx"), jsonObject.getInt("gy"), jsonObject.getInt("gz")));
//                        System.out.println(sb.toString());

                        sb.setLength(0);
                        bracketCount = 0;
                    }else{
                        sb.append(str);
                    }
                }
                MainFrame.dataList.append(str);
            }
        }
        catch (
                SerialPortException ex) {
            System.out.println(ex);
        }
    }

}
