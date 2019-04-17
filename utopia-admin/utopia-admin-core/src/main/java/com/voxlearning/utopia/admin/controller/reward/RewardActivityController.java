package com.voxlearning.utopia.admin.controller.reward;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.entity.RewardActivity;
import com.voxlearning.utopia.service.reward.entity.RewardActivityImage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;


/**
 * 奖品中心 - 活动 Controller
 * Created by haitian.gan on 2017/2/7.
 */
@Controller
@RequestMapping(value = "/reward/activity")
@Slf4j
public class RewardActivityController extends RewardAbstractController{

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;

    @RequestMapping(value = "index.vpage",method = RequestMethod.GET)
    public String index(Model model){
        int page = getRequestInt("page",1);
        if(page <= 0)
            page = 1;

        Pageable pageable = new PageRequest(page - 1,10);
        List<RewardActivity> activities = rewardLoaderClient.loadRewardActivitiesNoBuffer();
        // 按时间倒序排的
        activities.sort((a1,a2) -> a2.getCreateDatetime().compareTo(a1.getCreateDatetime()));

        Page<RewardActivity> activitiesPage = PageableUtils.listToPage(activities, pageable);
        model.addAttribute("activitiesPage", activitiesPage);
        model.addAttribute("currentPage", activitiesPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", activitiesPage.getTotalPages());
        model.addAttribute("hasPrev", activitiesPage.hasPrevious());
        model.addAttribute("hasNext", activitiesPage.hasNext());

        return "reward/activity/index";
    }

    @RequestMapping(value = "activity.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateActivity(){

        Long activityId = getRequestLong("id");
        String name =getRequestString("name");
        Long targetMoney = getRequestLong("targetMoney");
        String description = getRequestString("description");
        String progressDetail = getRequestString("progressDetail");
        Integer orderWeights = getRequestInt("orderWeights");
        String finishTimeStr = getRequestString("finishTime");
        String model =getRequestString("model");
        String summary = getRequestString("summary");

        RewardActivity original;
        if(activityId != 0){
            original = rewardLoaderClient.loadRewardActivityNoBuffer(activityId);
        }else
            original = new RewardActivity();

        original.setModel(model);
        original.setName(name);
        original.setTargetMoney(targetMoney);
        original.setDescription(description);
        original.setOrderWeights(orderWeights);
        original.setSummary(summary);

        if(!StringUtils.isEmpty(finishTimeStr)){
            Date finishTime = DateUtils.stringToDate(finishTimeStr, FORMAT_SQL_DATE);
            Date createTime = original.getCreateDatetime();
            if(createTime == null)
                createTime = new Date();

            // 完成时间不能大于创建时间
            if(finishTime.before(createTime)){
                return MapMessage.errorMessage("完成时间不能早于创建时间");
            }

            original.setFinishTime(finishTime);
        }

        // 判断进度详情有没有变化，有的话则更新时间
        if(!Objects.equals(original.getProgressDetail(),progressDetail)){
            original.setDetailUpdattime(new Date());
        }

        original.setProgressDetail(progressDetail);

        // 如果是新建的活动，默认是进行中
        // 如果是旧的项目 ，则沿用以前的
        if(StringUtils.isEmpty(original.getStatus()))
            original.setStatus(RewardActivity.Status.ONGOING.name());

        return rewardServiceClient.updateActivity(original);
    }

    @RequestMapping(value = "activity.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActivityInfo(){
        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivityNoBuffer(activityId);

        // 格式化完成时间
        if(activity.getFinishTime() != null)
            activity.setFinishTimeStr(DateUtils.dateToString(activity.getFinishTime(),FORMAT_SQL_DATE));

        return MapMessage.successMessage().add("activity",activity);
    }

    @RequestMapping(value = "imagelist.vpage",method = RequestMethod.GET)
    public String getImageList(Model model){
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        Long activityId = getRequestLong("activityId");
        List<RewardActivityImage> imageList = rewardLoaderClient.loadActivityImages(activityId).stream()
                .filter(i -> i.getActivityId() != null)
                .filter(i -> Objects.equals(i.getActivityId(),activityId))
                .collect(Collectors.toList());

        model.addAttribute("images",imageList);
        model.addAttribute("activityId",activityId);
        model.addAttribute("prePath",prePath);

        return "reward/activity/imagelist";
    }

    //上传图片 活动图片
    @RequestMapping(value = "uploadactivityimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows(IOException.class)
    public MapMessage uploadActivityImage(MultipartFile files, @RequestParam("activityId") Long activityId) {
        if (files.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }

        RewardActivity activity = rewardLoaderClient.loadRewardActivityNoBuffer(activityId);
        if(activity == null)
            return MapMessage.errorMessage("活动不存在!");

        String originalFileName = files.getOriginalFilename();
        String prefix = "rai-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + activityId;

        try (InputStream inStream = files.getInputStream()) {
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);

            RewardActivityImage pictures = new RewardActivityImage();
            pictures.setActivityId(activityId);
            pictures.setLocation(filename);

            pictures = crmRewardService.upsertActivityImage(pictures);
            if (pictures == null) {
                return MapMessage.errorMessage();
            }

            // 上传成功后，更新活动表里面的图片字段
            List<RewardActivityImage> images = rewardLoaderClient.loadActivityImages(activityId);
            if(images.size() > 0) {
                images.sort((i1, i2) -> i2.getCreateDatetime().compareTo(i1.getCreateDatetime()));

                activity.setImgUrl(images.get(0).getLocation());
                rewardServiceClient.updateActivity(activity);
            }

            return MapMessage.successMessage();
        }
    }

    @RequestMapping(value = "deleteimage.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteImage(){
        Long imageId = getRequestLong("imageId");
        RewardActivityImage image = crmRewardService.loadActivityImage(imageId);
        if(image == null)
            return MapMessage.errorMessage("活动图片,id:{}不存在",imageId);

        try{
            crmImageUploader.deletePhotoByFilename(image.getLocation());
            crmRewardService.deleteActivityImage(imageId);
            return MapMessage.successMessage();
        }catch(Exception ex){
            log.error("删除奖品图片异常", ex);
            return MapMessage.errorMessage("删除失败");
        }
    }

    /**
     * 更新上下线状态
     * @return
     */
    @RequestMapping(value = "online.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateOnlineStatus(){
        Long activityId = getRequestLong("activityId");
        RewardActivity activity = rewardLoaderClient.loadRewardActivityNoBuffer(activityId);
        if(activity == null)
            return MapMessage.errorMessage("活动不存在!");

        Boolean status = getRequestBool("status");
        activity.setOnline(status);

        return rewardServiceClient.updateActivity(activity);
    }

    /**
     * 上传公益活动显示在订单列表里面的图片
     * @param file
     * @param activityId
     * @return
     */
    @RequestMapping(value="uploadorderimage.vpage",method = RequestMethod.POST)
    @ResponseBody
    @SneakyThrows
    public MapMessage uploadOrderImage(MultipartFile file,@RequestParam("activityId") Long activityId){
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("没有文件上传");
        }

        if(activityId == null)
            return MapMessage.errorMessage("参数错误!");

        RewardActivity activity = rewardLoaderClient.loadRewardActivityNoBuffer(activityId);
        if(activity == null)
            return MapMessage.errorMessage("活动不存在!");

        String originalFileName = file.getOriginalFilename();
        String prefix = "rai-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + activityId;

        try (InputStream inStream = file.getInputStream()) {
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);

            activity.setImgUrlSquare(filename);
            rewardServiceClient.updateActivity(activity);
            return MapMessage.successMessage();
        }
    }


}
