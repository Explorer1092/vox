<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='黑白名单' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css" rel="stylesheet"/>

<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>
<div id="main_container" class="span9">
    <legend>
        代收老师白名单
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <button id="add_record_btn" type="button" class="btn btn-info">增加</button>
                <table class="table table-striped table-bordered">
                    <tr>
                        <td>学校Id</td>
                        <td>学校名称</td>
                        <td>老师Id</td>
                        <td>老师名称</td>
                        <td>创建日期</td>
                        <td>描述</td>
                        <td>操作</td>
                    </tr>
                    <#if receivers?? >
                        <#list receivers as receiver >
                            <tr>
                                <td>${receiver.id!}</td>
                                <td>${receiver.schoolName!}</td>
                                <td>${receiver.teacherId!}</td>
                                <td>${receiver.teacherName!}</td>
                                <td>${receiver.date!}</td>
                                <td>${receiver.description!}</td>
                                <td>
                                    <a href="javascript:void(0);" name="remove" remove="${receiver.id!}">移除</a>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- 添加的窗口 -->
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×
                </button>
                <h3 class="modal-title">添加白名单</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <div class="tab-content">
                    <div class="tab-pane fade active in" role="tabpanel" id="base"
                         aria-labelledby="base-tab">
                        <form id="add-record-frm" action="add.vpage" method="post" role="form">
                            <div class="form-group">
                                <label class="">学校ID</label>
                                <input class="form-control" type="text" id="schoolId"
                                       maxlength="15" pattern="[0-9]*"
                                       data-error="必须是数字" data-ispositivte="1" required>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">老师ID</label>
                                <input class="form-control" type="text" id="teacherId"
                                       maxlength="15" pattern="[0-9]*"
                                       data-error="必须是数字" data-ispositivte="1" required>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">描述</label>
                                <textarea class="form-control" id="description" cols="20"></textarea>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" id="confirm-edit-btn">确认</button>
            </div>
        </div>
    </div>
</div>
<style type="text/css">
    #add_dialog #add-record-frm .form-group{
        padding: 0 60px;
    }
    #add_dialog #add-record-frm .form-group label{
        display: inline-block;
        width: 100px;
        font-size:16px;
        text-align: right;
    }
    #add_dialog #add-record-frm .form-group input,
    #add_dialog #add-record-frm .form-group textarea{
        width: 200px;
        max-width: 200px;
    }

    .list-unstyled{
        margin-left: 100px;
    }
    .list-unstyled li{
        list-style:  none;
        color: #ff0000;
        padding-left: 5px;
    }


</style>
<script type="text/javascript">
    $(function(){
        $("#add_record_btn").click(function () {
            $("input,textarea[class=form-control]").val("");
            $("#add_dialog").modal("show");
        });

        $("#confirm-edit-btn").click(function () {
            var frm = $("form#add-record-frm");
            frm.submit();
        });

        $("form#add-record-frm").validator({
            custom: {
                // 查检正整数的区域
                ispositivte: function($e1) {
                    if ($e1.val() <= 0) {
                        return "该字段必须为数字!";
                    }
                }
            }
        }).on('submit', function(e) {
            if (!e.isDefaultPrevented()) {
                e.preventDefault();
                var frm = $("form#aadd-record-frm");
                $.post("add.vpage", {
                    schoolId: $("input#schoolId").val(),
                    teacherId: $("input#teacherId").val(),
                    description: $("textarea#description").val()
                }, function(data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");
                        alert("保存成功!");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
        $("a[name=remove]").click(function () {
            if (!confirm("确定移除代收老师吗？")) {
                return;
            }
            var id = $(this).attr("remove");
            debugger;
            $.post('remove.vpage', {schoolId: id}, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });

        });

    });
</script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-prompts-alert.js"></script>
</@layout_default.page>