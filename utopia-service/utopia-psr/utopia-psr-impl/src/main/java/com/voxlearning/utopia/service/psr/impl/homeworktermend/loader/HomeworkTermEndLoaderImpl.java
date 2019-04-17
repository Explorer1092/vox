package com.voxlearning.utopia.service.psr.impl.homeworktermend.loader;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.psr.homeworktermend.loader.HomeworkTermEndLoader;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.EnglishQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathMentalQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathQuestionBox;
import com.voxlearning.utopia.service.psr.impl.service.HomeWorkTermendService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016-05-12
 */
@Named
@ExposeService(interfaceClass = HomeworkTermEndLoader.class)
public class HomeworkTermEndLoaderImpl implements HomeworkTermEndLoader {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private HomeWorkTermendService homeWorkTermendService;
    @Inject private NewContentLoaderClient newContentLoaderClient;


    @Override
    public Map<String, EnglishQuestionBox> loadEnglishQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return homeWorkTermendService.loadEnglishQuestionBoxs(boxIds);
    }

    @Deprecated
    @Override
    public Map<String, MathMentalQuestionBox> loadMathMentalQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return homeWorkTermendService.loadMathMentalQuestionBoxs(boxIds);
    }

    @Override
    public Map<String, MathQuestionBox> loadMathQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return homeWorkTermendService.loadMathQuestionBoxs(boxIds);
    }

    @Override
    public List<EnglishQuestionBox> pushEnglishQuestionBoxes(List<Long> unitIds, Long userId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return homeWorkTermendService.getEnglishQuestionBoxes(unitIds);
    }

    @Override
    public List<MathQuestionBox> pushMathQuestionBoxes(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        //根据学校尾号选择题包版本
        long algoVersion = 0;
        if (school != null) {
            long schoolId = school.getId();
            algoVersion = (int) schoolId % 10 % 3;
        }
        List<String> wrappedUnitds = Lists.newArrayList();
        for (String unitId : unitIds) {
            String wrapped = unitId + "_algo_" + algoVersion;
            wrappedUnitds.add(wrapped);
        }
        return homeWorkTermendService.getMathQuestionBoxes(wrappedUnitds);
    }

    @Override
    public List<MathMentalQuestionBox> pushMathMentalQuestionBoxes(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return homeWorkTermendService.getMathMentalQuestionBoxes(unitIds);
    }

    @Override
    public List<MathMentalQuestionBox> pushMathMentalQuestionBoxesByTermEndUnit(List<String> unitIdList, Long teacherId) {
        List<MathMentalQuestionBox> result = Lists.newArrayList();
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIdList);
        NewBookCatalog newBookCatalog = null;
        if (unitIdList != null && unitIdList.size() == 1 && newBookCatalogMap != null) {
            newBookCatalog = newBookCatalogMap.getOrDefault(unitIdList.get(0), null);
        }
        if (newBookCatalog != null && Objects.equals(2, newBookCatalog.getNodeAttr())) {
            String termEndUnitId = unitIdList.get(0);
            NewBookCatalog catalog = newContentLoaderClient.loadBookCatalogByCatalogId(termEndUnitId);
            if (catalog == null || StringUtils.isBlank(catalog.getParentId())) {
                return Collections.emptyList();
            }
            String bookId = catalog.getParentId();
            List<String> unitIds = newContentLoaderClient.loadChildren(Collections.singletonList(bookId), BookCatalogType.UNIT).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(NewBookCatalog::getId)
                    .collect(Collectors.toList());

            List<MathMentalQuestionBox> list = pushMathMentalQuestionBoxes(unitIds, teacherId);

            for (MathMentalQuestionBox box : list) {
                box.setUnitId(termEndUnitId);
                result.add(box);

            }
        } else {
            result = pushMathMentalQuestionBoxes(unitIdList, teacherId);
        }
        return result;
    }

    @Override
    public List<MathQuestionBox> pushMathQuestionBoxesByTermEndUnit(List<String> unitIdList, Long teacherId) {
        List<MathQuestionBox> result;

        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIdList);
        NewBookCatalog newBookCatalog = null;
        if (unitIdList != null && unitIdList.size() == 1 && newBookCatalogMap != null) {
            newBookCatalog = newBookCatalogMap.getOrDefault(unitIdList.get(0), null);
        }

        if (newBookCatalog != null && Objects.equals(2, newBookCatalog.getNodeAttr())) {
            String termEndUnitId = unitIdList.get(0);
            NewBookCatalog catalog = newContentLoaderClient.loadBookCatalogByCatalogId(termEndUnitId);
            if (catalog == null || StringUtils.isBlank(catalog.getParentId())) {
                return Collections.emptyList();
            }
            String bookId = catalog.getParentId();
            List<String> unitIds = newContentLoaderClient.loadChildren(Collections.singletonList(bookId), BookCatalogType.UNIT).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(NewBookCatalog::getId)
                    .collect(Collectors.toList());

            List<MathQuestionBox> list = pushMathQuestionBoxes(unitIds, teacherId);

            result = new ArrayList<>();
            for (MathQuestionBox box : list) {
                box.setUnitId(termEndUnitId);
                result.add(box);
            }
        } else {
            result = pushMathQuestionBoxes(unitIdList, teacherId);
        }
        return result;
    }
}
