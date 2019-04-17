<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title="七巧板活动评审"
pageJs=["tangram"]
pageCssFile={"tangram" : ["public/skin/css/tangram/css/skin","public/skin/css/skin"]}
pageJsFile={"tangram" : "public/script/activity/tangram"}
>
<div class="qqb-header">
    <div class="inner">
        <a href="/" class="logo"></a>
        <span>七巧板作品评选</span>
        <a class="logout" href="javascript:;">退出登录</a>
    </div>
</div>
<div class="qqb-main">
    <div class="inner">
        <div class="selectSchool">
            <select class="schoolListLink">
                <#if schoolList?? && schoolList?has_content>
                    <#list schoolList as school>
                        <option value="${school.schoolId!''}">${school.schoolName!''}</option>
                    </#list>
                </#if>
            </select>
        </div>
        <div class="reviewTable">
            <table class="table-5">
                <thead>
                <tr>
                    <td class="tdCell-1">编号</td>
                    <td class="tdCell-2">班级</td>
                    <td class="tdCell-3">姓名</td>
                    <td class="tdCell-4">分数</td>
                    <td class="tdCell-5">评分</td>
                </tr>
                </thead>
                <tbody>
                <#if studentList?? && studentList?has_content>
                    <#list studentList as student>
                        <tr>
                            <td class="tdCell-1"><span>${student.code!''}</span></td>
                            <td class="tdCell-2"><span>${student.class!''}</span></td>
                            <td class="tdCell-3"><span>${student.name!''}</span></td>
                            <td class="tdCell-4"><span><#if student.judged!false>${student.score!'--'}<#else>--</#if></span></td>
                            <td class="tdCell-5"><a class="reviewBtn" href="/activity/tangram/student.vpage?student=${student.id!''}">评</a></td>
                        </tr>
                    </#list>
                <#else>
                <tr><td colspan="5" style="text-align: center;"><strong>没有数据</strong></td></tr>
                </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</@layout.page>