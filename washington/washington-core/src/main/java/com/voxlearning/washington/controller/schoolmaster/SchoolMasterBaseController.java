package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.ResearchStaffUserType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.washington.support.AbstractController;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: liuyong
 * @Description: 在这个controller里面做一些基础的判断
 * @Date:Created in 2018-06-19 10:30
 */

public class SchoolMasterBaseController extends AbstractController {

    @Inject private RaikouSDK raikouSDK;

    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject public NewContentLoaderClient newContentLoaderClient;


    public List<String> getGradeList(){
        List<String> gradeList = new LinkedList<>();
        gradeList.add(ClazzLevel.FIRST_GRADE.getDescription());
        gradeList.add(ClazzLevel.SECOND_GRADE.getDescription());
        gradeList.add(ClazzLevel.THIRD_GRADE.getDescription());
        gradeList.add(ClazzLevel.FOURTH_GRADE.getDescription());
        gradeList.add(ClazzLevel.FIFTH_GRADE.getDescription());
        gradeList.add(ClazzLevel.SIXTH_GRADE.getDescription());
        return gradeList;
    }

    public List<String> getSubjectList(){
        List<String> subjectList = new LinkedList<>();
        subjectList.add(Subject.CHINESE.getValue());
        subjectList.add(Subject.MATH.getValue());
        subjectList.add(Subject.ENGLISH.getValue());
        return subjectList;
    }

    public static Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator());

        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public List<Clazz> getClazzesByShooolId(Long schoolId) {
        List<Clazz> clazzInfoList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(schoolId)
                .enabled()
                .toList();
        List<Long> clazzIds = clazzInfoList.stream().map(Clazz::getId).collect(Collectors.toList());
        List<GroupMapper> clazzGroups = deprecatedGroupLoaderClient.loadClazzGroups(clazzIds).values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<Clazz> clazzList = new LinkedList<>();
        Set<Long> clazzExistGroupIds = new HashSet<>();
        for (GroupMapper mapper : clazzGroups) {
            if (mapper != null && !clazzExistGroupIds.contains(mapper.getClazzId())) {
                Clazz clazz = raikouSDK.getClazzClient()
                        .getClazzLoaderClient()
                        .loadClazz(mapper.getClazzId());
                clazzList.add(clazz);
                clazzExistGroupIds.add(clazz.getId());
            }
        }
        return clazzList;
    }

    public String getBookId(Long schoolId, String clazz,String grade,String subject) {
        List<Clazz> schoolClazzList = getClazzesByShooolId(schoolId);
        schoolClazzList = schoolClazzList.stream()
                .filter(p -> p.isSystemClazz() && p.getClazzLevel().getLevel() == SafeConverter.toInt(grade))
                .collect(Collectors.toList());

        List<Long> clazzL = new ArrayList<>();
        if(StringUtils.isEmpty(clazz) || clazz.equals("0")){
            clazzL = schoolClazzList.stream().map(Clazz::getId).collect(Collectors.toList());
        }else{
            clazzL.add(SafeConverter.toLong(clazz));
        }

        Map<Long, List<GroupMapper>> groupMap = groupLoaderClient.loadClazzGroups(clazzL);
        List<Long> groupIds = new ArrayList<>();
        if (!MapUtils.isEmpty(groupMap)){
            groupIds = groupMap.values().stream().flatMap(Collection::stream).map(GroupMapper::getId).collect(Collectors.toList());
        }
        List<NewClazzBookRef> clazzBookRefList = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
        Map<String,Integer> bookIdMap = new LinkedHashMap<>();
        for(NewClazzBookRef temp : clazzBookRefList){
            if(temp.getSubject().equals(subject)){
                Integer counts = bookIdMap.get(temp.getBookId());
                if(counts == null){
                    counts = 1;
                }else{
                    counts += 1;
                }
                bookIdMap.put(temp.getBookId(),counts);
            }
        }
        Map<String, Integer> bookIdMapSort = sortMapByValue(bookIdMap);
        String bookId = "";
        if(bookIdMapSort != null && !bookIdMapSort.isEmpty()){
            for (Map.Entry<String, Integer> entry : bookIdMapSort.entrySet()) {
                bookId = entry.getKey();
                break;
            }
        }
        return bookId;
    }


    public String getBaseDomain(){
        //String mainUrl = RuntimeMode.isDevelopment() ? "//www.test.17zuoye.net" : ProductConfig.getMainSiteBaseUrl();
        String mainUrl = "http://www.test.17zuoye.net";
        if(RuntimeMode.isProduction()){
            mainUrl = "http://www,17zuoye.com";
        }else if(RuntimeMode.isStaging()){
            mainUrl = "http://www.staging.17zuoye.net";
        }
        return mainUrl;
    }

    public List<Integer> getRegionCodesParam(ResearchStaff researchStaff) {
        List<Integer> regionCodesParam = new ArrayList();
        Set<Long> regionCodes = researchStaff.getManagedRegion().getAreaCodes();
        if(!regionCodes.isEmpty()){
            Iterator<Long> regionIt = regionCodes.iterator();
            while(regionIt.hasNext()){
                regionCodesParam.add(SafeConverter.toInt(regionIt.next()));
            }
        }
        return regionCodesParam;
    }

    public List<Integer> getCityCodesParam(ResearchStaff researchStaff) {
        List<Integer> cityCodesParam = new ArrayList<>();
        Set<Long> cityCodes = researchStaff.getManagedRegion().getCityCodes();
        if(!cityCodes.isEmpty()){
            Iterator<Long> cityIt = cityCodes.iterator();
            while(cityIt.hasNext()){
                cityCodesParam.add(SafeConverter.toInt(cityIt.next()));
            }
        }
        return cityCodesParam;
    }


    public void reckonRanking(List<Map<String,Object>> params){
        String temp="";
        int num=0;
        int final_num=0;
        int temp_num=0;
        for (Map<String,Object> tempMap : params) {
            //如果临时变量和错误率相等 说明本次错误率和上次是一致的 那么排名也应该和上次一样，但是要记录这种情况出现几次，最后如果当前排名不和上次一致时需要将排名加上空挡的增量
            Integer val = (Integer) tempMap.get("value");
            if(temp.equals(String.valueOf(val))){
                temp_num++;
                final_num=num;
            }else{
                num++;
                final_num=num+temp_num;
                num=final_num;
                temp_num=0;
            }
            temp=String.valueOf(val);
            tempMap.put("rank", final_num);
        }
    }

    public List<Map<String,Object>> getTerms() {
        List<Map<String,Object>> terms = new LinkedList<>();
        Map<String,Object> map0 = new LinkedHashMap<>();
        map0.put("startDate","20180201");
        map0.put("endDate","20180731");
        map0.put("jie",2017);
        map0.put("name","17~18下学期");
        map0.put("value","2017-NEXT_TERM");
        terms.add(map0);

        Map<String,Object> map1 = new LinkedHashMap<>();
        map1.put("startDate","20180801");
        map1.put("endDate","20190131");
        map1.put("jie",2018);
        map1.put("name","18~19上学期");
        map1.put("value","2018-LAST_TERM");
        terms.add(map1);

        Map<String,Object> map2 = new LinkedHashMap<>();
        map2.put("startDate","20190201");
        map2.put("endDate","20190731");
        map2.put("jie",2018);
        map2.put("name","18~19下学期");
        map2.put("value","2018-NEXT_TERM");
        terms.add(map2);

        Map<String,Object> map3 = new LinkedHashMap<>();
        map3.put("startDate","20190801");
        map3.put("endDate","20200131");
        map3.put("jie",2019);
        map3.put("name","19~20上学期");
        map3.put("value","2019-LAST_TERM");
        terms.add(map3);

        Map<String,Object> map4 = new LinkedHashMap<>();
        map4.put("startDate","20200201");
        map4.put("endDate","20200731");
        map4.put("jie",2019);
        map4.put("name","19~20下学期");
        map4.put("value","2019-NEXT_TERM");
        terms.add(map4);

        Map<String,Object> map5 = new LinkedHashMap<>();
        map5.put("startDate","20200801");
        map5.put("endDate","20210131");
        map5.put("jie",2020);
        map5.put("name","20~21上学期");
        map5.put("value","2020-LAST_TERM");
        terms.add(map5);

        Map<String,Object> map6 = new LinkedHashMap<>();
        map6.put("startDate","20210201");
        map6.put("endDate","20210731");
        map6.put("jie",2020);
        map6.put("name","20~21下学期");
        map6.put("value","2020-NEXT_TERM");
        terms.add(map6);

        Map<String,Object> map7 = new LinkedHashMap<>();
        map7.put("startDate","20210801");
        map7.put("endDate","20220131");
        map7.put("jie",2021);
        map7.put("name","21~22上学期");
        map7.put("value","2021-LAST_TERM");
        terms.add(map7);

        Map<String,Object> map8 = new LinkedHashMap<>();
        map8.put("startDate","20220201");
        map8.put("endDate","20220731");
        map8.put("jie",2021);
        map8.put("name","21~22下学期");
        map8.put("value","2021-NEXT_TERM");
        terms.add(map8);

        Map<String,Object> map9 = new LinkedHashMap<>();
        map9.put("startDate","20220801");
        map9.put("endDate","20230131");
        map9.put("jie",2022);
        map9.put("name","22~23上学期");
        map9.put("value","2022-LAST_TERM");
        terms.add(map9);

        Map<String,Object> map10 = new LinkedHashMap<>();
        map10.put("startDate","20230201");
        map10.put("endDate","20230731");
        map10.put("jie",2022);
        map10.put("name","22~23下学期");
        map10.put("value","2022-NEXT_TERM");
        terms.add(map10);

        Map<String,Object> map11 = new LinkedHashMap<>();
        map11.put("startDate","20230801");
        map11.put("endDate","20240131");
        map11.put("jie",2023);
        map11.put("name","23~24上学期");
        map11.put("value","2023-LAST_TERM");
        terms.add(map11);

        Map<String,Object> map12 = new LinkedHashMap<>();
        map12.put("startDate","20240201");
        map12.put("endDate","20240731");
        map12.put("jie",2023);
        map12.put("name","23~24下学期");
        map12.put("value","2023-NEXT_TERM");
        terms.add(map12);

        String currentDate = DateUtils.dateToString(new Date(),"yyyyMMdd");
        int index = 0;
        for(int i=0; i<terms.size(); i++){
            Map<String,Object> temp = terms.get(i);
            String startDate = (String) temp.get("startDate");
            String endDate = (String) temp.get("endDate");
            if(currentDate.compareTo(startDate)>=0 && currentDate.compareTo(endDate)<=0){
                index = i;
                break;
            }
        }
        List<Map<String,Object>> newTerms = new LinkedList<>();
        //每次显示两个学期
        if(index > 0){
            newTerms.add(terms.get(index-1));
        }
        newTerms.add(terms.get(index));
        return newTerms;
    }

    public void getSkillSeriesData(Map<String, Double> schoolSkillMap, Map<String, Double> areaSkillMap, Map<String,Double> nationSkillMap, List<String> skillNames, List<Double> schoolSeriesData, List<Double> areaSeriesData, List<Double> nationSeriesData) {
        for(String skillName : skillNames){
            Double schoolRate = schoolSkillMap.get(skillName);
            if(schoolRate == null){
                schoolSeriesData.add(0D);
            }else{
                BigDecimal b = new BigDecimal(schoolRate*100);
                schoolSeriesData.add(b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            Double areaRate = areaSkillMap.get(skillName);
            if(areaRate == null){
                areaSeriesData.add(0D);
            }else{
                BigDecimal b = new BigDecimal(areaRate*100);
                areaSeriesData.add(b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            Double nationRate = nationSkillMap.get(skillName);
            if(nationRate == null){
                nationSeriesData.add(0D);
            }else{
                BigDecimal b = new BigDecimal(nationRate*100);
                nationSeriesData.add(b.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
    }

    public String validatePasswdAndMobile(Long userId){
        UserAuthentication authentication = userLoaderClient.loadUserAuthentication(userId);
        ResearchStaff researchStaff = currentResearchStaff();
        ResearchStaff.ManagedRegion managedRegion = researchStaff.getManagedRegion();
        if(StringUtils.isBlank(authentication.getSensitiveMobile())){
            if(managedRegion != null && managedRegion.getResarchStaffUserType() != null && ResearchStaffUserType.RSTAFF.getType() == managedRegion.getResarchStaffUserType()){
                return "redirect:/rstaff/admincenter.vpage?module=accountsafe&type=2&errtype=1";
            }
        }else if(validatePassword(authentication)){
            if(managedRegion != null && managedRegion.getResarchStaffUserType() != null && ResearchStaffUserType.RSTAFF.getType() == managedRegion.getResarchStaffUserType()){
                return "redirect:/rstaff/admincenter.vpage?module=accountsafe&type=1&errtype=2";
            }
        }
        return "";
    }

    public boolean validatePassword(UserAuthentication authentication){
        String[] simplePasswds = new String[]{"1","123456"};
        for(String pd : simplePasswds){
            boolean res = authentication.verifyPassword(pd);
            if(res){
                return true;
            }
        }
        return false;
    }
}

class MapValueComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> me1, Map.Entry<String, Integer> me2) {
        return me2.getValue().compareTo(me1.getValue());
    }
}
