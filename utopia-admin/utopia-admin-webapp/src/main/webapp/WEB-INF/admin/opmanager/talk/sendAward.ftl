<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='17说' page_num=9>

<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>

<div class="span9">
    <legend>
        <strong>发送17说通知</strong>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <form id="replyForm" class="well form-horizontal" method="post" action="/opmanager/talk/sendAward.vpage">
                <fieldset>
                    <legend>发送17说通知</legend>
                    <div class="control-group">
                        <label class="control-label">通知类型：</label>
                        <div class="controls">
                            <select name="noticeType">
                                <option value="1">获奖通知</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">通知文案：</label>
                        <div class="controls" data="noticeMessage">
                            <textarea class="form-control span8"
                                      placeholder="通知文案"
                                      name="noticeMessage" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">目标用户：</label>
                        <div class="controls">
                            <textarea class="form-control span8"
                                      placeholder="用户id使用逗号分隔，例如：123,456"
                                      name="noticeUserId" rows="3"></textarea>

                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="submit" id="saveBtn" value="保存" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
</@layout_default.page>