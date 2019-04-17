package com.voxlearning.washington.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 自学工具广告配置
 *
 * @author jiangpeng
 * @since 2016-11-29 下午4:57
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SelfStudyAdConfig {

    List<SelfStudyAdInfo> selfStudyAdInfoList;
    List<String> positionList;
    List<String> bookIdList;
}
