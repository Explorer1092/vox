<#if (currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS">
    <#--班级列表-下载名单或导入学生姓名-->
    <#if (downloadClazzs)?? && (downloadClazzs)?size gt 0>
    <div id="newClazzList" style="margin-bottom: 15px;">
        <#list downloadClazzs as dc>
            <div class="t-school-notice-box t-school-notice-box-orange <#if dc_index == 0 && (downloadClazzs)?size gt 3>v-switch-newClazzList</#if>" style="margin: 0 0 1px; <#if dc_index gt 0 && (downloadClazzs)?size gt 3> display: none;</#if>">
                <strong class="w-fl-left"><a href="/teacher/clazz/clazzsdetail.vpage?clazzId=${dc.clazzId}" >${dc.clazzName}</a></strong>
                <p class="w-fl-left sn-b">下载班级名单（含学生账号、密码），指导学生登录</p>
                <div class="w-fl-right sn-c">
                    <a class="w-btn w-btn-green w-btn-mini data-ImportStudentName" href="javascript:void(0);" data-clazzid="${dc.clazzId}" data-clazzname="${dc.clazzName}">导入班级名单</a>
                        <span class="step-newClazzList-55" style="display: inline-block;">
                            <a class="w-btn w-btn-mini" href="/clazz/batchdownload.vpage?clazzIds=${dc.clazzId}" target="_blank"><i class="t-school-icon t-school-icon-add t-school-icon-down"></i>下载名单&nbsp;&nbsp;</a>
                        </span>
                    <#if dc_index == 0 && (downloadClazzs)?size gt 3><span class="t-school-icon t-school-icon-arrow"></span></#if>
                </div>
            </div>
        </#list>
    </div>
    </#if>
</#if>
<#--<#include "batchAddStudentName.ftl"/>-->
<script type="text/javascript">
    $(function(){
        //展开收起班级
        var newClazzList = $("#newClazzList");
        newClazzList.find(".v-switch-newClazzList").on("click", function(){
            var $this = $(this);
            $this.toggleClass("t-school-icon-arrow-up");
            $this.closest(".t-school-notice-box").siblings("div").toggle()
        });
    });
</script>
<script type="text/html" id="T:导入班级名单下载学生账号">
    <div style="margin: 0 auto; width:495px;">
        <h3 class="w-ft-big w-ag-center" style="line-height: 150%;">请老师发放学生账号！</h3>
        <div>
            <div class="t-download-step"></div>
            <%if(clazzIds.length > 0){%>
            <a href="/teacher/clazz/batchdownload.vpage?clazzIds=<%=clazzIds%>" target="_blank"><span class="w-icon w-icon-33"></span><span class="w-df-share w-blue">点击下载</span></a>
            <%}%>
        </div>
        <div class="w-ag-center" style="padding: 50px 0 20px;">
            老师也可以 <a href="javascript:void(0);" class="w-btn w-btn-small data-ImportStudentName" data-clazzid="<%=clazzIds%>">导入班级名单</a> 再下载
        </div>
    </div>
</script>