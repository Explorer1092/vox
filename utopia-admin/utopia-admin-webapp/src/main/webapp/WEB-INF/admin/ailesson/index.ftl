<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='情景对话管理' page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<style>
    .btn{
        margin-bottom: 10px;
    }
</style>
<div id="main_container" class="span9">
    <legend>
        情景对话管理
    </legend>
    <div >
        <a class="btn btn-primary" href="/chips/ailesson/dialogue/addform.vpage">添加-情景对话</a>
        <a class="btn btn-primary" href="/chips/ailesson/data/export.vpage?type=dialogue">导出</a>
        <a class="btn btn-primary" href="/chips/ailesson/data/export.vpage?type=dialogue&json=stone">导出石头堆格式</a>
        <!-- Button trigger modal -->
        <button type="button" class="btn btn-primary btn-lg" data-toggle="modal" data-target="#myModal">
            导入
        </button>
    </div>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>课程Id</td>
                        <td>对话标题</td>
                        <td>更新日期</td>
                        <td>操作</td>
                    </tr>
                    <#if result?? >
                        <#list result as r >
                            <tr>
                                <td>${r.id!}</td>
                                <td>${r.title!}</td>
                                <td>${r.updateTime!}</td>
                                <td>
                                    <a href="/chips/ailesson/dialogue/addform.vpage?id=${r.id!}" name="edit" data-id="${r.id!}">编辑</a>
                                    <a href="javascript:void(0);" name="remove" remove="${r.id!}">删除</a>
                                    <a href="javascript:void(0);" name="load" load="${r.id!}">加载到对话系统</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel">情景对话导入</h4>
                </div>
                <div class="modal-body">
                    <form method="post" action="/chips/ailesson/data/import.vpage" enctype="multipart/form-data" id="importDataForm">
                    <input type="file" name="file" id="file">
                    <input type="hidden" name="type" value="dialogue">
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-dismiss="modal" style="margin-bottom:0;">关闭</button>
                    <button type="button" class="btn btn-primary" id="saveImportData">保存</button>
                </div>
            </div>
        </div>
    </div>


</div>
<script type="text/javascript">
    $(function(){

        $("a[name=remove]").click(function () {
            if (!confirm("确定删除数据吗？")) {
                return;
            }
            var id = $(this).attr("remove");

            $.post('${requestContext.webAppContextPath}/chips/ailesson/dialogue/delete.vpage', {id: id}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("a[name=load]").click(function () {
            var id = $(this).attr("load");
            $.post('${requestContext.webAppContextPath}/chips/ailesson/talk/sync.vpage', {id: id, type:"dialogue"}, function (res) {
                if (res.success) {
                    alert("加载成功");
                } else {
                    alert(res.info);
                }
            });
        });

        $("#saveImportData").click(function () {
            var fileObj = document.getElementById("file").files[0]; // js 获取文件对象
            if (typeof (fileObj) == "undefined" || fileObj.size <= 0) {
                alert("请选择文件");
                return;
            }
            var formFile = new FormData();
            formFile.append("action", "UploadVMKImagePath");
            formFile.append("file", fileObj); //加入文件对象
            formFile.append("type", "dialogue"); //

            var data = formFile;
            $.ajax({
                url: "/chips/ailesson/data/import.vpage",
                data: data,
                type: "Post",
                dataType: "json",
                cache: false,//上传文件无需缓存
                processData: false,//用于对data参数进行序列化处理 这里必须false
                contentType: false, //必须
                success: function (result) {
                    alert(result.info);
                },
                error:function(err){
                    console.log(err)
                }
            });
        });
    });
</script>
</@layout_default.page>