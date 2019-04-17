package com.voxlearning.wechat.support.mapper.teacher;

import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by xinxin on 26/1/2016.
 */
@Getter
@Setter
public class BookProfileMapper implements Serializable {
    private static final long serialVersionUID = -5922381971348451832L;

    private String id;
    private String name;
    private String imgUrl;
    private Integer level;
    private Integer term;

    public BookProfileMapper(NewBookProfile profile) {
        this.id = profile.getId();
        this.name = profile.getName();
        this.imgUrl = profile.getImgUrl();
        this.level = profile.getClazzLevel();
        this.term = profile.getTermType();
    }
}
