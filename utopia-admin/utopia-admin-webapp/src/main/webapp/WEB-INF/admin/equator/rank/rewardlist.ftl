<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="排行榜全部奖励组合查询" page_num=24>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
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
        color: #a94442;
        background-color: #f2dede;
        border-color: #ebccd1;
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

    .label {
        margin-right: 5px;
    }

    .label-info {
        background-color: #5EA8DC;
    }

    hr {
        margin-top: 15px;
        margin-bottom: 10px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <ul class="nav nav-tabs" role="tablist" id="equatorUserInfoHeader">
        <li role="presentation"><a data-url="/equator/newwonderland/material" href="/equator/newwonderland/common/rank/detail.vpage?studentId=${studentId!''}">排行榜详情查询</a></li>
        <li role="presentation" class="active"><a data-url="/equator/newwonderland/architecture" href="/equator/newwonderland/common/rank/reward.vpage?studentId=${studentId!''}">排行榜全部奖励组合查询</a></li>
    </ul>

    <#if rankRewardTypeList?? && rewardResponseMap?? && rankRewardTypeList?size gt 0>
        <#list rankRewardTypeList as rewardType>
        <#if rewardResponseMap[rewardType.type]?? >
        <#assign currRewardVoList = rewardResponseMap[rewardType.type].rankRewardVoList >
        <div style="width:32%;display: inline-block;vertical-align: top;">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h4 class="panel-title" title="${rewardType.type?default("")}">${rewardType.name?default("")}</h4>
                </div>
                <div class="panel-body">
                    <table class="table table-bordered">
                        <tr>
                            <th width="70px">位次</th>
                            <th>奖励列表</th>
                        </tr>
                        <tbody>
                        <#list currRewardVoList as rewardVo>
                        <tr>
                            <th style="text-align: center;"><#if rewardVo.startRanking?default("") == rewardVo.endRanking?default("")>${rewardVo.startRanking?default("")}<#else>${rewardVo.startRanking?default("")}~${rewardVo.endRanking?default("")}</#if></th>
                            <td>
                                <#list rewardVo.materialQuantityInfoVoList as reward>
                                    <#if materialIdInfoCfgMap?? && materialIdInfoCfgMap[reward.materialId?default("")]?? >
                                        <#assign currentMaterialInfo = materialIdInfoCfgMap[reward.materialId?default("")]>
                                    <span title="道具名：${currentMaterialInfo.name?default("")}&#10;道具ID：${reward.materialId?default("")}&#10;${currentMaterialInfo.desc?default("")}">
                                        <#if currentMaterialInfo.icon?default("") != "" ><img style="width:20px;" src="${currentMaterialInfo.icon?default("")}"/> ${currentMaterialInfo.name?default("")}<#else>${currentMaterialInfo.name?default("")}</#if>*${reward.quantity?default("")}
                                    </span>
                                    <#else>
                                        ${reward.materialId?default("")}*${reward.quantity?default("")}
                                    </#if>
                                    <br/>
                                </#list>
                            </td>
                        </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        </#if>
        </#list>
    </#if>
</span>
</@layout_default.page>