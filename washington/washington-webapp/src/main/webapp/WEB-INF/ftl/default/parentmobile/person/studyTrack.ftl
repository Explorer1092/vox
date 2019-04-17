<#import '../layout.ftl' as layout>

<@layout.page className='PersonstudyTrack bgGray' title="家长APP-学习轨迹" pageJs="no_require_module" >

<#escape x as x?html>
    <#include "../constants.ftl">

    <#if result.success>
        <#assign infos = infos!{} myRank = infos.myRank!{}>

        <#assign studentImgUrl = infos.studentImgUrl!'' studentName = infos.studentName!'' monthUnFinishCount = infos.monthUnFinishCount!0>

        <div class="parentApp-pathHeader">
            <div class="parentApp-pathHeader-head">
                <div class="tp"><img src="${studentImgUrl!''}" alt=""></div>
                <div class="ft">${studentName!''}</div>
            </div>
            <div class="parentApp-pathHeader-msg">
                <div>学号：${infos.studentId!''}</div>
                <div>学豆：${infos.integral!0}</div>
                <div>学霸次数：${myRank.smCount!0}</div>
            </div>
            <div class="parentApp-pathHeader-info">
                <div>
                    <div class="hd">使用一起作业天数</div>
                    <div class="ft">${infos.passDaysCount!0}</div>
                </div>
                <div>
                    <div class="hd">30天内完成作业</div>
                    <div class="ft">${infos.monthFinishCount!0}</div>
                </div>
                <div>
                    <div class="hd">逾期未完成</div>
                    <div class="ft">${infos.monthUnFinishCount}</div>
                </div>
            </div>
        </div>

        <#if !isBindClazz || infos.monthFinishCount == 0 >
        <div class="parentApp-pathSection">
            <#if isBindClazz>
                <div class="parentApp-pathNull">目前作业量还不能形成学习轨迹<br>请完成多份作业后再来查看吧</div>
            <#else>
                <div class="parentApp-pathNull">还没绑定班级，快让孩子找老师绑定班级吧</div>
            </#if>
        </div>
        <#else>
            <#include "./studyTrackModule/situation.ftl">
        </#if>
    <#else>
        <#assign info = result.info errorCode = result.errorCode>
        <#include "../errorTemple/errorBlock.ftl">
    </#if>
</#escape>

</@layout.page>

