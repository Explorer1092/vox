<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=10>
    <#import "../../mizar/pager.ftl" as pager />
<div id="main_container" class="span9">
    <legend>
        <strong>付费商品管理</strong>
        <a id="add_product_btn" href="productdetail.vpage" type="button" class="btn btn-info" style="float: right">
            <i class="icon-plus icon-white"></i>添加商品
        </a>
        <a id="add_product_btn" href="itemlist.vpage" type="button" class="btn btn-info"
           style="float: right;margin-right:5px;">
            <i class="icon-list icon-white"></i>子商品管理
        </a>
        <a id="upload-btn" type="button" class="btn btn-info" style="float: right;margin-right:5px;">
            <i class="icon-upload icon-white"></i>批量导入
        </a>
    </legend>
    <form id="activity-query" class="form-horizontal" method="get"
          action="${requestContext.webAppContextPath}/appmanager/product/index.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="query_frm" class="form-horizontal" method="get" action="index.vpage">
                    <input type="hidden" id="page" name="page" value="${currentPage!'1'}"/>
                    <ul class="inline">
                        <li>
                            商品名称：<input type="text" id="name" name="name" value="<#if name??>${name}</#if>"
                                        placeholder="输入商品名称">
                        </li>
                        <li>
                            商品类别：<select id="productType" name="productType">
                            <option value="">全部</option>
                            <#list productTypes as c>
                                <option value="${c.name()!}"
                                        <#if productType?? && c.name() == productType>selected</#if>>${c.name()!}</option>
                            </#list>
                        </select>
                        </li>
                        <li>
                            <button type="submit" id="filter" class="btn btn-primary">
                                <i class="icon-search icon-white"></i> 查 询
                            </button>
                        </li>
                    </ul>
                </form>
                <@pager.pager/>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="200">ID</th>
                        <th>创建时间</th>
                        <th width="400">名称</th>
                        <th>价格</th>
                        <th>状态</th>
                        <th>类别</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if productPage?? && productPage.content?? >
                            <#list productPage.content as p >
                            <tr>
                                <td>
                                    <pre>${p.id!}</pre>
                                </td>
                                <td>
                                    <pre>${p.createDatetime!}</pre>
                                </td>
                                <td>
                                    <pre>${p.name!}</pre>
                                </td>
                                <td>
                                    <pre>${p.price!}</pre>
                                </td>
                                <td>
                                    <#switch p.status>
                                        <#case "ONLINE">
                                            上线
                                            <#break>
                                        <#case "OFFLINE">
                                            下线
                                            <#break>
                                        <#default>
                                    </#switch>
                                </td>
                                <td>
                                    <pre>${p.productType!}</pre>
                                </td>
                                <td>
                                    <a type="button" class="btn btn-info" href="productdetail.vpage?id=${p.id!''}">
                                        <i class="icon-edit icon-white"></i>编辑
                                    </a>
                                    <a type="button" class="btn btn-info delete-btn" href="javascript:void(0)"
                                       data-id="${p.id!''}">
                                        <i class="icon-remove icon-white"></i>删除
                                    </a>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <@pager.pager/>
            </div>
        </div>
    </div>
</div>

<!-- 批量上传的窗口 -->
<div id="upload-dialog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×
                </button>
                <h3 class="modal-title">批量上传</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="upload-product-form" action="save.vpage" method="post" role="form">
                    <div class="form-group">
                        <input type="file" id="upFile" name="upFile">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-upload-btn">确认</button>
            </div>
        </div>
    </div>
</div>

<style>
    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
</style>
<script type="text/javascript">
    $(function () {
        $("#confirm-upload-btn").click(function(){

            var formData = new FormData();
            var fileInput = $("#upFile")[0];
            if(fileInput.files.length > 0){
                formData.append("upFile", fileInput.files[0]);
                $.ajax({
                    url:"batch_add_product.vpage",
                    type:"POST",
                    async:false,
                    processData:false,
                    contentType: false,
                    data: formData,
                    success:function(result){
                        if(result.success){
                            alert("保存成功!");
                            window.location.reload();
                        }else{
                            alert("保存失败!" + result.info);
                        }
                    }
                })
            }
        });

        $(document).on('click', '.delete-btn', function () {
            if (!confirm("确定删除商品吗？")) {
                return;
            }
            var $this = $(this);
            $.post('deleteproduct.vpage', {id: $this.attr("data-id")}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("a#upload-btn").click(function(){
            $("#upload-dialog").modal("show");
        });

    });
</script>
</@layout_default.page>