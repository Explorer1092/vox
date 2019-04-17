<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    table td {font-size: 14px;}
    input {
        width: 197px;
    }
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend>学生作业列表</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>学生ID&nbsp;
                        <input type="text" id="searchStudentId" name="searchStudentId" value="${searchStudentId!''}"/>
                    </label>
                </li>
                <li>
                    <label>点评课程ID&nbsp;
                        <input type="text" id="searchCourseId" name="searchCourseId" value="${searchCourseId!''}"/>
                    </label>
                </li>
                <li>
                    <label>点评状态&nbsp;
                        <select id="status" name="status">
                            <option value="0" <#if status??&&status == 0>selected</#if>>待点评</option>
                            <option value="1" <#if status??&&status == 1>selected</#if>>已点评</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>购买状态&nbsp;
                        <select id="buy" name="buy">
                            <option value="0" <#if buy??&&buy == 0>selected</#if>>已购买</option>
                            <option value="1" <#if buy??&&buy == 1>selected</#if>>未购买</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>作业等级&nbsp;
                        <select id="level" name="level">
                            <option selected="selected" value="">-选择等级-</option>
                            <option value="0" <#if level??&&level == "0">selected</#if>>普通作品</option>
                            <option value="1" <#if level??&&level == "1">selected</#if>>优秀作品</option>
                        </select>
                    </label>
                </li>
                <li>
                    <button type="button" class="btn btn-primary" id="searchBtn">查询</button>
                </li>
                <li>
                    <button class="btn btn-primary" type="button" id="exportExcel">导出数据</button>
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
                        <th>家长ID</th>
                        <th>学生姓名</th>
                        <th>学生ID</th>
                        <th>古诗课程ID</th>
                        <th>点评课程ID</th>
                        <th>古诗名称</th>
                        <#--<th>学生录音</th>-->
                        <th>提交日期</th>
                        <th>购买状态</th>
                        <th>购买方式</th>
                        <th>点评状态</th>
                        <th>作业等级</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as comment>
                            <tr>
                                <td>${comment.parentId!''}</td>
                                <td>${comment.studentName!''}</td>
                                <td>${comment.studentId!''}</td>
                                <td>${comment.lessonId!''}</td>
                                <td>${comment.courseId!''}</td>
                                <td>${comment.title!''}</td>
                                <td>${comment.createDate!''}</td>
                                <td>
                                    <#if comment.isBuy == true>
                                        <span class="label label-success">已购买</span>
                                    <#else>
                                       <span class="label label-success">未购买</span>
                                    </#if>
                                </td>
                                <td>
                                    <#if comment.buyType?? && comment.buyType == 0>
                                        <span class="label label-success">免费课</span>
                                    <#elseif comment.buyType?? && comment.buyType == 1>
                                       <span class="label label-success">人民币</span>
                                    <#elseif comment.buyType?? && comment.buyType == 2>
                                        <span class="label label-success">学习币</span>
                                    </#if>
                                </td>
                                <td>
                                    <#if comment.status == 0>
                                       <span class="label label-success">待点评</span>
                                    <#elseif comment.status == 1>
                                       <span class="label label-success">已点评</span>
                                    </#if>
                                </td>
                                <td>
                                    <#if comment.status == 1>
                                         <#if comment.level == 0>
                                            <span class="label label-success">普通作品</span>
                                         <#elseif comment.status == 1>
                                             <span class="label label-success">优秀作品</span>
                                         </#if>
                                    <#else>
                                       <span class="label label-success">无等级</span>
                                    </#if>
                                </td>
                                <td>
                                    <#if comment.isBuy == false>
                                        <button disabled="disabled" class="btn btn-danger">无法点评</button>
                                     <#elseif comment.status == 0>
                                        <a href="javascript:;" class="js-couponOption btn" data-type="add"
                                           data-sid="${comment.studentId!''}"
                                           data-cid="${comment.courseId!''}"
                                           data-pid="${comment.parentId!''}"
                                           data-name="${comment.studentName!''}"
                                           data-url="${comment.voiceUrl!''}">添加点评</a>
                                    <#elseif comment.status == 1>
                                        <a href="javascript:;" class="js-couponOption btn"
                                           data-type="edit"
                                           data-eid="${comment.id!''}"
                                           data-pid="${comment.parentId!''}"
                                           data-cid="${comment.courseId!''}"
                                           data-name="${comment.studentName!''}"
                                           data-url="${comment.voiceUrl!''}">修改点评</a>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="12" style="text-align: center">暂无数据</td>
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
                eid = $this.data('eid'),
                studentId = $this.data('sid'),
                courseId = $this.data('cid'),
                parentId = $this.data('pid'),
                studentName = $this.data('name'),
                readUrl = $this.data('url'),
                mapLink = {
                    'add' : '?courseId=' + courseId + "&studentId=" + studentId + '&parentId=' + parentId + '&studentName=' + studentName + '&readUrl=' + readUrl,
                    'edit': '?evaluateId=' + eid + '&parentId=' + parentId + '&courseId=' + courseId + '&studentName=' + studentName + '&readUrl=' + readUrl
                };
            location.href = 'evaluatedetail.vpage' + mapLink[type];
        });

        $("#exportExcel").on('click', function () {
            var studentId = $("#searchStudentId").val();
            var courseId = $("#searchCourseId").val();
            var status = $("#status").val();
            var buy = $("#buy").val();
            var level = $("#level").val();
            location.href = "exportworkdata.vpage?studentId=" + studentId + "&courseId=" + courseId + "&status=" + status + "&level=" + level + "&buy=" + buy;
        });
    });
</script>
</@layout_default.page>