package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/1
 * Time: 15:25
 */
@Getter
@Setter
public class RecordMapper implements Serializable {
    private static final long serialVersionUID = 9080207338311751058L;

    private String recordId;
    private Long userId;
    private String recordTypeEnumName;
    private Long clazzId;
    private Long createTime = System.currentTimeMillis(); //默认当前时间
}
