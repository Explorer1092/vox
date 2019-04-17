<#import "../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" title="首页">
    <@sugar.capsule js=["voxSpread","voxLogs"]/>
    <#--判断是否为PC客户端-->
    <@sugar.capsule js=["VoxExternalPlugin"] />

    <#--新手任务奖品中心-->
    <#include "homeblock/novice_reward.ftl"/>

    <#--首页卡片-->
    <#include "homeblock/newcardlist.ftl"/>
    <#--亲子古诗活动-->
    <#if (ftlmacro.devTestSwitch || currentTeacherWebGrayFunction.isAvailable("AncientPoetry", "PCEntrance"))>
    <#include "homeblock/poetryactivity.ftl"/>
    </#if>
    <#--动态列表-->
    <#include "homeblock/dynamic.ftl"/>

    <#--自动弹出待处理班级请求 - template-->
    <#if (data.capl?index_of("[]") != 0)!false>
        <#include "homeblock/changeclasses.ftl"/>
    </#if>

    <#--惊喜码兑换 - template-->
    <#include "homeblock/surprise.ftl"/>

    <#--首页弹出 template-->
    <#include "homeblock/template_popup.ftl"/>

    <#import "block/record.ftl" as record />
    <@record.UGC_Record/>


    <script type="text/javascript">

        <#if !((currentTeacherDetail.is17XueTeacher())!false)>
        $(function(){
            LeftMenu.focus("main");
        });
        </#if>

        //CrmPopup
        var currentSubject = "${(currentTeacherDetail.subject)!}";
        var currentRootRegionCode = "${(currentTeacherDetail.rootRegionCode)!}";
        var currentCityCode = "${(currentTeacherDetail.cityCode)!}";
        var currentRegionCode = "${(currentTeacherDetail.regionCode)!}";
        var currentHasAuth = ${((currentUser.fetchCertificationState() == "SUCCESS")!false)?string};
        var questItemPopup = [];//Crm Popup
        <#if (pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'IndexQuestionnairePopup'))?has_content>
            questItemPopup = ${(pageBlockContentGenerator.getPageBlockContentHtml('TeacherIndex', 'IndexQuestionnairePopup'))!};
        </#if>


        //public variable
        var VoxTeacherHome = {
            sourceChannel : ${(([370700, 320700]?seq_contains(currentTeacherDetail.cityCode!0))!false)?string},//收集教师来源渠道POPUP
            dataPopup : "${(data.popup)!}",//弹出框
            dataCapl : ${((data.capl?index_of("[]") != 0)!false)?string},//自动弹出待处理班级请求
            tInviteTeacher : ${((currentTeacherWebGrayFunction.isAvailable("Chinese", "Register"))!false)?string},//老师邀请老师
            schoolHasAmb : ${((data.schoolAmbassadorName)?has_content)?string},//学校是否有校园大使
            ambassadorPopup : ${((currentTeacherDetail.schoolAmbassador && .now lt "2016-04-15 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false)?string},//大使新活动
            extensionPopup : ${((.now lt "2016-06-25 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && [330100,610100,120100,410100,130100]?seq_contains(currentTeacherDetail.cityCode))!false)?string},//杭州老师APP推广
            tInviteJunior : ${( (.now lt "2016-04-16 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && currentTeacherWebGrayFunction.isAvailable("JMS", "Invitation"))!false )?string},
            isBindTeacherApp : ${(isBindTeacherApp!false)?string},
            vacationPopup : ${(false && ftlmacro.devTestSwitch ||(.now gt "2017-01-09 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && .now lt "2017-02-12 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss") && currentTeacherDetail.subject != "CHINESE")!false)?string} //寒假作业广告,加false暂时隐藏
        };
    </script>

<#--index.js 含popup logic-->
    <@sugar.capsule js=["teacher.index"]/>

    <#if (currentUser.fetchCertificationState() != "SUCCESS")!false>
        <#--//同校mentor - start-->
        <#import "block/mentor.ftl" as mentorTemplate />
        <@mentorTemplate.notAuto/>
        <#--同校mentor - end//-->
    </#if>

    <#--人盯人客服-->
    <#--<#include "home/onetoone_service.ftl"/>-->
</@shell.page>
