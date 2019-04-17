<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='绘本馆卡片管理' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div id="main_container" class="span9">
    <legend>
        <strong>绘本馆卡片管理</strong>
    </legend>
    <form id="config-query" class="form-horizontal" method="get" action="list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>状态&nbsp;
                    <select id="type" name="isOnLine">
                        <option value="">全部</option>
                        <option value="1" <#if isOnLine == 1>selected</#if>>已上线</option>
                        <option value="2"<#if isOnLine == 2>selected</#if>>未上线</option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    <input id="title" name="query_title" type="text" value="${title!''}" placeholder="套系名称"
                           style="width: 100px">
                </label>
            </li>
            <li>
                <button type="button" class="btn btn-primary" id="searchBtn">查 询</button>
            </li>
            <li>
                <#--<a href="add.vpage" class="btn btn-primary" id="addPictureBookCard">新建</a>-->
                <button type="button" class="btn btn-primary" id="addPictureBookCard">新 建</button>
            </li>
        </ul>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="220px">序号</th>
                        <th>主题套系</th>
                        <th>卡片数</th>
                        <th>创建人</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                         <#if content?? && content?size gt 0>
                            <#list content as  config>
                            <tr>
                                <td>${config.id!''}</td>
                                <td>${config.name!''}</td>
                                <td>${config.cardNum!''}</td>
                                <td>${config.creator!''}</td>
                                <td>
                                    <#if config.isOnLine == 1>
                                        已上线
                                    <#else>
                                        已下线
                                    </#if>
                                </td>
                                <td>
                                    <#if config.isOnLine == 1>
                                        <button class="btn btn-default" onclick="modify('${config.id!''}',1)">查看</button>
                                        <#--<button class="btn btn-success" onclick="changeStatus('${config.id!''}',0)">下线
                                        </button>-->
                                    <#else>
                                        <button class="btn btn-success" onclick="modify('${config.id!''}',0)">编辑</button>
                                        <button class="btn btn-success" onclick="changeStatus('${config.id!''}',1)">上线
                                        </button>
                                        <#--<button disabled="disabled" class="btn btn-default"
                                                onclick="changeStatus('${config.id!''}',0)">下线
                                        </button>-->
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                         <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
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
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog" style="width: 500px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">新建主题套系</h3>
            </div>
            <div class="modal-body" style="overflow: auto;max-height: 500px;">
                <form id="add-subject-frm" action="subject/save.vpage" method="post" role="form">
                    <div class="form-group has-feedback">
                        <span style="font-size: 14px;"><span style="color: red">*</span>主题套系名称</span>
                        <input class="form-control" type="text" id="pb_title" name="subject_name"
                               style="margin:10px 0 0 78px"
                               maxlength="20" data-error="请填写主题套系名称" required>
                        <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
                        <div class="help-block with-errors"></div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="cancel_button" type="button" class="btn btn-default">取消</button>
                <button id="save-subject-submit" type="submit" class="btn btn-primary">提交</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#commodity-query").submit();
    }

    $("#searchBtn").on('click', function () {
        $("#pageNum").val(1);
        $("#config-query").submit();
    });

    $("#addPictureBookCard").on('click', function () {
        showSubjectDialog();
    });

    $("button#cancel_button").click(function () {
        $("#add_dialog").modal("hide");
        $("#add_dialog #pb_title").val("");
    });

    $("button#save-subject-submit").click(function () {
        var frm = $("form#add-subject-frm");
        var postData = {};
        $.each($("input:text", frm), function (index, field) {
            var _f = $(field);
            if(_f.prop("required")){
                if(_f.val()===''){
                    alert("请填写全部必填项");
                    flag = false;
                    return false;
                }
            }
            postData[_f.attr("name")] = _f.val();
        });
        $.post("subject/save.vpage", postData,
                function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");
                        $("form#add-subject-frm")[0].reset();
                        alert("保存成功");
                        window.location = "add.vpage?subjectId="+data.subjectId+"&subjectName="+data.subjectName;
                    }
                    else
                        alert(data.info);
                }
        );
    });
    function hideSubjectDialog() {
        $("#add_dialog").modal("hide");;
    }
    function showSubjectDialog() {
        $("#add_dialog").modal("show");
    }
    function modify(id,isOnline) {
        if (id === '') {
            alert("参数错误");
        }
        window.location.href = "detail.vpage?subjectId="+id+"&isOnline="+isOnline;
    }

    function changeStatus(id, enable) {
        if (id === '' || enable === '') {
            alert("参数错误");
        }
        $.ajax({
            type: "post",
            url: "enabled.vpage",
            data: {
                subjectId: id,
                enabled: enable
            },
            success: function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert("操作失败");
                }
            }
        });
    }
</script>
</@layout_default.page>