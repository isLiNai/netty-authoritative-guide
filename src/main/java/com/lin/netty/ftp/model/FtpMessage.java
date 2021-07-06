package com.lin.netty.ftp.model;

import lombok.Data;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/2 13:35
 */
@Data
public class FtpMessage {
    private String name;
    private Boolean ifDirectory;
    private String absoluteUrl;
}
