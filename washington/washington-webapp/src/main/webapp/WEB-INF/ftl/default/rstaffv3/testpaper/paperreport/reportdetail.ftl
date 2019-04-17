<#import "../../researchstaffv3.ftl" as com>
<@com.page menuIndex=20 menuType="normal">
<div class="row_vox_right">
    <input type="button" id="backOtherPaperBtn" value="返回查看其他试卷">
</div>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">大数据报告</a> <span class="divider">/</span></li>
    <li class="active">知识数据</li>
</ul>
<div class="r-mapResearch-box">
    <div class="r-table">
        <#if reportDetailList?has_content && reportDetailList?size gt 0>
        <p>
            ${(rsPaperAnalysisReports[0].paperName)!""}
        </p>
        <table>
            <thead>
            <tr>
                <#--学校id为0时为区级数据，否则为校级数据-->
                <td><#if reportDetailList[0].schoolId!=0>学校名称<#else>区域名称</#if></td>
                <td>评测人次</td>
                <td>完成率</td>
                <td>听力平均分</td>
                <td>笔试平均分</td>
                <td>总平均分</td>
                <td>薄弱知识点</td>
                <#if reportDetailList[0].schoolId==0><td>学校详情</td></#if>
            </tr>
            </thead>
            <tbody id="reportDetail">
                <#list reportDetailList as report>
                <tr>
                    <#--学校id为0时为区级数据，否则为校级数据-->
                    <td><#if report.schoolId!=0>${report.schoolName}<#else>${report.areaName}</#if></td>
                    <td>${(report.stuNum)!0}</td>
                    <td>
                        <#if (report.finishNum)?has_content && report.finishNum gt 0 && (report.stuNum)?has_content && (report.stuNum) gt 0>
                            ${(report.finishNum/report.stuNum * 100)?string("#0.##")}%
                        <#else>
                            0
                        </#if>
                    </td>
                    <td>
                        <#if (report.finishNum)?has_content && report.finishNum gt 0 && (report.listeningScore)?has_content>
                            ${(report.listeningScore / report.finishNum)?round}
                        <#else>
                            0
                        </#if>
                    </td>
                    <td>
                        <#if (report.finishNum)?has_content && report.finishNum gt 0 && (report.writtenScore)?has_content>
                            ${(report.writtenScore / report.finishNum)?round}
                        <#else>
                            0
                        </#if>
                    </td>
                    <td>
                        <#if report.finishNum gt 0>
                            ${((report.listeningScore + report.writtenScore) / report.finishNum)?round}
                        <#else>
                            0
                        </#if>
                    </td>
                    <td>
                        <#if report.weakPoints?has_content && report.weakPoints?size gt 0>
                            <#list report.weakPoints as weekPoint>
                                ${weekPoint}<#if weekPoint_has_next>，</#if>
                            </#list>
                        </#if>
                    </td>
                    <#if report.schoolId==0><td><a href="/rstaff/testpaper/paperreport/reportdetail.vpage?paperId=${report.paperId}&regionCode=${report.acode}">查看学校</a></td></#if>
                </tr>
                </#list>
            </tbody>
        </table>
        <#else>
            <table>
                <thead>
                 <tr><td colspan="10">暂无数据</td></tr>
                </thead>
            </table>
        </#if>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#backOtherPaperBtn").on("click",function(){
            $17.tongji("教研员-试卷及报告-返回查看其他试卷");
            window.location.href = "/rstaff/testpaper/paperreport/list.vpage";
        });
    });
</script>
</@com.page>
