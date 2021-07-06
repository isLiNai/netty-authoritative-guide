package com.lin.netty.ftp2.engine.utils;

import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lin
 */
public class UrlUtil {


    public static String getUrlParameter(String uri,String key){
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
