<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='数据同步' page_num=page_num>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>数据同步</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager()>
            <div class="pull-right">
                <a id="addOral" data-type="add" class="btn btn-success addPublish" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    上传数据
                </a>
            </div>
            </#if>
        </div>
        <div class="box-content">
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 20px;">编号</th>
                        <th class="sorting" style="width: 60px;">标题</th>
                        <th class="sorting" style="width: 60px;">创建时间</th>
                        <th class="sorting" style="width: 60px;">更新时间</th>
                        <#if requestContext.getCurrentUser().isCountryManager()>
                        <th class="sorting" style="width: 60px;">操作人</th>
                        <#else>
                            <th class="sorting" style="width: 60px;">发布人</th>
                        </#if>
                        <#if requestContext.getCurrentUser().isCountryManager()>
                        <th class="sorting" style="width: 60px;">是否发布</th>
                        </#if>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
    $(function () {
        //时间转换函数
        function formatDate(d) {
            var hh = d.getHours() < 10 ? "0" + d.getHours() : d.getHours().toString();
            var mm = d.getMinutes() < 10 ? "0" + d.getMinutes() : d.getMinutes().toString();
            var dd = d.getDate() < 10 ? "0" + d.getDate() : d.getDate().toString();
            var MM = d.getMonth() < 9 ? "0" + (d.getMonth() + 1) : (d.getMonth() + 1).toString();
            var yyyy = d.getFullYear().toString();
            return yyyy + "-" + MM + "-" + dd + " " + hh + ":" + mm;
        }

        //跳转添加页面
        $(document).on("click",'.addPublish',function () {
            window.location.href = 'save_page.vpage';
        });
        // 获取数据列表
        function getData() {
            $.get("publish_list.vpage",'',function (res) {
                if(res.success && res.dataList){
                    var dataTableList = [];
                    var arr = [];
                    res.dataList.forEach(function (v,index) {
                        <#if requestContext.getCurrentUser().isCountryManager()>
                            var pushlishBtn = v.status== 2 ? '<a class="btn btn-primary onlineBtn" data-id="'+ v.id +'">发布</a>' : '<a class="btn btn-primary offlineBtn" data-id="'+ v.id +'">下线</a>';
                            var operator = pushlishBtn+'<a class="btn btn-warning deleteBtn" data-id="'+ v.id +'">删除</a><a class="btn btn-primary editBtn" data-id="'+ v.id +'">编辑</a><a class="btn btn-primary viewBtn" data-id="'+ v.id +'">查看</a>';
                            arr = [res.dataList.length-index,v.title,formatDate(new Date(v.createTime)),formatDate(new Date(v.updateTime)),v.operatorName,v.status==2?'否':'是',operator];
                        <#else>
                            var operator = '<a class="btn btn-primary viewBtn" data-id="'+ v.id +'">查看</a>';
                            arr = [res.dataList.length-index,v.title,formatDate(new Date(v.createTime)),formatDate(new Date(v.updateTime)),v.operatorName,operator];
                        </#if>
                        dataTableList.push(arr);
                    });

                    var reloadDataTable = function () {
                        var table = $('#datatable').DataTable({
                            aaSorting: [[0, "desc"]],
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
                    alert(res.info);
                }
            });
        }
        getData();

        // 发布数据
        $(document).on('click','.onlineBtn',function () {
            var _this = $(this);
            var id = _this.data('id');
            $.get("publish_onOffLine.vpage",{id:id,status:1},function (res) {
                if(res.success){
                    _this.text('下线').removeClass('onlineBtn').addClass('offlineBtn');
                    _this.parent().prev().text('是');
                }else {
                    alert(res.info);
                }
            });
        });
        // 下线数据
        $(document).on('click','.offlineBtn',function () {
            var _this = $(this);
            var id = _this.data('id');
            $.get("publish_onOffLine.vpage",{id:id,status:2},function (res) {
                if(res.success){
                    _this.text('发布').removeClass('offlineBtn').addClass('onlineBtn');
                    _this.parent().prev().text('否');
                }else {
                    alert(res.info);
                }
            });
        });
        // 删除数据
        $(document).on('click','.deleteBtn',function () {
            var _this = $(this);
            var id = _this.data('id');

            if (confirm("是否确认删除该数据？")) {
                $.get("publish_delete.vpage",{id:id},function (res) {
                    if(res.success){
                        getData();
                    }else {
                        alert(res.info);
                    }
                });
            }

        });
        // 编辑数据
        $(document).on('click','.editBtn',function () {
            var id = $(this).data('id');
            window.location.href = 'save_page.vpage?publishId='+id;
        });
        // 查看数据详情
        $(document).on('click','.viewBtn',function () {
            var id = $(this).data('id');
            window.location.href = 'publish_detail.vpage?publishId='+id;
        });
    });
</script>
</@layout_default.page>