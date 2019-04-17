<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="JPush管理平台" page_num=9>
    <#if error?? && error?has_content>
    <h1>${error}</h1>
    <#else>
    <script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
    <link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
    <div id="main_container" class="span9">
        <legend class="legend_title">
            <strong>课外乐园消息</strong>
        </legend>
        <div class="row-fluid">
            <div class="span12">
                <div class="well">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">选择消息类型</label>
                            <div class="controls">
                                <select id="messageType" name="messageType" style="margin-bottom:0">
                                    <option value="OPEN_APP_MSG">
                                        打开应用
                                    </option>
                                    <option value="COMMON_MSG">
                                        普通消息
                                    </option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">消息过期截止时间</label>
                            <div class="controls">
                                <input id="expiredTime" name="expiredTime" style="width: 150px;" data-role="date"
                                       data-inline="true" type="text" placeholder="2016-01-01 00:00:00"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">消息标题</label>
                            <div class="controls">
                            <textarea name="title" cols="35" rows="3"
                                      placeholder="请在这里输入消息标题，长度不超过16个字"></textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">详细描述</label>
                            <div class="controls">
                            <textarea id="content" name="content" cols="35" rows="3"
                                      placeholder="请在这里输入消息详细内容"></textarea>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="col-sm-2 control-label">打开应用</label>
                            <div class="controls">
                                <select id="appKey" name="appKey">
                                    <#if onlineFairylandProducts ?? >
                                        <#list onlineFairylandProducts as fairylandProduct >
                                            <option value="${fairylandProduct.appKey?default("")}">
                                            ${fairylandProduct.productName?default("")}
                                            </option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div id="stuexttype" style="display: ''">
                            <div class="control-group">
                                <label class="col-sm-2 control-label">标签提醒</label>
                                <div class="controls">
                                    <select id="popupTitle" name="popupTitle">
                                        <option value="">不提醒</option>
                                        <option value="HOT">热门</option>
                                        <option value="NEW">新消息</option>
                                        <option value="RECOMMEND">推荐</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="control-group" style="display: none">
                            <label class="col-sm-2 control-label">内容地址</label>
                            <div class="controls">
                            <textarea id="linkUrl" name="linkUrl" cols="35" rows="3"
                                      placeholder="请在这里输入消息的链接跳转地址"></textarea>
                            </div>
                        </div>

                        <div class="control-group" style="display: none">
                            <label class="col-sm-2 control-label">地址类型</label>
                            <div class="controls">
                                <select id="linkType" name="linkType">
                                    <option value="0">站外绝对地址</option>
                                    <option value="1">站内相对地址</option>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否阅后即焚</label>
                            <div class="controls">
                                <select id="readType" name="readType">
                                    <option value="SNAPCHAT">阅后即焚</option>
                                    <option value="CHRONIC">有效期内长期存在</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="form-horizontal">
            <legend class="legend_title">
                <strong>选择发送方式（全部用户只投放非黑名单用户）</strong>
            </legend>
            <div style="height: 500px;">
                <div>
                    <table class="table table-stripped" style="width: 800px;">
                        <thead>
                        <tr>
                            <th>
                                <label><input type="radio" name="pushType" value='AllUser'/>&nbsp;&nbsp;投放所有用户</label>
                            </th>
                            <th>
                                <label><input type="radio" name="pushType" value='SpecifyUserId' checked="checked"/>&nbsp;&nbsp;投放指定用户</label>
                            </th>
                        </tr>
                        </thead>
                    </table>
                    <br>
                    <div>
                        <div id="target_user_modal" class="span7" style="display: none;">
                            <label class="col-sm-2 control-label">指定用户发送</label>
                            <div class="controls">
                                <input id="userId" name="userId"/>
                            </div>
                        </div>
                    </div>
                    <div>
                        <div id="target_user_modal" class="span7">
                            <div style="float: right;">
                                <br><br>&nbsp;&nbsp;
                                <button type="button" class="btn btn-success save_btn">确 认 发 送</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        var regionModel = $('#target_region_modal');
        var labelModel = $('#target_label_modal');
        var userModel = $('#target_user_modal');
        var schoolModel = $('#target_school_modal');

        regionModel.hide();
        labelModel.hide();
        userModel.show();
        schoolModel.hide();

        $(function () {

            $('#expiredTime').datetimepicker({
                format: 'yyyy-mm-dd hh:ii:ss',  //日期格式，自己设置
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

            $("#messageType").change(function () {
                if ($(this).val() == "COMMON_MSG") {
                    $("#appKey").parent().parent().attr("style", "display:none");
                    $("#linkUrl").parent().parent().attr("style", "display:''");
                    $("#linkType").parent().parent().attr("style", "display:''");
                } else if ($(this).val() == "OPEN_APP_MSG") {
                    $("#appKey").parent().parent().attr("style", "display:''");
                    $("#linkUrl").parent().parent().attr("style", "display:none");
                    $("#linkType").parent().parent().attr("style", "display:none");
                }
            });
            $("input[name='pushType']").on('change', function () {
                var pushType = $(this).val();
                if (pushType == 'AllUser') {
                    regionModel.hide();
                    labelModel.hide();
                    userModel.hide();
                    schoolModel.hide();
                } else if (pushType == 'SpecifyUserId') {
                    regionModel.hide();
                    labelModel.hide();
                    userModel.show();
                    schoolModel.hide();
                }
            });

            $(".save_btn").on("click", function () {
                var link = $("#linkUrl");

                if ($("#messageType").val() == "COMMON_MSG" && link.val() =='' &&　$("#readType").val() == 'SNAPCHAT') {
                    alert("发送失败，阅后即焚只针对配置有url的消息");
                    return;
                }

                if ($("#messageType").val() == "COMMON_MSG" && !/^\s*https/.test(link.val())) {
                    if (!confirm("当前地址不是以https开头的安全链接，确认发送吗？")) {
                        return;
                    }
                }

                var data = {
                    messageType: $("#messageType").val(),
                    title: $('[name="title"]').val(),
                    appKey: $('[name="appKey"]').val(),
                    popupTitle: $("#popupTitle").val(),
                    expiredTime: $("#expiredTime").val(),
                    linkUrl: $("#linkUrl").val(),
                    linkType: $("#linkType").val(),
                    pushType: $('[name="pushType"]:checked').val(),
                    content: $("#content").val(),
                    userId: $("#userId").val(),
                    readType:$("#readType").val()
                };
                $.post('fairyland/sendmessage.vpage', data, function (res) {
                    if (res.success) {
                        alert("已发布成功！");
                        window.location.reload();
                    } else {
                        alert("发布失败！" + res.info);
                    }
                });
            });
        });
    </script>
    </#if>
</@layout_default.page>