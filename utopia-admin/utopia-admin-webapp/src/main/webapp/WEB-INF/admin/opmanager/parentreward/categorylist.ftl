<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='家长奖励类型管理' page_num=9>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>

<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>

<div id="main_container" class="span9">
    <legend>
        <strong>家长奖励类型管理</strong>
        <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right" id="add-category-btn">添加奖励类型
        </button>
        <form id="category-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/opmanager/parentreward/categorylist.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        </form>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="display: none">ID</th>
                        <th>KEY</th>
                        <th>类型名称</th>
                        <th>类型icon</th>
                        <th>排序</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if categoryPage?? && categoryPage.content?? >
                            <#list categoryPage.content as category >
                            <tr>
                                <td style="display: none">${category.id!}</td>
                                <td>${category.key!}</td>
                                <td>${category.title!}</td>
                                <td>
                                    <img src="${category.iconUrl!}" width="60" style="height: 60px"/>
                                </td>
                                <td>${category.rank}</td>
                                <td>
                                    <button name="show-detail" class="btn btn-success">编辑</button>
                                    <#if category.disabled?? && category.disabled>
                                        <button id="online" class="btn btn-success">上线</button>
                                    <#else >
                                        <button id="offline" class="btn btn-danger">下线</button>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>

<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加/编辑奖励类型</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="add-category-frm" action="save.vpage" method="post" role="form">
                    <div class="form-group" style="height:800px;display: none">
                    <label class="col-sm-2 control-label"><strong>奖励类型ID</strong></label>
                    <div class="controls">
                        <input type="text" id="id" class="form-control" maxlength="20">
                    </div>
                    </div>
                    <div class="form-group">
                        <label class="">KEY</label>
                        <input class="form-control" type="text" id="key" maxlength="20" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">奖励名称</label>
                        <input class="form-control" type="text" id="title" maxlength="20" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">图片</label>
                        <input type="file" id="imageSquare">
                    </div>
                    <div class="form-group">
                        <label class="">排序</label>
                        <input class="form-control" type="text" id="rank" maxlength="20" required>
                        <div class="help-block with-errors"></div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-edit-btn">确认</button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#category-query").submit();
    }

    $(function () {
        $("button#add-category-btn").click(function() {
            // 清空
            mapForm(function (field, isCheck) {
                if (isCheck)
                    field.attr("checked", false);
                else
                    field.val('');
            });

            $("#add_dialog").modal("show");
        });

        function mapForm(func) {
            var frm = $("form#add-category-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("form#add-category-frm").on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // 校验不通过
                return;
                // handle the invalid form...
            } else {
                e.preventDefault();

                $.post("addcategory.vpage", {
                    "id": $("input#id").val(),
                    "key": $("input#key").val(),
                    "title": $("input#title").val(),
                    "rank": $("input#rank").val()
                }, function(data) {
                    if(data.success) {
                        $("#add_dialog").modal("hide");
                        var formData = new FormData();
                        var imageInput = $('#imageSquare')[0];
                        if (imageInput.files.length > 0) {
                            formData.append('inputFile', imageInput.files[0]);
                            formData.append('id', data.id);
                            formData.append('type', "category");
                            $.ajax({
                                url: "uploadimg.vpage",
                                type: "POST",
                                processData: false,
                                contentType: false,
                                data: formData,
                                async:false
                            })
                        }
                        alert("添加成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#confirm-edit-btn").click(function () {
            var frm = $("form#add-category-frm");
            frm.submit();
        });

        $("button[name=show-detail]").click(function () {
            var $this = $(this);
            var categoryId = $this.parent().siblings().first().html();
            $.get("getcategory.vpage", {categoryId : categoryId}, function (data) {
                if (data.success) {
                    $("#add_dialog").modal("show");
                    var category = data.category;
                    mapForm(function (f, isCheck) {
                        if (isCheck) {
                            f.prop("checked", category[f.attr("id")]);
                        } else
                            f.val(category[f.attr("id")]);
                    });

                    $("form#add-category-frm").validator('validate');
                } else {
                    alert(data.info);
                }
            })
        });

        $(document).on('click', '#online', function () {
            var $this = $(this);
            var id = $this.parent().siblings().first().html();
            if (confirm("确定上线？")) {
                $.post("onlinecategory.vpage", {id: id}, function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                })
            }
        });
        $(document).on('click', '#offline', function () {
            var $this = $(this);
            var id = $this.parent().siblings().first().html();
            if (confirm("确定下线？")) {
                $.post("offlinecategory.vpage", {id: id}, function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                })
            }
        });
    });
</script>

</@layout_default.page>