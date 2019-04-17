package com.voxlearning.utopia.schedule.schedule.vendor;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.vendor.api.VendorUserLoader;
import com.voxlearning.utopia.service.vendor.api.VendorUserService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUserRef;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ScheduledJobDefinition(
        jobName = "统计三方有效用户数",
        jobDescription = "每个月1日02:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 2 1 * ?"
)
@Slf4j
public class AutoVendorUsageStatJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = VendorUserLoader.class)
    private VendorUserLoader vendorUserLoader;
    @ImportService(interfaceClass = VendorUserService.class)
    private VendorUserService vendorUserService;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        //获取所有需要计算的appKey
        Set<String> appkeySet = generateAllAppKey();

        //获取上个月的日期
        MonthRange previous = MonthRange.current().previous();
        Date startDate = previous.getStartDate();
        Date endDate = previous.getEndDate();
        String yearMonth = DateUtils.dateToString(startDate, "yyyyMM");

        List<String> productsServiceType = getProductsServiceType();

        for (String appKey : appkeySet) {
            saveVendorUsageStatForAppKey(startDate, endDate, yearMonth, appKey, productsServiceType);
        }

    }

    private void saveVendorUsageStatForAppKey(Date startDate, Date endDate, String yearMonth, String appKey, List<String> productsServiceType) {
        long effectiveUser = 0L;
        long minUserId = 0L;

        //分页查询1000条用户开通产品记录，筛选出有效人数
        List<VendorUserRef> list = vendorUserLoader.countEffectiveUser(appKey, minUserId, 1000);

        while (CollectionUtils.isNotEmpty(list)) {

            for (VendorUserRef vendorUserRef : list) {

                List<UserActivatedProduct> activatedProducts = userOrderLoaderClient.loadUserActivatedProductList(vendorUserRef.getUserId())
                        .stream()
                        .filter(o -> productsServiceType.contains(o.getProductServiceType()))
                        .collect(Collectors.toList());

                for (UserActivatedProduct activatedProduct : activatedProducts) {

                    //过滤掉无有效时间的订单
                    Date serviceStartTime = activatedProduct.getServiceStartTime();
                    Date serviceEndTime = activatedProduct.getServiceEndTime();
                    if (serviceStartTime == null || serviceEndTime == null) {
                        continue;
                    }

                    boolean noEffective = startDate.after(serviceEndTime)|| endDate.before(serviceStartTime);

                    if (!noEffective) {
                        effectiveUser++;
                        break;
                    }
                }
            }

            //获取集合中最大的userId，循环查询用户开通产品记录，直至为空
            minUserId = list.get(list.size() - 1).getUserId();
            list = vendorUserLoader.countEffectiveUser(appKey, minUserId, 1000);
        }

        vendorUserService.saveVendorUsageStat(appKey, yearMonth, effectiveUser);
    }

    public Set<String> generateAllAppKey() {

        Set<String> appKeySet = new HashSet<>();
        appKeySet.add("byhy");

        return appKeySet;
    }

    private List<String> getProductsServiceType() {
        List<String> productIdList = new ArrayList<>();
        productIdList.add("5876f5067445fb1be06c25ac");
        productIdList.add("58784bb4e92b1b99cf707f8a");
        productIdList.add("5878536fe92b1b99cf708083");
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            productIdList.add("5c2ed0a8ced6cf384b02ee53");
        } else {
            productIdList.add("5c2dc447ac7459afb4f5446a");
        }

        List<String> productsServiceType = userOrderLoaderClient.loadOrderProducts(productIdList).values().stream()
                .map(o -> o.getProductType()).collect(Collectors.toList());


        return productsServiceType;
    }
}
