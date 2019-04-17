<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="青苗乐园" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
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
        padding: 15px;
    }

    .btn {
        height: 25px;
        font-size: 6px;
    }

    .table {
        width: 100%;
        margin-bottom: 0px;
    }

</style>

<span class="span9" style="font-size: 14px">
    <#include '../userinfotitle.ftl' />
    <form class="form-horizontal" action="/equator/newwonderland/sapling/saplingInfo.vpage" method="post">
        <#if error??>
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong>${error!}</strong>
        </div>
        </#if>
        <ul class="inline selectbtn">
            学生ID：<input type="text" id="studentId" name="studentId" value="${studentId!''}" autofocus="autofocus"
                        placeholder="输入学生ID" onkeyup="value=value.replace(/[^\d]/g,'')"/>
            <input type="submit" value="查询" class="btn btn-primary"/>
            <#if studentId??>
                <input type="button" class="btn btn-default" onclick="window.open('/equator/student/history/index.vpage?studentId=${studentId}&moduleType=Sapling')" value="学生青苗操作记录"/>
            </#if>
        </ul>
    </form>
    <#if treeData??>
    <#setting datetime_format="yyyy-MM-dd HH:mm:ss"/>
    <div class="panel panel-info" style="width: 34%; display: inline-block;vertical-align: top;">
            <div class="panel-heading">
                <h3 class="panel-title">学生青苗乐园信息
                </h3>
            </div>
            <div class="panel-body" ">
                <#if treeData.status??>
                    <input type="hidden" name="status" id="status" value="${treeData.status}"/>
                    <#if treeData.status=='seedBefore'>
                        <p>状态：<strong>未播种</strong></p>
                        <p>种子数量：<strong>${treeData.seedNum?default(0)}
                        </strong></p>
                    <#elseif treeData.status=='matching'>
                        <p>状态：<strong>匹配中</strong></p>
                        <p>播种时间：<strong><#if treeData.seedTime??>${treeData.seedTime?number_to_datetime}</#if></strong></p>
                        <p>当前昵称：<strong>${treeData.userNickName!"好友"}</strong></p>
                    <#elseif treeData.status=='growing'>
                        <ul style="width: auto;list-style-type:none;margin-left: 0px;">
                            <li style="width: 50%;float: left;padding-bottom: 10px;">状态：<strong>成长中</strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前伙伴id：<a href="/equator/newwonderland/sapling/saplingInfo.vpage?studentId=${treeData.partnerStudentId!''}" target="_blank"><strong>${treeData.partnerStudentId!'null'}</strong></a></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前昵称：<strong>${treeData.userNickName!"好友"}</strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前树的高度：<strong><#if treeData.treeHeight??>${(treeData.treeHeight/1000)?string("0.###")}</#if></strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前树的高度：<strong><#if treeData.treeHeight??>${(treeData.treeHeight/1000)?string("0.###")}</#if></strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前树的阶段：<strong>${treeData.treeStage?default("1")}</strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">升级需要的阳光数：<strong>${treeData.upgradeSunNum?default("0")}</strong></li>
                            <li style="width: 50%;float: left;padding-bottom: 10px;">当前树是否成熟：<strong><#if treeData.treeRipeFlag>是<#else>否</#if></strong></li>
                        </ul>
                        <p>
                            <a href="/equator/newwonderland/sapling/letterproanswer.vpage?studentId=${studentId!''}&saplingCommId=${treeData.saplingCommId!''}" target="_blank"><strong>查看信的题目及答案</strong></a>
                        </p>
                    <#else>
                        暂无记录
                    </#if>
                </#if>
            </div>
    </div>
    <div class="panel panel-info" style="width: 35%; display: inline-block;vertical-align: top;">
        <table class="table table-bordered">
            <div class="panel-heading">
                <h4 class="panel-title">
                    学科阳光   <button class="btn btn-success btn-small" onclick="updateSun()">增加阳光</button>
                </h4>
            </div>
            <tr>
                <th style="text-align:center;vertical-align:middle;">学科</th>
                <th style="text-align:center;vertical-align:middle;">数量</th>
                <#if treeData.status=='matching'>
                    <th style="text-align:center;vertical-align:middle;">冻结小时数</th>
                <#else>
                    <th style="text-align:center;vertical-align:middle;">到期时间</th>
                </#if>
            </tr>
            <tbody id="tbody">
                <#if treeData.downTimeSun?? && (treeData.downTimeSun?size>0)>
                    <#list treeData.downTimeSun as sun>
                        <tr>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if sun.subjectType??>
                                <#if sun.subjectType=='ChineseSun'>
                                    语文
                                <#elseif sun.subjectType=='EnglishSun'>
                                    英语
                                <#elseif sun.subjectType=='MathSun'>
                                    数学
                                <#else>
                                    其他
                                </#if>
                            </#if>
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${sun.sunCount?default(0)}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if treeData.status=='matching'>
                                <#if sun.deltaTime??>
                                    <#assign dlong = (sun.deltaTime/1000)?number />
                                    <#assign minNum=(dlong%(60*60))?number/>
                                    <#if dlong gte (60*60) >
                                        ${(dlong/(60*60))?int}小时
                                    </#if>
                                    <#if minNum gte 60>
                                        ${(minNum/60)?int}分钟
                                    </#if>
                                    <#if minNum%60 gt 0>
                                        ${minNum%60} 秒
                                    </#if>
                                <#else>
                                    无
                                </#if>
                            <#else>
                                <#if sun.endTime??>
                                    ${sun.endTime?number_to_datetime}
                                <#else>
                                            无
                                </#if>
                            </#if>
                        </td>
                        </tr>
                    </#list>
                <#else>
                <tr>
                    <td colspan="4" style="text-align:center;vertical-align:middle;">暂无</td>
                </tr>
                </#if>
            </tbody>
        </table>
    </div>
    <div class="panel panel-info" style="width: 70%; display: inline-block;vertical-align: top;">
            <div class="panel-heading">
                <h3 class="panel-title">当前树灵信息
                </h3>
            </div>
            <div class="panel-body" ">
                <#if treeData.currentSaplingSprite??>
                    <ul style="width: auto;list-style-type:none;margin-left: 0px;">
                        <li style="width: 50%;float: left;padding-bottom: 10px;">宠物id：<strong>${treeData.currentSaplingSprite.petId!'null'}</strong></li>
                        <li style="width: 50%;float: left;padding-bottom: 10px;">经验值：<strong>${treeData.currentSaplingSprite.exp!'0'}</strong></li>
                        <li style="width: 50%;float: left;padding-bottom: 10px;">当前阶段：<strong>${treeData.currentSaplingSprite.stage!'0'}</strong></li>
                        <li style="width: 50%;float: left;padding-bottom: 10px;">溢出成长值：<strong>${treeData.currentSaplingSprite.overTotalExp!'0'}</strong></li>
                    </ul>

                <#else>
                    暂无树灵
                </#if>
            </div>
    </div>
    <#if treeConfig??>
        <div class="panel panel-info" style="width:70%;display: inline-block;vertical-align: top;">
            <div class="panel-heading">
                <h3 class="panel-title">当前树配置信息
                </h3>
            </div>
            <div class="panel-body" ">
                <ul style="width: auto;list-style-type:none;margin-left: 0px;">
                    <li style="width: 33%;float: left;padding-bottom: 10px;">树配置ID：<strong>${treeConfig.id?default("null")}</strong></li>
                    <li style="width: 33%;float: left;padding-bottom: 10px;">总阶段：<strong>${treeConfig.stageNum?default("0")}</strong></li>
                    <li style="width: 33%;float: left;padding-bottom: 10px;">单位阳光的高度：<strong><#if treeConfig.oneSunHeight??>${(treeConfig.oneSunHeight/1000)?string("0.###")}米</#if></strong></li>
                </ul>
                <table class="table table-bordered">
                    <div class="panel-heading">
                        <h4 class="panel-title">各个阶段高度</h4>
                    </div>
                    <tr>
                        <th style="text-align:center;vertical-align:middle;">阶段</th>
                        <th style="text-align:center;vertical-align:middle;">最低高度(包含)</th>
                        <th style="text-align:center;vertical-align:middle;">最高高度(不包含)</th>
                        <th style="text-align:center;vertical-align:middle;">需要阳光个数</th>
                    </tr>
                    <tbody id="tbody">
                        <#if treeStage??>
                            <#list treeStage as stage>
                                <tr>
                                    <td style="text-align:center;vertical-align:middle;">${stage.stage?default("null")}</td>
                                    <td style="text-align:center;vertical-align:middle;"><#if stage.lastStageHeight??>${(stage.lastStageHeight/1000)?string("0.###")}米 <#else>0米</#if> </td>
                                    <td style="text-align:center;vertical-align:middle;"><#if stage.height??>${(stage.height/1000)?string("0.###")}米 <#else>0米</#if></td>
                                    <td style="text-align:center;vertical-align:middle;">${((stage.height-stage.lastStageHeight)/(treeConfig.oneSunHeight?number))?default(0)} </td>
                                </tr>
                            </#list>
                            <tr>
                                <td style="text-align:center;vertical-align:middle;">${treeConfig.stageNum!}</td>
                                <td style="text-align:center;vertical-align:middle;" colspan="3">
                                    <#list treeConfig.multiStageHeight?split(",") as tempStageHeight>
                                        <#if !tempStageHeight_has_next>
                                            <#if tempStageHeight??>${((tempStageHeight?number)/1000)?string("0.###")}米 <#else>0米</#if>
                                        </#if>
                                    </#list>
                                </td>
                            </tr>
                        <#else>
                            <tr>
                                <td colspan="3" style="text-align:center;vertical-align:middle;">暂无</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </#if>
    <div class="panel panel-info" style="width: 70%;display: inline-block;vertical-align: top;">
        <table class="table table-bordered">
            <div class="panel-heading">
                <h4 class="panel-title">
                    已成熟的树木
                </h4>
            </div>
            <tr>
                <th style="text-align:center;vertical-align:middle;">树木id</th>
                <th style="text-align:center;vertical-align:middle;">伙伴id</th>
                <th style="text-align:center;vertical-align:middle;">树配置id</th>
                <th style="text-align:center;vertical-align:middle;">播种时间</th>
                <th style="text-align:center;vertical-align:middle;">匹配时间</th>
                <th style="text-align:center;vertical-align:middle;">收获时间</th>
            </tr>
            <tbody id="tbody">
                <#if treeMaturity?? && (treeMaturity?size>0)>
                    <#list treeMaturity as tree>
                    <tr>
                        <td style="text-align:center;vertical-align:middle;">
                            ${tree.saplingCommId?default('null')}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${tree.partnerSid?default('null')}
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            ${tree.treeId?default('null')}
                        </td>

                        <td style="text-align:center;vertical-align:middle;">
                            <#if tree.seedTime??>
                                ${tree.seedTime?number_to_datetime}
                            </#if>
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if tree.matchSuccessTime??>
                                ${tree.matchSuccessTime?number_to_datetime}
                            </#if>
                        </td>
                        <td style="text-align:center;vertical-align:middle;">
                            <#if tree.receiveTime??>
                                ${tree.receiveTime?number_to_datetime}
                            </#if>
                        </td>
                    </tr>
                    </#list>
                <#else>
                    <td colspan="6" style="text-align:center;vertical-align:middle;">暂无</td>
                </#if>
            </tbody>
        </table>

    </div>
    <#if treeData.messageList??>
        <div class="panel panel-info" style="width:70%;display: inline-block;vertical-align: top;">
            <table class="table table-bordered">
                <div class="panel-heading">
                    <h4 class="panel-title">
                        动态消息
                    </h4>
                </div>
                <tr>
                    <th style="text-align:center;vertical-align:middle;">消息类型</th>
                    <th style="text-align:center;vertical-align:middle;">生成时间</th>
                    <th style="text-align:center;vertical-align:middle;">阳光类型</th>
                    <th style="text-align:center;vertical-align:middle;">阳光数</th>
                    <th style="text-align:center;vertical-align:middle;">谁产生的</th>
                </tr>
                <tbody id="tbody">
                    <#if (treeData.messageList?size>0)>
                        <#list treeData.messageList as message>
                        <tr>
                            <td style="text-align:center;vertical-align:middle;">
                            <#if message.sunType?? >
                                <#if message.saplingMessageType=='Produce'>
                                    任务产生
                                <#elseif message.saplingMessageType=='Collect'>
                                    收集
                                <#elseif message.saplingMessageType=='GainAndPlant'>
                                    好友任务提供
                                <#elseif message.saplingMessageType=='Pass'>
                                    树升级
                                <#elseif message.saplingMessageType=='Finish'>
                                    树成熟
                                </#if>
                            </#if>
                            </td>
                            <td style="text-align:center;vertical-align:middle;">
                                <#if message.createTime??>
                                    ${message.createTime?number_to_datetime}
                                </#if>
                            </td>
                            <td style="text-align:center;vertical-align:middle;">
                                <#if message.sunType?? >
                                    <#if message.sunType=='ChineseSun'>
                                        语文
                                    <#elseif message.sunType=='EnglishSun'>
                                        英语
                                    <#elseif message.sunType=='MathSun'>
                                        数学
                                    <#else>
                                        其他
                                    </#if>
                                </#if>
                            </td>

                            <td style="text-align:center;vertical-align:middle;">
                                ${message.sunlightNum?default(0)}
                            </td>
                            <td style="text-align:center;vertical-align:middle;">
                                <#if message.amI>
                                    自己
                                <#else>
                                    好友
                                </#if>
                            </td>
                        </tr>
                        </#list>
                    <#else>
                        <tr><td colspan="6" style="text-align:center;vertical-align:middle;">暂无</td></tr>
                    </#if>
                </tbody>
            </table>
        </div>
    </#if>



    </#if>

    <div id="grant_sun_dialog" class="modal hide fade">
        <div class="modal-header">
            <input type="hidden" name="sunType" id="sunType"/>
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>调整

                阳光</h3>
        </div>
        <div class="modal-body">
            <label for="adjustType">学科：<select id="adjustType">
                        <option value="ChineseSun">语文</option>
                        <option value="EnglishSun">英语</option>
                        <option value="MathSun">数学</option>
                        <option value="OtherSun">其他</option>
                    </select></label>

            <label for="contractDays">增加阳光数：<input id="sunNum" type="number" min="0"/></label>
            <#--<label for="newExpireDate" id="dateLabel">过期时间：-->
                <#--<input id="newExpireDate" type="text" class="input-large" placeholder="过期日期" name="newExpireDate"-->
                       <#--value="">-->
            <#--</label>-->
        </div>
        <div class="modal-footer">
            <button id="grant_sun_dialog_confirm_btn" class="btn btn-primary">确定</button>
            <button class="btn btn-primary" data-dismiss="modal">取消</button>
        </div>
    </div>
</span>
<script>
    $(function () {

        $("#grant_sun_dialog_confirm_btn").click(function () {
            var studentId = $('#studentId').val();
            var sunType = $('#adjustType').val();
            var sunNum = $('#sunNum').val();
            $.post('/equator/newwonderland/sapling/acquiresubjectsuns.vpage', {
                'studentId': studentId,
                'sunType': sunType,
                'sunNum': sunNum
            }, function (data) {
                if (data.success) {
                    location.reload();
                } else {
                    alert(data.info);
                }
            });
        })

    });

    function updateSun() {
        $('#grant_sun_dialog').modal("show");
    }

    function letterAnswer() {
        $('#grant_letter_dialog').modal("show");
    }
</script>
</@layout_default.page>