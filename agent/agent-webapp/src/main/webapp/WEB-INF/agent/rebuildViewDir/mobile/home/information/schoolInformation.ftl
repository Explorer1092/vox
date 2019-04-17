<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="information">
    <@sugar.capsule css=['home']/>
    <#if (schoolLevel!0) == 1 && (mode!0) == 1>
        <#assign level = 1>
    <#elseif (schoolLevel!0) == 24 && (mode!0) == 1>
        <#assign level = 2>
    <#elseif (schoolLevel!0) == 24 && (mode!0) == 2>
        <#assign level = 3>
    </#if>
<style type="text/css">
.fliterSchool ul li{float: none;}
</style>
<div class="primary-box" style="background-color: rgb(241, 242, 245)">
    <div class="feedbackList-pop show_now" style="display:none;z-index:100;position:fixed;top:0;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul style="background:#fff;margin-top:-0.1rem">
            <li style="padding:.2rem 0"><span class="school_row active" style="display:inline-block;text-align:center;height:2.5rem;line-height:2.5rem;width:100%;font-size: .75rem;border-bottom: .05rem solid #cdd3d3">小学</span></li>
            <li style="padding:.2rem 0"><span class="school_row" style="height:2.5rem;line-height:2.5rem;border-bottom: .05rem solid #cdd3d3;text-align:center;display:inline-block;width:100%;font-size: .75rem;" data-index="2" data-views="8" data-idtype="${idType!""}" data-id="${id!0}">初高中线上</span></li>
        </ul>
    </div>
    <div>
        <div class="schoolRecord-box show_school  view-box" style="overflow: inherit;">
            <div class="apply_nav fliterSchool">
                <ul class="nav_1">
                    <li class="tab_index" data-index="reorder">昨日英活由高到低</li>
                    <li class="tab_index" data-index="Infiltration"><#if level == 1>全部小学<#else>全部中学</#if></li>
                </ul>
                <div>
                    <ul class="nav_2 reorder" hidden>
                        <li class="active" data-index="reorder" data-tabindex="finEngHwGte3AuStuCountDf">昨日英活由高到低</li>
                        <#if level == 1>
                        <li data-index="reorder" data-tabindex="finMathHwGte3AuStuCountDf">昨日数活由高到低</li>
                        </#if>
                        <li data-index="reorder" data-tabIndex="regStuCountDf">昨日注册由高到低</li>
                    </ul>
                    <ul class="nav_2 Infiltration" style="display: none">
                        <li class="active" data-index="Infiltration" data-permeability=""><#if level == 1>全部小学<#else>全部中学</#if></li>
                        <li data-index="Infiltration" data-permeability="LOW">低渗校</li>
                        <li data-index="Infiltration" data-permeability="MIDDLE">中渗校</li>
                        <li data-index="Infiltration" data-permeability="HIGH">高渗校</li>
                        <li data-index="Infiltration" data-permeability="SUPER_HIGH">超高渗校</li>
                    </ul>
                </div>
            </div>
            <#--<input type="button" class="change" data-index="a">-->
            <#--<input type="button" class="change" data-index="b">-->
            <div class="schoolBox" style="background-color: rgb(241, 242, 245)"></div>
        </div>
    </div>
</div>
<script>
    var id = ${id!0};
    var idType = "${idType!""}";
    var schoolLevel = ${schoolLevel!1};
</script>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>
<script id="#schoolContant" type="text/html">

<%for(var i =0; i< data.length;i++){%>
<div class="school_card yearCard-box" data-sid="<%= data[i].schoolId%>" style="margin:.5rem 0;">
    <div class="year-column">
        <div class="left">
            <p class="name" id="detail<%= data[i].teacherId%>"><%=data[i].schoolName%>
                <span>
                    <#--<%if(data[i].schoolPopularity != null){%><%=data[i].schoolPopularity%><i class="icon-<%=data[i].schoolPopularity%>"></i><%}%>-->
                    <%if(data[i].permeability != null){%><i class="icon-<%=data[i].permeability%>"></i><%}%>
                    <%if(data[i].authState != null && data[i].authState == "SUCCESS"){%><%}else{%><i class="icon-unjian"></i><%}%>
                </span>

            </p>
        </div>
    </div>

    <div class="year-content">
    <#--中学数学-->
        <ul>
            <li style="width:33%">
                <div class="font"><span><%if(data[i].regStuCountDf && data[i].regStuCountDf != ""){%><%=data[i].regStuCountDf%><%}else{%>0<%}%></span></div>
                <div>昨日注册</div>
            </li>
            <li style="width:33%">
                <div class="font"><%if(data[i].finEngHwGte3AuStuCountDf && data[i].finEngHwGte3AuStuCountDf != ""){%><%=data[i].finEngHwGte3AuStuCountDf%><%}else{%>0<%}%></div>
                <div>昨日英活</div>
            </li>
            <#if level == 1>
                <li style="width:33%">
                    <div class="font"><%if(data[i].finMathHwGte3AuStuCountDf && data[i].finMathHwGte3AuStuCountDf != ""){%><%=data[i].finMathHwGte3AuStuCountDf%><%}else{%>0<%}%></div>
                    <div>昨日数活</div>
                </li>
            </#if>
        </ul>
    </div>

    <div class="year-side">
        <span>规模<%if(data[i].stuScale && data[i].stuScale != ""){%><%=data[i].stuScale%><%}else{%>0<%}%></span>
        <span>注册<%if(data[i].regStuCount && data[i].regStuCount != ""){%><%=data[i].regStuCount%><%}else{%>0<%}%></span>
        <span>认证<%if(data[i].auStuCount && data[i].auStuCount != ""){%><%=data[i].auStuCount%><%}else{%>0<%}%></span>
        <span>英活<%if(data[i].finEngHwGte3AuStuCount && data[i].finEngHwGte3AuStuCount != ""){%><%=data[i].finEngHwGte3AuStuCount%><%}else{%>0<%}%></span>
        <#if level == 1>
            <span>数活<%if(data[i].finMathHwGte3AuStuCount && data[i].finMathHwGte3AuStuCount != ""){%><%=data[i].finMathHwGte3AuStuCount%><%}else{%>0<%}%></span>
        </#if>
    </div>
</div>
<%}%>
</script>
<script>
    <#if level == 1>
    var rightText = "小学";
    <#else>
    var rightText = "初高中online";
    </#if>
    var roleType = "${idType!""}";
    var renderIndex = "maucDf";
    var schoolLevel = ${schoolLevel!0};
    var mode = ${mode!0};
</script>
</@layout.page>
