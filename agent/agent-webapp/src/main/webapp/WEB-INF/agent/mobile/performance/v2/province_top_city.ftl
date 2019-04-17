<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="省内排行榜">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
           <div class="headerText">省内排行榜</div>
        </div>
    </div>
</div>
<ul class="mobileCRM-V2-list">
    <#if provinceTopCity?? && provinceTopCity?has_content>
        <#assign dataRank=0 />
        <#list provinceTopCity as city>
            <#assign dataRank = dataRank + 1 />
            <li>
                    <div class="box">
                        <div class="side-fl" style="width:10%;">${dataRank}</div>
                        <div class="side-fl" style="width:22%;">${city["regionName"]!''}</div>
                        <div class="side-fr side-orange">${city["studentAuth"]!}</div>
                     </div>
            </li>
        </#list>
    <#else >
        &nbsp;&nbsp;&nbsp;&nbsp;暂无数据
    </#if>
</ul>
<script type="text/javascript">

    //url中的参数获取
        function getRequestMap(){
            var paraStr = window.location.search.substring(1);
            var result =null;
            var tempArr = paraStr.split("&");
            if(tempArr&&tempArr.length>0){
                result ={};
                var keyValue ;
                for(var i=0;i<tempArr.length;i++){
                    keyValue = tempArr[i].split("=");
                    if(keyValue[0]&&keyValue[1]){
                        result[keyValue[0]] = keyValue[1];
                    }
                }
            }
            return result;
        }
//
//     $(".headerBack").click(function(){
//          var reqParams = getRequestMap();
//          if(reqParams['back']==="list"){
//              window.location.href="province_top_city.vpage";
//          }
//          else{
//              window.location.href="index.vpage";
//          }
//
//     });
</script>
</@layout.page>