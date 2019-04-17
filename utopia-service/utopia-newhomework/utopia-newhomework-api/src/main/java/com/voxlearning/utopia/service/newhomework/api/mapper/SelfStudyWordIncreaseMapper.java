package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * clazzGroupId:{
 * 　　bookId1:{
 * 　　　　unitId1:[kpid数组],
 * 　　　　unitId2:[kpid数组],
 * 　　},
 * 　　bookId2:{
 * 　　　　unitId3:[kpid数组];
 * 　　}
 * }
 *
 * @author xuesong.zhang
 * @since 2017/2/15
 */
@Getter
@Setter
public class SelfStudyWordIncreaseMapper implements Serializable {

    private static final long serialVersionUID = 5110598924509241885L;

    private Long groupId;
    private Map<String, Map<String, List<String>>> bookToKpMap;
}
