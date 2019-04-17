<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='专员绩效分组' page_num=6>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-th"></i>部门HC维护</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content ">
            <div class="pull-right" style="margin-bottom:15px">
                <form id="importSchoolDict" method="post" enctype="multipart/form-data"
                      action="/sysconfig/headcount/import_headcount.vpage" data-ajax="false"
                      class="form-horizontal">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">上传excel</label>
                        <div class="controls">
                            <input id="sourceFile" name="sourceFile" type="file">
                            <a href="javascript:;" onclick="isSave()" class="btn btn-primary">批量导入</a>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <a href="download_template.vpage" class="btn btn-primary">下载导入模版</a>
                            <a href="export_group_hc.vpage" class="btn btn-primary">导出</a>
                        </div>
                    </div>
                </form>
            </div>

            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper " role="grid" style="margin-top:30px">
                <table id ="datatable" class="table table-striped table-bordered bootstrap-datatable datatable dataTable" >
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 80px;">部门</th>
                        <th class="sorting" style="width: 90px;">应招专员</th>
                        <th class="sorting" style="width: 60px;">实际带校专员</th>
                        <th class="sorting" style="width: 100px;">满编率</th>
                        <th class="sorting" style="width: 100px;">操作</th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<#--修改HC信息弹窗-->
<div id="setHC_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">修改信息</h4>
            </div>
            <div class="modal-body">
                <div id="setHCDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="setHCSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<#--修改应招专员信息模板-->
<script id="setHCDialogTemp" type="text/x-handlebars-template">
    <div class="control-group">
        <label class="control-label" for="editDepName">部门名称：</label>
        <input type="text" value="{{groupName}}" disabled>
        <input type="hidden" value="{{groupId}}" id="groupId">
    </div>
    <div class="control-group">
        <label class="control-label" for="focusedInput">应招专员：</label>
        <input id="headCount" type="text"  onkeyup="this.value=this.value.replace(/\D/g,'')" onafterpaste="this.value=this.value.replace(/\D/g,'')" value="{{headCount}}" name="headCount">
        <span style="color: #f00">请输入大于0的整数</span>
    </div>
</script>
<script>
    jQuery.extend(jQuery.fn.dataTableExt.oSort, {
        "html-percent-pre": function (a) {
            var x = String(a).replace(/<[\s\S]*?>/g, "");    //去除html标记
            x = x.replace(/&amp;nbsp;/ig, "");                   //去除空格
            x = x.replace(/%/, "");                          //去除百分号
            return parseFloat(x);
        },

        "html-percent-asc": function (a, b) {                //正序排序引用方法
            return ((a < b) ? -1 : ((a > b) ? 1 : 0));
        },

        "html-percent-desc": function (a, b) {                //倒序排序引用方法
            return ((a < b) ? 1 : ((a > b) ? -1 : 0));
        }
    });
</script>
<script type="text/javascript">
    // $(function () {
        function isSave() {
            var sourceFile = $("#sourceFile").val();
            if (blankString(sourceFile)) {
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt !== "xls" && fileExt !== "xlsx") {
                alert("请上传正确格式的excel！");
                return;
            }

            var formElement = document.getElementById("importSchoolDict");
            var postData = new FormData(formElement);

            $("#loadingDiv").show();

            $.ajax({
                url: "import_headcount.vpage",
                type: "POST",
                data: postData,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
                success: function (res) {
                    $("#loadingDiv").hide();
                    if (res.success) {
                        window.location.reload();
                    } else {
                        var error = res.errorList;
                        layer.alert(error.toString());
                    }
                },
                error: function (e) {
                    $("#loadingDiv").hide();
                }
            });
        }

        //加载页面 获取数据
        function refreshPage() {
            $.get("list.vpage",{},function (res) {
                if(res.success && res.dataList){
                    var dataTableList = [];
                    for(var i=0;i < res.dataList.length;i++){
                        var item = res.dataList[i];
                        var arr = [];

                        var operator = "<a class='btn btn-primary editBtn' data-gname='"+item.groupName+"' data-ghc='"+item.headCount+"' data-dpid='"+item.groupId+"'>编辑</a>";
                        arr = [item.groupName, item.headCount, item.actuallyCount,item.actuallyRate+'%', operator];
                        dataTableList.push(arr);
                    }

                    var reloadDataTable = function () {
                        var table = $('#datatable').DataTable({
                            "aoColumnDefs": [
                                { "sType": "html-percent", "aTargets": [3] }    //指定列号使用自定义排序
                            ],
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

        refreshPage();//首次进入页面加载数据

        //渲染模板
        var renderDepartment = function(tempSelector,data,container){
            var source   = $(tempSelector).html();
            var template = Handlebars.compile(source);

            $(container).html(template(data));
        };

        //设置部门应招专员信息
        $(document).on("click",".editBtn",function(){
            var groupId = $(this).data("dpid");
            var groupName = $(this).data("gname");
            var headCount = $(this).data("ghc");
            renderDepartment("#setHCDialogTemp",{
                groupId:groupId,
                groupName:groupName,
                headCount:headCount
            },"#setHCDialog");

            $("#setHC_dialog").modal('show');
        });

        //修改应招专员提交
        $(document).on("click","#setHCSubmitBtn",function(){
            var headCount = $("#headCount").val();
            var gpid = $("#groupId").val();

            var postData = {
                headCount: headCount,
                groupId: gpid
            };
            $.post("/user/orgconfig/set_hc.vpage",postData,function(res){
                if(res.success){
                    alert("修改成功");
                    $("#setHC_dialog").modal('hide');
                    refreshPage();
                }else{
                    alert(res.info);
                }
            });

        });
    // });
</script>
</@layout_default.page>
