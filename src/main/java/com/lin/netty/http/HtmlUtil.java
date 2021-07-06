package com.lin.netty.http;

import com.lin.netty.ftp.model.FtpMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/2 13:44
 */
public  class HtmlUtil {


    public static String readHtmlStr(String path) {
        String filePath = ClassLoader.getSystemResource(path).getPath();
        File file = new File(filePath);
        StringBuffer stringBuffer = new StringBuffer();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            FileChannel channel = fileInputStream.getChannel();
            // 一个个字节读 是不是影响时间效率， 有待研究
            ByteBuffer byteBuffer =  ByteBuffer.allocate((int) channel.size());
            while(-1!=channel.read(byteBuffer)){
                byte[] array = byteBuffer.array();
                stringBuffer.append(new String(array,"utf-8"));
                byteBuffer.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    public static byte[] readFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            FileChannel channel = fileInputStream.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            channel.read(byteBuffer);
            return byteBuffer.array();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
