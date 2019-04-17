<html>
    <head>
        <#include "../nuwa/meta.ftl" />
        <style>
            body { font-size: 14px; margin:0;padding: 0; background-color: white; }
            a { text-decoration: none; }
            div.row { margin: 20px 0 20px 0; }
            div.row span.field_desc { display: inline-block; width: 70px;}
            span.text_blue { color: #39f; }
            #feedbackSubType1-list label { display: inline-block; width: 190px; margin: 5px 0; }
        </style>
        <@sugar.capsule js=["jquery", "core", "jquery.flashswf"] />
    </head>
<body>
<div style="padding:10px;">
    <div id='page-feedback'>
        <div style="padding: 5px; border: 1px #C8F3C8 solid; background-color: #EFE; line-height: 18px;border-radius: 4px;box-shadow: 1px 1px 1px #67CC98;font-size: 14px;text-align: center;">
            我们会汇总大家反馈的问题和建议，逐步改进和完善我们的网站，为大家提供更好的服务。
        </div>

        <div class="row">
            <span class="field_desc">反馈类型：</span>
            <select name="feedbackType" style="width: 400px;padding:4px;border-radius: 3px;"></select>
        </div>
        <div id="feedbackSubType1-container" class="row" style="display: none;">
            <span class="field_desc">问题分类：</span>
            <div id="feedbackSubType1-list" style="margin: 5px 0; background: #eee;">
            </div>
        </div>
        <div class="row">
            <p id="record_p_b" style="color: #fe0421; display: none;">严重录音困难，请加QQ：1924988945 远程协助
                <span class='text_blue'><a href="http://help.17zuoye.com/?p=286" target="_blank">录音常见问题解决方法</a></span>
            </p>
            请描述您的问题和建议（紧急问题请拨打客服电话 <span class='text_blue'><@ftlmacro.hotline /></span>）
            <span id="other_b" style="display: none;"><input type="checkbox" name="otherSelect" id="otherSelect"/>数据收集</span>
            <textarea style="display: block; clear: both; width: 100%; height: 100px; margin: 5px 0;padding:5px;" name="content" maxlength="200"></textarea>
        </div>
        <div class="row" style="text-align: center;">
            <button id="submit" style="width: 160px; height: 30px;background-color: #60a00d;border: none;color: #fff;font-size: 14px; border-radius: 3px;">发送反馈</button>
        </div>
    </div>

    <div id='page-feedback-contact' style="display: none;">
        <div class="row" style="text-align: center;">
            您可以留下您的QQ或者联系电话。如果您的问题比较特殊，我们的工作人员会试着与您联系。
        </div>

        <div class="row" style="text-align: center;">
            <span class="field_desc">联系QQ：</span>
            <input type="text" name="contactQq" maxlength="20" style="width:340px;" />
        </div>

        <div class="row" style="text-align: center;">
            <span class="field_desc">联系电话：</span>
            <input type="text" name="contactPhone"  maxlength="20" style="width:340px;" />
        </div>
        <div class="row" style="text-align: center; margin-top: 40px;">
            <button id="submit_contact" style="width: 140px; height: 30px;">留下联系方式</button>
            &nbsp;&nbsp;&nbsp;&nbsp;
            <a id="skip_contact" href="javascript:;">不留联系方式了 &raquo;</a>
        </div>
    </div>


    <div id='page-feedback-complete' style="display: none;">
        <div class="row" style="">
            <br />
            您的反馈我们已经记下了，谢谢您对我们的支持。<br />
            <br />
            我们会汇总大家反馈的问题和建议，逐步改进和完善我们的网站，为大家提供更好的服务。<br />
            <br />
            紧急问题请拨打客服电话 <span class='text_blue'><@ftlmacro.hotline /></span>
            <br />
        </div>
        <div class="row" style="text-align: center; margin-top: 40px;">
            <button id="close" style="width: 160px; height: 30px;">关闭</button>
        </div>
    </div>
</div>
<script>
    (function(){
        var feedbackTypes = ${json_encode(feedbackTypes)};
        var postData = ${json_encode(postData)};
        var savedFeedbackId = null;
        var homeworkType = "";//设置问题类型

        //获得地址栏参数
        function getQuery(item){
            var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
            return svalue ? decodeURIComponent(svalue[1]) : '';
        }

        $(function(){
            var $feedbackType = $('select[name=feedbackType]');
            var $feedbackSubType1List = $('#feedbackSubType1-list');
            var $o = $('<option />').text('--请选择--').attr('value', '');
            $feedbackType.append($o);
            for(var k in feedbackTypes) {
                var text = k;
                if(feedbackTypes[k]._text) text = feedbackTypes[k]._text;
                $o = $('<option />').text(text).attr('value', k);
                $feedbackType.append($o);
            }

            //自动选择反馈类型
            switch (postData.homeworkType){
                case "english" :
                    if(postData.practiceName == "exam"){
                        homeworkType = "英语同步试题";
                    }else if(postData.practiceName == "阅读应用"){
                        homeworkType = "英语阅读应用";
                    }else{
                        homeworkType = "英语基础作业";
                    }
                    break;
                case "math" :
                    if(postData.practiceName == "数学应试练习"){
                        homeworkType = "数学同步试题";
                    }else{
                        homeworkType = "数学基础作业";
                    }
                    break;
                case "AfentiExercise" :
                    homeworkType = "课外练习";
                    break;
                case "pk" :
                    homeworkType = "PK问题";
                    break;
                case "iandyou100" :
                    homeworkType = "爱儿优问题";
                    break;
                case "SanguoDmz" :
                    homeworkType = "进击的三国问题";
                    break;
                case "PetsWar" :
                    homeworkType = "英语宠物大乱斗问题";
                    break;
                case "babel" :
                    homeworkType = "通天塔问题";
                    break;
                case "TravelAmerica" :
                    homeworkType = "走遍美国问题";
                    break;
                case "Stem101" :
                    homeworkType = "趣味数学问题";
                    break;
                case "Walker" :
                    homeworkType = "沃克单词冒险问题";
                    break;
                case "Koloalegend" :
                    homeworkType = "洛亚传说问题";
                    break;
                default :
                    homeworkType = postData.homeworkType;
            }

            //奖品中心问题反馈定位
            if(getQuery("rewardType") == "reward"){
                homeworkType = "奖品中心问题";
            }

            //初始化选择反馈类型
            setTimeout(function(){
                feedbackTypeChange(homeworkType);
            }, 200);

            //选择反馈类型
            $feedbackType.change(function() {
                var type = $(this).val();
                feedbackTypeChange(type);
            });

            //选择反馈类型方法
            function feedbackTypeChange(type){
                $feedbackSubType1List.empty();
                $feedbackType.children("[value="+ type +"]").attr("selected", "selected");

                var subTypes = feedbackTypes[type];
                var count = 0;
                for(var k in subTypes) {
                    if( ! subTypes.hasOwnProperty(k) || k == '_text')
                        continue;

                    var text = k;
                    if(subTypes[k]._text) text = subTypes[k]._text;

                    var tmpId = 'feedbackSubTypeId' + count;
                    var $radio = $('<input id="' + tmpId + '" type="radio" name="feedbackSubType1" />').val(k);
                    var $label = $('<label for="' + tmpId + '" />').append($radio).append(text);
                    $feedbackSubType1List.append($label);
                    count++;
                }

                if(count > 0) {
                    $('#feedbackSubType1-container').show();
                }
                else {
                    $('#feedbackSubType1-container').hide();
                }
            }

            //选择提示。
            var recordProblemsBox = $('#record_p_b');
            var otherProblemsBox = $('#other_b');
            var otherSelect = $("#otherSelect");
            $(document).on('change','#feedbackSubType1-list input:radio', function(){
                var $this = $(this);
                //选择‘录音问题’问题，增加提示。
                if($this.val() == '录音问题'){
                    recordProblemsBox.show();
                }else{
                    recordProblemsBox.hide();
                }

                //选择‘其他’问题，增加反馈字段。
                if($this.val() == '其他'){
                    otherProblemsBox.show();
                }else{
                    otherProblemsBox.hide();
                    otherSelect.prop('checked', false);
                }
            });

            //提交反馈
            var $submit = $("#submit");
            var submit_text = $submit.text();
            var isVoxExternalPluginExisting = VoxExternalPluginExists();

            $submit.click(function() {
                postData.feedbackType = $feedbackType.val();
                postData.feedbackSubType1 = $('input[name=feedbackSubType1]:checked').val();
                postData.content = $.trim($('textarea[name=content]').val());
                postData.dataCollectionSelect = otherSelect.prop('checked');

                var desProblem = postData.content;

                if(!postData.feedbackType) {
                    alert('需要选择 [反馈类型] 哦~~');
                    return false;
                }
                if(!postData.feedbackSubType1 && $('input[name=feedbackSubType1]').length > 0) {
                    alert('需要选择 [问题分类] 哦~~');
                    return false;
                }
                if(!postData.content) {
                    alert('请写一下您的问题或建议');
                    return false;
                }

                //给白屏用户加入特殊的信息
                if(postData.feedbackSubType1 == '作业白屏' || postData.feedbackSubType1 == '作业无法打开' || postData.feedbackType == '英语同步试题') {
                    postData.content += "(userAgent:"+ window.navigator.userAgent +",flashVersion:"+ $.flashswf.version.string+")";
                }
                //客户端标记
                if(isVoxExternalPluginExisting){
                    postData.content += "[client]";
                }

                $submit.prop('disabled', true);
                $submit.text('发送反馈中 ...');

                var $serverInfo = "账号：${(currentUser.id)!}|";
                $serverInfo += "反馈类型：" + postData.feedbackType + '-' + postData.feedbackSubType1 + postData.homeworkType + "|";
                $serverInfo += "描述问题：" + desProblem;

                window.open('/redirector/onlinecs_new.vpage?type=student&origin=PC-学生首页&question_type=question_advice_ps&serverInfo='+ encodeURIComponent($serverInfo), '','width=856,height=519');

                /*$.post('/ucenter/feedback.vpage', postData)
                        .done(function(data) {
                            savedFeedbackId = data.feedbackId;
                            $submit.text('反馈发送成功');
                            $('#page-feedback').hide();
                            $('#page-feedback-contact').show();
                        }).fail(function(){
                            alert('反馈发送失败，请重试');
                            $submit.prop('disabled', false);
                            $submit.text(submit_text);
                        });*/

                if(window.parent && window.parent.$ && window.parent.$.prompt){
                    window.parent.$.prompt.close();
                }

                return false;
            });

            var $submit_contact = $('#submit_contact');
            var $skip_contact = $('#skip_contact');
            var submit_contact_text = $submit_contact.text();

            $submit_contact.click(function() {
                var data = {
                    feedbackId: savedFeedbackId,
                    contactPhone: $.trim($('input[name=contactPhone]').val()),
                    contactQq: $.trim($('input[name=contactQq]').val())
                };

                if(!data.contactPhone && !data.contactQq) {
                    alert('请填写 QQ号 或者 电话');
                    return false;
                }

                $submit_contact.prop('disabled', true);
                $submit_contact.text('发送联系方式中 ...');
                $.post('/ucenter/feedback-contact.vpage', data)
                        .done(function() {
                            $submit_contact.text('反馈发送成功');
                            $skip_contact.click();
                        }).fail(function(){
                            alert('反馈发送失败，请重试');
                            $submit_contact.prop('disabled', false);
                            $submit_contact.text(submit_contact_text);
                        });

                return false;
            });

            $skip_contact.click(function(){
                $('#page-feedback-contact').hide();
                $('#page-feedback-complete').show();
                return false;
            });

            var $close = $('#close');
            $close.click(function(){
                if(window.parent && window.parent.$ && window.parent.$.prompt)
                    window.parent.$.prompt.close();
            });
        });
    })();

    $(function() {
        $("textarea[maxlength]").bind('input propertychange', function() {
            var $this = $(this);
            var maxLength = $this.attr('maxlength');
            if ($this.val().length > maxLength) {
                $this.val($this.val().substring(0, maxLength));
            }
        });
    });

</script>
</body></html>
