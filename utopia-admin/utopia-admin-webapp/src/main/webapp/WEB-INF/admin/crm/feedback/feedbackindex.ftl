<#-- @ftlvariable name="feedbackType" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->
<#-- @ftlvariable name="unmask" type="java.lang.String" -->
<#-- @ftlvariable name="feedbackQuickReplyList" type="java.util.List" -->
<#-- @ftlvariable name="feedbackStateMap" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
 <#-- @ftlvariable name="feedbackInfoList" type="java.util.List<java.util.Map>" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="用户反馈查询" page_num=3>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<#assign operationMap = {"0" : "确认", "1" : "解决", "2" : "关闭", "3" : ""}/>
<div class="span9">
    <div>
        <form method="post" action="?" class="form-horizontal">
            <fieldset>
                <legend>用户反馈查询&nbsp;
                    <#--<a href="/crm/feedback/tag/index.vpage">用户反馈Tag管理</a>&nbsp;-->
                </legend>
            </fieldset>
            <ul class="inline form_datetime">
                <li>
                    <label for="userId">
                        反馈ID
                        <input name="feedbackId" type="text" value="${feedbackId!}" style="width: 100px;"/>
                    </label>
                </li>
                <li>
                    <label for="userId">
                        用户ID
                        <input name="userId" type="text" style="width: 100px;"/>
                    </label>
                </li>
                <li>
                    <label>
                        反馈内容
                        <input name="content" type="text" placeholder="支持模糊查询" value="${content!''}" style="width: 100px;" />
                    </label>
                </li>

                <li>
                    <label for="startDate">
                        起始时间
                        <input name="startDate" id="startDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
                <li>
                    <label for="endDate">
                        截止时间
                        <input name="endDate" id="endDate" type="text" placeholder="格式：2013-11-04"/>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="userType">
                        用户类型
                        <select name="userType" id="userType">
                            <option value='-1' selected="selected">全部</option>
                            <option value='1'>老师</option>
                            <option value='2'>家长</option>
                            <option value='3'>学生</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="feedbackState">
                        反馈状态
                        <select name="feedbackState" id="feedbackState">
                            <option value='-1'>全部</option>
                            <#if feedbackStateMap?has_content>
                                <#assign keys = feedbackStateMap?keys/>
                                <#list keys as key>
                                    <option value="${key}" <#if key == "0">selected="selected"</#if>>${feedbackStateMap[key]}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label>
                        反馈类型<select name="feedbackType">
                            <option value="">全部</option>
                            <#if feedbackTypeList?has_content>
                                <#list feedbackTypeList as subFeedbackType>
                                    <option value="${subFeedbackType}" <#if subFeedbackType = feedbackType!''>selected="selected"</#if>>${subFeedbackType!}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label>
                        问题分类
                        <input name="feedbackSubType1" type="text" value="${feedbackSubType1!}" style="width: 100px;"/>
                    </label>
                </li>
                <li>
                    <label for="deliverState">
                        转发状态
                        <select name="deliverState" id="deliverState">
                            <option value='-1' selected="selected">全部</option>
                            <option value='0'>未转发</option>
                            <option value='1'>已转发</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="watcher">
                        跟踪者
                        <select name="watcher" id="watcher_filter">
                            <option value='-1' selected="selected">全部</option>
                            <#if feedbackWatchers?has_content>
                                <#list feedbackWatchers as watcher>
                                    <option value="${watcher.WATCHER_NAME!}">${watcher.WATCHER_NAME!}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="tag">
                        Tag
                        <select name="tag" id="tag_filter">
                            <option value='-1' selected="selected">全部</option>
                        </select>
                    </label>
                </li>
                <li>
                    <button type="submit" class="btn btn-success">查询</button>
                </li>
            </ul>
        </form>
    </div>
    <div>
        <fieldset><legend>查询结果</legend></fieldset>
        <div>
            <ul class="inline">
                <li>
                    <label>修改状态
                        <select id="select_batch_state">
                            <#if feedbackStateMap?has_content>
                                <#assign keys = feedbackStateMap?keys/>
                                <#list keys as key>
                                    <option value="${key}">${feedbackStateMap[key]}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <button class="btn btn-primary" id="batch_state_button">确定</button>
                </li>

                <li>|</li>

                <li>
                    <form action="downloadexcel.vpage" method="post">
                        <input type="hidden" name="startDate" value="${startDate!''}">
                        <input type="hidden" name="endDate" value="${endDate!''}">
                        <input type="hidden" name="userType" value="${userType!'-1'}">
                        <input type="hidden" name="feedbackState" value="${feedbackState!'-1'}">
                        <input type="hidden" name="content" value="${content!}">
                        <input type="hidden" name="feedbackId" value="${feedbackId!}">
                        <input type="hidden" name="feedbackType" value="${feedbackType!}">
                        <input type="hidden" name="feedbackSubType1" value="${feedbackSubType1!}"/>
                        <button type="submit" class="btn btn-primary">导出excel</button>
                    </form>
                </li>
                <li>
                    <button class="btn btn-primary" id="batch_reply_button">批量回复</button>
                </li>
                <li>
                    <button class="btn btn-success" id="batch_send_email_button"> 转发</button>
                </li>
                <li>
                    <button class="btn btn-danger" id="batch_delete_button"> 删除</button>
                </li>
                <li>
                    <button class="btn btn-primary" id="batch_tag_button"> 标记tag</button>
                </li>
            </ul>
        </div>
        <br/>
        <table class="table table-striped table-bordered" style="font-size: 14px;">
            <thead>
                <tr>
                    <th><label><input type="checkbox" id="all_select"/></label></th>
                    <th style="width: 90px;max-width:90px;">反馈时间</th>
                    <th>用户姓名/ID</th>
                    <th>反馈类型</th>
                    <th>问题分类</th>
                    <th>反馈内容</th>
                    <th>用户身份</th>
                    <th>练习类型</th>
                    <th>解决办法</th>
                    <th>跟踪者:Tag</th>
                    <th style="width: 80px;">操作</th>
                    <th style="width: 80px;">回复</th>
                </tr>
            </thead>
            <#if feedbackInfoList?has_content>
                <#list feedbackInfoList as feedbackInfo>
                    <tr <#if feedbackInfo.state gt 0>class="warning"</#if> data-id="${feedbackInfo.id}" data-user_id="${feedbackInfo.userId}">
                        <td style="width: 20px;max-width:20px;">
                            <label><input type="checkbox" id="checkbox_${feedbackInfo.id}"/></label>
                        </td>
                        <td nowrap>
                            ${feedbackInfo.createDatetime?string('yyyy-MM-dd')}<br />
                            ${feedbackInfo.createDatetime?string('HH:mm:ss')}
                        </td>
                        <td>
                            <div style="width: 100px;max-width: 100px;">
                                <a title="${feedbackInfo.id}" href="../user/userhomepage.vpage?userId=${feedbackInfo.userId}">${feedbackInfo.realName!}</a>(${feedbackInfo.userId})
                                <a target="_blank" href="http://stat.log.17zuoye.net/nodestat?userId=${feedbackInfo.userId}&date=${today?string('yyyyMMdd')}"><span class="icon-book"></span></a>
                                <br />
                                ${feedbackInfo.address!}
                            </div>
                        </td>
                        <td nowrap style="width: 90px;max-width:90px;">${feedbackInfo.feedbackType!}</td>
                        <td><div style="width: 100px;max-width:100px;">${feedbackInfo.feedbackSubType1!}</div></td>
                        <td style="width: 200px;max-width:200px;"><div style="word-break:break-all;  word-wrap:break-word; width: 150px;">
                            <#if (feedbackInfo.feedbackType == '移动版问题' || feedbackInfo.feedbackType == '中学移动问题') && (feedbackInfo.extStr2)?has_content>
                                ${feedbackInfo.extStr2!}
                                <#if (feedbackInfo.practiceName)?has_content>
                                    练习类型:${feedbackInfo.practiceType!}<br>
                                    练习名称:${feedbackInfo.practiceName!}<br>
                                </#if>
                                用户反馈内容：
                            </#if>
                            ${feedbackInfo.content?html}
                            <#if feedbackInfo.reply?has_content><br />--<br />回复：<br /><i class="icon-star"></i>${(feedbackInfo.reply?html)?replace('@!=@','<br /><i class="icon-star"></i>')}</#if>
                        </div></td>
                        <td nowrap style="width: 110px;max-width:120px;">
                            <#if feedbackInfo.userType = 1>老师<#elseif feedbackInfo.userType = 2>家长<#else >学生</#if><#if feedbackInfo.studentKeyParentId?has_content><i class="icon-user"></i></#if>
                            <#if feedbackInfo.feedbackUser?? && feedbackInfo.feedbackUser.profile.sensitiveQq??>(QQ)</#if>
                            <#if feedbackInfo.contactQq?has_content><br />联系QQ号:${feedbackInfo.contactQq}</#if>
                            <#if feedbackInfo.contactPhone?has_content><br />联系电话:${feedbackInfo.contactPhone}</#if>
                        </td>
                        <td nowrap style="width: 90px;max-width:90px;">
                            ${feedbackInfo.practiceName!}<br />
                            <#switch feedbackInfo.feedbackType>
                                <#case "英语同步试题">
                                    <#if (feedbackInfo.extStr2)?has_content>
                                        <a href="${urlPrefix}/container/viewpaper.vpage?qid=${feedbackInfo.extStr2!}" target="_blank">${feedbackInfo.extStr2!}</a>
                                    </#if>
                                    <#break />
                                <#case "数学同步试题">
                                    <#if (feedbackInfo.extStr2)?has_content>
                                        <a href="${urlPrefix}/container/viewpaper.vpage?qid=${feedbackInfo.extStr2!}" target="_blank">${feedbackInfo.extStr2!}</a>
                                    </#if>
                                    <#break />
                                <#case "数学基础作业">
                                    <#if (feedbackInfo.extStr1)?has_content>
                                        <a href="../content/index.vpage?subjectId=102&lessonId=${feedbackInfo.extStr1!}&feedbackId=${feedbackInfo.id}" target="_blank">${feedbackInfo.extStr1!}</a>
                                        <a href="${urlPrefix}/flash/loader/mathselfstudy-${feedbackInfo.practiceType!}-0-0-0-0-${feedbackInfo.extStr1!}.vpage" target="_blank"><span class="icon-eye-open"></span></a>
                                    </#if>
                                    <#break />
                                <#default>
                                    <#if (feedbackInfo.extStr1)?has_content>
                                        <a href="../content/index.vpage?subjectId=103&lessonId=${feedbackInfo.extStr1!}&feedbackId=${feedbackInfo.id}" target="_blank">${feedbackInfo.extStr1!}</a>
                                        <a href="${urlPrefix}/flash/loader/selfstudy-${feedbackInfo.practiceType!}-0-0-0-${feedbackInfo.extStr1!}.vpage" target="_blank"><span class="icon-eye-open"></span></a>
                                    </#if>
                            </#switch>
                        </td>
                        <td nowrap style="width: 80px;max-width:80px;" id="comment_${feedbackInfo.id}">${feedbackInfo.comment!}</td>
                        <#--<td nowrap style="width: 100px;max-width:100px;" id="deliver_${feedbackInfo.id}">${feedbackInfo.deliverReason!}</td>-->
                        <td nowrap style="width: 100px;max-width:100px;" id="tag_${feedbackInfo.id}">
                            <#--<a href="/crm/feedback/feedbackindex.vpage?watcher=${feedbackInfo.watcher!}&startDate=2013-01-01">${feedbackInfo.watcher!}</a>:<br/>-->
                            <#--<a href="/crm/feedback/feedbackindex.vpage?tag=${feedbackInfo.tag!}&startDate=2013-01-01">${feedbackInfo.tag!}</a>-->
                            <#if feedbackInfo.tag?has_content>
                                ${feedbackInfo.watcher!}:<br/>${feedbackInfo.tag!}
                            </#if>
                        </td>
                        <td nowrap>
                            <span id="state_${feedbackInfo.id}">${feedbackStateMap[feedbackInfo.state?string]}</span><br />
                            <a href="javascript:void(0)" id="change_state_${feedbackInfo.id}" data-state="${feedbackInfo.state}">${operationMap[feedbackInfo.state?string]}</a>
                            <br/>
                            <a href="javascript:void(0)" id="delete_item_${feedbackInfo.id}"><#if feedbackInfo.state = 0>删除</#if></a>
                            <br/>
                            <a href="javascript:void(0)" id="manage_tag_${feedbackInfo.id}">标记tag</a>
                        </td>
                        <td nowrap>
                            <a href="javascript:void(0)" id="reply_feedback_${feedbackInfo.id}" data-user_id="${feedbackInfo.userId}">回复</a>
                            <#--<a href="javascript:void(0)" id="replied_feedback_${feedbackInfo.id}" title="${feedbackInfo.reply!}" data-content="${feedbackInfo.content?html}"-->
                                    <#--data-user_id="${feedbackInfo.userId}">${(feedbackInfo.reply?has_content)?string('已回复','')}</a>-->
                            <#--<a href="javascript:void(0)" id="send_email_a_${feedbackInfo.id}" data-feedback_id="${feedbackInfo.id}">转发</a>-->
                        </td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<div id="reply_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>回复用户<span id="user_id"></span></h3>
        <input type="hidden" id="feedback_id"/>
    </div>
    <div class="modal-body">
        <div class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dl>
                        <dt>请填写回复内容</dt>
                        <dd><label><textarea id="reply_content" cols="35" rows="4" value=""></textarea></label></dd>
                    </dl>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dl>
                        <dt>快捷回复内容：</dt>
                        <dd>
                            <label><select id="feedback_quick_reply_content">
                                <option value="" selected="selected">无</option>
                                <#if feedbackQuickReplyList?has_content>
                                    <#list feedbackQuickReplyList as feedbackQuickReply>
                                        <option value="${feedbackQuickReply!}">${feedbackQuickReply!}</option>
                                    </#list>
                                </#if>
                            </select></label>
                        </dd>
                    </dl>
                </li>
            </ul>
        </div>
    </div>
    <div class="modal-footer">
        <button id="reply_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="batch_reply_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量回复用户</h3>
    </div>
    <div class="modal-body">
        <div class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dl>
                        <dt>请填写回复内容</dt>
                        <dd><label><textarea id="batch_reply_content" cols="35" rows="4" value=""></textarea></label></dd>
                    </dl>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dl>
                        <dt>快捷回复内容：</dt>
                        <dd>
                            <label><select id="feedback_quick_batch_reply_content">
                                <option value="" selected="selected">无</option>
                                <#if feedbackQuickReplyList?has_content>
                                    <#list feedbackQuickReplyList as feedbackQuickReply>
                                        <option value="${feedbackQuickReply!}">${feedbackQuickReply!}</option>
                                    </#list>
                                </#if>
                            </select></label>
                        </dd>
                    </dl>
                </li>
            </ul>
        </div>
    </div>
    <div class="modal-footer">
        <button id="batch_reply_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="replied_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>回复用户<span id="replied_user_id"></span></h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户反馈内容</dt>
                    <dd><textarea id="replied_content" cols="35" rows="4" readonly="readonly"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>回复内容</dt>
                    <dd><textarea id="replied_reply_content" cols="35" rows="4" readonly="readonly"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
    </div>
</div>

<div id="send_email_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>转发反馈<span id="replied_user_id"></span></h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt>转发原因：</dt>
            <dd><textarea id="send_email_dialog_text" cols="35" rows="4" placeholder=""></textarea></dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="send_email_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        <input type="hidden" id="send_email_feedback_id"/>
    </div>
</div>

<div id="add_comment_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>解决反馈<span id="replied_user_id"></span></h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl>
            <dt>解决办法：</dt>
            <dd><textarea id="add_comment_dialog_text" cols="35" rows="4"></textarea></dd>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="add_comment_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        <input type="hidden" id="add_comment_feedback_id"/>
    </div>
</div>

<div id="add_tag_on_feedback" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加Tag关联</h3>
    </div>
    <div class="modal-body">
        <#--筛选 <input name="tag_filter" id="tag_filter" placeholder="筛选条件..." style="width:150px">-->
        <#--<button name="btn_delete_filter" id="btn_delete_filter">&times;</button>-->
        <#--<input type="checkbox" id="tag_hide_mode">只显示匹配项-->
        <#---->
        <textarea id="tag_message" rows="3"></textarea>
    </div>
    <div class="modal-body" style=" max-height: 160px;">
        <div class="dl-horizontal" id="tagtree" style="width:400px;">
        </div>
    </div>
    <div class="modal-footer">
        <button id="add_tag_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="curfeedbackid" value=""/>
</div>

<div id="batch_add_tag_on_feedback" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>批量添加Tag关联</h3>
    </div>
    <div class="modal-body">
        筛选 <input name="tag_filter" id="tag_filter" placeholder="筛选条件..." style="width:150px">
        <button name="btn_delete_filter" id="btn_delete_filter">&times;</button>
        <input type="checkbox" id="tag_hide_mode">只显示匹配项
    </div>
    <div class="modal-body">
        <div class="dl-horizontal" id="batchtagtree" style="width:400px">
        </div>
    </div>
    <div class="modal-footer">
        <button id="batch_add_tag_dialog_btn_ok" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="curfeedbackid" value=""/>
</div>


<script type="text/javascript">

    $(function(){
        $("#startDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        $("#endDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });
    });
    var sltData = {
        no_results_text: "没有符合您要找的Tag！",
        disable_search_threshold: 10,
        width: "300px"
    };
    $(function(){
        $(".chosen-select").chosen(sltData);
        // init
        $('#startDate').val('${startDate!''}');
        $('#endDate').val('${endDate!''}');
        $('#userType').val('${userType!'-1'}');
        $('input[name="userId"]').val('${userId!}');

        function updateState(id, state) {
            var $that = $('#change_state_' + id);
            $that.data('state', state);
            var $delete = $('#delete_item_' + id);
            var $state = $('#state_' + id);
            var $reply = $('#reply_feedback_' + id);
//            var replied = feedbackWatchers.length > 0;
            switch(parseInt(state)) {
                case 0:
                    $state.text('${feedbackStateMap["0"]}');
                    $that.text('${operationMap["0"]}');
                    $delete.text('删除');
                    $that.parent().parent().removeClass("warning");
//                    if(!replied) {
//                        $reply.text('回复');
//                    }
                    $reply.text('回复');
                    break;
                case 1:
                    $state.text('${feedbackStateMap["1"]}');
                    $that.text('${operationMap["1"]}');
                    $delete.text('');
                    $that.parent().parent().addClass("warning");
//                    if(!replied) {
//                        $reply.text('回复');
//                    }
                    $reply.text('回复');
                    break;
                case 2:
                    $state.text('${feedbackStateMap["2"]}');
                    $that.text('${operationMap["2"]}');
                    $delete.text('');
                    $that.parent().parent().addClass("warning");
//                    $reply.text('');
                    break;
                case 3:
                    $state.text('${feedbackStateMap["3"]}');
                    $that.text('${operationMap["3"]}');
                    $delete.text('');
                    $that.parent().parent().addClass("warning");
                    $reply.text('');
                    break;
            }
        }


        $('[id^="change_state_"]').on('click', function(){
            var $this = $(this);
            var feedbackId = $this.closest('tr').data('id');
            var state = $this.data('state');
            if("1" == state) {
                $('#add_comment_dialog_text').val('');
                $('#add_comment_feedback_id').val(feedbackId);
                $('#add_comment_dialog').modal('show');
            } else {
                $.getJSON('editstate.vpage?feedbackId=' + feedbackId + '&state=' + state, function(data){
                    if(data.success){
                        updateState(feedbackId, data.state);
                    }
                });
            }
        });

        $('#add_comment_dialog_btn_ok').click(function() {
            var comment =  $('#add_comment_dialog_text').val();
            if(comment == ""){
                alert("请填写解决办法");
                return false;
            }
            var postData = {
                comment : comment,
                feedbackIdList : $('#add_comment_feedback_id').val().split(',')
            };
            $.post('addfeedbackcomment.vpage', postData, function(data) {
                alert(data.info);
                if (data.success) {
                    for (var i in postData.feedbackIdList) {
                        updateState(postData.feedbackIdList[i], data.state);
                        $("#comment_"+postData.feedbackIdList[i]).html(comment);
                    }
                    $('#add_comment_dialog').modal('hide');
                }
            });
        });

        $('[id^="delete_item_"]').on('click', function() {
            var $this = $(this);
            var $parent = $this.closest('tr');
            var feedbackId = $parent.data('id');

            if(confirm('确定要删除此条反馈？')) {
                $.getJSON('deletefeedback.vpage?feedbackId=' + feedbackId, function(data) {
                    alert(data.info);
                    if(data.success) {
                        $parent.remove();
                    }
                });
            }
        });

        $('[id^="reply_feedback_"]').on('click', function() {
            var $this = $(this);
            $('#feedback_id').val($this.closest('tr').data('id'));
            $('#user_id').text($this.data('user_id'));
            $('#reply_content').val('');
            $('#feedback_quick_reply_content').val('');
            $('#reply_dialog').modal('show');
        });

        $('#reply_dialog_btn_ok').on('click', function() {
            var feedbackId = $('#feedback_id').val();
            var feedbackMap = {};
            feedbackMap[feedbackId] = $('#user_id').text();
            var data = {
                feedbackMapJson : JSON.stringify(feedbackMap),
                reply       :   $('#reply_content').val()
            };
            $.post('replyfeedback.vpage', data, function(data) {
                if(data.success) {
                    $('#reply_dialog').modal('hide');
//                    $('#reply_feedback_' + feedbackId).text('');
//                    var $that = $('#replied_feedback_' + feedbackId);
//                    $that.text('回复');
//                    $that.attr('title', data.reply);
//                    updateState(feedbackId, data.state)
                }
                alert(data.info);
            });
        });

        $('#batch_reply_button').on('click', function() {
            $('#batch_reply_content').val('');
            $('#feedback_quick_batch_reply_content').val('');
            $('#batch_reply_dialog').modal('show');
        });

        $('#batch_reply_dialog_btn_ok').on('click', function() {

            var feedbackMap = {};
            var $checkeds = $('input[id^="checkbox_"]:checked');
            // 所选记录是否存在未回复的记录
            var hasValidReply = false;
            var feedbackIds = [];
            $checkeds.closest('tr').each(function() {
                var feedbackId = $(this).data('id');
                if($('#reply_feedback_' + feedbackId).text() != '') {
                    feedbackMap[$(this).data('id')] = $(this).data('user_id');
                    feedbackIds.push(feedbackId);
                    hasValidReply = true;
                } else {
                    $('#checkbox_' + feedbackId).removeAttr('checked');
                }
            });
            if(!hasValidReply) {
                alert('请先选择未回复的用户');
                $('#batch_reply_dialog').modal('hide');
                return;
            }

            var postData = {
                feedbackMapJson : JSON.stringify(feedbackMap),
                reply : $('#batch_reply_content').val()
            };
            $.post('replyfeedback.vpage', postData, function(data) {

                var successFeedbackIds = feedbackIds;
                if(successFeedbackIds.length > 0) {

                    for(var i in successFeedbackIds) {

                        var feedbackId = successFeedbackIds[i];
                        $('#reply_feedback_' + feedbackId).text('');
                        var $that = $('#replied_feedback_' + feedbackId);
                        $that.text('已回复');
                        $that.attr('title', data.reply);
//                        updateState(feedbackId, data.state);
                        $('#checkbox_' + feedbackId).removeAttr('checked');
                    }
                }
                alert(data.info);
                $('#batch_reply_dialog').modal('hide');

            });

        });

        $('[id^="replied_feedback_"]').on('click', function() {
            var $this = $(this);
            $('#replied_user_id').text($this.data('user_id'));
            $('#replied_reply_content').val($this.attr('title'));
            $('#replied_content').val($this.data('content'));
            $('#replied_dialog').modal('show');
        });

        $('#batch_state_button').on('click', function() {
            var ids = [];
            var $checkeds = $('input[id^="checkbox_"]:checked');
            $checkeds.closest('tr').each(function() {
                ids.push($(this).data('id'));
            });

            var postData = {
                ids : ids,
                state : $('#select_batch_state').val()
            };
            if(ids.length) {

                if ("2" == postData.state) {
                    $('#add_comment_dialog_text').val('');
                    $('#add_comment_feedback_id').val(ids.join(','));
                    $('#add_comment_dialog').modal('show');
                } else {
                    $.post("batchstate.vpage", postData, function(data) {
                        if(data.success) {
                            for(var i = 0; i < ids.length; i++) {
                                updateState(postData.ids[i], postData.state);
                            }
                            $checkeds.removeAttr('checked');
                        }
                        alert(data.info);
                    });
                }
            }
        });

        $('#batch_delete_button').on('click', function() {

            if(confirm('确定要删除选中的反馈？')) {

                var ids = [];
                var $checkeds = $('input[id^="checkbox_"]:checked');
                $checkeds.closest('tr').each(function() {
                    ids.push($(this).data('id'));
                });

                $.post('batchdelete.vpage', {ids : ids}, function(data) {
                    alert(data.info);
                    if(data.success) {
                        for(var i = 0; i < ids.length; i++) {
                            $('tr[data-id="' + ids[i] + '"]').remove();
                        }
                    }
                });

            }
        });

        $('#batch_tag_button').on('click', function() {

            var $checkeds = $('input[id^="checkbox_"]:checked');
            if($checkeds.length == 0){
                alert("请选择要标记tag的feedback。");
                return false;
            }
            $('#batch_tags').html('');
            $("#batch_watcher").attr("value",'0');
            $('#batch_add_tag_on_feedback').modal('show');
        });

        $('#batch_add_tag_dialog_btn_ok').on('click', function() {

            var ids = [];
            var $checkeds = $('input[id^="checkbox_"]:checked');
            $checkeds.closest('tr').each(function() {
                ids.push($(this).data('id'));
            });

            var tagId = getSelectedTagId("batchtagtree");
            if(!tagId){
                alert("请选择Tag");
                return false;
            }
            $.post('batchaddtag.vpage', {ids : ids,tagId:parseInt(tagId)}, function(data) {
                if(data.success) {
                    for(var i = 0; i < ids.length; i++) {
                        $('#tag_'+ids[i]).html(data.tags.watcherName+":<br>"+data.tags.name);
                    }
                }
            });
            $('#batch_add_tag_on_feedback').modal('hide');
            var tree = $("#batchtagtree").fancytree("getTree");
            tree.visit(function(node){
                node.setSelected(false);
            });
        });



        $('input[id^="checkbox_"]').on('click', function() {
            if(!$(this).prop('checked')) {
                $('#all_select').prop('checked', false);
            }
        });

        $('#all_select').on('click', function() {
            $('input[id^="checkbox_"]').prop('checked', $(this).prop('checked'));
        });

        /**
         * 回复用户对话框，默认回复内容下拉框单击事件
         */
        $('#feedback_quick_reply_content').click(function() {
            $('#reply_content').val($(this).val());
        });

        /**
         * 批量回复用户对话框，默认回复内容下拉框单击事件
         */
        $('#feedback_quick_batch_reply_content').click(function() {
            $('#batch_reply_content').val($(this).val());
        });

        /**
         * 发送邮件
         */
        $('#batch_send_email_button').click(function() {
            $('#send_email_dialog_text').val('');
            var ids = [];
            var $checkeds = $('input[id^="checkbox_"]:checked');
            $checkeds.closest('tr').each(function() {
                ids.push($(this).data('id'));
            });
            $('#send_email_feedback_id').val(ids.join(','));
            $('#send_email_dialog').modal('show');
        });

        $('#send_email_dialog_btn_ok').click(function() {
            // 邮箱地址
            var textContent = $('#send_email_dialog_text').val();

            if(textContent == ""){
                alert('请输入转发原因');
                return;
            }
            // 反馈id
            var ids = [];
            var idsStr = $('#send_email_feedback_id').val();
            if (idsStr.length > 0) {
                ids = idsStr.split(',');
            }

            if (ids.length == 0) {
                alert('请选择要发送的反馈');
                return;
            }

            var postData = {
                textContent :   textContent,
                ids         :   ids
            };
            $.post('sendfeedbackbyemail.vpage', postData, function(data) {
                alert(data.info);
                if (data.success) {
                    for(var i in ids) {
                        var feedbackId = ids[i];
                        $('#reply_feedback_' + feedbackId).text('');
                        updateState(feedbackId, data.state);
                        $('#checkbox_' + feedbackId).removeAttr('checked');
                        $('#deliver_'+feedbackId).text(textContent);
                    }
                    $('#send_email_dialog').modal('hide');
                }
            });

        });

        $('[id^="send_email_a_"]').click(function() {
            $('#send_email_dialog_text').val('');
            $('#send_email_feedback_id').val($(this).data('feedback_id'));
            $('#send_email_dialog').modal('show');
        });

        $('[id^="manage_tag_"]').click(function() {
            var id = $(this).attr("id").substring("manage_tag_".length);
            $('#curfeedbackid').val(id);
//            var tag = $('#tag_'+id).html().trim();
//            if(tag == ''){
//
//            }
            $('#tags').html('');
            $("#watcher").attr("value",'0');
            $("#tag_message").val('');
            $('#add_tag_on_feedback').modal('show');
        });

        $('#watcher').on('change',function(){

            var watcher = $('#watcher').find('option:selected').val();
            if(watcher == '0'){
                alert("请选择跟踪者");
                return false;
            }

            $.post('loadtags.vpage',{
                watcher:watcher
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $('#tags').html('');
                    for(var i=0; i<data.tags.length; i++){
                        $('<option value="'+data.tags[i].name+'">'+data.tags[i].name+'</option>').appendTo($('#tags'));
                    }
                }
            });
        });

        $('#batch_watcher').on('change',function(){

            var watcher = $('#batch_watcher').find('option:selected').val();
            if(watcher == '0'){
                alert("请选择跟踪者");
                return false;
            }

            $.post('loadtags.vpage',{
                watcher:watcher
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    $('#batch_tags').html('');
                    for(var i=0; i<data.tags.length; i++){
                        $('<option value="'+data.tags[i].name+'">'+data.tags[i].name+'</option>').appendTo($('#batch_tags'));
                    }
                }
            });
        });

//        $('#watcher_filter').on('change',function(){
//
//            var watcher = $('#watcher_filter').find('option:selected').val();
//            if(watcher == '0'){
//                alert("请选择跟踪者");
//                return false;
//            }
//
//            $.post('loadtags.vpage',{
//                watcher:watcher
//            },function(data){
//                if(!data.success){
//                    alert(data.info);
//                }else{
//                    $('#tag_filter').html('<option value=\'-1\' selected=\"selected\">全部</option>');
//                    for(var i=0; i<data.tags.length; i++){
//                        $('<option value="'+data.tags[i].name+'">'+data.tags[i].name+'</option>').appendTo($('#tag_filter'));
//                    }
//                }
//            });
//        });

        $('#add_tag_dialog_btn_ok').on('click',function(){

            $(this).attr("disabled","true")
            var tagId = getSelectedTagId("tagtree");
            if(!tagId){
                alert("请选择Tag");
                return false;
            }
            var id = $('#curfeedbackid').val();
            var tagMessage = $('#tag_message').val().trim();
            $.post('addtagtofeedback.vpage',{
                tagId:parseInt(tagId),
                feedbackId:id,
                tagMessage:tagMessage
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
//                    $('#tag_'+id).html("<a href=\"/crm/feedback/feedbackindex.vpage?watcher="+watcher+"&startDate=2013-01-01\">"+watcher+"</a>:<br>"+
//                            "<a href=\"/crm/feedback/feedbackindex.vpage?tag="+tag+"&startDate=2013-01-01\">"+tag+"</a>");
                    $('#tag_'+id).html(data.tags.watcherName+":<br>"+data.tags.name);
                }
                $('#add_tag_on_feedback').modal('hide');
                var tree = $("#tagtree").fancytree("getTree");
                // Expand all tree nodes
                tree.visit(function(node){
                    node.setSelected(false);
                });
                $("#add_tag_dialog_btn_ok").removeAttr("disabled");
            });
        });

        $("#tagtree ").fancytree({
            extensions: ["filter"],
            source: {
                url: "tag/loadtagtree.vpage",
                cache:true
            },
            checkbox: true,
            selectMode: 1,
            init: function(event, data, flag) {
                var tree = $("#tagtree").fancytree("getTree");
                tree.visit(function(node){
                    if (node.data.type == 'root') {
                        node.setExpanded(true);
                    }
                });
            }
        });
        $("#batchtagtree").fancytree({
            extensions: ["filter"],
            source: {
                url: "tag/loadtagtree.vpage",
                cache:true
            },
            checkbox: true,
            selectMode: 1,
            init: function(event, data, flag) {
                var tree = $("#batchtagtree").fancytree("getTree");
                tree.visit(function(node){
                    if (node.data.type == 'root') {
                        node.setExpanded(true);
                    }
                });
            }
        });

        $("input[name=tag_filter]").keyup(function(e){
            var match = $(this).val();
            if(e && e.which === $.ui.keyCode.ESCAPE || $.trim(match) === ""){
                $("button#btn_delete_filter").click();
                return;
            }

            var tagTree = $("#tagtree").fancytree("getTree");
            tagTree.applyFilter(match);
        }).focus();

        $('#btn_delete_filter').on('click',function(){
            $("#tag_filter").val("");
            var tagTree = $("#tagtree").fancytree("getTree");
            tagTree.clearFilter();
        });

        $('#tag_hide_mode').on('change',function(){
            var tagTree = $("#tagtree").fancytree("getTree");
            tagTree.options.filter.mode = $(this).is(":checked") ? "hide" : "dimm";
            tagTree.clearFilter();
            $("input[name=tag_filter]").keyup();
        });

    });
    function getSelectedTagId(tree){
        var tagTree = $("#"+tree).fancytree("getTree");
        var tag = tagTree.getSelectedNodes();
        if(tag == null || tag == "undefined") return null;
        var tagIdArray = new Array();
        var tagIds = $.map(tag, function(node){
            return node.key;
        });
        return tagIds.join(",");
    }
</script>
</@layout_default.page>