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
        <div class="reviewTable">
            <table class="table-3">
                <thead>
                <tr>
                    <td class="tdCell-1">评审姓名</td>
                    <td class="tdCell-2">评审学校</td>
                    <td class="tdCell-3">评分</td>
                </tr>
                </thead>
                <tbody>
                    <#if juryList?? && juryList?has_content>
                        <#list juryList as jury>
                        <tr>
                            <td class="tdCell-1"><span>${jury.name!''}</span></td>
                            <td class="tdCell-2"><span>${jury.schoolName!''}</span></td>
                            <td class="tdCell-3">
                                <#if jury.judge!false>
                                    <a class="reviewBtn" href="/activity/tangram/studentlist.vpage?schoolId=${jury.schoolId!''}">评</a>
                                </#if>
                            </td>
                        </tr>
                        </#list>
                    <#else>
                        <tr><td colspan="3" style="text-align: center;"><strong>没有数据</strong></td></tr>
                    </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</@layout.page>