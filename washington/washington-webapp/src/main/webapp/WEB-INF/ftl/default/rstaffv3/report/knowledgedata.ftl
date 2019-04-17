<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=10>
<@sugar.capsule js=["echarts"]/>
<ul class="breadcrumb_vox">
    <li><a href="/rstaff/report/knowledgedata.vpage">大数据报告</a> <span class="divider">/</span></li>
    <li class="active">知识数据</li>
</ul>
<div class="r-titleResearch-box">
    <p>
        <#if year?has_content>
            ${year}学年<#if term == "1">第一学期<#else>第二学期</#if>${(currentUser.formatManagedRegionStr())!}小学英语数据分析报告
        <#else>
            ${termText}${(currentUser.formatManagedRegionStr())!}小学英语数据分析报告
        </#if>
    </p>
    <p> 数据更新日期：${updateDate}</p>
    <div style="text-align: center">
        查询历史数据:
        <select id="select-year">
            <#list historyYears as yearUnit>
                <option value="${yearUnit}" <#if year?has_content && year == yearUnit>selected="selected"</#if>>${yearUnit}</option>
            </#list>
        </select>
        学年
        <select id="select-term">
            <option value="1" <#if term?has_content && term == "1">selected="selected"</#if>>上学期</option>
            <option value="2" <#if term?has_content && term == "2">selected="selected"</#if>>下学期</option>
        </select>
        <a id="btn-query" href="javascript:void(0);" class="btn_vox btn_vox_small">
            查询
        </a>
        <a id="btn-download" href="javascript:void(0);" class="btn_vox btn_vox_small">
            下载报告
        </a>
    </div>
    <#--<a class="btn_vox btn_vox_warning" href="javascript:void(0);">-->
        <#--下载-->
    <#--</a>-->
</div>
<div class="r-mapResearch-box">
    <div class="y-nav-tip">
        <ul id="tabUL">
            <li><a data-tab-ref="totalEmbedded" id="skillChartsData" data-url="/rstaff/report/skillchart.vpage?year=${year!}&term=${term!}" href="javascript:void(0);">总表</a></li>
            <li><a id="skillData" data-tab-ref="skillEmbedded" data-url="/rstaff/report/skilldata.vpage?year=${year!}&term=${term!}" href="javascript:void(0);">语言技能</a></li>
            <li><a data-tab-ref="languageEmbedded" data-url="/rstaff/report/languagedata.vpage?year=${year!}&term=${term!}" href="javascript:void(0);" id="languageData">语言知识</a></li>
            <#if !currentUser.isResearchStaffForProvince() >
                <li>
                    <a data-tab-ref="weakPointEmbedded" data-url="/rstaff/report/weakknowledge.vpage?year=${year!}&term=${term!}" id="weakPointData" href="javascript:void(0);">
                        <#if currentUser.isResearchStaffForCounty() || currentUser.isResearchStaffForStreet() >各校薄弱<#else>各区薄弱</#if>
                    </a>
                </li>
                <li><a data-tab-ref="weakUnitPointEmbedded" data-url="/rstaff/report/unitknowledgeweak.vpage?year=${year!}&term=${term!}" id="unitknowledgeweak" href="javascript:void(0);">教材薄弱</a></li>
            </#if>
            <li><a id="patternData" data-tab-ref="patternEmbedded" data-url="/rstaff/report/patterndata.vpage?year=${year!}&term=${term!}" href="javascript:void(0);">题型数据</a></li>
        </ul>
    </div>
    <div id="totalEmbedded" class="tabDiv">

    </div>
    <#--语言技能-->
    <div id="skillEmbedded" class="tabDiv"></div>
    <#--语言知识-->
    <div id="languageEmbedded" class="tabDiv"></div>
    <#--各校薄弱-->
    <div id="weakPointEmbedded" class="tabDiv"></div>
    <#--单元薄弱-->
    <div id="weakUnitPointEmbedded" class="tabDiv"></div>
    <#--题型数据-->
    <div id="patternEmbedded" class="tabDiv"></div>

    <#--<div class="mb-info">-->
        <#--<h5>总表详细数据</h5>-->
    <#--</div>-->
</div>

<script type="text/javascript">
    $(function(){
        $("#tabUL").on("click","a",function(){
            var $this = $(this);
            var $li = $(this).closest("li");
            if(!$li.hasClass("active")){
                $li.addClass("active").siblings("li").removeClass("active");
                var $targetDiv = $("#" + $this.attr("data-tab-ref"));
                $targetDiv.addClass("pageLoding").show().siblings("div.tabDiv").hide();
                //表示是否加载过数据
                if($this.isFreezing()){
                    $targetDiv.removeClass("pageLoding").show();
                }else{
                    var url = $this.attr("data-url");
                    if($17.isBlank(url)){return false;}
                    $.get(url,function(data){
                        $this.freezing();
                        $targetDiv.empty().html(data).removeClass("pageLoding").show();
                    });
                }
            }
            return false;
        });

        $("#btn-query").on("click",function(){
            var year = $("#select-year").val();
            var term = $("#select-term").val();
            window.location  = '/rstaff/report/knowledgedata.vpage?year=' + year +'&term=' + term;
        });

        $("#btn-download").on("click", function(){
            <#if year?has_content && term?has_content>
                window.location  = '/rstaff/report/downloadknowledgedata.vpage?year=${year}&term=${term}';
            <#else>
                window.location  = '/rstaff/report/downloadknowledgedata.vpage';
            </#if>
            return false;
        });

        $("#skillChartsData").trigger("click");
    });
</script>
</@com.page>