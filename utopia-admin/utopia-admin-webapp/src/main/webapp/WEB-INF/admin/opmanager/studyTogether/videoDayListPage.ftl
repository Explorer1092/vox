<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div class="span9">
    <fieldset>
        <legend>古诗词日榜视频审核</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>课程ID&nbsp;
                    <select id="selectLessonId" name="selectLessonId">
                        <option style="display: none" <#if select_lessonId??> selected="selected" </#if> value=${select_lessonId}>${select_lessonId}</option>
                        <#if lessonIds?? && lessonIds?size gt 0>
                            <#list lessonIds as lessonId>
                                <option value="${lessonId}">${lessonId}</option>
                            </#list>
                        <#else>
                            <option value="">暂无数据</option>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>审核状态&nbsp;
                    <select id="status" name="status">
                        <option value=10>全部</option>
                        <option <#if status??><#if status==0>selected="selected" </#if></#if> value=0>待审核</option>
                        <option <#if status??><#if status==1>selected="selected" </#if></#if> value=1>审核通过</option>
                        <option <#if status??><#if status==2>selected="selected" </#if></#if> value=2>审核未通过</option>
                    </select>
                </label>
            </li>
            <li>
                <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
            </li>
        </ul>
    </form>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>学生ID</th>
                        <th>姓名</th>
                        <th>视频链接</th>
                        <th>上传时间</th>
                        <th>点赞数</th>
                        <th>审核状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as  video>
                            <tr>
                                <td>${video_index+1!''}</td>
                                <td>${video.studentId!''}</td>
                                <td>${video.studentName!''}</td>
                                <td><a href="${video.videoUrl}" target="_blank"/>播放地址</a></td>
                                <td>${video.createDate!''}</td>
                                <td>${video.likeNum!''}</td>
                                <td>
                                    <#if video.status == 0>
                                    待审核
                                    <#elseif video.status == 1>
                                    通过
                                    <#elseif video.status == 2>
                                    未通过
                                    </#if>
                                </td>
                                <td>
                                    <#if video.status == 0>
                                        <button class="btn btn-success" onclick="changeStatus(1,'${video.lessonId!''}',${video.studentId!''})">审核通过</button>
                                        <button class="btn btn-danger" onclick="changeStatus(2,'${video.lessonId!''}',${video.studentId!''})">审核未通过</button>
                                    <#elseif video.status == 1>
                                        <button disabled="disabled" class="btn btn-default">审核通过</button>
                                        <button class="btn btn-danger" onclick="changeStatus(2,'${video.lessonId!''}',${video.studentId!''})">审核未通过</button>
                                    <#else>
                                        <button class="btn btn-success" onclick="changeStatus(1,'${video.lessonId!''}',${video.studentId!''})">审核通过</button>
                                        <button disabled="disabled" class="btn btn-default">审核未通过</button>
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

    })


    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#op-query").submit();
    }

    function changeStatus(status,lessonId,sid) {
        if (lessonId === '' || sid  === '') {
            alert("参数错误")
        }
        if (confirm("是否确认")) {
            $.ajax({
                type: "post",
                url: "changestatus.vpage",
                data: {
                    status: status,
                    lessonId: lessonId,
                    sid: sid
                },
                success: function (data) {
                    if (data.success) {
                        alert("操作成功");
                        window.location.href='videodaylist.vpage';
                    } else {
                        alert("操作失败");
                    }
                }
            });
        }
    }
</script>

    </@layout_default.page>