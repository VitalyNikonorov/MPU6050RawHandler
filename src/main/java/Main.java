import jssc.*;

import java.nio.charset.StandardCharsets;

/**
 * Created by vitaly on 29.10.15.
 */

public class Main {
    static SerialPort serialPort;

    public static void main(String[] args){
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
        }

        serialPort = new SerialPort(portNames[0]);
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(9600, 8, 1, 0);//Set params
            byte[] buffer = serialPort.readBytes(24);
            System.out.print(new String(buffer, StandardCharsets.UTF_8));

            buffer = serialPort.readBytes(65);
            System.out.print(new String(buffer, StandardCharsets.UTF_8));

            System.out.println("\n---------------");

            buffer = serialPort.readBytes(34);
            System.out.print(new String(buffer, StandardCharsets.UTF_8));

//            buffer = serialPort.readBytes(34);
//            System.out.print(new String(buffer, StandardCharsets.UTF_8));

            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }

    }

    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                if(event.getEventValue() == 34){//Check bytes count in the input buffer
                    //Read data, if 10 bytes available
                    try {
                        byte buffer[] = serialPort.readBytes(34);
                        System.out.print(buffer);
                    }
                    catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            }
            else if(event.isCTS()){//If CTS line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("CTS - ON");
                }
                else {
                    System.out.println("CTS - OFF");
                }
            }
            else if(event.isDSR()){///If DSR line has changed state
                if(event.getEventValue() == 1){//If line is ON
                    System.out.println("DSR - ON");
                }
                else {
                    System.out.println("DSR - OFF");
                }
            }
        }
    }


}
