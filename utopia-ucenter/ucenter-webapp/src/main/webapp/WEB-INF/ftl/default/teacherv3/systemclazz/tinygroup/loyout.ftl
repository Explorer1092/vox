<#--是否多学科-->
<#assign multiSubject = ((currentTeacherDetail.subjects?size gt 1)!false)/>
<#assign curSubject = curSubject!((currentTeacherDetail.subject)!'')/>
<#assign curSubjectText = curSubjectText!((currentTeacherDetail.subject.value)!'')/>
<#macro tinyGroup title="小组奖励">
    <script type="text/javascript">
        if(location.hash == ""){
            location.href = "/teacher/systemclazz/clazzindex.vpage#" + location.href;
        }
    </script>
    <div class="w-base">
        <div class="w-base-title">
            <h3>${clazzName!} ${title}</h3>
            <#if title != "任命小组长">
                <div class="w-base-right w-base-switch" style="float: left;">
                    <ul>
                        <li class="special" style="float: left;">
                            <a href="http://help.17zuoye.com/?page_id=1081" target="_blank">
                                <span class="ico"></span>
                                使用说明
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="w-base-right w-base-switch">
                    <ul>
                        <li class="${(title == "小组奖励")?string("active", "")}">
                            <a class="v-cm-main" href="#/teacher/clazz/tinygroup/index.vpage?clazzId=${clazzId!0}&subject=${curSubject!}">
                                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                小组奖励
                            </a>
                        </li>
                        <li class="${(title == "调整小组")?string("active", "")}">
                            <a class="v-cm-main" href="#/teacher/clazz/tinygroup/editcrew.vpage?clazzId=${clazzId!0}&subject=${curSubject!}">
                                <span class="h-arrow"><i class="w-icon-arrow w-icon-arrow-blue"></i></span>
                                调整小组
                            </a>
                        </li>
                    </ul>
                </div>
            <#else>
                <div class="w-base-ext">小组长能帮您督促学生完成作业</div>
            </#if>
        </div>
        <div class="w-base-container">
        <#--//start-->
            <#nested>
        <#--end//-->
        </div>
    </div>
</#macro>

