package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.ExLinkedHashMap;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.IndependentOcrProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.request.SaveOcrRecognitionRequest;
import com.voxlearning.utopia.service.newhomework.api.service.IndependentOcrService;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/12/25
 */
public class IndependentOcrServiceClient implements IndependentOcrService {

    @Getter
    @ImportService(interfaceClass = IndependentOcrService.class)
    private IndependentOcrService remoteReference;

    @Override
    public Map<String, Object> batchProcessOcrResult(Long userId, SaveOcrRecognitionRequest request) {
        return remoteReference.batchProcessOcrResult(userId, request);
    }

    @Override
    public boolean batchProcessOcrResult(Collection<IndependentOcrProcessResult> documents) {
        return remoteReference.batchProcessOcrResult(documents);
    }

    @Override
    public MapMessage reportError(Long studentId, String imgUrl, String coordinate, ExLinkedHashMap<String, String> kibanaMap) {
        return remoteReference.reportError(studentId, imgUrl, coordinate, kibanaMap);
    }

    @Override
    public MapMessage fetchOcrResultList(Long studentId, Integer pageNum, Integer pageSize) {
        return remoteReference.fetchOcrResultList(studentId, pageNum, pageSize);
    }

    @Override
    public MapMessage deleteResult(List<String> processIdList, Long studentId) {
        return remoteReference.deleteResult(processIdList, studentId);
    }

    @Override
    public MapMessage fetchAnswerAndAnalysis(List<String> questionIdList) {
        return remoteReference.fetchAnswerAndAnalysis(questionIdList);
    }

    @Override
    public MapMessage mentalSymptomAnalysis(String imgUrl, List<String> textList, Long studentId) {
        return remoteReference.mentalSymptomAnalysis(imgUrl, textList, studentId);
    }

    @Override
    public List<Map<String, Object>> saveOcrStudentWorkbook(List<String> backCoverImgUrlList, Long studentId) {
        return remoteReference.saveOcrStudentWorkbook(backCoverImgUrlList, studentId);
    }

    @Override
    public MapMessage fetchOcrStudentWorkbook(Long studentId) {
        return remoteReference.fetchOcrStudentWorkbook(studentId);
    }

    @Override
    public MapMessage removeOcrStudentWorkbook(Long studentId, String myWorkbookId) {
        return remoteReference.removeOcrStudentWorkbook(studentId, myWorkbookId);
    }
}
