<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=22 menuType="normal">
<div class="row_vox_right">
    <a class="btn_vox btn_vox_small" href="/rstaff/oral/index.vpage">返回查看其他报告</a>
</div>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">组卷统考</a> <span class="divider">/</span></li>
    <li class="active">口语测试</li>
    <span class="divider">/</span></li>
    <li class="active">报告数据</li>
</ul>

<div class="r-mapResearch-box">
    <div class="r-table">
        <#if reports?has_content>
            <table id="reportTable">
                <thead>
                <tr>
                    <td><#if schoolStat?has_content && schoolStat>学校<#else>区域</#if>名称</td>
                    <td>完成人数</td>
                    <td>应完成人数</td>
                    <#if partList?has_content>
                        <#list partList as part>
                            <td>${part}</td>
                        </#list>
                    </#if>
                    <td>总分</td>
                </tr>
                </thead>
                <tbody>
                    <#list reports as report>
                    <tr>
                        <#if schoolStat?has_content && schoolStat>
                            <td>${report.schoolName}</td>
                        <#else>
                            <td><a style="text-decoration: underline;" href="oralreport.vpage?id=${id!}&acode=${report.acode!}">${report.aname!}</a></td>
                        </#if>
                        <td>${report.joinNum!0}</td>
                        <td>${report.stuNum!0}</td>
                        <#if partList?has_content>
                            <#assign mp = report.fetchPartsMap() />
                            <#list partList as part>
                                <#if (mp?keys)?seq_contains(part) >
                                    <td>${(mp[part])?string("###")}</td>
                                <#else>
                                    <td>0</td>
                                </#if>
                            </#list>
                            <td>${(report.fetchTotalScore())?string("###")}</td>
                        </#if>
                    </tr>
                    </#list>
                </tbody>
            </table>
        <#else>
            <table>
                <thead>
                <tr>
                    <td>暂无相关数据</td>
                </tr>
                </thead>
            </table>
        </#if>
    </div>
</div>
</@com.page>