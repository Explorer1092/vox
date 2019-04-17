package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Data;

import java.io.Serializable;

/**
 * 区域
 *
 * @author xiaolei.li
 * @version 2018/8/13
 */
@Data
public class RegionDto implements Serializable {

    /**
     * 键
     */
    private int id;

    /**
     * 名称
     */
    private String name;

    /**
     * 区域类型
     *
     * @see RegionType
     */
    private int type;

//    /**
//     * 区域实例
//     */
//    private ExRegion region;

    public static class Builder {
        public static RegionDto build(ExRegion region) {
            RegionDto dto = new RegionDto();
            dto.setId(region.getCode());
            String name;
            switch (region.fetchRegionType()) {
                case PROVINCE:
                    name = region.getName();
                    break;
                case CITY:
                    name = String.format("%s%s", region.getProvinceName(), region.getCityName());
                    break;
                case COUNTY:
                    name = String.format("%s%s%s", region.getProvinceName(), region.getCityName(), region.getCountyName());
                    break;
                default:
                    name = region.getName();
                    break;
            }
            dto.setName(name);
            dto.setType(region.fetchRegionType().getType());
            return dto;
        }
    }
}
