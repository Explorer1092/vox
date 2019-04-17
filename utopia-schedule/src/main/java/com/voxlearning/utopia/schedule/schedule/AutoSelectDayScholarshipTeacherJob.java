package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.cache.TeacherScholarshipRecordCache;
import com.voxlearning.utopia.entity.activity.TeacherScholarshipRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.JobOssManageUtils;
import com.voxlearning.utopia.service.business.consumer.TeacherActivityServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.stringToDate;

@Named
@ScheduledJobDefinition(
        jobName = "每日“期末教师先锋奖”活动抽取中奖老师的job",
        jobDescription = "每天早上2点运行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?"
)
@ProgressTotalWork(100)
public class AutoSelectDayScholarshipTeacherJob extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private TeacherActivityServiceClient teacherActivityServiceClient;

    @Inject protected UserLoaderClient userLoaderClient;

    @Inject protected SmsServiceClient smsServiceClient;

    @Inject private EmailServiceClient emailServiceClient;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() {
        utopiaSql = UtopiaSqlFactory.instance().getUtopiaSql("hs_misc");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        //活动12月10号开始，12月11号零点开始运行此任务
        Date now = new Date();
        // 保护一下
        Date startDate = stringToDate("2018-12-11", DateUtils.FORMAT_SQL_DATE);
        if (now.before(startDate)) {
            return;
        }

        Date endDate = stringToDate("2018-12-30 23:59:59", DateUtils.FORMAT_SQL_DATETIME);
        if (now.after(endDate)) {
            return;
        }

        //查询所有申请了每日期末先锋奖的老师,且基础必过未删除的老师
        Date yesterday = DateUtils.addDays(new Date(), -1);
        String yesterdayStrFormat = DateUtils.dateToString(yesterday, "yyyy.MM.dd");
        String yesterdayStr = DateUtils.dateToString(yesterday, "yyyy-MM-dd");
        List<Long> teacherIds = utopiaSql.withSql(
                "SELECT TEACHER_ID FROM VOX_TEACHER_SCHOLARSHIP_RECORD " +
                        " WHERE LAST_ASSIGN_DATE = ?" +
                        " AND BASIC_REVIEW_NUM > 0 " +
                        " AND TERM_REVIEW_NUM > 0 " +
                        " AND FINISH_RATE > 0.5 " +
                        " AND SCORE > 60 " +
                        " AND DAILY_LOTTERY = 1")
                .useParamsArgs(yesterdayStr)
                .queryColumnValues(Long.class);

        //去掉已经抽取的人
        Set<Long> scholarshipedTeas = TeacherScholarshipRecordCache.loadAllDayScholarshipTeachers();
        if(Objects.nonNull(scholarshipedTeas)){
            teacherIds.removeAll(scholarshipedTeas);
        }

        //少于10人的情况，考虑主副账号，全部中奖
        Set<Long> specialTeacherIds = new TreeSet<>();
        for(Long id : teacherIds){
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(id);
            if (mainTeacherId == null || mainTeacherId == 0L) {
                mainTeacherId = id;
            }
            specialTeacherIds.add(mainTeacherId);
        }

        Set<Long> scholarshipTeachers = new TreeSet<>();
        if(specialTeacherIds.size() > 10){
            //随机抽取10个人
            while(scholarshipTeachers.size() < 10){
                try {
                    if(teacherIds.size()==0){
                        break;
                    }
                    int selectIndex = RandomUtils.nextInt(teacherIds.size());
                    Long selectTeacherId = teacherIds.get(selectIndex);
                    // 选中的就移除
                    teacherIds.remove(selectIndex);
                    // 最后阶段判断假老师
                    if (teacherLoaderClient.isFakeTeacher(selectTeacherId)) {
                        continue;
                    }

                    // 副账号的要算在主账号头上
                    Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(selectTeacherId);
                    if (mainTeacherId == null || mainTeacherId == 0L) {
                        mainTeacherId = selectTeacherId;
                    }

                    // 在最后阶段验证老师是不是认证的
                    TeacherDetail selectTeacher = teacherLoaderClient.loadTeacherDetail(mainTeacherId);
                    if (selectTeacher == null || selectTeacher.getAuthenticationState() != AuthenticationState.SUCCESS.getState()) {
                        continue;
                    }
                    scholarshipTeachers.add(selectTeacher.getId());
                }catch (Exception e){
                    logger.error("期末复习每日抽奖异常",e);
                }
            }
        }else{
            scholarshipTeachers = specialTeacherIds;
        }

        //抽完讲所有的状态置为false
        teacherActivityServiceClient.updateDailyLottery();

        List<String> mobiles = new ArrayList<>();
        List<Map<String,Object>> records = new ArrayList<>();
        //添加附加的信息
        if(CollectionUtils.isNotEmpty(scholarshipTeachers)){
            TeacherScholarshipRecordCache.addAllDayScholarshipTeachers(scholarshipTeachers);
            //拼接其他数据
            List<Map<String,Object>> dayScholarshopMaps = new LinkedList<>();
            Iterator<Long> it = scholarshipTeachers.iterator();
            int count = 0;
            while(it.hasNext()){
                Long teacherId = it.next();
                TeacherDetail selectTeacher = teacherLoaderClient.loadTeacherDetail(teacherId);
                Map<String,Object> awardDetail = new LinkedHashMap<>();
                awardDetail.put("dateStr",yesterdayStrFormat);
                awardDetail.put("teacher",selectTeacher.fetchRealname().isEmpty()?"":selectTeacher.fetchRealname().substring(0,1)+"老师");
                awardDetail.put("school",selectTeacher.getTeacherSchoolName());
                if(count == 0){
                    awardDetail.put("award","智能音箱+电教笔");
                }else{
                    awardDetail.put("award","电教笔");
                }
                dayScholarshopMaps.add(awardDetail);

                UserAuthentication authentication = userLoaderClient.loadUserAuthentication(teacherId);
                if (StringUtils.isNotBlank(authentication.getSensitiveMobile())) {
                    mobiles.add(SensitiveLib.decodeMobile(authentication.getSensitiveMobile()));
                }
                TeacherScholarshipRecord record = teacherActivityServiceClient.loadTeacherScholarshipRecord(teacherId);
                Map<String,Object> exportData = buildExportData(selectTeacher,record,awardDetail.get("award"));
                records.add(exportData);
                count++;
            }
            //将列表放入缓存
            TeacherScholarshipRecordCache.setDayScholarshipTeachers(dayScholarshopMaps);
            String filePath = writeExcel(records);
            try {
                emailServiceClient.createPlainEmail()
                        .to("te.wang@17zuoye.com")
                        .cc("yong.liu@17zuoye.com")
                        .subject("每日期末教师先锋奖中奖老师")
                        .body(filePath)
                        .send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sendMessage(mobiles);
        }
    }

    private String writeExcel(List<Map<String,Object>> records) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        XSSFRow topRow = sheet.createRow(0);
        String[] titles = new String[]{"老师ID","老师姓名","学校","日期","科目","基础必过布置次数","期末复习布置次数","期末复习检查次数","完成率","完成数","平均分","奖品"};
        for(int i=0; i<titles.length; i++){
            String title = titles[i];
            XSSFCell topCell0 = topRow.createCell(i);
            topCell0.setCellValue(title);
        }
        for(int i=0; i<records.size(); i++){
            Map<String,Object> record = records.get(i);
            XSSFRow dataRow = sheet.createRow(i+1);
            XSSFCell dataCell0 = dataRow.createCell(0);
            dataCell0.setCellValue(SafeConverter.toString(record.get("teacherId")));
            XSSFCell dataCell1 = dataRow.createCell(1);
            dataCell1.setCellValue(SafeConverter.toString(record.get("teacherName")));
            XSSFCell dataCell2 = dataRow.createCell(2);
            dataCell2.setCellValue(SafeConverter.toString(record.get("school")));
            XSSFCell dataCell3 = dataRow.createCell(3);
            dataCell3.setCellValue(SafeConverter.toString(record.get("date")));
            XSSFCell dataCell4 = dataRow.createCell(4);
            dataCell4.setCellValue(SafeConverter.toString(record.get("subject")));
            XSSFCell dataCell5 = dataRow.createCell(5);
            dataCell5.setCellValue(SafeConverter.toString(record.get("baseReview")));
            XSSFCell dataCell6 = dataRow.createCell(6);
            dataCell6.setCellValue(SafeConverter.toString(record.get("termReview")));
            XSSFCell dataCell7 = dataRow.createCell(7);
            dataCell7.setCellValue(SafeConverter.toString(record.get("termReviewChecked")));
            XSSFCell dataCell8 = dataRow.createCell(8);
            dataCell8.setCellValue(SafeConverter.toString(record.get("finishRate")));
            XSSFCell dataCell9 = dataRow.createCell(9);
            dataCell9.setCellValue(SafeConverter.toString(record.get("finishNum")));
            XSSFCell dataCell10 = dataRow.createCell(10);
            dataCell10.setCellValue(SafeConverter.toString(record.get("score")));
            XSSFCell dataCell11 = dataRow.createCell(11);
            dataCell11.setCellValue(SafeConverter.toString(record.get("award")));
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        String filePath = JobOssManageUtils.upload(is,content.length,"xlsx");
        return filePath;
    }

    private Map<String, Object> buildExportData(TeacherDetail selectTeacher, TeacherScholarshipRecord record,Object award) {
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("teacherId",selectTeacher.getId());
        data.put("teacherName",selectTeacher.fetchRealname());
        data.put("school",selectTeacher.getTeacherSchoolName());
        if(Objects.nonNull(record)){
            data.put("date",DateUtils.dateToString(record.getLastAssignDate(),DateUtils.FORMAT_SQL_DATE));
            data.put("subject",record.getSubjects());
            data.put("baseReview",record.getBasicReviewNum());
            data.put("termReview",record.getTermReviewNum());
            data.put("termReviewChecked",record.getTermReviewChecked());
            data.put("finishRate",record.getFinishRate());
            data.put("finishNum",record.getMaxGroupFinishNum());
            data.put("score",record.getScore());
        }
        data.put("award",award);
        return data;
    }

    private void sendMessage(List<String> mobiles){
        String smsContent = "恭喜您在期末布置作业活动中获奖，客服将在7个工作日内联系您，请保持手机通话畅通，奖品会在1月20日后发放，请注意查收。";
        if(CollectionUtils.isNotEmpty(mobiles)){
            for(String mobile : mobiles){
                SmsMessage sms = new SmsMessage();
                sms.setMobile(mobile);
                sms.setSmsContent(smsContent);
                sms.setType(SmsType.TEACHER_TASK_REWARD_NOTIFY.name());
                smsServiceClient.getSmsService().sendSms(sms);
            }
        }
    }

    public static void main(String[] args) {
        int selectIndex = RandomUtils.nextInt(10);
        System.out.println(selectIndex);
    }
}
