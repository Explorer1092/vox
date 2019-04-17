<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="注册日报">

<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <#--<#if backRegion?? && backRegion=="top" >-->
                <#--<a href="index.vpage" class="headerBack">&lt;&nbsp;返回</a>-->
            <#--<#elseif backRegion?? &&  backRegion =="index" >-->
                <#--<a href="daily_performance_summary.vpage" class="headerBack">&lt;&nbsp;返回</a>-->
            <#--<#else>-->
                <#--<a href="daily_performance_summary.vpage?region=${backRegion!''}" class="headerBack">&lt;&nbsp;返回</a>-->
            <#--</#if>-->
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerSelect">
                <span >当期业绩汇总</span>
                <select id="selectType">
                    <option value="daily_performance.vpage?date=${curDate!''}&region=${curRegion!0}">业绩日报</option>
                    <option value="week_performance.vpage">业绩周报</option>
                    <option value="month_performance.vpage">业绩月报</option>
                    <option value="" selected>当期业绩汇总</option>
                </select>
            </div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">区域</div>
            <div class="side-fr side-time side-width">注册</div>
            <div class="side-fr side-time side-width">认证</div>
        </div>
    </li>
    <#if performanceData??>
        <#list performanceData?values as value>
            <#if value['studentRegister'] gt 0 || value['studentAuth'] gt 0>
            <li>
                <#if value['type']?? && value['type']=="region">
                    <a href="daily_performance_summary.vpage?region=${value['regionCode']!''}&pregion=${pregion!''}" class="link link-ico">
                <#elseif value['type']?? && value['type']=="school">
                    <a href="javascript:void(0)" class="link">
                <#elseif value['type']?? && value['type']=="summary">
                    <div class="link">
                </#if>
                    <div class="side-fl" style="width: 48%;">${value['regionName']!'未知'}</div>
                    <div class="side-fr side-orange side-width">${value['studentRegister']!''}</div>
                    <div class="side-fr side-orange side-width">${value['studentAuth']!''}</div>
            </a>
            </li>
            </#if>
        </#list>
    </#if>
</ul>

<script type="text/javascript">
    $("#selectType").change(function(){
        var url = $("#selectType option:selected").val();
        window.location.href = url;
    });
</script>
</@layout.page>