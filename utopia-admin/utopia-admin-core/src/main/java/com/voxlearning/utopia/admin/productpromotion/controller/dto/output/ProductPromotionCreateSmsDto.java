package com.voxlearning.utopia.admin.productpromotion.controller.dto.output;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 产品推广创建短信Dto
 * @author: kaibo.he
 * @create: 2019-01-29 10:58
 **/
@Data
public class ProductPromotionCreateSmsDto implements Serializable{
    private String bizType;
    private String phone;
    private String targetUserType;
    private String smsContent;
    private String status;

}
