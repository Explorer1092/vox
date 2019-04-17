<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Push管理平台" page_num=21 jqueryVersion="1.7.1">
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .sender-selector{width: 40%;height: 600px;float:left;margin-right: 3px;}
    .push-target{width:80%; height: 100%;}
    .target-school{resize: none;overflow-y: auto;  overflow-x: hidden;}
    .target-user{resize: none;overflow-y: auto; overflow-x: hidden; margin-top: 20px;}
    .ext-key{width: 120px; text-align: center!important; font-weight: bold;}
    .ext-value > input[type=checkbox]{margin: 0}
    .title{padding:5px 0;width: 83%;margin-bottom:5px;font-weight: bold;border:1px solid #0f92a8;background:#27a9bf;border-radius:2px;text-align: center;color:#fff;}
</style>
<div id="main_container" class="span9">
    <legend class="legend_title">
        <strong>APP Push 消息详情</strong>
        <#if audit?? && audit>
            <div style="float:right;">
                <#if workflowRecord??>
                    <#if workflowRecord.status == 'lv1' ||  workflowRecord.status == 'lv2'>
                        <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="send" data-text="发送" class="btn btn-success operation">
                            <i class="icon-ok icon-white"></i> 发 送
                        </a>
                        <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="reject" data-text="驳回" class="btn btn-danger operation">
                            <i class="icon-remove icon-white"></i> 驳 回
                        </a>
                    </#if>
                    <#if workflowRecord.status == 'lv1'>
                        <a href="javascript:void(0);" data-id="${workflowRecord.id!''}" data-type="raiseup" data-text="转上级" class="btn btn-info operation">
                            <i class="icon-signal icon-white"></i> 转上级
                        </a>
                    </#if>
                </#if>
            </div>
        </#if>
    </legend>

    <div class="row-fluid"><div class="span12"><div class="well">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="col-sm-2 control-label">发送的App</label>
                <div class="controls" style="line-height: 30px;">
                    <#if pushMsg??>
                        <#switch (pushMsg.sendApp)>
                            <#case 'PARENT'> 小学家长APP <#break/>
                            <#case 'STUDENT'> 小学学生APP <#break/>
                            <#case 'PRIMARY_TEACHER'> 小学老师APP <#break/>
                            <#case 'JUNIOR_PARENT'> 中学家长APP <#break/>
                            <#case 'JUNIOR_STUDENT'> 中学学生APP <#break/>
                            <#case 'JUNIOR_TEACHER'> 中学老师APP <#break/>
                        </#switch>
                    </#if>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">消息类型</label>
                <div class="controls" style="line-height: 30px;">
                    <#if pushMsg?? && pushMsg.canSendPush()> [发送push]</#if>
                    <#if pushMsg?? && pushMsg.canSendMsg()> [发送系统消息]</#if>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">发送时间</label>
                <div class="controls" style="line-height: 30px;">
                    <#if pushMsg?? && pushMsg.sendTime??>${(pushMsg.sendTime)?string('yyyy-MM-dd HH:mm:ss')}
                    <#else> 未设置
                    </#if>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">置顶设置</label>
                <div class="controls" style="line-height: 30px;">
                    <#--<#if pushMsg?? && pushMsg.isTop>-->
                        <#--<input id="isTop" name="isTop" type="checkbox"/>&nbsp;&nbsp;[置顶]&nbsp;&nbsp;&nbsp;-->
                        <#--<span id="topEndTimeBox">置顶截止时间：<input id="topEndTime" name="topEndTime" style="width: 10em;" data-role="date" data-inline="true" type="text" placeholder=${(pushMsg.topEndTimeStr)!}/></span>-->
                    <#--<#else>-->
                        <#--不置顶-->
                    <#--</#if>-->

                    <input id="isTop" name="isTop" type="checkbox" <#if pushMsg?? && pushMsg.isTop>checked="true"</#if> /><span id="topState"><#if pushMsg?? && pushMsg.isTop>&nbsp;&nbsp;[置顶]<#else>&nbsp;&nbsp;[不置顶]</#if></span>
                    <span id="topEndTimeBox">&nbsp;&nbsp;置顶截止时间：<input id="topEndTime" name="topEndTime" style="width: 10em;" data-role="date" data-inline="true" type="text" placeholder="${(pushMsg.topEndTimeStr)!}" value="${(pushMsg.topEndTimeStr)!}"/></span>

                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">Jpush内容</label>
                <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.notifyContent)!'--'}
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">消息标题</label>
                <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.title)!'--'}
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">消息概要</label>
                <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.content)!'--'}
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">图片详情</label>
                <div class="controls" style="line-height: 30px;">
                    <#if pushMsg?? && pushMsg.fileName?has_content>
                        <img src="${prePath}${(pushMsg.fileName)}" style="height: 200px;">
                    <#else>
                        无
                    </#if>
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">内容地址</label>
                <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.link)!'--'}
                </div>
            </div>
            <div class="control-group">
                <label class="col-sm-2 control-label">推送时长</label>
                <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.durationTime)!'--'}
                </div>
            </div>
            <div id="sharetr" <#if pushMsg?? && pushMsg.userType?has_content && pushMsg.userType!='parent'>style="display: none"</#if> >
                <div class="control-group">
                    <label class="col-sm-2 control-label">是否可分享</label>
                    <div class="controls" style="line-height: 30px;">
                        <#if pushMsg?? && pushMsg.share>可以<#else>不可</#if>分享
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">分享文案
                    <div class="controls" style="line-height: 30px;">
                        ${(pushMsg.shareContent)!'--'}
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">分享地址</label>
                    <div class="controls" style="line-height: 30px;">
                        ${(pushMsg.shareUrl)!'--'}
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">消息类型</label>
                    <div class="controls" style="line-height: 30px;">
                    ${(pushMsg.messageTag)!'--'}
                    </div>
                </div>
            </div>
            <div id="stuexttype" <#if pushMsg?? && pushMsg.userType?has_content && pushMsg.userType!='student'>style="display: none"</#if>>
                <div class="control-group">
                    <label class="col-sm-2 control-label">消息扩展类型</label>
                    <div class="controls" style="line-height: 30px;">
                        <#if pushMsg.msgExtType == 2>小铃铛活动提醒
                        <#elseif pushMsg.msgExtType == 80>广告中心提醒
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div>
        <legend class="legend_title" style="margin-bottom:5px; margin-top: 5px;">
            <strong>扩展选项 </strong>
        </legend>
        <table class="table table-striped table-bordered">
            <#--<tr>
                <td class="ext-key">学 段</td>
                <td>
                    <#if pushMsg?? && pushMsg.ktwelve == "j"> 小学</#if>
                    <#if pushMsg?? && pushMsg.ktwelve == "m"> 初中</#if>
                    <#if pushMsg?? && pushMsg.ktwelve == "i"> 学前</#if>
                    <#if pushMsg?? && pushMsg.ktwelve == "s"> 高中</#if>
                </td>
            </tr>-->
            <tr>
                <td class="ext-key">年 级</td>
                <td>
                    <#if pushMsg??>
                        <#list pushMsg.parseClazzLevels() as item>
                            ${item.description}<#if item_has_next> / </#if>
                        </#list>
                    </#if>
                </td>
            </tr>
            <#if pushMsg?? && pushMsg.userType?has_content && pushMsg.userType == 'teacher'>
                <tr class="teacher-option">
                    <td class="ext-key">学 科</td>
                    <td>
                        <#if pushMsg??>
                            <#list pushMsg.parseSubjects() as item>
                                ${item.value}<#if item_has_next> / </#if>
                            </#list>
                        </#if>
                    </td>
                </tr>
                <tr class="teacher-option">
                    <td class="ext-key">认证状态</td>
                    <td>
                        <#if pushMsg??>
                            <#list pushMsg.parseAuthenticationStates() as item>
                                ${item.description}<#if item_has_next> / </#if>
                            </#list>
                        </#if>
                    </td>
                </tr>
            <#else>
                <tr class="other-option">
                    <td class="ext-key">黑名单配置</td>
                    <td>
                        <#if pushMsg?? && pushMsg.inPaymentBlackList()> [只发送付费黑名单用户] </#if>
                        <#if pushMsg?? && pushMsg.inNoneBlackList()> [不包含黑名单] </#if>
                    </td>
                </tr>
            </#if>
        </table>
    </div>

    <div class="form-horizontal" style="height: 700px;">
        <legend class="legend_title">
            <strong>发送方式</strong>&nbsp;&nbsp;
            <select id="ptSelector" style="background: none;font-weight: bold;" disabled>
                <option value="1" <#if pushMsg?? && pushMsg.pushType == 1> selected </#if>>&nbsp;&nbsp;自定义投放策略</option>
                <option value="2" <#if pushMsg?? && pushMsg.pushType == 2> selected </#if>>&nbsp;&nbsp;投放指定用户</option>
                <option value="3" <#if pushMsg?? && pushMsg.pushType == 3> selected </#if>>&nbsp;&nbsp;投放指定地区</option>
                <option value="4" <#if pushMsg?? && pushMsg.pushType == 4> selected </#if>>&nbsp;&nbsp;投放指定学校</option>
                <option value="4" <#if pushMsg?? && pushMsg.pushType == 5> selected </#if>>&nbsp;&nbsp;投放指定标签</option>
            </select>
        </legend>
        <div style="width: 100%;<#if pushMsg?? && pushMsg.pushType == 1>display: none;</#if>">
            <#if pushMsg?? && pushMsg.pushType == 2>
                <div class="sender-selector">
                    <div class="title">投放指定用户</div>
                    <div id="idTypeCheck" style="display: none;margin-top: 10px;">
                        <#if pushMsg?? && pushMsg.idType == 2>&nbsp;&nbsp;导入家长ID&nbsp;</#if>
                        <#if pushMsg?? && pushMsg.idType == 1>&nbsp;&nbsp;导入学生ID&nbsp;&nbsp;</#if>
                    </div>
                    <#if pushMsg.targetUser?has_content>
                        <textarea class="form-control push-target target-user" rows="20" readonly>${(pushMsg.targetUser)?join('\n')}</textarea>
                    </#if>
                    <#if pushMsg.fileUrl?has_content>
                        <div class="well" style="width: 75%">
                            <a href="${(pushMsg.fileUrl)!}"> 点击下载附件</a>
                        </div>
                    </#if>
                </div>
            </#if>

            <#if pushMsg?? && pushMsg.pushType == 3>
                <div class="sender-selector">
                    <div class="title">投放指定地区</div>
                    <textarea class="form-control push-target target-school" rows="20" readonly>${(targetRegion)?join('\n')}</textarea>
                </div>
            </#if>

            <#if pushMsg?? && pushMsg.pushType == 4>
                <div class="sender-selector">
                    <div class="title">投放指定学校</div>
                    <textarea class="form-control push-target target-school" rows="20" readonly>${(targetSchool)?join('\n')}</textarea>
                </div>
            </#if>
            <#if pushMsg?? && pushMsg.pushType == 5>
                <div class="sender-selector">
                    <div class="title">投放指定标签</div>
                    <textarea class="form-control push-target target-school" rows="20" readonly>${(targetTagNameGroups)?join('\n')}</textarea>
                </div>
            </#if>
        </div>
    </div>
    <#if histories?? && histories?has_content>
        <div class="control-group well" style="margin-top: 50px;">
            <label class="col-sm-2 control-label"><strong>处理意见区</strong></label>
            <div class="controls">
                <table class="table table-striped table-bordered" style="font-size: 14px;">
                    <thead>
                    <tr>
                        <th>处理日期</th>
                        <th>处理人</th>
                        <th>处理结果</th>
                        <th>处理意见</th>
                    </tr>
                    </thead>
                    <#list histories as history>
                        <tr>
                            <td>${history.updateDatetime?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${history.processorName!}(${history.processorAccount!})</td>
                            <td>${history.result!}</td>
                            <td>${history.processNotes!}</td>
                        </tr>
                    </#list>
                </table>
            </div>
        </div>
    </#if>
</div>
<#if jpushTag?? && jpushTag?has_content>
<div id="jpushTag" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h4>JpushTag</h4>
    </div>
    <div class="modal-body">
        <div>
           <textarea style="resize: none;width: 80%;" rows="5">${jpushTag!}</textarea>
        </div>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
</#if>
<script type="text/javascript">
    $(function () {
        // 置顶时间编辑
        var isTop = $("#isTop"), topEndTime = $("#topEndTime"), topEndTimeBox = $("#topEndTimeBox"), topState = $("#topState");
        updateTopEndTimeBoxState();

        isTop.on("change", function () {
            var $this = $(this);
            updateTopEndTimeBoxState();
            if (this.checked && !topEndTime.val()) {
                topEndTime.focus();
            }
        });

        function updateTopEndTimeBoxState () {
            if (isTop.is(':checked')) {
                topEndTimeBox.show();
                topState.html("&nbsp;&nbsp;[置顶]")
            } else {
                topEndTimeBox.hide();
                topState.html("&nbsp;&nbsp;[不置顶]")
            }
        }

        function checkTop() {
            if (isTop[0].checked && !topEndTime.val()) {
                $(document).scrollTop(0);
                alert("你选择了置顶，请选择置顶截止时间！");
                topEndTime.focus();
                return false;
            } else if (!isTop[0].checked && topEndTime.val()) {
                $(document).scrollTop(0);
                alert("您选择了置顶截止时间，如果需要置顶的话必须勾选置顶复选框，或者清空时间取消置顶！");
                 return false;
            } else if (topEndTime.val()) {
                if (topEndTime.val()) {
                    var date = new Date(topEndTime.val()), now = new Date();
                    if (date <= now) {
                        $(document).scrollTop(0);
                        alert("您选择的置顶截止时间是过去，请选择当前时间之后的某个时间！");
                        topEndTime.focus();
                        return false;
                    }
                }
            }
            return true;
        }

        $('#topEndTime').datetimepicker({
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false,
            onSelect: function (selectedDate) {
            }
        });

        $(".operation").on('click', function() {
            if ($(this).attr("disabled") === "disabled") {
                return false;
            }
            var ops = $('.operation');
            // 锁住按钮
            ops.attr("disabled", "disabled");

            // 修改了置顶设置，confirm提示一下
            <#if pushMsg?? && pushMsg.isTop>
                var isTopState = true;
            <#else>
                var isTopState = false;
            </#if>
            var initTopEndTime = "${(pushMsg.topEndTimeStr)!}";
            if (isTopState !== isTop.is(':checked') || initTopEndTime !== topEndTime.val()) {
                if (!confirm("您已更改置顶设置，是否确认" + $(this).data("text") + "该条消息？" )) {
                    ops.removeAttr("disabled");
                    return ;
                }
            } else {
                if (!confirm("是否确认" + $(this).data("text") + "该条消息？" )) {
                    ops.removeAttr("disabled");
                    return ;
                }
            }
            var workFlowRecordId = $(this).data("id");
            var operationType = $(this).data("type");
            $.post('checkapppushmsg.vpage',{
                "workFlowRecordId":workFlowRecordId,
                "operationType":operationType,
                "isTopState":  isTop.is(':checked'),
                "topEndTime":  isTop.is(':checked') ? topEndTime.val() : ''
            },function (data){
                if(data.success){
                    alert("操作成功");
                    ops.removeAttr("disabled");
                    window.location.href = '/audit/workflow/todo_list.vpage';
                }else{
                    alert(data.info);
                    ops.removeAttr("disabled");
                }
            });
        });
    });
</script>
</@layout_default.page>