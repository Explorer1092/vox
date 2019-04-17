package com.voxlearning.utopia.agent.view.partner.input;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 创建修改合作伙伴入参
 * @author: kaibo.he
 * @create: 2019-04-02 11:00
 **/
@Data
@Builder
public class UpsertPartnerParams implements Serializable{
    private Long id;                    //id
    private String name;                // 名称
    private Integer regionCode;       // 区域CODE
    private Double longitude;           // 地理坐标：经度
    private Double latitude;            // 地理坐标：纬度
    private String type;//机构类别
    private String homePhotoUrl;     // 门头照片
    private List<String> otherPhotoUrls; //其他图片
}
