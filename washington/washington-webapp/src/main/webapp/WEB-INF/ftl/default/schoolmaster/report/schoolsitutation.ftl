<#import "../schoolmaster.ftl" as com>
<@com.page menuIndex=10 >
    <@sugar.capsule js=["echarts"]/>
<ul class="breadcrumb_vox">
    <li><a href="/schoolmaster/report/schoolsitutation.vpage">大数据报告</a> <span class="divider">/</span></li>
    <li class="active">学校概况</li>
    <li>班级学情分布</li>
    <li>知识能力分析</li>
</ul>
<#if infofailed!false>
    查询失败
<#else>
<div class="r-titleResearch-box">
    <p>
        <#if schoolName?has_content>
            ${schoolName}概况分析
        </#if>
    </p>
    <div style="text-align: center">
        查询时间
        <select id="select-month">
            <option value="01" <#if month?has_content && month == "01">selected="selected"</#if>>1月</option>
            <option value="02" <#if month?has_content && month == "02">selected="selected"</#if>>2月</option>
            <option value="03" <#if month?has_content && month == "03">selected="selected"</#if>>3月</option>
            <option value="04" <#if month?has_content && month == "04">selected="selected"</#if>>4月</option>
            <option value="05" <#if month?has_content && month == "05">selected="selected"</#if>>5月</option>
            <option value="06" <#if month?has_content && month == "06">selected="selected"</#if>>6月</option>
            <option value="07" <#if month?has_content && month == "07">selected="selected"</#if>>7月</option>
            <option value="08" <#if month?has_content && month == "08">selected="selected"</#if>>8月</option>
            <option value="09" <#if month?has_content && month == "09">selected="selected"</#if>>9月</option>
            <option value="10" <#if month?has_content && month == "10">selected="selected"</#if>>10月</option>
            <option value="11" <#if month?has_content && month == "11">selected="selected"</#if>>11月</option>
            <option value="12" <#if month?has_content && month == "12">selected="selected"</#if>>12月</option>
        </select>
        <a id="btn-query" href="javascript:void(0);" class="btn_vox btn_vox_small">
            查询
        </a>
    </div>

    <div id="schoolSitutaionEmbedded" class="tabDiv r-table">
        <table class="table table-hover table-striped table-bordered">
            <#if (schoolSitutationInfoList?size gt 0)!false>
                <tbody>
                <tr>
                    <td>学科</td>
                    <td>老师累计使用人数</td>
                    <td>当月学生使用</td>
                </tr>
                    <#list schoolSitutationInfoList as schoolSitutationInfo>
                    <tr>
                        <td>${(schoolSitutationInfo.subject)!""}</td>
                        <td>${(schoolSitutationInfo.auth_use_tea_num_total)!""}</td>
                        <td>${(schoolSitutationInfo.month_sasc)!""}</td>
                    </tr>
                    </#list>
                </tbody>
            <#else >
                <td>暂无数据</td>
            </#if>
        </table>
    </div>

</div>
<script type="text/javascript">
$(function(){
    $("#btn-query").on("click",function(){
        var month = $("#select-month").val();
        window.location  = '/schoolmaster/report/schoolsitutation.vpage?month=' + month;
    });
});
</script>
</#if>
</@com.page>