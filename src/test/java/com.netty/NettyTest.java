package com.netty;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.lin.netty.ftp2.engine.utils.AscUtil;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class NettyTest {


    /**
     *  获取本机 ip
     */
    @Test
    public void ipTest(){
        try {

            System.out.println(  InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void strSubTest(){
        String url = "/a/b";
        System.out.println(url.substring(1));
    }
    @Test
    public void baseTest(){
        String url = "D://你好";
        String encode = Base64.encode(url,StandardCharsets.UTF_8);
        System.out.println(encode);
        System.out.println(Base64.decodeStr(encode, StandardCharsets.UTF_8));
    }

    @Test
    public void aesTest(){
        //随机生成密钥
//        byte[] key = {-68, 13, 111, -22, 34, -4, 11, 45, 23, -53, 123, -99, -36, 12, -20, 44};
//        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key);

        String url = "D://ftp";
        String s = AscUtil.encrypt(url);
        System.out.println(s);
        String str = AscUtil.decrypt(s);
        System.out.println(str);
    }
}
