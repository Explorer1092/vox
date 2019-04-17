package com.voxlearning.utopia.admin.service.experiment;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.admin.dao.experiment.*;
import com.voxlearning.utopia.admin.entity.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/16
 */
@Service
public class CourseQualityMonitorService {

    @Inject
    private BookCourseAnalysisResultDao bookCourseAnalysisResultDao;
    @Inject
    private BookVariantCourseAnalysisResultDao bookVariantCourseAnalysisResultDao;
    @Inject
    private VariantCourseAnalysisResultDao variantCourseAnalysisResultDao;
    @Inject
    private CoursePageAnalysisResultDao coursePageAnalysisResultDao;
    @Inject
    private CourseAnswerAnalysisResultDao courseAnswerAnalysisResultDao;

    public List<BookCourseAnalysisResult> findAllBookCourseAnalysisResult() {
        return bookCourseAnalysisResultDao.findAllBookCourseAnalysisResult();
    }

    public List<BookVariantCourseAnalysisResult> finaBookVariantCourseAnalysisResultBySeriesIdBookId(String seriesId, String bookId) {
        return bookVariantCourseAnalysisResultDao.finaBookVariantCourseAnalysisResultBySeriesIdBookId(seriesId, bookId);
    }

    public List<VariantCourseAnalysisResult> findVariantCourseAnalysisResult(String seriesId, String bookId, String unitId, String sectionId, String variantId) {
        return variantCourseAnalysisResultDao.findVariantCourseAnalysisResult(seriesId, bookId, unitId, sectionId, variantId);
    }

    public List<CoursePageAnalysisResult> findCoursePageAnalysisResult(String seriesId, String bookId, String unitId, String sectionId, String variantId, String courseId, String preId, String postId) {
        return coursePageAnalysisResultDao.findCoursePageAnalysisResult(seriesId, bookId, unitId, sectionId, variantId, courseId, preId, postId);
    }

    public List<CourseAnswer> findCourseAnswerAnalysisResult(String seriesId, String bookId, String unitId, String sectionId, String variantId, String courseId, String preId, String postId) {
        List<CourseAnswerAnalysisResult> courseAnswerAnalysisResultList = courseAnswerAnalysisResultDao.findCourseAnswerAnalysisResult(seriesId, bookId, unitId, sectionId, variantId, courseId, preId, postId);
        return convertCourseAnswerAnalysisResultList(courseAnswerAnalysisResultList);
    }

    public  List<CourseAnswer> convertCourseAnswerAnalysisResultList(List<CourseAnswerAnalysisResult> courseAnswerAnalysisResultList) {
        List<CourseAnswer> list = new ArrayList<>();
        if (courseAnswerAnalysisResultList == null || CollectionUtils.isEmpty(courseAnswerAnalysisResultList)) {
            return list;
        }
        courseAnswerAnalysisResultList.forEach(q -> {
            List<Map<String, Object>> answerList = convertCourseAnswerAnalysisResult(q);
            if (answerList == null || CollectionUtils.isEmpty(answerList)) {
                return;
            }
            CourseAnswer courseAnswer = new CourseAnswer();
            courseAnswer.setAnswerList(answerList);
            courseAnswer.setCourseAnswerTimeAvg(q.getCourseAnswerTimeAvg());
            courseAnswer.setPage(q.getPage());
            courseAnswer.setAnswerSize(answerList.size());
            list.add(courseAnswer);
        });
        return list;
    }

    public  List<Map<String, Object>> convertCourseAnswerAnalysisResult(CourseAnswerAnalysisResult courseAnswerAnalysisResult) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (courseAnswerAnalysisResult == null || CollectionUtils.isEmpty(courseAnswerAnalysisResult.getAnswerList())) {
            return list;
        }
        courseAnswerAnalysisResult.getAnswerList().forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("userAnswer", e.getUserAnswer());
            bean.put("result",e.getResult());
            bean.put("rate", e.getRate());
            bean.put("answerNum", e.getAnswerNum());
            list.add(bean);
        });
        return list;
    }

    public  List<Map<String, Object>> convertBookCourseAnalysisList(List<BookCourseAnalysisResult> bookCourseAnalysisResultList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(bookCourseAnalysisResultList)) {
            return list;
        }
        bookCourseAnalysisResultList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("seriesId", e.getSeriesId());
            bean.put("bookId",e.getBookId());
            bean.put("seriesName", e.getSeriesName());
            bean.put("bookName",e.getBookName());
            bean.put("courseNumOnline",e.getCourseNumOnline());
            bean.put("courseNumUsed",e.getCourseNumUsed());
            bean.put("courseCompleteRateAvg",e.getCourseCompleteRateAvg());
            bean.put("courseRightRateAvg",e.getCourseRightRateAvg());
            list.add(bean);
        });
        return list;
    }

    public List<Map<String, Object>> convertBookVariantCourseAnalysisResult(List<BookVariantCourseAnalysisResult> bookVariantCourseAnalysisResultList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(bookVariantCourseAnalysisResultList)) {
            return list;
        }
        bookVariantCourseAnalysisResultList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("seriesId", e.getSeriesId());
            bean.put("bookId",e.getBookId());
            bean.put("unitId", e.getUnitId());
            bean.put("sectionId",e.getSectionId());
            bean.put("variantId",e.getVariantId());
            bean.put("unitName",e.getUnitName());
            bean.put("sectionName",e.getSectionName());
            bean.put("variantName",e.getVariantName());
            bean.put("courseCompleteRateAvg",e.getCourseCompleteRateAvg());
            bean.put("postCompleteRateAvg",e.getPostCompleteRateAvg());
            bean.put("controlPostCompleteRate",e.getControlPostCompleteRate());
            bean.put("courseRightRateAvg",e.getCourseRightRateAvg());
            bean.put("controlCourseRightRate",e.getControlCourseRightRate());
            list.add(bean);
        });
        return list;
    }

    public List<Map<String, Object>> convertVariantCourseAnalysisResult(List<VariantCourseAnalysisResult> bookVariantCourseAnalysisResultList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(bookVariantCourseAnalysisResultList)) {
            return list;
        }
        bookVariantCourseAnalysisResultList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("seriesId", e.getSeriesId());
            bean.put("bookId",e.getBookId());
            bean.put("unitId", e.getUnitId());
            bean.put("sectionId",e.getSectionId());
            bean.put("variantId",e.getVariantId());
            bean.put("preId",e.getPreId());
            bean.put("postId",e.getPostId());
            bean.put("courseId",e.getCourseId());
            bean.put("courseName",e.getCourseName());
            bean.put("courseTargetNum",e.getCourseTargetNum());
            bean.put("courseBeginNum",e.getCourseBeginNum());
            bean.put("courseFinishNum",e.getCourseFinishNum());
            bean.put("courseCompleteRate",e.getCourseCompleteRate());
            bean.put("postDoNum",e.getPostDoNum());
            bean.put("postDoNumFilter",e.getPostDoNumFilter());
            bean.put("postCompleteRate",e.getPostCompleteRate());
            bean.put("postRightRate",e.getPostRightRate());
            bean.put("createTime",e.getCreateTime());
            bean.put("updateTime",e.getUpdateTime());
            bean.put("postRightTimeSum",e.getPostRightTimeSum());
            bean.put("postRightSum",e.getPostRightSum());
            list.add(bean);
        });
        return list;
    }

    public List<Map<String, Object>> convertCoursePageAnalysisResult(List<CoursePageAnalysisResult> coursePageAnalysisResultList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(coursePageAnalysisResultList)) {
            return list;
        }
        coursePageAnalysisResultList.forEach(e -> {
            Map<String, Object> bean = new HashMap<>();
            bean.put("seriesId", e.getSeriesId());
            bean.put("bookId",e.getBookId());
            bean.put("unitId", e.getUnitId());
            bean.put("sectionId",e.getSectionId());
            bean.put("variantId",e.getVariantId());
            bean.put("courseId",e.getCourseId());
            bean.put("page",e.getPage());
            bean.put("loadNum",e.getLoadNum());
            bean.put("quitNum",e.getQuitNum());
            bean.put("preNum",e.getPreNum());
            bean.put("postNum",e.getPostNum());
            bean.put("stayNum",e.getStayNum());
            bean.put("stayTimeTotal",e.getStayTimeTotal());
            bean.put("stayTimeAvg",e.getStayTimeAvg());
            bean.put("stayTimeDefult",e.getStayTimeDefult());
            bean.put("createTime",e.getCreateTime());
            bean.put("updateTime",e.getUpdateTime());
            list.add(bean);
        });
        return list;
    }

}
