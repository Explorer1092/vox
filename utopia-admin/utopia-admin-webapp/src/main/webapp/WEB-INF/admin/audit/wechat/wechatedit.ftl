<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=21>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend><strong>发送微信模板消息</strong></legend>
        </fieldset>
        <div id="error_div" class="alert alert-error" style="display: none;">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="error_msg"></strong>
        </div>
        <div class="row-fluid form-horizontal">
            <div>
                <ul class="inline">
                    <li>
                        <label><strong> 选择消息发送端：</strong>&nbsp;&nbsp;&nbsp;&nbsp;
                            <select id="wechatType" name="wechatType" class="input-xlarge">
                                <option value=-1>--请选择--</option>
                                <option value=0>微信家长通</option>
                                <option value=1>微信老师端</option>
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
                                    <input id="firstInfo" name="firstInfo" type="text" class="input-xlarge" placeholder="可空" />
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_keyword1">Keyword1：</strong></label>
                                <div class="controls">
                                    <input id="keyword1" name="keyword1" type="text" class="input-xlarge" placeholder="Keyword至少填一项" />
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong id="lb_keyword2">Keyword2：</strong></label>
                                <div class="controls">
                                    <input id="keyword2" name="keyword2" type="text" class="input-xlarge" placeholder="Keyword至少填一项" />
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>备注：</strong></label>
                                <div class="controls">
                                    <input id="remark" name="remark" type="text" class="input-xlarge" placeholder="可空"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="col-sm-2 control-label"><strong>跳转链接：</strong></label>
                                <div class="controls">
                                    <input id="url" name="url" type="text" class="input-xlarge" placeholder="http://url?userId=#userId#,可空"/>
                                </div>
                            </div>

                        </div>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <label><strong>直播广告专用：</strong><input name="isWkt" type="radio" value="isZbgg">&nbsp;&nbsp;<span style="color: red;">仅用于投放模版消息广告</span></label>
                    </li>
                    <br>
                    <li>
                        <label><strong>微课堂专用：</strong><input name="isWkt" type="radio" value="isWkt">&nbsp;&nbsp;<span style="color: red;">微课堂特殊处理，其他推送者可不选此项</span></label>
                    </li>
                </ul>
                <ul class="inline">
                    <ul class="nav nav-tabs">
                        <li class="active JS-menuSwitch" data-id="1" data-user-from="txt"><a href="javascript:void(0);">输入id</a></li>
                        <li class="JS-menuSwitch" data-id="2" data-user-from="excel"><a href="javascript:void(0);">上传id</a></li>
                    </ul>
                    <li id="mode-1">
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>发送时间：</strong></label>
                            <div class="controls">
                                <input id="sendTime" name="sendTime" type="text" class="input-xlarge" placeholder="可空" value="">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>请输入用户ID：</strong></label>
                            <div class="controls">
                                <textarea id="userIds" name="userIds" cols="150" rows="10" style="width: 40%" placeholder="请输入用户ID, 一行一条记录，最多30条" ></textarea>
                            </div>
                        </div>
                    </li>
                    <li id="mode-2" style="display:none;">
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>上传excel：</strong></label>
                            <div class="controls">
                                <input type="file" id="uploadExcel" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel">
                                <br/>
                                <span style="color:red">1.表格第一列为用户ID，第二列为发送时间
                                <br/>
                                2.单个文件行数请控制在5W以内
                            </span>
                            </div>
                        </div>
                    </li>
                </ul>
                <input type="hidden" name="sendType" value="1" id="sendType">
                <input type="hidden" name="fileUrl" value="" id="fileUrl">

                <ul class="inline">
                    <li>
                        <button id="send_msg"  class="btn btn-primary">提交审核</button>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 150%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在处理，请稍候……</p>
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
                $('#lb_keyword1').html("公告类型：");
                $('#lb_keyword2').html("公告内容：");
            } else if (type == 1) {
                $('#lb_keyword1').html("发件人：");
                $('#lb_keyword2').html("内容：");
            } else {
                $('#lb_keyword1').html("Keyword1：");
                $('#lb_keyword2').html("Keyword2：");
            }
        });

        $('#uploadExcel').change(function () {
            $("#loadingDiv").show();
            var $this = $(this);
            var sourceFile = $this.val();
            if (blankString(sourceFile)) {
                $("#loadingDiv").hide();
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt != "xls" && fileExt != "xlsx") {
                alert("请上传正确格式的excel！");
                $("#loadingDiv").hide();
                return;
            }
            if ($this.val() !== '') {
                var formData = new FormData();
                formData.append('file', $this[0].files[0]);
                $.ajax({
                    url: 'uploadexcel.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (res) {
                        $("#loadingDiv").hide();
                        if (res.success) {
//                            alert(res.fileUrl);
                            $('#fileUrl').val(res.fileUrl);
                            alert('上传成功');
                        } else {
                            alert(res.info);
                        }
                    }
                });
            }
        });

        $('#send_msg').on('click', function () {
            // show loading
            $("#loadingDiv").show();
            var data = {
                wechatType: $('#wechatType').val(),
                firstInfo: $('#firstInfo').val(),
                keyword1: $('#keyword1').val(),
                keyword2: $('#keyword2').val(),
                remark: $('#remark').val(),
                url: $('#url').val(),
                isUstalk: false,
                isWkt: $("input[name='isWkt']:checked").val()||"",
                sendType: $('#sendType').val()
            };

            if (data.sendType == 1) {
                data.userList = $('#userIds').val().trim();
                data.sendTime = $('#sendTime').val();
            } else if (data.sendType == 2) {
                data.fileUrl = $('#fileUrl').val();
            }

            console.info(data);
            $.post('createwechatmsg.vpage', data, function(res) {
                // hide loading
                $("#loadingDiv").hide();
                if (res.success) {
                    alert("提交成功，请等待审核");
                    window.location.href = "/audit/apply/list.vpage";
                } else {
                    $('#error_msg').html("出错啦！" + res.info);
                    $('#error_div').show();
                }
            });
        });
    });

    //switch
    $(document).on('click', '.JS-menuSwitch', function () {
        var $this = $(this);
        $("#sendType").val($this.data("id"));
        $this.addClass('active').siblings().removeClass('active');
        $('#mode-' + $this.attr('data-id')).show().siblings('li').hide();
    });

    function clearTextInfo() {
        $('#firstInfo').val('');
        $('#keyword1').val('');
        $('#keyword2').val('');
        $('#remark').val('');
        $('#url').val('');
        $('#sendTime').val('');
        $('#userIds').val('');
    }
</script>
</@layout_default.page>
