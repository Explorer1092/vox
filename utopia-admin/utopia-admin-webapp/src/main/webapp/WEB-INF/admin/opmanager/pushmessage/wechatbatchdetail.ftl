<#import "../../layout_default.ftl" as layout_default />
<#import "../../site/batch/head.ftl" as h/>
<@layout_default.page page_title="新建微信任务" page_num=9>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>
                <strong>发送微信模板消息</strong>&nbsp;&nbsp;
                <input type="button" id="return" onclick="window.location.href='wechatbatchindex.vpage'"
                       class="btn btn-primary" value="返  回"/>
            </legend>
        </fieldset>
        <div id="error_div" class="alert alert-error" style="display: none;">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error_msg"></strong>
        </div>
        <div class="row-fluid form-horizontal">
            <form id="frm" method="post">
                <ul class="inline">
                    <li>
                        <label><strong> 选择消息发送端：</strong>&nbsp;&nbsp;&nbsp;&nbsp;
                            <select id="wechatType" name="wechatType" class="input-xlarge">
                                <option value=-1>--请选择--</option>
                                <option value=0 <#if wechatType?? && wechatType == 1> selected </#if>>微信家长通</option>
                                <option value=1 <#if wechatType?? && wechatType == 2> selected </#if>>微信老师端</option>
                            </select>
                            <span style="color: red;">*必选</span>
                        </label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>请填写消息模板：</strong></label>
                        <div class="well" style="float: left;">
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>消息主题：</strong></label>
                                <div class="controls">
                                    <input id="first" name="first" type="text" class="input-xlarge" placeholder="可空"
                                           value="<#if first??>${first!''}</#if>">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_k1">Keyword1：</strong></label>
                                <div class="controls">
                                    <input id="k1" name="k1" type="text" class="input-xlarge" placeholder="Keyword至少填一项"
                                           value="<#if k1??>${k1!''}</#if>">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_k2">Keyword2：</strong></label>
                                <div class="controls">
                                    <input id="k2" name="k2" type="text" class="input-xlarge" placeholder="Keyword至少填一项"
                                           value="<#if k2??>${k2!''}</#if>">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>备注：</strong></label>
                                <div class="controls">
                                    <input id="remark" name="remark" type="text" class="input-xlarge" placeholder="可空"
                                           value="<#if remark??>${remark!''}</#if>">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>跳转链接：</strong></label>
                                <div class="controls">
                                    <input id="url" name="url" type="text" class="input-xlarge"
                                           placeholder="http://url?userId=#userId#,可空"
                                           value="<#if url??>${url!''}</#if>">
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>发送时间：</strong></label>
                                <div class="controls">
                                    <input id="sendTime" name="sendTime" type="text" class="input-xlarge"
                                           placeholder="可空" value="<#if sendtime??>${sendtime!''}</#if>">
                                </div>
                            </div>
                        </div>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>是否ustalk专用：</strong><input id="isUstalk" name="isUstalk" type="checkbox"
                                                                  value="ustalk">&nbsp;&nbsp;<span style="color: red;">ustalk特殊处理，其他推送者可不选此项</span></label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>请输入用户ID：</strong>&nbsp;&nbsp;&nbsp;&nbsp;
                            <textarea id="userId" name="userId" cols="150" rows="10" style="width: 40%"
                                      placeholder="请输入用户ID, 一行一条记录，用户数量尽量控制在1000以内"><#if userId??>${userId!''}</#if></textarea>
                        </label>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <button id="send_msg" class="btn btn-primary">发送模板消息</button>
                    </li>
                </ul>
            </form>
        </div>
    </div>
</div>
<script>
    $(function () {
        $('#sendTime').datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $('#wechatType').on('change', function () {
            clearTextInfo();
            var type = $(this).val();
            if (type == 0) {
                $('#lb_k1').html("公告类型：");
                $('#lb_k2').html("公告内容：");
            } else if (type == 1) {
                $('#lb_k1').html("发件人：");
                $('#lb_k2').html("内容：");
            } else if (type == -1) {
                $('#lb_k1').html("Keyword1：");
                $('#lb_k2').html("Keyword2：");
            }
        });

        $('#frm').on('submit', function () {
            $('#frm').ajaxSubmit({
                url: '/crm/workflow/createwechatmsg.vpage',
                type: 'post',
                success: function (data) {
                    if (data.success) {
                        alert("发送成功！");
                        clearTextInfo();
                    } else {
                        $('#error_msg').html("出错啦！" + data.info);
                        $('#error_div').show();
                    }
                }
            });
            return false;
        });
    });

    function clearTextInfo() {
        $('#first').val('');
        $('#k1').val('');
        $('#k2').val('');
        $('#remark').val('');
        $('#url').val('');
        $('#sendTime').val('');
        $('#userId').val('');
    }
</script>

</@layout_default.page>