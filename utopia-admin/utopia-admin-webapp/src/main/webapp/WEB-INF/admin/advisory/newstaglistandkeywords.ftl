<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-内容类型设置' page_num=13>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">
    <fieldset>
        <legend>内容类型设置</legend>
    </fieldset>

    <fieldset>
        <div>
            <button class="btn btn-primary" id="add_parent_tag">添加一级内容类型</button>
            <button class="btn btn-danger" id="refreshtags" style="float: right">刷新标签</button>
        </div>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>


<div id="edit_dialog_1" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>内容类型设置</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <input id="mode" type="hidden" value="add">
        <dl>
            <dt>一级类型id</dt>
            <dd>
                <input type="text" id="parentTagId" name="parentTagId"/>
            </dd>
        </dl>
        <dl>
            <dt>一级类型名称</dt>
            <dd>
                <input type="text" id="parentTagName" name="parentTagName"/>
            </dd>
        </dl>
        <dl>
            <dt>二级类型id</dt>
            <dd>
                <input type="text" id="secondTagId" name="secondTagId"/>
            </dd>
        </dl>
        <dl>
            <dt>二级类型名称</dt>
            <dd>
                <input type="text" id="secondTagName" name="secondTagName"/>
            </dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取消</button>
        <button class="btn btn-primary" id="save_btn_1">保存</button>
    </div>
</div>


<div id="edit_dialog_2" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>关键词设置</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <input id="mode" type="hidden" value="add">
        <dl>
            <dt>添加关键词</dt>
            <dd>
                <input type="text" data-bind="value:inputKeyword"/>
                <input type="hidden" id="keyWordSecondTagId" name="keyWordSecondTagId"/>
                <input type="hidden" id="keyWordFirstTagId" name="keyWordFirstTagId"/>
                <button class="btn btn-primary" id="save_btn_2">保存</button>

            </dd>
        </dl>
        <div data-bind="foreach:currentKeywords">
            <p>what</p>
        </div>
        <dl>
            <dt>已有关键词</dt>
            <dd>
                <div id="keyWordsBox">
                    <div data-bind="foreach:currentKeywords">
                        <p>what</p>
                    <#--<input type="checkbox" id="keyWord" name="keyWord" data-bind="value:$data.id"/><span data-text="text:$data.keyword"></span>-->
                    </div>
                </div>
            </dd>
        </dl>
    </div>
</div>

<script type="text/html" id="keyWordCheckBox">
    <%if(keyWords.length > 0){%>
    <%for(var i = 0; i < keyWords.length;i++){%>
    <input type="checkbox" id="keyWord" name="keyWord" value="<%=keyWords[i].id%>"/>
    <span><%==keyWords[i].keyword%></span>
    <%}%>
    <%}%>
    <button class="btn btn-danger delete-source" id="del_slot_btn">删除</button>
</script>

<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>序号</th>
            <th>一级内容类型</th>
            <th>二级内容类型</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td><%=i+1%></td>
            <td id="ptn_<%=content[i].parentTagId%>"><%=content[i].parentTagName%></td>
            <td id="stn_<%=content[i].parentTagId%>"><label>
                <select id="stn_<%=content[i].parentTagId%>" name="secondTagName">
                    <%for(var j = 0; j < content[i].childList.length;j++){%>
                    <option value="<%=content[i].childList[j].id%>"><%=content[i].childList[j].tagName%></option>
                    <%}%>
                </select>
            </label>
            </td>
            <td data-_id="<%=content[i].id%>">
                <a class="btn btn-primary edit_parent" data-id="<%=content[i].parentTagId%>">编辑一级类型</a>
                <a class="btn btn-primary edit_second" data-id="<%=content[i].parentTagId%>">编辑二级类型</a>
                <a class="btn btn-primary add_second" data-id="<%=content[i].parentTagId%>">添加二级类型</a>
                <button class="btn btn-danger del_parent" data-id="<%=content[i].parentTagId%>">删除一级类型</button>
                <button class="btn btn-danger del_second" data-id="<%=content[i].parentTagId%>">删除二级类型</button>
                <a class="btn btn-primary edit_key" data-id="<%=content[i].parentTagId%>">设置二级类型关键词</a>

            </td>
        </tr>
        <%}%>
        <%}else{%>
        <tr>
            <td colspan="8">暂无数据</td>
        </tr>
        <%}%>
    </table>
</script>

<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>

<script type="text/javascript">

    function getArticleList(page) {
        var postData = {
            currentPage: page
        };

        $.post('load_tag_list.vpage', postData, function (data) {
            if (data.success) {
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.tagMapList
                }));

                $(".message_page_list").page({
                    total: data.totalPage,
                    current: data.currentPage,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        getArticleList(index);
                    }
                });
            }
        });
    }
    $(function () {
        getArticleList(1);

        // 手动刷新tags
        $("#refreshtags").on("click", function () {
            if (!confirm("确定刷新标签树？刷新后，请手动查询查看结果")) {
                return false;
            }
            $.post("refreshkeywordtags.vpage", function (data) {
                if (data.success) {
                    alert("刷新程序成功启动，请耐心等待，可以手动刷新查询页面看是否已生效");
                } else {
                    alert("启动刷新程序失败");
                }
            })
        });

        //添加一级内容类型
        $('#add_parent_tag').on('click', function () {
            $('#mode').val("add");
            $('#parentTagId').val('');
            $('#parentTagId').attr("disabled", true);
            $('#parentTagName').val('');
            $('#parentTagName').attr("disabled", false);
            $('#secondTagId').val('');
            $('#secondTagId').attr("disabled", true);
            $('#secondTagName').val('');
            $('#secondTagName').attr("disabled", true);
            $("#edit_dialog_1").modal('show');
        });


        //编辑一级内容类型
        $(document).on('click', '.edit_parent', function () {
            var parentTagId = $(this).data("id");
            var parentTagName = $("#ptn_" + parentTagId).html();
            $('#mode').val("add");
            $('#parentTagId').val(parentTagId);
            $('#parentTagId').attr("disabled", true);
            $('#parentTagName').val(parentTagName);
            $('#parentTagName').attr("disabled", false);
            $('#secondTagId').val('');
            $('#secondTagId').attr("disabled", true);
            $('#secondTagName').val('');
            $('#secondTagName').attr("disabled", true);
            $("#edit_dialog_1").modal('show');
        });


        //添加二级内容类型
        $(document).on('click', '.add_second', function () {
            var parentTagId = $(this).data("id");
            var parentTagName = $("#ptn_" + parentTagId).html();
            $('#mode').val("add");
            $('#parentTagId').val(parentTagId);
            $('#parentTagId').attr("disabled", true);
            $('#parentTagName').val(parentTagName);
            $('#parentTagName').attr("disabled", true);
            $('#secondTagId').val('');
            $('#secondTagId').attr("disabled", false);
            $('#secondTagName').val('');
            $('#secondTagName').attr("disabled", false);
            $("#edit_dialog_1").modal('show');
        });

        //编辑二级内容类型
        $(document).on('click', '.edit_second', function () {
            var parentTagId = $(this).data("id");
            var parentTagName = $("#ptn_" + parentTagId).html();
            var secondTagId = $("#stn_" + parentTagId).find("option:selected").val();
            var secondTagName = $("#stn_" + parentTagId).find("option:selected").text();
            $('#mode').val("add");
            $('#parentTagId').val(parentTagId);
            $('#parentTagId').attr("disabled", true);
            $('#parentTagName').val(parentTagName);
            $('#parentTagName').attr("disabled", true);
            $('#secondTagId').val(secondTagId);
            $('#secondTagId').attr("disabled", true);
            $('#secondTagName').val(secondTagName);
            $('#secondTagName').attr("disabled", false);
            $("#edit_dialog_1").modal('show');
        });


        //编辑关键词
        $(document).on('click', '.edit_key', function () {
            var parentTagId = $(this).data("id");
            var secondId = $("#stn_" + parentTagId).find("option:selected").val();
            location.href = "keywordsmgn.vpage?firstId=" + parentTagId + "&secondId=" + secondId;
            return false;
//            $('#mode').val("edit");
//            $('#keyWordFirstTagId').val(parentTagId);
//            $('#keyWordSecondTagId').val(secondId);
//            $('#keyWord').val('');
//            viewModel.inputKeyword("");
//            viewModel.loadCurrentKeywords();
//            $("#edit_dialog_2").modal('show');
        });


        //保存标签信息
        $("#save_btn_1").on('click', function () {
            var slot = {
                parentTagId: $('#parentTagId').val(),
                parentTagName: $('#parentTagName').val(),
                secondTagId: $('#secondTagId').val(),
                secondTagName: $('#secondTagName').val()

            };
            var valid = validate(slot);
            if (valid.length > 0) {
                alert(valid);
                return false;
            }
            if (slot.secondTagName == '' && slot.secondTagId == '') {
                $.post('add_parent_tag.vpage', slot, function (data) {
                    if (data.success) {
                        alert("保存成功！");
                        window.location.href = 'view_tag_list_keywords.vpage';
                    } else {
                        alert(data.info);
                    }
                });
            }
            else if (slot.secondTagId == '' && slot.secondTagName != '') {
                $.post('add_second_tag.vpage', slot, function (data) {
                    if (data.success) {
                        alert("保存成功！");
                        window.location.href = 'view_tag_list_keywords.vpage';
                    } else {
                        alert(data.info);
                    }
                });
            } else {
                $.post('edit_tag.vpage', slot, function (data) {
                    if (data.success) {
                        alert("保存成功！");
                        window.location.href = 'view_tag_list_keywords.vpage';
                    } else {
                        alert(data.info);
                    }
                });
            }

        });


        //保存关键词信息
        $("#save_btn_2").on('click', function () {
            var keyword = $('#keyWord').val();
            if (!keyword) {
                alert("请输入关键字");
                return false;
            }
            var slot = {
                firstId: $('#keyWordFirstTagId').val(),
                secondId: $('#keyWordSecondTagId').val(),
                keyWord: keyword
            };
            var valid = validate(slot);
            if (valid.length > 0) {
                alert(valid);
                return false;
            }
            $.post('add_tag_keywords.vpage', slot, function (data) {
                if (data.success) {
                    alert("保存成功！");
                    window.location.href = 'view_tag_list_keywords.vpage';
                } else {
                    alert(data.info);
                }
            });
        });


        //删除关键词
        $(document).on('click', '.delete-source', function () {
            var toDeletes = [];
            var checked = $("input:checkbox[name=keyWord]:checked");
            if (checked.length == 0) {
                alert("请选择要删除的关键词");
                return false;
            }
            checked.each(function (i, item) {
                toDeletes.push($(item).val());
            });
            console.info("del" + toDeletes);
            var slot = {
                secondId: $('#keyWordSecondTagId').val(),
                delKeyWords: toDeletes.join(',')

            };
            if (confirm("确定删除？")) {
                $.post("del_tag_keywords.vpage", slot, function (data) {
                    if (data.success) {
                        console.info("successfully delted");
                        reshowKeyWordsBox();
                        //getArticleList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });


        //删除一级内容类型
        $(document).on('click', '.del_parent', function () {
            var parentTagId = $(this).data("id");
            if (confirm("确定删除？")) {
                $.post("del_tag.vpage", {tagId: parentTagId}, function (data) {
                    if (data.success) {
                        getArticleList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });


        //删除二级内容类型
        $(document).on('click', '.del_second', function () {
            var parentTagId = $(this).data("id");
            var secondTagId = $("#stn_" + parentTagId).find("option:selected").val();
            if (confirm("确定删除？")) {
                $.post("del_tag.vpage", {tagId: secondTagId}, function (data) {
                    if (data.success) {
                        getArticleList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    });
    function validate(slot) {
        var msg = "";
        if (slot.name == '') {
            msg += "请填写文章源名称！\n";
        }
        if (slot.num == '') {
            msg += "请填写文章源号！\n";
        }
        if (slot.grade == '') {
            msg += "请选择文章源等级！\n";
        }

        return msg;
    }

</script>
</@layout_default.page>