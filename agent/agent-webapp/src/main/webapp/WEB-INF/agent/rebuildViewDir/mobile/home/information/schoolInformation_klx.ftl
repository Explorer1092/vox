<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="information">
    <@sugar.capsule css=['home']/>
<style type="text/css">
    .schoolRecord-box ul li{float: none;}
</style>
<div class="primary-box">
    <div class="schoolRecord-box show_school  view-box" style="overflow: inherit;">
        <div class="schoolBox" style="background-color: rgb(241, 242, 245)"></div>
    </div>
</div>
<script>
    var id = ${id!0};
    var idType = "${idType!""}";
    var schoolLevel = ${schoolLevel!1};
    var mode = ${mode!1};
</script>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>
<script id="#schoolContant" type="text/html">
    <%for(var i =0; i< data.length;i++){%>
    <div class="school_card yearCard-box" data-sid="<%= data[i].schoolId%>" data-type="offline" style="margin:.5rem 0;padding:.5rem">
        <div class="year-column">
            <div class="left">
                <p class="name" id="detail<%= data[i].teacherId%>"><%=data[i].schoolName%>
                    <span>
                        <%if(data[i].schoolPopularity != null){%><i class="icon-<%=data[i].schoolPopularity%>"></i><%}%>
                        <%if(data[i].authState && data[i].authState == "SUCCESS"){%><%}else{%><i class="icon-unjian"></i><%}%>
                    </span>
                </p>
            </div>
        </div>
        <div class="year-content">
            <ul>
                <li>
                    <div class="font"><span><%if(data[i].pdFinTpGte1StuCount && data[i].pdFinTpGte1StuCount != ""){%><%=data[i].pdFinTpGte1StuCount%><%}else{%>0<%}%></span></div>
                    <div>昨日普扫≥1次</div>
                </li>
                <li>
                    <div class="font"><span><%if(data[i].pdFinTpGte3StuCount && data[i].pdFinTpGte3StuCount != ""){%><%=data[i].pdFinTpGte3StuCount%><%}else{%>0<%}%></span></div>
                    <div>昨日普扫≥3次</div>
                </li>
            </ul>
        </div>

        <div class="year-side">
            <span>规模<%if(data[i].stuScale && data[i].stuScale != ""){%><%=data[i].stuScale%><%}else{%>0<%}%></span>
            <span>考号<%if(data[i].klxTnCount && data[i].klxTnCount != ""){%><%=data[i].klxTnCount%><%}else{%>0<%}%></span>
            <span>普扫1次<%if(data[i].tmFinTpGte1StuCount && data[i].tmFinTpGte1StuCount != ""){%><%=data[i].tmFinTpGte1StuCount%><%}else{%>0<%}%></span>
            <span>普扫3次<%if(data[i].tmFinTpGte3StuCount && data[i].tmFinTpGte3StuCount != ""){%><%=data[i].tmFinTpGte3StuCount%><%}else{%>0<%}%></span>
        </div>
    </div>
    <%}%>
</script>
<script>
    var rightText = "初高中offline";
    var roleType = "${idType!""}";
    var renderIndex = "anshGte2StuCountDf";
</script>
</@layout.page>