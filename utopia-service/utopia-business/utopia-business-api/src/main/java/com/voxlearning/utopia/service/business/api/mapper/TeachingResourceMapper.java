package com.voxlearning.utopia.service.business.api.mapper;

import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by haitian.gan on 2017/8/10.
 */
@Getter
@Setter
public class TeachingResourceMapper implements Serializable{
    private static final long serialVersionUID = -8596104212641225551L;

    private String id;
    private String image;
    private String name;
    private Date updateAt;
    private String subjectNames;
    private String category;
    private String categoryName;
    private Boolean online;
    private Integer displayOrder;
    private Boolean featuring;    // 是否展示在首页(精选)
    private TeachingResource.Label label;

}
