package com.example.client.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public class MSocketClient {
    private Socket socket;

    public MSocketClient(){}

    // 进行socket连接
    public void connect(String ip, int port, Context context){
        try{
            socket = new Socket(ip, port);
            System.out.println("Connect to Server "+ip+":"+port);
            Toast.makeText(context, "Connect to Server "+ip+":"+port, Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            if(socket != null && !socket.isClosed()) socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void ack() throws IOException {
        if(socket == null) throw new NullPointerException();
        OutputStream out = socket.getOutputStream();
        MFrame ack_frame = new MFrame();
        ack_frame.set_type(MFrame.FRAME_TYPE_ACK);
        out.write(ack_frame.toBytes());
    }

    // 初始化receive，每一次运行receive之前都需要调用
    public void initReceive(){
        try{
            if(socket == null) throw new NullPointerException();
            // 向服务器发送一个包，表明想要请求资源
            OutputStream out = socket.getOutputStream();
            MFrame mFrame = new MFrame();
            mFrame.set_type(MFrame.FRAME_TYPE_REQUEST_DATA);
            out.write(mFrame.toBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 同步接收文件
    public void receiveSyn(MBinaryFileManager mBinaryFileManager, Callable<Void> callable) {
        try{
            byte[] buffer = new byte[1024];
            InputStream in = socket.getInputStream();
            // 从socket中读取数据
            int readSize = in.read(buffer);
            // 将数据传到mBinaryFileManager中，进行处理和保存
            mBinaryFileManager.next(buffer);
            // 调用回调函数更新UI
            callable.call();
            // 向服务端发送一个ack，表示当前的包已经收到了
            ack();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //    receiveSyn用法示例
    public void receive(Context context){
        // 待保存的文件路径
        Uri filePath = Uri.parse("123.txt");
        // 大文件传输包拆分和拼接的Manager
        MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(context, filePath);
        // 接收大文件之前需要进行的一些初始化
        initReceive();
        // 循环地接收数据包
        while(mBinaryFileManager.has_next()){
            receiveSyn(mBinaryFileManager, () -> {
                // UI更新逻辑
                // 要获取的包的总数
                int totalPackageNum = mBinaryFileManager.getTotalNum();
                // 当前已经获取到的包的数量
                int curPackageNum = mBinaryFileManager.getCurNum();
                // TODO
                return null;
            });
        }
    }

    public void initSend(){
        try{
            if(socket == null) throw new NullPointerException();
            // 向服务器发送一个包，表明想要发送资源
            OutputStream out = socket.getOutputStream();
            MFrame mFrame = new MFrame();
            mFrame.set_type(MFrame.FRAME_TYPE_SEND_DATA);
            out.write(mFrame.toBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendSyn(MBinaryFileManager mBinaryFileManager, Callable<Void> callable){
        try{
            byte[] buffer = new byte[1024];
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            // 获取包的字节数据，然后通过socket发送给server
            byte[] resp = mBinaryFileManager.next();
            out.write(resp);
            // 调用回调函数更新UI
            callable.call();
            // 从socket中获取ack，表示服务端已经收到了我们上一次发出的包
            int readSize = in.read(buffer);
            // 检查从服务端获取到的包是不是ack
            MFrame mFrame = new MFrame().fromBytes(buffer);
            if(mFrame.get_type() != MFrame.FRAME_TYPE_ACK) throw new RuntimeException("获取到的Frame不是ack类型");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //    sendSyn用法示例
    public void send(Context context){
        // 待保存的文件路径
        Uri filePath = Uri.parse("123.txt");
        // 大文件传输包拆分和拼接的Manager
        MBinaryFileManager mBinaryFileManager = new MBinaryFileManager(context, filePath);
        // 发送大文件之前需要进行的一些初始化
        initSend();
        // 循环地发送数据包
        while(mBinaryFileManager.has_next()){
            sendSyn(mBinaryFileManager, () -> {
                // UI更新逻辑
                // 要获取的包的总数
                int totalPackageNum = mBinaryFileManager.getTotalNum();
                // 当前已经获取到的包的数量
                int curPackageNum = mBinaryFileManager.getCurNum();
                // TODO
                return null;
            });
        }
    }
}
