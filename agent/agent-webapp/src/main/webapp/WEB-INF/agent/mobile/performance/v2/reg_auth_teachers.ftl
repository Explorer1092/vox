<#import "../../layout_new.ftl" as layout>
<@layout.page group="业绩" title="">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <#if schoolName?? && schoolName?length gt 15>
                <div class="headerText">${schoolName[0..10]!''}...</div>
            <#else>
                <div class="headerText">${schoolName!''}</div>
            </#if>

        </div>
    </div>
</div>
<div class="mobileCRM-V2-rankInfo">
    <div class="infoBox infoTab" >
        <div class="active" method="reg" name="action">
            <div class="boxNum"><#if (registerTeachers?size gt 0)!false>${registerTeachers?size}<#else>0</#if></div>
            <div class="boxFoot">新增注册老师</div>
        </div>
        <div method="auth" name="action">
            <div class="boxNum"><#if (authTeachers?size gt 0 )!false>${authTeachers?size}<#else>0</#if></div>
            <div class="boxFoot">新增认证老师</div>
        </div>
    </div>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info" id="reg">
    <ul class="mobileCRM-V2-list">
        <#if (registerTeachers?size gt 0)!false>
            <ul class="mobileCRM-V2-list">
                <#list registerTeachers as regteacher>
                    <li>
                        <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=addteareg_${startDate!''}_${endDate!''}_${schoolId!''}&teacherId=${regteacher['teacherId']!''}" class="link link-ico">
                            <div class="side-fl side-mode">${regteacher['teacherName']!''}</div>
                            <div class="side-fr side-gray"><#if (regteacher['subject'])?has_content>${regteacher['subject'].value!''}</#if></div>
                        </a>
                    </li>
                </#list>
            </ul>
        </#if>
    </ul>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info" style="display:none" id="auth">
    <#if (authTeachers?size gt 0)!false>
        <ul class="mobileCRM-V2-list">
            <#list authTeachers as authteacher>
                <li>
                    <a href="/mobile/teacher/v2/teacher_info.vpage?pageSource=addteaauth_${startDate!''}_${endDate!''}_${schoolId!''}&teacherId=${authteacher['teacherId']!''}" class="link link-ico">
                        <div class="side-fl side-mode">${authteacher['teacherName']!''}</div>
                        <div class="side-fr side-gray">${authteacher['subject'].value!''}</div>
                    </a>
                </li>
            </#list>
        </ul>
    </#if>
</div>
<script type="text/javascript">
      $("div[name='action']").click(function(){
          $("div[name='action']").removeClass("active");
          $(this).addClass("active");
          var method = $(this).attr("method");
          $(".mobileCRM-V2-box.mobileCRM-V2-info").hide();
          $("#"+method).show();
      });
</script>
</@layout.page>