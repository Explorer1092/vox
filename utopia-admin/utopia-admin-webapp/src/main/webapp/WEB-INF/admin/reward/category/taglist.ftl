<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=12>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {
        font: "arial";
    }

    .index {
        color: #0000ff;
    }

    .index, .item {
        font-size: 18px;
        font: "arial";
    }

    .warn {
        color: red;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>标签管理</legend>
        <ul class="inline">
            <form id="frm" action="" method="post">
                <li>
                    <label>级别：<select id="tagLevel" name="tagLevel">
                        <#if tagLevel?has_content>
                            <#list tagLevel as type>
                                <option value="${type.name()!}">${type.getDescription()!}</option>
                            </#list>
                        </#if>
                    </select>
                    </label>
                </li>
                <li>
                    <label>角色：<select id="roles" name="roles">
                        <option value="TEACHER">老师</option>
                        <option value="STUDENT">学生</option>
                    </select>
                    </label>
                </li>
            </form>
            <li>
                <button class="btn btn-primary" id="submit_select">查询</button>
                <button class="btn btn-success" id="submit_add">添加</button>
            </li>
        </ul>
    </fieldset>
    <div>
        <fieldset>
            <div>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th></th>
                        <th>ID</th>
                        <th>标签名称</th>
                        <th>级别</th>
                        <th>老师可见</th>
                        <th>学生可见</th>
                        <th>是否显示</th>
                        <th>排序</th>
                        <th>操作</th>
                    </tr>
                    <#if tagList?has_content>
                        <#list tagList as tag>
                            <tr>
                                <th><input name="tagId" type="checkbox" value="${tag.id!}"></th>
                                <td>${tag.id!}</td>
                                <td>${tag.tagName!}</td>
                                <td>
                                    <#if tag.tagLevel == 'ONE_LEVEL'>一级标签<#else>二级标签</#if>
                                </td>
                                <td><#if tag.teacherVisible>可见<#else>不可见</#if></td>
                                <td><#if tag.studentVisible>可见<#else>不可见</#if></td>
                                <td><#if tag.disabled>是<#else>否</#if></td>
                                <td>${tag.displayOrder!}</td>
                                <td>
                                    <button class="btn btn-primary" name="update_button">编辑</button>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
        </fieldset>
    </div>

    <div id="add_product_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>添加标签</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline" style="display: none">
                    <li>
                        <dt>ID</dt>
                        <dd><input id="tagId_add" type="text"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>名称</dt>
                        <dd><input id="tagName_add" type="text"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>级别</dt>
                        <dd>
                            <select id="tagLevel_add">
                                <#if tagLevel?has_content>
                                    <#list tagLevel as type>
                                        <option value="${type.name()!}">${type.getDescription()!}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>老师可见</dt>
                        <dd><select id="teacherVisible_add">
                            <option value="true">可见</option>
                            <option value="false">不可见</option>
                        </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学生可见</dt>
                        <dd><select id="studentVisible_add">
                            <option value="true">可见</option>
                            <option value="false">不可见</option>
                        </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>是否显示</dt>
                        <dd><select id="display_add">
                            <option value="true">是</option>
                            <option value="false">否</option>
                        </select>
                        </dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="add_category_button" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>

<script>

    $(function () {
        var submitSelect = $('#submit_select');

        submitSelect.on('click', function () {
            $('#frm').submit();
        });

        $('#submit_add').on('click', function () {
            // reset
            $("#tagId_add").val('');
            $("#tagName_add").val('');
            $("#tagLevel_add").val("ONE_LEVEL");
            $("#teacherVisible_add").val(true);
            $('#studentVisible_add').val(true);
            $('#display_add').val(true);

            $('#add_product_dialog').modal();
        });

        $("button[name='update_button']").on('click', function (e) {
            tds = $(e.target.parentNode.parentNode).find("td");

            $("#tagId_add").val(tds[0].innerText);
            $("#tagName_add").val(tds[1].innerText);

            levelStatus = tds[2].innerText.indexOf("二级标签") < 0 ? "ONE_LEVEL" : "TWO_LEVEL";
            teacherStatus = tds[3].innerText.indexOf("不可见") < 0 ? "true" : "false"; // 类型有坑
            studetnStatus = tds[4].innerText.indexOf("不可见") < 0 ? "true" : "false";
            showStatus = tds[5].innerText.indexOf("否") < 0 ? "true" : "false";

            $("#tagLevel_add").val(levelStatus);
            $('#teacherVisible_add').val(teacherStatus);
            $('#studentVisible_add').val(studetnStatus);
            $('#display_add').val(showStatus);

            $('#add_product_dialog').modal();
        });

        $("#add_category_button").on("click", function () {
            $.ajax({
                type: "post",
                url: "upserttag.vpage",
                data: {
                    tagId: $("#tagId_add").val(),
                    tagName: $("#tagName_add").val(),
                    tagLevel: $("#tagLevel_add").val(),
                    teacherVisible: $("#teacherVisible_add").val(),
                    studentVisible: $('#studentVisible_add').val(),
                    disabled: $('#display_add').val()
                },
                success: function (data) {
                    if (data.success) {
                        alert("添加成功");
                    } else {
                        alert(data.info);
                    }
                }
            });
        });


    });
</script>
</@layout_default.page>