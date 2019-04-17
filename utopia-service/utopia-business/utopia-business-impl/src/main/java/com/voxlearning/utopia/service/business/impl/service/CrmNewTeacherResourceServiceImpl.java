package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.business.api.CrmNewTeacherResourceService;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.impl.dao.NewTeacherResourceDao;
import com.voxlearning.utopia.service.business.impl.dao.buffer.version.NewTeacherResourceWrapperVersion;
import com.voxlearning.utopia.service.business.impl.utils.NewTeacherResourceUtils;
import com.voxlearning.utopia.service.campaign.api.TeacherCoursewareContestService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * CRM 专用, 不走 cache, 及时更新
 */
@Named
@Slf4j
@ExposeService(interfaceClass = CrmNewTeacherResourceService.class)
public class CrmNewTeacherResourceServiceImpl implements CrmNewTeacherResourceService {

    @Inject
    private NewTeacherResourceDao newTeacherResourceDao;
    @Inject
    private NewTeacherResourceWrapperVersion teacherResourceBufferVersion;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    public List<NewTeacherResource> loadAll() {
        return newTeacherResourceDao.loadAll();
    }

    public NewTeacherResource load(String id) {
        return newTeacherResourceDao.load(id);
    }

    public MapMessage upsert(NewTeacherResource newTeacherResource) {
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(newTeacherResource.getBookId());
        if (newBookProfile != null) {
            newTeacherResource.setBookShortPublisher(newBookProfile.getName());
        }
        newTeacherResource.setCategory(TeachingResource.Category.NEW_COURSEWARE.name());

        String task = newTeacherResource.getTask();
        if (StringUtils.isBlank(task)) {
            newTeacherResource.setTask(TeachingResourceTask.FREE.name());
        } else {
            if (Objects.equals(task, TeachingResourceTask.NONE.name())) {
                newTeacherResource.setTask(TeachingResourceTask.FREE.name());
            }
        }

        if (newTeacherResource.getDisabled() == null) {
            newTeacherResource.setDisabled(false);
        }
        if (newTeacherResource.getOnline() == null) {
            newTeacherResource.setOnline(false);
        }
        if (newTeacherResource.getOnline() && newTeacherResource.getFirstOnlineTime() == null) {
            newTeacherResource.setFirstOnlineTime(new Date());
        }
        if (newTeacherResource.getDisplayOrder() == null) {
            newTeacherResource.setDisplayOrder(0L);
        }
        if (CollectionUtils.isNotEmpty(newTeacherResource.getFileList())) {
            List<NewTeacherResource.File> fileList = newTeacherResource.getFileList();
            for (NewTeacherResource.File file : fileList) {
                String nameExtName = getFileExtensionName(file.getFileName());
                if (StringUtils.isBlank(nameExtName)) {
                    String fileExtensionName = getFileExtensionName(file.getFileUrl());
                    if (StringUtils.isNotBlank(fileExtensionName)) {
                        file.setFileName(file.getFileName() + "." + fileExtensionName);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(newTeacherResource.getLabel())) {
            newTeacherResource.getLabel().removeIf(StringUtils::isBlank);
        }

        // 编辑时不能更改这些值
        newTeacherResource.setReadCount(null);
        newTeacherResource.setCollectCount(null);
        newTeacherResource.setParticipateNum(null);
        newTeacherResource.setFinishNum(null);

        newTeacherResource.setScore(null);
        newTeacherResource.setStars(null);
        newTeacherResource.setEvaluateNum(null);

        NewTeacherResource upsert = newTeacherResourceDao.upsert(newTeacherResource);
        incrBufferVersion();
        return MapMessage.successMessage().add("data", upsert);
    }

    @Override
    public NewTeacherResource disabled(String id) {
        NewTeacherResource resource = newTeacherResourceDao.load(id);
        if (resource != null) {
            resource.setDisabled(true);
            newTeacherResourceDao.upsert(resource);
            incrBufferVersion();
        }
        return resource;
    }

    @Override
    public NewTeacherResource onlineOffline(String id) {
        NewTeacherResource resource = newTeacherResourceDao.load(id);
        if (resource != null) {
            resource.setOnline(!resource.getOnline());
            if (resource.getOnline() && resource.getFirstOnlineTime() == null) {
                resource.setFirstOnlineTime(new Date());
            }
            newTeacherResourceDao.upsert(resource);
            incrBufferVersion();
        }
        return resource;
    }

    @Override
    public MapMessage syncTeacherCoursewareData() {
        if (RuntimeMode.lt(Mode.PRODUCTION)) {
            newTeacherResourceDao.deleteAll();
        }

        AlpsThreadPool.getInstance().submit(() -> {
            List<TeacherCourseware> data = getProductCoursewareData();

            for (TeacherCourseware courseware : data) {
                NewTeacherResource newTeacherResource = new NewTeacherResource();
                newTeacherResource.setId(courseware.getId().split("-")[1]);
                newTeacherResource.setTitle(courseware.getTitle());
                newTeacherResource.setSubHead(null);

                newTeacherResource.setSubject(courseware.getSubject());
                newTeacherResource.setClazzLevel(courseware.getClazzLevel());
                newTeacherResource.setTermType(courseware.getTermType());
                newTeacherResource.setBookId(courseware.getBookId());

                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(courseware.getBookId());
                if (newBookProfile != null) {
                    newTeacherResource.setBookShortPublisher(newBookProfile.getName());
                }
                newTeacherResource.setUnitId(courseware.getUnitId());
                newTeacherResource.setLessonId(courseware.getLessonId());

                String description = courseware.getDescription();
                if (description == null || Objects.equals(description.trim(), "null")) {
                    description = "";
                }
                newTeacherResource.setDesc(description.trim());
                newTeacherResource.setCategory(TeachingResource.Category.NEW_COURSEWARE.name());
                newTeacherResource.setLabel(null);
                newTeacherResource.setWorkType(TeachingResource.WorkType.无);
                newTeacherResource.setTask(TeachingResourceTask.FREE.name());
                newTeacherResource.setValidityPeriod(null);
                newTeacherResource.setImage(courseware.getCoverUrl());
                newTeacherResource.setAppImage(null);
                newTeacherResource.setHeadImage(null);

                newTeacherResource.setFeaturing(false);
                newTeacherResource.setDisplayOrder(0L);

                newTeacherResource.setOnline(true);
                newTeacherResource.setFirstOnlineTime(courseware.getExamineUpdateTime());

                newTeacherResource.setReceiveLimit(false);

                newTeacherResource.setReadCount(SafeConverter.toLong(courseware.getVisitNum()));
                newTeacherResource.setCollectCount(0L);
                newTeacherResource.setParticipateNum(SafeConverter.toLong(courseware.getDownloadNum()));
                newTeacherResource.setFinishNum(SafeConverter.toLong(courseware.getDownloadNum()));

                newTeacherResource.setSource(0);

                List<String> labelList = new ArrayList<>();
                if (NewTeacherResourceUtils.popularityMap.contains(courseware.getId())) {
                    labelList.add("人气");
                }
                if (NewTeacherResourceUtils.highScoreMap.contains(courseware.getId())) {
                    labelList.add("高分");
                }
                newTeacherResource.setLabel(labelList);

                String coursewarePrize = NewTeacherResourceUtils.prizeMap.get(courseware.getId());
                if (coursewarePrize != null) {
                    newTeacherResource.setCoursewarePrize(coursewarePrize.replace("作品", ""));
                }
                if (StringUtils.isNotBlank(courseware.getAwardLevelName())
                        && (!Objects.equals("无", courseware.getAwardLevelName()))
                        && (!Objects.equals("其他", courseware.getAwardLevelName()))
                ) {
                    newTeacherResource.setPrizeLevel(courseware.getAwardLevelName());
                }

                newTeacherResource.setCoursewareId(courseware.getId());
                newTeacherResource.setAuthorId(RuntimeMode.ge(Mode.STAGING) ? courseware.getTeacherId() : 125110);

                List<NewTeacherResource.File> fileList = convertNewFileList(courseware);
                newTeacherResource.setFileList(fileList);

                newTeacherResource.setCreateAt(courseware.getCreateTime());
                newTeacherResource.setUpdateAt(new Date());
                newTeacherResource.setDisabled(false);
                newTeacherResourceDao.upsert(newTeacherResource);
            }
        });
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage fixBookName() {
        AlpsThreadPool.getInstance().submit(() -> {
            log.info("fixBookName start");
            List<NewTeacherResource> newTeacherResources = loadAll();
            for (NewTeacherResource item : newTeacherResources) {
                String bookId = item.getBookId();
                if (StringUtils.isNotBlank(bookId)) {
                    NewBookProfile newBookProfile = newContentLoaderClient.loadBook(item.getBookId());
                    item.setBookShortPublisher(newBookProfile.getName());
                    newTeacherResourceDao.upsert(item);
                }
            }
            log.info("fixBookName end");
        });
        return MapMessage.successMessage();
    }

    @NotNull
    private List<NewTeacherResource.File> convertNewFileList(TeacherCourseware courseware) {
        List<NewTeacherResource.File> fileList = new ArrayList<>();
        NewTeacherResource.File docFile = new NewTeacherResource.File();
        docFile.setFileName(courseware.getWordName());
        docFile.setFileUrl(courseware.getWordUrl());
        fileList.add(docFile);

        // 如果没有解压后的 ppt, 那就说明老师上传的 ppt 是可以用的, 否则用解压后的文件
        NewTeacherResource.File pptFile = new NewTeacherResource.File();
        if (StringUtils.isBlank(courseware.getPptCoursewareFile())) {
            pptFile.setFileName(courseware.getCoursewareFileName());
            pptFile.setFileUrl(courseware.getCoursewareFile());
        } else {
            pptFile.setFileName(courseware.getPptCoursewareFileName());
            pptFile.setFileUrl(courseware.getPptCoursewareFile());
        }
        fileList.add(pptFile);

        if (StringUtils.isNotBlank(courseware.getZipFileUrl())) {
            NewTeacherResource.File zipFile = new NewTeacherResource.File();
            zipFile.setFileName(courseware.getTitle() + "-压缩包.zip");
            zipFile.setFileUrl(courseware.getZipFileUrl());
            fileList.add(zipFile);
        }
        return fileList;
    }

    private List<TeacherCourseware> getProductCoursewareData() {
        Class<TeacherCoursewareContestService> serviceClass = TeacherCoursewareContestService.class;
        ServiceVersion annotation = serviceClass.getAnnotation(ServiceVersion.class);

        Map<String, Object> baseInfo = new HashMap<>();
        baseInfo.put("method", "loadTeacherCoursewareBufferData");
        baseInfo.put("version", annotation.version());
        baseInfo.put("service", serviceClass.getName());
        baseInfo.put("group", "alps-hydra-production");

        String baseUrl = "http://10.6.3.241:1889";
        String url = UrlUtils.buildUrlQuery(baseUrl, baseInfo);

        Map<String, Object> params = new HashMap<>();
        params.put("paramValues", Collections.singletonList(-1));

        String json = HttpRequestExecutor.defaultInstance().post(url).json(params).socketTimeout(20000).connectionTimeout(20000).execute().getResponseString();

        JSONObject jsonObject = JSONObject.parseObject(json);
        JSONArray data = jsonObject.getJSONArray("data");
        String dataJsonString = JSON.toJSONString(data);
        return JSON.parseArray(dataJsonString, TeacherCourseware.class);
    }

    private void incrBufferVersion() {
        teacherResourceBufferVersion.increment();
    }

    private static String getFileExtensionName(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            fileName = fileName.trim();
            int i = fileName.lastIndexOf(".");
            String type = fileName.substring(i + 1);
            if (type.length() < 5) {
                return type;
            }
        }
        return "";
    }
}
