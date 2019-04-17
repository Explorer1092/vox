<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=12>
<div id="main_container" class="span9">
    <legend>订单管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <form id="frm" class="form-horizontal" action="" >
                    用户ID：  <input name="userId" id="userId" type="text" value="${userId!}"/>
                    订单状态：<select id="status" name="status">
                                <option value="">全部</option>
                                <#if statusList?has_content>
                                    <#list statusList as s>
                                        <option value="${s.name()!}">${s.getDesc()!}</option>
                                    </#list>
                                </#if>
                            </select>
                    开始月份： <input id="startDate" class="input-medium" type="text" placeholder="2016-01-01">
                    结束月份： <input id="endDate" class="input-medium" type="text" placeholder="2016-01-01">
                    <br/><br/>
                    <a id="selectOrder"  role="button" class="btn btn-primary">查询</a>
                    <a id="deliverExport" href="downloaddeliverinfo.vpage" role="button" class="btn btn-inverse">导出发货单</a>
                    <a id="deleteOrder"  role="button" class="btn btn-warning">批量修改订单状态</a>
                </form>
        </div>
    </div>
</div>

<div>
    <fieldset>
        <div id="order_list_chip"></div>
    </fieldset>
</div>

<div id="delete_order_modal" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改订单状态</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd><textarea id="userIds" name="userIds" placeholder="请输入要修改的用户ID，从excel直接粘贴，一行一条"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>状态变更原因</dt>
                    <dd><textarea id="reason" name="reason" placeholder="请输入状态变更原因"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>变更状态为：</dt>
                    <dd>
                        <select id="updateStatus" name="updateStatus">
                            <option value="EXCEPTION">用户信息异常</option>
                            <option value="PREPARE">配货中</option>
                        </select>
                    </dd>
                </li>
            </ul>
        </dl>
        <div id="errorList"></div>
    </div>
    <div class="modal-footer">
        <button id="delete_order_button" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>


<script type="text/javascript">

    $(function() {
        $("#startDate,#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $('#deleteOrder').on('click', function () {
            $("#userIds").val("");
            $("#reason").val("");
            $('#delete_order_modal').modal();
        });

        $("#delete_order_button").on("click", function () {
            var reason = $("#reason").val().trim();
            if(reason.length <= 0){
                alert("请输入原因");
                return;
            }
            $.ajax({
                type: "post",
                url: "updateuserorder.vpage",
                data: {
                     userIds: $("#userIds").val(),
                      reason: reason,
                      status: $("#updateStatus").val()
                },
                success: function (data) {
                    if(data.success){
                        if(data.errorList.length > 0){
                            var _html = "<table class='table table-bordered table-striped table-hover'>";
                            for(var i = 0, errorList = data.errorList; i < errorList.length; i++){
                                _html += "<tr><td>"+ errorList[i].userId +"</td><td>" + errorList[i].info + "</td></tr>"
                            }
                            _html += "</table>";
                            $("#errorList").html(_html);
                        }else{
                            $("#errorList").html("<label style='color: red'>" + data.info + "</label>");
                        }

                    }else{
                        $("#errorList").html(data.info);
                    }
                }
            });
        });

        $('#selectOrder').on('click', function() {

            var startDate =  $("#startDate").val();
            var endDate = $("#endDate").val();

            if(startDate == "") {
                alert("起始时间不能为空!");
                return;
            }

            if(endDate == "") {
                alert("结束时间不能为空!");
                return;
            }

            $('#order_list_chip').load('getorderlist.vpage',
                    {   userId : $('#userId').val(),
                        status: $("#status").val(),
                        startDate: $("#startDate").val(),
                        endDate: $("#endDate").val()
                    }
            );
        });
    });
</script>
</@layout_default.page>