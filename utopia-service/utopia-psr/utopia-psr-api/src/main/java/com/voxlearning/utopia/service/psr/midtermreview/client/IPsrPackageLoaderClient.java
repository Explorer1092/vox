package com.voxlearning.utopia.service.psr.midtermreview.client;

import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.psr.entity.midtermreview.EnglishPackage;

import java.util.List;

/**
 * 期中复习，个性化题包推荐
 * Created by Administrator on 2016/10/9.
 */

public interface IPsrPackageLoaderClient extends IPingable{

    /**
     * 题包获取接口
     *
     * @param bookId    教材id
     * @param groupId   班组id
     * @return List
     */
    List<EnglishPackage> loadPackage(String bookId, Integer groupId);


    /* 测试题包获取接口,for php dubbo*/
    //TODO 上线之后去掉
    List<EnglishPackage> testLoadPackage(String bookId, Integer groupId);

}
