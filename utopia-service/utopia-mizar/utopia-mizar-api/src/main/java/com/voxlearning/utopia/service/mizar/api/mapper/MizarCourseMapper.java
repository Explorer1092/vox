package com.voxlearning.utopia.service.mizar.api.mapper;

import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarCourse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Summer Yang on 2016/9/23.
 */
@Data
public class MizarCourseMapper implements Serializable {

    private static final long serialVersionUID = 3175645460920530708L;
    private String id;
    private String title;
    private String subTitle;
    private String background;
    private String description;
    private String redirectUrl;
    private MizarCourse.Status status;
    private String keynoteSpeaker;
    private long readCount;
    private List<String> tags;
    private String category;
    private Boolean soldOut;
    private String price;
    private String speakerAvatar;
    private String classTime;
    private String color;
}