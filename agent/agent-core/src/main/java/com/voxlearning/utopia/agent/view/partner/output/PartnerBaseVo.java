package com.voxlearning.utopia.agent.view.partner.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.agent.service.partner.model.Partner;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @description: 机构基础信息视图
 * @author: kaibo.he
 * @create: 2019-04-02 21:01
 **/
@Data
public class PartnerBaseVo {
    private Long id;                    //id
    private String name;                // 名称
    private Integer provinceCode;       // 省份CODE
    private String provinceName;        // 省份名称
    private Integer cityCode;           // 城市CODE
    private String cityName;            // 城市名称
    private Integer countyCode;         // 地区CODE
    private String countyName;          // 地区名称
    private Double longitude;           // 地理坐标：经度
    private Double latitude;            // 地理坐标：纬度
    private String address;             //地址
    private String type;//机构类别
    private String homePhotoUrl;     // 门头照片
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> otherPhotoUrls; //其他图片

    public static class Builder {
        public static PartnerBaseVo build(Partner partner) {
            PartnerBaseVo vo = new PartnerBaseVo();
            BeanUtils.copyProperties(partner, vo);
            vo.setLongitude(NumberUtils.toDouble(partner.getLongitude()));
            vo.setLatitude(NumberUtils.toDouble(partner.getLatitude()));
            return vo;
        }
    }
}
