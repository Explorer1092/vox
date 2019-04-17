<#-- @ftlvariable name="conditionMap" type="java.util.LinkedHashMap" -->
<#macro queryConditons>
<div>
    <form method="post" action="teacherlist.vpage" class="form-horizontal">
        <fieldset>
            <legend>老师查询</legend>
            <ul class="inline">
                <li>
                    <label for="teacherId">
                        老师学号
                        <input name="teacherId" id="teacherId" type="text"/>
                    </label>
                </li>
                <li>
                    <label for="teacherMobile">
                        老师手机
                        <input name="teacherMobile" id="teacherMobile" type="text">
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="klxLoginName">
                        快乐学老师登录名
                        <input name="klxLoginName" id="klxLoginName" type="text">
                    </label>
                </li>
                <li>
                    <label for="klxUserName">
                        快乐学老师用户名
                        <input name="klxUserName" id="klxUserName" type="text">
                    </label>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <button id='submit' type="submit" class="btn btn-primary">查 询</button>
                </li>
            </ul>
            <br/>
        </fieldset>
    </form>
</div>
<script>
    $(function(){
        <#if teacherList?has_content && teacherList?size == 1>
            window.open("teacherhomepage.vpage?teacherId=${teacherList[0].teacherId!''}", "_blank");
        </#if>
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#submit').click();
            }
        });
        <#if conditionMap?has_content>
            $('#teacherId').val('${(conditionMap.teacherId?html)!''}');
            $('#teacherName').val('${(conditionMap.teacherName?html)!''}');
            $('#schoolName').val('${(conditionMap.schoolName?html)!''}');
            $('#teacherMobile').val('${(conditionMap.teacherMobile?html)!''}');
            $('#klxLoginName').val('${(conditionMap.klxLoginName?html)!''}');
            $('#klxUserName').val('${(conditionMap.klxUserName?html)!''}');
        <#--$('#teacherEmail').val('${(conditionMap.teacherEmail?html)!''}');-->
        </#if>
    });
</script>
</#macro>