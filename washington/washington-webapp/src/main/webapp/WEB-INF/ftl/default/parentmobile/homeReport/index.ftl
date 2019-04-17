<#import '../layout.ftl' as layout>
<#assign title = (studentName!"您孩子") + "的学业报告">
<@layout.page className='PersonstudyTrack bgGray' title="学业报告" pageJs="studyTrack" globalJs = []>

<#escape x as x?html>
    <#include "../constants.ftl">

    <#if result.success>
        <#if isGraduate!false ><#--是否毕业判断-->
            <div class="parentApp-messageNull">暂时不支持小学毕业账号</div>
        <#else>
            <#assign infos = infos!{} myRank = infos.myRank!{}>

            <#assign studentImgUrl = infos.studentImgUrl!'' studentName = infos.studentName!'' monthUnFinishCount = infos.monthUnFinishCount!0>

            <div class="parentApp-topBar" id="J-do-title">
                <div class="topBox">
                    <div class="topHead">${studentName}的学业报告</div>
                </div>
            </div>

            <#assign monthUnFinishCount = infos.monthUnFinishCount!0>

            <div class="parentApp-pathHeader parentApp-pathHeader-blue">
                <div class="parentApp-pathHeader-info">
                    <div>
                        <div class="hd">学霸次数</div>
                        <div class="ft">${myRank.smCount!0}</div>
                    </div>
                    <div>
                        <div class="hd">30天内完成作业</div>
                        <div class="ft">${infos.monthFinishCount!0}</div>
                    </div>
                    <div>
                        <div class="hd">逾期未完成</div>
                        <div class="ft">${monthUnFinishCount}</div>
                    </div>
                </div>
            </div>

            <div class="parentApp-pathSection  parentApp-pathWhite">
                <div class="parentApp-pathInfo">
                    <div>
                        <#if (infos.totalWrongCount!0)?number == 0 >
                            <span class="infoText">本月作业错题：无错题</span>
                        <#else>
                            <span class="infoText">本月作业错题：${infos.totalWrongCount}题</span><a href="/parentMobile/homework/homeworkReport.vpage?sid=${sid}&subject=ENGLISH" ${buildTrackData("report|faultnotes_click")} class="infoBtn doTrack">查看错题本</a>
                        </#if>
                    </div>
                    <div><span class="infoText">学豆：${infos.integral!0}</span><a href="/parentMobile/home/integralchip.vpage?sid=${sid}&student_name=${studentName}" class="infoBtn doTrack" data-track="report|beanrecord_click">查看近期学豆记录</a></div>
                    <div><span class="infoText">本月课堂奖励学豆：${infos.reward_integral!0}</span><a href="/parentMobile/homework/loadsmart.vpage?sid=${sid}" class="infoBtn doTrack" data-track="report|loadsmart_click">查看近期学校表现</a></div>
                    <#list (infos.reportList![]) as report>
                        <div><span class="infoText">${report.title!''}</span><a href="${report.url!"javascript:void(0);"}" class="infoBtn doTrack" data-track="report|loadsmart_click">查看</a></div>
                    </#list>
                </div>
            </div>

            <#if !isBindClazz || infos.monthFinishCount == 0 >
                <div class="parentApp-pathSection">
                    <#if isBindClazz>
                        <div class="parentApp-pathNull">近期作业量还不能形成学业报告，请完成多份作业后再来查看吧</div>
                    <#else>
                        <div class="parentApp-pathNull">还没绑定班级，快让孩子找老师绑定班级吧</div>
                    </#if>
                </div>
            <#else>
                <#include "./module/situation.ftl">
                <#include "./module/result.ftl">
            </#if>
        </#if>
    <#else>
        <em class="hide doAutoTrack" data-track="report|reportv2_error"></em>
        <#assign info = result.info errorCode = result.errorCode>
        <#include "../errorTemple/errorBlock.ftl">
    </#if>
</#escape>

</@layout.page>

