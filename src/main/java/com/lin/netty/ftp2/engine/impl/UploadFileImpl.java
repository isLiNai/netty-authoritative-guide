package com.lin.netty.ftp2.engine.impl;

import com.lin.netty.ftp2.engine.constant.DeskMappingConstant;
import com.lin.netty.ftp2.engine.utils.AscUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.Data;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/5 11:27
 */
public class UploadFileImpl extends MappingAbs {

    static HttpPostRequestDecoder httpDecoder = null;

    @Override
    public void execute(ChannelHandlerContext ctx,FullHttpRequest fullHttpRequest, String url) {
        FormData formData = getMultipartFile(fullHttpRequest);
        fileUpload(formData);
        String res = "{ error:'" + "" + "', msg:'" + "" + "',imgurl:'" + "" + "'}";
        // 这里响应给前端需要以 <pre></pre>
        ctx.channel().write(processFullHttpResponse(HttpResponseStatus.OK,  res.getBytes(StandardCharsets.UTF_8)));
        if(httpDecoder!=null){
            httpDecoder.destroy();
        }
    }

    private void fileUpload(FormData formData) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            Map<String, FileUpload> multipartFile = formData.getFileUploadMap();
            Map<String, Attribute> attributeMap = formData.getAttributeMap();
            Attribute attribute = attributeMap.get("id");
            String value = attribute.getValue();
            String path = AscUtil.decrypt(value);
            for (String key : multipartFile.keySet()) {
                //获取文件对象
                FileUpload file = multipartFile.get(key);
                fileInputStream = new FileInputStream(file.getFile());
                fileOutputStream = new FileOutputStream(path + "\\" + file.getFilename());

                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int read = 0;
                while((read = fileInputStream.getChannel().read(byteBuffer)) > 0){
                    byteBuffer.flip();
                    fileOutputStream.getChannel().write(byteBuffer);
                    byteBuffer.clear();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fileInputStream!=null){
                    fileInputStream.close();
                }
                if(fileOutputStream!=null){
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 功能描述
     * <p>解析文件上传</p>
     * @params [ctx, httpDecode]
     */
    private static FormData getMultipartFile(FullHttpRequest request) {
        try {
            //创建HTTP对象工厂
            HttpDataFactory factory = new DefaultHttpDataFactory(true);
            //使用HTTP POST解码器
            httpDecoder = new HttpPostRequestDecoder(factory, request, StandardCharsets.UTF_8);
            httpDecoder.setDiscardThreshold(0);
            if (httpDecoder != null) {
                //获取HTTP请求对象
                final HttpContent chunk = (HttpContent) request;
                //加载对象到加吗器。
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {

                    FormData formData = new FormData();
                    //存放文件对象
                    Map<String, FileUpload> fileUploads = new HashMap<>();
                    //存放参数对象
                    Map<String, Attribute> attributeMap = new HashMap<>();
                    //通过迭代器获取HTTP的内容
                    java.util.List<InterfaceHttpData> InterfaceHttpDataList = httpDecoder.getBodyHttpDatas();
                    for (InterfaceHttpData data : InterfaceHttpDataList) {
                        //如果数据类型为文件类型，则保存到fileUploads对象中
                        if (data != null && InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                            FileUpload fileUpload = (FileUpload) data;
                            fileUploads.put(data.getName(), fileUpload);
                            formData.setFileUploadMap(fileUploads);
                        }
                        if (data != null && InterfaceHttpData.HttpDataType.Attribute.equals(data.getHttpDataType())) {
                            Attribute attribute = (Attribute) data;
                            attributeMap.put(attribute.getName(),attribute);
                            formData.setAttributeMap(attributeMap);
                        }
                    }
                    return formData;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Data
    static class FormData{
        private Map<String, FileUpload> fileUploadMap;
        private Map<String, Attribute> attributeMap;
    }



}
