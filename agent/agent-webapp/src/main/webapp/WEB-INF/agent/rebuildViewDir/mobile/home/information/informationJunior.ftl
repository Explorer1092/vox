<#import "../../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="业绩" pageJs="groupSchoolList">
    <@sugar.capsule css=['home']/>
    <#if (schoolLevel!0) == 1 && (mode!0) == 1>
        <#assign level = 1>
    <#elseif (schoolLevel!0) == 24 && (mode!0) == 1>
        <#assign level = 2>
    <#elseif (schoolLevel!0) == 24 && (mode!0) == 2>
        <#assign level = 3>
    </#if>
<style>
    .srd-module .mHead{margin-left:1rem;padding:.5rem 1rem .5rem 0;font-size:.65rem;color:#9199bb}
    .srd-module .mTable table{width:100%;text-align:center;font-size:.65rem;color:#636880}
    .srd-module .mTable table thead tr{border-bottom:.025rem solid #e6e9f0}
    .srd-module .mTable table tr td{padding:.4rem 0;vertical-align:middle;width:20%;cursor: pointer;}
</style>
<div class="primary-box">
    <a href="javascript:void(0);" class="inner-right arrow_btn js-showHand" style="display:none;"><#if level?? ><#if level == 1>小学<#elseif level == 2>初高中线上<#elseif level ==3>初高中扫描</#if></#if></a>
    <div class="apply_nav" style="display:none;z-index:100;position:fixed;background-color:rgba(125,125,125,.5);width:100%;height:100%">
        <ul class="nav_2 Infiltration" style="top:0">
            <li class="chooseGroupCode active" data-info="1">全部小学</li>
            <li class="chooseGroupCode" data-info="2">低渗校</li>
            <li class="chooseGroupCode" data-info="3">中渗校</li>
            <li class="chooseGroupCode" data-info="4">高渗校</li>
            <li class="chooseGroupCode" data-info="5">超高渗校</li>
        </ul>
    </div>
    <div class="schoolRecord-box show_school  view-box" style="overflow: inherit;background: none">
        <div class="schoolBox" style="background-color: rgb(241, 242, 245)"></div>
    </div>
</div>
<script type="text/html" id="groupList">
    <%if(res){%>
    <#--小学完成-->
    <%var dataMap = res.dataMap%>
    <#if level?? && level == 1>
        <%if(res.groupCode == 1){%>
            <div class="schoolRecord-box view-box">
                <div class="srd-module">
                    <ul class="srd-nav">
                        <%for (var key in dataMap){%>
                        <%var resKey = dataMap[key][0]%>
                            <%if(resKey.viewName == "1套到3套"){%>
                                <li class="tab_li" style="width:16.5%" data-index="<%=key%>">1套到2套</li>
                            <%}else{%>
                                <li class="tab_li" style="width:16.5%" data-index="<%=key%>"><%=resKey.viewName%></li>
                            <%}%>
                        <%}%>
                    </ul>
                    <div class="mTable">
                        <%for (var key in dataMap){%>
                            <%var resKey = dataMap[key]%>
                            <%if (key == 1){%>
                                <table class="table_<%=key%>" cellpadding="0" cellspacing="0" style="display:none;">
                                    <thead>
                                    <tr>
                                        <td class="sortable" style="display: none;"></td>
                                        <td class="sortable">部门/姓名</td>
                                        <td class="sortable">小英月活</td>
                                        <td class="sortable">小英日浮</td>
                                        <td class="sortable">小数月活</td>
                                        <td class="sortable">小数日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%for(var j=0;j< resKey.length;j++){%>
                                        <%var resList = resKey[j]%>
                                        <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                            <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                            <td class="1" style="display: none"><%=resList.groupId%></td>
                                            <td class="2"><%=resList.engMauc%></td>
                                            <td class="3"><%=resList.engMaucDf%></td>
                                            <td class="4"><%=resList.mathMauc%></td>
                                            <td class="5"><%=resList.mathMaucDf%></td>
                                        </tr>
                                    <%}%>
                                    </tbody>
                                </table>
                            <%}else if (key == 2 || key ==3){%>
                                <table class="table_<%=key%>" cellpadding="0" cellspacing="0" style="display:none;">
                                    <thead>
                                    <tr>
                                        <td class="sortable" style="display: none;"></td>
                                        <td class="sortable">部门/姓名</td>
                                        <td class="sortable">目标</td>
                                        <td class="sortable">完成数</td>
                                        <td class="sortable">完成率</td>
                                        <td class="sortable">日浮</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%for(var j=0;j< resKey.length;j++){%>
                                        <%var resList = resKey[j]%>
                                        <%if (key == 2){%>
                                            <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                                <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                                <td class="1" style="display: none"><%=resList.groupId%></td>
                                                <td class="2"><%=resList.engBudget%></td>
                                                <td class="3"><%=resList.engMauc%></td>
                                                <td class="4"><%=Math.round(resList.engCompleteRate *100)%>%</td>
                                                <td class="5"><%=resList.engMaucDf%></td>
                                            </tr>
                                        <%}else if (key == 3){%>
                                            <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                                <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                                <td class="1" style="display: none"><%=resList.groupId%></td>
                                                <td class="2"><%=resList.mathBudget%></td>
                                                <td class="3"><%=resList.mathMauc%></td>
                                                <td class="4"><%=Math.round(resList.mathCompleteRate *100)%>%</td>
                                                <td class="5"><%=resList.mathMaucDf%></td>
                                            </tr>
                                        <%}%>
                                    <%}%>
                                    </tbody>
                                </table>
                            <%}else if (key == 4){%>
                                <table class="table_<%=key%>" cellpadding="0" cellspacing="0" style="display:none;">
                                    <thead>
                                    <tr>
                                        <td class="sortable" style="display: none;"></td>
                                        <td class="sortable">部门/姓名</td>
                                        <td class="sortable">小英次月留存</td>
                                        <td class="sortable">小数次月留存</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%for(var j=0;j< resKey.length;j++){%>
                                        <%var resList = resKey[j]%>
                                        <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                            <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                            <td class="1" style="display: none"><%=resList.groupId%></td>
                                            <td class="2"><%=Math.round(resList.engMrtRate *100)%>%</td>
                                            <td class="3"><%=Math.round(resList.mathMrtRate *100)%>%</td>
                                        </tr>
                                    <%}%>
                                    </tbody>
                                </table>
                            <%}else if(key == 5){%>
                                <table class="table_<%=key%>" cellpadding="0" cellspacing="0" style="display:none;">
                                    <thead>
                                    <tr>
                                        <td class="sortable" style="display: none;"></td>
                                        <td class="sortable">部门/姓名</td>
                                        <td class="sortable">小英1套</td>
                                        <td class="sortable">小英2套</td>
                                        <td class="sortable">小数1套</td>
                                        <td class="sortable">小数2套</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%for(var j=0;j< resKey.length;j++){%>
                                    <%var resList = resKey[j]%>
                                    <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                    <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                    <td class="1" style="display: none"><%=resList.groupId%></td>
                                    <td class="2"><%=resList.finEngHwEq1StuCount%></td>
                                    <td class="3"><%=resList.finEngHwEq2StuCount%></td>
                                    <td class="4"><%=resList.finMathHwEq1StuCount%></td>
                                    <td class="5"><%=resList.finMathHwEq2StuCount%></td>
                                    </tr>
                                    <%}%>
                                    </tbody>
                                </table>
                            <%}else if(key == 6){%>
                                <table class="table_<%=key%>" cellpadding="0" cellspacing="0" style="display:none;">
                                    <thead>
                                    <tr>
                                        <td class="sortable" style="display: none;"></td>
                                        <td class="sortable">部门/姓名</td>
                                        <td class="sortable">本月注册</td>
                                        <td class="sortable">昨日注册</td>
                                        <td class="sortable">本月认证</td>
                                        <td class="sortable">昨日认证</td>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <%for(var j=0;j< resKey.length;j++){%>
                                    <%var resList = resKey[j]%>
                                    <tr class="js-item" data-id="<%=resList.id%>" data-type="<%=resList.idType%>" data-level="<%=resList.schoolLevel%>">
                                        <td><%=resList.name%><%if(resList.idType == "USER"){%><p style="color: #999;font-size: .5rem;"><%=resList.groupName%></p><%}%></td>
                                        <td class="1" style="display: none"><%=resList.groupId%></td>
                                        <td class="2"><%=resList.tmRegStuCount%></td>
                                        <td class="3"><%=resList.regStuCountDf%></td>
                                        <td class="4"><%=resList.tmAuStuCount%></td>
                                        <td class="5"><%=resList.auStuCountDf%></td>
                                    </tr>
                                    <%}%>
                                    </tbody>
                                </table>
                            <%}%>
                        <%}%>
                    </div>
                </div>
            </div>
        <%}%>
    </#if>
    <%}%>
</script>
<script src="/public/rebuildRes/js/mobile/home/sortTable.js"></script>

<script>
    var groupLevel = "小学";
    $(document).on("click",".js-moreBtn",function(e){
        e.stopPropagation();
        $('.showMore').toggle();
    });
    $(document).on("click",".closeBtn",function(){
        $('.showMore').hide();
    });
    var roleType = "${groupRole!""}";
    var idType = "${idType!""}" ;
    var id = "${id!""}" ;
    var schoolLevel = "${schoolLevel!0}" ;
    var mode = "${mode!0}" ;
    var userType = "" ;
    if(roleType == "Country"){
        userType = "全国";
    }else if(roleType == "Region"){
        userType = "大区";
    }else if(roleType == "City"){
        userType = "分区";
    }else{
        userType = "专员"
    }
    $(document).on("click",".js-item",function(){
        if($(this).data("type") == "GROUP"){
            openSecond("/mobile/performance/performance_list_page.vpage?schoolLevel="+$(this).data("level")+"&id="+$(this).data("id")+"&idType="+$(this).data("type")+"&mode=" + mode)
        }else{
            openSecond("/mobile/performance/school_performance.vpage?schoolLevel="+$(this).data("level")+"&id="+$(this).data("id")+"&idType="+$(this).data("type")+"&mode=" + mode);
        }
    });
</script>
</@layout.page>
