package com.voxlearning.utopia.admin.controller.studyTogether.course;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.coin.api.CoinTypeBufferLoaderClient;
import com.voxlearning.galaxy.service.coin.api.entity.CoinType;
import com.voxlearning.galaxy.service.ebook.api.CrmEbookService;
import com.voxlearning.galaxy.service.ebook.api.entity.Ebook;
import com.voxlearning.galaxy.service.studycourse.api.*;
import com.voxlearning.galaxy.service.studycourse.api.entity.component.template.NewTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.*;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ChineseReadingLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ChineseStoryLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ClassicalChineseLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.PictureBookLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.constant.StudyCourseContentType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.data.CourseConstMapper;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author xuerui.zhang
 * @since 2018/9/20 上午10:57
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/common/")
public class CrmCourseCommonController extends AbstractAdminSystemController {

    private static final String FORMAT_SQL_DATETIME = "yyyy/MM/dd HH:mm:ss";

    @Inject
    private CoinTypeBufferLoaderClient coinTypeBufferLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;

    @ImportService(interfaceClass = CrmCourseStructSeriesLoader.class)
    private CrmCourseStructSeriesLoader courseStructSeriesLoader;

    @ImportService(interfaceClass = CrmCourseStructSkuLoader.class)
    private CrmCourseStructSkuLoader skuLoader;

    @ImportService(interfaceClass = CrmCourseStructSpuLoader.class)
    private CrmCourseStructSpuLoader spuLoader;

    @ImportService(interfaceClass = CrmCourseSubjectLoader.class)
    private CrmCourseSubjectLoader courseSubjectLoader;

    @ImportService(interfaceClass = CrmCourseChapterLoader.class)
    private CrmCourseChapterLoader courseChapterLoader;

    @ImportService(interfaceClass = CrmLessonTemplateLoader.class)
    private CrmLessonTemplateLoader lessonTemplateLoader;

    @ImportService(interfaceClass = CrmCourseWeeklyRewardLoader.class)
    private CrmCourseWeeklyRewardLoader courseWeeklyRewardLoader;

    @ImportService(interfaceClass = CrmCourseStructSpuLoader.class)
    private CrmCourseStructSpuLoader courseStructSpuLoader;

    @ImportService(interfaceClass = CrmCourseStructSkuLoader.class)
    private CrmCourseStructSkuLoader courseStructSkuLoader;

    @ImportService(interfaceClass = CrmEbookService.class)
    private CrmEbookService crmEbookService;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @ImportService(interfaceClass = CrmNewTemplateLoader.class)
    private CrmNewTemplateLoader crmNewTemplateLoader;

    /**
     * 系列验证
     */
    @ResponseBody
    @RequestMapping(value = "series_name.vpage", method = RequestMethod.GET)
    public MapMessage getSeriesName() {
        Long seriesId = getRequestLong("seriesId");
        if (0L == seriesId) return MapMessage.errorMessage();
        CourseStructSeries courseStructSeries = courseStructSeriesLoader.loadCourseStructSeriesById(seriesId);
        return courseStructSeries == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("seriesName", courseStructSeries.getName());
    }

    /**
     * 主题验证
     */
    @ResponseBody
    @RequestMapping(value = "subject_name.vpage", method = RequestMethod.GET)
    public MapMessage getSubjectName() {
        Long subjectId = getRequestLong("subjectId");
        if (0L == subjectId) return MapMessage.errorMessage();
        CourseSubject courseSubject = courseSubjectLoader.loadCourseSubjectById(subjectId);
        return courseSubject == null ? MapMessage.errorMessage() :
                MapMessage.successMessage().add("subjectName", courseSubject.getName());
    }

    /**
     * 章节验证
     */
    @ResponseBody
    @RequestMapping(value = "chapter_name.vpage", method = RequestMethod.GET)
    public MapMessage getChapterName() {
        Long chapterId = getRequestLong("chapterId");
        if (0L == chapterId) return MapMessage.errorMessage();
        CourseChapter courseChapter = courseChapterLoader.loadCourseChapter(chapterId);
        return courseChapter == null ? MapMessage.errorMessage() : MapMessage.successMessage()
                .add("chapterName", courseChapter.getChapterName());
    }

    /**
     * 章节模板验证
     */
    @ResponseBody
    @RequestMapping(value = "template_name.vpage", method = RequestMethod.GET)
    public MapMessage getTemplateName() {
        Long templateId = getRequestLong("templateId");
        Long chapterId = getRequestLong("chapterId");
        if (0L == templateId || 0L == chapterId) {
            return MapMessage.errorMessage("参数错误");
        }

        CourseChapter chapter = courseChapterLoader.loadCourseChapter(chapterId);
        if (null == chapter) {
            return MapMessage.errorMessage("章节不存在");
        }

        CourseStructSku sku = skuLoader.loadCourseStructSkuById(chapter.getSkuId());
        if (null == sku) {
            return MapMessage.errorMessage("SKU不存在");
        }

        CourseStructSpu spu = spuLoader.loadCourseStructSpuById(sku.getSpuId());
        if (null == spu) {
            return MapMessage.errorMessage("SPU不存在");
        }

        CourseStructSeries series = courseStructSeriesLoader.loadCourseStructSeriesById(spu.getSeriesId());
        if (null == series) {
            return MapMessage.errorMessage("系列不存在");
        }

        Integer type = series.getCourseType();
        if (StudyCourseContentType.CLASSICAL_CHINESE.getId() == type) {
            ClassicalChineseLessonTemplate chineseLessonTemplate = lessonTemplateLoader.loadClassicalChineseTemplate(templateId);
            if (null != chineseLessonTemplate) {
                return MapMessage.successMessage().add("templateName", chineseLessonTemplate.getName());
            }
        } else if (StudyCourseContentType.PIC_BOOK.getId() == type) {
            PictureBookLessonTemplate bookLessonTemplate = lessonTemplateLoader.loadPictureBookTemplate(templateId);
            if (null != bookLessonTemplate) {
                return MapMessage.successMessage().add("templateName", bookLessonTemplate.getName());
            }
        } else if (StudyCourseContentType.CHINESE_READING.getId() == type) {
            ChineseReadingLessonTemplate readingLessonTemplate = lessonTemplateLoader.loadChineseReadLessonTemplate(templateId);
            if (null != readingLessonTemplate) {
                return MapMessage.successMessage().add("templateName", readingLessonTemplate.getName());
            }
        } else if (StudyCourseContentType.CHINESE_STORY.getId() == type) {
            ChineseStoryLessonTemplate storyLessonTemplate = lessonTemplateLoader.loadChineseStoryLessonTemplate(templateId);
            if (null != storyLessonTemplate) {
                return MapMessage.successMessage().add("templateName", storyLessonTemplate.getName());
            }
        } else if (type == 5) {
            return MapMessage.successMessage().add("templateName", "数学编程课，无需模板 id");
        } else if (type == 7) {
            NewTemplate newTemplate = crmNewTemplateLoader.loadNewTemplateById(templateId);
            if (null != newTemplate) {
                return MapMessage.successMessage().add("templateName", newTemplate.getName());
            }
        }
        return MapMessage.errorMessage();
    }

    /**
     * 周激励验证
     */
    @ResponseBody
    @RequestMapping(value = "weeklyreward.vpage", method = RequestMethod.POST)
    public MapMessage getWeeklyRewardByType() {
        Long weeklyRewardId = getRequestLong("weeklyRewardId");
        if (0L == weeklyRewardId) return MapMessage.errorMessage();
        CourseWeeklyReward courseWeeklyReward = courseWeeklyRewardLoader.loadCourseWeeklyReward(weeklyRewardId);
        return courseWeeklyReward == null || courseWeeklyReward.getType() == 1 ? MapMessage.errorMessage() : MapMessage.successMessage().add("name", courseWeeklyReward.getName());
    }

    /**
     * sku验证
     */
    @ResponseBody
    @RequestMapping(value = "sku_name.vpage", method = RequestMethod.GET)
    public MapMessage getSkuName() {
        Long skuId = getRequestLong("skuId");
        if (0L == skuId) return MapMessage.errorMessage();
        CourseStructSku courseStructSku = courseStructSkuLoader.loadCourseStructSkuById(skuId);
        return courseStructSku == null ? MapMessage.errorMessage() : MapMessage.successMessage();
    }

    /**
     * SPU验证
     */
    @ResponseBody
    @RequestMapping(value = "spu_name.vpage", method = RequestMethod.GET)
    public MapMessage getSpuName() {
        Long spuId = getRequestLong("spuId");
        if (0L == spuId) return MapMessage.errorMessage();
        CourseStructSpu courseStructSpu = courseStructSpuLoader.loadCourseStructSpuById(spuId);
        return courseStructSpu == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("spuName", courseStructSpu.getName());
    }

    @ResponseBody
    @RequestMapping(value = "coin.vpage", method = RequestMethod.GET)
    public MapMessage getCoinName() {
        Integer coinType = getRequestInt("coinType");
        if (0 == coinType) return MapMessage.errorMessage();
        CoinType coin = coinTypeBufferLoaderClient.getCoinType(coinType);
        return coin == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("coinName", coin.getName());
    }

    @ResponseBody
    @RequestMapping(value = "parent_reward.vpage", method = RequestMethod.GET)
    public MapMessage getRewardName() {
        String rewardType = getRequestString("rewardType");
        if (StringUtils.isBlank(rewardType)) {
            return MapMessage.errorMessage();
        }
        ParentRewardItem parentRewardItem = parentRewardBufferLoaderClient.getParentRewardItem(rewardType);
        return parentRewardItem == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("rewardName", parentRewardItem.getTitle());
    }

    @ResponseBody
    @RequestMapping(value = "ebook.vpage", method = RequestMethod.GET)
    public MapMessage getEbookName() {
        String ebookId = getRequestString("ebookId");
        if (StringUtils.isBlank(ebookId)) return MapMessage.errorMessage();
        Ebook ebook = crmEbookService.getEbookById(ebookId);
        return ebook == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("ebookName", ebook.getTitle());
    }

    /**
     * 上传视频到OSS
     */
    @ResponseBody
    @RequestMapping(value = "upload.vpage", method = RequestMethod.POST)
    public MapMessage uploadImgToOss(MultipartFile inputFile) {
        String activityName = "study_course";
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(inputFile.getSize());
            String env = activityName + "/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = activityName + "/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            String fileName = inputFile.getOriginalFilename();
            String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
            String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get(CourseConstMapper.OSS_HOST)) + realName;
            return MapMessage.successMessage().add("fileName", realName).add("fileUrl", fileUrl);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    /**
     * 商品验证
     */
    @ResponseBody
    @RequestMapping(value = "product_name.vpage", method = RequestMethod.GET)
    public MapMessage getProductName() {
        String productId = getRequestString("productId");
        if (StringUtils.isBlank(productId)) return MapMessage.errorMessage();
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        return product == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("productName", product.getName());
    }

    public static Date safeConvertDate(String dateStr) {
        if (dateStr.contains("/")) {
            return DateUtils.stringToDate(dateStr, FORMAT_SQL_DATETIME);
        } else {
            return DateUtils.stringToDate(dateStr);
        }
    }
}
