<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='合同管理' page_num=15>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>合同管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addOral" data-type="add" class="btn btn-success changeManage" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
            </div>
            <div class="pull-right">
                <a class="btn btn-success reportBtn" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    导出回款明细
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">合同编号</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="id" name="id" value="">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">学校</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="school" name="school" value="">
                                <input type="hidden" id="schoolId" name="schoolId">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">签约人</label>
                        <div class="controls">
                            <label class="control-label">
                                <input type="text" id="contractor" name="contractor" value="">
                                <input type="hidden" id="contractorId" name="contractorId" value="">
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">合同类型</label>
                        <div class="controls">
                            <label class="control-label">
                                <select id="contractType" name="contractType">
                                    <option value="">请选择</option>
                                    <option value="PAY_EXAM">付费</option>
                                    <option value="LARGE_EXAM">大考</option>
                                </select>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">开始日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="beginDate" name="beginDate">
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError3">结束日期</label>
                        <div class="controls">
                            <input type="text" class="input-medium" id="endDate" name="endDate">
                        </div>
                    </div>
                    <div class="control-group">
                        <button id="queryOralBtn" type="button" class="btn btn-primary">查询</button>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 60px;">合同编号</th>
                            <th class="sorting" style="width: 60px;">学校ID</th>
                            <th class="sorting" style="width: 160px;">学校名称</th>
                            <th class="sorting" style="width: 60px;">合同类型</th>
                            <th class="sorting" style="width: 60px;">金额</th>
                            <th class="sorting" style="width: 100px;">签约人</th>
                            <th class="sorting" style="width: 100px;">签约日期</th>
                            <th class="sorting" style="width: 40px;">操作</th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var schoolId = 0;
    $(function(){

        $('#school,#contractor').val('');//解决返回页面 查询条件在但查询不了结果问题

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
        $('#contractor').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("search_user.vpage",{userKey: request.term},function(result){
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
                $('#contractorId').val(ui.item.id) ;
                $('#contractor').val(ui.item.realName) ;
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
            if($('#contractor').val() == ''){
                $('#contractorId').val('');
            }
            if($('#school').val() == ''){
                $('#schoolId').val('');
            }

            var dataObj = {
                id:$('#id').val(),
                schoolId:$('#schoolId').val(),
                contractorId:$('#contractorId').val(),
                contractType:$('#contractType').val(),
                beginDate:$('#beginDate').val(),
                endDate:$('#endDate').val()
            };
            $.get("search_contract.vpage",dataObj,function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.dataList.length;i++){
                        res.dataList[i].contractDate = new Date(res.dataList[i].contractDate).Format("yyyy-MM-dd");
                        var item = res.dataList[i];
                        if(item.contractType == 'PAY_EXAM'){
                            item.contractType = '付费';
                        }else if(item.contractType == 'LARGE_EXAM'){
                            item.contractType = '大考';
                        }
                        var operator = '<span class="contract_detail btn btn-primary" data-id="' + item.id + '">详情</span>'
                                +'<span class="deleteManage btn btn-primary" data-schoolname="'+item.schoolName+'" data-schoolid="'+item.schoolId+'" data-id="'+ item.id+'">删除</span>';
                        var arr = [item.contractNumber, item.schoolId, item.schoolName, item.contractType, item.contractAmount, item.contractorName, item.contractDate, operator];
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
        //跳转详情页
        $(document).on('click','.contract_detail',function () {
            window.location.href = 'detail.vpage?id='+$(this).data('id');
        });
        $(document).on("click",'.deleteManage',function () {
            var _id = $(this).data("id");

            layer.confirm("是否确认删除"+$(this).data("schoolname") + '(' + $(this).data("schoolid") + ')的合同信息？', {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.get('delete_contract.vpage?contractId='+_id,function (res) {
                    if(res.success){
                        layer.alert('删除成功');
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
