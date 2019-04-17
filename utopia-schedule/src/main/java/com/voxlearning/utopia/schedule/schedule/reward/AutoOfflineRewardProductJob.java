package com.voxlearning.utopia.schedule.schedule.reward;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.util.DuibaTool;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Named
@ScheduledJobDefinition(
        jobName = "自动下架兑吧商品",
        jobDescription = "每10分钟跑一次",
        disabled = {Mode.DEVELOPMENT,Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0/10 * * * ?"
)
public class AutoOfflineRewardProductJob  extends ScheduledJobWithJournalSupport {

    @Inject
    private RewardLoaderClient rewardLoaderClient;

    @Inject private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String url = "https://www.duiba.com.cn/queryForFrontItem/query?";
        Map<String, String> param = new HashMap<>();

        param.put("timestamp", SafeConverter.toString(System.currentTimeMillis()));
        param.put("count", "20");
        String yuandingdouUrl = "";
        String xuedouUrl = "";
        if(RuntimeMode.isProduction()){
            yuandingdouUrl = DuibaTool.buildUrlWithSign(url, param, DuibaTool.DuibaApp.ONLINE_YUANDINGDOU);
            xuedouUrl = DuibaTool.buildUrlWithSign(url, param, DuibaTool.DuibaApp.ONLINE_XUEDOU);
        }else{
            yuandingdouUrl = DuibaTool.buildUrlWithSign(url, param, DuibaTool.DuibaApp.TEST_YUANDINGDOU);
            xuedouUrl = DuibaTool.buildUrlWithSign(url, param, DuibaTool.DuibaApp.TEST_XUEDOU);
        }

        String content = "";

        try{
            //园丁豆优惠券处理
            AlpsHttpResponse yuandingdouResponse = HttpRequestExecutor.defaultInstance().get(yuandingdouUrl).execute();
            Map<String ,Object> yuandingdouMap = JsonUtils.fromJson(yuandingdouResponse.getResponseString());
            List<RewardProduct> teachRewardProducts = rewardLoaderClient.loadAllTeacherProducts().stream().filter(o-> Objects.nonNull(o.getUsedUrl()) && o.getUsedUrl().contains("m.duiba.com.cn")).collect(Collectors.toList());
            List<RewardProduct> havedTeachRewardProducts = new ArrayList<>();
            if(MapUtils.isNotEmpty(yuandingdouMap) && SafeConverter.toBoolean(yuandingdouMap.get("success"))){
                List<Map> yuandingdouData = (List<Map>) yuandingdouMap.get("data");
                //如果我们的列表有，兑吧中没有，就我这些没有的下线
                for(Map temp : yuandingdouData){
                    String tempUrl = (String)temp.get("url");
                    String duibaItemVal = getItemValue(tempUrl);
                    for(RewardProduct rewardProduct : teachRewardProducts){
                        String usedUrl = rewardProduct.getUsedUrl();
                        String myItemVal = getItemValue(usedUrl);
                        if(Objects.equals(duibaItemVal,myItemVal)){
                            havedTeachRewardProducts.add(rewardProduct);
                        }
                    }
                }
            }

            //下线商品
            List<RewardProduct> offlineRewardProduct = new ArrayList<>();
            //排除都有的数据
            teachRewardProducts.removeAll(havedTeachRewardProducts);
            for(RewardProduct temp : teachRewardProducts){
                temp.setOnlined(false);
                temp.setOfflineDatetime(new Date());
                RewardProduct rewardProduct = crmRewardService.$upsertRewardProduct(temp);
                offlineRewardProduct.add(rewardProduct);
            }
            //学豆
            AlpsHttpResponse xuedouResponse = HttpRequestExecutor.defaultInstance().get(xuedouUrl).execute();
            Map<String ,Object> xuedouMap = JsonUtils.fromJson(xuedouResponse.getResponseString());
            List<RewardProduct> stuRewardProducts = rewardLoaderClient.loadAllStudentProducts().stream().filter(o-> Objects.nonNull(o.getUsedUrl()) && o.getUsedUrl().contains("m.duiba.com.cn")).collect(Collectors.toList());
            List<RewardProduct> havedStuRewardProducts = new ArrayList<>();
            if(MapUtils.isNotEmpty(xuedouMap) && SafeConverter.toBoolean(xuedouMap.get("success"))){
                List<Map> xuedouData = (List<Map>) xuedouMap.get("data");
                for(Map temp : xuedouData){
                    String tempUrl = (String)temp.get("url");
                    String duibaItemVal = getItemValue(tempUrl);
                    for(RewardProduct rewardProduct : stuRewardProducts){
                        String usedUrl = rewardProduct.getUsedUrl();
                        String myItemVal = getItemValue(usedUrl);
                        if(Objects.equals(duibaItemVal,myItemVal)){
                            havedStuRewardProducts.add(rewardProduct);
                        }
                    }
                }
            }

            stuRewardProducts.removeAll(havedStuRewardProducts);
            for(RewardProduct temp : stuRewardProducts){
                temp.setOnlined(false);
                temp.setOfflineDatetime(new Date());
                RewardProduct rewardProduct = crmRewardService.$upsertRewardProduct(temp);
                offlineRewardProduct.add(rewardProduct);
            }

            //如果有下线的产品进行邮件通知
            if(CollectionUtils.isNotEmpty(offlineRewardProduct)){
                StringBuffer productNames = new StringBuffer();
                for(RewardProduct product : offlineRewardProduct){
                    productNames.append(product.getProductName()).append("\n");
                }
                content = productNames.toString();
            }

        }catch(Exception e){
            logger.error("下线兑吧商品失败",e);
            content = "下线兑吧商品失败,错误信息为："+e.getMessage()+"\n"+JsonUtils.toJson(e.getStackTrace());
        }
        if(StringUtils.isNotBlank(content)){
            if (RuntimeMode.isProduction()) {
                emailServiceClient.createPlainEmail()
                        .to("shan.wang@17zuoye.com;zhilong.hu@17zuoye.com;yong.liu@17zuoye.com")
                        .subject("兑吧下线商品")
                        .body("各位好，下线商品如下：" + content)
                        .send();
            }
        }

    }

    public static String getItemValue(String url){
        Pattern p = Pattern.compile("[^?&]?itemId=[^&]+");
        Matcher m = p.matcher(url);
        String itemVal = "";
        if(m.find()) {
            String itemval = m.group(0);
            itemVal = itemval.split("=")[1];
        }
        return itemVal;
    }


}
