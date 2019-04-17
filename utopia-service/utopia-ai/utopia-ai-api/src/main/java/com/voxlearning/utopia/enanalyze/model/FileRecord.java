package com.voxlearning.utopia.enanalyze.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件上传信息
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class FileRecord implements Serializable {
    private String fileId;
    private String url;
    private String openId;
    private Date createDate;
    private Date updateDate;
}
