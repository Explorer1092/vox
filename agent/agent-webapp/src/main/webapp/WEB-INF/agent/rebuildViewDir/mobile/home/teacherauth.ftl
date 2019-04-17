<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="新老师认证进度" pageJs="teacherauth" footerIndex=1>
    <@sugar.capsule css=['analysis','res','intoSchoEffeNew']/>
<style>
    .resCom-table{
        display: none;
    }
    .resCommissioner-list li:first-child .resCom-table{
        display: block;
    }
</style>
<div class="crmList-box">
    <#--<div class="res-top fixed-head">-->
        <#--<a href="/mobile/performance/index.vpage"><div class="return"><i class="return-icon"></i>返回</div></a>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">新老师认证进度</span>-->
        <#--<#if user?has_content><a href="javascript:;" class="icoPersonal js-changeBtn"></a></#if>-->
        <#--<#if user?has_content><span class="nameInfo">${user.realName!""}</span></#if>-->
    <#--</div>-->
</div>
<div style="background:#fff">
    <div class="crmList-box resources-box">
        <div class="c-main resCom-content">
            <#if schoolTeacherAuthInfo?? && schoolTeacherAuthInfo?size gt 0>
                <ul class="resCommissioner-list">
                    <#list schoolTeacherAuthInfo as cl>
                        <#if cl.authList?has_content && cl.authList?size gt 0>
                            <li class="active">
                                <div class="resCom-info js-togBtn">${cl.schoolName!"--"}(${cl.authList?size})</div>
                                <div class="resCom-table">
                                    <table cellpadding="0" cellspacing="0">
                                        <tbody>
                                            <#list cl.authList as teacher>
                                                <tr class="js-teacher" nameId="${teacher.teacherId!0}">
                                                    <td style="width:5.5rem">
                                                        <span class="name" style="display:inline-block;vertical-align: middle;width:5.5rem">${teacher.teacherName!'--'}
                                                            <span class="icon-box">
                                                            <#if teacher.isEnglish?has_content && teacher.isEnglish ><i class="icon-ying" style="vertical-align: middle"></i></#if>
                                                                <#if teacher.isMath?has_content && teacher.isMath><i class="icon-shu" style="vertical-align: middle"></i></#if>
                                                                <#if teacher.isChinese?has_content && teacher.isChinese><i class="icon-yu" style="vertical-align: middle"></i></#if>
                                                        </span>
                                                            <#if teacher.isFollow?? && teacher.isFollow><i class="icon-gen" style="vertical-align: middle"></i> </#if>
                                                        </span>
                                                    </td>
                                                    <td>${teacher.registerDate!'--'}</td>
                                                    <td>布置${teacher.authCond!0}</td>
                                                </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </li>
                        </#if>
                    </#list>
                </ul>
            </#if>
        </div>
    </div>
</div>
<script src="https://cdn-cnc.17zuoye.cn/public/script/voxLogs.js?v=2016-06-02"></script>
<script>
    <#if user?has_content>
    var userName = "${user.realName!""}";
    var url = "/mobile/performance/choose_agent.vpage?breakUrl=teacherauth&selectedUser=${user.id!0}&needCityManage=1";
    <#else>
    var userName = "";
    var url = "/mobile/performance/choose_agent.vpage?breakUrl=teacherauth";
    </#if>
    var userId = "${requestContext.getCurrentUser().getUserId()!0}";
</script>
</@layout.page>