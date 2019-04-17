/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.lang.convert.SafeConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-9-16.
 */
public class TestOpenApiActiveCard {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            String[] schoolMetaList = {
                    "330106#学军中学#公立#高中",
                    "330103#杭州高级中学#公立#高中",
                    "330211#镇海中学#公立#高中",
                    "330212#效实中学#公立#高中",
                    "330381#瑞安市第二中学#公立#高中",
                    "330382#柳市镇第一中学#公立#高中",
                    "330302#温州第二高级中学#公立#高中",
                    "330302#温州第二十二中学#公立#高中",
                    "370705#山东省潍坊第一中学#公立#高中",
                    "370705#潍坊中学#公立#高中",
                    "370782#山东省诸城第一中学#公立#高中",
                    "370782#山东省诸城繁华中学#公立#高中",
                    "370702#潍坊第七中学#公立#高中",
                    "370705#潍坊行知中学#公立#高中",
                    "370703#寒亭一中#公立#高中",
                    "370782#诸城市实验中学#公立#高中",
                    "370704#潍坊市第四中学#公立#高中",
                    "370103#实验中学#公立#高中",
                    "370102#山师附中#公立#高中",
                    "370102#外国语高中#公立#高中",
                    "370102#济南一中#公立#高中",
                    "370103#济南中学#公立#高中",
                    "370103#济南三中#公立#高中",
                    "370102#济南七中#公立#高中",
                    "370112#历城五中#公立#高中",
                    "370104#济南九中#公立#高中",
                    "370113#长清一中#公立#高中",
                    "110108#北达资源#公立#高中",
                    "110108#人大附中#公立#高中",
                    "110108#清华附中#公立#高中",
                    "110108#一零一中学#公立#高中",
                    "110108#首都师范大学附属中学#公立#高中",
                    "110108#海淀实验#公立#高中",
                    "110108#教师进修学校附属海淀实验学校#公立#高中",
                    "110108#北大附中#公立#高中",
                    "110102#北师大附中#公立#高中",
                    "110102#15中学#公立#高中",
                    "110102#十四中学#公立#高中",
                    "110105#第八十中学#公立#高中",
                    "110105#东方德才高中部#公立#高中",
                    "110105#日坛中学高中部#公立#高中",
                    "110105#和平街一中#公立#高中",
                    "110105#陈经纶中学#公立#高中",
                    "110105#九十四中学#公立#高中",
                    "110106#航天中学高中部#公立#高中",
                    "110106#北京师范大学第四附属中学高中部#公立#高中",
                    "110113#顺义一中#公立#高中",
                    "110113#杨镇一中#公立#高中",
                    "110101#东城广渠门中学#公立#高中",
                    "110101#171中学#公立#高中",
                    "110111#北京四中房山校区#公立#高中",
                    "110111#北京师范大学良乡附属中学#公立#高中",
                    "110112#通州区第四中学#公立#高中",
                    "110112#人大附中#公立#高中",
                    "110114#北京昌平区第一中学#公立#高中",
                    "110114#北京昌平区第二中学#公立#高中",
                    "120101#天津一中#公立#高中",
                    "120116#大港一中#公立#高中",
                    "120105#天津二中#公立#高中",
                    "120105#十四中#公立#高中",
                    "120104#育红中学#公立#高中",
                    "120106#民族中学#公立#高中",
                    "120103#四十一中#公立#高中",
                    "440605#南海区石门中学#公立#高中",
                    "440606#顺德一中#公立#高中",
                    "440606#顺德国华纪念中学#公立#高中",
                    "440605#华师附中南海实验高中#公立#高中",
                    "440606#顺德郑裕彤中学#公立#高中",
                    "440604#佛山市第三中学#公立#高中",
                    "440606#顺德大良实验中学#公立#高中",
                    "440604#佛山四中#公立#高中",
                    "440304#深圳外国语学校高中部#公立#高中",
                    "440304#深圳高级中学#公立#高中",
                    "440304#深圳实验学校高中部#公立#高中",
                    "440306#观澜中学#公立#高中",
                    "440305#深圳南山外国语学校滨海中学#公立#高中",
                    "440307#华中师范大学龙岗附属中学#公立#高中",
                    "440308#深圳外国语学校#公立#高中",
                    "440306#深圳实验学校高中部#公立#高中",
                    "440306#松岗中学#公立#高中",
                    "440306#西乡中学#公立#高中",
                    "320506#江苏省苏州中学#公立#高中",
                    "320508#江苏省苏州第一中学#公立#高中",
                    "320102#南外本校#公立#高中",
                    "320105#南外仙林#公立#高中",
                    "320106#南师附中#公立#高中",
                    "320104#南京第一中学#公立#高中",
                    "320106#二十九中#公立#高中",
                    "320105#中华中学#公立#高中",
                    "320106#十三中#公立#高中",
                    "320102#九中#公立#高中",
                    "320115#南师江宁分校#公立#高中",
                    "320114#雨花台中学#公立#高中",
                    "320106#田家炳中学#公立#高中",
                    "140106#太原五中#公立#高中",
                    "140105#山西大学附属中学#公立#高中",
                    "140106#太原市成成中学#公立#高中",
                    "140107#第二外国语学校（高中十八中）#公立#高中",
                    "140107#太原市十二中#公立#高中",
                    "140106#太原师范学院附属中学#公立#高中",
                    "140109#太原市外国语学校#公立#高中",
                    "140105#小店一中#公立#高中",
                    "140106#太原进山中学#公立#高中",
                    "610103#西安交通大学附属中学#公立#高中",
                    "610103#西安市铁一中#公立#高中",
                    "410102#郑州外国语中学#公立#高中",
                    "410102#郑州一中#公立#高中",
                    "500106#南开中学#公立#高中",
                    "500106#重庆八中#公立#高中",
                    "440114#广州市花都区秀全中学#公立#高中",
                    "440104#广州市第七中学#公立#高中",
                    "440113#广东仲元中学#公立#高中",
                    "440104#执信中学#公立#高中",
                    "440106#华南师范大学附属中学#公立#高中",
                    "440106#广州市四十七中学#公立#高中",
                    "440112#广州八十六中#公立#高中",
                    "110101#东莞市高级中学#公立#高中",
                    "370203#青岛第九中学#公立#高中",
                    "370202#青岛三十九中#公立#高中",
                    "330702#金华一中#公立#高中",
                    "330784#永康市第一中学#公立#高中",
                    "330481#上海外国语大学附属宏达高级中学#公立#高中",
                    "330402#嘉兴市第一中学#公立#高中",
                    "340103#六中#公立#高中",
                    "130606#保定市第一中学#公立#高中",
                    "130606#保定市第三中学#公立#高中",
                    "430406#衡阳市八中（成章）#公立#高中",
                    "430408#衡阳市一中（船山）#公立#高中",
                    "370303#淄博市实验中学#公立#高中",
                    "370306#淄博六中#公立#高中",
                    "430203#株洲市一中#公立#高中",
                    "430203#株洲市南方中学#公立#高中",
                    "321111#镇江高级中学#公立#高中",
                    "321102#镇江市第一中学#公立#高中",
                    "430602#湖南岳阳市第一中学#公立#高中",
                    "430602#岳阳市第十五中学#公立#高中",
                    "321012#江都中学#公立#高中",
                    "321081#仪征中学#公立#高中",
                    "370602#山东省烟台第二中学#公立#高中",
                    "370602#山东省烟台第一中学#公立#高中",
                    "130203#河北唐山一中#公立#高中",
                    "130203#河北唐山开滦一中#公立#高中",
                    "321204#姜堰二中#公立#高中",
                    "321204#姜堰中学#公立#高中",
                    "331002#台州第一中学#公立#高中",
                    "331002#书生中学（高中部）#公立#高中",
                    "130102#石家庄市第一中学#公立#高中",
                    "130104#石家庄市第十七中学#公立#高中",
                    "330681#诸暨中学#公立#高中",
                    "330603#柯桥中学#公立#高中",
                    "410311#洛阳市第一高级中学#公立#高中",
                    "410305#洛阳理工学院附属中学#公立#高中",
                    "371302#临沂第一中学#公立#高中",
                    "371302#临沂第四中学#公立#高中",
                    "330304#南浦实验中学#公立#初中",
                    "370105#外国语初中#公立#初中",
                    "110107#石景山景山远洋分校#公立#初中",
                    "110108#北京海淀外国语实验学校#公立#初中",
                    "110115#大兴一中#公立#初中",
                    "110107#石景山九中#公立#初中",
                    "110113#杨镇二中#公立#初中",
                    "110114#北京昌平区长陵中学#公立#初中",
                    "120106#天津市方舟实验中学#公立#初中",
                    "440604#佛山市实验学校#公立#初中",
                    "440606#翁佑中学#公立#初中",
                    "440305#南山第二外国语学校#公立#初中",
                    "320116#六合励志学校#公立#初中",
                    "320106#科利华中学#公立#初中",
                    "320116#南京六合区科利华分校#公立#初中",
                    "140109#万柏林五中#公立#初中",
                    "420111#华师一附中#公立#初中",
            };

            Map<String, Integer> SCHOOL_TYPE_DEF = new HashMap<>();
            SCHOOL_TYPE_DEF.put("小学", 1);
            SCHOOL_TYPE_DEF.put("初中", 2);
            SCHOOL_TYPE_DEF.put("高中", 4);


            for (String schoolMeta : schoolMetaList) {
                String[] metaInfo = schoolMeta.split("#");
                int region = SafeConverter.toInt(metaInfo[0]);
                String schoolName = metaInfo[1];
                String type = metaInfo[2];
                int level = SCHOOL_TYPE_DEF.get(metaInfo[3]);

                System.out.println(region + "--" + schoolName + "--" + level);

            }


            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
