<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div class="span9">
    <fieldset>
        <legend>数据管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get"
          action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <span style="white-space: nowrap;">
                课程ID：<select id="selectLessonId" name="selectLessonId">
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
                微信号：<input type="text" id="wechat" name="wechat" value="${wechat!''}"/>
            </span>
            <span style="white-space: nowrap;">
                开始时间：<input type="text" id="startTime" name="startTime" value="${startDate!''}"/>
            </span>
            <span style="white-space: nowrap;">
                结束时间：<input type="text" id="endTime" name="endTime" value="${endDate!''}"/>
            </span>
        </div>
    </form>
    <button class="btn btn-primary" type="button" id="searchBtn">查询</button>
    <a class="btn btn-success" id="exportData" name="exportData">导出数据</a>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>序号</th>
                        <th>个人微信号</th>
                        <th>被报名次数</th>
                        <th>微信群名称</th>
                        <th>班级Id</th>
                        <th>课程激活码</th>
                        <th>激活人数</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as groupInfo>
                            <tr>
                                <td>${groupInfo_index+1!''}</td>
                                <td>${groupInfo.wechatNumber!''}</td>
                                <td>${groupInfo.joinCount!''}</td>
                                <td>${groupInfo.wechatGroupName!''}</td>
                                <td>${groupInfo.studyGroupId!''}</td>
                                <td>${groupInfo.verifyCode!''}</td>
                                <td>${groupInfo.verifyCount!''}</td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <div class="message_page_list">
                <#--<li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>-->
                    <#--<#if hasPrev>-->
                        <#--<li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>-->
                    <#--<#else>-->
                        <#--<li class="disabled"><a href="#">&lt;</a></li>-->
                    <#--</#if>-->
                    <#--<li class="disabled"><a>第 ${currentPage!} 页</a></li>-->
                    <#--<li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>-->
                    <#--<#if hasNext>-->
                        <#--<li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>-->
                    <#--<#else>-->
                        <#--<li class="disabled"><a href="#">&gt;</a></li>-->
                    <#--</#if>-->
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">


    $(function () {
        /*时间控件*/
        $("#startTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',
            minView: 0,
            autoclose: true
        }).on('changeDate', function (ev) {
            $("#startTime").val($(this).val());

        });

        $("#endTime").datetimepicker({
            format: 'yyyy-mm-dd hh:ii:ss',
            minView: 0,
            autoclose: true
        }).on('changeDate', function (ev) {
            $("#endTime").val($(this).val());
        });
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
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            if (!startTime || !endTime) {
                alert("时间不能为空");
                return;
            }
            $("#pageNum").val(1);
            $("#op-query").submit();
        });
        //导出数据
        $("#exportData").on('click', function () {
            var selectLessonId = $("#selectLessonId").val();
            var wechat = $("#wechat").val();
            var startTime = $("#startTime").val();
            var endTime = $("#endTime").val();
            if (!startTime || !endTime) {
                alert("时间不能为空");
                return;
            }
            location.href = "/opmanager/studyTogether/exportData.vpage?selectLessonId=" + selectLessonId + "&wechat=" + wechat + "&startTime=" + startTime + "&endTime=" + endTime;
        });
    });

    function savePost(opIds, clazzId, lessonId) {
        var ids = opIds.toString().trim();
        $.ajax({
            url: 'upsertStudyGroup.vpage',
            type: 'POST',
            async: false,
            data: {"ids": ids, "clazzId": clazzId, "lessonId": lessonId},
            success: function (data) {
                if (data.success) {
                    alert("保存成功");
                    $("#wechat_dialog").modal('hide');
                    window.location.reload();
                } else {
                    alert(data.info);
                    console.log("data error");
                }
            }
        });
    }
</script>
</@layout_default.page>