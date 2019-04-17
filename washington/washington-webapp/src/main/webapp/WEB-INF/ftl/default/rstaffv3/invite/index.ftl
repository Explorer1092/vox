<#import "invitemodule.ftl" as com>
<@com.page tagindex = 0>
<ul class="inviteBox">
    <li class="row_vox_left">
        <h4><i class='icon_rstaff icon_rstaff_22'></i> 短信邀请</h4>
        <div class="paslist">
            <p><strong>请在下面的输入框中填写要邀请的老师手机号码</strong><br/>（一次可填入多个手机号码，每行填写一个手机号）</p>
            <p><textarea id="phone_number_list_box" placeholder="填写要邀请的老师手机号码" name="phone_number" cols="" rows="" class="int_vox" style="width:400px; height:80px; color:#666;"></textarea></p>
            <p>发送邀请后将自动为老师注册账号和密码，并以免费短信通知。</p>
            <div class="text_center ">
                <a id="sms_invite_teacher_but" href="javascript:void(0);" class="btn_vox btn_vox_primary">发送邀请</a>
            </div>
        </div>
        <div class="msgctn">
            <h5>老师将收到如下短信内容：</h5>
            <p>${(currentUser.profile.realname)!}老师邀请您参与外专委“十二五”规划课题,登录www.17zuoye.com即可参与,系统已为您注册账号*******,登录密码******。［一起作业］</p>
        </div>
    </li>
    <li class="row_vox_right">
        <h4><i class='icon_rstaff icon_rstaff_23'></i> 链接邀请</h4>
        <div class="paslist">
            <p><strong>［复制下面的内容和链接］</strong><br/>通过 QQ 、 msn、飞信、微博、微信、邮箱等发给老师:</p>
            <textarea id="copy_info_url" rows="5" readonly style="resize: none; width: 410px; font: 12px/20px arial; height: 180px;" class="int_vox" readonly="readonly">我区申请参加外专委“十二五”规划课题的数据收集活动，各位老师可使用www.17zuoye.com网络平台参与：布置帮助学生提高学习兴趣的听说读写练习，准确控制学习时间，自动打分并生成报告。同时我在一起作业平台上传了一些单元同步试题，老师可根据教学情况酌情使用，以及时了解教学效果。为响应国家减负政策，网络练习需学生自愿参与，不得强制。未使用过一起作业的老师，可以点击以下链接注册参与：
                <#if link??>${link}</#if>
            </textarea>

            <div class="text_center spacing_vox">
                <a class="btn_vox btn_vox_primary" href="javascript:void(0);">
                    <span id="clip_container"><span id="clip_button">复制此邀请链接地址</span></span>
                </a>
            </div>
        </div>
    </li>
</ul>

<script type="text/javascript">
    $(function(){
        //剪贴板
        $17.copyToClipboard($("#copy_info_url"), $("#clip_button"));

        //发送短信邀请
        $("#sms_invite_teacher_but").on('click',function(){
            var _this = $(this);
            var pn = $("#phone_number_list_box");
            var phoneNumList = $.trim( pn.val() ).split('\n');
            if(phoneNumList.length > 50 || pn.val() == ""){
                $17.alert("请输入您要邀请老师的手机号（最多可邀请50个）");
                return false;
            }

            if(_this.hasClass("waiting")){return false;}
            _this.addClass("waiting");
            $.post("/rstaff/invite/sms.vpage", {mobile : phoneNumList.join(',')}, function(data){
                if(data.success){
                    pn.val('');
                    var error=[];
                    $.each(data.errorMobiles,function(i){
                        error += data.errorMobiles[i];
                    });
                    $17.alert(data.info + "。成功发送：" + data.successMobiles.length + "条，" + "发送失败：" + data.errorMobiles.length + "条。" + error);
                }else{
                    $17.alert(data.info);
                }
                _this.removeClass("waiting");
            });
        });
    });
</script>
</@com.page>



