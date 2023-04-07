package com.poc.j2mod;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * ModbusRTU报文格式：
 * Tx：  从站(或server)地址 + 功能码 + 起始地址（起始地址高位，起始地址低位） + 寄存器个数 + 校验码
 * Rx:   从站地址 + 功能码 + 字节个数 + 数据 + 校验码
 *
 * ModbusTcp: 将头RTU的地址部分移除，然后加上一些TCP层需要的部分构成 MBAP头
 * ModbusTcp读写（ j2mod 解析库, Modbus Salve模拟软件）
 * TCP报文 = MBAP报文头（长度 7 字节） + PDU(protocol data unit)
 * MBAP报文= 2字节事务标识 + 2字节协议标识(modbustcp标识 00 00) + 2字节的数据长度标识（长度指PDU部分字节数） + 1字节的单元标识（即modbus slave或 modbus server的站地址）
 * PDU = 1字节功能码 + 数据部分（如果是读操作，数据部分包含开始地址，读多少个；如果是写则数据部分包含了数据长度，写入的字节计数 及 实际要写入的值）
 * (在modbus slave模拟软件中，功能码设为 01后，每个单元格代表一个bit???)
 *
 *   功能码             名称             功能                                                                对应地址类型（地址区间的开始标识）
 *    01           读线圈状态      读位(读 N 个 bit)，读slave的线圈寄存器，位操作                                     0x
 *    02           读输入离散量     读位(读 N 个 bit),读slave的离散输入寄存器，位操作                                  1x
 *    03           读多个寄存器     读整形、字符型、状态字、浮点型(读N个words), 读保持寄存器，字节操作                     4x
 *    04           读输入寄存器     读整形、状态字、浮点型(读N个words), 读输入寄存器，字节操作                           3x
 *    05           写单个线圈       写位(写一个bit),写线圈寄存器 ，位操作                                            0x
 *    06           写单个保持寄存器   写整形、字符型、状态字、浮点型(写一个word), 写保持寄存器，字节操作                   4x
 *    0F           写多个线圈        写位（写 N个 bit）,强置一连串逻辑线圈的通断                                      0x
 *    10        写多个保持寄存器     写整形、字符型、状态字、浮点型(写N个word), 把具体的二进制值装入一串连续的保持寄存器      4x
 */
public class ModbusTcpAndJ2mod {

    public static void main(String[] args) throws Exception {

        readInputRegister();
    }


    /**
     * 读输出线圈 01功能码: 读 5 个 bit
     * 数据均为 Hex
     * Modbus slave 开5个单元 00000 - 00004:  10100
     * Tx: 00 00 00 00 00 06 01 01 00 00 00 05  :  事务标识(2字节) 协议标识(2字节)  PDU数据长度(2字节) 单元标识（1字节） 功能码(1字节)  起始地址(2字节)  读取数量(00 005)
     * Rx: 00 00 00 00 00 04 01 01 01 05        ： 事务标识(2字节) 协议标识(2字节)  PDU数据长度(2字节) 单元标识（1字节） 功能码(1字节) 字节计数 读取内容
     * j2mod response: 00101 (小端)
     * @throws Exception
     */
    public static void readOutputCoil() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        final BitVector bitVector = client.readCoils(1,0, 5);
        System.out.println(bitVector.toString());
    }

    /**
     * 读输入线圈  02功能码: 读 6 个 bit
     * 数据均为 Hex
     * Modbus slave 开6个单元 10000 - 10005:  110010
     * Tx: 00 00 00 00 00 06 01 02 00 00 00 06
     * Rx: 00 00 00 00 00 04 01 02 01 13
     * j2mod response: 010011 (小端)
     * @throws Exception
     */
    public static void readInputCoil() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        final BitVector bitVector = client.readInputDiscretes(1,0, 6);
        System.out.println(bitVector.toString());
    }

    /**
     * 读输入寄存器  04功能码: 读4个寄存器
     * 数据均为 Hex
     * Modbus slave 开4个单元 30001 - 30004:  21 22 23 0 (十进制)
     * Tx: 00 00 00 00 00 06 01 04 00 00 00 04 ：
     * Rx: 00 00 00 00 00 0A 01 04 08 00 14 00 15 00 16 00 00 ： 00 0A个字节的PDU，01号站地址，08个字节， 读取到的值为： 00 14 ，00 15, 00 16， 00 00
     * j2mod response: 读到十进制数字： 20 21 22 0
     * @throws Exception
     */
    public static void readInputRegister() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        final InputRegister[] inputRegisters = client.readInputRegisters(1, 0, 4);
        for (InputRegister inputRegister : inputRegisters) {
            System.out.println(inputRegister.getValue());
        }
    }


    /**
     * 读保持寄存器  03功能码: 读 5 个寄存器（即读 5 个 word,每个寄存器是一个word--16bit）
     * 数据均为 Hex
     * Modbus slave 开5个单元 40000 - 40004:  12345（十进制）
     * Tx: 00 00 00 00 00 06 01 03 00 00 00 05  :
     * Rx: 00 00 00 00 00 0D 01 03 0A 00 01 00 02 00 03 00 04 00 05
     * j2mod response: 答应寄存器值:  12345
     * @throws Exception
     */
    public static void readHoldingRegister() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        final Register[] registers = client.readMultipleRegisters(1, 0, 5);
        for (Register register : registers) {
            System.out.println(register.getValue());
        }
    }


    /**
     * 写单个线圈  05功能码: 写
     * 数据均为 Hex
     * Modbus slave 开3个单元 00000 - 00002:  初始值均为 off(fasle)
     * Tx: 00 00 00 00 00 06 01 05 00 01 FF 00  :  线圈通断表示：  FF 00 为置线圈为 ON , 00 00为置线圈OFF
     * Rx: 00 00 00 00 00 06 01 05 00 01 FF 00  ：原样返回接收的报文
     * j2mod response: 答应寄存器值:  12345
     * @throws Exception
     */
    public static void writeSingleCoil() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        final boolean writeRes = client.writeCoil(1, 1, true);
        System.out.println(writeRes);
    }

    /**
     * 写多个线圈  0F功能码: modbus slave 初始化 4 个，程序写 3 个线圈
     * 数据均为 Hex
     * Modbus slave 开3个单元 40001 - 40002:
     * Tx: 00 00 00 00 00 07 01 0F 00 00 00 03 01 03  : 从00 00地址开始写，写00 03个bit，写入的字节计数个数为 01 ,写入的值为 03
     * Rx: 00 00 00 00 00 06 01 0F 00 00 00 03
     * j2mod response: 答应寄存器值:  写入的寄存器个数
     * @throws Exception
     */
    public static void writeMultipleCoils() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        BitVector bitVector = new BitVector(3);//写三个线圈 （写 3个 bit）
        bitVector.setBit(0,true); // 第一个线圈置开
        bitVector.setBit(1,true); // 第二个线圈置开
        client.writeMultipleCoils(1,0,bitVector);
    }

    /**
     * 写单个寄存器  06功能码: 向 offset为1的位置写入 13
     * 数据均为 Hex
     * Modbus slave 开3个单元 40001 - 40002:
     * Tx: 00 00 00 00 00 06 01 06 00 01 00 0D  :  向起始地址 00 01 写入值 00 0D
     * Rx: 00 00 00 00 00 06 01 06 00 01 00 0D  ： 原文返回
     * j2mod response: 写入的数值
     * @throws Exception
     */
    public static void writeSingleRegister() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        // SimpleInputRegister() VS SimpleRegister()
        Register register = new SimpleRegister(13);
        final int value = client.writeSingleRegister(1, 1, register);
        System.out.println(value);
    }


    /**
     * 写多个寄存器  10功能码: 从 offset 为 0 的地方开始写 2 个寄存器（写 2个word）
     * 数据均为 Hex
     * Modbus slave 开3个单元 40001 - 40002:
     * Tx: 00 00 00 00 00 0B 01 10 00 00 00 02 04 00 0A 00 0B  :  从起始地址 00 00 开始写入 00 02 个，总共写入字节数 04  写入值为 00 0A 00 0B
     * Rx: 00 00 00 00 00 06 01 10 00 00 00 02  ： 返回PDU为00 06个字节 ，00 00：写入地址 ，00 02: 写入数量
     * j2mod response: 答应寄存器值:  写入的寄存器个数
     * @throws Exception
     */
    public static void writeMultipleRegister() throws Exception{
        ModbusTCPMaster client = new ModbusTCPMaster("192.168.16.58",502);
        client.connect();
        Register register1 = new SimpleRegister(10);
        Register register2 = new SimpleRegister(11);

        final int value = client.writeMultipleRegisters(1,0,new Register[]{register1,register2});
        System.out.println(value);
    }



}
