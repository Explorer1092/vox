package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.utopia.service.reward.entity.PublicGoodStyle;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PGStyleMapper implements Serializable{

    private static final long serialVersionUID = 3658465006237390230L;

    private Long id;
    private String name;         // 名称
    private String summary;      // 简介
    private String previewImg;   // 预览图
    private String status;       // 状态
    private String schoolName;   // 学校简称
    private String collectId;    // 教室ID，在梦想教室列表里面进去查看用

    public static PGStyleMapper of(PublicGoodStyle style){
        PGStyleMapper pgStyleMapper = new PGStyleMapper();
        pgStyleMapper.setName(style.getName());
        pgStyleMapper.setId(style.getId());
        pgStyleMapper.setSummary(style.getSummary());
        pgStyleMapper.setPreviewImg(style.getPreviewImg());

        return pgStyleMapper;
    }
}
