<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    p {
        margin: 0 0 10px;
        font-size: 10px;
        line-height: 0.1px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>KOL-班长招募</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
                <option value="">--选择课程--</option>
                <#if lessonIds?? && lessonIds?size gt 0>
                    <#list lessonIds as lessonId>
                        <option value="${lessonId}"
                                <#if (((selectLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                    </#list>
                <#else>
                    <option value="">暂无数据</option>
                </#if>
        </select>
        </span>
        <span style="white-space: nowrap;">
                家长Id：<input type="text" id="searchParentId" name="searchParentId" value="${parentId!''}"/>
        </span>
        <span style="white-space: nowrap;">
                学习群号：<input type="text" id="wechatGroupName" name="wechatGroupName" value="${wechatGroupName!''}"/>
        </span>
        <span style="white-space: nowrap;">
                微信号：<input type="text" id="userWechatName" name="userWechatName" value="${userWechatName!''}"/>
        </span>
        <span style="white-space: nowrap;">
                状态：<select id="selectStatus" name="selectStatus">
                <option value="1" <#if status??&&status==1>selected</#if>>待审核</option>
                <option value="2" <#if status??&&status==2>selected</#if>>审核未通过</option>
                <option value="3" <#if status??&&status==3>selected</#if>>审核通过</option>
                <option value="4" <#if status??&&status==4>selected</#if>>离职</option>
                <option value="5" <#if status??&&status==5>selected</#if>>休整</option>
            </select>
            </span>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <button class="btn btn-primary" type="button" id="exportData">导出数据</button>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>家长ID</th>
                        <th>家长姓名</th>
                        <th>课程名称</th>
                        <th>期数</th>
                        <th>适用年级</th>
                        <th>微信号</th>
                        <th>学习群号</th>
                        <th>申请时间</th>
                        <th>审核状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  monitor>
                            <tr>
                                <td>${monitor.parentId!''}</td>
                                <td>${monitor.parentName!''}</td>
                                <td>${monitor.lessonName!''}</td>
                                <td>${monitor.phase!''}</td>
                                <td>${monitor.clazzLevelText!''}</td>
                                <td>${monitor.currentWechatId!''}</td>
                                <td>${monitor.wechatGroupName!''}</td>
                                <td>${monitor.createDate!''}</td>
                                <td>${monitor.status!''}</td>
                                <td>
                                    <a class="btn btn-success" name="checkStatus"
                                       data-parent_id="${monitor.parentId!''}">查看
                                    </a>
                                    <a class="btn btn-success" name="log"
                                       data-parent_id="${monitor.parentId!''}">操作日志
                                    </a>
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

        $("a[name='checkStatus']").on('click', function () {
            var parentId = $(this).data("parent_id");
            var url = "monitordetails.vpage?parentId=" + parentId + "&currentPage=" +${currentPage!};
            window.open(url);
        });
        $("a[name='log']").on('click', function () {
            var parentId = $(this).data("parent_id");
            var url = "monitorInfoLog.vpage?parentId=" + parentId + "&currentPage=" +${currentPage!};
            window.open(url);
        });
        //导出
        $("#exportData").on('click', function () {
            var lessonIdSearch = $("#selectLessonId").val();
            var selectStatus = $("#selectStatus").val();
            location.href = "/opmanager/studyTogether/exportApplyData.vpage?lessonIdSearch=" + lessonIdSearch + "&selectStatus=" + selectStatus;
        });
    });

</script>
</@layout_default.page>