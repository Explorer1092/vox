<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='产品推广管理' page_num=3>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<div id="export-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <legend>导出短信</legend>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    开始日期： <input id="startDay" class="input-small" value="${beginDay!''}" type="text" readonly>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    结束日期： <input id="endDay" class="input-small" value="${endDay!''}" type="text" readonly>
                </li>
            </ul>
            <div class="modal-footer">
                <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
                <button id="export-btn" class="btn btn-success">导出</button>
            </div>
        </dl>
    </div>
</div>


<div id="create-dialog" class="modal fade hide" style="width: 40%; left: 40%;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新建短信</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>类型</dt>
                    <dd>
                        <select name="bizType" id="bizType" class="multiple">
                            <option value="JZT">家长通推荐</option>
                            <option value="LIVECAST">直播课推荐</option>
                            <option value="IMPERIAL_PALACE">故宫24节气课程</option>
                            <option value="POINT_READER">点读机推荐</option>
                            <option value="NOTICE">通知类信息</option>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>手机号码</dt>
                    <dd>
                        <input type="number" id="phone" name="phone"/>
                    </dd>
                </li>
            </ul>
            <ul class="inline" id="need-not-to-know">
                <li>
                    <dt>用户类型</dt>
                    <dd>
                        <input type="hidden" id="userType" name="userType" value="PARENT"/>
                        <input id="card_one" type="radio" checked="checked" value="PARENT" >家长&nbsp;&nbsp;&nbsp;
                        <input id="card_two" type="radio" value="TEACHER" >教师
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>短信内容</dt>
                    <dd>
                    <dd><textarea id="smsContent"  cols="35" rows="5"></textarea></dd>

                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        <button id="saveSms" class="btn btn-primary">保存</button>
        <button id="sendSms" class="btn btn-primary">发送</button>
    </div>

</div>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">产品推广管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="data-query" class="form-horizontal" method="get" action="${requestContext.webAppContextPath}/crm/product_promotion/sms_promotion_list.vpage" >
                    <ul class="inline">
                        <input type="hidden" id="page" name="page" value="0">
                    </ul>
                </form>
                <ul class="inline">
                    <input type="hidden" id="page" name="page" value="0">
                    <br><br>
                    <li>
                        <button type="submit" id="createSms" class="btn btn-primary">新建短信</button>
                    </li>
                    <li>
                        <button type="submit" id="exportSms" class="btn btn-primary">导出短信</button>
                    </li>
                </ul>
                <div id="data_table_journal">
                    <table class="table table-bordered table-striped">
                        <tr>
                            <td width="60px">序号</td>
                            <td width="150px">发送时间</td>
                            <td width="100px">类型</td>
                            <td width="100px">手机号码</td>
                            <td width="150px">用户类型</td>
                            <td width="80px">用户id</td>
                            <td width="150px">短信内容</td>
                            <td width="80px">发送人</td>
                            <td width="200px">操作</td>
                        </tr>
                        <#if dataList??>
                            <#list dataList as data >
                                <tr>
                                    <td>${data.id!}</td>
                                    <td>${data.sendTime!}</td>
                                    <td>${data.bizType!}</td>
                                    <td>${data.phone!}</td>
                                    <td>${data.targetUserType!}</td>
                                    <td>${data.targetUserId!'---'}</td>
                                    <td>${data.smsContent!}</td>
                                    <td>${data.operationUserName!}</td>
                                    <td>
                                        <#if data.status!='ALREADY_SEND'>
                                            <button class="btn btn-success" onclick="changeStatus('${data.id}')">发送</button>
                                        </#if>
                                        <#if data.status=='ALREADY_SEND'>
                                            发送成功
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
                <ul class="pager">
                    <#if page gt 0>
                        <li><a href="#" onclick="pagePost('${page-1}')" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${page + 1!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPages==0>1<#else>${totalPages!}</#if> 页</a></li>
                    <#if page + 1 lt totalPages>
                        <li><a href="#" onclick="pagePost('${page+1}')" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("#createSms").on("click", function () {
            $("#create-dialog").modal("show");
        });

        $("#card_one").click(function() {
            $("#card_two").attr("checked", false);
            $("#userType").val($("#card_one").val());
        });

        $("#card_two").click(function() {
            $("#card_one").attr("checked", false);
            $("#userType").val($("#card_two").val());
        });

        //保存
        $("#saveSms").on("click", function () {
            save("PENDING_SEND");
        });
        //发送
        $("#sendSms").on("click", function () {
            save("ALREADY_SEND");
        });

        $("#exportSms").on("click", function () {
            $("#export-dialog").modal("show");
        });

        $("#export-btn").on('click', function () {
            var startDay = $('#startDay').val();
            var endDay = $('#endDay').val();

            if (!startDay || startDay.trim() == '') {
                alert("请选择开始日期");
                return;
            }

            if (!endDay || endDay.trim() == '') {
                alert("请选择开始结束");
                return;
            }
            $("#export-dialog").modal("hide");
            location.href = "export.vpage?" +
                    "beginDay=" + startDay +
                    "&endDay=" + endDay;

        });

        $("#startDay").datetimepicker({
            language: "cn",
            format: "yyyy-mm-dd",
            autoclose: true,
            minView: 2,
            endDate: new Date()
        }).on("changeDate",function(e){
            var endTime = addDay(e.date,30);
            var startTime = addDay(e.date,-1);
            $("#endDay").datetimepicker("setStartDate",startTime);
            $("#endDay").datetimepicker("setEndDate",endTime);
        });

        $("#endDay").datetimepicker({
            language: "cn",
            format: "yyyy-mm-dd",
            autoclose: true,
            minView: 2
        }).on("changeDate",function(e){
            var startTime = addDay(e.date,-30);
            var endTime = addDay(e.date,-1);
            $("#startDay").datetimepicker("setStartDate",startTime);
            $("#startDay").datetimepicker("setEndDate",endTime);
        });

    });

    function save(status) {
        var data = {
            bizType: $("#bizType").val(),
            phone: $("#phone").val(),
            targetUserType: $("#userType").val(),
            smsContent: $("#smsContent").val(),
            status: status,//待发送
        };
        if (data.bizType == undefined || data.bizType.trim() == '') {
            alert("类型不能为空");
            return false;
        }
        if (data.phone == undefined || data.phone.trim() == '') {
            alert("手机号码不能为空");
            return false;
        }
        if (!checkTel(data.phone)) {
            alert("手机号码不正确！");
            return false;
        }
        if (data.targetUserType == undefined || data.targetUserType.trim() == '') {
            alert("用户类型不能为空");
            return false;
        }
        if (data.smsContent == undefined || data.smsContent.trim() == '') {
            alert("短信内容不能为空");
            return false;
        }
        if (data.smsContent.length > 120) {
            alert("短信字数不能大于120");
            return false;
        }

        $.ajax({
            type: "post",
            url: "create_sms.vpage",
            data: JSON.stringify(data),
            contentType: 'application/json',
            success: function (data) {
                $("#record_success").val(data.success);
                if (data.success) {
                    alert("操作成功！");
                    window.location.href='sms_promotion_list.vpage';
                } else {
                    alert(data.info);
                }
            }
        });
    }

    function checkTel(phone){
//        var isMob=/^1[3|4|5|7|8][0-9]{9}$/;
        var isMob=/^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$/
        if(isMob.test(phone)){
            return true;
        }
        else{
            return false;
        }
    }

    function pagePost(page) {
        $("#page").val(page);
        $("#data-query").submit();
    }
    function changeStatus(id) {
        if (confirm("是否确认发送短信")) {
            $.ajax({
                type: "post",
                url: "send_sms.vpage",
                data: {
                    id: id,
                },
                success: function (data) {
                    if (data.success) {
                        alert("短信发送成功！");
                        window.location.href='sms_promotion_list.vpage';
                    } else {
                        alert("短信发送失败！");
                    }
                }
            });
        }
    }
</script>
</@layout_default.page>