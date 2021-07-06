package com.netty;

import cn.hutool.core.util.ClassUtil;
import com.sun.jndi.toolkit.url.Uri;
import org.junit.Test;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClassUtilTest {


    @Test
    public void classUtilTest() throws UnknownHostException {
        String ip = "http://"+ InetAddress.getLocalHost().getHostAddress()+":8080/";
        System.out.println(ip);
    }

    private String getUrlParameter(String uri,String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        String[] split = uri.split("\\?");
        if(split.length>1){
            String p = split[1];
            String[] split1 = p.split("&");
            List<String> f = Arrays.stream(split1).filter(v -> v.contains(key+"=")).collect(Collectors.toList());
            if(f.size()>0){
                return f.get(0).substring(key.length()+1);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

}
