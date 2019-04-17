<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='活动管理' page_num=12>
<script type="text/javascript"
        src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/select2/select2.min.css"
      rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css"
      rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/select2/select2.full.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/validator.min.js"></script>

<link href="${requestContext.webAppContextPath}/public/js/kindeditor/themes/default/default.css"
      rel="stylesheet"/>
<#-- 使用已改过的 kindeditor-all.js-->
<#--<script src="${requestContext.webAppContextPath}/public/js/kindeditor/kindeditor-all.js"></script>-->
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>

<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">

<div id="main_container" class="span9">
    <legend>
        <strong>活动管理</strong>
        <button type="button" class="btn btn-primary" style="margin-bottom: 5px;float:right"
                id="add-activity-btn">添加活动
        </button>
        <form id="activity-query" class="form-horizontal" method="get"
              action="${requestContext.webAppContextPath}/reward/activity/index.vpage">
            <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        </form>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="90px">活动ID</th>
                        <th>活动名称</th>
                        <th>参加人数</th>
                        <th>目标金额</th>
                        <th>已筹金额</th>
                        <th>排序权重</th>
                        <th>状态</th>
                        <th>创建时间</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if activitiesPage?? && activitiesPage.content?? >
                            <#list activitiesPage.content as activity >
                            <tr>
                                <td>${activity.id!}</td>
                                <td>${activity.name!}</td>
                                <td>${activity.partakeNums!}</td>
                                <td>${activity.targetMoney!}</td>
                                <td>${activity.raisedMoney!}</td>
                                <td>${activity.orderWeights!}</td>
                                <td><#if activity.online>上线<#else >下线</#if></td>
                                <td>${activity.createDatetime!}</td>
                                <td>
                                    <button type="button" class="btn btn-default btn-xs"
                                            name="show-detail">编辑
                                    </button>
                                    <a name="uploadPhotoButton"
                                       href="imagelist.vpage?activityId=${activity.id!}"
                                       role="button"
                                       class="btn btn-primary" style="font-size: 12px;">图片</a>
                                    <#if activity.online>
                                        <button name="offline-btn" class="btn btn-danger">下线
                                        </button>
                                    <#else >
                                        <button name="online-btn" class="btn btn-success">上线
                                        </button>
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
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a>
                        </li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a>
                    </li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a>
                        </li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>

<!-- 添加编辑活动的窗口 -->
<div id="add_dialog" class="modal fade hide setActivity" aria-hidden="true" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×
                </button>
                <h3 class="modal-title">添加/编辑活动</h3>
            </div>
            <div class="modal-body" style="overflow: visible;max-height: 800px;">
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active">
                        <a href="#base" id="base-tab" role="tab" data-toggle="tab"
                           aria-controls="base" aria-expanded="true">基础信息</a>
                    </li>
                    <li role="presentation">
                        <a href="#detail" id="detail-tab" role="tab" data-toggle="tab"
                           aria-controls="detail" aria-expanded="true">详情</a>
                    </li>
                    <li role="presentation">
                        <a href="#progress" id="progress-tab" role="tab" data-toggle="tab"
                           aria-controls="progress" aria-expanded="true">进展</a>
                    </li>
                </ul>
                <div class="tab-content" id="activityTabContent">
                    <div class="tab-pane fade active in" role="tabpanel" id="base"
                         aria-labelledby="base-tab">
                        <form id="add-activity-frm" action="save.vpage" method="post" role="form">
                            <div class="form-group" style="height:800px;display: none">
                                <label class="col-sm-2 control-label"><strong>活动ID</strong></label>
                                <div class="controls">
                                    <input type="text" id="id">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="">活动名称</label>
                                <input class="form-control" type="text" id="name" maxlength="15"
                                       required>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">简介</label>
                                <input class="form-control" type="text" id="summary" maxlength="50">
                            </div>
                            <div class="form-group">
                                <label class="">模式</label>
                                <select class="form-control" id = "model">
                                    <option value="NONE">老项目</option>
                                    <option value="CLASS_ROOM">教室</option>
                                </select>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">目标金额</label>
                                <input class="form-control" type="text" id="targetMoney"
                                       maxlength="15" pattern="[0-9]*"
                                       data-error="必须是正整数" data-ispositivte="1" required>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">排序权重</label>
                                <input class="form-control" type="text" id="orderWeights"
                                       maxlength="15" pattern="[0-9]*"
                                       data-error="必须是整数" required>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">完成时间</label>
                                <input class="form-control" type="text" id="finishTimeStr" readonly>
                                <div class="help-block with-errors"></div>
                            </div>
                            <div class="form-group">
                                <label class="">订单封面</label>
                                <input type="file" id="imageSquare">
                            </div>
                        </form>
                    </div>
                    <div class="tab-pane fade" role="tabpanel" id="detail"
                         aria-labelledby="detail-tab">
                        <script id="description" name="description" type="text/plain"></script>
                        <#--<textarea class="form-control" id="description" cols="20"></textarea>-->
                    </div>
                    <div class="tab-pane fade" role="tabpanel" id="progress"
                         aria-labelledby="progress-tab">
                        <script id="progressDetail" name="progressDetail" type="text/plain"></script>
                        <#--<textarea class="form-control" id="progressDetail" rows="20"-->
                                  <#--cols="10"></textarea>-->
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

    function pagePost(pageNumber){
        $("#pageNum").val(pageNumber);
        $("#activity-query").submit();
    }

    $(function () {
        var currentActivityId = 0;
        var descriptionEditor = UE.getEditor('description', {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            initialFrameHeight: 450,
            autoHeightEnabled: false,
            autoFloatEnabled: true,
            elementPathEnabled:false,
            wordCount : false,
            maximumWords:1000,
            zIndex:1,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist','formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                '|', 'preview'
            ]]
        });
        var progressDetailEditor = UE.getEditor('progressDetail', {
            serverUrl: "/advisory/ueditorcontroller.vpage",
            initialFrameHeight: 450,
            autoHeightEnabled: false,
            autoFloatEnabled: true,
            elementPathEnabled:false,
            wordCount : false,
            maximumWords:1000,
            zIndex:1,
            fontsize: [16, 18, 20, 22, 24, 26, 28, 30, 32, 34],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', '|', 'forecolor', 'backcolor', 'insertorderedlist','formatmatch', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                '|', 'preview'
            ]]
        })


        $("#finishTimeStr").datetimepicker(
                {
                    language: "cn",
                    format: "yyyy-mm-dd",
                    autoclose: true,
                    minView: 2
                });

        $("button#add-activity-btn").click(function () {
            $("#add_dialog input#id").val(0);

            // 清空
            mapForm(function (field, isCheck) {
                if (isCheck) {
                    field.attr("checked", false);
                } else {
                    field.val('');
                }
            });

            $("#add_dialog").modal("show");

            descriptionEditor.ready(function () {
                descriptionEditor.setContent(activity.description.replace('<br/>', ''));
                // descriptionEditor.setHeight(600);
            });
            progressDetailEditor.ready(function () {

                progressDetailEditor.setContent(activity.progressDetail.replace('<br/>', ''));
                // progressDetailEditor.setHeight(600);
            });
        });

        function mapForm(func) {
            var frm = $("form#add-activity-frm");
            $.each($("input,textarea,select", frm), function (index, field) {
                var _f = $(field);
                func(_f, _f.attr("type") == "checkbox");
            });
        }

        $("form#add-activity-frm").validator(
                {
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
                var activityId = $("input#id", frm).val();

                $.post("activity.vpage", {
                    id: activityId,
                    name: $("input#name").val(),
                    targetMoney: $("input#targetMoney").val(),
                    description: descriptionEditor.getContent().replace(/\n/g, ''),
                    orderWeights: $("input#orderWeights").val(),
                    progressDetail: progressDetailEditor.getContent().replace(/\n/g, ''),
                    finishTime: $("input#finishTimeStr").val(),
                    model:$("select#model").val(),
                    summary:$("input#summary").val()
                }, function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");

                        // 放在这个地方，防止新建的时候，活动记录没生成就去更新
                        var formData = new FormData();
                        var imageInput = $('#imageSquare')[0];
                        if (imageInput.files.length > 0) {
                            formData.append('file', imageInput.files[0]);
                            formData.append('activityId', data.id);
                            $.ajax({
                                url: "uploadorderimage.vpage",
                                type: "POST",
                                async: false,
                                processData: false,
                                contentType: false,
                                data: formData
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
            var activityId = $this.parent().siblings().first().html();
            currentActivityId = activityId;

            $.get("activity.vpage", {activityId: activityId}, function (data) {
                if (data.success) {
                    $("#add_dialog").modal("show");

                    var activity = data.activity;
                    mapForm(function (f, isCheck) {
                        if (isCheck) {
                            f.prop("checked", activity[f.attr("id")]);
                        } else {
                            f.val(activity[f.attr("id")]);
                        }
                    });

                    $("form#add-activity-frm").validator('validate');

                    descriptionEditor.ready(function () {
                        descriptionEditor.setContent(activity.description.replace(/\n/g, '<p><br/></p>'));
                    });
                    progressDetailEditor.ready(function () {
                        progressDetailEditor.setContent(activity.progressDetail.replace(/\n/g, '<p><br/></p>'));
                    });

                    // descriptionEditor.html(activity.description);
                    // progressDetailEditor.html(activity.progressDetail);
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