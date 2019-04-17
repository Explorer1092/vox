<#import "module.ftl" as com>
<@com.page title="学生" type="student">
    <@app.css href="public/skin/helpkf/css/skin.css" />
<!--//start-->
<div class="t-helpCenter-main">
    <div class="nav">
        <ul>
            <li class="title">问题帮助</li>
            <li class="js-clickLeftTypeBtn" data-type="APP下载使用问题"><a href="javascript:void(0);">APP下载使用问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="账号密码与登录问题"><a href="javascript:void(0);">账号密码与登录问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="学习用品中心问题"><a href="javascript:void(0);">学习用品中心问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="班级问题"><a href="javascript:void(0);">班级问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="作业相关"><a href="javascript:void(0);">作业相关</a></li>
            <li class="js-clickLeftTypeBtn" data-type="学豆问题"><a href="javascript:void(0);">学豆问题</a></li>
            <li class="js-clickLeftTypeBtn" data-type="其他问题"><a href="javascript:void(0);">其他问题</a></li>
            <#--<li class="js-clickLeftTypeBtn textColor" data-type="给我们写建议" ><a href="javascript:void(0);">给我们写建议</a></li>-->
            <li class="js-clickLeftTypeBtn textColor" data-type="联系客服" ><a href="javascript:void(0);">联系客服</a></li>
        </ul>
    </div>
    <div id="detailsItems"></div>
</div>
<!--end//-->
<script type="text/javascript">
    publicTel = "<@ftlmacro.hotline/>";
    detailItem = {
        "APP下载使用问题": [{
            name: "一起小学学生端、家长通下载方式？",
            content: "请您登录按钮旁的【APP】下载，扫描对应二维码即可完成下载。",
            tel: false,
            server: false
        }, {
            name: "学生端和家长通APP的区别是什么？",
            content: "一起小学学生端：主要是为学生提供一个做作业的平台，方便孩子做作业以及参加同学互动；<br/>家长通：主要作用为帮助家长查看孩子最新作业，错题报告，老师通知，学校表现以及设定家长奖励，增强家长与宝贝互动。",
            tel: false,
            server: false
        },{
            name: "下载APP时点击“立即下载”无反应，怎么办？",
            content: "请您尝试退出重新下载或关机重启后再尝试下载。如无法解决您的问题，请联系客服帮您解决。",
            tel: false,
            problem : "question_other",
            server: false
        }],
        "账号密码与登录问题": [{
            name: "忘记密码怎么办？如何找回？",
            content: "1.请您点击<a href='/ucenter/resetnavigation.vpage?ref=help' class='linkFont' style='color: #39f;' target='_blank'>【忘记密码】</a>即可自助找回账号密码；<br/>2.忘记账号或密码时，请联系自己的任课老师询问账号或重置密码；<br/>3.如果您绑定了家长通，登录之后点击右下方【个人中心】-【学生端设置】-【孩子忘记密码？点击重置】，输入两次新密码就可以修改成功。",
            tel: false,
//            problem : "question_account",
            server: false
        },{
            name: "我是学生，登录的时候显示登录失败，怎么办？",
            content: "请您检查网络是否正常，或更换猎豹或谷歌浏览器进行尝试。",
            tel: false,
            server: false,
            specialType : false
        },{
            name: "绑定手机获取的验证码在哪里？为什么我收不到？",
            content: "  手机验证码是以短信的形式发送到您的绑定手机上，如无法获取验证码，建议您间隔30分钟后重新操作，如果仍然无法收到验证码，请您用收取验证码的手机致电400-160-1717客服会协助你查询验证码。",
            tel: false,
            server: false
        },{
            name: "我登录错了账号，怎么退出？",
            content: "请您点击右上角【学生姓名】-找到【退出】，点击即可退出当前账号。",
            tel: false,
            server: false
        },{
            name: "怎么修改密码？怎么绑定手机？怎么绑定邮箱？",
            content: "请您在首页右上角【学生姓名】-【个人中心】-【账号安全】，即可进行修改密码、绑定手机等操作。",
            tel: false,
            server: false
        }]
    };
    detailItem["学习用品中心问题"] = [
        {
            name: "学习用品到了，可不是我兑换的怎么办？学习用品收到了，可是坏了怎么办?",
            content: "请您将有问题的学习用品拍照，并将图片发送给在线客服进行处理。",
            tel: false,
            problem : "question_award",
            server: false
        },{
            name: "我兑换了学习用品什么时候能收到？",
            content: "除寒暑假外，你在当月兑换的学习用品，将于下个月20日左右寄到学校，6、7、8月兑换的学习用品集中在9月寄送，12、1、2月兑换的学习用品集中在3月寄送，学习用品会寄送到老师手中，请您联系老师领取。",
            tel: false,
            server: false
        },{
            name: "兑换时显示没有认证怎么办？",
            content: "兑换学习用品必须您的老师通过认证之后才可以兑换，学生是没有办法认证的，请联系您的老师拨打400-160-1717通过认证。",
            tel: false,
            server: false
        },{
            name: "兑换学习用品的时候显示老师没有填写地址怎么办？",
            content: "因为您的学习用品是统一寄送给老师，老师再发给同学的，所以地址需要老师填写，如果兑换时提示没有填写地址，请联系老师在个人中心完善收货地址。",
            tel: false,
            server: false
        },{
            name: "学习用品被抢光了怎么办？我想要兑换的学习用品没有了怎么办？",
            content: "您可以随时关注学习用品情况，每个月月初或者月中都会有学习用品上架，注意抢购哦。",
            tel: false,
            server: false
        },{
            name: "学习用品为什么不分男女和颜色？",
            content: "学习用品中心的学习用品颜色、款式均为随机发货，兑换目前不支持选择颜色、款式，如果您不喜欢兑换的学习用品颜色、款式，建议您与同班同学交换。感谢您的理解。",
            tel: false,
            server: false
        },{
            name: "毕业班能不能兑换学习用品？",
            content: "为了保证毕业班的学生可以正常的收到兑换的学习用品，我们5月初在学生端和老师端做了提示公告，所以毕业班学生6月1日起兑换的学习用品已无法进行发货。【没有及时兑换的学生，可以用学豆购买一些虚拟学习用品，或者待中学端上线后可以邀请老师使用哦（学豆是会继续累积的，不会清零）】。",
            tel: false,
            server: false
        },{
            name: "我把礼物加入愿望盒，是可以帮我存储下来吗？",
            content: "非常抱歉，愿望盒只能显示您的学豆距离学习用品的价格差距还有多大，并不能存储。如果这个学习用品被抢光了，愿望盒中的学习用品也会没有的。",
            tel: false,
            server: false
        }
    ];
    detailItem["班级问题"] = [
        {
            name: "怎么给同班同学送礼物？怎么给老师送礼物？",
            content: "登录网站后，点击班级空间，在您头像下方有【我的礼物】-【赠送礼物】，点击之后选择相应的老师或同学以及对应的礼物，即可给老师或者同学赠送礼物。",
            tel: false,
            problem : "question_class",
            server: false
        },{
            name: "怎么更新班级新鲜事的气泡？",
            content: "登录网站后，点击班级空间，在您头像的右上角有一个彩色的小气泡，点击即可更换气泡。",
            tel: false,
            server: false
        },{
            name: "怎么加入老师的班级？",
            content: "请您先向您的老师询问注册一起小学老师的手机号码或老师的一起小学老师端账号，然后在注册页面输入老师手机号或老师的一起小学老师端账号，点击“注册学生账号“，按照指示完成注册即可。",
            tel: false,
            server: false
        },{
            name: "怎么更换班级？",
            content: "请您点击【首页】-【输入老师号码】根据提示输入现在老师的一起小学老师端账号，即可完成换班，每个学期只有一次更换班级机会，请谨慎操作。",
            tel: false,
            server: false
        }
    ];
    detailItem["作业相关"] = [
        {
            name: "点击开始作业后加载到一半不动或不显示怎么办？",
            content: "建议您使用谷歌浏览器或者猎豹浏览器，加载不出来的话建议您清理一下缓存，方法如下：<br/>1.您在谷歌浏览器中点击右上角“三个点”的图标，点击【历史记录】，然后点击上方的【清除浏览数据】即可。<br/> 2.您点击猎豹浏览器左上角的猎豹图标，点击【清除浏览数据】，然后点击【立即清理】即可。",
            tel: false,
            problem : "question_homework",
            server: false
        },{
            name: "为什么我看不到老师布置的作业？",
            content: "1.请检查【主页】-【学习任务】旁边的【老师姓名】是否正确，如果不正确请点击【输入老师号码】填写正确的老师手机号加入正确老师的班级即可看到作业；<br/>2.请查看右上角用户姓名是否与您的名字一致，如果不一致请退出用您的账号密码重新登录；<br/> 3.如以上答案均不能解决您的问题，请联系客服帮您解决问题。",
            tel: false,
            problem : "question_homework",
            server: false
        },{
            name: "为什么我打不开作业？",
            content: "建议您使用猎豹浏览器，可能由于其他浏览器不稳定，导致不能录音或无法打开作业等情况。",
            tel: false,
            server: false
        },{
            name: "因网络中断作业是否能重做？",
            content: "如果作业已经提交，就不能重做了。如果登录之后，能看到【开始作业】即可继续做作业。",
            tel: false,
            server: false
        },{
            name: "作业做完之后可以重做吗？",
            content: "如果作业已经提交，就不能重做了。如果登录之后，能看到【开始作业】那么就还可以继续做作业。",
            tel: false,
            server: false
        },{
            name: "在主页显示我有一份要补做的作业，可是为什么点击学习中心看不到补做作业呢？ ",
            content: "学习中心只显示30天内的作业情况，如果您要补做的作业超过30天，就不会在【学习中心】显示了，但是主页是可以显示的。",
            tel: false,
            server: false
        },{
            name: "如何调麦克风音量？",
            content: "请您点击电脑左下角开始键，点击控制面板，选中【声音】-【录制】，选中【麦克风】鼠标右键点击【属性】-【级别】调整麦克风声音大小。",
            tel: false,
            server: false
        },{
            name: "我录不上音怎么办？",
            content: "请您点击电脑左下角开始键，点击【程序】-【附件】-【录音机】，进行录音测试来检测一下您的麦克风有没有声音，如果没有声音，请您检查麦克风是否插好并调整麦克风音量，如果有声音，请联系客服帮您解决问题。",
            tel: false,
            problem : "question_homework",
            server: false
        },{
            name: "怎么查看孩子的错题？",
            content: " 如果您绑定了【家长通】，孩子完成作业24小时之后，可以点击【学习进度】-下拉【错题本】可以查看孩子的错题情况，您可以有针对性的对孩子进行辅导。",
            tel: false,
            server: false
        }
    ];
    detailItem["学豆问题"] = [
        {
            name: "我如何能够获得学豆？",
            content: "请您点击右上角【学生姓名】-找到【个人中心】-【我的学豆】，即可查看详细的学豆获取规则。",
            tel: false,
            problem : "question_award",
            server: false
        },{
            name: "我的学豆减少了怎么办？",
            content: "请您点击右上角【学生姓名】-找到【个人中心】-【我的学豆】，可以查看最近三个月获取以及消耗学豆的明细，如果消耗的学豆并非您本人操作，请您修改登录密码，以此保证您的账号安全。",
            tel: false,
            server: false
        },{
            name: "学豆学期末会清空吗？",
            content: "不会。学豆是可以累加的，期末不会清空。六年级同学请注意，5月为六年级学生最后一个兑换月，5月31日之后六年级的孩子将不能再兑换学习用品，请抓紧时间兑换。",
            tel: false,
            server: false
        },{
            name: "我的学豆可以做什么？",
            content: "学豆可以在学习用品中心兑换相应的学习用品。",
            tel: false,
            server: false
        }
    ];
    detailItem["其他问题"] = [
        {
            name: "老师没有使用，我可以使用吗？",
            content: "非常抱歉，目前网站不支持个人使用，请您联系老师注册账号后一起使用。",
            tel: false,
            problem : "question_other",
            server: false
        },{
            name: "怎么修改我的头像？",
            content: "目前电脑端不支持修改头像，请您登陆手机端进行修改，感谢您的理解。",
            tel: false,
            server: false
        },{
            name: "儿童上网管家在哪里下载？ ",
            content: " 您电脑登录账号后，右下角可以看见儿童健康上网，可以进行安装。",
            tel: false,
            server: false
        },{
            name: "下载了儿童上网管家并安装了，退出时忘记密码了怎么办？ ",
            content: " 您可以拨打上网管家的客服电话400-609-9588。",
            tel: false,
            server: false
        }
    ];
    /*detailItem["给我们写建议"] = [
        {
            name: "给我们写建议",
            content: "",
            tel: false,
            problem : "question_advice",
            server: false
        }
    ];*/
    detailItem["联系客服"] = [
        {
            name: "联系客服",
            content: "",
            tel: false,
            problem : "question_other",
            server: true
        }
    ];

</script>
</@com.page>