<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='家长奖励项管理' page_num=9>
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
        <strong>家长奖励项管理</strong>
        <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right" id="add-item-btn">添加奖励项
        </button>
        <form id="item-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/opmanager/parentreward/itemlist.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        </form>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well" style="font-size: 12px;">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="display: none">ID</th>
                        <th >奖励项KEY</th>
                        <th>类型名称</th>
                        <th>业务</th>
                        <th>学科</th>
                        <th>奖励项名称</th>
                        <th>奖励项等级名称</th>
                        <th>奖品类型</th>
                        <th style="width: 20px">数量</th>
                        <th style="width: 20px">排序</th>
                        <th style="width: 180px">跳转地址</th>
                        <th style="width: 180px">发奖页面地址</th>
                        <th style="width: 30px">发放过期时间</th>
                        <th style="width: 30px">领取过期时间</th>
                        <th style="width: 77px">icon</th>
                        <th style="width: 77px">level_icon</th>
                        <th>颜色</th>
                        <th>说明</th>
                        <th>是否发送push</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if itemPage?? && itemPage.content?? >
                            <#list itemPage.content as item >
                            <tr>
                                <td style="display: none">${item.id!}</td>
                                <td>${item.key!}</td>
                                <td>${item.categoryTitle!}</td>
                                <td>${item.business!}</td>
                                <td>${item.subject!}</td>
                                <td>${item.title!}</td>
                                <td>${item.levelTitle!}</td>
                                <td>${item.type!0}</td>
                                <td>${item.count!0}</td>
                                <td>${item.rank!0}</td>
                                <td>${item.redirectUrl!}</td>
                                <td>${item.secondaryPageUrl!}</td>
                                <td>${item.sendExpireDays!0}</td>
                                <td>${item.receiveExpireDays!0}</td>
                                <td>
                                    <img src="${item.icon!}" width="60" style="height: 60px"/>
                                </td>
                                <td>
                                    <img src="${item.levelIcon!}" width="60" style="height: 60px"/>
                                </td>
                                <td>${item.color!}</td>
                                <td>${item.description!}</td>
                                <td><#if item.sendGeneratePush?? && item.sendGeneratePush>发送<#else >不发送</#if></td>
                                <td>
                                    <button name="show-detail" class="btn btn-success">编辑</button>
                                    <#if item.disabled?? && item.disabled>
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
                <h3 class="modal-title">添加/编辑奖励项</h3>
            </div>
            <div class="modal-body" style="overflow: auto;max-height: 480px;">
                <form id="add-item-frm" action="save.vpage" method="post" role="form">
                    <div class="form-group" style="height:800px;display: none">
                    <label class="col-sm-2 control-label"><strong>奖励项ID</strong></label>
                    <div class="controls">
                        <input type="text" id="id" class="form-control" maxlength="20">
                    </div>
                    </div>
                    <div class="form-group">
                        <label class="">奖励项KEY</label>
                        <input class="form-control" type="text" id="key" maxlength="30" required>
                    </div>
                    <div class="form-group">
                        <label class="">奖励类型</label>
                        <#if categoryMap?has_content>
                            <select class="form-control" id="categoryId">
                                <#list categoryMap?keys as key>
                                    <option value="${categoryMap[key]}">${key}</option>
                                </#list>
                            </select>
                        </#if>
                    </div>
                    <div class="form-group">
                        <label class="">业务</label>
                        <#if businessMap?has_content>
                            <select class="form-control" id="business">
                                <option value="">无</option>
                                <#list businessMap?keys as key>
                                    <option value="${key}">${businessMap[key]}</option>
                                </#list>
                            </select>
                        </#if>
                    </div>
                    <div class="form-group">
                        <label class="">学科</label>
                        <#if subjectMap?has_content>
                            <select class="form-control" id="subject">
                                <option value="">无</option>
                                <#list subjectMap?keys as key>
                                    <option value="${key}">${subjectMap[key]}</option>
                                </#list>
                            </select>
                        </#if>
                    </div>
                    <div class="form-group">
                        <label class="">奖励项名称</label>
                        <input class="form-control" type="text" id="title" maxlength="30" required>
                    </div>
                    <div class="form-group">
                        <label class="">奖励项等级名称</label>
                        <input class="form-control" type="text" id="levelTitle" maxlength="30" required>
                    </div>
                    <div class="form-group">
                        <label class="">奖品类型</label>
                        <#if itemTypeMap?has_content>
                            <select class="form-control" id="type">
                                <#list itemTypeMap?keys as key>
                                    <option value="${key}">${itemTypeMap[key]}</option>
                                </#list>
                            </select>
                        </#if>
                    </div>
                    <div class="form-group">
                        <label class="">数量</label>
                        <input class="form-control" type="text" id="count" maxlength="20">
                    </div>
                    <div class="form-group">
                        <label class="">排序</label>
                        <input class="form-control" type="text" id="rank" maxlength="20">
                    </div>
                    <div class="form-group">
                        <label class="">跳转链接</label>
                        <input class="form-control" type="text" id="redirectUrl">
                    </div>
                    <div class="form-group">
                        <label class="">发奖页面链接</label>
                        <input class="form-control" type="text" id="secondaryPageUrl">
                    </div>
                    <div class="form-group">
                        <label>发放过期时间</label>
                        <input class="form-control" type="text" id="sendExpireDays">
                    </div>
                    <div class="form-group">
                        <label>领取过期时间</label>
                        <input class="form-control" type="text" id="receiveExpireDays">
                    </div>
                    <div class="form-group">
                        <label class="">分数图片</label>
                        <input type="file" id="imageSquare">
                    </div>
                    <div class="form-group">
                        <label class="">等级图片</label>
                        <input type="file" id="imageSquareLevel">
                    </div>
                    <div class="form-group">
                        <label class="">颜色</label>
                        <input class="form-control" type="text" id="color" maxlength="20">
                    </div>
                    <div class="form-group">
                        <label class="">说明</label>
                        <input class="form-control" type="text" id="description">
                    </div>
                    <div>
                        <input type="checkbox" id="sendGeneratePush">生成奖励后发送push
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

            $("#add_dialog").modal("show");
            $("#key").attr("disabled", false);
            $("#type").attr("disabled", false);
        });

        function mapForm(func) {
            var frm = $("form#add-item-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("form#add-item-frm").validator({

        }).on('submit', function (e) {
            if (e.isDefaultPrevented()) {
                // 校验不通过
                return;
                // handle the invalid form...
            } else {
                e.preventDefault();
                $.post("additem.vpage", {
                    "id": $("input#id").val(),
                    "key": $("input#key").val(),
                    "categoryId": $("#categoryId option:selected").val(),
                    "business": $("#business option:selected").val(),
                    "subject": $("#subject option:selected").val(),
                    "title": $("input#title").val(),
                    "levelTitle": $("input#levelTitle").val(),
                    "type": $("#type option:selected").val(),
                    "count": $("input#count").val(),
                    "rank": $("input#rank").val(),
                    "redirectUrl": $("input#redirectUrl").val(),
                    "secondaryPageUrl": $("#secondaryPageUrl").val(),
                    "sendExpireDays": $("input#sendExpireDays").val(),
                    "receiveExpireDays": $("input#receiveExpireDays").val(),
                    "sendGeneratePush": $("#sendGeneratePush").is(":checked"),
                    "color":$("#color").val(),
                    "desc": $("#description").val()
                }, function(data) {
                    if(data.success) {
                        $("#add_dialog").modal("hide");
                        var formData = new FormData();
                        var imageInput = $('#imageSquare')[0];
                        if (imageInput.files.length > 0) {
                            formData.append('inputFile', imageInput.files[0]);
                            formData.append('id', data.id);
                            formData.append('type', "itemScore");
                            $.ajax({
                                url: "uploadimg.vpage",
                                type: "POST",
                                processData: false,
                                contentType: false,
                                data: formData,
                                async:false
                            })
                        };
                        var levelFormData = new FormData();
                        var levelImageInput = $('#imageSquareLevel')[0];
                        if (levelImageInput.files.length > 0) {
                            levelFormData.append('inputFile', levelImageInput.files[0]);
                            levelFormData.append('id', data.id);
                            levelFormData.append('type', "itemLevel");
                            $.ajax({
                                url: "uploadimg.vpage",
                                type: "POST",
                                processData: false,
                                contentType: false,
                                data: levelFormData,
                                async:false
                            })
                        };
                        alert("添加成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $("#confirm-edit-btn").click(function () {
            var frm = $("form#add-item-frm");
            frm.submit();
        });

        $("button[name=show-detail]").click(function () {
            var $this = $(this);
            var itemId = $this.parent().siblings().first().html();
            $.get("getitem.vpage", {itemId : itemId}, function (data) {
                if (data.success) {
                    $("#add_dialog").modal("show");
                    $("#key").attr("disabled", true);
                    var item = data.item;
                    mapForm(function (f, isCheck) {
                        if (isCheck) {
                            f.prop("checked", item[f.attr("id")]);
                        } else
                            f.val(item[f.attr("id")]);
                    });

                    $("form#add-item-frm").validator('validate');
                } else {
                    alert(data.info);
                }
            });
        });

        $(document).on('click', '#online', function () {
            var $this = $(this);
            var itemId = $this.parent().siblings().first().html();
            if (confirm("确定上线？")) {
                $.post("onlineitem.vpage", {itemId: itemId}, function (data) {
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
            var itemId = $this.parent().siblings().first().html();
            if (confirm("确定下线？")) {
                $.post("offlineitem.vpage", {itemId: itemId}, function (data) {
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