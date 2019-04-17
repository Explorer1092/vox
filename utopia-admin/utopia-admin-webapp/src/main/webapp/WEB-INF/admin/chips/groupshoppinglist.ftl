<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='薯条拼团活动列表' page_num=26>
<style type="text/css">
    body {
        line-height: 20px !important;
        font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    }

    .navbar {
        min-height: 41px;
        height: 41px !important;
        border-width: 0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }

    .navbar .navbar-inner {
        min-height: 41px;
        height: 41px !important;
        border-width: 0;
        -webkit-box-sizing: border-box;
        -moz-box-sizing: border-box;
        box-sizing: border-box;
    }

    .collapse {
        display: block;
    }

    .video_list {

    }

    .video_list .item {
        height: 460px;
        width: 220px;
        margin: 10px;
        border: 1px solid #e6e6e6;
        display: inline-block;
        box-shadow: 3px 3px 5px #DDD;
        border-radius: 3px;
        position: relative;
    }

    .video_list video {
        position: absolute;
        top: 0;
        width: 100%;
        height: 300px;
    }

    .video_list .play {
        position: absolute;
        top: 0;
        width: 100%;
        height: 300px;
        z-index: 9;
        background: rgba(0, 0, 0, 0.2);
    }

    .video_list .bottom {
        position: absolute;
        bottom: 0;
        width: 100%;
        height: 158px;
        border-top: 1px solid #e6e6e6;
        padding: 5px;
        box-sizing: border-box;
    }

    .video_list .bottom .form_detail p {
        margin: 0;
        padding: 0;
    }

    .video_list .bottom .form_detail p span {
        font-weight: 600;
        margin-left: 5px;
    }

    .video_list .bottom .select {
        width: 90px;
        display: inline-block;
        padding: 5px;
        margin-top: 5px;
    }

    .video_list .bottom .btn-box {
        position: absolute;
        bottom: 10px;
        width: 100%;
    }

    #ui-datepicker-div {
        z-index: 3000 !important;
    }
</style>
<div id="main_container" class="span9">
    <legend>拼团活动列表</legend>
    <form id="frm" class="form-horizontal form-inline" action="/chips/group/shopping/list.vpage">
        <input type="hidden" id="pageNumber" name="pageNumber" value="1">
    </form>

    <button id="addGroup" class="btn btn-primary" type="button">增加拼团</button>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered">
            <tr>
                <td>序号</td>
                <td>用户</td>
                <td>拼团人数</td>
                <td>创建时间</td>
                <td>操作</td>
            </tr>
            <#if pageData.content?? && pageData.content?size gt 0>
                <#list pageData.content as e >
                    <tr >
                        <td>${e.id!}</td>
                        <td>${e.user!}</td>
                        <td>1/2</td>
                        <td>${e.createDate!}</td>
                        <td>
                            <button type="button" name="shareurl" data-url="${e.link!}" class="btn btn-primary share">分享引导</button>
                        </td>
                    </tr>
                </#list>
            <#else >
                <tr>
                    <td colspan="6"><strong>暂无数据</strong></td>
                </tr>
            </#if>
        </table>
    </div>
    <ul class="pager">
        <#if (pageData.hasPrevious())>
            <li><a href="#" onclick="pagePost(${pageNumber - 1})" title="Pre">上一页</a></li>
        <#else>
            <li class="disabled"><a href="#">上一页</a></li>
        </#if>
        <#if (pageData.hasNext())>
            <li><a href="#" onclick="pagePost(${pageNumber + 1})" title="Next">下一页</a></li>
        <#else>
            <li class="disabled"><a href="#">下一页</a></li>
        </#if>
        <li>当前第 ${pageNumber!} 页 |</li>
        <li>共 ${pageData.totalPages!} 页|</li>
        <li>共 ${total !} 条</li>
    </ul>
    
    <!-- Modal -->
    <div class="modal fade hide" id="dialog" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="myModalLabel" style="text-align:center;">分享引导</h4>
                   <textarea id="content" style="width: 457px; height: 92px;"></textarea>
                </div>
                <div class="modal-body" style="max-height: 550px !important;">
                   <button class="copy-btn" data-clipboard-target="#content">复制</button>
                </div>
            </div>
        </div>
    </div>

    <script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
    <script type="text/javascript">
        //$('#dialog').modal('show');

        var clipboard = new Clipboard('.copy-btn');
        clipboard.on('success', function (e) {
            alert("复制成功");
        });
        clipboard.on('error', function (e) {
            alert("复制失败，请手动复制");
        });

        //    console.log("longBookList","");
        <#--var longBookList = JSON.parse() ${longBooks};-->
        //    console.log("longBookList",longBookList);

        <#--var shortBookList = ${shortBooks!};-->
        <#--console.log("shortBookList",shortBookList);-->
        <#--var allBookList = ${books!};-->
        <#--console.log("allBookList",allBookList);-->
        $(function () {
            $(".share").click(function () {
                var linkUrl = $(this).data().url;
                $("#content").text("将拼团链接分享到群里，或者发送给好友可以增加拼团成功的几率哦～\n分享链接如下:" + linkUrl);
                $('#dialog').modal('show');
            })
            $("#addGroup").click(function () {
                $.post('/chips/group/shopping/add.vpage', {}, function (res) {
                    if (res.success) {
                        alert("成功");
                        location.reload();
                    } else {
                        alert("失败:" + res.info);
                    }
                });
            });
        });

        function pagePost(pageNumber) {
            $("#pageNumber").val(pageNumber != '' && pageNumber > 0 ? pageNumber : 1);
            $("#frm").submit();
        }

    </script>
</@layout_default.page>