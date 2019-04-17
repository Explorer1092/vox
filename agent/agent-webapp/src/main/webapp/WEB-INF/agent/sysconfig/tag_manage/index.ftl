<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='标签管理' page_num=6>
<style>
    .table>tbody>tr>td{
        text-align:center;
    }
</style>
<div class="tag_manage_wrap">
    <div class="row-fluid sortable ui-sortable">
        <div class="box span12">
            <div class="alert alert-error" hidden>
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong id="error-panel"></strong>
            </div>
            <div class="alert alert-info" hidden>
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong id="info-panel"></strong>
            </div>
            <div class="box-header well" data-original-title="">
                <h2><i class="icon-th"></i>标签管理</h2>
                <div class="box-icon">
                    <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                    <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
                </div>
            </div>
            <div class="box-content" style="width:auto;height:50px">
                <div class="control-group span5" >
                    <div class="controls">
                        <a href="javascript:;" class="btn btn-success editTag"" style="height:30px;line-height:30px" id="addTag" data-type="add">添加标签</a>
                    </div>
                </div>
            </div>
            <div class="box-content">
                <div class="dataTables_wrapper" role="grid">
                    <table class="table table-striped table-bordered bootstrap-datatable datatable dataTable "
                           id="datatable"
                           aria-describedby="DataTables_Table_0_info">
                        <thead>
                        <tr>
                            <th class="sorting" style="width: 60px; text-align: center">类别</th>
                            <th class="sorting" style="width: 60px; text-align: center">标签类别</th>
                            <th class="sorting" style="width: 60px;text-align: center">标签名称</th>
                            <th class="sorting" style="width: 140px;text-align: center">覆盖数量</th>
                            <th class="sorting" style="width: 140px;text-align: center">天玑是否可见</th>
                            <th class="sorting" style="width: 140px;text-align: center">天玑显示顺序</th>
                            <th class="sorting" style="width: 140px;text-align: center">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <#--编辑标签弹窗-->
    <div id="editTag_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title changeTitle" id="editTagName">添加标签</h4>
                </div>
                <div class="modal-body">
                    <div class="control-group" id="tagTypeWrap" style="display: block">
                        <div class="row-fluid">
                            <div class="span3">
                                <label for="">类型:</label>
                            </div>
                            <div>
                                <input type="radio" name="tagType" value="SCHOOL" style="margin-left: 3px">学校&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" name="tagType" value="TEACHER" style="margin-left: 3px">老师
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="row-fluid">
                            <div class="span3">
                                <label for="">子类别:</label>
                            </div>
                            <div>
                                <select name="" id="tagSub">

                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="row-fluid">
                            <div class="span3">
                                <label for="">标签名称:</label>
                            </div>
                            <div class="">
                                <input type="text" maxlength="10" id="tagName">
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="row-fluid">
                            <div class="span3">
                                <label for="">天玑可见:</label>
                            </div>
                            <div>
                                <input type="radio" name="visible" value="true"  style="margin-left: 3px">是&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" name="visible" value="false"  style="margin-left: 3px">否
                            </div>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="row-fluid">
                            <div class="span3">
                                <label for="">标签排序:</label>
                            </div>
                            <div class="">
                                <input type="text" maxlength="10" id="orderDate">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <div>
                        <button id="editDepSubmitBtn" type="button" class="btn btn-large btn-primary" data-id="">确定</button>
                        <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#--删除弹窗-->
    <div id="deleteTag_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <h3 >删除标签后,该标签下关联的学校或老师也将删除该标签,确认要删除吗?</h3>
                </div>
                <div class="modal-footer">
                    <div>
                        <button id="deleteDepSubmitBtn" type="button" class="btn btn-large btn-default">删除</button>
                        <button type="button" class="btn btn-large btn-primary" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <#--导入弹窗-->
    <div id="insertTag_dialog" class="modal fade hide">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title changeTitle" id="editTagName">导入标签</h4>
                </div>
                <div class="modal-body">
                    <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                          action="/sysconfig/schooldic/bulkImportSchoolDictInfo.vpage" data-ajax="false"
                          class="form-horizontal">
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">上传excel</label>
                            <div class="controls">
                                <input id="sourceFile" name="sourceFile" type="file">
                                &nbsp;&nbsp;&nbsp;&nbsp;
                                <a href="/sysconfig/tag/download_import_tag_template.vpage" class="btn btn-primary">下载导入模版</a>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <div>
                        <button id="insertDepSubmitBtn" type="button" class="btn btn-large btn-default" >导入</button>
                        <button type="button" class="btn btn-large btn-primary" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
<script>
    $(function () {
//        获取子标签类型
        $.get("/sysconfig/tag/tag_sub_type_list.vpage",{},function (res) {
            if(res.success){
                var tagSubHtml = '<option value="0">请选择</option>';
                for(var i = 0; i < res.tagSubTypeList.length ; i++){
                    tagSubHtml += '<option class="tagSubList" value="'+ res.tagSubTypeList[i].code+'">'+res.tagSubTypeList[i].desc +'</option>'
                };
                $('#tagSub').html(tagSubHtml);
            }else{
                layer.alert(res.info)
            }
        });
        $(document).on("click",".editTag",function () {
            var type = $(this).attr('data-type'),
                tagName = $("#tagName"),
                order =   $("#orderDate"),
                editTag_dialog = $('#editTag_dialog');
            for(var i = 0 ;i <$(".tagSubList").length;i++){
                if($(".tagSubList").eq(i).attr("selected") ){
                    $(".tagSubList").eq(i).attr("selected",false)
                }
            };
            $("input[type = 'radio']").parent().removeAttr("class");
            $("input[type = 'radio']:checked").attr("checked",false);
            if(type === 'add'){
                $("#tagTypeWrap").show();
                $("#editTagName").html("添加标签");
                tagName.val("");
                order.val("");
                editTag_dialog.modal('show');
            }else if(type === "edit"){
                $("#tagTypeWrap").hide();
                var _this =  this;
                var tagSub = $(this).attr("data-tagsubcode");
                var tagType_val = $(_this).attr("data-tagType"),
                     isVisible_val = $(_this).attr("data-isVisible");
                tagName.val($(_this).attr("data-tagName"));
                order.val($(_this).attr("data-sortNum"));
                for(var i = 0 ;i <$(".tagSubList").length;i++){
                    if($(".tagSubList").eq(i).attr("value") === tagSub){
                        $(".tagSubList").eq(i).attr("selected",true)
                    }
                }
                if(tagType_val === "SCHOOL"){
                    $("input[name = 'tagType'][value = 'SCHOOL']").parent().attr("class","checked");
                    $("input[name = 'tagType'][value = 'SCHOOL']").attr("checked",true);
                }else {
                    $("input[name = 'tagType'][value = 'TEACHER']").parent().attr("class","checked");
                    $("input[name = 'tagType'][value = 'TEACHER']").attr("checked",true);
                }
                if(isVisible_val === "true"){
                    $("input[name = 'visible'][value = 'true']").parent().attr("class","checked");
                    $("input[name = 'visible'][value = 'true']").attr("checked",true);
                }else {
                    $("input[name = 'visible'][value = 'false']").parent().attr("class","checked");
                    $("input[name = 'visible'][value = 'false']").attr("checked",true);
                }
                $("#editTagName").html("修改标签");
                editTag_dialog.modal('show');
                $("#editDepSubmitBtn").attr("data-id",$(_this).attr("data-id"))
            };
        });
        //        点击列表删除按钮
        $(document).on("click","#delete",function () {
            var _this = this;
            $("#deleteDepSubmitBtn").attr("data-id",$(_this).attr("data-id"));
            $("#deleteTag_dialog").modal("show")
        });
        //点击弹窗提示二次确认删除
        $(document).on("click","#deleteDepSubmitBtn",function () {
            var _this = this;
            $.get("/sysconfig/tag/delete_tag.vpage",{id:$(_this).attr("data-id")},function (res) {
                if (res.success) {
                    alert("删除成功");
                    window.location.reload();
                }else{
                    alert(res.info);
                }
                $('#deleteTag_dialog').modal('hide');
            });
        });
//       点击确定
        $(document).on("click","#editDepSubmitBtn",function () {
            var tagName = $("#tagName").val(),
                order =   $("#orderDate").val(),
                tagType = $("input[name = 'tagType']:checked").val(),
                visible = $("input[name = 'visible']:checked").val(),
                tagSub = $("#tagSub").val();
            if(!tagType){
                alert('请选择类型');
                return;
            }else if(tagSub === "0"){
                alert("请选择子类别");
                return;
            }else if(!tagName){
                alert('请填写标签名称');
                return;
            }else if(!visible){
                alert('请选择天玑是否可见');
                return;
            }else if(!order){
                alert('请填写标签排序');
                return;
            }else if(order){
                var reg=/^[0-9]+.?[0-9]*$/;
                if(!reg.test(order)){
                    alert("请输入数字");
                    $("#orderDate").val("");
                    return;
                }
            }
            if($("#editDepSubmitBtn").attr("data-id")){
                var dataObj = {
                    id:$("#editDepSubmitBtn").attr("data-id"),
                    tagType:tagType,
                    name:tagName,
                    isVisible:visible === "true" ? true : false,
                    sortNum:order,
                    tagSubTypeCode:tagSub,
                },
                successInfo = "修改标签成功";
            }else{
                var dataObj = {
                    tagType:tagType,
                    name:tagName,
                    isVisible:visible === "true" ? true : false,
                    sortNum:order,
                    tagSubTypeCode:tagSub,
                },
                    successInfo = "添加标签成功";
            }
            $.post("/sysconfig/tag/edit_tag.vpage",dataObj,function (res) {
                $('#editTag_dialog').modal('hide');
                if (res.success) {
                    alert(successInfo);
                    window.location.reload();
                }else{
                    alert(res.info);
                }
            });
        });
//        获取类表数据
        $.get("/sysconfig/tag/tag_list.vpage",{},function (res) {
            if(res.success){
                var dataTableList = [];
                for(var i=0;i < res.dataList.length;i++){
                    var item = res.dataList[i];
                    var operator = '<a href="/sysconfig/tag/detail.vpage?tagId='+ item.id +'">查看</a>&nbsp;&nbsp;&nbsp;&nbsp;'+
                            '<a href="javascript:;" data-type="edit" class="editTag" data-sortNum="'+ item.sortNum +
                            '" data-isVisible="'+item.isVisible+
                            '" data-tagType="'+ item.tagType +
                            '" data-tagName="'+ item.name +'" data-id="'+ item.id +
                            '" data-tagSubCode="'+ item.tagSubTypeCode+'">修改</a>&nbsp;&nbsp;&nbsp;&nbsp;'+
                            '<a href="/sysconfig/tag/export_tag_info.vpage?tagId='+ item.id+'">导出</a>&nbsp;&nbsp;&nbsp;&nbsp;'+
                            '<a href="javascript:;" id="insertBtn" data-id="'+ item.id +'">导入</a>&nbsp;&nbsp;&nbsp;&nbsp;'+
                            '<a href="javascript:;" id="delete" data-id="'+ item.id +'">删除</a>&nbsp;&nbsp;&nbsp;&nbsp;'
                    if(item.tagType == 'TEACHER'){
                        item.tagType = '老师';
                    }else if(item.tagType == 'NOTIFY'){
                        item.tagType = '通知';
                    }else if(item.tagType === 'SCHOOL'){
                        item.tagType = '学校';
                    };
                    if(item.isVisible){
                        item.isVisible = "是"
                    }else {
                        item.isVisible = "否"
                    };
                    if(item.tagType !== "通知"){
                        var arr = [item.tagType,item.tagSubTypeDesc, item.name, item.coverNum?item.coverNum:0, item.isVisible, item.sortNum, operator];
                        dataTableList.push(arr);
                    }else if(item.tagType === '通知'){
                        operator = "";
                        var arr = [item.tagType,item.tagSubTypeDesc ? item.tagSubTypeDesc : "", item.name, item.coverNum?item.coverNum:0, item.isVisible, item.sortNum,operator];
                        dataTableList.push(arr);
                    }
                }
                var reloadDataTable = function () {
                    var table = $('#datatable').DataTable({
                        'aaSorting':[],
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
//        点击导入
        $(document).on("click","#insertBtn",function () {
            var _this = this;
            console.log($("#sourceFile").val())
            $("#sourceFile").val("");
            $(".filename").html("");
            $("#insertDepSubmitBtn").attr("data-id",$(_this).attr("data-id"));
            $("#insertTag_dialog").modal("show");

        });
        $(document).on("click","#insertDepSubmitBtn",function () {
            var sourceFile = $("#sourceFile").val(),
                    _this =  this;
            var tagId = $(_this).attr("data-id");
            if (blankString(sourceFile)) {
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt != "xls" && fileExt != "xlsx") {
                alert("请上传正确格式的excel！");
                return;
            }

            var formElement = document.getElementById("importSchoolDict");
            var postData = new FormData(formElement);
            postData.append('tagId',tagId);
            $.ajax({
                url: "/sysconfig/tag/import_tag_info.vpage",
                type: "POST",
                data:postData ,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
                success: function (res) {
                    $("#loadingDiv").hide();
                    if (res.success) {
                        alert("导入成功");
                        $("#insertTag_dialog").modal("hide");
                        window.location.reload();
                    } else {
                        var errListStr = res.errorInfoList.join(",");
                        alert(errListStr);
                    }
                },
            });
        })
    });
</script>
</@layout_default.page>