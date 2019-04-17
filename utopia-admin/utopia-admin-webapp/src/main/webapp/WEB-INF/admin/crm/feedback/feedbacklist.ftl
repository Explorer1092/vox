<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="全部反馈查询" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jppaginator/jqPaginator.min.js"></script>
<style>
    .list_style{
        float:left;
        text-decoration:none;
        list-style: none;
        width:90px;
        text-align:center;
    }
    .relationNum{
        position: relative;
    }
    .relationNum:hover .relationNumList{
        display: block;
    }
    .relationNum .relationNumList{
        position: absolute;
        width: 60px;
        background: #fff;
        padding: 5px;
        bottom: -34px;
        z-index: 10;
        display: none;
    }
</style>
<div class="span11">
    <form method="GET" id="feedback_list_form" class="form-horizontal">
        <ul class="inline">
            <li>
                <label>
                    开始日期：
                    <input id="startDate" name="startDate" value="${statDate?string("yyyy-MM-dd")}" type="text"/>
                </label>
            </li>
            <li>
                <label>
                    结束日期：
                    <input id="endDate" name="endDate" value="${endDate?string("yyyy-MM-dd")}" type="text"/>
                </label>
            </li>
            <li>
                <label>
                    学科：
                    <select id="subject" name="subject">
                        <option value="0"></option>
                        <#if subject?has_content>
                            <#list subject as s>
                                <option value="${s.id!'0'}">${s.desc!'-'}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    反馈类型：
                    <select id="feedbackType" name="type"
                            onchange="changeFeedbackType('feedbackType','firstCategory','secondCategory','thirdCategory')">
                        <option value="0"></option>
                        <#if type?has_content>
                            <#list type as t>
                                <option value="${t.type!'0'}">${t.desc!'-'}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    状态：
                    <select id="feedbackStatus" name="status">
                        <option value="0"></option>
                        <#if status?has_content>
                            <#list status as s>
                                <option value="${s.id!'0'}">${s.desc!'-'}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    一级分类：
                    <select id="firstCategory"
                            onchange="secondCategories('firstCategory','secondCategory','thirdCategory')"
                            name="firstCategory">
                        <option value="0"></option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    二级分类：
                    <select id="secondCategory"
                            onchange="thirdCategories('firstCategory','secondCategory','thirdCategory')"
                            name="secondCategory">
                        <option value="0"></option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    三级分类：
                    <select id="thirdCategory" name="thirdCategory">
                        <option value="0"></option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    指定PM：
                    <select id="pmData" name="pmData">
                        <option value="0"></option>
                        <#if pmList?has_content>
                            <#list pmList as item>
                                <option value="${item.account}"
                                        userPlatform="${item.userPlatform}">${item.accountName!}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    是否上线：
                    <select id="online" name="online">
                        <option value="0"></option>
                        <option value="1">已上线</option>
                        <option value="2">未上线</option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    建议/需求：
                    <input value="" id="content" name="content" type="text"/>
                </label>
            </li>
            <li>
                <label>
                    反馈人:
                    <input value="" id="feedbackPeople" name="feedbackPeople" type="text"/>
                </label>
            </li>
            <li>
                <label>
                    反馈老师:
                    <input value="" id="teacher" name="teacher" type="text"/>
                </label>
            </li>
            <li>
                <label>
                    反馈编号:
                    <input value="" id="feedbackId" name="id" type="text"/>
                </label>
            </li>
            <li>
                <button type="button" class="submit_but">查询</button>
                <button type="button" class="export_but">导出Excel</button>
                <span style="margin-left:20px">共有<span id="listSize">0</span>条反馈</span>
            </li>
        </ul>
    </form>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>编号</th>
                <th>关联反馈</th>
                <th>反馈日期</th>
                <th>反馈人</th>
                <th>学科</th>
                <th>反馈类型</th>
                <th style="width:250px;">建议/需求</th>
               <#-- <th>反馈老师</th>-->
                <th>销运意见</th>
                <th>PM意见</th>
                <th>状态</th>
                <#--<th>三级分类</th>-->
                <th>指定PM</th>
                <th>预计上线时间</th>
                <th>是否上线</th>
                <th>操作</th>
            </tr>
            <tbody id="fb_table_body">
            </tbody>
        </table>
        <div id="list_foot"></div>
    </div>
</div>

<script id="feedbackListTemp" type="text/html">
    <%if (list){%>
    <%for(var i = 0; i < list.length; ++i){%>
    <tr>
        <td><%=list[i].id%></td>
        <td class="relationNum">
            <%=list[i].relationIds.length%>
            <%if (list[i].relationIds.length > 0){%>
            <div class="relationNumList">
                <%for(var j = 0; j < list[i].relationIds.length; ++j){%>
                <p><a target="_blank" href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&applyId=<%=list[i].relationIds[j]%>"><%=list[i].relationIds[j]%></a></p>
                <%}%>
            </div>
            <%}%>
        </td>
        <td><%=list[i].feedbackDate%></td>
        <td><%=list[i].feedbackPeople%></td>
        <td><%=list[i].subject%></td>
        <td><%=list[i].type%></td>
        <td  style="width:250px;"><%=list[i].content%></td>
        <#--<td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=<%=list[i].teacherId%>"><%=list[i].teacherName%>(<%=list[i].teacherTelephone%>)</a>-->
        <td><%=list[i].soOpinion%></td>
        <td><%=list[i].pmOpinion%></td>
        <td><%=list[i].status%></td>
       <#-- <td><% if(list[i].firstCategory){%>
            <%=list[i].firstCategory%>
            <%}%>
            <% if(list[i].secondCategory){%>
            /<%=list[i].secondCategory%>
            <%}%>
            <% if(list[i].thirdCategory){%>
            / <%=list[i].thirdCategory%>
            <%}%>
        </td>-->
        <td><%=list[i].pmData%></td>
        <td><%=list[i].onlineData%></td>
        <td> <%if(list[i].online){%>已上线<%}else{%>未上线<%}%></td>
        <td>
            <a target="_blank"
               href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&applyId=<%=list[i].id%>">查看</a>
            <a href="javascript:void(0);" class="update-feedback-but" data-feedbackId="<%=list[i].id%>">编辑</a>
            <%if(!(list[i].online)&& list[i].status =="PM已采纳"){%><a href="javascript:void(0);"
                                                                    class="sure-online-feedback"
                                                                    data-feedbackId="<%=list[i].id%>">确认上线</a><%}%>
        </td>
    </tr>
    <%}%>
    <%}%>
</script>

<script id="sureFeedbackOnline" type="text/html">
    <fieldset>
        <legend>上线确认</legend>
    </fieldset>
    <input type="hidden" id="sure_online_feedback_id" value="<%=f.feedback.id%>">
    <div class="form-horizontal">
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>编号</strong></label>
                <div class="controls">
                    <label><%=f.feedback.id%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>反馈人</strong></label>
                <div class="controls">
                    <label><%=f.feedback.accountName%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>学科</strong></label>
                <div class="controls">
                    <label><%=f.subjectStr%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>反馈类型</strong></label>
                <div class="controls">
                    <label><%=f.typeStr%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>建议/需求</strong></label>
                <div class="controls">
                    <label><%=f.feedback.content%></label>
                </div>
            </div>
        </div>
        <%if (f.feedback.teacherId){%>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>老师信息</strong></label>
                <div class="controls">
                    <label><%=f.feedback.teacherName%>(<%=f.mobile%>)</label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>通知老师</strong></label>
                <div class="controls">
                    <input type="checkbox" id="selectTeacher"/>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"></label>
                <div class="controls">
                    <textarea name="" id="teacher_content" cols="30" rows="3" maxlength="38"></textarea>
                </div>
            </div>
        </div>
        <%}%>
    </div>
</script>


<script id="updateFeedbackInfo" type="text/html">
    <fieldset>
        <legend>编辑反馈</legend>
    </fieldset>
    <input type="hidden" id="updateFeedbackId" value="<%=f.feedback.id%>">
    <div class="form-horizontal">
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>编号</strong></label>
                <div class="controls" style="">
                    <label><%=f.feedback.id%>&nbsp;&nbsp;&nbsp;&nbsp;<input id="addRelationBtn" type="button" onclick="" value="添加关联"/></label>

                </div>
            </div>
        </div>
        <%if (f.relationList){%>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>关联反馈</strong></label>
                <div class="controls" style="">
                    <table id="relation-table" class="table-bordered" width="40%">
                        <%for(var i = 0; i < f.relationList.length ; ++i){%>
                            <tr>
                                <td><a target="_blank" style="color: blue"
                                   href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&applyId=<%=f.relationList[i].id%>"><%=f.relationList[i].id%></a></td>
                                <td width="40%"><input class="deleteRelation" type="button" onclick="deleteRelation(this,<%=f.feedback.id%>,<%=f.relationList[i].id%>)" value="删除关联"/></td>
                            </tr>
                        <%}%>
                    </table>
                </div>
            </div>
        </div>
        <%}%>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>反馈人</strong></label>
                <div class="controls">
                    <label><%=f.feedback.accountName%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div>
                <label class="col-sm-2 control-label"><strong>建议/需求</strong></label>
                <div class="controls">
                    <label><%=f.feedback.content%></label>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>学科</strong></label>
                <div class="controls">
                    <select id="update_subject">
                        <%if (f.subject){%>
                        <%for(var i = 0; i < f.subject.length ; ++i){%>
                        <option
                        <%if(f.subject[i].this == f.feedback.teacherSubject){%>selected<%}%>
                        value="<%=f.subject[i].id%>"><%=f.subject[i].desc%></option>
                        <%}%>
                        <%}%>
                    </select>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>反馈类型</strong></label>
                <div class="controls">
                    <select id="update_feedback_type"
                            onchange="changeFeedbackType('update_feedback_type','updateFirstCategory','updateSecondCategory','updateThirdCategory')">
                        <%if (f.type){%>
                        <%for(var j = 0; j< f.type.length ; ++j){%>j
                        <option
                        <%if(f.type[j].this == f.feedback.feedbackType){%>selected <%}%>
                        value="<%=f.type[j].type%>"><%=f.type[j].desc%></option>
                        <%}%>
                        <%}%>
                    </select>
                </div>
            </div>
        </div>
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>三级分类</strong></label>
                <div class="controls">
                    <select id="updateFirstCategory"
                            onchange="secondCategories('updateFirstCategory','updateSecondCategory','updateThirdCategory')"
                            name="firstCategory" category="<%=f.category1%>">
                        <option value="0"></option>
                    </select>
                    <select id="updateSecondCategory"
                            onchange="thirdCategories('updateFirstCategory','updateSecondCategory','updateThirdCategory')"
                            name="secondCategory" category="<%=f.category2%>">
                        <option value="0"></option>
                    </select>
                    <select id="updateThirdCategory" name="thirdCategory" category="<%=f.category3%>">
                        <option value="0"></option>
                    </select>
                </div>
            </div>
        </div>
        <#--<div class="modal-body" style="height: auto; overflow: visible;">-->
            <#--<div class="control-group">-->
                <#--<label class="col-sm-2 control-label"><strong>指定PM</strong></label>-->
                <#--<div class="controls">-->
                    <#--<%if(f.canSelectedPm){%>-->
                    <#--<select id="update_pm">-->
                        <#--<%if (f.pmList.length>0){%>-->
                        <#--<%for(var i = 0; i < f.pmList.length ; ++i){%>-->
                        <#--<option-->
                        <#--<%if(f.pmList[i].account == f.feedback.pmAccount){%>selected<%}%>-->
                        <#--value="<%=f.pmList[i].account%>"><%=f.pmList[i].accountName%></option>-->
                        <#--<%}%>-->
                        <#--<%}%>-->
                    <#--</select>-->
                    <#--<%}else{%>-->
                    <#--<label><%=f.feedback.pmAccountName%></label>-->
                    <#--<%}%>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div>-->
        <div class="modal-body" style="height: auto; overflow: visible;">
            <div class="control-group">
                <label class="col-sm-2 control-label"><strong>预计上线日期</strong></label>
                <div class="controls">
                    <%if(f.canSelectedOnlineDate && !f.feedback.onlineFlag){%>
                    <input id="update_online_date_<%=f.feedback.id%>" value="<%=f.feedback.onlineEstimateDate%>" >
                    <%}else{%>
                    <label><%=f.feedback.onlineEstimateDate%></label>
                    <%}%>
                </div>
            </div>
        </div>
    </div>
</script>

<div id="update-feedback" title="编辑反馈" class="span9" style="font-size: small; display: none">
    <div id="update_feedback_info"></div>
    <div style="text-align:center">
        <input id="sure_update_feedback" type="button" onclick="updateFeedback()" value="确认"/>
        <input id="close_update_feedback" type="button" onclick="closeDialog('update-feedback')" value="取消"/>
    </div>
</div>

<div id="sure-feedback-online" title="确认上线" class="span9" style="font-size: small; display: none">
    <div id="sure_online_info"></div>
    <div style="text-align:center">
        <input id="sure_online_but" type="button" onclick="onlineFeedback()" value="确认已上线"/>
        <input id="close_online_but" type="button" onclick="closeDialog('sure-feedback-online')" value="取消"/>
    </div>
</div>

<div id="addRelationCodeWrap" title="子需求编号" class="span9" style="font-size: small; display: none">
    <#--<div id="addRelationCodeWrap">-->
        <div class="form-horizontal">
            <div class="modal-body" style="height: auto; overflow: visible;">
                <div class="control-group">
                    <label class="col-sm-2 control-label"><strong>子需求编号</strong></label>
                    <div class="controls">
                        <input class="relationCode" value="" >
                    </div>
                </div>
            </div>
        </div>
    <#--</div>-->
    <div style="text-align:center">
        <input id="sureAddRelationCodeBtn" type="button" onclick="sureAddRelationCode()" value="确认"/>
        <input id="cancelAddRelationCodeBtn" type="button" onclick="closeDialog('addRelationCodeWrap')" value="取消"/>
    </div>
</div>

<script type="text/javascript">
    var pageSize = ${pageSize!0};
    var page = 1;
    function initPagInator(totalCounts) {
        $("#list_foot").jqPaginator({
            totalCounts: totalCounts,
            totalPages: totalCounts / pageSize,
            pageSize: pageSize,
            visiblePages: 7,
            currentPage: 1,
            first: '<li class="first list_style"><a href="javascript:void(0);">第一页</a></li>',
            prev: '<li class="prev list_style"><a href="javascript:void(0);">上一页</a></li>',
            next: '<li class="next list_style"><a href="javascript:void(0);">下一页</a></li>',
            last: '<li class="last list_style"><a href="javascript:void(0);">最后一页</a></li>',
            page: '<li class="page list_style"><a href="javascript:void(0);">{{page}}</a></li>',
            onPageChange: function (num, type) {
                page = num;
                if(type == 'change') {
                    searchFeedbackList2(page);
                }
            }
        });
    }
    function searchFeedbackList2(page) {
        var data = getCondition();
        data["page"] = page;
        $.post("/crm/productfeedback/feedback_list.vpage", data, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $("#fb_table_body").html(template("feedbackListTemp", {list: data.dataList}));
            }
        })
    }

    $(document).on("click", "#selectTeacher", function () {
        if ($("#selectTeacher").is(":checked")) {
            $('#teacher_content').removeAttr('readonly');
        }else{
            $('#teacher_content').attr('readonly',true);
        }
    });
    $(document).on("click", ".sure-online-feedback", function () {
        var feedbackId = $(this).attr("data-feedbackId");
        $.post("/crm/productfeedback/load_feedback_page.vpage", {feedbackId: feedbackId}, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $("#sure_online_info").html(template("sureFeedbackOnline", {f: data}));
                $("#sure-feedback-online").dialog({
                    height: "auto",
                    width: "950",
                    autoOpen: true
                })
            }
        })
    });

    $(document).on("click", ".update-feedback-but", function () {
        var feedbackId = $(this).attr("data-feedbackId");
        $.post("/crm/productfeedback/load_feedback_page.vpage", {feedbackId: feedbackId}, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                $("#update_feedback_info").html(template("updateFeedbackInfo", {f: data}));
                changeFeedbackType('update_feedback_type','updateFirstCategory','updateSecondCategory','updateThirdCategory');
                addDate("update_online_date_"+(data.feedback.id));
                $("#update-feedback").dialog({
                    height: "auto",
                    width: "950",
                    autoOpen: true
                })
            }
        })
    });

    function addDate(id) {
        $("#" + id).datepicker({
            dateFormat: 'yy-mm',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            changeMonth: true,
            changeYear: true,
            minDate: new Date()
        });
    }

    $(document).on("click", ".export_but", function () {
        $("#feedback_list_form").attr({
            "action": "export_feedback_list.vpage",
            "method": "POST"
        });
        var formElement = document.getElementById("feedback_list_form");
        formElement.submit();
    });

    $(function () {
        searchFeedbackList3(1);
        $("#startDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false
        });

        $("#endDate").datepicker({
            dateFormat: 'yy-mm-dd',  //日期格式，自己设置
            monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
            dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate: new Date(),
            numberOfMonths: 1,
            changeMonth: false,
            changeYear: false
        });
    });

    function getUpdateCondition() {
        return {
            id: $("#updateFeedbackId").val(),
            subject: $("#update_subject").val(),
            type: $("#update_feedback_type").val(),
            firstCategory: $("#updateFirstCategory").val(),
            secondCategory: $("#updateSecondCategory").val(),
            thirdCategory: $("#updateThirdCategory").val(),
            onlineEstimateDate: $("#update_online_date" + '_' + $("#updateFeedbackId").val()) ? $("#update_online_date" + '_' + $("#updateFeedbackId").val()).val() : null
        }
    }

    function checkSureOnline() {
        if ($("#teacher_content") && $("#teacher_content").val() && $("#teacher_content").val().length > 0 && !($("#selectTeacher").is(':checked'))) {
            return false;
        }
        return true;
    }

    function onlineFeedback() {
        if (!checkSureOnline()) {
            alert("输入错误！")
        }

        $.post("sure_online.vpage", {
            feedbackId: $("#sure_online_feedback_id").val(),
            noticeFlag: $("#selectTeacher").is(':checked'),
            noticeContent: $("#teacher_content").val()
        }, function (data) {
            if (data.success) {
                alert("确认上线成功");
                closeDialog('sure-feedback-online')
                searchFeedbackList2(page);
            } else {
                alert(data.info);
            }
        })
    }

    function updateFeedback() {
        $.post("update_feedback_info.vpage", getUpdateCondition(), function (data) {
            if (data.success) {
                alert("更新成功");
                closeDialog('update-feedback');
                searchFeedbackList2(page);
            } else {
                alert(data.info);
            }
        })
    }

    function getCondition() {
        return {
            startDate: $("#startDate").val(),
            endDate: $("#endDate").val(),
            subject: $("#subject").val(),
            type: $("#feedbackType").val(),
            status: $("#feedbackStatus").val(),
            firstCategory: $("#firstCategory").val(),
            secondCategory: $("#secondCategory").val(),
            thirdCategory: $("#thirdCategory").val(),
            pmData: $("#pmData").val(),
            online: $("#online").val(),
            content: $("#content").val(),
            feedbackPeople: $("#feedbackPeople").val(),
            teacher: $("#teacher").val(),
            id: $("#feedbackId").val()
        }
    }
    function searchFeedbackList3(page1) {
        var data = getCondition();
        data["page"] = page1;
        $.post("/crm/productfeedback/feedback_list.vpage", data, function (data) {
            if (data.success) {
                if(data.dataList.length > 0){
                    $('#listSize').html(data.size);
                    $("#fb_table_body").html(template("feedbackListTemp", {list: data.dataList}));
                    initPagInator(data.size);
                }else{
                    alert('暂无反馈记录');
                    $("#fb_table_body").html('');
                    $("#list_foot").html('0');
                    $('#listSize').html('0');

                }
            } else {
                alert(data.info);
            }
        })
    }
    $(document).on("click", ".submit_but", function(){
        searchFeedbackList3(1);
    });

    //添加子关联
    $(document).on("click", '#addRelationBtn',function(){
        $("#addRelationCodeWrap").dialog({
            height: "auto",
            width: "500",
            autoOpen: true
        })
    });

    function sureAddRelationCode(){
        var code = $('.relationCode').val();
        var id = $('#updateFeedbackId').val();
        var html = '';
        $.post('/crm/productfeedback/add_relation.vpage', {
            thisId: id,
            addId: code
        }, function (data) {
            if (data.success) {
                alert('添加成功');
                closeDialog('addRelationCodeWrap');
                html = '<tr> <td><a target="_blank" style="color: blue"' +
                        'href="/audit/apply/apply_detail.vpage?applyType=AGENT_PRODUCT_FEEDBACK&applyId='+ code + '">' + code +'</a></td> ' +
                        '<td width="40%"><input class="deleteRelation" type="button" onclick="deleteRelation(this,' + id + ',' +code + ')" value="删除关联"/></td> ' +
                        '</tr>';
                $('#relation-table').append(html);
            } else {
                alert(data.info);
            }
        });


    }

    var CATEGORIES = {};

    function changeFeedbackType(typeId, firstCategoroesId, secondCategoriesId, thirdCategoriesId) {
        var feedbackType = $("#" + typeId).val();
        $.post('/crm/productfeedback/load_category.vpage', {
            typeId: feedbackType
        }, function (data) {
            if (data.success) {
                CATEGORIES = JSON.parse(data.category);
                firstCategories(firstCategoroesId, secondCategoriesId, thirdCategoriesId);
            } else {
                alert(data.info)
            }
        });
    }

    function firstCategories(firstCategoriesId, secondCategoriesId, thirdCategoriesId) {
        $("#" + firstCategoriesId).empty();
        $("#" + firstCategoriesId).append('<option value="0"></option>');
        if (CATEGORIES != null) {
            for (var first in CATEGORIES) {
                $("#" + firstCategoriesId).append("<option value='" + first + "'>" + first + "</option>");
            }
        }
        var category = $("#" + firstCategoriesId).attr("category");
        if (!blankString(category)) {
            $("#" + firstCategoriesId).val(category);
        }
        secondCategories(firstCategoriesId, secondCategoriesId, thirdCategoriesId);
    }

    function secondCategories(firstCategoriesId, secondCategoriesId, thirdCategoriesId) {
        $("#" + secondCategoriesId).empty();
        $("#" + secondCategoriesId).append('<option value="0"></option>');
        var first = $("#" + firstCategoriesId).val();
        if (CATEGORIES != null && !blankString(first)) {
            var seconds = CATEGORIES[first];
            for (var second in seconds) {
                $("#" + secondCategoriesId).append("<option value='" + second + "'>" + second + "</option>");
            }
        }
        var firstDefault = $("#" + firstCategoriesId).attr("category");
        var category = $("#" + secondCategoriesId).attr("category");
        if (!blankString(firstDefault) && first == firstDefault && !blankString(category)) {
            $("#" + secondCategoriesId).val(category);
        }
        thirdCategories(firstCategoriesId, secondCategoriesId, thirdCategoriesId);
    }

    function thirdCategories(firstCategoriesId, secondCategoriesId, thirdCategoriesId) {
        $("#" + thirdCategoriesId).empty();
        $("#" + thirdCategoriesId).append('<option value="0"></option>');
        var first = $("#" + firstCategoriesId).val();
        var second = $("#" + secondCategoriesId).val();
        if (CATEGORIES != null && !blankString(first) && !blankString(second) && first != 0 && second != 0) {
            var thirds = CATEGORIES[first][second];
            for (var i in thirds) {
                var third = thirds[i];
                $("#" + thirdCategoriesId).append("<option value='" + third + "'>" + third + "</option>");
            }
        }
        var category = $("#" + thirdCategoriesId).attr("category");
        if (!blankString(category)) {
            $("#" + thirdCategoriesId).val(category);
        }
    }
    function formatDate(d) {
        var dd = d.getDate() < 10 ? "0" + d.getDate() : d.getDate().toString();
        var mm = d.getMonth() < 9 ? "0" + (d.getMonth() + 1) : (d.getMonth() + 1).toString();
        var yyyy = d.getFullYear().toString();
        return yyyy + "-" + mm + "-" + dd
    }

    function addRelation(thisId, addId){
        $.post('/crm/productfeedback/add_relation.vpage', {
            thisId: thisId,
            addId: addId
        }, function (data) {
            if (data.success) {
            } else {
                alert(data.info);
            }
        });

    }
    function deleteRelation(ele,thisId, deleteId){
        console.log(thisId)
        $.post('/crm/productfeedback/delete_relation.vpage', {
            thisId: thisId,
            deleteId: deleteId
        }, function (data) {
            if (data.success) {
                $(ele).parents('tr').remove();
            } else {
                alert(data.info);
            }
        });
    }
</script>
</@layout_default.page>
