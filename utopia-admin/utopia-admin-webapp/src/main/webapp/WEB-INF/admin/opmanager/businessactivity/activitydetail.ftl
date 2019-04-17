<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑通用导流活动" page_num=9>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑通用导流活动
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel"
           class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_btn" class="btn btn-primary" value="保存商品"/>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" name="detail_form" action="save.vpage" method="post">
                    <input id="aid" name="aid" value="${aid!''}" type="hidden">
                    <div class="form-horizontal">
                        <#if (aid?? && activity??)>
                            <div class="control-group">
                                <label class="col-sm-2 control-label">生成后的链接</label>
                                <div class="controls">
                                    <input type="text" id="activityUrl" name="activityUrl" value="${(activity.activityUrl)!}" class="form-control" style="width: 336px"/>
                                </div>
                            </div>
                        </#if>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">原商品链接</label>
                            <div class="controls">
                                <input type="text" id="innerUrl" name="innerUrl" value="${(activity.innerUrl)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">商品类型</label>
                            <div class="controls">
                                <#list types as activityType>
                                    <label class="type-radio"
                                           style="position:relative;top:4px;margin-right:10px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                        <input type="radio" <#if activity??><#if activityType==activity.activityType!>checked="checked"</#if><#else><#if activityType=="Pay">checked="checked"</#if></#if>
                                               name="activityType" value="${activityType}"
                                               style="position: relative;top:-3px;"/>
                                        <#switch activityType>
                                            <#case "Pay">支付<#break>
                                            <#case "Reserve">报名<#break>
                                            <#case "joinGroup">加群<#break>
                                            <#case "Subscribe">预约<#break>
                                            <#default>
                                        </#switch>
                                    </label>
                                </#list>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">商品名称</label>
                            <div class="controls">
                                <input type="text" id="productName" name="productName" value="${(activity.productName)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">课程单价</label>
                            <div class="controls">
                                <input type="text" id="productPrice" name="productPrice" value="${(activity.productPrice)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">页面标题</label>
                            <div class="controls">
                                <input type="text" id="title" name="title" class="form-control" value="${(activity.title)!}" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">群号</label>
                            <div class="controls">
                                <input type="text" id="qcode" name="qcode" class="form-control" value="${(activity.qcode)!}" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">群名称</label>
                            <div class="controls">
                                <input type="text" id="qname" name="qname" class="form-control" value="${(activity.qname)!}" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">页面底部bar文案</label>
                            <div class="controls">
                                <input type="text" id="barContent" name="barContent" value="${(activity.barContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">成功后跳转</label>
                            <div class="controls">
                                <input type="text" id="returnUrl" name="returnUrl" value="${(activity.returnUrl)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">成功后提示文案</label>
                            <div class="controls">
                                <input type="text" id="returnContent" name="returnContent" value="${(activity.returnContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">支付/报名成功底部文案</label>
                            <div class="controls">
                                <input type="text" id="successContent" name="successContent" value="${(activity.successContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">支付/报名成功底部按钮文案</label>
                            <div class="controls">
                                <input type="text" id="successBarContent" name="successBarContent" value="${(activity.successBarContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">活动下线提示文案</label>
                            <div class="controls">
                                <input type="text" id="disabledContent" name="disabledContent" value="${(activity.disabledContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">状态</label>
                            <div class="controls">
                                <select name="status">
                                    <#list status as s>
                                        <option value="${s}" <#if s==(activity.status)!>selected</#if>>
                                            <#switch s>
                                                <#case "Online">上线<#break>
                                                <#case "Offline">下线<#break>
                                                <#default>
                                            </#switch>
                                        </option>
                                    </#list>
                                </select>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否支持备注</label>
                            <div class="controls">
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" name="remarked" <#if (activity.remarked)!false>checked="checked"</#if> value="true" style="position: relative;top:-3px;"/> 是
                                </label>
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" <#if activity??><#if !activity.remarked>checked="checked"</#if><#else>checked="checked"</#if> value="false" name="remarked" style="position: relative;top:-3px;"/> 否
                                </label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注提示文案</label>
                            <div class="controls">
                                <input type="text" id="remarkContent" name="remarkContent" value="${(activity.remarkContent)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="well">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">支付成功后是否发消息</label>
                            <div class="controls">
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" name="sendMsg" <#if (activity.sendMsg)!false>checked="checked"</#if> value="true" style="position: relative;top:-3px;"/> 是
                                </label>
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" <#if activity??><#if !activity.sendMsg>checked="checked"</#if><#else>checked="checked"</#if> value="false" name="sendMsg" style="position: relative;top:-3px;"/> 否
                                </label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">发Push消息时间</label>
                            <div class="controls">
                                <input type="text" id="sendSubscribeDate" name="sendSubscribeDate" <#if activity?? && activity.sendSubscribeDate??> value="${activity.sendSubscribeDate?string('yyyy-MM-dd HH:mm:ss')}"</#if>/>
                                <span style="color: red;font-size: 10px;">目前只对预约类活动有效</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">消息标题</label>
                            <div class="controls">
                                <input type="text" id="msgTitle" name="msgTitle" value="${(activity.msgTitle)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">消息内容</label>
                            <div class="controls">
                                <textarea id="msgContent" name="msgContent" class="form-control">${(activity.msgContent)!}</textarea>
                            </div>
                        </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否需要登录态</label>
                            <div class="controls">
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" name="needLogin" <#if activity??><#if activity.needLogin>checked="checked"</#if><#else>checked="checked"</#if> value="true" style="position: relative;top:-3px;"/> 是
                                </label>
                                <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;">
                                    <input type="radio" name="needLogin"<#if !((activity.needLogin)!true)>checked="checked"</#if> value="false" style="position: relative;top:-3px;"/> 否
                                </label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">最大预约/支付数量（默认为0不限制）</label>
                            <div class="controls">
                                <input type="text" id="limit" name="limit" value="${(activity.limit)!}" class="form-control" style="width: 336px"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">短信内容</label>
                            <div class="controls">
                                <textarea id="smsContent" name="smsContent" class="form-control">${(activity.smsContent)!}</textarea>
                                <span style="color: red;font-size: 10px;">短信内容不为空则视为发短信</span>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">微信模板消息</label>
                            <div class="controls">
                                <textarea id="wechatContent" name="wechatContent" class="form-control">${(activity.wechatContent)!}</textarea>
                                <span style="color: red;font-size: 10px;">微信模板消息不为空则视为发送微信模板消息</span>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $("#sendSubscribeDate").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss'
        });

        $('#frm').on('submit', function () {
            $('#frm').ajaxSubmit({
                type: 'post',
                url: 'save.vpage',
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                        window.location.href = 'activitydetail.vpage?aid=' + data.id;
                    } else {
                        alert(data.info);
                    }
                },
                error: function () {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_btn').on('click', function () {
            var detail = {
                aid: $('#aid').val(),
                innerUrl: $('#innerUrl').val().trim(),
                productName: $('#productName').val().trim(),
                productPrice: $('#productPrice').val().trim(),
                title: $('#title').val().trim(),
                barContent: $('#barContent').val().trim(),
                returnUrl: $('#returnUrl').val().trim(),
                returnContent: $('#returnContent').val().trim(),
                qcode: $('#qcode').val().trim(),
                qname: $('#qname').val().trim()
            };
            if (validateInput(detail)) {
                if (confirm("是否确认保存？")) {
                    $('#frm').submit();
                }
            }
        });
        function validateInput(detail) {
            var msg = "";
            if (detail.innerUrl == '') {
                msg += "请输入商品链接！\n";
            }
            if (detail.productName == '') {
                msg += "请输入商品名称！\n";
            }
            if (detail.productPrice == '') {
                msg += "请输入课程单价！\n";
            }
            if (detail.title == '') {
                msg += "请输入页面标题！\n";
            }
            if (detail.qcode == '') {
                msg += "请输入群号！\n";
            }
            if (detail.qname == '') {
                msg += "请输入群名称！\n";
            }
            if (detail.barContent == '') {
                msg += "页面底部bar文案不可为空！\n";
            }
            if (detail.returnContent == '') {
                msg += "请输入成功后提示文案！\n";
            }
            if (msg.length > 0) {
                alert(msg);
                return false;
            }
            return true;
        }
    });
</script>
</@layout_default.page>