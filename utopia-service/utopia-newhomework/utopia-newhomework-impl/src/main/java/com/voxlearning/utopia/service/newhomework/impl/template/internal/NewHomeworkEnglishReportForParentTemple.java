package com.voxlearning.utopia.service.newhomework.impl.template.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.service.content.api.constant.PracticeCategory;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkEnglishReportForParentTemple extends AbstractNewHomeworkBaseReportForParentTemple {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    public Subject getSubject() {
        return Subject.ENGLISH;
    }

    @Override
    protected Set<String> handleHomeworkContent(List<Map<String, Object>> bookInfo) {
        Set<String> homeworkContent = new HashSet<>();
        for (Map infoMap : bookInfo) {
            String moduleName = SafeConverter.toString(infoMap.get("moduleName"), "");
            String unitName = SafeConverter.toString(infoMap.get("unitName"), "");
            String content = null;
            if (StringUtils.isNotBlank(moduleName)) {
                content = moduleName;
            }
            if (StringUtils.isNotBlank(unitName)) {
                if (StringUtils.isNotEmpty(content)) {
                    content = content + " " + unitName;
                } else {
                    content = unitName;
                }
            }
            if (StringUtils.isNotBlank(content)) {
                homeworkContent.add(content);
            }
        }
        return homeworkContent;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> handlePracticeList(List<Map<String, Object>> newHomeworkInfo, NewHomework newHomework) {
        List<String> practiceContentList = new ArrayList<>();
        int applicationQuestionNum = 0;
        List<String> lastTypeContent = new LinkedList<>();
        for (Map contentMap : newHomeworkInfo) {
            String objectiveConfigTypeName = SafeConverter.toString(contentMap.get("objectiveConfigTypeName"));
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeName);
            //基础练习
            if (objectiveConfigType == ObjectiveConfigType.BASIC_APP) {
                List<Map<String, Object>> appsInformation = new ArrayList<>();
                if (contentMap.get("appsInformation") != null) {
                    appsInformation = (List<Map<String, Object>>) contentMap.get("appsInformation");
                }
                for (Map appsInfoMap : appsInformation) {
                    int sentencesNum = SafeConverter.toInt(appsInfoMap.get("sentencesNum"));
                    String lessonName = SafeConverter.toString(appsInfoMap.get("lessonName"), "");
                    Map<Integer, List<String>> translateData = (Map<Integer, List<String>>) appsInfoMap.get("translateData");
                    for (Integer practiceCategory : translateData.keySet()) {
                        StringBuilder basicAppContent = new StringBuilder("会");
                        List<String> contentTypeList = translateData.get(practiceCategory);
                        for (String contentType : contentTypeList) {
                            basicAppContent.append(contentType).append("、");
                        }
                        basicAppContent = new StringBuilder(basicAppContent.substring(0, basicAppContent.length() - 1) + lessonName);
                        if (practiceCategory == 1) {
                            basicAppContent.append("共").append(sentencesNum).append("个单词");
                        } else if (practiceCategory == 2) {
                            basicAppContent.append("的单词和句子");
                        } else if (practiceCategory == 3) {
                            basicAppContent.append("的段落或对话");
                        }
                        practiceContentList.add(basicAppContent.toString());
                    }
                }
            } else if (objectiveConfigType == ObjectiveConfigType.READING || objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
                //绘本阅读
                StringBuilder content = new StringBuilder(objectiveConfigType.getValue() + "：");
                List<Map<String, Object>> appsInformation = new ArrayList<>();
                if (contentMap.get("appsInformation") != null) {
                    appsInformation = (List<Map<String, Object>>) contentMap.get("appsInformation");
                }
                for (Map<String, Object> appsInfoMap : appsInformation) {
                    String bookName = SafeConverter.toString(appsInfoMap.get("pictureBookName"), "");
                    content.append(StringUtils.isNotBlank(bookName) ? bookName + "/" : "");
                }
                content = new StringBuilder(content.substring(0, content.length() - 1));
                practiceContentList.add(content.toString());
            } else if (objectiveConfigType == ObjectiveConfigType.ORAL_PRACTICE) {
                lastTypeContent.add(objectiveConfigType.getValue() + "共" + SafeConverter.toInt(contentMap.get("questionNum")) + "题");
            } else if (objectiveConfigType == ObjectiveConfigType.EXAM ||
                    objectiveConfigType == ObjectiveConfigType.LISTEN_PRACTICE ||
                    objectiveConfigType == ObjectiveConfigType.UNIT_QUIZ) {
                applicationQuestionNum += SafeConverter.toInt(contentMap.get("questionNum"));
            } else {
                if (objectiveConfigType == ObjectiveConfigType.LS_KNOWLEDGE_REVIEW) {
                    String content = "\"听\"\"说\"未达标知识练习";
                    int questionNum = SafeConverter.toInt(contentMap.get("questionNum"));
                    content += "共" + questionNum + "题";
                    lastTypeContent.add(content);
                } else if (objectiveConfigType == ObjectiveConfigType.RW_KNOWLEDGE_REVIEW) {
                    String content = "\"读\"\"写\"未达标知识练习";
                    int questionNum = SafeConverter.toInt(contentMap.get("questionNum"));
                    content += "共" + questionNum + "题";
                    lastTypeContent.add(content);
                } else if (objectiveConfigType == ObjectiveConfigType.NATURAL_SPELLING) {
                    NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.NATURAL_SPELLING);
                    if (target != null && CollectionUtils.isNotEmpty(target.getApps())) {
                        for (NewHomeworkApp newHomeworkApp : target.getApps()) {
                            if (newHomeworkApp.getCategoryId() != null) {
                                switch (newHomeworkApp.getCategoryId()) {
                                    case 10325:
                                        practiceContentList.add("字母练习：会认读、书写字母，掌握笔画顺序");
                                        break;
                                    case 10326:
                                        practiceContentList.add("单词拼读：会根据规则及发音规律拼读单词");
                                        break;
                                    case 10327:
                                        practiceContentList.add("趣味拼写：会根据发音规律完成单词拼写练习");
                                        break;
                                    case 10328:
                                        practiceContentList.add("读音归类：会根据发音规律完成读音归类练习");
                                        break;
                                    case 10329:
                                        practiceContentList.add("绕口令：会读语音绕口令");
                                        break;

                                }
                            }
                        }
                    }
                } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
                    int videoNum = 0;
                    StringBuilder content = new StringBuilder(objectiveConfigType.getValue());
                    NewHomeworkPracticeContent practiceContent = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.DUBBING);
                    if (practiceContent != null && CollectionUtils.isNotEmpty(practiceContent.getApps())) {
                        List<NewHomeworkApp> apps = practiceContent.getApps();
                        videoNum = apps.size();
                    }
                    content.append("共").append(videoNum).append("集");
                    lastTypeContent.add(content.toString());
                } else {
                    String content = objectiveConfigType != null ? objectiveConfigType.getValue() : "";
                    int questionNum = SafeConverter.toInt(contentMap.get("questionNum"));
                    content += "共" + questionNum + "题";
                    lastTypeContent.add(content);
                }
            }
        }
        if (applicationQuestionNum != 0) {
            practiceContentList.add("应试练习共" + applicationQuestionNum + "题");
        }
        if (CollectionUtils.isNotEmpty(lastTypeContent)) {
            practiceContentList.addAll(lastTypeContent);
        }
        return practiceContentList;
    }

    @Override
    protected List<Map<String, Object>> handlerNewHomework(NewHomework newHomework) {
        List<Map<String, Object>> result = new LinkedList<>();
        List<NewHomeworkPracticeContent> newHomeworkPracticeContents = newHomework.getPractices();
        for (NewHomeworkPracticeContent p : newHomeworkPracticeContents) {
            Map<String, Object> contentData = new LinkedHashMap<>();
            result.add(contentData);
            contentData.put("objectiveConfigTypeName", p.getType().name());
            if (p.getType() == ObjectiveConfigType.BASIC_APP) {
                List<Map<String, Object>> appsInformation = new LinkedList<>();
                contentData.put("appsInformation", appsInformation);
                List<Map<String, Object>> baseAppData = new LinkedList<>();
                List<NewHomeworkApp> apps = p.getApps();
                Set<String> lessonIds = apps
                        .stream()
                        .map(NewHomeworkApp::getLessonId)
                        .collect(Collectors.toSet());
                Map<String, NewBookCatalog> lessonNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
                for (NewHomeworkApp app : apps) {
                    Map<String, Object> appData = new LinkedHashMap<>();
                    PracticeType practiceType = practiceLoaderClient.loadPractice(app.getPracticeId());
                    PracticeCategory practiceCategory = PracticeCategory.getPracticeCategory(practiceType.getCategoryName());
                    NewBookCatalog lessonNewBookCatalog = lessonNewBookCatalogMap.get(app.getLessonId());
                    appData.put("type", practiceCategory != null ? practiceCategory.getType() : null);
                    appData.put("contentType", practiceCategory != null ? practiceCategory.getContentType() : null);
                    appData.put("sentencesNum", app.getQuestions().size());
                    appData.put("lessonId", app.getLessonId());
                    appData.put("lessonName", lessonNewBookCatalog.getAlias());
                    baseAppData.add(appData);
                }
                //按照lessonId 和 type group by
                Map<String, List<Map<String, Object>>> lessonData = baseAppData
                        .stream()
                        .collect(Collectors
                                .groupingBy(o -> (String) (o.get("lessonId"))));
                for (String lessonId : lessonIds) {
                    Map<String, Object> appBylessonData = new LinkedHashMap<>();
                    List<Map<String, Object>> typeListData = lessonData.get(lessonId);
                    Map<String, List<Map<String, Object>>> typeData = typeListData
                            .stream()
                            .collect(Collectors
                                    .groupingBy(o -> (String) (o.get("type"))));
                    Map<Integer, List<String>> translateData = new LinkedHashMap<>();
                    for (String type : typeData.keySet()) {
                        List<Map<String, Object>> oTypeData = typeData.get(type);
                        Set<Integer> contentTypes = oTypeData
                                .stream()
                                .map(o -> SafeConverter.toInt(o.get("contentType")))
                                .collect(Collectors.toSet());
                        for (Integer in : contentTypes) {
                            if (translateData.containsKey(in)) {
                                List<String> l = translateData.get(in);
                                l.add(type);
                            } else {
                                List<String> l = new LinkedList<>();
                                l.add(type);
                                translateData.put(in, l);
                            }
                        }
                    }
                    appBylessonData.put("sentencesNum", typeListData.get(0).get("sentencesNum"));
                    appBylessonData.put("lessonId", lessonId);
                    appBylessonData.put("translateData", translateData);
                    appBylessonData.put("lessonName", typeListData.get(0).get("lessonName"));
                    appsInformation.add(appBylessonData);
                }
            } else if (p.getType() == ObjectiveConfigType.READING) {
                List<Map<String, Object>> appsInformation = new LinkedList<>();
                contentData.put("appsInformation", appsInformation);
                List<String> pictureBookIds = p
                        .getApps()
                        .stream()
                        .map(NewHomeworkApp::getPictureBookId)
                        .collect(Collectors.toList());
                Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds);
                List<NewHomeworkApp> apps = p.getApps();
                for (NewHomeworkApp app : apps) {
                    Map<String, Object> pictureBookInfo = new LinkedHashMap<>();
                    PictureBook pictureBook = pictureBookMap.get(app.getPictureBookId());
                    if (pictureBook == null)
                        continue;
                    pictureBookInfo.put("pictureBookName", pictureBook.getName());
                    appsInformation.add(pictureBookInfo);
                }
            } else if (p.getType() == ObjectiveConfigType.LEVEL_READINGS) {
                List<Map<String, Object>> appsInformation = new LinkedList<>();
                contentData.put("appsInformation", appsInformation);
                List<String> pictureBookIds = p
                        .getApps()
                        .stream()
                        .map(NewHomeworkApp::getPictureBookId)
                        .collect(Collectors.toList());
                Map<String, PictureBookPlus> pictureBookMap = pictureBookPlusServiceClient.loadByIds(pictureBookIds);
                List<NewHomeworkApp> apps = p.getApps();
                for (NewHomeworkApp app : apps) {
                    Map<String, Object> pictureBookInfo = new LinkedHashMap<>();
                    PictureBookPlus pictureBook = pictureBookMap.get(app.getPictureBookId());
                    if (pictureBook == null)
                        continue;
                    pictureBookInfo.put("pictureBookName", pictureBook.getEname());
                    appsInformation.add(pictureBookInfo);
                }

            } else {
                contentData.put("questionNum", p.processNewHomeworkQuestion(true).size());
            }
        }
        return result;
    }

    @Override
    protected List<Map<String, Object>> handlerBookInfo(NewHomeworkBook newHomeworkBook) {
        if (newHomeworkBook == null || newHomeworkBook.getPractices() == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new LinkedList<>();
        Set<String> unitIds = new HashSet<>();
        for (List<NewHomeworkBookInfo> newHomeworkBookInfoList : newHomeworkBook.getPractices().values()) {
            for (NewHomeworkBookInfo n : newHomeworkBookInfoList) {
                if (n.getUnitId() != null) {
                    unitIds.add(n.getUnitId());
                }
            }
        }
        Map<String, NewBookCatalog> unitNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
        Set<String> moduleIds = new HashSet<>();
        Map<String, String> unitIdToModuleId = new LinkedHashMap<>();
        for (NewBookCatalog cl : unitNewBookCatalogMap.values()) {
            if (cl.getAncestors() != null) {
                for (NewBookCatalogAncestor nbcla : cl.getAncestors()) {
                    if (nbcla.getNodeType().equals("MODULE")) {
                        moduleIds.add(nbcla.getId());
                        unitIdToModuleId.put(cl.getId(), nbcla.getId());
                        break;
                    }
                }
            }
        }
        Map<String, NewBookCatalog> moduleNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(moduleIds);
        for (String unitId : unitIds) {
            NewBookCatalog unitNewBookCatalog = unitNewBookCatalogMap.get(unitId);
            NewBookCatalog moduleNewBookCatalog = null;
            if (unitIdToModuleId.get(unitId) != null) {
                moduleNewBookCatalog = moduleNewBookCatalogMap.get(unitIdToModuleId.get(unitId));
            }
            Map<String, Object> englishContent = new LinkedHashMap<>();
            englishContent.put("unitName", unitNewBookCatalog.getAlias());
            englishContent.put("unitId", unitId);
            englishContent.put("moduleName", moduleNewBookCatalog != null ? moduleNewBookCatalog.getAlias() : null);
            englishContent.put("unitRank", unitNewBookCatalog.getRank());
            englishContent.put("moduleRank", moduleNewBookCatalog != null ? moduleNewBookCatalog.getRank() : null);
            result.add(englishContent);
        }
        result.sort((o1, o2) -> {
            int moduleCompare = Integer.compare(SafeConverter.toInt(o1.get("moduleRank")), SafeConverter.toInt(o2.get("moduleRank")));
            if (moduleCompare != 0) {
                return moduleCompare;
            } else {
                return Integer.compare(SafeConverter.toInt(o1.get("unitRank")), SafeConverter.toInt(o2.get("unitRank")));
            }
        });
        return result;
    }


}
