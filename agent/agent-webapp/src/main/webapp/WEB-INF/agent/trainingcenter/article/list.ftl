<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='合同管理' page_num=17>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>文章管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a data-type="add" class="btn btn-success addBtn" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    添加
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">一级栏目</label>
                        <div class="controls">
                            <select name="oneLevelColumnId" id="oneLevelColumnId">
                                <option value="">请选择</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">二级栏目</label>
                        <div class="controls">
                            <select name="twoLevelColumnId" id="twoLevelColumnId">
                                <option value="">请选择</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">标题</label>
                        <div class="controls">
                            <input type="text" id="title" name="title">
                        </div>
                    </div>
                    <div class="control-group">
                        <button id="queryArticleBtn" type="button" class="btn btn-primary">查询</button>
                    </div>
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 20px;">编号</th>
                        <th class="sorting" style="width: 160px;">标题</th>
                        <th class="sorting" style="width: 60px;">一级栏目</th>
                        <th class="sorting" style="width: 60px;">二级栏目</th>
                        <th class="sorting" style="width: 60px;">发布人</th>
                        <th class="sorting" style="width: 100px;">发布时间</th>
                        <th class="sorting" style="width: 60px;">浏览次数</th>
                        <th class="sorting" style="width: 100px;">是否发布</th>
                        <th class="sorting" style="width: 140px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<script id="columnList" type="text/html">
    <option value="">请选择</option>
    <%for(var i = 0; i< res.length; i++){%>
    <%var data = res[i].first || res[i]%>
    <option value="<%=data.id%>"><%=data.name%></option>
    <%}%>
</script>

<script type="text/javascript">
    $(function(){
        var column = [];
        $.get('/trainingcenter/column/findLinkageColumnList.vpage',function (res) {
            if(res.success){
                column = res.data;
                $('#oneLevelColumnId').html(template('columnList',{res:column || ''}));
            }else{
                layer.alert('获取栏目失败');
            }
        });

        $(document).on('change','#oneLevelColumnId',function () {
            var _this = $(this);
            var list = [];
            column.forEach(function (item) {
                if(item.first.id == _this.val()){
                    list = item.second;
                }
            });
            $('#twoLevelColumnId').html(template('columnList',{res:list || ''}));
        });

        $("#queryArticleBtn").on("click",function(){
            var dataObj = {
                oneLevelColumnId:$('#oneLevelColumnId').val() ? $('#oneLevelColumnId').val() : '',
                twoLevelColumnId:$('#twoLevelColumnId').val() ? $('#twoLevelColumnId').val() : '',
                title:$('#title').val() ? $('#title').val() : ''
            };
            $.get("article_list.vpage",dataObj,function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.dataList.length;i++){
                        var item = res.dataList[i];
                        var articleStatus = item.publishStatus == '未发布' ? '发布' : '下线';
                        var url = item.publishStatus == '未发布' ? 'publish_article.vpage' : 'offline_article.vpage';
                        var operator = '<span class="article_delete btn btn-warning" data-id="' + item.id + '">删除</span>'
                                +'<span class="article_edit btn btn-primary" data-id="'+ item.id+'">修改</span>'
                                +'<span class="article_detail btn btn-primary" data-id="'+ item.id+'">查看</span>'
                                +'<span class="article_oper btn btn-primary" data-id="'+ item.id+'" data-url="'+ url+'">'+articleStatus+'</span>';
                        var arr = [i+1, item.title, item.oneLevelColumnName, item.twoLevelColumnName, item.publisherName, item.publishTime, item.viewsNumAll, item.publishStatus, operator];
                        dataTableList.push(arr);
                    }
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
                    layer.alert(res.info)
                }
            });

        });
        //跳转添加页
        $(document).on("click",'.addBtn',function () {
            window.location.href = 'add.vpage';
        });
        //跳转编辑页
        $(document).on("click",'.article_edit',function () {
            window.location.href = 'edit.vpage?id='+$(this).data('id');
        });
        //跳转详情页
        $(document).on('click','.article_detail',function () {
            window.location.href = 'detail.vpage?id='+$(this).data('id');
        });
        //删除文章
        $(document).on("click",'.article_delete',function () {
            var _id = $(this).data("id");

            layer.confirm('是否确认删除该文章', {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.get('delete_article.vpage?id='+_id,function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        $("#queryArticleBtn").click();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });
        //发布文章
        $(document).on("click",'.article_oper',function () {
            var id = $(this).data("id");
            var url = $(this).data("url");
            $.get(url,{id:id},function (res) {
                if(res.success){
                    layer.alert('操作成功');
                    $("#queryArticleBtn").trigger("click");
                }else{
                    layer.alert('操作失败');
                }
            })
        });

        $("#queryArticleBtn").trigger("click");
    });
</script>
</@layout_default.page>
