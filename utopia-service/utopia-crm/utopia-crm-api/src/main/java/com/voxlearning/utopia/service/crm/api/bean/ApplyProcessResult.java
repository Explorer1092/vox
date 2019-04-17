package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 申请处理结果
 *
 * @author song.wang
 * @date 2017/1/5
 */
@Getter
@Setter
public class ApplyProcessResult implements Serializable {
    private static final long serialVersionUID = 8979059280291290824L;
    private Date processDate;                    // 处理日期
    private SystemPlatformType userPlatform;      // 用户平台，标志处理者是哪个平台的
    private String account;             // 处理者账号
    private String accountName;                // 处理者姓名
    private String result;        //处理结果
    private String processNotes;                 // 处理意见
}
