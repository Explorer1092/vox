package com.voxlearning.utopia.service.psr.mathnewkp.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.entity.mathnewkp.MathQuestion;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/20.
 */
public interface IPsrQuestionsLoaderClient extends IPingable{

    /**
     * 根据知识点获取对应题目接口
     *
     * @param bookId    教材id
     * @param unitId    单元id
     * @param knowledgePoints  该单元下的知识点id
     * @param count  每个知识点下挂载题目的最大数量
     * @return Map  map{key->KPid,value->[{eid,et},{eid,et}]}
     */
    Map<String, List<MathQuestion>> loadMathQuestionsByKp(String bookId, String unitId, List<String> knowledgePoints, Integer count );


    /* 测试接口,for php dubbo*/
    //TODO 上线之后去掉
    Map<String, List<MathQuestion>> testLoadMathQuestionsByKp(String unitId, List<String> knowledgePoints);

}
