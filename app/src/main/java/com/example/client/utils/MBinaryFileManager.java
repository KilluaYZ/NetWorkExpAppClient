package com.example.client.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class MBinaryFileManager {
    private Uri filePath;
    private int _cur;
    private int _ext_size;
    private int _size;
    private int _byte_size;
    private OutputStream outfile;
    private InputStream infile;
    private Context context;

    public MBinaryFileManager(Context context, Uri filePath){
        this.filePath = filePath;
        this._cur = 0;
        this._ext_size = 0;
        this._size = 0;
        this._byte_size = 0;
        this.context = context;
    }

    private void first(byte[] data) throws FileNotFoundException {
        MFrame mframe = new MFrame().fromBytes(data);
        if(mframe.get_type() != MFrame.FRAME_TYPE_START) throw new RuntimeException();
        if(mframe.get_id() != 0) throw new RuntimeException();
        this._cur = 0;
        this._size = mframe.get_length();
        this._ext_size = this._size + 2;
        ContentResolver contentResolver = context.getContentResolver();
        this.outfile = contentResolver.openOutputStream(this.filePath);
    }

    private void mid (byte[] data) throws IOException {
        MFrame mframe = new MFrame().fromBytes(data);
        if(mframe.get_type() != MFrame.FRAME_TYPE_DATA) throw new RuntimeException();
        outfile.write(mframe._buf);
        outfile.flush();
    }

    private void end(byte[] data) throws IOException {
        MFrame mframe = new MFrame().fromBytes(data);
        if(mframe.get_type() != MFrame.FRAME_TYPE_END) throw new RuntimeException();
        if(mframe.get_id() != this._ext_size - 1) throw new RuntimeException();
        this.outfile.close();
    }

    private byte[] first() throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        this.infile = contentResolver.openInputStream(this.filePath);
        ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(this.filePath, "r");
        FileDescriptor fd = pfd.getFileDescriptor();
        FileChannel fileChannel = new FileInputStream(fd).getChannel();
        long fileSize = fileChannel.size();
        this._cur = 0;
        this._byte_size = (int) fileSize;
        this._size = (int) Math.ceil((double) fileSize / MFrame.FRAME_BUF_SIZE);
        this._ext_size = this._size + 2;

        MFrame mframe = new MFrame()
                .set_id(0)
                .set_type(MFrame.FRAME_TYPE_START)
                .set_length(this._size);
        return mframe.toBytes();
    }

    private byte[] mid() throws IOException {
        byte[] _bytes = new byte[MFrame.FRAME_BUF_SIZE];
        int readSize = this.infile.read(_bytes, 0, MFrame.FRAME_BUF_SIZE);

        MFrame mframe = new MFrame()
                .set_id(this._cur)
                .set_type(MFrame.FRAME_TYPE_DATA)
                .set_length(readSize)
                .set_buf(_bytes);

        return mframe.toBytes();
    }

    private byte[] end() throws IOException {
        infile.close();
        MFrame mframe = new MFrame()
                .set_id(this._ext_size - 1)
                .set_type(MFrame.FRAME_TYPE_END)
                .set_length(0);
        return mframe.toBytes();
    }

    public byte[] next() throws IOException {
        byte[] ret;
        if(this._cur == 0){
            ret = first();
        }else if(this._cur == _ext_size - 1){
            ret = end();
        }else{
            ret = mid();
        }
        this._cur++;
        return ret;
    }

    public void next(byte[] _bytes) throws IOException {
        if(this._cur == 0){
            first(_bytes);
        }else if(this._cur == _ext_size - 1){
            end(_bytes);
        }else{
            mid(_bytes);
        }
        this._cur++;
    }

    public boolean has_next() {
        if(this._ext_size == 0) return true;
        if(this._cur < this._ext_size) return true;
        return false;
    }

    // 获取总的包的数量
    public int getTotalNum() {
        return this._ext_size;
    }

    // 获取当前已经收到的包的数量
    public int getCurNum() {
        return this._cur;
    }
}
