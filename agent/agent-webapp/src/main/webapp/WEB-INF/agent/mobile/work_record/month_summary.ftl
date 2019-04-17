<#import "../layout_new.ftl" as layout>
<@layout.page group="work_record">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <div class="headerBack">&lt;&nbsp;返回</div>
            <div class="headerText">${region!}</div>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-page">
    <div class="prev">&lt;&nbsp;上一月</div>
    <div class="next">下一月&nbsp;&gt;</div>
    <#if month??>
        <div id="dateStr" value = ${month?string("yyyy-MM-dd")}>${month?string("yyyy-MM")}</div>
    </#if>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info">
    <div class="side">合计</div>
    <#if summary??>
        <#if summary.summary?has_content>
            <div class="list">
                <div>
                    <div class="hd red">${summary.summary["SCHOOL"]}</div>
                    <div class="ft">进校</div>
                </div>
                <div>
                    <div class="hd red">${summary.summary["MEETING"]}</div>
                    <div class="ft">组会</div>
                </div>
                <div>
                    <div class="hd red">${summary.summary["VISIT"]}</div>
                    <div class="ft">陪访</div>
                </div>
            </div>
        </#if>
    </#if>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <#if summary??>
             <#if summary.detail?exists>
                 <#list summary.detail?keys as key>
                     <li>
                         <div class="box">
                             <div class="side-fl side-num">${key}</div>
                             <div class="side-fl side-table side-td">${summary.detail[key]['SCHOOL']}</div>
                             <div class="side-fl side-table side-td">${summary.detail[key]['MEETING']}</div>
                             <div class="side-fl side-table side-td">${summary.detail[key]['VISIT']}</div>
                         </div>
                     </li>
                  </#list>
             </#if>
        </#if>
    </ul>
</div>
<script type="text/javascript">

     var dateStr = $("#dateStr").attr("value");
     var curUrl = window.location.search.substring(1).toLowerCase();
     if(!dateStr){
         var curDate = new Date();
         var month = curDate.getMonth()<9?"0"+(curDate.getMonth()+1):curDate.getMonth()+1;
         dateStr =curDate.getFullYear()+"-"+month+"-"+curDate.getDate();
     }
     var pageType = "${pageType}";
     var regionId = "${reginId}";
     var dateStrArr = dateStr.split("-");
     var dateCur = new Date(parseInt(dateStrArr[0]),parseInt(dateStrArr[1])-1,parseInt(dateStrArr[2]));

     var urlPattern = "{#pageType}_month_summary.vpage?{#pageType}={#typeValue}&month={#month}";
     $("div.prev").click(function(){
              var preDate = new Date(new Date(parseInt(dateStrArr[0]),parseInt(dateStrArr[1])-2,1));
              var month  = preDate.getMonth()<9?"0"+(preDate.getMonth()+1):preDate.getMonth()+1;
              var resultUrl = urlPattern.replace("{#pageType}",pageType).replace("{#pageType}",pageType).replace("{#typeValue}",regionId).replace("{#month}",preDate.getFullYear()+"-"+month+"-01");
              window.location.href = resultUrl;
     });
    $("div.next").click(function(){
        var nextDate = new Date(new Date(parseInt(dateStrArr[0]),parseInt(dateStrArr[1]),1));
        var month  = nextDate.getMonth()<9?"0"+(nextDate.getMonth()+1):nextDate.getMonth()+1;
        var resultUrl = urlPattern.replace("{#pageType}",pageType).replace("{#pageType}",pageType).replace("{#typeValue}",regionId).replace("{#month}",nextDate.getFullYear()+"-"+month+"-01");
        window.location.href = resultUrl;
    });
</script>
</@layout.page>
