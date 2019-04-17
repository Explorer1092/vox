package com.voxlearning.utopia.agent.mockexam.domain.support;

import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.agent.mockexam.dao.ExamPlanDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPlanEntity;
import com.voxlearning.utopia.agent.mockexam.domain.ExamDomain;
import com.voxlearning.utopia.agent.mockexam.domain.exception.BusinessException;
import com.voxlearning.utopia.agent.mockexam.domain.model.ExamStudentScore;
import com.voxlearning.utopia.agent.mockexam.integration.ExamClient;
import com.voxlearning.utopia.agent.mockexam.integration.FileClient;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamMakeupParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamReplenishParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamScoreQueryParams;
import com.voxlearning.utopia.agent.mockexam.service.dto.input.ExamUploadParams;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试领域层实现
 *
 * @author xiaolei.li
 * @version 2018/8/12
 */
@Service
public class ExamDomainImpl implements ExamDomain {

    @Resource
    ExamPlanDao examPlanDao;

    @Resource
    ExamClient examClient;

    @Resource
    FileClient fileClient;

    @Override
    public Map<Long, String> makeup(ExamMakeupParams params) {

        // 入参校验
        if (null == params.getId() && Long.valueOf(0) == params.getId())
            throw new BusinessException(ErrorCode.EXAM_MAKEUP, "请求参数中id为空");
        String studentIds = params.getStudentIds();
        if (StringUtils.isBlank(studentIds))
            throw new BusinessException(ErrorCode.EXAM_MAKEUP, "请求参数中学生id为空");
        List<Long> sIds;
        try {
            sIds = Arrays.stream(studentIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_MAKEUP, "学生id格式不正确,studentIds=" + params.getStudentIds());
        }

        // 调用客户端
        ExamClient.RetryRequest request = new ExamClient.RetryRequest();
        ExamPlanEntity plan = examPlanDao.findById(params.getId());
        String examId = plan.getExamId();
        if (StringUtils.isBlank(examId))
            throw new BusinessException(ErrorCode.EXAM_MAKEUP, "测评没有关联考试,id = " + params.getId());
        Date now = new Date();
        if (now.after(plan.getTeacherMarkDeadline()))
            throw new BusinessException(ErrorCode.EXAM_MAKEUP, "要在教师批改截止时间之前【补考】");
        request.setExamId(examId);
        request.setMakeUp(true);
        request.setStudentIds(sIds);
        return examClient.retry(request);
    }

    @Override
    public Map<Long, String> replenish(ExamReplenishParams params) {
        // 入参校验
        final Long id = params.getId();
        if (null == id || Long.valueOf(0).equals(id))
            throw new BusinessException(ErrorCode.EXAM_REPLENISH, "请求参数中id为空");
        String studentIds = params.getStudentIds();
        if (StringUtils.isBlank(studentIds))
            throw new BusinessException(ErrorCode.EXAM_REPLENISH, "请求参数中学生id为空");
        List<Long> sIds;
        try {
            sIds = Arrays.stream(studentIds.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXAM_REPLENISH, "学生id格式不正确,studentIds=" + params.getStudentIds());
        }

        // 调用客户端
        ExamClient.RetryRequest request = new ExamClient.RetryRequest();
        Date now = new Date();
        ExamPlanEntity plan = examPlanDao.findById(id);
        String examId = plan.getExamId();
        if (StringUtils.isBlank(examId))
            throw new BusinessException(ErrorCode.EXAM_REPLENISH, "测评没有关联考试,id = " + id);
        if (now.after(plan.getTeacherMarkDeadline()))
            throw new BusinessException(ErrorCode.EXAM_REPLENISH, "要在教师批改截止时间之前【重考】");
        request.setExamId(examId);
        request.setMakeUp(false);
        request.setStudentIds(sIds);
        return examClient.retry(request);
    }

    @Override
    public ExamStudentScore queryScore(ExamScoreQueryParams params) {
        ExamPlanEntity planEntity = examPlanDao.findById(params.getId());
        final String examId = planEntity.getExamId();
        if (StringUtils.isBlank(examId))
            throw new BusinessException(ErrorCode.EXAM_SCORE,
                    String.format("测评计划[id=%s]没有考试id", params.getId()));
        params.setExamId(examId);
        return examClient.queryScore(params);
    }

    @Override
    public Result<Boolean> uploadFile(ExamUploadParams params) {
        MultipartFile inputFile = params.getInputFile();
        if (inputFile.isEmpty()) {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "文件为空");
        }
        if (inputFile.getSize() > 1024 * 1024 * 10) {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "文件大小不能超过10M");
        }
        // 获取文件类型
        String originalFileName = inputFile.getOriginalFilename();
        String ext = StringUtils.substringAfterLast(originalFileName, ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();
        if (!ObjectUtils.equals(ext, "docx") && !Objects.equals(ext, "doc")) {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "文件格式有误请重新上传");
        }
        SupportedFileType fileType;
        try {
            fileType = SupportedFileType.valueOf(ext);
        } catch (Exception ex) {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "不支持此格式文件");
        }
        String fileId = RandomUtils.nextObjectId();
        FileClient.UploadRequest request = new FileClient.UploadRequest();
        request.setInputFile(inputFile);
        request.setFileId(fileId);
        request.setFileName("testPaper-" + fileId + "." + ext);
        request.setContentType(fileType.getContentType());
        InputStream inputStream;
        try {
            inputStream = inputFile.getInputStream();
        } catch (IOException e) {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, "获取文件流异常");
        }
        request.setInputStream(inputStream);
        MapMessage mapMessage = fileClient.upload(request);
        if (mapMessage.isSuccess()) {
            return Result.success(true);
        } else {
            return Result.error(ErrorCode.FILE_UPLOAD_ERROR, mapMessage.getInfo());
        }
    }

}
