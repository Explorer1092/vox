<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='合同管理' page_num=17>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>栏目管理</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a data-type="add" class="btn btn-success addBtn" href="javascript:;">
                    <i class="icon-plus icon-white"></i>
                    新建栏目
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <input class="column_level" type="checkbox" value="1" checked>一级栏目
                        <input class="column_level" type="checkbox" value="2" checked>二级栏目
                    </div>
                    <#--<div class="control-group">-->
                        <#--<button id="queryArticleBtn" type="button" class="btn btn-primary">查询</button>-->
                    <#--</div>-->
                </fieldset>
            </form>
            <div class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                       id="datatable"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                    <tr>
                        <th class="sorting">排序</th>
                        <th class="sorting">栏目名称</th>
                        <th class="sorting">栏目级别</th>
                        <th class="sorting">上级栏目</th>
                        <th class="sorting">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<div id="editDepInfo_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">新建/编辑栏目</h4>
            </div>
            <div class="modal-body">
                <div id="editInfoDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script id="editInfoDialogTemp" type="text/x-handlebars-template">
    <input class="column_id" type="hidden" value="{{_id}}">
    <div class="control-group">
        <label class="control-label" for="editDepName">级别</label>
        <div class="controls">
            <input class="edit_level" type="radio" value="1" name="editDepName" {{#compare _level 1}}checked{{/compare}}>一级
            <input class="edit_level" type="radio" value="2" name="editDepName" {{#compare _level 2}}checked{{/compare}}>二级
        </div>
    </div>
    <div class="control-group businessType">
        <label class="control-label">栏目名称</label>
        <div class="controls" style="height:18px;line-height:18px">
            <input type="text"  name ="businessType" value="{{_name}}" id="name" maxlength="10">
        </div>
    </div>
    <div class="control-group businessType parent_level">
        <label class="control-label">上级栏目</label>
        <div class="controls">
            <select id="parentName" name="dpLevel" class="js-groupLevelChoice">
                {{#each data}}
                    {{#if selected}}
                            <option class="group_name" data-id="{{id}}" selected>{{name}}</option>
                    {{else}}
                        <option class="group_name" data-id="{{id}}">{{name}}</option>
                    {{/if}}
                {{/each}}
            </select>
        </div>
    </div>
    <div class="control-group businessType">
        <label class="control-label">排序</label>
        <div class="controls" style="height:18px;line-height:18px">
            <input id="sort" type="number"  name ="businessType" value="{{_sort}}" placeholder="请输入正整数">
        </div>
    </div>
</script>
<script type="text/javascript">
    $(function(){
        var renderDepartment = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };
        var add_column = function (_id,_parentId,_name,_sort,_level) {
            Handlebars.registerHelper("compare",function (v1,v2,options) {
                if(v1 === v2){
                    return options.fn(this);
                }else{
                    return options.inverse(this);
                }
            });
            if(_level == 1){
                var level = true;
            }
            $.get('findColumnListByLevel.vpage?levels=1',function (res) {
                if(res.success){
                    if(res.data && res.data.length > 0){
                        res.data.forEach(function (v) {
                            if(v.id == (_parentId || '').trim()){
                                v.selected = true;
                            }
                        });
                    }
                    renderDepartment("#editInfoDialogTemp",{
                        data:res.data,
                        _id:_id,
                        _name:_name,
                        _sort:_sort,
                        level:level,
                        _level:_level,
                        show_parent:1
                    },"#editInfoDialog");
                    if(_level == 1){
                        $('.parent_level').hide('');
                    }else{
                        $('.parent_level').show('');
                    }
                }
            });
            $("#editDepInfo_dialog").modal('show');

        };
        var get_list = function (level) {
            $.get("findColumnListByLevel.vpage?levels="+level,function (res) {
                if(res.success){
                    var dataTableList = [];
                    for(var i=0;i < res.data.length;i++){
                        var item = res.data[i];
                        var operator = '<span class="edit_column btn btn-primary" data-id="' + item.id + '" data-name=" '+ item.name +'" data-parentid=" '+ item.parentId +'" data-level=" '+ item.level +'" data-sort=" '+ item.sortId +'">编辑栏目</span>'
                                +'<span class="deleteManage btn btn-primary" data-name="'+item.name+'" data-id="'+ item.id+'">删除栏目</span>';
                        var arr = [item.sortId, item.name, item.level, item.parentName, operator];
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
        };
        $(document).on('change','.edit_level',function () {
            if($(this).val() == 1){
                $('.parent_level').hide('');
            }else{
                $('.parent_level').show('');
            }
        });
        $(".column_level").on("change",function(){
            var _level = [],
                    level='',
                    column_level = $(".column_level:checked");
            for (var i = 0;i< column_level.length;i++){
                _level.push(column_level.eq(i).val());
            }
            level = _level.toString();
            get_list(level);
        });

        //跳转dialog
        $(document).on("click",'.addBtn',function () {
            add_column();
        });
        $(document).on("click",'#editDepSubmitBtn',function () {
            var data = {};
            data.level = $('input[name="editDepName"]:checked').val();
            if(!data.level){
                alert('级别必选');
                return false;
            }
            data.name = $('#name').val();
            if(data.name == ''){
                alert('栏目名称必填');
                return false;
            }
            data.id = $('.column_id').val();
            if(data.level == 2){
                data.parentId = $('#parentName option:selected').data('id');
                if(data.parentId == ''){
                    alert('上级栏目必选');
                    return false;
                }
            }
            data.sortId = $('#sort').val();
            if(data.sortId == ''){
                alert('排序必填');
                return false;
            }
            $.get('saveColumnData.vpage',data,function (res) {
                if(res.success){
                    alert('保存成功');
                    $("#editDepInfo_dialog").modal('hide');
                    window.location.reload();
                }else{
                    alert(res.info)
                }
            })
        });
        $(document).on("click",'.edit_column',function () {
            var _parentId = $(this).data('parentid');
            var _name = $(this).data('name');
            var _id = $(this).data('id');
            var _sort = $(this).data('sort');
            var _level = $(this).data('level');
            add_column(_id,_parentId,_name,_sort,_level);
        });
        //跳转详情页
        $(document).on('click','.contract_detail',function () {
            window.location.href = 'detail.vpage?id='+$(this).data('id');
        });
        $(document).on("click",'.deleteManage',function () {
            var _id = $(this).data("id");

            layer.confirm("是否确认删除"+$(this).data("name") +'栏目？', {
                btn: ['确认','取消'] //按钮
            }, function(){
                $.get('deleteColumn.vpage?id='+_id,function (res) {
                    if(res.success){
                        layer.alert('删除成功');
                        window.location.reload();
                    }else{
                        layer.alert(res.info)
                    }
                })
            });
        });
        $("#queryOralBtn").trigger("click");
        $("#queryArticleBtn").trigger("click");
        get_list('1,2')
    });
</script>
</@layout_default.page>
