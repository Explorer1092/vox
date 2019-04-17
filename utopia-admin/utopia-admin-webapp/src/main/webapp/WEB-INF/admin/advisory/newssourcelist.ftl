<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-文章源设置' page_num=13>
<div class="span9">
    <fieldset>
        <legend>文章源设置</legend>
    </fieldset>

    <fieldset>
        <div>
            <span>
                来源筛选：
                <input type="text" placeholder="输入来源" id="searchVal">
                <button class="btn btn-primary" id="searchBtn">筛选</button>
            </span>
            <button class="btn btn-primary" id="add_news_source">添加文章源</button>
        </div>
        <div id="articleBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>


<div id="edit_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>编辑文章源信息</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <input id="mode" type="hidden" value="add">
        <dl>
            <dt>id</dt>
            <dd>
                <input type="text" id="sourceId" name="sourceId"/>
            </dd>
        </dl>
        <dl>
            <dt>文章源号</dt>
            <dd>
                <input type="text" id="sourceNum" name="sourceNum"/>
            </dd>
        </dl>
        <dl>
            <dt>文章源名称</dt>
            <dd>
                <input type="text" id="sourceName" name="sourceName"/>
            </dd>
        </dl>
        <dl>
            <dt>文章源等级</dt>
            <dd>
                <select id="sourceGrade">
                    <option value=>--请选择--</option>
                    <option value="A">A</option>
                    <option value="B">B</option>
                    <option value="C">C</option>
                </select>
            </dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取消</button>
        <button class="btn btn-primary" id="save_slot_btn">保存</button>
    </div>
</div>

<script type="text/html" id="articleBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>序号</th>
            <th>文章源</th>
            <th>文章源等级</th>
            <th>操作</th>
        </tr>
        <%if(content.length > 0){%>
        <%for(var i = 0; i < content.length;i++){%>
        <tr>
            <td><%=i+1%></td>
            <td id="sn_<%=content[i].id%>"><%=content[i].newsSourceName%></td>
            <td id="sg_<%=content[i].id%>"><%=content[i].sourceGrade%></td>
            <td id="snu_<%=content[i].id%>" hidden><%=content[i].newsSourceNum%></td>
            <td data-_id="<%=content[i].id%>">
                <a class="btn btn-primary edit_source" data-id="<%=content[i].id%>">编辑</a>
                <button class="btn btn-danger delete-source" data-id="<%=content[i].id%>">删除</button>
                <a class="btn btn-info start-crawl" data-id="<%=content[i].id%>">抓取该公众号</a>
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
    var currentSearchVal = '';

    function getArticleList(page) {
        var postData = {
            currentPage: page
        };
        if (currentSearchVal != '') {
            postData.source = currentSearchVal;
        }

        $.post('load_news_source.vpage', postData, function (data) {
            if (data.success) {
                console.info(data);
                $('#articleBox').html(template("articleBox_tem", {
                    content: data.zyParentNewsSources
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

        $(document).on('click', '.delete-source', function () {
            var $this = $(this);
            var sourceId = $this.data('id');
            if (confirm("确定删除？")) {
                $.post("delete_news_source.vpage", {sourceId: sourceId}, function (data) {
                    if (data.success) {
                        getArticleList(1);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        //标题筛选
        $(document).on('click', "#searchBtn", function () {
            currentSearchVal = $("#searchVal").val();
            getArticleList(1);
        });

        //标题筛选
        $(document).on('click', ".start-crawl", function () {
            var _id=$(this).data("id");
            console.info(_id);
            $.post("startwechatsogoucrawler.vpage",{_id:_id,type:'wechat_sogou'},function(data){
                console.info(data);
                alert(data.success);
            })
        });


        //添加文章源
        $('#add_news_source').on('click', function () {
            $('#mode').val("add");
            $('#sourceId').val('');
            $('#sourceId').attr("disabled", true);
            $('#sourceNum').val('');
            $('#sourceNum').attr("disabled", false);
            $('#sourceName').val('');
            $('#sourceGrade').val('');
            $('#sourceGrade').attr("disabled", false);
            $("#edit_dialog").modal('show');
        });

        //编辑文章源
        $(document).on('click', '.edit_source',function () {
            var id = $(this).data("id");
            var slot = {
                id: id,
                sourceName: $("#sn_" + id).html().trim(),
                sourceGrade: $("#sg_" + id).html().trim(),
                sourceNum: $("#snu_" + id).html().trim(),

            };
            $('#mode').val("edit");
            $('#sourceId').val(slot.id);
            $('#sourceId').attr("disabled", true);
            $('#sourceNum').val(slot.sourceNum);
            $('#sourceNum').attr("disabled", true);
            $('#sourceName').val(slot.sourceName);
            $('#sourceGrade').val(slot.sourceGrade);
            $("#edit_dialog").modal('show');
        });


        //保存文章源信息
        $("#save_slot_btn").on('click', function () {
            var slot = {
                id: $('#sourceId').val(),
                name: $('#sourceName').val(),
                num: $('#sourceNum').val(),
                grade: $('#sourceGrade').val(),

            };
            var valid = validate(slot);
            if (valid.length > 0) {
                alert(valid);
                return false;
            }
            $.post('insert_news_source.vpage', slot, function (data) {
                if (data.success) {
                    alert("保存成功！");
                    window.location.href = 'view_news_source.vpage';
                } else {
                    alert(data.info);
                }
            });
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