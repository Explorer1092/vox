package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSkuLoader;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSku;
import com.voxlearning.utopia.admin.constant.RecommendationFiledNameType;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.controller.studyTogether.course.CrmCourseCommonController;
import com.voxlearning.utopia.admin.entity.RecommendationErrorResult;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.parent.api.CrmRecommendationPositionService;
import com.voxlearning.utopia.service.parent.api.RecommendationPositionLoader;
import com.voxlearning.utopia.service.parent.api.entity.courseshop.RecommendationFilePath;
import com.voxlearning.utopia.service.parent.api.entity.courseshop.RecommendationPosition;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author xuerui.zhang
 * @since 2019/1/16 下午5:45
 */
@Controller
@RequestMapping(value = "opmanager/studytogether/recommend/")
public class CrmLightCourseShopController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = CrmRecommendationPositionService.class)
    private CrmRecommendationPositionService recommendationPositionService;

    @ImportService(interfaceClass = RecommendationPositionLoader.class)
    private RecommendationPositionLoader recommendationPositionLoader;

    @ImportService(interfaceClass = CrmCourseStructSkuLoader.class)
    private CrmCourseStructSkuLoader courseStructSkuLoader;

    /**
     * 推荐位配置页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        RecommendationFilePath filePath = recommendationPositionLoader.loadLatelyRecommendationFilePath();
        if (null != filePath) {
            model.addAttribute("lastUrl", filePath.getPath());
        }
        return "/opmanager/studyTogether/recommend/index";
    }

    /**
     * 批量保存推荐位信息
     */
    @ResponseBody
    @RequestMapping(value = "batch_upload.vpage", method = RequestMethod.POST)
    public MapMessage saveRecommendationBatch(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return MapMessage.errorMessage("文件为空，请重新上传");
        }
        List<RecommendationErrorResult> errorPositions = new ArrayList<>();
        try {
            String content = new String(file.getBytes(), Charset.forName("GBK"));
            String[] counter = content.split("\n");
            List<RecommendationPosition> positions = new ArrayList<>(counter.length);

            for (int i = 1; i < counter.length; i++) {
                RecommendationPosition bean = new RecommendationPosition();
                RecommendationErrorResult errorResult = new RecommendationErrorResult();
                StringBuilder sb = new StringBuilder();
                String line = counter[i];
                String[] lineArray = line.split(",");

                //处理数据
                Long id = SafeConverter.toLong(lineArray[0]);
                if (id > 0) {
                    CourseStructSku sku = courseStructSkuLoader.loadCourseStructSkuById(id);
                    if (sku != null) {
                        bean.setId(id);
                        bean.setCreateUser(getCurrentAdminUser().getAdminUserName());
                    } else {
                        sb.append(RecommendationFiledNameType.FILED_1.getName()).append("【").append(id).append("】对应的SKU不存在,");
                    }
                } else {
                    sb.append(RecommendationFiledNameType.FILED_1.getName()).append(":只能是数值型,");
                }

                String startDate = lineArray[1];
                if (StringUtils.isBlank(startDate)) {
                    sb.append(RecommendationFiledNameType.FILED_2.getName()).append(" 不能为空,");
                } else {
                    Date date = CrmCourseCommonController.safeConvertDate(startDate);
                    if (date == null) {
                        sb.append(RecommendationFiledNameType.FILED_2.getName()).append(" 日期填写错误,");
                    } else {
                        bean.setStartDate(date);
                    }
                }

                String endDate = lineArray[2];
                if (StringUtils.isBlank(endDate)) {
                    sb.append(RecommendationFiledNameType.FILED_3.getName()).append(" 不能为空,");
                } else {
                    Date date = CrmCourseCommonController.safeConvertDate(endDate);
                    if (date == null) {
                        sb.append(RecommendationFiledNameType.FILED_3.getName()).append(" 日期填写错误,");
                    } else {
                        bean.setEndDate(date);
                    }
                }

                Integer weight = SafeConverter.toInt(lineArray[3]);
                if (weight > 0 && weight <= 100) {
                    bean.setWeight(weight);
                } else {
                    sb.append(RecommendationFiledNameType.FILED_4.getName()).append(":必须是(0,100]区间的数值, ");
                }

                String matchGrade = lineArray[4];
                if (StringUtils.isBlank(matchGrade)) {
                    sb.append(RecommendationFiledNameType.FILED_5.getName()).append("为空, ");
                } else {
                    switch (matchGrade) {
                        case "是":
                            bean.setMatchGrade(true);
                            break;
                        case "否":
                            bean.setMatchGrade(false);
                            break;
                        default:
                            sb.append(RecommendationFiledNameType.FILED_5.getName()).append(" 只能填写[是]或者[否], ");
                            break;
                    }
                }

                int len = lineArray.length;
                if (len >= 6) {
                    String recommendTagFirst = lineArray[5];
                    if (StringUtils.isNotBlank(recommendTagFirst)) {
                        bean.setRecommendTagFirst(recommendTagFirst.trim());
                    }
                }
                if (len >= 7) {
                    String recommendTagSecond = lineArray[6];
                    if (StringUtils.isNotBlank(recommendTagSecond)) {
                        bean.setRecommendTagSecond(recommendTagSecond.trim());
                    }
                }

                if (StringUtils.isNotBlank(sb.toString())) {
                    errorResult.setMessage(sb.toString());
                    errorResult.setRow(i);
                    errorPositions.add(errorResult);
                } else {
                    positions.add(bean);
                }
            }
            if (CollectionUtils.isEmpty(errorPositions) && CollectionUtils.isNotEmpty(positions)) {
                recommendationPositionService.saveRecommendationPosition(positions);
                String url = AdminOssManageUtils.upload(file, "studytogether");
                saveLastSaveFileUrl(url);
                return MapMessage.successMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MapMessage.errorMessage().add("errors", errorPositions);
    }

    /**
     * 批量导出推荐位信息
     */
    @RequestMapping(value = "batch_export.vpage", method = RequestMethod.GET)
    public void batchExport() {
        try {
            List<RecommendationPosition> positions = recommendationPositionLoader.loadAllRecommendationPositionNotBuffer();
            if (CollectionUtils.isEmpty(positions)) {
                return;
            }
            List<String> head = Arrays.asList("轮播课程id","轮播开始时间","轮播结束时间","轮播权重值","是否在适龄模块展示","轮播标签1(非必填)","轮播标签2(非必填)");
            List<List<String>> lists = getLists(positions);
            buildExportCsv( System.currentTimeMillis()+ ".csv", lists, head);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveLastSaveFileUrl(String url) {
        RecommendationFilePath filePath = new RecommendationFilePath();
        filePath.setPath(url);
        filePath.setCreateUser(getCurrentAdminUser().getAdminUserName());
        recommendationPositionService.saveRecommendationFilePath(filePath);
    }

    private List<List<String>> getLists(List<RecommendationPosition> positions) {
        List<List<String>> returnList = new ArrayList<>();
        for (RecommendationPosition mapper : positions) {
            List<String> list = new ArrayList<>();
            list.add(SafeConverter.toString(mapper.getId()));
            list.add(DateUtils.dateToString(mapper.getStartDate()));
            list.add(DateUtils.dateToString(mapper.getEndDate()));
            list.add(SafeConverter.toString(mapper.getWeight()));
            list.add(SafeConverter.toString(mapper.getMatchGrade() ? "是" : "否"));
            list.add(SafeConverter.toString(null == mapper.getRecommendTagFirst() ? "" : mapper.getRecommendTagFirst()));
            list.add(SafeConverter.toString(null == mapper.getRecommendTagSecond() ? "" : mapper.getRecommendTagSecond()));
            returnList.add(list);
        }
        return returnList;
    }

    private void buildExportCsv(String fileName, List<List<String>> lists, List<String> head){
        StringBuilder sb = new StringBuilder();
        String titles = String.join(",", head);
        sb.append(titles).append("\n");
        for (List<String> str : lists) {
            for (int i = 0; i < str.size(); i++) {
                sb.append(str.get(i));
                if (i < str.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        try {
            HttpRequestContextUtils.currentRequestContext()
                    .downloadFile(fileName, "application/csv", sb.toString().getBytes("GBK"));
        }catch (Exception e){
            logger.error("Export Failed");
        }
    }

}
