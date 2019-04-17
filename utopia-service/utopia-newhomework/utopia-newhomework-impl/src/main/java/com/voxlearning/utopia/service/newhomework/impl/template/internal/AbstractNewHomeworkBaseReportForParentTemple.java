package com.voxlearning.utopia.service.newhomework.impl.template.internal;


import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkReportForParentTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractNewHomeworkBaseReportForParentTemple extends NewHomeworkReportForParentTemple {


    @Override
    protected List<Map<String, Object>> handlerBookInfo(NewHomeworkBook newHomeworkBook) {
        if (newHomeworkBook == null || newHomeworkBook.getPractices() == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new LinkedList<>();
        Set<String> sectionIds = new HashSet<>();
        Set<String> unitIds = new HashSet<>();
        Map<String, String> sectionToUnitMap = new LinkedHashMap<>();
        Set<String> unitQuiz = new HashSet<>();

        for (Map.Entry<ObjectiveConfigType, List<NewHomeworkBookInfo>> entry : newHomeworkBook.getPractices().entrySet()) {
            List<NewHomeworkBookInfo> newHomeworkBookInfoList = entry.getValue();
            if (entry.getKey() == ObjectiveConfigType.UNIT_QUIZ) {
                for (NewHomeworkBookInfo n : newHomeworkBookInfoList) {
                    if (n.getUnitId() != null && !unitIds.contains(n.getUnitId())) {
                        unitQuiz.add(n.getUnitId());
                    }
                }
            } else {
                for (NewHomeworkBookInfo n : newHomeworkBookInfoList) {
                    if (n.getUnitId() != null) {
                        if (n.getSectionId() != null) {
                            sectionIds.add(n.getSectionId());
                            sectionToUnitMap.put(n.getSectionId(), n.getUnitId());
                        }
                        unitIds.add(n.getUnitId());
                    }
                }
            }
        }
        List<String> newBookCatalogIds = new LinkedList<>();
        newBookCatalogIds.addAll(unitIds);
        newBookCatalogIds.addAll(sectionIds);
        Map<String, NewBookCatalog> newBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(newBookCatalogIds);

        Map<String, NewBookCatalog> unitQuizNewBookCatalogMap = unitQuiz.stream()
                .filter(Objects::nonNull)
                .filter(newBookCatalogMap::containsKey)
                .map(newBookCatalogMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NewBookCatalog::getId, Function.identity()));
        Map<String, NewBookCatalog> unitNewBookCatalogMap = unitIds.stream()
                .filter(Objects::nonNull)
                .filter(newBookCatalogMap::containsKey)
                .map(newBookCatalogMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NewBookCatalog::getId, Function.identity()));

        Map<String, NewBookCatalog> sectionNewBookCatalogMap = sectionIds.stream()
                .filter(Objects::nonNull)
                .filter(newBookCatalogMap::containsKey)
                .map(newBookCatalogMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(NewBookCatalog::getId, Function.identity()));

        List<String> lessonIds = sectionNewBookCatalogMap.values()
                .stream()
                .map(o -> o.getAncestors().get(o.getAncestors().size() - 1).getId())
                .collect(Collectors.toList());
        Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);

        for (String sectionId : sectionNewBookCatalogMap.keySet()) {
            Map<String, Object> contentData = new LinkedHashMap<>();
            NewBookCatalog sectionNewBookCatalog = sectionNewBookCatalogMap.get(sectionId);
            NewBookCatalog unitNewBookCatalog = null;
            NewBookCatalog lessonNewBookCatalog = null;
            if (sectionToUnitMap.get(sectionId) != null) {
                unitNewBookCatalog = unitNewBookCatalogMap.get(sectionToUnitMap.get(sectionId));
                if (lessonNewBookCatalogMap != null &&
                        sectionNewBookCatalog != null &&
                        sectionNewBookCatalog.getAncestors() != null &&
                        sectionNewBookCatalog.getAncestors().size() > 0) {
                    lessonNewBookCatalog = lessonNewBookCatalogMap.get(sectionNewBookCatalog.getAncestors().get(sectionNewBookCatalog.getAncestors().size() - 1).getId());
                }
            }
            contentData.put("sectionName", sectionNewBookCatalog != null ? sectionNewBookCatalog.getName() : null);
            contentData.put("sectionRank", sectionNewBookCatalog != null ? sectionNewBookCatalog.getRank() : null);
            contentData.put("lessonRank", lessonNewBookCatalog != null ? lessonNewBookCatalog.getRank() : null);
            contentData.put("unitName", unitNewBookCatalog != null ? unitNewBookCatalog.getName() : null);
            contentData.put("unitRank", unitNewBookCatalog != null ? unitNewBookCatalog.getRank() : null);
            result.add(contentData);
        }
        if (sectionToUnitMap.isEmpty()) {
            for (String unitId : unitIds) {
                NewBookCatalog unitNewBookCatalog = unitNewBookCatalogMap.get(unitId);
                if (unitNewBookCatalog != null) {
                    Map<String, Object> contentData = new LinkedHashMap<>();
                    contentData.put("unitName", unitNewBookCatalog.getName());
                    contentData.put("unitRank", unitNewBookCatalog.getRank());
                    result.add(contentData);
                }
            }
        }
        for (String unitId : unitQuiz) {
            NewBookCatalog unitNewBookCatalog = unitQuizNewBookCatalogMap.get(unitId);
            if (unitNewBookCatalog != null) {
                Map<String, Object> contentData = new LinkedHashMap<>();
                contentData.put("unitName", unitNewBookCatalog.getName());
                contentData.put("unitRank", unitNewBookCatalog.getRank());
                result.add(contentData);
            }
        }

        result.sort((o1, o2) -> {
            int unitCompare = Integer.compare(SafeConverter.toInt(o1.get("unitRank")), SafeConverter.toInt(o2.get("unitRank")));
            if (unitCompare != 0) {
                return unitCompare;
            } else {
                int lessonCompare = Integer.compare(SafeConverter.toInt(o1.get("lessonRank")), SafeConverter.toInt(o2.get("lessonRank")));
                if (lessonCompare != 0) {
                    return lessonCompare;
                } else {
                    return Integer.compare(SafeConverter.toInt(o1.get("sectionRank")), SafeConverter.toInt(o2.get("sectionRank")));
                }
            }
        });
        return result;
    }
}
