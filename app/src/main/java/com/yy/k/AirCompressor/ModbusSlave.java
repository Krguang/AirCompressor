package com.yy.k.AirCompressor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by K on 2018/2/1.Failed to dispatch window animation state change.
         */

public class ModbusSlave extends Thread{


    private ArrayList<Byte> rxTemp = new ArrayList<Byte>();
    private Serial com2=new Serial();
    private Timer timer10ms=new Timer();
    private boolean rxFlag;

    private int SLAV_addr=1;
    private int[] localArray = new int[1024];

    private int jiZuStartStop = 0;

    public static int temperature = 250;
    public static int humidity = 500;
    public static int pressure = 833;


    private boolean getDataFlag =false;

    private int overPressure = 0;
    private int underPressure = 0;

    public void closeCom(){
        com2.Close();
    }

    @Override
    /**
     * 循环读取串口数据，rxFlag为true时处理数据
     */
    public void run() {
        super.run();
        com2.Open(2, 19200);
        timer10ms.schedule(taskPoll,5,5);//5ms后开始，每5ms轮询一次

        while (!isInterrupted()) {

            int[] RX = com2.Read();
            if (RX != null) {

                for (int rx:RX){        //遍历RX添加到txTemp中
                    rxTemp.add((byte)rx);
                }
            }

            if (rxFlag){
                rxFlag=false;
                byte[] rxTempByteArray = new byte[rxTemp.size()];
                int i=0;
                Iterator<Byte> iterator = rxTemp.iterator();
                while (iterator.hasNext()) {
                    rxTempByteArray[i] = iterator.next();
                    i++;
                }

                onDataReceived(rxTempByteArray,rxTemp.size());
                getDataFlag = true;
                rxTemp.clear();
            }

        }
    }

    /**
     *判断接收空闲，总线空闲时置位rxFlag
     */
    private TimerTask taskPoll=new TimerTask() {
        int txDataLengthTemp=0;
        int txIdleCount=0;
        public void run() {
            if(rxTemp.size()>0){
                if(txDataLengthTemp!=rxTemp.size()){
                    txDataLengthTemp=rxTemp.size();
                    txIdleCount=0;
                }
                if(txIdleCount<4){
                    txIdleCount++;
                    if (txIdleCount>=4){
                        txIdleCount=0;
                        rxFlag=true;
                    }
                }
            }
            else {
                txDataLengthTemp=0;
            }
        }
    };

    /***
     *  @param reBuf
     * @param size
     */

    private void onDataReceived(byte[] reBuf, int size) {

        if (size < 8)  return;
        if (SLAV_addr != reBuf[0]) return;

        if (CRC_16.checkBuf(reBuf)) {
            switch (reBuf[1]) {
                case 0x03:
                    mod_Fun_03_Slav(reBuf);
                    break;
                //case 0x06:	    mod_Fun_06_Slav(reBuf,size);	break;
                case 0x10:
                    mod_Fun_16_Slav(reBuf, size);
                    break;
                default:
                    break;
            }
        }
    }


    /***
     * slave  功能码03
     * @param reBuf
     */
    private void mod_Fun_03_Slav(byte[] reBuf) {
        slav_int_03();
        int addr;
        int len;
        CRC_16 crc = new CRC_16();
        byte[] seBuf = new byte[1024];
        addr = (crc.getUnsignedByte(reBuf[2])) << 8;
        addr |= crc.getUnsignedByte(reBuf[3]);
        len = (crc.getUnsignedByte(reBuf[4])) << 8;
        len |= crc.getUnsignedByte(reBuf[5]);

        if (len + addr > 64)
            return;
        else {
            seBuf[0] = (byte) reBuf[0];
            seBuf[1] = (byte) reBuf[1];
            seBuf[2] = (byte) (2 * len);

            for (int i = 0; i < len; i++) {
                seBuf[3 + 2 * i] = (byte) (crc.getUnsignedIntt(localArray[i + addr]) >> 8);
                seBuf[4 + 2 * i] = (byte) (crc.getUnsignedIntt(localArray[i + addr]));

            }

            crc.update(seBuf, 2 * len + 3);
            int value = crc.getValue();

            seBuf[3 + 2 * len] = (byte) crc.getUnsignedByte((byte) ((value >> 8) & 0xff));
            seBuf[4 + 2 * len] = (byte) crc.getUnsignedByte((byte) (value & 0xff));
        }


       onDataSend(seBuf,4 + 2 * len + 1);
        int[] disTemp= new int [4 + 2 * len + 1];
        for (int i=0;i<(4+2*len+1);i++){
            disTemp[i] = seBuf[i];
        }
        //Log.d(TAG, "mod_Fun_03_Slav: "+ Arrays.toString(disTemp));
    }


    /***
     * 发送数据
     * @param seBuf
     */
    public void onDataSend(byte[] seBuf, int size) {

        int sendTemp[] = new int[size];
        for (int i=0;i<size;i++){
            sendTemp[i]=seBuf[i];
        }
        com2.Write(sendTemp,size);
    }


    /***
     * slave  功能码03初始化
     */

    private void slav_int_03() {
        if (jiZuStartStop == 0){
            localArray[0] &= ~0x01;
        }else {
            localArray[0] |= 0x01;
        }
    }

    /***
     * slave   功能码16
     * @param reBuf
     * @param size
     */

    private void mod_Fun_16_Slav(byte[] reBuf, int size) {

        int addr, len;
        short val;
        byte[] seBuf = new byte[1024];
        CRC_16 crc = new CRC_16();
        addr = (crc.getUnsignedByte(reBuf[2])) << 8;
        addr |= crc.getUnsignedByte(reBuf[3]);
        len = (crc.getUnsignedByte(reBuf[4])) << 8;
        len |= crc.getUnsignedByte(reBuf[5]);

        for (int i = 0; i < len; i++) {
            val = (short) ((crc.getUnsignedByte(reBuf[7 + 2 * i])) << 8);
            val |= crc.getUnsignedByte(reBuf[8 + 2 * i]);

            /***
             * 取起始地址开始的数据
             */
            localArray[addr + i] = val;
        }

        for (int i = 0; i < 6; i++) {
            seBuf[i] = reBuf[i];
        }

        crc.update(seBuf, 6);
        int value = crc.getValue();
        seBuf[6] = (byte) crc.getUnsignedByte((byte) ((value >> 8) & 0xff));
        seBuf[7] = (byte) crc.getUnsignedByte((byte) (value & 0xff));

        slav_hand_10();
        onDataSend(seBuf, 8);
    }

    /***
     * slave   功能码16处理函数
     */
    private void slav_hand_10() {

        temperature = localArray[6];
        Log.d(TAG, "slav_hand_10: "+temperature);
        humidity = localArray[7];
        pressure = localArray[8];
        overPressure = (localArray[15]>>13)&1;
        underPressure = (localArray[15]>>12)&1;
    }

    public void setSLAV_addr(int SLAV_addr) {
        this.SLAV_addr = SLAV_addr;
    }

    public void setJiZuStartStop(int jiZuStartStop) {
        this.jiZuStartStop = jiZuStartStop;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getJiZuStartStop() {
        return jiZuStartStop;
    }


    public int getOverPressure() {
        return overPressure;
    }

    public int getUnderPressure() {
        return underPressure;
    }
    public boolean isGetDataFlag() {
        return getDataFlag;
    }

    public void setGetDataFlag(boolean getDataFlag) {
        this.getDataFlag = getDataFlag;
    }

    public void setOverPressure(int overPressure) {
        this.overPressure = overPressure;
    }

    public void setUnderPressure(int underPressure) {
        this.underPressure = underPressure;
    }
}
