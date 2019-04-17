package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.IndependentOcrProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.SaveOcrRecognitionRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
@ServiceVersion(version = "20190325")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface IndependentOcrService {

    /**
     * 提交识别结果
     * @param userId 用户ID
     * @param request {@link SaveOcrRecognitionRequest}
     * @return
     */
    Map<String, Object> batchProcessOcrResult(Long userId, SaveOcrRecognitionRequest request);

    /**
     * 批量添加识别结果数据
     * @param documents {@link IndependentOcrProcessResult}
     * @return 是否操作成功
     */
    boolean batchProcessOcrResult(Collection<IndependentOcrProcessResult> documents);

    /**
     * 识别报错
     * @param studentId 学生ID
     * @param imgUrl 图片url
     * @param coordinate 小图坐标
     * @param kibanaMap
     * @return
     */
    MapMessage reportError(Long studentId, String imgUrl, String coordinate, ExLinkedHashMap<String, String> kibanaMap);

    /**
     * 查询检查记录
     * @param studentId 学生ID
     * @return
     */
    MapMessage fetchOcrResultList(Long studentId, Integer pageNum, Integer pageSize);

    /**
     * 批量删除检查记录
     * @param processIdList 结果ids
     * @return
     */
    MapMessage deleteResult(List<String> processIdList, Long studentId);

    /**
     * 根据题docIds查询答案和解析
     * @param questionIdList
     * @return
     */
    MapMessage fetchAnswerAndAnalysis(List<String> questionIdList);

    /**
     * 口算错因分析
     */
    MapMessage mentalSymptomAnalysis(String imageUrl, List<String> textList, Long studentId);

    /**
     * 保存学生独立拍照练习册url
     * @param backCoverImgUrlList 练习册背面封皮
     * @param studentId 学生ID
     * @return
     */
    List<Map<String, Object>> saveOcrStudentWorkbook(List<String> backCoverImgUrlList, Long studentId);

    /**
     * 查询我的练习册列表
     * @param studentId 学生ID
     * @return
     */
    MapMessage fetchOcrStudentWorkbook(Long studentId);

    /**
     * 删除我的练习册
     * @param studentId 学生ID
     * @param myWorkbookId 要删除的练习册ID
     * @return
     */
    MapMessage removeOcrStudentWorkbook(Long studentId, String myWorkbookId);
}
