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
        <legend>分类管理</legend>
        <ul class="inline">
            <li>
                <label>类型：<select id="categoryType" name="categoryType">
                    <#if types?has_content>
                        <#list types as type>
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
            <li>
                <button class="btn btn-primary" id="submit_select">查询</button>
                <button class="btn btn-success submit_add" data-id="">添加</button>
            </li>
        </ul>
    </fieldset>

    <div id="contentBox"></div>
    <div id="add_product_dialog" class="modal hide fade"></div>
</div>

<script type="text/html" id="contentBox_tem">
    <fieldset>
        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th>ID</th>
                    <th>分类名称</th>
                    <th>分类编码</th>
                    <th>类型</th>
                    <th>老师可见</th>
                    <th>学生可见</th>
                    <th>小学可见</th>
                    <th>中学可见</th>
                    <th>是否显示</th>
                    <th>排序</th>
                    <th>操作</th>
                </tr>
                <% for(var i in content) { %>
                    <tr>
                        <td><%=content[i].id%></td>
                        <td><%=content[i].categoryName%></td>
                        <td><%=content[i].categoryCode%></td>
                        <td><% if (content[i].productType == 'JPZX_SHIWU') {%>实物<% } else { %>体验<% } %></td>
                        <td><% if (content[i].teacherVisible) {%>可见<% } else { %>不可见<% } %></td>
                        <td><% if (content[i].studentVisible) {%>可见<% } else { %>不可见<% } %></td>
                        <td><% if (content[i].primaryVisible) {%>是<% } else { %>否<% } %></td>
                        <td><% if (content[i].juniorVisible) {%>是<% } else { %>否<% } %></td>
                        <td><% if (content[i].display) {%>是<% } else { %>否<% } %></td>
                        <td><%=content[i].displayOrder%></td>
                        <td>
                            <a href="#" data-id="<%=content[i].id%>" class="btn btn-primary submit_add">编辑</a>
                            <a href="#" data-id="<%=content[i].id%>" class="btn btn-danger delete">删除</a>
                        </td>
                    </tr>
                <% } %>
            </table>
        </div>
    </fieldset>
</script>

<script type="text/html" id="add_product_dialog_tem">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        <h3>添加分类</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>分类名称</dt>
                    <dd><input id="categoryName_add" <% if(content) { %> value="<%=content.categoryName%>" <% } %> type="text"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>分类编码</dt>
                    <dd><input id="categoryCode_add" <% if(content) { %> value="<%=content.categoryCode%>" <% } %> type="text"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>类型</dt>
                    <dd>
                        <select id="categoryType_add">
                            <#if types?has_content>
                                <#list types as type>
                                    <option value="${type.name()!}" <% if(content && content.productType == "${type.name()!}") { %> selected <% } %> >${type.getDescription()!}</option>
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
                        <% if(content) { %>
                            <option value="true" <% if(content.teacherVisible) { %>selected<% } %> >可见</option>
                            <option value="false" <% if(!content.teacherVisible) { %>selected<% } %> >不可见</option>
                        <% } else { %>
                            <option value="true">可见</option>
                            <option value="false">不可见</option>
                        <% } %>
                    </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学生可见</dt>
                    <dd><select id="studentVisible_add">
                        <% if(content) { %>
                            <option value="true" <% if(content.studentVisible) { %>selected<% } %> >可见</option>
                            <option value="false" <% if(!content.studentVisible) { %>selected<% } %> >不可见</option>
                        <% } else { %>
                            <option value="true">可见</option>
                            <option value="false">不可见</option>
                        <% } %>
                    </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>是否小学可见</dt>
                    <dd>
                        <select id="display_primary">
                            <% if(content) { %>
                            <option value="true" <% if(content.primaryVisible) { %>selected<% } %> >是</option>
                            <option value="false" <% if(!content.primaryVisible) { %>selected<% } %> >否</option>
                            <% } else { %>
                            <option value="true">是</option>
                            <option value="false">否</option>
                            <% } %>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>是否中学可见</dt>
                    <dd>
                        <select id="display_junior">
                            <% if(content) { %>
                            <option value="true" <% if(content.juniorVisible) { %>selected<% } %> >是</option>
                            <option value="false" <% if(!content.juniorVisible) { %>selected<% } %> >否</option>
                            <% } else { %>
                            <option value="true">是</option>
                            <option value="false">否</option>
                            <% } %>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>是否显示</dt>
                    <dd>
                        <select id="display_add">
                            <% if(content) { %>
                                <option value="true" <% if(content.display) { %>selected<% } %> >是</option>
                                <option value="false" <% if(!content.display) { %>selected<% } %> >否</option>
                            <% } else { %>
                                <option value="true">是</option>
                                <option value="false">否</option>
                            <% } %>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>排序</dt>
                    <dd><input id="displayOrder_add" <% if(content) { %> value="<%=content.displayOrder%>" <% } %> type="text"/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="add_category_button" class="btn btn-primary" data-id="<% if (content) { %><%=content.id%><% } %>">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

</script>

<script>
    $(function () {
        var categoryMap = {};

        $('#submit_select').on('click', function () {
            var type = $("#categoryType").find('option:selected').val();
            var role = $("#roles").find('option:selected').val();

            $.post("/reward/category/categories.vpage", {categoryType: type, roles: role}, function (data) {
                $('#contentBox').html(template("contentBox_tem", {content: data.categoryList}));
                for (var i = 0, list = data.categoryList; i < list.length; i++) {
                    categoryMap[list[i].id] = list[i];
                }
            });
        });

        $(document).on('click', '.submit_add', function () {
            var id = $(this).data('id');
            $('#add_product_dialog').html(template("add_product_dialog_tem", {content: categoryMap[id]})).modal();
        });

        $(document).on("click", "#add_category_button", function () {
            $.ajax({
                type: "post",
                url: "addcategory.vpage",
                data: {
                    categoryName: $("#categoryName_add").val(),
                    categoryCode: $("#categoryCode_add").val(),
                    categoryType: $("#categoryType_add").val(),
                    teacherVisible: $("#teacherVisible_add").val(),
                    studentVisible: $('#studentVisible_add').val(),
                    display: $('#display_add').val(),
                    primaryVisible: $('#display_primary').val(),
                    juniorVisible: $('#display_junior').val(),
                    displayOrder: $('#displayOrder_add').val(),
                    categoryId: $(this).data('id')
                },
                success: function (data) {
                    if (data.success) {
                        alert("添加成功");
                        $('#add_product_dialog').modal('hide');
                        $('#submit_select').click();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $(document).on("click", ".delete", function () {
            var $this = $(this);
            if (!confirm('确定要删除选中的分类？')) {
                return false;
            }
            var categoryId = $(this).data("id");
            $.ajax({
                type: "post",
                url: "deletecategory.vpage",
                data: {
                    categoryId: categoryId
                },
                success: function (data) {
                    if (data.success) {
                        $this.closest('tr').remove();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });
</script>
</@layout_default.page>