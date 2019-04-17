<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='活动管理' page_num=12>
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
        <strong>特权管理</strong>
        <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right" id="add-activity-btn">添加特权
        </button>
        <form id="activity-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/reward/product/headwearlist.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        </form>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="90px">头饰ID</th>
                        <th>图片</th>
                        <th>头饰编码</th>
                        <th>名称</th>
                        <th>类型</th>
                        <th>来源</th>
                        <th>是否在空间显示</th>
                        <th>创建时间</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if headWearsPage?? && headWearsPage.content?? >
                            <#list headWearsPage.content as headWear >
                            <tr>
                                <td>${headWear.id!}</td>
                                <td>
                                    <img src="${headWear.img!}" width="60" style="height: 60px"/>
                                </td>
                                <td>${headWear.code!}</td>
                                <td>${headWear.name!}</td>
                                <td>${headWear.type!}</td>
                                <td>${headWear.origin!}</td>
                                <td><#if headWear.displayInCenter>显示<#else>不显示</#if></td>
                                <td>${headWear.createTime!}</td>
                                <td>
                                    <button name="show-detail" class="btn btn-success">编辑</button>
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

<!-- 添加编辑活动的窗口 -->
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h3 class="modal-title">添加/编辑特权</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <form id="add-activity-frm" action="save.vpage" method="post" role="form">
                    <div class="form-group" style="height:800px;display: none">
                        <label class="col-sm-2 control-label"><strong>特权ID</strong></label>
                        <div class="controls">
                            <input type="text" id="id" class="form-control" maxlength="20">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="">特权类型</label>
                        <select class="form-control" id="type">
                            <#if origins??>
                                <#list type as t>
                                    <option value="${t}">${t}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="">特权编码</label>
                        <input class="form-control" type="text" id="code" maxlength="20" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">特权名称</label>
                        <input class="form-control" type="text" id="name" maxlength="20" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">来源</label>
                        <select class="form-control" id="origin">
                            <#if origins??>
                                <#list origins as origin>
                                    <option value="${origin}">${origin}</option>
                                </#list>
                            </#if>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="">获取条件</label>
                        <input class="form-control" type="text" id="acquireCondition" maxlength="50" required>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <label class="">图片</label>
                        <input type="file" id="imageSquare">
                    </div>
                    <div class="form-group" style="margin-top: 20px;">
                        <div class="checkbox">
                            <label class=""><input type="checkbox" id="displayInCenter">是否在空间显示</label>
                        </div>
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

    .checkbox label {
        font-weight: 400;
    }

    .list-unstyled {
        padding-left: 0;
        list-style: none;
    }

    .help-block {
        display: block;
        margin-top: 5px;
        margin-bottom: 10px;
        color: #a94442;
    }

    .table td, .table th {
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }

    .form-checkbox {
        padding-top: 3px;
    }

    .left-compact-label {
        padding-top: 3px;
        width: 90px;
        float: left;
        text-align: left;
    }

    .admin-select {
        width: 410px;
    }

    .form-control {
        display: block;
        width: 95%;
        height: 34px;
        padding: 6px 12px;
        font-size: 14px;
        line-height: 1.42857143;
        color: #555;
        background-color: #fff;
        background-image: none;
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
        $("#activity-query").submit();
    }

    $(function () {
        var currentActivityId = 0;

        $("button#add-activity-btn").click(function () {
            $("#add_dialog input#id").val(0);

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
            var frm = $("form#add-activity-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("form#add-activity-frm").validator({
            custom: {
                // 查检正整数的区域
                ispositivte: function ($e1) {
                    if ($e1.val() <= 0) {
                        return "该字段必须为正整数!";
                    }
                }
            }
        }).on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // 校验不通过
                return;
                // handle the invalid form...
            } else {
                e.preventDefault();

                var frm = $("form#add-activity-frm");
                var headWearId = $("input#id", frm).val();

                $.post("headwear.vpage", {
                    id: headWearId,
                    code: $("input#code").val(),
                    name: $("input#name").val(),
                    origin: $("select#origin").val(),
                    type: $("select#type").val(),
                    displayInCenter: $("input#displayInCenter").is(':checked'),
                    acquireCondition:$("#acquireCondition").val()
                }, function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");

                        // 放在这个地方，防止新建的时候，活动记录没生成就去更新
                        var formData = new FormData();
                        var imageInput = $('#imageSquare')[0];
                        if (imageInput.files.length > 0) {
                            formData.append('file', imageInput.files[0]);
                            formData.append('headWearId', data.id);
                            $.ajax({
                                url: "uploadheadwearimg.vpage",
                                type: "POST",
                                processData: false,
                                contentType: false,
                                data: formData,
                                async:false
                            })
                        }

                        alert("保存成功!");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#confirm-edit-btn").click(function () {
            var frm = $("form#add-activity-frm");
            frm.submit();
        });

        $("button[name=show-detail]").click(function () {
            var $this = $(this);
            var headWearId = $this.parent().siblings().first().html();
            currentActivityId = headWearId;

            $.get("headwear.vpage", {headWearId: headWearId}, function (data) {
                if (data.success) {
                    $("#add_dialog").modal("show");

                    var headwear = data.headwear;
                    mapForm(function (f, isCheck) {
                        if (isCheck) {
                            f.prop("checked", headwear[f.attr("id")]);
                        } else
                            f.val(headwear[f.attr("id")]);
                    });

                    $("form#add-activity-frm").validator('validate');
                }
            });
        });

        $("button[name=online-btn]").click(function () {
            var $this = $(this);
            var activityId = $this.parent().siblings().first().html();

            updateOnlineStatus(activityId, true);
        });

        $("button[name=offline-btn]").click(function () {
            var $this = $(this);
            var activityId = $this.parent().siblings().first().html();

            updateOnlineStatus(activityId, false);
        });

        function updateOnlineStatus(activityId, status) {
            $.post("online.vpage", {status: status, activityId: activityId}, function (data) {
                if (data.success) {
                    alert("保存成功!");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        }

    });


</script>

</@layout_default.page>