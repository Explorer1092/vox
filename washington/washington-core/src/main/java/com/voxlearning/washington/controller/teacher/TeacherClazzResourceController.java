package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.mid_english_question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.mid_english_question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.mid_english_question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher/clazzresource")
public class TeacherClazzResourceController extends AbstractTeacherController {

    @Inject
    private PictureBookLoaderClient pictureBookLoaderClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        Subject subject = teacher.getSubject();
        try {
            List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId()).stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.toList());
            if (clazzs.isEmpty()) {
                return "redirect:/teacher/showtip.vpage";
            }
            // 班级列表
            List<ExClazz> exClazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherExClazzsWithSpecifiedSubject(teacher.getId(), teacher.getSubject(), false, false);
            Map<Long, ExClazz> exClazzMap = exClazzList.stream()
                    .collect(Collectors.toMap(ExClazz::getId, e -> e));
            // 将clazz信息组织好加到年级map中
            Map<Integer, List<Map<String, Object>>> batchClazzs = new LinkedHashMap<>();
            clazzs.forEach(clazz -> {
                Map<String, Object> clazzMap = new LinkedHashMap<>();
                clazzMap.put("classId", clazz.getId());
                clazzMap.put("className", clazz.getClassName());
                clazzMap.put("canBeAssigned", true);
                if (exClazzMap.containsKey(clazz.getId()) && exClazzMap.get(clazz.getId()) != null &&
                        CollectionUtils.isNotEmpty(exClazzMap.get(clazz.getId()).getCurTeacherGroups())) {
                    clazzMap.put("groupId", MiscUtils.firstElement(exClazzMap.get(clazz.getId()).getCurTeacherGroups()).getId());
                }
                int clazzLevel = clazz.getClazzLevel().getLevel();
                batchClazzs.computeIfAbsent(clazzLevel, k -> new ArrayList<>())
                        .add(clazzMap);
            });

            // 生成各年级信息
            List<Map<String, Object>> batchClazzsList = new ArrayList<>();
            // 1~6年级
            for (int i = 1; i <= 6; i++) {
                List<Map<String, Object>> clazzList = batchClazzs.getOrDefault(i, Collections.emptyList());
                if (CollectionUtils.isNotEmpty(clazzList)) {
                    Map<String, Object> batchClazzsMap = new LinkedHashMap<>();
                    batchClazzsMap.put("canBeAssigned", true);
                    batchClazzsMap.put("clazzs", clazzList);
                    batchClazzsMap.put("classLevel", i);
                    batchClazzsList.add(batchClazzsMap);
                }
            }
            List<GroupTeacherMapper> groupTeacherMappers = deprecatedGroupLoaderClient.loadTeacherGroups(teacher.getId(), true);
            boolean hasStudents = groupTeacherMappers.stream().anyMatch(group -> CollectionUtils.isNotEmpty(group.getStudents()));
            model.addAttribute("hasStudents", hasStudents);
            model.addAttribute("batchclazzs", JsonUtils.toJson(batchClazzsList));
            model.addAttribute("subject", subject);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "teacherv3/clazzresource/index";
    }

    /**
     * 绘本的课堂讲解视频
     *
     * @return
     */
    @RequestMapping(value = "picturebook/videolist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage pictureBookVideoList() {
        int classLevel = getRequestInt("clazzLevel");
        int pageNum = getRequestInt("pageNum", 1);
        int pageSize = getRequestInt("pageSize", 10);
        ClazzLevel clazzLevel = ClazzLevel.parse(classLevel);
        List<PictureBookVideoInfo> pictureBookVideoList = getMappingInfo();
        if (CollectionUtils.isEmpty(pictureBookVideoList)) {
            return MapMessage.successMessage().add("pictureBookVideo", Collections.emptyList());
        }
        List<String> docIds = pictureBookVideoList.stream().filter(v -> {
            if (clazzLevel == null) {
                return true;
            }
            return v.classLevel == classLevel;
        }).map(PictureBookVideoInfo::getPictureBookId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(docIds)) {
            return MapMessage.successMessage().add("pictureBookVideo", Collections.emptyList());
        }
        Map<String, PictureBookPlus> pictureBook = pictureBookLoaderClient.loadPictureBookPlusByIds(docIds);
        pictureBookVideoList = pictureBookVideoList.stream().filter(v -> {
            if (clazzLevel == null) {
                return true;
            }
            return v.classLevel == classLevel;
        }).map(v -> {
            if (MapUtils.isEmpty(pictureBook) || !pictureBook.containsKey(v.getPictureBookId()) || pictureBook.get(v.getPictureBookId()) == null) {
                return v;
            }
            v.setPictureBookThumbImgUrl(pictureBook.get(v.getPictureBookId()).getCoverUrl());
            PictureBookNewClazzLevel pictureBookNewClazzLevel = null;
            if (CollectionUtils.isNotEmpty(pictureBook.get(v.getPictureBookId()).getNewClazzLevels())) {
                pictureBookNewClazzLevel = pictureBook.get(v.getPictureBookId()).getNewClazzLevels().get(0);
            }
            v.setPictureBookClazzLevelName(pictureBookNewClazzLevel == null ? "" : pictureBookNewClazzLevel.getLevelName());
            return v;
        }).collect(Collectors.toList());
        //处理视频地址
        boolean isHttps = getWebRequestContext().isHttpsRequest();
        if (isHttps) {
            pictureBookVideoList.forEach(e -> {
                if (!e.getVideoUrl().contains("https")) {
                    e.setVideoUrl(e.getVideoUrl().replace("http:", "https:"));
                }
            });
        }
        int totalCount = pictureBookVideoList.size();
        int pageCount = pageSize == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) pageSize);
        pageNum = pageNum < 1 ? 1 : pageNum;
        int skip = (pageNum - 1) * pageSize > pictureBookVideoList.size() ? pictureBookVideoList.size() : (pageNum - 1) * pageSize;
        pictureBookVideoList = pictureBookVideoList.stream().skip(skip).limit(pageSize).collect(Collectors.toList());
        return MapMessage.successMessage()
                .add("pictureBookVideo", pictureBookVideoList)
                .add("totalSize", totalCount)
                .add("pageCount", pageCount)
                .add("pageNum", pageNum);
    }

    /**
     * 获取绘本视频的配置数据
     *
     * @return
     */
    private List<PictureBookVideoInfo> getMappingInfo() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("picture_book_video_resource");
        if (CollectionUtils.isEmpty(teacherTask)) {
            return Collections.emptyList();
        }
        PageBlockContent configPageBlockContent = teacherTask.stream()
                .filter(p -> "picture_book_video".equals(p.getBlockName()))
                .findFirst()
                .orElse(null);
        if (configPageBlockContent == null) {
            return Collections.emptyList();
        }
        String configContent = configPageBlockContent.getContent();
        if (StringUtils.isBlank(configContent)) {
            return Collections.emptyList();
        }
        configContent = configContent.replaceAll("[\n\r\t]", "").trim();
        return JsonUtils.fromJsonToList(configContent, PictureBookVideoInfo.class);
    }

    @Getter
    @Setter
    private static class PictureBookVideoInfo implements Serializable {
        private static final long serialVersionUID = -8529556557487955172L;
        private String pictureBookName;
        private String videoUrl;
        private Integer classLevel;
        private String pictureBookClazzLevelName;
        private String pictureBookId;
        private String pictureBookSeries;
        private String pictureBookThumbImgUrl;
    }
}
