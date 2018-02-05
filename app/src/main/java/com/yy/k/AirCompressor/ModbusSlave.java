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


    private ArrayList<Byte> rxTemp = new ArrayList();
    private Serial com2=new Serial();
    private Timer timer10ms=new Timer();
    private boolean rxFlag;
    private final int SLAV_addr=1;
    int[] regHodingBuf = new int[1024];

    public boolean allowWriteShiDuSet = true;
    public boolean allowWriteWenDuSet = true;

    private int jiZuStartStop = 0;
    private int zhiBanStartStop = 0;
    private int fuYaStartStop = 0;
    private int wenDuSet = 250;
    private int shiDuSet = 500;
    private int yaChaSet = 500;
    private int wenDu = 250;
    private int shiDu = 500;
    private short yaCha = 500;
    private short fengJiZhuangTai = 0;
    private short zhiBanZhuangTai = 0;
    private short fuYaZhuangtai = 0;
    private short fengJiGuZhang = 0;
    private short GaoXiao;

    private short ColdWaterValveOpening = 0;//冷水阀
    private short HotWaterValveOpening = 0;//热水阀
    private short HumidifieOpening = 0;   //加湿器
    private short TheAirTemperature = 0;//新风温度

    private short upperComputerHeartBeatMonitoringPoint = 0;       //上位机心跳监控点
    private short upperComputerHandAutomaticallyMonitoringPoint = 0;//上位机手自动监控点
    private short upperComputerFengjiZHuangTaiMonitoringPoint;//上位机风机状态监控点
    private short upperComputerZhongXiaoMonitoringPoint;//上位机盘管低温监控点
    private short upperComputerGaoXiaoMonitoringPoint;//上位机高效报警监控点
    private short upperComputerChuXiaoMonitoringPoint;//上位机中效报警监控点
    private short upperComputerElectricWarmOneMonitoringPoint;//上位机电加热1监控点
    private short upperComputerElectricWarmTwoMonitoringPoint;//上位机电加热2监控点
    private short upperComputerElectricWarmThreeMonitoringPoint;//上位机电加热3监控点
    private short upperComputerElectricWarmHighTemperatureMonitoringPoint;//上位机电加热高温监控点
    private short upperComputerFengJiQueFengMonitoringPoint;//上位机风机缺风监控点
    private short upperComputerSterilizationMonitoringPoint;//上位机灭菌监控点
    private short upperComputerFengJiStartMonitoringPoint;//上位机风机已启动监控点
    private short upperComputerPaiFengJiStartMonitoringPoint;//上位机排风机已启动监控点
    private short upperComputerZhiBanStartMonitoringPoint;//上位机值班已启动监控点
    private short upperComputerFuYaStartMonitoringPoint;//上位机负压启动监控点
    private short upperComputerElectricPreheatOneMonitoringPoint;//上位机电预热1监控点
    private short upperComputerElectricPreheatTwoMonitoringPoint;//上位机电预热2监控点
    private short upperComputerElectricPreheatThreeMonitoringPoint;//上位机电预热3监控点
    private short upperComputerElectricPreheatHighTemperatureMonitoringPoint;//上位机电预热高温监控点
    private short upperComputerCompressorOneStartMonitoringPoint;//上位机压缩机1运行监控点
    private short upperComputerCompressorTwoStartMonitoringPoint;//上位机压缩机2运行监控点
    private short upperComputerCompressorThreeStartMonitoringPoint;//上位机压缩机3运行监控点
    private short upperComputerCompressorFourStartMonitoringPoint;//上位机压缩机4运行监控点
    private short upperComputerCompressorOneBreakdownMonitoringPoint;//上位机压缩机1故障监控点
    private short upperComputerCompressorTwoBreakdownMonitoringPoint;//上位机压缩机2故障监控点
    private short upperComputerCompressorThreeBreakdownMonitoringPoint;//上位机压缩机3故障监控点
    private short upperComputerCompressorFourBreakdownMonitoringPoint;//上位机压缩机4故障监控点
    private short WinterInSummer = 0;//冬夏季

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
                Log.d(TAG, "run: "+rxTemp.toString());
                Log.d(TAG, "run: "+rxTemp.size());
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
                seBuf[3 + 2 * i] = (byte) (crc.getUnsignedIntt(regHodingBuf[i + addr]) >> 8);
                seBuf[4 + 2 * i] = (byte) (crc.getUnsignedIntt(regHodingBuf[i + addr]));

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
        Log.d(TAG, "mod_Fun_03_Slav: "+ Arrays.toString(disTemp));


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
        regHodingBuf[0] = jiZuStartStop;
        regHodingBuf[1] = zhiBanStartStop;
        regHodingBuf[2] = 0;//预留
        regHodingBuf[3] = fuYaStartStop;
        regHodingBuf[4] = 0;//预留
        regHodingBuf[5] = wenDuSet;
        regHodingBuf[6] = shiDuSet;
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
            regHodingBuf[addr + i] = val;
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

        if (allowWriteWenDuSet) {
            wenDuSet = (short) regHodingBuf[5];
        }
        if (allowWriteShiDuSet) {
            shiDuSet = (short) regHodingBuf[6];
        }

        wenDu = (short) regHodingBuf[7];
        shiDu = (short) regHodingBuf[8];
        fengJiZhuangTai = (short) regHodingBuf[9];
        //zhiBanZhuangTai = (short) regHodingBuf[10];
        zhiBanZhuangTai=(short)(regHodingBuf[10]&0x01);
        //upperComputerFuYaStartMonitoringPoint = (short) ((regHodingBuf[10] & 0x02) >> 1);                      //上位机负压启动监控点
        fuYaZhuangtai = (short) ((regHodingBuf[10] & 0x02) >> 1);                      //上位机负压启动监控点

        fengJiGuZhang = (short) regHodingBuf[11];
        GaoXiao = (short) regHodingBuf[12];
        ColdWaterValveOpening = (short) regHodingBuf[13];//冷水阀开度
        HotWaterValveOpening = (short) regHodingBuf[14];//热水阀开度
        HumidifieOpening = (short) regHodingBuf[15];  //加湿器开度
        TheAirTemperature = (short) regHodingBuf[16]; //新风温度

        upperComputerHeartBeatMonitoringPoint = (short) ((regHodingBuf[17] & 0x0100) >> 8);                    //上位机心跳监控点
        upperComputerHandAutomaticallyMonitoringPoint = (short) ((regHodingBuf[17] & 0x0200) >> 9);            //上位机手自动监控点
        upperComputerFengjiZHuangTaiMonitoringPoint = (short) ((regHodingBuf[17] & 0x0400) >> 10);             //上位机风机状态监控点
        upperComputerZhongXiaoMonitoringPoint = (short) ((regHodingBuf[17] & 0x0800) >> 11);                     //上位机中效报警监控点
        upperComputerGaoXiaoMonitoringPoint = (short) ((regHodingBuf[17] & 0x1000) >> 12);                     //上位机高效报警监控点
        upperComputerChuXiaoMonitoringPoint = (short) ((regHodingBuf[17] & 0x2000) >> 13);                     //上位机初效报警监控点
        upperComputerElectricWarmOneMonitoringPoint = (short) ((regHodingBuf[17] & 0x4000) >> 14);             //上位机电加热1监控点
        upperComputerElectricWarmTwoMonitoringPoint = (short) ((regHodingBuf[17] & 0x8000) >> 15);             //上位机电加热2监控点

        upperComputerElectricWarmThreeMonitoringPoint = (short) (regHodingBuf[17] & 0x01);                     //上位机电加热3监控点
        upperComputerElectricWarmHighTemperatureMonitoringPoint = (short) ((regHodingBuf[17] & 0x02) >> 1);    //上位机电加热高温监控点
        upperComputerFengJiQueFengMonitoringPoint = (short) ((regHodingBuf[17] & 0x04) >> 2);                  //上位机风机缺风监控点
        upperComputerSterilizationMonitoringPoint = (short) ((regHodingBuf[17] & 0x08) >> 3);                  //上位机灭菌监控点
        upperComputerFengJiStartMonitoringPoint = (short) ((regHodingBuf[17] & 0x10) >> 4);                    //上位机风机已启动监控点
        upperComputerPaiFengJiStartMonitoringPoint = (short) ((regHodingBuf[17] & 0x20) >> 5);                 //上位机排风机已启动监控点
        upperComputerZhiBanStartMonitoringPoint = (short) ((regHodingBuf[17] & 0x40) >> 6);                    //上位机值班已启动监控点
        //   upperComputerFuYaStartMonitoringPoint = (short) ((regHodingBuf[17] & 0x80) >> 7);                      //上位机负压启动监控点
/*
        upperComputerElectricPreheatOneMonitoringPoint = (short) ((regHodingBuf[18] & 0x10) >> 4);             //上位机电预热1监控点
        upperComputerElectricPreheatTwoMonitoringPoint = (short) ((regHodingBuf[18] & 0x20) >> 5);             //上位机电预热2监控点
        upperComputerElectricPreheatThreeMonitoringPoint = (short) ((regHodingBuf[18] & 0x40) >> 6);           //上位机电预热3监控点
        upperComputerElectricPreheatHighTemperatureMonitoringPoint = (short) ((regHodingBuf[18] & 0x80) >> 7); //上位机电预热高温监控点
        upperComputerCompressorOneStartMonitoringPoint = (short) ((regHodingBuf[18] & 0x0100) >> 8);           //上位机压缩机1运行监控点
        upperComputerCompressorTwoStartMonitoringPoint = (short) ((regHodingBuf[18] & 0x0200) >> 9);           //上位机压缩机2运行监控点
        upperComputerCompressorThreeStartMonitoringPoint = (short) ((regHodingBuf[18] & 0x0400) >> 10);        //上位机压缩机3运行监控点
        upperComputerCompressorFourStartMonitoringPoint = (short) ((regHodingBuf[18] & 0x0800) >> 11);         //上位机压缩机4运行监控点
        upperComputerCompressorOneBreakdownMonitoringPoint = (short) (regHodingBuf[18] & 0x01);                //上位机压缩机1故障监控点
        upperComputerCompressorTwoBreakdownMonitoringPoint = (short) ((regHodingBuf[18] & 0x02) >> 1);         //上位机压缩机2故障监控点
        upperComputerCompressorThreeBreakdownMonitoringPoint = (short) ((regHodingBuf[18] & 0x04) >> 2);       //上位机压缩机3故障监控点
        upperComputerCompressorFourBreakdownMonitoringPoint = (short) ((regHodingBuf[18] & 0x08) >> 3);        //上位机压缩机4故障监控点
        WinterInSummer = (short) ((regHodingBuf[20] & 0x04) >> 2);                                             //冬夏季监控控制点偏移2
    */
    }

}
