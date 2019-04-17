<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>

    .words-split {
        vertical-align: middle;
    }

    .lbl-input {
        display: inline-block;
        width: 120px;
        height: 26px;
        line-height: 26px;
        min-height: 26px;
        text-indent: 1em;
        border: 1px solid #ddd;
        border-radius: 5px;
        vertical-align: middle;
    }

    .words-split a {
        display: inline-block;
        padding: 0 20px 0 8px;
        position: relative;
        margin: 0 4px;
    }

    .words-split a em {
        display: none;
        width: 16px;
        height: 100%;
        position: absolute;
        background: #f60;
        right: 0;
        top: 0;
    }

    .words-split a em:after {
        content: "-";
        color: #fff;
        font: bold 20px 'Microsoft Yahei';
    }

    .words-split a:hover em {
        display: block;
    }

    a.words-split-add {
        display: inline-block;
        font: bold 20px 'Microsoft Yahei';
        color: #2cac93
    }

    .fm-button {
        display: inline-block;
        text-align: center;
        color: #fff;
        height: 28px;
        line-height: 28px;
        font-size: 14px;
        padding: 0 1em;
        border-radius: 3px;
        opacity: .9;
        filter: alpha(opacity=90);
        background: #2cac93;
    }

    a {
        text-decoration: none;
    }

</style>
<div class="span9">
    <fieldset>
        <legend>KOL-班长管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>微信号&nbsp;
                    <input type="text" id="inputWechatId" name="inputWechatId" value="${inputWechatId!''}"/>
                </label>
            </li>
            <li>
                <label>家长ID&nbsp;
                    <input type="text" id="searchParentId" name="searchParentId" value="${searchParentId!''}"/>
                </label>
            </li>
            <li>
                <label>管理的课程ID&nbsp;
                    <select id="lessonIdSearch" name="lessonIdSearch">
                        <option value="">--选择课程--</option>
                        <#if allLessonIds?? && allLessonIds?size gt 0>
                            <#list allLessonIds as lessonId>
                                <option value="${lessonId}"
                                        <#if (((lessonIdSearch)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                            </#list>
                        <#else>
                            <option value="">暂无数据</option>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>激活的课程ID&nbsp;
                    <select id="activeLessonIdSearch" name="activeLessonIdSearch">
                        <option value="">--选择课程--</option>
                        <#if lessonIds?? && lessonIds?size gt 0>
                            <#list lessonIds as lessonId>
                                <option value="${lessonId}"
                                        <#if (((activeLessonIdSearch)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                            </#list>
                        <#else>
                            <option value="">暂无数据</option>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>申请时选择的课程&nbsp;
                    <select id="selectedLessonIdSearch" name="selectedLessonIdSearch">
                        <option value="">--选择课程--</option>
                        <#if allLessonIds?? && allLessonIds?size gt 0>
                            <#list allLessonIds as lessonId>
                                <option value="${lessonId}"
                                        <#if (((selectedLessonIdSearch)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                            </#list>
                        <#else>
                            <option value="">暂无数据</option>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>级别&nbsp;
                    <select id="level" name="level">
                        <option selected="selected" value=-1 <#if level??&&level==-1>selected</#if>>选择级别</option>
                        <option value=1 <#if level??&&level==1>selected</#if>>班长</option>
                        <option value=2 <#if level??&&level==2>selected</#if>>初级辅导员</option>
                        <option value=3 <#if level??&&level==3>selected</#if>>中级辅导员</option>
                        <option value=4 <#if level??&&level==4>selected</#if>>高级辅导员</option>
                        <option value=5 <#if level??&&level==5>selected</#if>>资深辅导员</option>
                        <option value=6 <#if level??&&level==6>selected</#if>>荣誉辅导员</option>
                    </select>
                </label>

            </li>
            <li>
                <label>状态&nbsp;
                    <select id="status" name="status">
                        <option selected="selected" value="">选择</option>
                        <option value=3 <#if status??&&status==3>selected</#if>>审核通过</option>
                        <option value=4 <#if status??&&status==4>selected</#if>>离职</option>
                        <option value=5 <#if status??&&status==5>selected</#if>>休整</option>
                    </select>
                </label>
            </li>
            <li>
                <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
                <button class="btn btn-primary" type="button" id="exportData">导出数据</button>
            </li>
        </ul>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长ID</th>
                        <th>家长姓名</th>
                        <th>微信号</th>
                        <th>级别</th>
                        <th>管理的课程ID</th>
                        <th>激活的课程ID</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  monitor>
                            <tr>
                                <td>${monitor.parentId!''}</td>
                                <td>${monitor.parentName!''}</td>
                                <td>${monitor.inputWechatId!''}</td>
                                <td>${monitor.level!''}</td>
                                <td>${monitor.lessonIds!''}</td>
                                <td>${monitor.activeLessonIds!''}</td>
                                <td>
                                    <a class="btn btn-primary" name="checkInfo" data-parent_id="${monitor.parentId!''}">
                                        查看
                                    </a>
                                    <#if monitor.status != 4>
                                        <a class="btn btn-danger" name="leave" data-parent_id="${monitor.parentId!''}">
                                            离职
                                        </a>
                                    <#else>
                                        <a disabled="disabled" class="btn btn-default">已离职</a>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="8" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>

                <ul class="message_page_list">
                </ul>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {
    <#--$("#classArea").val("${classArea!}");-->
        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        //离职
        $("a[name='leave']").on('click', function () {
            if (!confirm("确认离职？")) {
                return;
            }
            var parentId = $(this).data("parent_id");
            var status = 4;
            $.ajax({
                url: 'changeMonitorstatus.vpage',
                type: 'POST',
                data: {parentId: parentId, status: status},
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                        console.log("data error");
                    }
                }
            });
        });

        //查看
        $("a[name='checkInfo']").on('click', function () {
            var parentId = $(this).data("parent_id");
            var url = "passmonitordetails.vpage?parentId=" + parentId + "&currentPage=" +${currentPage!};
            window.open(url);
        });

        //导出
        $("#exportData").on('click', function () {
            var lessonIdSearch = $("#lessonIdSearch").val();
            var activeLessonIdSearch = $("#activeLessonIdSearch").val();
            var selectLessonIdSearch = $("#selectedLessonIdSearch").val();
            if (!lessonIdSearch && !activeLessonIdSearch && !selectLessonIdSearch) {
                alert("请选择至少一个条件进行导出");
                return;
            }
            if ((lessonIdSearch && activeLessonIdSearch) || (activeLessonIdSearch && selectLessonIdSearch) || (lessonIdSearch && selectLessonIdSearch) || (lessonIdSearch && selectLessonIdSearch && activeLessonIdSearch)) {
                alert("仅支持一个条件的导出");
                return;
            }
            location.href = "/opmanager/studyTogether/exportMonitorData.vpage?activeLessonIdSearch=" + activeLessonIdSearch + "&lessonIdSearch=" + lessonIdSearch + "&selectLessonIdSearch=" + selectLessonIdSearch;
        });
    });

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#op-query").submit();
    }
</script>
</@layout_default.page>