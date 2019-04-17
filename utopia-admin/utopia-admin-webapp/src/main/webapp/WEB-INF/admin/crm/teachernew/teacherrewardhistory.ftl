<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="background: none;">


<div style="margin-top: 2em">
    <ul class="inline">
        <li><span style="font-weight:600">老师可参与任务进度说明</span></li>
    </ul>
    <table class="table table-hover table-striped table-bordered">
        <tr id="title">
            <th> 任务类型</th>
            <th> 任务名称</th>
            <th> 是否自动领取</th>
            <th>是否周期性任务</th>
            <th> 领取时间</th>
            <th> 状态</th>
            <th> 任务进度</th>
            <th> 完成时间</th>
            <th> 过期时间</th>
        </tr>
    <#if teacherTask?has_content>
        <#list teacherTask as teacherTask>
            <#if teacherTask.taskList?has_content>
                <#list teacherTask.taskList as task>
                    <#if (task.crmIsDisplay == true)!false>
                        <tr <#if task.crmTaskDesc?has_content>class="task_tr_hover"</#if>>
                            <td>${teacherTask.name!''}</td>
                            <td class="task_name">
                            ${task.name!''}
                                <div class="task_hover_box" style="display: none;">
                                    <ul>
                                        <#if task.crmTaskDesc?has_content>
                                            <#list task.crmTaskDesc as desc>
                                                <li>${desc!''}</li>
                                            </#list>
                                        </#if>

                                    </ul>
                                </div>
                            </td>
                            <#if (task.autoReceive == true)!false>
                                <td>是</td>
                            <#else>
                                <td>否</td>
                            </#if>
                            <#if (task.cycle == true)!false>
                                <td>
                                    <#if (task.cycleUnit) == 'D'>
                                        日
                                    <#elseif (task.cycleUnit) == 'W' >
                                        周
                                    <#else>
                                        月
                                        </#if>任务
                                    </td>
                            <#else>
                                <td>否</td>
                            </#if>
                            <td>${task.receiveDate!'/'}</td>
                            <td>
                                <#if task.status == 'EXPIRED'>
                                    已过期
                                <#elseif task.status == 'ONGOING'>
                                    进行中
                                <#elseif task.status == 'FINISHED'>
                                    已完成
                                <#elseif task.status =='CANCEL'>
                                    已取消
                                <#elseif task.status == 'INIT'>
                                    未领取
                                </#if>
                            </td>
                            <td>
                                <#if task.crmProgressList?has_content>
                                    <#list task.crmProgressList as list>
                                        <#if list.target?? && list.desc??>
                                            <div>${list.desc!''} （${list.curr!''}/${list.target!''}）</div>
                                        <#else>
                                            <#if list.target??>
                                                <div>${list.curr!''}/${list.target!''}</div>
                                            <#else>
                                                <div>${list.desc!''}${list.curr!''}${list.q!''}</div>
                                            </#if>
                                        </#if>
                                    </#list>
                                <#else>/
                                </#if>
                            </td>
                            <td>${task.finishDate!'/'}</td>
                            <td>${task.expireDate!'/'}</td>
                        </tr>
                    </#if>
                </#list>
            </#if>
        </#list>
    <#else >
        <td >暂无历史信息</td>
    </#if>
    </table>
</div>
<script>
    $(".task_tr_hover").on('mouseover', '.task_name', function () {
        var _this = $(this);
        $(".task_hover_box").hide();
        _this.find(".task_hover_box").show();
    }).on('mouseout','.task_name', function () {
        $(".task_hover_box").hide();
    })
</script>
<style>
    .task_name{
        position: relative;
        cursor: pointer;
    }
    .task_name .task_hover_box{
        width: 200px; border: 1px solid grey;
        line-height:30px; padding:10px;
        position: absolute;
        background: #fff;
        z-index:10;
        right: -200px; top: 10px;
    }
    .reward_url{
        height: 40px;
        font-size: 16px;
        list-style: none;
    }
    .reward_ul li{
        list-style: none;
        overflow: hidden;
        margin-top:10px;
        float:left;width: 150px; line-height: 40px; height: 40px; border: 1px solid #0C0C0C;text-align: center;
        cursor: pointer;
    }
    .reward_ul li.active{
        background:#949494;
        text-shadow: 1px 1px 1px #000;
        color:#fff;
    }
    .reward_box .reward_div{
        display: none;}
</style>
<ul class="reward_ul">
    <li class="active">话费奖励记录</li>
    <li>园丁豆奖励记录</li>
    <li>等级积分奖励记录</li>
</ul>
<script>
    $(".reward_ul").on('click', 'li', function () {
        var _this = $(this);
        _this.addClass('active').siblings().removeClass('active');
        $('.reward_div').eq(_this.index()).show().siblings().hide();
    });
</script>
<div style="clear: both"></div>
<div class="reward_box" style="margin-left: 2em;">
    <div class="reward_div" style="margin-top: 20px; display: block;">
        <ul class="inline">
            <li><span style="font-weight:600">话费奖励记录</span></li>
        </ul>
        <ul class="inline">
            <li>
                获得话费总金额：<span style="font-weight:600"><#if teacherSummary??>${teacherSummary.rewardChargingSum!}</#if></span>
                <span style="margin-left: 3em;">获得话费总次数：<span style="font-weight:600"><#if teacherSummary??>${teacherSummary.rewardChargingCount!}</#if></span></span>
            </li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 时间</th>
                <th> 活动名称</th>
                <th> 充值手机号码</th>
                <th> 话费金额</th>
                <th> 短信内容</th>
            </tr>
        <#if wirelessChargingMapList?has_content>
            <#list wirelessChargingMapList as wirelessChargingMap>
                <tr>
                    <td>${wirelessChargingMap["wirelessCharging"].createDatetime!''}</td>
                    <td>${wirelessChargingMap["wirelessType"]!''}(${wirelessChargingMap["wirelessCharging"].chargeType!''})</td>
                    <td>${wirelessChargingMap["wirelessCharging"].targetSensitiveMobile!''}</td>
                    <td>${wirelessChargingMap["wirelessCharging"].amount/100}</td>
                    <td>${wirelessChargingMap["wirelessCharging"].notifySmsMessage!''}</td>
                </tr>
            </#list>
        <#else ><td >暂无奖励记录</td>
        </#if>
        </table>
    </div>

    <div class="reward_div" style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">园丁豆奖励记录<span style="font-size: 13px;color: grey;">(仅当前园丁豆数量为实时数据)</span></span></li>
        </ul>
        <ul class="inline">
            <li>
                当前园丁豆数量：<span style="font-weight:600">${nowIntegral/10}</span>
                <span style="margin-left: 3em;">历史获得园丁豆总量：<span style="font-weight:600">--</span></span>
                <span style="margin-left: 3em;">历史消耗园丁豆总量：<span style="font-weight:600">--</span></span>
                <span style="margin-left: 3em;">获得园丁豆次数：<span style="font-weight:600">--</span></span>
                <span style="margin-left: 3em;">消耗园丁豆次数：<span style="font-weight:600">--</span></span>
            </li>
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 时间</th>
                <th> 积分类型</th>
                <th> 园丁豆数量</th>
                <th> 备注</th>
            </tr>
            <#if integralHistoryMapList?has_content>
                <#list integralHistoryMapList as integralHistoryMap>
                    <tr>
                        <td>${integralHistoryMap["IntegralHistory"].createtime!''}</td>
                        <td>${integralHistoryMap["integralType"]!''}</td>
                        <td>${integralHistoryMap["IntegralHistory"].integral!''}</td>
                        <td>${integralHistoryMap["IntegralHistory"].comment!''}</td>
                    </tr>
                </#list>
            <#else ><td >暂无奖励记录</td>
            </#if>
        </table>
    </div>

    <div class="reward_div" style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">等级积分奖励记录</span></li>
        </ul>
        <div>
            <span style="padding-left:20px;">当前等级：${expInfo.levelName!''}</span>
            <span style="padding-left:20px;">当前等级积分：${expInfo.exp!''}</span>
            <span style="padding-left:20px;">当前等级有效期：${expInfo.levelValidDate}</span>
        </div>
        <table style="margin-top:20px;" class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 时间</th>
                <th> 类型</th>
                <th> 等级积分数</th>
                <th> 备注</th>
            </tr>
            <#if expInfo.teacherExpHistory?has_content>
                <#list expInfo.teacherExpHistory as his>
                    <tr>
                        <td>${his.createDatetime!'/'}</td>
                        <td>${his.type!'/'}</td>
                        <td>${his.exp!0}</td>
                        <td>${his.comment!'无'}</td>
                    </tr>
                </#list>
            <#else>
                <td >暂无奖励记录</td>
            </#if>
        </table>
    </div>

</div>
</body>
</html>