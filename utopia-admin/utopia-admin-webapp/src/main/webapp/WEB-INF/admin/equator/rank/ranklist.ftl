<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="排行榜详情查询" page_num=24>
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

    .label-info {
        background-color: #5EA8DC;
    }

    dd {
        margin-left: 0px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <ul class="nav nav-tabs" role="tablist" id="equatorUserInfoHeader">
        <li role="presentation" class="active"><a data-url="/equator/newwonderland/material" href="/equator/newwonderland/common/rank/detail.vpage?studentId=${studentId!''}">排行榜详情查询</a></li>
        <li role="presentation"><a data-url="/equator/newwonderland/architecture" href="/equator/newwonderland/common/rank/reward.vpage?studentId=${studentId!''}">排行榜全部奖励组合查询</a></li>
    </ul>

    <form class="form-horizontal" id="selectForm" action="/equator/newwonderland/common/rank/detail.vpage" method="get">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <input type="number" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus" placeholder="请输入学生id"/>
        <#if rankTypeList?? && rankTypeList?size gt 0>
            <select id="targetRankType" name="targetRankType">
                <#list rankTypeList as rankType>
                    <#if rankType_index != 0 >
                    <option value="${rankType.type?default("")}"
                            data-keys="<#list rankType.keys as key><#if key_index != 0>,</#if>${key}</#list>"
                            <#if targetRankType?? && targetRankType == rankType.type?default("")>selected</#if> >${rankType.name?default("")}</option>
                    </#if>
                </#list>
            </select>
        </#if>
        <input type="hidden" name="selectType" id="selectType"/>
        <input type="button" value="自动查询" class="btn btn-primary" onclick="$('#selectType').val('auto'); $('#selectForm').submit();"/>
        (如需要改变个别缓存Key参数查询时，请修改默认参数后点击变参查询!!)
        <#if studentId?? >
        <div class="panel-body">
            <ul class="inline">
                <#list supplementKeyTypeList as supplementKeyType>
                <li class="rankSupplementKeyInput" id="input_${supplementKeyType.name?default("")}"
                    style="display: none;">
                    <dt>${supplementKeyType.desc?default("")}</dt>
                    <dd>
                        <#if supplementKeyType.name?default("") == "ATTID" && activityConfigInfoList?? && activityConfigInfoList?size gt 0>
                        <select name="ATTID" style="width: 350px;">
                            <#list activityConfigInfoList as activityInfo>
                                <option value="${activityInfo.id?default("")}"
                                    <#if supplementFinalInfoMap?? && activityInfo.id?default("") == supplementFinalInfoMap[supplementKeyType.name] >selected</#if> >${activityInfo.projectName!""}(${activityInfo.startDate?default("")}~${activityInfo.endDate?default("")})</option>
                            </#list>
                        </select>
                        <#elseif supplementKeyType.name?default("") == "PCD" && exRegionList?? && exRegionList?size gt 0>
                        <select name="PCD" style="width: 150px;">
                            <#list exRegionList as exRegion>
                                <option value="${exRegion.provinceCode?default("")}"
                                    <#if supplementFinalInfoMap?? && exRegion.provinceCode?default("")?c == supplementFinalInfoMap[supplementKeyType.name] >selected</#if> >${exRegion.provinceName?default("")}(${exRegion.provinceCode?default("")})</option>
                            </#list>
                        </select>
                        <#else>
                        <input type="text" style="width: 100px;" name="${supplementKeyType.name?default("")}"
                               value="<#if supplementFinalInfoMap?? && supplementFinalInfoMap[supplementKeyType.name]??>${supplementFinalInfoMap[supplementKeyType.name]}</#if>"/>
                        </#if>
                    </dd>
                </li>
                </#list>
                <li>
                    <dt>其他附加信息</dt>
                    <dd><input type="text" style="width: 100px;" name="OTHER" value="${OTHER?default("")}"/>
                        <input type="button" value="变参查询" class="btn btn-primary" onclick="$('#selectType').val('hand'); $('#selectForm').submit();"/>
                    </dd>
                </li>
            </ul>
            *活动ID：请根据活动自行选择，系统只会默认选中时间靠后一个活动!!<br/>
            *格式化日期：周榜请以当周周一日期为准进行查询，日榜或其他形式的日期可自行修改后查询!!
        </div>
        </#if>
    </form>

    <#if currentRankType?? >
    <div style="width:74%;display: inline-block;vertical-align: top;">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title">本榜详情列表</h4>
            </div>
            <div class="panel-body">
                <table class="table table-bordered">
                    <tr style="text-align: center;">
                        <th width="50px">位次</th>
                        <th>基本信息</th>
                        <th width="70px">排行分数</th>
                        <th width="130px" title="这是最后一次因分数变化而刷新榜单的时间，交换名次类榜单（如伙伴竞技）此值不会刷新无参考意义">分数更新时间</th>
                        <#if rankFieldDescInfo?? && rankFieldDescInfo?size gt 0>
                            <#list rankFieldDescInfo?keys as fieldname>
                            <th>${rankFieldDescInfo[fieldname]?string}</th>
                            </#list>
                        </#if>
                    </tr>
                    <tbody>
                    <#if userRankBase?? >
                    <tr style="background-color: #FFE4E1;" title="这是上面输入的用户排名信息">
                        <td><#if userRankBase.ranking?default(0) != 0>${userRankBase.ranking?default(0)}<#else>暂无</#if></td>
                        <td>
                            ${userRankBase.schoolName?default("")}
                            ${userRankBase.className?default("")}
                            ${userRankBase.studentName?default("")}
                            <img width="20px" src="${userRankBase.profileUrl?default("")}"/>
                        </td>
                        <td><#if userRankBase.rankScore?default(0)?int != 0 >${userRankBase.rankScore?default(0)?int}</#if></td>
                        <td>
                            <#if rankInfoMapList?? && rankInfoMapList?size gt 0 && userRankBase.ranking?default(0) != 0>
                            <button class="btn btn-danger" id="removeTargetMemberRankInfo" <#if !isSuperAdmin!false >style="display: none;"</#if> title="该操作将删除当前用户在排行榜上的数据，请谨慎操作哦~"
                                    data-targetRankType="${currentRankType?default("")}" data-targetId="${userRankBase.uniqueId?default("")}"
                                    data-url="/equator/newwonderland/common/rank/removeuserrankinfo.vpage?<#if currentRankType.supplementKeys?? && currentRankType.supplementKeys?size gt 0><#list currentRankType.supplementKeys as supplementKey>&${supplementKey?default("")}=${supplementFinalInfoMap[supplementKey]}</#list></#if>&OTHER=${OTHER?default("")}" >删除榜单信息</button></td>
                            </#if>
                         <#if rankFieldDescInfo?? && rankFieldDescInfo?size gt 0>
                            <td colspan="${rankFieldDescInfo?size}"></td></#if>
                    </tr>
                    </#if>
                    <#if rankInfoMapList?? && rankInfoMapList?size gt 0>
                    <#list rankInfoMapList as rankInfo>
                    <tr>
                        <td>${rankInfo["ranking"]?default("暂无")}</td>
                        <td>
                            ${rankInfo["uniqueId"]?default("")}
                            ${rankInfo["schoolName"]?default("")}
                            ${rankInfo["className"]?default("")}
                            ${rankInfo["studentName"]?default("")}
                            <#if rankInfo["studentName"]?? && rankInfo["studentName"] != ""><img width="20px" src="${rankInfo["profileUrl"]}"/></#if>
                        </td>
                        <td><#if rankInfo["rankScore"]?default(0)?int != 0 >${rankInfo["rankScore"]?default(0)?int}</#if></td>
                        <td>${rankInfo["lastUpdateRankTime"]?default("")}</td>
                        <#if rankFieldDescInfo?? && rankFieldDescInfo?size gt 0>
                            <#list rankFieldDescInfo?keys as fieldname>
                            <td><#if rankInfo[fieldname]?? >${rankInfo[fieldname]?default("")?string}</#if></td>
                            </#list>
                        </#if>
                    </tr>
                    </#list>
                    </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    </#if>

    <div style="width:25%;display: inline-block;vertical-align: top;">
        <#if currentRankType?? >
        <div>
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h4 class="panel-title">本榜相关信息</h4>
                </div>
                <div class="panel-body">
                    <table class="table table-bordered">
                        <tbody>
                        <tr><td width="100px"><strong>榜单名</strong></td><td>${currentRankType.desc?default("")}</td></tr>
                        <tr><td><strong>榜单枚举名</strong></td><td>${currentRankType?default("")}</td></tr>
                        <tr><td><strong>榜单辅助变量</strong></td><td>
                            <#if currentRankType.supplementKeys?? && currentRankType.supplementKeys?size gt 0>
                            <#list currentRankType.supplementKeys as supplementKey>
                                ${supplementKey}:${supplementFinalInfoMap[supplementKey]}<br/>
                            </#list>
                            </#if>
                            OTHER:${OTHER?default("")}
                        </td></tr>
                        <#if currentRankType.provinceFilter?? ><tr title="true表示该榜对测试账号进行过滤"><td><strong>测试号省榜过滤</strong></td><td>${currentRankType.provinceFilter?c}</td></tr></#if>
                        <tr><td><strong>缓存默认有效期</strong></td><td><#if defaultTTL?default(0) gt 72 >${(defaultTTL/24)?int}天<#else>${defaultTTL?default(0)}小时</#if></td></tr>

                        <tr><td><strong>缓存剩余有效期</strong></td><td>
                            <#if surplusTTL?default(0) != 0 && rankCount?default(0) != 0>
                                <#if surplusTTL?default(0) gt 72 >${(surplusTTL/24)?int}天<#else>${surplusTTL?default(0)}小时</#if>
                                <div style="float: right"><button id="resetRankTTL" class="btn" title="点我可以延长剩余有效期哦" data-targetRankType="${currentRankType?default("")}" data-url="/equator/newwonderland/common/rank/resetrankttl.vpage?<#if currentRankType.supplementKeys?? && currentRankType.supplementKeys?size gt 0><#list currentRankType.supplementKeys as supplementKey>&${supplementKey?default("")}=${supplementFinalInfoMap[supplementKey]}</#list></#if>&OTHER=${OTHER?default("")}" >延长</button>
                                <#if OTHER?default("") == ""><button id="removeTargetRank" class="btn btn-danger" <#if !isSuperAdmin!false >style="display: none;"</#if> title="点我可就直接删除当前的榜单喽！！该操作只限管理员~" data-targetRankType="${currentRankType?default("")}" data-url="/equator/newwonderland/common/rank/removerank.vpage?<#if currentRankType.supplementKeys?? && currentRankType.supplementKeys?size gt 0><#list currentRankType.supplementKeys as supplementKey>&${supplementKey?default("")}=${supplementFinalInfoMap[supplementKey]}</#list></#if>&OTHER=${OTHER?default("")}" >删除</button></#if>
                                </div>
                            <#else>
                                不存在或已失效被删除
                            </#if>
                        </td></tr>
                        <#if surplusTTLForBackKey?? && surplusTTLForBackKey?default(0) != 0>
                        <tr><td><strong>缓存备份有效期</strong></td><td><#if surplusTTLForBackKey?default(0) gt 72 >${(surplusTTLForBackKey/24)?int}天<#else>${surplusTTLForBackKey?default(0)}小时</#if>
                            <div style="float: right"><button id="recoverTargetRank" class="btn btn-danger" data-targetRankType="${currentRankType?default("")}" data-url="/equator/newwonderland/common/rank/removerank.vpage?<#if currentRankType.supplementKeys?? && currentRankType.supplementKeys?size gt 0><#list currentRankType.supplementKeys as supplementKey>&${supplementKey?default("")}=${supplementFinalInfoMap[supplementKey]}</#list></#if>&OTHER=${OTHER?default("")}&recover=true" >恢复榜单</button></div>
                        </td></tr>
                        </#if>
                        <tr><td><strong>当前榜单总人数</strong></td><td>${rankCount?default(0)}名</td></tr>
                        <tr><td title="右侧为该榜单能容纳的详情最大人数"><strong>详情榜单总人数</strong></td><td><#if rankInfoMapList?? >${rankInfoMapList?size}名<#else>
                            暂无</#if>/<#if currentRankType.rankRange?default(0) == 0 >不限制<#else>${currentRankType.rankRange?default(0)}名</#if></td></tr>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <#if rankRewardTypeList?? && rewardResponseMap?? && rankRewardTypeList?size gt 0>
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title">本榜奖励信息
                <select id="rankRewardTypeSelect" style="float: right; width: 200px;" title="这里是将所有奖励类型直接全部展示，每期活动所用的奖励类型可能不同，查询时请向开发者先行询问当前期活动所使用的奖励类型~">
                    <#list rankRewardTypeList as rankRewardType>
                    <option value="${rankRewardType.type?default("")}" <#if rankRewardType_index == 0> selected</#if> >${rankRewardType.name?default("")}</option>
                    </#list>
                </select>
                </h4>
            </div>

            <#list rankRewardTypeList as rankRewardType>
            <#if rewardResponseMap[rankRewardType.type]?? >
            <div class="panel-body rankRewardList" id="${rankRewardType.type?default("")}" style="display: none;">
                <table class="table table-bordered">
                    <tr>
                        <th width="70px">位次</th>
                        <th>奖励列表</th>
                    </tr>
                    <tbody>
                    <#assign currRewardVoList = rewardResponseMap[rankRewardType.type].rankRewardVoList >
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
            </#if>
            </#list>
            <div class="panel-body">
                可以给${studentId!''}直接发送第<input id="targetRanking" type="number" style="width: 50px; vertical-align: center;" value="<#if userRankBase?? >${userRankBase.ranking?default(0)}</#if>" placeholder="指定用户名次"/>名奖励哦~<br/>
                邮件通知内容：<br/><textarea id="targetMailContent" style="width: 90%;" placeholder="输入内容时用户可同时收到邮件通知，所以请注意别有错别字哈~是不是很nice~" ></textarea><br/>
                <input id="sendTargetRankingReward" type="button" value="发送" class="btn btn-danger" />
            </div>
        </#if>
    </#if>
</span>
<script>

    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    $(function () {
        // 排行榜种类菜单初始化
        if ($("#targetRankType").length > 0) {
            filterInputView();
        }

        // 榜单奖励下拉菜单初始化
        if($("#rankRewardTypeSelect").length > 0){
            filterRankData($("#rankRewardTypeSelect").val());
        }
    });

    $('#targetRankType').on('change', function () {
        filterInputView();
    });

    function filterInputView() {
        var showKeys = $("#targetRankType").find("option:selected").attr("data-keys").split(',');
        $("li.rankSupplementKeyInput").hide();
        $.each(showKeys, function (index, key) {
            $("#input_" + key).show();
        });
    }

    $('#rankRewardTypeSelect').on('change', function () {
        filterRankData($(this).val());
    });

    function filterRankData(index) {
        $("div.rankRewardList").hide();
        $("#" + index).show();
    }

    // 删除排行榜中指定成员的信息
    $(function () {
        $('#removeTargetMemberRankInfo').click(function () {
            var targetRankType = $(this).attr("data-targetRankType");
            var targetId = $(this).attr("data-targetId");
            var requestUrl = $(this).attr("data-url");
            if (isBlank(targetRankType) || isBlank(targetId)) {
                alert("榜单种类或指定ID不能为空！");
                return false;
            }
            if (!confirm("确定要删除吗？管理员可是会收到邮件提醒的哦！~")) {
                return false;
            }
            $.post(requestUrl, {
                targetId:targetId,
                targetRankType:targetRankType
            }, function (data) {
                if (data.success) {
                    alert("操作成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    // 重置榜单缓存有效时间
    $(function () {
        $('#resetRankTTL').click(function () {
            var targetRankType = $(this).attr("data-targetRankType");
            var requestUrl = $(this).attr("data-url");
            if (!confirm("确定要延长到默认最大有效期吗？")) {
                return false;
            }
        $.post(requestUrl, {
            targetRankType:targetRankType
        }, function (data) {
                if (data.success) {
                    alert("操作成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    // 删除指定排行榜
    $(function () {
        $('#removeTargetRank').click(function () {
            var targetRankType = $(this).attr("data-targetRankType");
            var requestUrl = $(this).attr("data-url");
            if (isBlank(targetRankType)) {
                alert("榜单种类不能为空！");
                return false;
            }
            if (!confirm("确定要删除吗？管理员可是会收到邮件提醒的哦！~")) {
                return false;
            }
            if (!confirm("真的确定要删除吗？你可能因此而熬夜修数据哦！！~")) {
                return false;
            }
            $.post(requestUrl, {
                targetRankType:targetRankType
            }, function (data) {
                if (data.success) {
                    alert("操作成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    // 恢复指定排行榜
    $(function () {
        $('#recoverTargetRank').click(function () {
            var targetRankType = $(this).attr("data-targetRankType");
            var requestUrl = $(this).attr("data-url");
            if (isBlank(targetRankType)) {
                alert("榜单种类不能为空！");
                return false;
            }
            if (!confirm("确定要恢复吗？管理员可是会收到邮件提醒的哦！~")) {
                return false;
            }
            $.post(requestUrl, {
                targetRankType:targetRankType
            }, function (data) {
                if (data.success) {
                    alert("操作成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
    });

    // 发送指定名次排行榜奖励
    $(function () {
        $('#sendTargetRankingReward').click(function () {
            var targetRankRewardType = $("#rankRewardTypeSelect").val();
            var targetStudentId = $("#studentId").val();
            var targetRanking = $("#targetRanking").val();
            var targetMailContent = $("#targetMailContent").val();

            if (isBlank(targetRankRewardType)) {
                alert("榜单奖励种类不能为空！");
                return false;
            }
            if (isBlank(targetStudentId)) {
                alert("指定用户ID不能为空！");
                return false;
            }
            if (isBlank(targetRanking) || targetRanking < 1) {
                alert("指定奖励名次有误！");
                return false;
            }
            if (!confirm("确定的要给"+targetStudentId+"发送第"+targetRanking+"名的奖励吗？\n邮件内容为:"+targetMailContent+"")) {
                return false;
            }
            $.post("/equator/newwonderland/common/rank/sendrankreward.vpage", {
                targetRankRewardType:targetRankRewardType,
                targetStudentId:targetStudentId,
                targetRanking:targetRanking,
                targetMailContent:targetMailContent
            }, function (data) {
                if (data.success) {
                    alert("操作成功！");
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>