<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='学习币类型管理页' page_num=9>
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
        <strong>学习币类型管理</strong>
        <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right" id="add-item-btn">添加类型
        </button>
        <form id="item-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/opmanager/cointype/list.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        </form>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>名称</th>
                            <th>奖励数量</th>
                            <th>操作类型</th>
                            <th>每周限制</th>
                            <th>每月限制</th>
                            <th>类型描述</th>
                            <th>是否人工添加</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#if typePage?? && typePage.content??>
                            <#list typePage.content as type>
                                 <tr>
                                     <td>${type.id}</td>
                                     <td>${type.name}</td>
                                     <td>${type.count}</td>
                                     <td>${type.opType}</td>
                                     <td>${type.weekLimitCount}</td>
                                     <td>${type.monthLimitCount}</td>
                                     <td>${type.desc}</td>
                                     <td><#if type.manual?? && type.manual>是<#else >否</#if></td>
                                     <td> <button name="show-detail" class="btn btn-success">编辑</button></td>
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
                    <li><a href="#" onclick="pagePost(${totalPage!})" title="Pre">尾页</a></li>
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
                <h3 class="modal-title">添加/编辑类型</h3>
            </div>
            <div class="modal-body" style="overflow: auto;max-height: 480px;">
                <form id="add-type-frm" action="save.vpage" method="post" role="form">
                    <div class="form-group" id="typeId">
                        <label class="">类型ID</label>
                        <input class="form-control" type="text" id="id" maxlength="20">
                    </div>
                    <div class="form-group">
                        <label class="">类型名称</label>
                        <input class="form-control" type="text" id="name" maxlength="20" required>
                    </div>
                    <div class="form-group">
                        <label class="">奖励数量</label>
                        <input class="form-control" type="text" id="count" maxlength="20" required>
                    </div>
                    <div class="form-group">
                        <label class="">操作类型</label>
                        <select class="form-control" id="opType">
                        <#if opTypes??>
                            <#list opTypes as opType>
                                <option value="${opType}">${opType}</option>
                            </#list>
                        </#if>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="">每周上限</label>
                        <input class="form-control" type="text" id="weekLimitCount" maxlength="20" placeholder="默认-1不限制">
                    </div>
                    <div class="form-group">
                        <label class="">每月上限</label>
                        <input class="form-control" type="text" id="monthLimitCount" maxlength="20" placeholder="默认-1不限制">
                    </div>
                    <div class="form-group">
                        <label class="">类型描述</label>
                        <input class="form-control" type="text" id="desc" maxlength="100">
                    </div>
                    <div>
                        <input type="checkbox" id="manual">是否人工发放
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

<style>
    label {
        font-weight: 700;
    }
    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }
    .form-control {
        display: block;
        width: 95%;
        height: 34px;
        padding: 6px 12px;
        font-size: 14px;
        line-height: 1.42857143;
        color: #555;
        background: #fff none;
        border: 1px solid #ccc;
        border-radius: 4px;
        -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
        box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
        -webkit-transition: border-color ease-in-out .15s, -webkit-box-shadow ease-in-out .15s;
        -o-transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
        transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
        margin-bottom: 0px;
    }
    ul, ol {
        padding: 0;
        margin: 0 0 0px 0px;
    }
</style>

<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#item-query").submit();
    }

    $(function () {
        $("#add-item-btn").on("click", function () {
            // 清空
            mapForm(function (field, isCheck) {
                if (isCheck)
                    field.attr("checked", false);
                else
                    field.val('');
            });
            $("#typeId").hide();
            $("#addType").val("insert");
            $("#add_dialog").modal("show");
            $("#id").attr("disabled", false);
        });

        function mapForm(func) {
            var frm = $("form#add-type-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("form#add-type-frm").validator({

        }).on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // 校验不通过
                return;
                // handle the invalid form...
            } else {
                e.preventDefault();
                $.post("addType.vpage", {
                    "id": $("input#id").val(),
                    "name": $("input#name").val(),
                    "count": $("input#count").val(),
                    "opType": $("#opType option:selected").val(),
                    "weekLimitCount": $("input#weekLimitCount").val(),
                    "monthLimitCount": $("input#monthLimitCount").val(),
                    "desc": $("input#desc").val(),
                    "manual": $("#manual").is(":checked")
                }, function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");
                        alert("保存类型成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                })
            }
        });

        $("#confirm-edit-btn").click(function () {
            var weekLimit = $("input#weekLimitCount").val();
            var monthLimit = $("input#monthLimitCount").val();
            if (Number(weekLimit) > Number(monthLimit)) {
                alert("每月限制不能小于每周限制!");
                return
            }
            var frm = $("form#add-type-frm");
            frm.submit();
        });

        $("button[name=show-detail]").click(function () {
            var $this = $(this);
            var typeId = $this.parent().siblings().first().html();
            $.get("getType.vpage", {id: typeId}, function (data) {
                if (data.success) {
                    $("#addType").val("edit");
                    $("#add_dialog").modal("show");
                    $("#id").attr("disabled", true);
                    var type = data.type;
                    mapForm(function (f, isCheck) {
                        if (isCheck) {
                            f.prop("checked", type[f.attr("id")]);
                        } else
                            f.val(type[f.attr("id")]);
                    });
                    $("form#add-type-frm").validator('validate');
                } else {
                    alert(data.info)
                }
            })
        })
    })


</script>

</@layout_default.page>