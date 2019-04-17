package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.entity.ChipsEncourageVideo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author guangqing
 * @since 2019/3/26
 */
@ServiceVersion(version = "20190326")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface UploadVideoToOOS {
    /**
     * 关键词点评视频
     * 文件名 #word#.mp4
     * @param path
     */
    MapMessage updateKeyWordVideo(String path);

    /**
     * 单词列表的语态、时态等变形反查
     * @param path
     */
    MapMessage updateKeyWordStem(String path);

    /**
     * 鼓励语上传
     * @param path
     * @return
     */
    MapMessage updateEncourage(String path);

    /**
     * upsert 关键字原型
     * @param id
     * @param prototype
     */
    MapMessage upsertChipsKeywordPrototype(String id, String prototype);

    /**
     * upsert 关键字视频
     * @param id
     * @param video
     * @return
     */
    MapMessage upsertChipsKeywordVideo(String id, String video);

    /**
     * upsert 鼓励语视频
     * @param id
     * @param video
     * @param type
     * @param unitType
     * @param level
     * @return
     */
    MapMessage upsertChipsEncourageVideo(String id,String video, String type, String unitType, String level);

    /**
     * 鼓励语点评视频
     * 从测试环境数据库，调用staging环境 dp接口，更新到线上数据库
     * @return
     */
    MapMessage exportChipsEncourageVideo() ;

    /**
     * 关键字点评视频
     * 从测试环境数据库，调用staging环境 dp接口，更新到线上数据库
     * @return
     */
    MapMessage exportChipsKeywordVideo() ;

    /**
     * 关键字原型
     * 从测试环境数据库，调用staging环境 dp接口，更新到线上数据库
     * @return
     */
    MapMessage exportChipsKeywordPrototype() ;

    /**
     * 每个班级的用户购买产品时的支付金额
     * @param clazzId
     * @param lessThan
     * @return
     */
    Map<Long, Object> paidAmount(Long clazzId, Double lessThan);

    /**
     * 统计没有点评数据的数据
     * @param clazzId
     * @param unitId
     * @return
     */
    MapMessage statNoRemarkVideo(Long clazzId, String unitId);

    /**
     *  /**
     * 统计点评数据的数据
     * @param clazzId
     * @param unitId
     * @return
     */
    MapMessage statNotFullRemarkVideo(Long clazzId, String unitId);

    /**
     * 删除关键词点评视频
     * @param keyword
     * @return
     */
    MapMessage removeKeywordVideo(String keyword);

    /**
     * 删除关键词原型
     * @param keyword
     * @return
     */
    MapMessage removeKeywordPrototype(String keyword) ;

    /**
     * 删除鼓励语视频
     * @param id
     * @return
     */
    MapMessage removeEncourageVideo(String id) ;

    /**
     * 每个单元完课，确没有用户视频的用户
     * @param clazzId
     * @param unitId
     * @return
     */
    MapMessage finishButNoUserVideo(Long clazzId, String unitId);
}
