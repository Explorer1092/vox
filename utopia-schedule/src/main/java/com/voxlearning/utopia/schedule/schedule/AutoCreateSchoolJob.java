package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Named
@ScheduledJobDefinition(
        jobName = "手动创建学校",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 8 * * ? "
)

@ProgressTotalWork(100)
public class AutoCreateSchoolJob extends ScheduledJobWithJournalSupport {

    @Inject private SchoolServiceClient schoolServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        List<String> dataList = Arrays.asList(
                "610103_陕西西安西工大习得中心",
                "610103_陕西西安太乙路习得中心",
                "120116_天津滨海习得中心",
                "120116_天津塘沽新北习得中心",
                "110114_北京昌平习得中心",
                "110114_北京西三旗习得中心",
                "220104_北京十里堡习得中心",
                "220104_北京劲松习得中心",
                "220104_北京欢乐谷习得中心",
                "220104_北京望京方恒习得中心",
                "220104_北京亚运村习得中心",
                "220104_北京朝阳门习得中心",
                "220104_北京管庄习得中心",
                "220104_北京北苑习得中心",
                "220104_吉林长春习得中心",
                "411602_河南周口习得中心",
                "130403_河北邯郸习得中心",
                "110115_北京西红门习得中心",
                "110115_北京瀛海习得中心",
                "610825_陕西定边习得中心",
                "130682_河北定州习得中心",
                "110101_北京和平里习得中心",
                "110101_北京崇文门习得中心",
                "360102_ 江西南昌习得中心",
                "120110_天津空港习得中心",
                "110111_北京房山习得中心",
                "130826_河北丰宁习得中心",
                "110106_北京北大地习得中心",
                "110106_北京角门习得中心",
                "370611_山东烟台福山习得中心",
                "320106_江苏南京龙江习得中心",
                "131022_河北固安习得中心",
                "530111_云南昆明官渡习得中心",
                "131003_河北廊坊广阳习得中心",
                "110108_北京金源习得中心",
                "110108_北京五棵松习得中心",
                "110108_北京上地习得中心",
                "110108_北京中关村习得中心",
                "130302_河北秦皇岛习得中心",
                "321003_江苏扬州习得中心",
                "610702_陕西汉中习得中心",
                "120101_辽宁沈阳习得中心",
                "120103_天津河西习得中心",
                "410702_河南新乡习得中心",
                "120106_天津红桥宝能习得中心",
                "140824_山西运城稷山习得中心",
                "330104_浙江杭州庆春习得中心",
                "320115_南京江宁万达习得中心",
                "410105_河南郑州文化路习得中心",
                "150603_内蒙古鄂尔多斯康巴什习得中心",
                "370705_山东潍坊习得中心",
                "150203_内蒙古包头钢铁大街习得中心",
                "371302_山东临沂校区",
                "370213_山东青岛李沧习得中心",
                "370102_山东济南优品汇习得中心",
                "411402_河南商丘习得中心",
                "370305_山东淄博校区",
                "410184_河南新郑龙湖习得中心",
                "330302_浙江温州小南门习得中心",
                "130203_河北唐山习得中心",
                "420302_湖北十堰习得中心",
                "110228_北京密云习得中心",
                "310112_上海闵行习得中心",
                "120104_天津黄河道习得中心",
                "120104_天津奥城习得中心",
                "230404_深圳南山校区",
                "320111_江苏南京明发滨江习得中心",
                "130104_河北石家庄恒大华府习得中心",
                "150105_内蒙古呼和浩特长安金座习得中心",
                "131082_河北燕郊习得中心",
                "131082_河北三河习得中心",
                "610821_陕西神木习得中心",
                "110100_北京石景山习得中心",
                "370203_山东青岛北区浮山习得中心",
                "110113_北京顺义习得中心",
                "210302_辽宁鞍山习得中心",
                "110112_ 北京通州梨园习得中心",
                "350212_福建厦门同安习得中心",
                "140109_山西太原理工下元习得中心",
                "130481_河北武安习得中心",
                "110102_北京西城红莲习得中心‍",
                "110102_北京西直门习得中心",
                "131024_河北香河习得中心",
                "140105_山西太原学府街习得中心",
                "410402_河南平顶山习得中心",
                "320102_江苏南京新世界习得中心",
                "222401_吉林延吉北山习得中心",
                "411103_河南漯河习得中心",
                "411503_河南信阳习得中心",
                "140702_山西晋中盛景习得中心",
                "340504_安徽马鞍山习得中心",
                "411081_河南禹城习得中心",
                "370812_山东济宁习得中心",
                "410102_河南郑州桐柏路习得中心"
                );

        for (String data : dataList) {
            String[] infoStr = data.split("_");
            if (infoStr.length != 2) {
                logger.warn("error format :" + data);
                continue;
            }

            Integer regionCode = SafeConverter.toInt(infoStr[0]);
            String schoolName = infoStr[1];

            School school = new School();
            school.setDisabled(false);
            school.setCname(schoolName.trim());
            school.setCmainName(schoolName.trim());
            school.setShortName(schoolName.trim());
            school.setCode(SafeConverter.toString(regionCode));
            school.setRegionCode(regionCode);
            school.setType(SchoolType.TRAINING.getType());
            school.setLevel(SchoolLevel.JUNIOR.getLevel());
            school.setAuthenticationState(0);

            School newSchool = schoolServiceClient.getSchoolService().updateSchool(school).getUninterruptibly();
            if (newSchool != null) {
                logger.info("create school success {}:{}", schoolName, newSchool.getId());
            } else {
                logger.info("create school failed {}:{}", schoolName);
            }
        }

    }
}
