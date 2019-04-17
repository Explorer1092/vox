package com.voxlearning.washington.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 自学工具广告信息
 *
 * @author jiangpeng
 * @since 2016-11-29 下午4:56
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SelfStudyAdInfo {

    private String imgUrl;
    private String jumpUrl;
    private String content;
}
