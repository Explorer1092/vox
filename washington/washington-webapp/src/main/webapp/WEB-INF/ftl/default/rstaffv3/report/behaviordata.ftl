<#import "../researchstaffv3.ftl" as com>
<@com.page menuIndex=11>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">大数据报告</a> <span class="divider">/</span></li>
    <li class="active">行为数据</li>
</ul>
<div class="r-titleResearch-box">
    <p>
    ${termText}年度${currentUser.formatManagedRegionStr()}小学${(currentUser.subject.value)!}作业行为统计报告
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
</div>
<div id="tb-behaivordata" class="r-mapResearch-box" >

</div>
<script type="text/javascript">
    $(function(){
        var AreaOperate = {
            init : function(){
                var $tableDiv = $("#tb-behaivordata");
                $tableDiv.addClass("pageLoding")
                var url = "/rstaff/report/behaviordatachip.vpage?year=${year!}&term=${term!}"
                $.get(url,function(data){
                    $tableDiv.empty().html(data).removeClass("pageLoding").show();
                });
            }
        };

        $("#btn-query").on("click",function(){
            var year = $("#select-year").val();
            var term = $("#select-term").val();
            window.location  = '/rstaff/report/behaviordata.vpage?year=' + year +'&term=' + term;
        });

        $("#btn-download").on("click", function(){
            var year = $("#select-year").val();
            var term = $("#select-term").val();
            window.location  = '/rstaff/report/downloadbehaviordata.vpage?year=' + year +'&term=' + term;
            return false;
        });

        AreaOperate.init();
    });
</script>
</@com.page>