<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="Push管理平台" page_num=21 jqueryVersion="1.7.1">
<#if error?? && error?has_content>
<h1>${error}</h1>
<#else>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    .input-area {width: 350px; resize: none;}
    .sender-selector{width: 40%;height: 600px;float:left;margin-right: 3px;}
    .push-target{width:80%; height: 100%;}
    .target-school{resize: none;overflow-y: auto;  overflow-x: hidden;}
    .target-user{resize: none;overflow-y: auto; overflow-x: hidden; margin-top: 20px; height: 60%;}
    .ext-key{width: 120px; text-align: center!important; font-weight: bold;}
    .ext-value{display:inline; margin-left: 20px; margin-bottom: 0;}
    .ext-value > input[type=checkbox]{margin: 0}
    .title{padding:5px 0;width: 83%;margin-bottom:5px;font-weight: bold;border:1px solid #0f92a8;background:#27a9bf;border-radius:2px;text-align: center;color:#fff;}
</style>
<div id="main_container" class="span9">
    <legend class="legend_title">
        <strong>发送App Push消息</strong>
        <a href="javascript:void(0);" id="fastPush" class="btn btn-danger" style="float:right;">
            快速推送
        </a>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <div class="form-horizontal">
                    <div class="control-group">
                        <label class="col-sm-2 control-label">选择要发送的App</label>
                        <div class="controls">
                            <select id="sendApp" name="sendApp" style="margin-bottom:0;width: 360px;">
                                <option value="">请选择</option>
                                <option value="PRIMARY_TEACHER" data-klazz="junior" data-type="teacher" data-kwelve="j" >小学老师APP</option>
                                <option value="PARENT" data-klazz="junior" data-type="parent" data-kwelve="j">小学家长APP</option>
                                <option value="STUDENT" data-klazz="junior" data-type="student" data-kwelve="j">小学学生APP</option>
                                <option value="JUNIOR_PARENT" data-klazz="middle,senior" data-type="parent" data-kwelve="m">中学家长APP</option>
                                <option value="JUNIOR_TEACHER" data-klazz="middle,senior" data-type="teacher" data-kwelve="m">中学老师APP</option>
                                <option value="JUNIOR_STUDENT" data-klazz="middle,senior" data-type="student" data-kwelve="m">中学学生APP</option>
                            </select>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">发送方式</label>
                        <div class="controls">
                            <input id="onlyPush" type="checkbox" name="onlyPush" onchange="jpushCheck()"/>&nbsp;&nbsp;<span style="color: red;">发送push</span>&nbsp;&nbsp;
                            <input id="onlyMsg" type="checkbox" name="onlyMsg" style="margin-left: 80px;" onchange="msgCheck()"/>&nbsp;&nbsp;<span style="color: red;">发送系统消息</span>&nbsp;&nbsp;
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">发送时间</label>
                        <div class="controls">
                            <input id="sendTime" name="sendTime" style="width:350px;" data-role="date" data-inline="true" type="text" placeholder="yyyy-MM-dd HH:mm:ss"/>
                        </div>
                    </div>
                    <div class="control-group msg-config" style="display: none;">
                        <label class="col-sm-2 control-label">置顶设置</label>
                        <div class="controls">
                            <input id="isTop" name="isTop" type="checkbox"/>&nbsp;&nbsp;是否置顶&nbsp;&nbsp;&nbsp;
                            置顶截止时间：<input id="topEndTime" name="topEndTime" style="width: 10em;" data-role="date" data-inline="true" type="text" placeholder="2014-11-26 12:30"/>
                        </div>
                    </div>
                    <div class="control-group jpush-config" style="display: none;">
                        <label class="col-sm-2 control-label">Jpush内容</label>
                        <div class="controls">
                            <textarea class="input-area" name="notifyContent" cols="35" rows="3" placeholder="请在这里输入要发送的JPush内容"></textarea>
                        </div>
                    </div>
                    <div id="title-div" style="display: none">
                        <div class="control-group msg-config" style="display: none;">
                            <label class="col-sm-2 control-label">消息标题</label>
                            <div class="controls">
                                <textarea class="input-area" name="title" cols="35" rows="3" placeholder="请在这里输入消息抬头"></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="control-group msg-config" style="display: none;">
                        <label class="col-sm-2 control-label">消息概要</label>
                        <div class="controls">
                            <textarea class="input-area" id="content" name="content" cols="35" rows="3" placeholder="请在这里输入消息概要"></textarea>
                        </div>
                    </div>
                    <div class="control-group msg-config">
                        <label class="col-sm-2 control-label">图片地址</label>
                        <div class="controls">
                            <input type="file" name="file" id="file" value="" accept="image/gif,image/jpeg,image/png,image/jpg,image/bmp">
                            <a href="javascript:void (0);" id="fileUpBtn" data-value=""></a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">内容地址</label>
                        <div class="controls">
                            <textarea class="input-area" id="link" name="link" cols="35" rows="3" placeholder="请在这里输入消息的链接跳转地址"></textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="col-sm-2 control-label">推送时长</label>
                        <div class="controls">
                            <input type="text" id="durationTime" name="durationTime" placeholder="定速推送时长，单位分钟" style="width: 350px;" value="30"/>&nbsp;<span style="color: red;">全量推送至少3小时</span>
                        </div>
                    </div>
                    <div id="sharetr" style="display: none">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">是否可分享</label>
                            <div class="controls">
                                <input id="share" type="checkbox" name="share"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享文案</label>
                            <div class="controls">
                                <textarea class="input-area" id="shareContent" name="shareContent" cols="35" rows="3" placeholder="分享文案"></textarea>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">分享地址</label>
                            <div class="controls">
                                <textarea class="input-area" id="shareUrl" name="shareUrl" cols="35" rows="3" placeholder="请在这里输入分享的地址"></textarea>
                            </div>
                        </div>
                    </div>
                    <div id="parent-opts" style="display: none">
                        <div class="control-group msg-config" style="display: none">
                            <label class="col-sm-2 control-label">消息类型</label>
                            <div class="controls">
                                <select id="messageTag" name="messageTag" style="width: 360px;">
                                    <option value="通知">通知</option>
                                    <option value="公告">公告</option>
                                    <option value="活动">活动</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div id="stuexttype" style="display: none">
                        <div class="control-group msg-config" style="display: none">
                            <label class="col-sm-2 control-label">消息扩展类型</label>
                            <div class="controls">
                                <select id="msgexttype" name="msgexttype" style="width: 360px;">
                                    <option value="2">小铃铛活动提醒</option>
                                    <option value="80">广告中心提醒</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div>
        <div id="extArea">
            <legend class="legend_title" style="margin-bottom:5px; margin-top: 5px;">
                <strong>扩展选项 </strong><span style="font-size: smaller;">（不选即不做限制）</span>
            </legend>
            <table class="table table-striped table-bordered">
                <#--<tr>
                    <td class="ext-key">学 段</td>
                    <td>
                        <label class="ext-value"><input class="ktwelve-value" type="radio" name="ktwelve-choose" value="i"> 学前</label>
                        <label class="ext-value"><input class="ktwelve-value" type="radio" name="ktwelve-choose" value="j" checked> 小学</label>
                        <label class="ext-value"><input class="ktwelve-value" type="radio" name="ktwelve-choose" value="m"> 初中</label>
                        <label class="ext-value"><input class="ktwelve-value" type="radio" name="ktwelve-choose" value="s"> 高中</label>
                    </td>
                </tr>-->
                <tr name="klazz-row">
                    <td class="ext-key">年 级</td>
                    <td>
                        <div id="infant-clazz" style="display: none;">
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="54"> 学前班</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="53"> 大班</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="52"> 中班</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="51"> 小班</label>
                        </div>
                        <div id="junior-clazz">
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="1"> 一年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="2"> 二年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="3"> 三年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="4"> 四年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="5"> 五年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="6"> 六年级</label>
                        </div>
                        <div id="middle-clazz" style="display: none;">
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="6"> 六年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="7"> 七年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="8"> 八年级</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="9"> 九年级</label>
                        </div>
                        <div id="senior-clazz" style="display: none;">
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="11"> 高一</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="12"> 高二</label>
                            <label class="ext-value"><input class="clazz-value" type="checkbox" value="13"> 高三</label>
                        </div>
                    </td>
                </tr>
                <tr class="teacher-option" name="subject-row">
                    <td class="ext-key">学 科</td>
                    <td>
                        <label class="ext-value junior middle"><input class="subject-value" type="checkbox" value="ENGLISH"> 英语</label>
                        <label class="ext-value junior middle"><input class="subject-value" type="checkbox" value="MATH"> 数学</label>
                        <label class="ext-value junior middle"><input class="subject-value" type="checkbox" value="CHINESE"> 语文</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="PHYSICS"> 物理</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="CHEMISTRY"> 化学</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="BIOLOGY"> 生物</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="POLITICS"> 政治</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="GEOGRAPHY"> 地理</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="HISTORY"> 历史</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="INFORMATION"> 信息</label>
                        <label class="ext-value middle"><input class="subject-value" type="checkbox" value="HISTORY_SOCIETY"> 历史与社会</label>
                        <label class="ext-value middle"><input class="subject-value middle" type="checkbox" value="SCIENCE"> 科学</label>
                    </td>
                </tr>
                <tr class="teacher-option">
                    <td class="ext-key">认证状态</td>
                    <td>
                        <label class="ext-value"><input class="auth-value" type="checkbox" value="0"> 未认证</label>
                        <label class="ext-value"><input class="auth-value" type="checkbox" value="1"> 认证成功</label>
                        <label class="ext-value"><input class="auth-value" type="checkbox" value="3"> 未通过</label>
                    </td>
                </tr>
                <tr class="other-option" style="display: none;">
                    <td class="ext-key">黑名单配置</td>
                    <td>
                        <label class="ext-value"><input id="paymentBlackList" type="radio" name="payment-list"/> 只发送付费黑名单用户</label>
                        <label class="ext-value"><input id="noneBlackList" type="radio" name="payment-list"/> 不包含黑名单</label>
                    </td>
                </tr>
            </table>
        </div>
        <div class="form-horizontal" style="height: 700px;">
            <legend class="legend_title">
                <strong>选择发送方式</strong>&nbsp;&nbsp;
                <select id="ptSelector" style="background: none;font-weight: bold;">
                    <option value="1" selected>&nbsp;&nbsp;自定义投放策略</option>
                    <option value="2">&nbsp;&nbsp;投放指定用户</option>
                    <option value="3">&nbsp;&nbsp;投放指定地区</option>
                    <option value="4">&nbsp;&nbsp;投放指定学校</option>
                    <option value="5">&nbsp;&nbsp;投放指定标签</option>
                </select>
                <button type="button" class="btn btn-warning save_btn" style="margin-left: 50px;">
                    <i class="icon-ok icon-white"></i> 提 交 审 核
                </button>
            </legend>
            <div id="pushUser" style="width: 100%;display: none;">
                <div class="sender-selector">
                    <div class="title">投放指定用户</div>
                    <div id="idTypeCheck" style="display: none;margin-top: 10px;">
                        <input type="radio" name="idType" value='2' checked="checked"/>&nbsp;&nbsp;导入家长ID&nbsp;
                        <input type="radio" name="idType" value='1'/>&nbsp;&nbsp;导入学生ID&nbsp;&nbsp;
                    </div>
                    <textarea id="targetUser" name="targetUser" class="form-control push-target target-user" rows="20"
                              placeholder="一行输入一条数据，如果超过100行建议使用其他策略投放"></textarea>
                    <div class="well" style="width: 75%; margin-top: 5px;">
                        请上传文件： <input type="file" id="uploadExcel" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel">
                        <span style="color:red"><br/>1.表格第一列为用户ID<br/>2.单个文件行数请控制在2W以内
                        <input type="hidden" name="fileUrl" value="" id="fileUrl">
                    </div>
                </div>
            </div>
            <div id="pushRegion" style="width: 100%;display: none;">
                <div class="sender-selector">
                    <div class="title" style="width: 80%;">投放指定地区</div>
                    <div id="regionTree" class="sampletree push-target"></div>
                </div>
            </div>
            <div id="pushSchool" style="width: 100%;display: none;">
                <div class="sender-selector">
                    <div class="title">投放指定学校</div>
                    <textarea id="targetSchool" name="targetSchool" class="form-control push-target target-school" rows="20"
                              placeholder="一行输入一条数据，建议从EXCEL编辑导入，不要超过20条"></textarea>
                </div>
            </div>
            <div id="pushTag" style="width: 100%;display: none;">
                <div class="sender-selector">
                    <div class="title" style="width: 80%;">投放指定标签</div>
                    <div id="tagTree" class="sampletree push-target"></div>
                </div>
                <div style="height: 600px; overflow: scroll;">
                    <div class="well">
                        <strong>已选标签：</strong><span id="selectedTag"></span><br/>
                        <button name="save_target_btn" type="button" class="btn btn-success" data-type="4">保存标签组</button>
                        <button name="save_target_btn" id="clear_target_btn_4" type="button" class="btn btn-danger" data-type="5">清空标签组</button>
                    </div>
                    <table class="table table-striped table-bordered table-condensed" style="font-size: 14px; margin-top: 10px;">
                        <thead>
                        <tr class="first"><th>标签组</th><th>操作</th><th>标签组</th><th>操作</th></tr>
                        </thead>
                        <tbody class="tagGroupList"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<div id="tagView" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h4>JpushTag</h4>
    </div>
    <div class="modal-body">
        <div>
            是否匹配：<input id="match" type="text"/> <br/>
            PushTag：<textarea style="resize: none;width: 80%;" rows="5" id="jpushTag"></textarea> <br/>
            UserTag：<textarea style="resize: none;width: 80%;" rows="5" id="userTag"></textarea> <br/>
        </div>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 150%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在处理，请稍候……</p>
</div>
<script>
    $(function () {
        var isTop = $("#isTop"), topEndTime = $("#topEndTime");
        isTop.on("change", function () {
            var $this = $(this), parent = $this.parent();
            if (this.checked && !topEndTime.val()) {
                topEndTime.focus();
            }
            if (parent.hasClass("red-color")) {
                parent.removeClass("red-color");
            }
        });

        topEndTime.blur(function () {
            if (!topEndTime.val() && isTop.parent().hasClass("red-color")) {
                isTop.parent().removeClass();
            }
        });

        function checkTop() {
            if (isTop[0].checked && !topEndTime.val()) {
                $(document).scrollTop(0);
                alert("你选择了置顶，请选择置顶截止时间！");
                topEndTime.focus();
                return false;
            } else if (!isTop[0].checked && topEndTime.val()) {
                $(document).scrollTop(0);
                alert("您选择了置顶截止时间，如果需要置顶的话必须勾选置顶复选框，或者清空时间取消置顶！");
                isTop.parent().addClass("red-color");
                return false;
            } else if (topEndTime.val()) {
                if (topEndTime.val()) {
                    var date = new Date(topEndTime.val()), now = new Date();
                    if (date <= now) {
                        $(document).scrollTop(0);
                        alert("您选择的置顶截止时间是过去，请选择当前时间之后的某个时间！");
                        topEndTime.focus();
                        return false;
                    }
                }
            }
            return true;
        }

        var sendApp = $("#sendApp"),
                shareOptions = $("#sharetr"),
                msgTypeOptions = $("#stuexttype"),
                idTypeCheck = $("#idTypeCheck"),
                parentOptions = $("#parent-opts"),
                titleDiv = $("#title-div"),
                teacherOptions = $("#teacher-opts");

        $(".save_btn").on("click", function () {
            if(sendApp.val() == ""){
                alert("请选择要发送的app");
                return false;
            }

            var link=$("#link");
            if(!/^\s*https/.test(link.val())){
                if(!confirm("当前地址不是以https开头的安全链接，确认发送吗？")){
                    return;
                }
            }

            if (!checkLink(link.val())) {
                alert("如果要使用链接作为参数，请将参数链接进行URLEncode");
                return;
            }
            var clazzLevels = [], subjects = [], authList =[];
            var clazzNodes = $('.clazz-value'), subjectNodes = $('.subject-value'), authNodes = $('.auth-value');

            if(clazzNodes.length !==0){
                $.each(clazzNodes,function(i,item){
                    if ($(item).is(":checked")) clazzLevels.push($(item).val());
                })
            }
            if(subjectNodes.length !==0){
                $.each(subjectNodes,function(i,item){
                    if ($(item).is(":checked"))  subjects.push($(item).val());
                })
            }
            if(authNodes.length !==0){
                $.each(authNodes,function(i,item){
                    if ($(item).is(":checked"))  authList.push($(item).val());
                })
            }

            var data = {
                sendApp: sendApp.val(),
                userType: $(sendApp).find("option:selected").data("type"),
                sendTime: $('#sendTime').val(),
                onlyPush: $("#onlyPush").is(':checked'),
                onlyMsg: $("#onlyMsg").is(':checked'),
                isTop: $("#isTop").is(':checked'),
                topEndTimeStr: $("#topEndTime").val(),
                notifyContent: $('[name="notifyContent"]').val(),
                title: $('[name="title"]').val(),
                content: $("#content").val(),
                fileName: $("#fileUpBtn").data('value'),
                link: $('[name="link"]').val(),
                durationTime: $("#durationTime").val(),
                share: $("#share").is(':checked'),
                shareContent: $("#shareContent").val(),
                shareUrl: $("#shareUrl").val(),
                msgExtType: $("#msgexttype").val(),
                messageTag: $("#messageTag").val(),
                //ktwelve: $('input[name=ktwelve-choose]:checked').val(),
                ktwelve: $(sendApp).find("option:selected").data("kwelve"),
                subject: subjects.join(","),
                clazzLevel: clazzLevels.join(","),
                authStat: authList.join(","),
                paymentBlackList : $("#paymentBlackList").is(":checked"),
                noneBlackList: $("#noneBlackList").is(":checked")
            };

            data.pushType = $("#ptSelector").find("option:selected").val();
            data.tagGroups = JSON.stringify(tagGroupList);

            var regionList = [];
            var regionTree = $("#regionTree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            $.map(regionNodes, function (node) {
                regionList.push(node.key);
            });
            data.regionList = regionList.join(",");

            data.idType = $("input[name='idType']:checked").val();
            data.userList = $('#targetUser').val().trim();
            data.fileUrl = $('#fileUrl').val();
            data.schoolList = $('#targetSchool').val().trim();

            console.info(data);
            if (checkTop() && check()) {
                $.post('createapppush.vpage', data, function (res) {
                    if(res.success){
                        alert("提交成功，请等待审核！");
                        location.href = '/audit/apply/list.vpage';
                    }else{
                        alert("发布失败！\n"+res.info);
                    }
                });
            }
        });

        var tagGroupList = [];//定义标签数组
        var tagGroupNameList = [];//定义标签数组
        $("button[name=save_target_btn]").on('click', function () {
            var type = $(this).data("type");
            if (type == 4) {
                $("#selectedTag").text('');//清空已选择标签
                var tagList = [];
                var tagNameList = [];
                var tagTree = $("#tagTree").fancytree("getTree");
                var tagNodes = tagTree.getSelectedNodes();
                if(!tagNodes || tagNodes.length == 0 ){
                    return ;
                }
                $.map(tagNodes, function (node) {
                    tagList.push({tagName:node.title,tagId:node.key});
                    tagNameList.push(node.title);
                });
                tagGroupList.push(tagList);
                tagGroupNameList.push(tagNameList);
                addTagGroupList(tagGroupNameList);
                initTagTree();
            }else if(type == 5){//清空标签组
                tagGroupList = [];
                tagGroupNameList = [];
                addTagGroupList(tagGroupNameList);
            }
        });


        $(document).on('click','.deleteLabel',function () {
            var _this = $(this);
            var index = _this.data('index');
            _this.remove();
            tagGroupList.splice(index,1);
            tagGroupNameList.splice(index,1);
            addTagGroupList(tagGroupNameList);
        });


        sendApp.on("change", function () {
            alert("切换之后将清除所有选择的扩展选项");
            clearExt();
            var option = $(this).find("option:selected");

            // 显示对应学段的学科
            $("tr[name='subject-row'] td label").hide();
            // 显示对应学段的年级
            $("tr[name='klazz-row'] td div").hide();
            option.data("klazz").split(",").forEach(function(k,index){
                $("#" + k + "-clazz").show();
                $("tr[name=subject-row] label." + k).show();
            });

            var userType = option.data("type");
            console.log(userType);
            if (userType === "teacher") {
                $(".teacher-option").show();
                $(".other-option").hide();
                titleDiv.show();
                teacherOptions.show().next().hide();
                shareOptions.hide();
                idTypeCheck.hide();
                msgTypeOptions.hide();
                parentOptions.hide();
            } else if (userType === "student") {
                $(".teacher-option").hide();
                $(".other-option").show();
                titleDiv.show();
                teacherOptions.hide().next().show();
                parentOptions.hide();
                shareOptions.hide();
                idTypeCheck.hide();
                msgTypeOptions.show();
            } else if (userType === "parent") {
                $(".teacher-option").hide();
                $(".other-option").show();
                titleDiv.hide();
                parentOptions.show();
                teacherOptions.hide().next().show();
                shareOptions.show();
                idTypeCheck.show();
                msgTypeOptions.hide();
            }

        });

        var pushUser = $('#pushUser');
        var pushRegion = $('#pushRegion');
        var pushSchool = $('#pushSchool');
        var pushTag = $('#pushTag');
        var extArea = $('#extArea');
        initRegionTree();
        initTagTree();
        initPushTarget(1);

        $('#ptSelector').on('change', function () {
            var pushType = $(this).val();
            initPushTarget(pushType);
        });

        function initPushTarget(pushType) {
            clearExt();
            var type = parseInt(pushType);
            switch (type) {
                case 2:
                    pushUser.show();
                    pushRegion.hide();
                    pushSchool.hide();
                    pushTag.hide();
                    extArea.hide();
                    break;
                case 3:
                    pushUser.hide();
                    pushRegion.show();
                    pushSchool.hide();
                    pushTag.hide();
                    extArea.show();
                    break;
                case 4:
                    pushUser.hide();
                    pushRegion.hide();
                    pushSchool.show();
                    pushTag.hide();
                    extArea.show();
                    break;
                case 5:
                    pushUser.hide();
                    pushRegion.hide();
                    pushSchool.hide();
                    pushTag.show();
                    extArea.hide();
                    break;
                default:
                    pushUser.hide();
                    pushRegion.hide();
                    pushSchool.hide();
                    pushTag.hide();
                    extArea.show();
                    break;
            }
        }

        function initRegionTree() {
            $("#regionTree").fancytree({
                source: ${targetRegion!},
                checkbox: true,
                selectMode: 2
            });
        }
        function initTagTree (){
            $('#tagTree').fancytree({
                extensions: ["filter"],
                source:${tagTree!},
                checkbox: true,
                selectMode: 2,
                select: function(event, data) {
                    var nodes = data.tree.getSelectedNodes();
                    var tagNames = [];
                    $.map(nodes, function (node) {
                        tagNames.push(node.title);
                    });
                    $("#selectedTag").text(tagNames.join("  |  "))
                }
            });
        }

        //向表格内展示标签组
        function addTagGroupList(list){
            var html = '';
            list.forEach(function(item,index) {
                if(index%2 == 0){
                    html += '<tr>'
                }
                html += '<td style="width: 40%">'+ item +'</td>' +
                        '<td style="width: 10%;"><button class="btn btn-danger deleteLabel" data-index="' + index + '">删除</button></td>';
                if(index%2 == 1){
                    html += '</tr>'
                }
            });
            $('.tagGroupList tr:not(".first")').remove();
            $('.tagGroupList').append(html);
        }
        //清空所有数据
        function clearExt() {
            // 地区
            var regionTree = $("#regionTree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            $.each(regionNodes, function(i, node) {
                node.setSelected(false);
            });

            // 标签tree清空数据
            var tagTree = $("#tagTree").fancytree("getTree");
            var tagNodes = tagTree.getSelectedNodes();
            $.each(tagNodes, function(i, node) {
                node.setSelected(false);
            });

            //clear tag data
            $("#selectedTag").text("");
            tagGroupNameList = [];
            addTagGroupList(tagGroupNameList);

            // 用户
            $('#targetUser').val('');

            // 学校
            $('#targetSchool').val('');

            // 学段
            $('.ktwelve-value').eq(0).attr('checked', true);

            // 年级
            $('.clazz-value').each(function() {
                $(this).attr('checked', false);
            });

            // 学科
            $('.subject-value').each(function() {
                $(this).attr('checked', false);
            });

            // 认证状态
            $('.auth-value').each(function() {
                $(this).attr('checked', false);
            });

            // 付费黑名单
            $("#paymentBlackList").attr('checked', false);

            // 不包含黑名单
            $("#noneBlackList").attr('checked', false);
        }

        $('input[name=ktwelve-choose]').on('change', function () {
            // 年级清空
            $('.clazz-value').each(function() {
                $(this).attr('checked', false);
            });

            var infantClazz = $('#infant-clazz');
            var juniorClazz = $('#junior-clazz');
            var middleClazz = $('#middle-clazz');
            var seniorClazz = $('#senior-clazz');

            if ($(this).val() === 'm') {
                infantClazz.hide();
                juniorClazz.hide();
                middleClazz.show();
                seniorClazz.hide();
            } else if ($(this).val() === 'i') {
                infantClazz.show();
                juniorClazz.hide();
                middleClazz.hide();
                seniorClazz.hide();
            } else if ($(this).val() === 's') {
                infantClazz.hide();
                juniorClazz.hide();
                middleClazz.hide();
                seniorClazz.show();
            } else {
                infantClazz.hide();
                juniorClazz.show();
                seniorClazz.hide();
                middleClazz.hide();
            }
        });

      $('#uploadExcel').change(function () {
            $("#loadingDiv").show();
            var $this = $(this);
            var sourceFile = $this.val();
            if (blankString(sourceFile)) {
                $("#loadingDiv").hide();
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt != "xls" && fileExt != "xlsx") {
                alert("请上传正确格式的excel！");
                $("#loadingDiv").hide();
                return;
            }
            if ($this.val() !== '') {
                var formData = new FormData();
                formData.append('file', $this[0].files[0]);
                $.ajax({
                    url: 'uploadexcel.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (res) {
                        $("#loadingDiv").hide();
                        if (res.success) {
//                            alert(res.fileUrl);
                            $('#fileUrl').val(res.fileUrl);
                            alert('上传成功');
                        } else {
                            alert(res.info);
                        }
                    }
                });
            }
        });

      $('#fastPush').on('click', function () {
          $("#loadingDiv").show();
            var clazzLevels = [], subjects = [], authList =[];
            var clazzNodes = $('.clazz-value'), subjectNodes = $('.subject-value'), authNodes = $('.auth-value');

            if(clazzNodes.length !==0){
                $.each(clazzNodes,function(i,item){
                    if ($(item).is(":checked")) clazzLevels.push($(item).val());
                })
            }
            if(subjectNodes.length !==0){
                $.each(subjectNodes,function(i,item){
                    if ($(item).is(":checked"))  subjects.push($(item).val());
                })
            }
            if(authNodes.length !==0){
                $.each(authNodes,function(i,item){
                    if ($(item).is(":checked"))  authList.push($(item).val());
                })
            }

            var data = {
                sendApp: $('#sendApp').val(),
                userType: $(sendApp).find("option:selected").data("type"),
                sendTime: $('#sendTime').val(),
                onlyPush: $("#onlyPush").is(':checked'),
                onlyMsg: $("#onlyMsg").is(':checked'),
                isTop: $("#isTop").is(':checked'),
                topEndTimeStr: $("#topEndTime").val(),
                notifyContent: $('[name="notifyContent"]').val(),
                title: $('[name="title"]').val(),
                content: $("#content").val(),
                fileName: $("#fileUpBtn").data('value'),
                link: $('[name="link"]').val(),
                durationTime: $("#durationTime").val(),
                share: $("#share").is(':checked'),
                shareContent: $("#shareContent").val(),
                shareUrl: $("#shareUrl").val(),
                msgExtType: $("#msgexttype").val(),
                messageTag: $("#messageTag").val(),
                ktwelve: $(sendApp).find("option:selected").data("kwelve"),
                subject: subjects.join(","),
                clazzLevel: clazzLevels.join(","),
                authStat: authList.join(","),
                paymentBlackList : $("#paymentBlackList").is(":checked"),
                noneBlackList: $("#noneBlackList").is(":checked")
            };

            data.pushType = $("#ptSelector").find("option:selected").val();
            data.idType = $("input[name='idType']:checked").val();
            data.userList = $('#targetUser').val().trim();
            data.tagGroups = JSON.stringify(tagGroupList);
             if (!checkLink(data.link)) {
               alert("如果要使用链接作为参数，请将参数链接进行URLEncode");
               return;
            }
            if (checkTop() && check()) {
                $.post('fastpush.vpage', data, function (res) {
                    $("#loadingDiv").hide();
                    if (res.success) {
                        alert("发送成功");
                    } else {
                        alert(res.info);
                    }
                });
            }
        });

    });

    $('#sendTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss',
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        minDate:new Date(),
        onSelect: function (selectedDate) {
        }
    });

    $('#topEndTime').datetimepicker({
        monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
        dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
        defaultDate: new Date(),
        numberOfMonths: 1,
        changeMonth: false,
        changeYear: false,
        onSelect: function (selectedDate) {
        }
    });

    $("#file").change(function () {
        var $this = $(this);
        if ($this.val() !== '') {
            var formData = new FormData();
            formData.append('file', $this[0].files[0]);
            $.ajax({
                url: '/opmanager/pushmessage/uploadfile.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        $("#fileUpBtn").data('value', data.fileName).text("已选择图片：" + data.fileName);
                        alert("上传成功");
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    });

    function jpushCheck() {
        var flag = $("#onlyPush").is(':checked');
        var jpushConfig = $('.jpush-config');
        $.each(jpushConfig,function(i,item){
            if (flag) $(item).show();
            else $(item).hide();
        });
    }

    function msgCheck() {
        var flag = $("#onlyMsg").is(':checked');
        var msgConfig = $('.msg-config');
        $.each(msgConfig,function(i,item){
            if (flag) $(item).show();
            else $(item).hide();
        });
    }

    function check() {
        var onlyPush = $("#onlyPush").is(':checked');
        if (onlyPush) {
            var notifyContent = $('[name="notifyContent"]').val();
            if (notifyContent === '') {
                alert('Jpush内容不能为空');
                return false;
            }
        }

        var onlyMsg = $("#onlyMsg").is(':checked');
        if (onlyMsg) {
            var summary = $('[name="content"]').val();
            if (summary === '') {
                alert('消息概要不能为空');
                return false;
            }
        }
        return true;
    }

    function checkJpushTag(userId) {
        var clazzLevels = [], subjects = [], authList =[];
        var clazzNodes = $('.clazz-value'), subjectNodes = $('.subject-value'), authNodes = $('.auth-value');

        if(clazzNodes.length !==0){
            $.each(clazzNodes,function(i,item){
                if ($(item).is(":checked")) clazzLevels.push($(item).val());
            })
        }
        if(subjectNodes.length !==0){
            $.each(subjectNodes,function(i,item){
                if ($(item).is(":checked"))  subjects.push($(item).val());
            })
        }
        if(authNodes.length !==0){
            $.each(authNodes,function(i,item){
                if ($(item).is(":checked"))  authList.push($(item).val());
            })
        }

        var data = {
            sendApp: $('#sendApp').val(),
            sendTime: $('#sendTime').val(),
            onlyPush: $("#onlyPush").is(':checked'),
            onlyMsg: $("#onlyMsg").is(':checked'),
            isTop: $("#isTop").is(':checked'),
            topEndTimeStr: $("#topEndTime").val(),
            notifyContent: $('[name="notifyContent"]').val(),
            title: $('[name="title"]').val(),
            content: $("#content").val(),
            fileName: $("#fileUpBtn").data('value'),
            link: $('[name="link"]').val(),
            durationTime: $("#durationTime").val(),
            share: $("#share").is(':checked'),
            shareContent: $("#shareContent").val(),
            shareUrl: $("#shareUrl").val(),
            msgExtType: $("#msgexttype").val(),
            messageTag: $("#messageTag").val(),

            ktwelve: $('input[name=ktwelve-choose]:checked').val(),
            subject: subjects.join(","),
            clazzLevel: clazzLevels.join(","),
            authStat: authList.join(","),
            paymentBlackList : $("#paymentBlackList").is(":checked"),
            noneBlackList: $("#noneBlackList").is(":checked")
        };

        data.pushType = $("#ptSelector").find("option:selected").val();

        var regionList = [];
        var regionTree = $("#regionTree").fancytree("getTree");
        var regionNodes = regionTree.getSelectedNodes();
        $.map(regionNodes, function (node) {
            regionList.push(node.key);
        });
        data.regionList = regionList.join(",");

        data.idType = $("input[name='idType']:checked").val();
        data.userList = $('#targetUser').val().trim();
        data.schoolList = $('#targetSchool').val().trim();
        data.tagGroups = JSON.stringify(tagGroupList);
        data.userId = userId;

        console.info(data);
        $.get('checktag.vpage', data, function(res) {
            if (res.success) {
//                alert(res.jpushTag);
                $('#match').val(res.match);
                $('#jpushTag').val(res.jpushTag);
                $('#userTag').val(res.userTag);
                $("#tagView").modal('show');
            } else {
                alert(res.info);
            }
        });
    }

    function checkLink(linkUrl) {
//        if (linkUrl == '') {
//            return true;
//        }
//        var cnt = 0;
//        for (var i = 0; i < linkUrl.length; ++i) {
//            if (linkUrl.charAt(i) == '?') {
//                cnt++;
//            }
//        }
//        return cnt <= 1;
        return true;
    }

</script>
</#if>
</@layout_default.page>