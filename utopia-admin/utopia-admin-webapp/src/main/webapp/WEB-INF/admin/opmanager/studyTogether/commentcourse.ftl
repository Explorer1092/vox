<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    table td {
        font-size: 14px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>点评课配置管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>古诗课程ID&nbsp;
                        <select id="searchLessonId" name="searchLessonId">
                            <option value="">--选择课程--</option>
                        <#if lessonIds?? && lessonIds?size gt 0>
                            <#list lessonIds as lessonId>
                                <option value="${lessonId}"
                                        <#if (((searchLessonId)!'') == lessonId)>selected="selected"</#if>>${lessonId}</option>
                            </#list>
                        <#else>
                            <option value="">暂无数据</option>
                        </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label>点评课ID&nbsp;
                        <input type="text" id="searchCourseId" name="searchCourseId" value="${searchCourseId!''}"/>
                    </label>
                </li>
                <li>
                    <button type="button" class="btn btn-primary" id="searchBtn">查询</button>
                </li>
                <li>
                    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增点评课</button>
                </li>
            </ul>
        </div>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>点评课ID</th>
                        <th>课题名称</th>
                        <th>古诗课程ID</th>
                        <th>小课次课程ID</th>
                        <th>课程提示</th>
                        <th>商品ID</th>
                        <th>开课时间</th>
                        <th>课程内容</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as course>
                            <tr>
                                <td>${course.courseId!''}</td>
                                <td>${course.title!''}</td>
                                <td>${course.lessonId!''}</td>
                                <td>${course.innerId!''}</td>
                                <td>${course.reminder!''}</td>
                                <td>${course.productId!''}</td>
                                <td>${course.openDate!''}</td>
                                <td>
                                    <p>${course.poemTitle!''}</p>
                                    <p>${course.author!''}</p>
                                    <#list course.contents as content>
                                        <p>${content.content!''}</p>
                                    </#list>
                                </td>
                                <td>
                                    <a href="javascript:;" class="js-couponOption btn" data-type="edit" data-cid="${course.courseId!''}" data-lid="${course.lessonId!''}">编辑</a>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="9" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <div class="message_page_list"></div>
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

        $(document).on('click',".js-couponOption",function () {
            var $this = $(this),
                type = $this.data('type'),
                cid = $this.data('cid'),
                lid = $this.data('lid'),
                mapLink = {
                   'add':'',
                   'edit':'?courseId='+cid + "&lid=" + lid
                };
            location.href = 'coursedetail.vpage'+mapLink[type];
        });
    });
</script>
</@layout_default.page>