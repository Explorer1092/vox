<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="奖励通知" page_num=24>
<style xmlns="http://www.w3.org/1999/html">
    .panel-info {
        border-color: #bce8f1;
    }

    .panel {
        margin-bottom: 10px;
        background-color: #fff;
        border: 1px solid transparent;
        border-radius: 4px;
        -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
        box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
    }

    .panel-info > .panel-heading {
        color: #31708f;
        background-color: #d9edf7;
        border-color: #bce8f1;
    }

    .panel-heading {
        padding: 10px 15px;
        border-bottom: 1px solid transparent;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
    }

    .panel-title {
        margin-top: 0;
        margin-bottom: 0;
        font-size: 16px;
        color: inherit;
    }

    .panel-body {
        padding-top: 10px;
        padding-bottom: 10px;
        padding-left: 15px;
        padding-right: 15px;
    }

    .btn {
        height: 25px;
        font-size: 6px;
    }

    .table {
        width: 100%;
        margin-bottom: 0px;
    }

    hr {
        margin-top: 15px;
        margin-bottom: 10px;
    }

</style>
<span class="span9" style="font-size: 14px">

    <#include '../userinfotitle.ftl' />

    <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
    </#if>

    <form class="form-horizontal" action="/equator/mailservice/list.vpage" method="get">
        <ul class="inline selectbtn">
            <input type="number" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="请输入学生id" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            <#if mailTypeList?? && mailTypeList?size gt 0>
            <select id="targetMailType" name="targetMailType">
                <#list mailTypeList as mailType>
                    <option value="${mailType.type?default("")}" <#if targetMailType?? && targetMailType == mailType.type?default("")>selected</#if> >${mailType.name?default("")}</option>
                </#list>
            </select>
            </#if>
            <input type="submit" value="查询" class="btn btn-primary"/>
        </ul>
    </form>

    <#if mailList?? && mailList?size gt 0>
    <div class="panel panel-info">
        <div class="panel-heading">
            <h4 class="panel-title">邮件列表</h4>
        </div>

        <div class="panel-body">
            <table class="table table-bordered">
                <tr>
                    <th width="150px">创建时间</th>
                    <th width="800px">邮件内容</th>
                    <th>奖励内容</th>
                </tr>
                <tbody>
                <#list mailList as mail>
                    <tr>
                        <td>${mail.ct?number_to_datetime}</td>
                        <td>${mail.mailDes!""}</td>
                        <td>
                            <#if mail.materialList?? >
                            <#list mail.materialList as reward>
                                <#if materialIdInfoCfgMap?? && materialIdInfoCfgMap[reward.materialId?default("")]?? >
                                    <#assign currentMaterialInfo = materialIdInfoCfgMap[reward.materialId?default("")]>
                                    <span title="道具名：${currentMaterialInfo.name?default("")}&#10;道具ID：${reward.materialId?default("")}&#10;${currentMaterialInfo.desc?default("")}">
                                        <#if currentMaterialInfo.icon?default("") != "" ><img style="width:20px;" src="${currentMaterialInfo.icon?default("")}"/> ${currentMaterialInfo.name?default("")}<#else>${currentMaterialInfo.name?default("")}</#if>*${reward.delta?default("")}
                                    </span>
                                <#else>
                                    ${reward.materialId?default("")}*${reward.delta?default("")}
                                </#if>
                                <br/>
                            </#list>
                            </#if>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
    </#if>
</span>

</@layout_default.page>