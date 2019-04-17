<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='总部接口人维护' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header btn-group" style="padding: 5px;height: auto;">
            <button type="button" class="btn btn-primary tab_btn" style="padding: 8px 30px;">事项列表</button>
            <button type="button" class="btn tab_btn" style="padding: 8px 30px;">类型维护</button>
            <div class="pull-right">
                <a id="addMatter" class="btn btn-success" href="javascript:;">
                    <i class="icon-plus icon-white"></i>添加事项
                </a>
                <a id="addName" class="btn btn-success" href="javascript:;" style="display: none;">
                    <i class="icon-plus icon-white"></i>添加类型
                </a>
            </div>
        </div>
        <div class="box-content list_content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr>
                        <th style="width: 1px;">序号</th>
                        <th class="sorting" style="width: 50px;">类型</th>
                        <th class="sorting" style="width: 90px;">事项</th>
                        <th class="sorting" style="width: 60px;">说明</th>
                        <th class="sorting" style="width: 30px;">联系人</th>
                        <th class="sorting" style="width: 100px;">邮箱</th>
                        <th class="sorting" style="width: 100px;">微信群</th>
                        <th class="sorting" style="width: 70px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
        <div class="box-content matter_content" style="display: none">
            <div id="DataTables_Table_1_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table id ="datatable1" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">序号</th>
                        <th class="sorting" style="width: 80px;">类型</th>
                        <th class="sorting" style="width: 90px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>

<#--修改类别名称弹窗-->
<div id="setName_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">请填写类别名称</h4>
            </div>
            <div class="modal-body">
                <div id="setNameDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                    <button id="setNameSubmitBtn" type="button" class="btn btn-large btn-primary">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>
<#--修改类别名称信息模板-->
<script id="setNameDialogTemp" type="text/x-handlebars-template">
    <div class="control-group">
        <span>序号：</span>
        <input id="sortId" type="text" value="{{sortId}}" name="sortId" style="width: 350px">
        <span style="color: #f00">请输入正整数</span>
    </div>
    <div class="control-group">
        <span>名称：</span>
        <input type="hidden" value="{{id}}" id="groupId">
        <input id="typeName" type="text" value="{{typeName}}" name="typeName" style="width: 350px">
        <span style="color: #f00">20字以内</span>
    </div>
</script>


<script type="text/javascript">
    $(function () {
        // 跳转添加事项
        $(document).on('click','#addMatter',function () {
            window.location.href = '/sysconfig/config/addViewPage.vpage';
        });

        // tab切换
        $(document).on('click','.tab_btn',function () {
            var _this = $(this),
                _index = _this.index();
            $(this).addClass('btn-primary').siblings('.tab_btn').removeClass('btn-primary');
                if(_index==0){
                    $('.list_content,#addMatter').show();
                    $('.matter_content,#addName').hide();
                    refreshPage()
                }else{
                    $('.list_content,#addMatter').hide();
                    $('.matter_content,#addName').show();
                    refreshTypeList();
                }
        })

        //加载页面 获取数据
        function refreshPage() {
            $.get("selfHelpList.vpage",{},function (res) {
                if(res.success && res.data){
                    var dataTableList = [];
                    var num = 1;
                    for(var i=0;i < res.data.length;i++){
                        var tlist = res.data[i].tlist ? res.data[i].tlist : [];
                        for(var j=0;j < tlist.length;j++){
                            var item = tlist[j]
                            var arr = [];
                            var operator = "<a href='javascript:;' class='btn btn-primary editBtn' data-id='"+item.id+"'>修改</a><a href='javascript:;' class='btn btn-warning deleteBtn' data-id='"+item.id+"'>删除</a><a href='javascript:;' class='btn btn-primary viewBtn' data-id='"+item.id+"'>查看</a>";
                            arr = [num++,res.data[i].typeName,item.title,item.comment,item.contact,item.email,item.wechatGroup,operator];
                            dataTableList.push(arr);
                        }
                    }

                    var reloadDataTable = function () {
                        var table = $('#datatable').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据
                    };
                    setTimeout(reloadDataTable(),0);

                }else{
                    layer.alert(res.info);
                }
            });
        }

        //加载页面 获取类型维护数据
        function refreshTypeList() {
            $.get("typeList.vpage",{},function (res) {
                if(res.success && res.dataList){
                    var dataTableList = [];
                    for(var i=0;i < res.dataList.length;i++){
                        var item = res.dataList[i];
                        var arr = [];

                        var operator = "<a class='btn btn-warning matterDeleteBtn' data-id='"+item.id+"'>删除</a><a class='btn btn-primary matterEditBtn' data-sortid='"+item.sortId+"' data-id='"+item.id+"' data-name='"+item.typeName+"'>编辑</a>";
                        arr = [item.sortId,item.typeName,operator];
                        dataTableList.push(arr);
                    }

                    var reloadDataTable1 = function () {
                        var table = $('#datatable1').dataTable();
                        table.fnClearTable();
                        table.fnAddData(dataTableList); //添加添加新数据
                    };
                    setTimeout(reloadDataTable1(),0);
                }else{
                    layer.alert(res.info);
                }
            });
        }

        refreshPage();//首次进入页面加载数据

        // 删除事项
        $(document).on('click','.deleteBtn',function () {
            var id = $(this).data("id");
            layer.confirm("是否确认删除？", {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.get('delSelfHelpItem.vpage?',{id:id},function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        refreshPage();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });

        // 跳转事项编辑页面
        $(document).on('click','.editBtn',function () {
            var id = $(this).data('id')
            window.location.href = 'itemInfo.vpage?type=edit&id='+id;
        });

        // 跳转事项详情页面
        $(document).on('click','.viewBtn',function () {
            var id = $(this).data('id')
            window.location.href = 'itemInfo.vpage?type=view&id='+id;
        });


        // 类型维护开始
        //渲染模板
        var renderDepartment = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };

        //设置类别名称信息
        $(document).on("click",".matterEditBtn",function(){
            var id = $(this).data("id");
            var typeName = $(this).data("name");
            var sortId = $(this).data("sortid");
            renderDepartment("#setNameDialogTemp",{
                id:id,
                sortId:sortId,
                typeName:typeName
            },"#setNameDialog");

            $("#setName_dialog").modal('show');
        });

        // 添加类别
        $(document).on('click','#addName',function () {
            renderDepartment("#setNameDialogTemp",{},"#setNameDialog");
            $("#setName_dialog").modal('show');
        });

        // 删除类别
        $(document).on('click','.matterDeleteBtn',function () {
            var id = $(this).data("id");
            layer.confirm("是否确认删除？", {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.get('delType.vpage?',{typeId:id},function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        refreshTypeList();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });

        //修改类别提交
        $(document).on("click","#setNameSubmitBtn",function(){
            var typeName = $("#typeName").val();
            var id = $("#groupId").val();
            var sortId = $("#sortId").val();

            var postData = {
                typeName: typeName,
                sortId: sortId,
                id: id
            };
            if(postData.typeName.length <= 20 && /^[1-9]\d*$/.test(postData.sortId)){
                $.get("saveTypeData.vpage",postData,function(res){
                    if(res.success){
                        layer.alert("编辑成功");
                        $("#setName_dialog").modal('hide');
                        refreshTypeList();
                    }else{
                        layer.alert(res.info);
                    }
                });
            }
        });
    });
</script>
</@layout_default.page>
