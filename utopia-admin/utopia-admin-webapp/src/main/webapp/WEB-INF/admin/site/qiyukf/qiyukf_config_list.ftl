<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='七鱼客服配置' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/js/datatables/css/datatables.min.css" rel="stylesheet">
<link  href="${requestContext.webAppContextPath}/public/js/datatables/css/dataTables.bootstrap4.min.css" rel="stylesheet">
<link  href="${requestContext.webAppContextPath}/public/js/datatables/css/jquery.dataTables.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/datatables/datatables.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html">
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="config-query" class="form-horizontal" >
                    <ul class="inline">
                        <li>App Key&nbsp;
                            <select id="appKeys" name="appKeys" width="25px">
                                <option value=''>请选择</option>
                            </select>
                        </li>
                        <li>
                            <button type="button" id="filter" class="btn btn-primary" onclick="query()">查询</button>
                        </li>
                        <li>
                            <button type="button" class="btn btn-info" onclick="loadAddDialog()">新增</button>
                        </li>
                    </ul>
                </form>

            </div>
        </div>
    </div>
    <div>
        <table id="dataList" class="display" cellspacing="0" style="width:100%">
                <thead>
                    <tr>
                        <td>App Key</td>
                        <td>问题类型</td>
                        <td>名称</td>
                        <td>客服组</td>
                        <td>问题模板</td>
                        <td>机器人</td>
                        <td>操作</td>
                    </tr>
                </thead>
            </table>
    </div>
</div>


<div id="add_dialog" class="modal fade hide" style="width:600px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3 class="modal-title">七鱼客服配置</h3>
            </div>
            <div class="form-horizontal" style="height:auto;">
                <div class="modal-body" style="height: auto; overflow: visible;">
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>App Key</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="appKey" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>问题类型</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="questionType" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>名称</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="name" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>客服组</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="csGroupId" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>问题模板</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="qtype" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label" style="width: 140px;"><strong>机器人</strong></label>
                        <div class="controls" style="margin-left: 160px;">
                            <input type="text" id="robotId" style="width: 286px;"/>
                            <span style="color: red">*必填</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="btn_modal_submit" type="button" class="btn btn-primary">保存</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->
<input type="hidden" id="currentId" value=""/>

<script type="text/javascript">
    $(function () {

        //app key 选择列表
        $.get('/site/qiyukf/config/appKeys.vpage',
            {},
            function(result){
                if(result.success){
                    $.each(result.data,function(index, item){
                        $("#appKeys").append("<option value='" + item + "'>" + item + "</option>");
                    });
                }
            }
        );

        $("#btn_modal_submit").on("click",function(){
            var appKey = $("#appKey").val();
            var questionType = $("#questionType").val();
            var name = $("#name").val();
            var csGroupId = parseInt($("#csGroupId").val());
            var qtype = parseInt($("#qtype").val());
            var robotId = parseInt($("#robotId").val());
            if(appKey.trim() == ''){
                alert("请填写App Key!");
                return false;
            }
            if(questionType.trim() == ''){
                alert("请填写问题类型!");
                return false;
            }
            if(name.trim() == ''){
                alert("请填写名称!");
                return false;
            }
            if(isNaN(csGroupId)){
                alert("客服组必须为数字");
                return false;
            }
            if(isNaN(qtype)){
                alert("问题模板必须为数字");
                return false;
            }
            if(isNaN(robotId)){
                alert("机器人必须为数字");
                return false;
            }
            $.post('/site/qiyukf/config/save.vpage',{
                appKey:appKey,
                questionType:questionType,
                name:name,
                csGroupId:csGroupId,
                qtype:qtype,
                robotId:robotId
            },function(data){
                if(!data.success){
                    alert('操作失败!');
                }else{
                    alert('操作成功!');
                    $('#add_dialog').modal('hide');
                }
            });
        });
    });

$.extend( true, $.fn.dataTable.defaults, {
    "searching": false,
    "ordering": false,
    "paging":   false,
    "ordering": false,
    "info":     false
} );
var table;
function query(){
    var appKeys = $("#appKeys").val();
    if(appKeys.trim() == ''){
//        alert("请选择App Key!");
        return false;
    }
    table = $('#dataList').DataTable({
        ajax:'/site/qiyukf/config/list.vpage?appKey=' + appKeys,
        "bDestroy" : true,
        "columns": [
                    { "data": "appKey" },
                    { "data": "questionType" },
                    { "data": "name" },
                    { "data": "csGroupId" },
                    { "data": "qtype" },
                    { "data": "robotId" },
                    {
                        "data": null,
                        "defaultContent": "<button>删除</button>"
                    }
                  ]
    });

}
//新增页
function loadAddDialog(){
    $('#appKey').val('');
    $('#questionType').val('');
    $('#name').val('');
    $('#csGroupId').val('');
    $('#qtype').val('');
    $('#robotId').val('');
    $('#add_dialog').modal('show');
}

$('#dataList tbody').on( 'click', 'button', function () {
    alert(111)

})
</script>
</@layout_default.page>