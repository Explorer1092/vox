<!-- 链接邀请-->
<div class="form_wrap" >
    <div class="key_information" style="width:530px; margin:0 auto;">
        复制以下邀请链接地址，用<i class="chatIcon"></i>邮件等发送给您希望邀请的老师，对方通过链接注册成功后，您将获得园丁豆作为奖励！<br><br>
        <textarea id="invite_teacher_info_url" rows="5" readonly style=" height: 150px; background: #ffffef;color:#666;border: 1px solid #ddd; resize: none; width:510px; overflow:hidden; padding:10px; line-height:22px;">我是${(realName)!}老师，我在一起作业网站布置英语听说作业很方便，学生特别喜欢，成绩也提高了，还是免费的，你也试试吧！邀请链接：
        ${(link)!}
        </textarea>
    </div>
    <div class="padding_ten align_center">
        <a class="public_b green_b" href="javascript:void(0);"><i><span id="clip_container"><span id="clip_button"><b>复制此邀请链接地址</b></span></span></i></a>
    </div>
    <div class=" padding_ten align_center">
        <div id="follow">
            <div id="ckepop" class="share">
                <span class="jiathis_txt">&nbsp;&nbsp;分享到：</span>
                <a class="jiathis_button_qzone">QQ空间</a>
                <a class="jiathis_button_tsina">新浪微博</a>
                <a class="jiathis_button_tqq">腾讯微博</a>
                <a class="jiathis_button_renren">人人网</a>
                <a class="jiathis_button_kaixin001">开心网</a>
            </div>
        </div>
    </div>
</div>
<!--//-->
<script type="text/javascript">
    $(function(){
        $17.copyToClipboard($("#invite_teacher_info_url"), $("#clip_button"));
    });
</script>
<script type="text/javascript">
    var jiathis_config = {
        data_track_clickback : true,
        title                : "我是${(realName)!}老师，我在一起作业网站布置英语听说作业很方便，学生特别喜欢，成绩也提高了，还是免费的，你也试试吧！邀请链接：${(link)!}"
    };
</script>
<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1342767691250526" charset="utf-8"></script>
