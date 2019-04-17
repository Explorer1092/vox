<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='活动管理' page_num=19>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>活动列表</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addActivity" data-type="add" class="addActivity btn btn-success" href="javascript:;">
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
                        <th class="sorting" style="width: 10px;">序号</th>
                        <th class="sorting" style="width: 160px;">活动名称</th>
                        <th class="sorting" style="width: 60px;">开始时间</th>
                        <th class="sorting" style="width: 60px;">结束时间</th>
                        <th class="sorting" style="width: 60px;">活动状态</th>
                        <th class="sorting" style="width: 60px;">上线状态</th>
                        <th class="sorting" style="width: 135px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
$(function () {
    var now = Date.now();

    $.get("/activity/manage/list.vpage",function (res) {
        if(res.success){
            var dataTableList = [];
            for(var i=0;i < res.dataList.length;i++){
                var item = res.dataList[i];
                var status = '';
                var onlineStatus = '';
                if(now > item.endDate) {
                    status = '已结束';
                }else if(now < item.startDate){
                    status = '未开始';
                }else {
                    status = '进行中';
                }
                if(item.isShow){
                    onlineStatus = '已上线';
                }else{
                    onlineStatus = '已下线';
                }
                item.startDate = new Date(item.startDate).Format("yyyy-MM-dd");
                item.endDate = new Date(item.endDate).Format("yyyy-MM-dd");

                var operator = '<span class="edit_btn btn btn-primary" data-id="' + item.id + '">修改</span>&nbsp;' +
                        '<span class="extend_btn btn btn-inverse" data-id="' + item.id + '">扩展项</span>&nbsp;' +
                        '<span class="quota_btn btn btn-success" data-id="' + item.id + '">指标配置</span>&nbsp;' +
                        '<span class="authority_btn btn btn-success" data-id="' + item.id + '">权限配置</span>&nbsp;' +
                        '<span class="delete_btn btn btn-danger" data-id="' + item.id + '">删除</span>&nbsp;';
                if(item.isShow){
                    operator += '<span class="offline_btn btn btn-danger" data-id="' + item.id + '">下线</span>&nbsp;';
                }else{
                    operator += '<span class="online_btn btn btn-danger" data-id="' + item.id + '">上线</span>';
                }
                var arr = [i+1,item.name, item.startDate, item.endDate, status, onlineStatus, operator];
                dataTableList.push(arr);

            }
            var reloadDataTable = function () {
                var table = $('#datatable').DataTable({
                    aaSorting: [[2, "desc"]],
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

    //跳转添加页
    $(document).on("click",'.addActivity',function () {
        window.location.href = 'add_page.vpage';
    });
    //跳转编辑页
    $(document).on('click','.edit_btn',function () {
        window.location.href = 'update_page.vpage?activityId='+$(this).data('id');
    });
    //跳转扩展项配置页
    $(document).on('click','.extend_btn',function () {
        window.location.href = 'update_extend_page.vpage?activityId='+$(this).data('id');
    });
    //跳转权限配置页
    $(document).on('click','.authority_btn',function () {
        window.location.href = '/authority/record/authority_page.vpage?recordType=2&recordId='+$(this).data('id');
    });
    //跳转指标配置页
    $(document).on('click','.quota_btn',function () {
        window.location.href = 'indicator_page.vpage?activityId='+$(this).data('id');
    });
    //删除
    $(document).on("click",'.delete_btn',function () {
        var _this = $(this);
        var id = _this.data('id');
        var index = layer.confirm('确认删除该活动？', {
            btn: ['取消','确定'] //按钮
        }, function(){
            layer.close(index);
        }, function(){
            $.post('delete.vpage',{
                activityId:id
            },function (res) {
                if(res.success){
                    layer.alert('删除成功');
                    _this.parent().parent().remove();
                }else{
                    layer.alert(res.info);
                }
            });
        });
    });

    //上线
    $(document).on("click",'.online_btn',function () {
        var _this = $(this);
        var id = _this.data('id');
        var index = layer.confirm('确认上线该活动？', {
            btn: ['取消','确定'] //按钮
        }, function(){
            layer.close(index);
        }, function(){
            $.post('on_off_line.vpage',{
                activityId:id,
                isShow:true
            },function (res) {
                if(res.success){
                    layer.alert('上线成功');
                    _this.parent('td').prev().text('已上线');
                    _this.parent().append('<span class="offline_btn btn btn-danger" data-id="' + id + '">下线</span>');
                    _this.remove();
                }else{
                    layer.alert(res.info);
                }
            });
        });
    });

    //下线
    $(document).on("click",'.offline_btn',function () {
        var _this = $(this);
        var id = _this.data('id');
        var index = layer.confirm('确认下线该活动？', {
            btn: ['取消','确定'] //按钮
        }, function(){
            layer.close(index);
        }, function(){
            $.post('on_off_line.vpage',{
                activityId:id,
                isShow:false
            },function (res) {
                if(res.success){
                    layer.alert('下线成功');
                    _this.parent('td').prev().text('已下线');
                    _this.parent().append('<span class="online_btn btn btn-danger" data-id="' + id + '">上线</span>');
                    _this.remove();
                }else{
                    layer.alert(res.info);
                }
            });
        });
    });
});
</script>
</@layout_default.page>
