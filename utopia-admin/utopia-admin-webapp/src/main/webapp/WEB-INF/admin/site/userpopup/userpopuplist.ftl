<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
<div class="span9">
    <fieldset>
        <legend>
            弹窗广告&nbsp;&nbsp;
            <a href="batchpopuphomepage.vpage">批量弹窗</a> &nbsp;&nbsp;
            <a href="globaluserpopup.vpage">全局弹窗</a> &nbsp;&nbsp;
        </legend>

        <ul class="inline">
            <li>
                <label>输入用户ID：<textarea name="popupUserId" cols="35" rows="3" placeholder="请以','或空白符隔开"></textarea></label>
            </li>
            <li>
                <label>输入内容：<textarea name="popupContent" cols="35" rows="3" placeholder="请在这里输入要发送的内容"></textarea></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label>
                    发送时间
                    <input name="nextDateTime" type="text" placeholder="格式：2012-12-22 00:00"/>
                </label>
            </li>
            <li>
                <button class="btn btn-primary" id="submit_button">提交</button>
            </li>
        </ul>
    </fieldset>
    <br/>
    <fieldset>
        <legend>用户ID列表</legend>
        <div class="clear"></div>
        <div id="popup_tip"></div>
        <div class="clear"></div>
        <div id="popup_list"></div>
    </fieldset>
</div>
<script>

    $(function(){
        $.fn.datetimepicker.dates['zh-CN'] = {
            days        : ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
            daysShort   : ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
            daysMin     : ["日", "一", "二", "三", "四", "五", "六", "日"],
            months      : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            monthsShort : ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
            today       : "今日",
            suffix      : [],
            meridiem    : []
        };
        $('[name="nextDateTime"]').datetimepicker({
            language:  'zh-CN',
            weekStart: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });
    });

    $(function(){
        $('[name="popupUserId"]').on('keyup', function(){

            var content = $(this).val();
            var userIdList = content.split(/[,，\s]+/);

            var $popupList = $('#popup_list');
            $popupList.empty();
            $popupList.append('<br/><ul class="inline"></ul>');

            var $popupTip = $('#popup_tip');
            $popupTip.text('');

            var $popupListULNode = $popupList.find('ul');
            var wrongIds = '';

            for(var i = 0, length = userIdList.length; i < length; i++) {

                if(userIdList[i] == '') {
                    continue;
                }

                if(!userIdList[i].match(/^\d+$/)) {
                    if(wrongIds != '') {
                        wrongIds += ','
                    } else {
                        wrongIds += '<span class="warn">提示：</span>';
                    }

                    wrongIds += '<span class="warn">[' + i + ']</span><span>' + userIdList[i] + '</span>';
                    $popupListULNode.append('<li><span class="index warn">[' + i + '] </span><span class="item">' + userIdList[i] + '</span></li><br/>');
                } else {
                    $popupListULNode.append('<li><span class="index">[' + i + '] </span><span class="item">' + userIdList[i] + '</span></li><br/>');
                }

            }

            if (wrongIds != '') {
                $popupTip.append( wrongIds + '<span class="warn"> 不是规范的用户ID</span>');
            }

        });

        $('#submit_button').on('click', function() {
            var postData = {
                popupUserId     :   $('[name="popupUserId"]').val(),
                popupContent    :   $('[name="popupContent"]').val(),
                nextDateTime    :   $('[name="nextDateTime"]').val()
            };
            $.post('?', postData, function(data) {
                alert(data.info);
                if(data.success) {
                    location.href = "?";
                }
            });
        });
    });
</script>
</@layout_default.page>