<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='Push列表' page_num=18>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
    td{word-break: break-all}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header btn-group" style="padding: 5px;height: auto;">
            <button type="button" class="btn change_page <#if messageType == 1>btn-primary</#if> tab_btn" data-type="1" style="padding: 8px 30px;">Push列表</button>
            <button type="button" class="btn change_page <#if messageType == 2>btn-primary</#if> tab_btn" data-type="2" style="padding: 8px 30px;">系统消息列表</button>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">创建人</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="createUserName" name="createUserName" value="">
                                <input type="hidden" id="createUserId" name="createUserId" value="">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" class="input-medium" id="beginDate" name="beginDate">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" class="input-medium" id="endDate" name="endDate">
                            </label>
                        </div>
                    </div>
                    <#if messageType?has_content && messageType == 2>
                        <div class="control-group">
                            <label class="control-label">消息类型</label>
                            <div class="controls">
                                <label class="control-label">
                                    <select id="messageType" name="messageType">
                                        <option value="SYSTEM" selected>默认</option>
                                    </select>
                                </label>
                            </div>
                        </div>
                    </#if>
                    <div class="control-group">
                        <label class="control-label">状态</label>
                        <div class="controls">
                            <label class="control-label">
                                <select id="msgStatus" name="msgStatus">
                                    <option value="">请选择</option>
                                    <option value="0">草稿</option>
                                    <option value="1">已发送</option>
                                    <option value="-1">已删除</option>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <label class="control-label">
                                <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
                            </label>
                        </div>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <#if messageType?has_content && messageType == 1>
                        <tr>
                            <th class="sorting">ID</th>
                            <th class="sorting">PUSH内容</th>
                            <th class="sorting">发送日期</th>
                            <th class="sorting">创建人</th>
                            <th class="sorting">状态</th>
                            <th class="sorting">发送量</th>
                            <th class="sorting">打开量</th>
                            <th class="sorting">操作</th>
                        </tr>
                    <#elseif messageType?has_content && messageType == 2>
                        <tr>
                            <th class="sorting">ID</th>
                            <th class="sorting">消息类型</th>
                            <th class="sorting">消息标题</th>
                            <th class="sorting">消息内容</th>
                            <th class="sorting">发送日期</th>
                            <th class="sorting">创建人</th>
                            <th class="sorting">状态</th>
                            <th class="sorting">发送量</th>
                            <th class="sorting">打开量</th>
                            <th class="sorting">操作</th>
                        </tr>
                    </#if>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var schoolId = 0;
    var messageType = ${messageType!1};
    $(document).on("click",".change_page",function () {
        window.location.href = "message_list.vpage?messageType="+$(this).data("type")
    });
    $(function(){

        $('#school,#createUserName').val('');//解决返回页面 查询条件在但查询不了结果问题

        $('#school').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("search_school.vpage",{schoolKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            label: item.cmainName,
                            value: item.cmainName,
                            id: item.id
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                schoolId = ui.item.id;
                $('#school').val(ui.item.value);
                $('#schoolId').val(ui.item.id)
            }
        });
        $('#createUserName').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("/exam/contractmanage/search_user.vpage",{userKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            label: item.realName,
                            value: item.realName,
                            id: item.id
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                $('#createUserId').val(ui.item.id) ;
                $('#createUserName').val(ui.item.realName) ;
            }
        });
        $("#beginDate,#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            closeText       : "确定",
            currentText     : "本月",
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            onSelect : function (selectedDate){}
        });

        $("#queryOralBtn").on("click",function(){
            if($('#createUserName').val() == ''){
                $('#createUserId').val('');
            }
            if($('#school').val() == ''){
                $('#schoolId').val('');
            }

            var dataObj = {
                createUserId:$('#createUserId').val(),
                messageType:messageType,
                beginDate:$('#beginDate').val(),
                endDate:$('#endDate').val(),
                msgStatus:$('#msgStatus option:selected').val(),
                notifyType:'SYSTEM'
            };
            $.get("agent_message_list.vpage",dataObj,function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.messageList.length;i++){
                        if(res.messageList[i].sendDatetime !=null){
                            res.messageList[i].sendDatetime = new Date(res.messageList[i].sendDatetime).Format("yyyy-MM-dd hh:mm:ss");
                        }
                        var item = res.messageList[i];
                        if(item.msgStatus == 0){
                            item.msgStatus = '草稿';
                            var operator = '<span class="edit_btn btn btn-primary" data-id="' + item.id + '">编辑</span>'
                                            +'<span class="js-test btn btn-primary" data-id="' + item.id + '">测试</span>'
                                    +'<span class="js-push btn btn-primary" data-id="' + item.id + '">发送</span>'
                                    +'<span class="deleteManage btn btn-primary" data-id="'+ item.id+'">删除</span>';
                        }else if(item.msgStatus == 1){
                            item.msgStatus = '已发送';
                            var operator = '<span class="js-downLoad btn btn-primary" data-id="' + item.id + '">下载</span>';
                        }else if(item.msgStatus == -1){
                            item.msgStatus = '已删除';
                            var operator = '';
                        }
                        <#if messageType == 1>
                            var arr = [i, item.pushContent, item.sendDatetime, item.createUserName, item.msgStatus, item.sendNum, item.openNum, operator];
                        <#elseif messageType == 2>
                            var arr = [i, item.notifyType, item.notifyTitle, item.notifyContent, item.sendDatetime, item.createUserName, item.msgStatus, item.sendNum, item.openNum, operator];
                        </#if>
                        dataTableList.push(arr);

                    }
                    var reloadDataTable = function () {
                        var table = $('#datatable').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据

                    };
                    setTimeout(reloadDataTable(),0);
                }else{
                    layer.alert(res.info)
                }
            });

        });
        //跳转添加页
        $(document).on("click",'.changeManage',function () {
            window.location.href = 'changeManage.vpage';
        });
        //跳转编辑页
        $(document).on('click','.edit_btn',function () {
            window.location.href = 'edit_page.vpage?messageTypes='+messageType+'&id='+$(this).data('id');
        });
        $(document).on('click','.js-test',function () {
            var postData = {
              messageId:$(this).data('id'),
              messageType:messageType
            };
            layer.prompt({title: '请输入测试用户ID，每次仅限1个', formType: 2}, function(text, index){
                layer.close(index);
                postData.userIds = text;
                $.post("test_send_message.vpage",postData,function (res) {
                    if(res.success){
                        layer.tip('发送成功')
                    }else{
                        layer.tip(res.info)
                    }
                })
            });
        });
        $(document).on('click','.js-downLoad',function () {
            window.location.href = "message_info_export.vpage?id="+$(this).data('id')+"&messageType="+messageType;
            // $.get("message_info_export.vpage",postData,function (res) {
            //     if(res.success){
            //         layer.tip('下载成功')
            //     }else{
            //         layer.tip(res.info)
            //     }
            // })
        });

        $(document).on("click",'.deleteManage',function () {
            var _id = $(this).data("id");

            layer.confirm("是否确认删除", {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.post('delete_message.vpage?id='+_id +"&messageType=" + messageType,function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        $("#queryOralBtn").click();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });
        $(document).on("click",'.js-push',function () {
            var _id = $(this).data("id");

            layer.confirm("是否确认发布", {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.post('send_message.vpage?id='+_id +"&messageType=" + messageType,function (res) {
                    if(res.success){
                        layer.alert('发送成功');
                        $("#queryOralBtn").click();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });


        });

        $("#queryOralBtn").trigger("click");

        //导出回款明细
        var reportBtn = $('.reportBtn'),beginDate = $('#beginDate'),endDate = $('#endDate');
        reportBtn.on('click',function () {
            if(beginDate.val()==''){
                layer.alert('请选择开始日期');
                return;
            }
            window.location.href = 'contract_payback_export.vpage?beginDate='+beginDate.val()+'&endDate='+endDate.val();
        });
    });

</script>
</@layout_default.page>
