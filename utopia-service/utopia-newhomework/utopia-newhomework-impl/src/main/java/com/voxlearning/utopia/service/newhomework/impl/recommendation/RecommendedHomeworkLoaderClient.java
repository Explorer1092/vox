package com.voxlearning.utopia.service.newhomework.impl.recommendation;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.recom.homework.api.loader.IHomeworkRecomLoader;
import lombok.Getter;

import javax.inject.Named;

/**
 * @Description: 作业推荐
 * @author: Mr_VanGogh
 * @date: 2019/4/4 上午9:01
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.recommendation.RecommendedHomeworkLoaderClient")
public class RecommendedHomeworkLoaderClient {

    @Getter
    @ImportService(interfaceClass = IHomeworkRecomLoader.class)
    private IHomeworkRecomLoader homeworkRecomLoader;
}
