/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.babel;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Babel controller implementation.
 * Created by Sadi.Wan on 2014/6/12.
 */
@Controller
@RequestMapping("/student/babel/api")
@NoArgsConstructor
public class BabelApiController extends AbstractBabelController {

    /**
     * 通天塔flash页面展示
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/student/index.vpage";

//        StudentDetail student = currentStudentDetail();
//        Date compareDate = DateUtils.stringToDate("2016-11-05 00:00:00");
//        if (!hasPermission(currentUserId()) || null == student.getClazz() || student.getCreateTime() == null || student.getCreateTime().after(compareDate)) {
//            return "redirect:/student/index.vpage";
//        }
//        model.addAttribute("battleOpen", BabelBossBattleConf.isNowLegalToBattle());
//        return "studentv3/afenti/babel/index";
    }

//    /**
//     * 学生换教材 抄袭自阿分题
//     */
//    @RequestMapping(value = "babel.vpage", method = RequestMethod.GET)
//    public String replaceBook(Model model) {
//        return "redirect:/student/index.vpage";
//
////        if (null == currentStudentDetail().getClazz() || null == currentBabelRole()) {
////            return "redirect:/student/index.vpage";
////        }
////        model.addAttribute("subject", getRequestParameter("subject", ""));
////        return "studentv3/book/babel";
//    }
//
//    /**
//     * 传年级和科目，返回该年级全部课本，按学生地区排列
//     */
//    @RequestMapping(value = "booklist.vpage", method = {RequestMethod.POST})
//    @ResponseBody
//    public MapMessage bookList() {
//        ClazzLevel clazzLevel = ClazzLevel.valueOf(getRequestParameter("clazzLevel", ""));
//        if(null == currentStudentDetail().getClazz()){
//            return MapMessage.errorMessage();
//        }
//        int myLevel = currentStudentDetail().getClazzLevelAsInteger() > 6 ? 6 : currentStudentDetail().getClazzLevelAsInteger();
//        String subjectName = getRequestParameter("subject", "");
//        if (!Subject.isValidSubject(subjectName)) {
//            return MapMessage.errorMessage("参数非法");
//        }
//        Subject subject = Subject.valueOf(subjectName);
//
//        MapMessage rtn = MapMessage.successMessage();
//        Integer region = currentStudentDetail().getStudentSchoolRegionCode();
//        if(null == region){
//            return MapMessage.errorMessage("您不属于任何学校，无法获取课本");
//        }
//        //取指定年级全部书籍
//        switch (subject) {
//            case ENGLISH:
//                List<Book> bookList = englishContentLoaderClient.getExtension()
//                        .loadBooksByRegionCodeAndClassLevelSortRegionCode(region, clazzLevel, regionServiceClient.getExRegionBuffer());
//                for (Iterator<Book> iter = bookList.iterator(); iter.hasNext(); ) {
//                    if (!Integer.valueOf(1).equals(iter.next().getOpenExam()) && myLevel > 2) {//书没有开放应试。不能选
//                        iter.remove();
//                    }
//                }
//                engPaintedSkin(bookList);
//                rtn.add("bookList", bookList);
//                break;
//            case MATH:
//                List<MathBook> mathBookList = mathContentLoaderClient.getExtension()
//                        .loadMathBooksByClassLevelWithSortByRegionCode(clazzLevel, currentStudentDetail().getStudentSchoolRegionCode(), regionServiceClient.getExRegionBuffer());
//                mathPaintedSkin(mathBookList);
//                rtn.add("bookList", mathBookList);
//                break;
//            default:
//                break;
//        }
//        return rtn;
//    }



}