<#assign titleTip="呼入">
<#assign firstCategoryText="一级分类">
<#assign secondCategoryText="二级分类">
<#assign thirdCategoryText="三级分类">
<#if isPhoneOut == true>
    <#assign titleTip="呼出">
    <#assign firstCategoryText="是否接通">
    <#assign secondCategoryText="身份判断">
    <#assign thirdCategoryText="跟进阶段">
</#if>
<div id="record-new" title="新建工作记录（${titleTip}）" style="font-size: small; display: none" task-id="" user-id="" task-type="">
    <table width="100%">
    <#if isPhoneOut == true>
        <tr style="display: none">
            <td colspan="2"><input id="record-new-contact" type="hidden" value="电话呼出"/></td>
        </tr>
    <#else>
        <tr>
            <td style="text-align: left">沟通渠道：</td>
            <td style="text-align: left">
                <select id="record-new-contact" style="width:180px">
                    <#if contactTypes?has_content>
                        <#list contactTypes as contactType>
                            <option value="${contactType.name()!''}">${contactType.name()!}</option>
                        </#list>
                    </#if>
                </select>
            </td>
        </tr>
    </#if>
        <tr>
            <td style="text-align: left">${firstCategoryText}：</td>
            <td style="text-align: left">
                <select id="record-new-firstCategory" onchange="secondCategories(false)" style="width:180px" class="firstCategory"></select>
            </td>
        </tr>
        <tr>
            <td style="text-align: left">${secondCategoryText!}：</td>
            <td style="text-align: left">
                <select id="record-new-secondCategory" onchange="thirdCategories(false)" first="" style="width:180px" class="secondCategory"></select>
            </td>
        </tr>
        <tr>
            <td style="text-align: left">${thirdCategoryText!}：</td>
            <td style="text-align: left">
                <select id="record-new-thirdCategory" style="width:180px" class="thirdCategory"></select>
            </td>
        </tr>
        <tr>
            <td style="text-align: left">记录内容：</td>
            <td style="text-align: left"><textarea id="record-new-content" style="height: 120px; width: 300px" placeholder="解决方式记录（不超过150字）"></textarea></td>
        </tr>
        <tr>
            <td style="text-align: left">创建Redmine给：</td>
            <td style="text-align: left">
                <select id="record-new-redmineAssigned" style="width:120px">
                    <option value=""></option>
                    <option value="110">产品</option>
                    <option value="5">技术</option>
                </select>
            </td>
        </tr>
        <tr>
            <td colspan="2" style="text-align: right">
                <input type="button" value="任务修改" onclick="record.updateTask()" class="record-new-task"/>
                <input type="button" value="任务已完成" onclick="record.finishTask()" class="record-new-task"/>
                <input type="button" value="任务待跟进" onclick="record.followTask()" class="record-new-task"/>
                <input type="button" value="新建任务" onclick="record.newTask()" class="record-new-record"/>
                <input type="button" value="提交" onclick="record.save()" class="record-new-record"/>
                <input type="button" value="取消" onclick="closeDialog('record-new')"/>
            </td>
        </tr>
    </table>
</div>

<script type="text/javascript">
    var CATEGORIES = ${recordCategoryJson!''};

    $(function () {
        record.renderCategories(false);
    });

    function initRecordNewData(){
        $("#record-new-firstCategory").prop("selectedIndex", 0);
        secondCategories(false);
        $("#record-new-content").val("");
    }
</script>
