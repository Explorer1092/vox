<#import "../../layout_new.ftl" as layout>
<#assign groupName ="业绩">
<#if viewType=="addTeacher"><#assign groupName ="搜索"></#if>
<@layout.page group=groupName title="新增学生">

<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">

            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">
                <#if viewType=="addTeacher">
                    <span>新增老师</span>
                <#else>
                    <span>新增学生</span>
                </#if>
            </div>
        </div>
    </div>
</div>

<div class="mobileCRM-V2-date">
    <div class="date" id="selectDate"><span><em></em>${startDate!''} ~  ${endDate!''}</span></div>
</div>

<ul class="mobileCRM-V2-list">
    <li>
        <div class="box link-ico">
            <div class="side-fl side-time">区域</div>
            <div class="side-fr side-time side-width">认证</div>
            <div class="side-fr side-time side-width">注册</div>
        </div>
    </li>
    <#if performanceData??>
        <#list performanceData?values as value>
            <#if viewType=="addTeacher">
                <#assign authCount = value['teacherAuth']>
                <#assign regCount = value['teacherRegister']>
            <#else>
                <#assign authCount = value['studentAuth']>
                <#assign regCount = value['studentRegister']>
            </#if>
            <#if authCount gt 0 || regCount gt 0>
            <li>
                    <#if value['type']?? && value['type']=="region">
                          <#if viewType=="addTeacher">
                             <a href="term_performance.vpage?viewType=addTeacher&startDate=${startDate!''}&endDate=${endDate!''}&region=${value['regionCode']!''}" class="link link-ico">
                          <#else>
                             <a href="term_performance.vpage?viewType=addStudent&startDate=${startDate!''}&endDate=${endDate!''}&region=${value['regionCode']!''}" class="link link-ico">
                          </#if>
                    <#elseif value['type']?? && value['type']=="school">
                        <#if viewType=="addTeacher">
                            <a href="reg_auth_teachers.vpage?startDate=${startDate!''}&endDate=${endDate!''}&schoolId=${value['regionCode']!''}" class="link link-ico">
                        <#else>
                            <a href="javascript:void(0)" class="link">
                        </#if>

                    <#elseif value['type']?? && value['type']=="summary">
                            <div class="link">
                    </#if>
                            <div class="side-fl" style="width:48%;">${value['regionName']!'未知'}</div>
                            <div class="side-fr <#if value_has_next>side-orange</#if> side-width">${authCount!'0'}</div>
                            <div class="side-fr <#if value_has_next>side-orange</#if> side-width">${regCount!'0'}</div>
                    </a>
            </li>
            </#if>
        </#list>
    </#if>
</ul>

<div class="mobileCRM-V2-layer" style="display:none">
    <div class="dateBox">
        <div class="boxInner">
            <ul class="mobileCRM-V2-list">
                <li>
                    <div class="box">
                        <div class="side-fl">起始日期</div>
                        <input type="date" placeholder="${startDate!''}" class="textDate" onfocus="(this.type='date')" id="start" value="${startDate!''}">
                    </div>
                </li>
                <li>
                    <div class="box">
                        <div class="side-fl">结束日期</div>
                        <input type="date" placeholder="${endDate!''}" class="textDate" onfocus="(this.type='date')" id="end" value="${endDate!''}">
                    </div>
                </li>
            </ul>
            <div class="boxFoot">
                <div class="side-fl" id="req_cancel">取消</div>
                <div class="side-fr" id="req_ok">确定</div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $("#selectType").change(function(){
        var url = $("#selectType option:selected").val();
        window.location.href = url;
    });
    $("#selectDate").click(function(){
              $(".mobileCRM-V2-layer").show();
    });
    $("#req_cancel").click(function(){
        $(".mobileCRM-V2-layer").hide();
    });
    $("#req_ok").click(function(){

              var startDate = $("#start").val();
              var endDate= $("#end").val();

              if(!startDate||startDate ===""){
                  alert("请输入起始日期");
                  return;
              }
              if(!endDate || endDate==="") {
                  alert("请输入结束日期");
                  return;
              }
              if(new Date(startDate) <= new Date(endDate)){
                  var url = "/mobile/myperformance/term_performance.vpage?viewType=${viewType!''}&startDate="+startDate+"&endDate="+endDate+"&region=${curRegion!''}";
                  window.location.href = url;
              }
              else {
                  alert("起始日期大于结束日期，请重新输入");
                  return false;
              }
    })

</script>
</@layout.page>