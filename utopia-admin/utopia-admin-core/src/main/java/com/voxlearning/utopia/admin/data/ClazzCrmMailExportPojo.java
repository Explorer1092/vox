package com.voxlearning.utopia.admin.data;


import lombok.Data;

/**
 * @author guangqing
 * @since 2018/12/25
 */
@Data
public class ClazzCrmMailExportPojo {
    //用户名
    private String userName;
    //用户id
    private Long userId;
    //收货人姓名
    private String recipientName;
    //收货人电话
    private String recipientTel;
    //收货人地址
    private String recipientAddr;
}
