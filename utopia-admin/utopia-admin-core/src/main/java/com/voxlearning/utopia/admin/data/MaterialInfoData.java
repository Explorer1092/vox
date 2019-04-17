package com.voxlearning.utopia.admin.data;

import com.voxlearning.equator.service.configuration.api.entity.material.Material;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lei.liu
 * @version 18-7-23
 */
@Getter
public class MaterialInfoData {

    private String id;              // id

    private String subject;         // 学科
    private String materialType;    // 道具类型
    private String processorType;   // 道具处理方式
    private String activityType;    // 活动类型
    private String name;            // 道具名称

    @Setter
    private Integer quality;        // 数量

    public MaterialInfoData(Material material, Integer quality) {
        this.id = material.getId();
        this.subject = material.getSubject();
        this.materialType = material.getMaterialType();
        this.processorType = material.getProcessorType();
        this.activityType = material.getActivityType();
        this.name = material.getName();

        this.quality = quality;
    }
}
