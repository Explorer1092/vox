<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-文章管理' page_num=13>
<style>
    .modal {
        background-color: inherit;
    !important;
    }

    .modal.fade.in {
        top: 1%
    }

    .device {
        background-image: url("/public/img/device-sprite.png");
        background-position: 0 0;
        background-repeat: no-repeat;
        background-size: 300% auto;
        display: block;
        font-family: "Helvetica Neue", sans-serif;
        height: 813px;
        position: relative;
        transition: background-image 0.1s linear 0s;
        width: 395px;
    }

    .device .device-content {
        background: #eeeeee none repeat scroll 0 0;
        font-size: 0.85rem;
        height: 569px;
        left: 37px;
        line-height: 1.05rem;
        overflow: hidden;
        position: absolute;
        top: 117px;
        width: 321px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>文章管理</legend>
        <div style="padding-bottom: 10px">

            发布状态：<select id="filter_pushed"><option value="configing">待配置</option><option value="onlineing">已配置</option></select>

            <span>
                编辑人员：
                <input type="text" placeholder="输入编辑人员crm帐号" id="searchEditor" value="${currentUser!""}">
            </span>
            <br>
            <span>
                标题：
                <input type="text" placeholder="输入标题" id="searchTitle">
            </span>
            <span>
                类别：
                <select id="filter_category">
                    <option value="0">不限</option>
                    <option value="1">导流专用</option>
                </select>
            </span>
            <br>
            <button class="btn btn-primary" id="searchBtn">查询</button>&nbsp;&nbsp;&nbsp;&nbsp;
            <button class="btn btn-primary" id="resetFilters">重置查询条件</button>&nbsp;&nbsp;&nbsp;&nbsp;
            <a class="btn btn-primary" href="${requestContext.webAppContextPath}/advisory/contentedit.vpage"
               id="contentCreateBtn">新建文章</a>

        </div>
    </fieldset>

    <fieldset>
        <div id="contentBox">

        </div>
        <div class="message_page_list"></div>
    </fieldset>
</div>

<script type="text/html" id="contentBox_tem">
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width: 20%">ID</th>
            <th style="width: 20%">标题</th>
            <th style="width: 20%">备注</th>
            <th style="width: 20%">编辑者</th>
            <th style="width: 20%">操作</th>
        </tr>
        <%for(var i=0;i < articles.length;i++){ var article=articles[i];%>
        <tr>
            <td><%=article.id%></td>
            <td><%=article.title%></td>
            <td><%=article.remark%></td>
            <td><%=article.editor%></td>
            <td data-id="<%=article.id%>">
                <%if(article.pushed){%>
                <a class="btn btn-primary" href="/advisory/jxtnewsedit.vpage?newsId=<%=article.news_id%>"
                   target="_blank">已配置</a>
                <%}else{%>
                <a class="btn btn-primary" href="/advisory/jxtnewsedit.vpage?articleId=<%=article.id%>" target="_blank">发布配置</a>
                <%}%>
                <button class="btn btn-danger delete">删除</button>
                <a href="/advisory/contentupdateedit.vpage?id=<%=article.id%>" class="btn btn-success" target="_blank">编辑</a>
                <a href="javascript:void (0);" data-id="<%=article.id%>" class="btn btn-success previewBtn">预览</a>
                <a href="javascript:void (0);"
                   data-clipboard-text="<%= mainSiteBaseUrl.replace('http:','https:')+'/view/mobile/parent/information/promotview?id='+article.id%>"
                   class="btn btn-success copyBtn">复制推广连接</a>
            </td>
        </tr>
        <%}%>
    </table>
</script>


<!-- 模态框（Modal） -->
<div id="previewModal" class="modal hide fade" tabindex="-1" style="width: 430px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body" style="max-height: 900px; width: 400px;" id="previewBox"></div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="previewBox_tem">
    <div class="device" style="" id="layoutInDevice">
        <div class="device-content">
            <div id="iwindow">
                <iframe width="320" height="569" frameborder="0" src="<%=url%>"></iframe>
            </div>
        </div>
    </div>
</script>

<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>

<script type="text/javascript">
    var mainSiteBaseUrl="${mainSiteBaseUrl!}";
    var currentUser="${currentUser!''}";
    var currentPage = 0;
    var pushed = false;
    var currentSearchTitle = "";
    var currentSearchEditor = "${currentUser!''}";
    var currentSearchCategory = 0;
    function getContentList(page) {
        var title = '';
        var editor = "";
        var category=0;
        if (currentSearchTitle != '') {
            title = currentSearchTitle;
        } else {
            title = '';
        }
        if (currentSearchEditor != '') {
            editor = currentSearchEditor;
        } else {
            editor = '';
        }
        if (currentSearchCategory != 0) {
            category = currentSearchCategory;
        } else {
            category = '';
        }
        $.post('loadarticles.vpage', {
            currentPage: page,
            pushed: pushed,
            title: title,
            editor: editor,
            category:category
        }, function (data) {
            if (data.success) {
                $('#contentBox').html(template("contentBox_tem", {
                    articles: data.articles.content,
                    mainSiteBaseUrl: "${mainSiteBaseUrl!}"
                }));

                $(".message_page_list").page({
                    total: data.articles.totalPages,
                    current: data.articles.number + 1,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        getContentList(index);
                        currentPage = index;
                    }
                });
            }
        });
    }
    $(function () {
        //初始化
        getContentList(1);

        $("#filter_pushed").on("change", function () {
            if($(this).val()=="configing"){
                pushed=false;
            }else{
                pushed=true;
            }
        });

        $("#filter_category").on("change", function () {
            currentSearchCategory=parseInt($("#filter_category").val());
        });

        //disable article
        $(document).on("click", ".delete", function () {
            var id = $(this).parent().data("id");
            if (confirm("你确定删除吗？")) {
                $.post("deletearticle.vpage", {id: id}, function (data) {
                    if (data.success) {
                        getContentList(currentPage);
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        //预览
		var get_url_pre = function(){
			var pre = $('<a>', {href : '/'})[0].href,
				env = /admin\.(\w+)\./.exec(pre)[1];

			return  env === '17zuoye' ? 'http://www.17zuoye.com/' : 'http://www.' + env + '.17zuoye.net/';

		};
        $(document).on("click", ".previewBtn", function () {
            var $this = $(this);
            var id = $this.data('id');
            // 推广连接使用https
            var baseUrl=mainSiteBaseUrl.replace('http:','https:');
            $("#previewBox").html(template("previewBox_tem", {url: baseUrl + '/view/mobile/parent/information/preview?id=' + id }));
            $("#previewModal").modal("show");
        });

        //重置查询条件
        $(document).on("click","#resetFilters",function () {
           console.info("reset filters");
            $("#filter_pushed").val("configing");
            $("#searchTitle").val("");
            $("#searchEditor").val(currentUser);
            $("#filter_category").val("0");
        });

        // 生成页面
        $(document).on("click", ".generateStatic", function () {
            var $this = $(this);
            var id = $this.data("id");
            $.post("generatestaticpage.vpage", {id: id}, function (data) {
                console.info(data);
            })
        })

        $(document).on('click', "#searchBtn", function () {
            currentSearchTitle = $("#searchTitle").val().trim();
            currentSearchEditor = $("#searchEditor").val().trim();
            getContentList(1);
        });


        var clipboard = new Clipboard('.copyBtn');
        clipboard.on('success', function (e) {
            alert("复制成功: " + e.text);
        });
        clipboard.on('error', function (e) {
            alert("复制失败，请手动复制");
        });
    });
</script>
</@layout_default.page>
