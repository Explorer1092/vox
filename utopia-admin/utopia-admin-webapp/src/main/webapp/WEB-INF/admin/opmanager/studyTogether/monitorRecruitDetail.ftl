<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='班长招募详情' page_num=9>
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }

    .control-group {
        margin-top: 10px;
        margin-bottom: 80px;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>班长招募详情</legend>
    </fieldset>
<#--<div class="span12">-->
    <div class="control-group">
        <label class="control-label" for="productName">家长ID：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.parentId!''}"
                   name="parentId" id="parentId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">手机号：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.phone!''}"
                   name="phone" id="phone" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">家长姓名：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.parentName!''}"
                   name="parentName" id="parentName" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">微信号：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.inputWechatId!''}"
                   name="inputWechatId" id="inputWechatId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">城市：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.city!''}"
                   name="pageId" id="pageId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">孩子生日：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.birthday!''}"
                   name="birthday" id="birthday" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">学历：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.education!''}"
                   name="education" id="education" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">职业：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.profession!''}"
                   name="profession" id="profession" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">申请时选择的课程：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.lessonName!''}"
                   name="lessonName" id="lessonName" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">当前学习群号：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.currentWechatId!''}"
                   name="currentWechatId" id="currentWechatId" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">历史学习课程：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.historyLessonName!''}"
                   name="historyLessonName" id="historyLessonName" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div class="control-group">
        <label class="control-label" for="productName">每天可支配的时间：</label>
        <label for="title">
            <input type="text" value="${MonitorRecruitInfo.time!''}"
                   name="time" id="time" maxlength="50"
                   style="width: 20%" class="input" readonly="readonly">
        </label>
    </div>
    <div style="margin-top: 10px">
        <label class="control-label" for="productName">性格特点：</label>
        <div class="controls">
            <label for="content">
                <textarea name="content" rows="10" cols="30" style="width: 1000px"
                          readonly="readonly">${MonitorRecruitInfo.character!''}</textarea>
            </label>
        </div>
    </div>
    <div style="margin-top: 10px">
        <label class="control-label" for="productName">竞选班长的优势：</label>
        <div class="controls">
            <label for="content">
                <textarea name="content" rows="10" cols="30" style="width: 1000px"
                          readonly="readonly">${MonitorRecruitInfo.advantage!''}</textarea>
            </label>
        </div>
    </div>
    <div style="margin-top: 10px">
        <label class="control-label" for="productName">育儿理念：</label>
        <div class="controls">
            <label for="content">
                <textarea name="content" rows="10" cols="30" style="width: 1000px"
                          readonly="readonly">${MonitorRecruitInfo.idea!''}</textarea>
            </label>
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <input type="button" id="pass" value="通过" class="btn btn-large btn-success">
            <input type="button" id="fail" value="未通过" class="btn btn-large btn-danger">
        </div>
    </div>
</div>
    <script type="text/javascript">


        $(function () {
            var status =${MonitorRecruitInfo.status!-1};
            if (status === -1) {
                alert("状态错误！");
                return;
            }
            switch (status) {
                case 1:
                    $("#fail").attr('disabled', false);
                    $("#pass").attr('disabled', false);
                    break;
                case 2:
                    $("#fail").attr('disabled', true);
                    break;
                default:
                    $("#fail").attr('disabled', true);
                    $("#pass").attr('disabled', true);
                    break;
            }

            $('#pass').on('click', function () {
                var parentId = $('#parentId').val();
                var status = 3;
                var postData = {
                    parentId: parentId,
                    status: status
                };
                // //数据校验
                if (!parentId) {
                    alert("bookId不能为空");
                    return false;
                }
                $.getUrlParam = function (name) {
                    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                    var r = window.location.search.substr(1).match(reg);
                    if (r != null) return unescape(r[2]);
                    return null;
                };
                $.post('changeMonitorstatus.vpage', postData, function (data) {
                    if (data.success) {
                        // var currentPage = $.getUrlParam('currentPage');
                        location.href = 'recruit_list.vpage';
                    } else {
                        alert(data.info);
                        return false;
                    }
                });
            });

            $('#fail').on('click', function () {
                var parentId = $('#parentId').val();
                var status = 2;
                var postData = {
                    parentId: parentId,
                    status: status
                };
                // //数据校验
                if (!parentId) {
                    alert("bookId不能为空");
                    return false;
                }
                $.getUrlParam = function (name) {
                    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
                    var r = window.location.search.substr(1).match(reg);
                    if (r != null) return unescape(r[2]);
                    return null;
                };
                $.post('changeMonitorstatus.vpage', postData, function (data) {
                    if (data.success) {
                        // var currentPage = $.getUrlParam('currentPage');
                        location.href = 'recruit_list.vpage';
                    } else {
                        alert(data.info);
                        return false;
                    }
                });
            });
        });
    </script>
</@layout_default.page>