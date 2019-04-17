<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='维护老师' page_num=16>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>维护老师</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addOral" data-type="add" class="btn btn-success editBtn" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
            </div>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 40px;">创建时间</th>
                        <th class="sorting" style="width: 60px;">标题</th>
                        <th class="sorting" style="width: 20px;">老师数量</th>
                        <th class="sorting" style="width: 40px;">截止时间</th>
                        <th class="sorting" style="width: 180px;">任务说明</th>
                        <th class="sorting" style="width: 20px;">发布人</th>
                        <th class="sorting" style="width: 40px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
$(function () {
    //跳转添加页
    $(document).on("click",'.editBtn',function () {
        window.location.href = 'task_add_page.vpage';
    });
    //获取任务列表
    $.get('/taskmanage/maintainteacher/main_task_list.vpage',function (res) {
        if(res.success){
            var dataTableList = [];
            for(var i=0;i < res.dataList.length;i++){
                var item = res.dataList[i];
                var operator = '<span class="task_edit btn btn-primary" data-id="' + item.id + '">编辑</span>'
                        +'<span class="task_delete btn btn-warning"  data-id="'+ item.id+'" data-title="'+ item.title+'">删除</span>';
                var arr = [item.createTime,item.title, item.teacherNum, item.endTime, item.comment, item.publisherName, item.ifEnd ? '' : operator];
                dataTableList.push(arr);
            }
            var reloadDataTable = function () {
                var table = $('#datatable').DataTable({
                    aaSorting: [],
                    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
                    "sPaginationType": "bootstrap",
                    oLanguage: {
                        oPaginate: {
                            sFirst: "首页",
                            sLast: "末页",
                            sNext: "下一页",
                            sPrevious: "上一页"
                        },
                        sEmptyTable: "表中无数据存在！",
                        sInfo: "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
                        sInfoEmpty: "当前显示 0 到 0 条，共 0 条记录",
                        sInfoFiltered: "数据表中共为 _MAX_ 条记录",
                        sInfoPostFix: "",
                        sInfoThousands: ",",
                        sLengthMenu: "每页显示 _MENU_ 条记录",
                        sLoadingRecords: "正在加载...",
                        sProcessing: "正在加载中......",
                        sSearch: "搜索:",
                        sUrl: "",
                        sZeroRecords: "对不起，查询不到相关数据！"
                    },
                    "bDestroy": true
                });
                table.fnClearTable();
                table.fnAddData(dataTableList); //添加添加新数据

            };
            setTimeout(reloadDataTable(),0);
        }else{
            layer.alert(res.info)
        }
    });

    //跳转编辑页
    $(document).on("click",'.task_edit',function () {
        var id = $(this).data('id');
        window.location.href = 'task_edit_page.vpage?id='+id;
    });

    //删除操作
    $(document).on('click','.task_delete',function () {
        var _this = $(this);
        var _id = _this.data('id');
        var _title = _this.data('title');
        layer.confirm("是否确认删除【"+ _title +"】", {
            btn: ['确认','取消'] //按钮
        }, function(){
            $.get('/taskmanage/maintainteacher/delete_task.vpage',{id:_id},function (res) {
                if(res.success){
                    layer.alert('删除成功');
                    _this.parents('tr').remove();
                }else{
                    layer.alert(res.info);
                }
            })
        });
    });
});
</script>
</@layout_default.page>