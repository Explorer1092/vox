<#import '../layout.ftl' as layout>

<@layout.page className='PersonQuestionDetail bg-fff' title="常见问题与解答" pageJs="question" specialCss="skin2" specialHead='
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>常见问题与解答</title>
'>
    <script>
        //window.PM.questionHash = '${hash!""}';
       <#--改版的样式，不适用adapt-->
        window.notUseAdapt=true;
    </script>
    <#assign hashInfo = [
        {
            "id" : "我下载了家长通APP，可以注册学生账号吗？",
            "text" : [
                "打开家长通APP输入手机号获取验证码之后，进入家长通。之后点击【绑定新学生号】，按照步骤注册新学生账号即可。"
            ],
            "contactType":"online"
        },
        {
            "id" : "如何下载一起作业学生APP?",
            "text" : [
                "下载手机app的方式有以下几种：",
                "1、请您登录网站，点击【学习中心】左侧的【课本随身听】扫描页面的二维码，下载一起作业手机（平板）版做作业。",
                "2、安卓用户可以通过应用宝，360助手等大型应用商店搜索一起作业学生端，即可下载，苹果用户可通过app store里搜索一起作业学生端，下载安装即可。"
            ],
            "contactType":"online"
        },
        {
            "id" : "在家长通APP如何修改孩子密码呢？",
            "text" : [
                "您好，您登录之后，在首页左上角点击头像进入【个人中心】-【重置孩子密码】，输入两次新密码就可以修改成功。"
            ],
            "contactType":"none"
        },
        {
            "id" : "一位家长最多可以关注几个孩子？怎么关注？",
            "text" : [
                 "一位家长最多可以关注三个孩子的一起作业；点击【个人中心】后，点击第一个学生右侧的加号添加学生，输入第二个孩子的账号密码即可。"
            ],
            "contactType":"online"
        },
        {
            "id" : "家长关注了多个孩子，怎么切换到其他孩子的账号？",
            "text" : [
                "家长登录家长通，点击【个人中心】选择想要切换的孩子的头像，单击即可更换成功。"
            ],
            "contactType":"none"
        },
        {
            "id" : "如何修改孩子名字",
            "text" : [
                "您好，如果要修改名字，请联系孩子的老师哦~"
            ],
            "contactType":"none"
        },
        {
            "id" : "如何修改家长头像",
            "text" : [
                "您好，请您点击【个人中心】上方，家长姓名前的头像位置，会弹出提示，选择【拍照】或者【从相册选择】找到自己喜欢的头像照片就可以修改完成了~"
            ],
            "contactType":"none"
        },
        {
            "id" : "1.手机做作业提示录不上音或提示没有录音权限怎么办？",
            "text" : [
                "如果遇到提示“没有录音权限”，您可以尝试一下方式重新获取权限：",
                "1、iphone通用设置：在手机的“设置->隐私->麦克风”中，允许一起作业访问；",
                "2、安卓通用设置：依次进入手机的：设置->应用管理->一起作业->权限管理”选择允许录音权限；",
                "3、OPPO：依次进入手机的“设置->安全服务->个人信息安全->按程序管理->一起作业->录音”，选择允许录音权限；",
                "4、华为：依次进入手机的“设置->权限管理->应用->一起作业->录音”，选择允许录音权限；",
                "5、Vivio：依次进入手机的“i管家->软件管理->软件权限管理->录音->一起作业”，选择允许录音权限；",
                "6、如上述方法仍无法解决问题，可尝试安装以下安全管理软件：",
                "360卫士：在“360卫士->软件管理->权限管理”中，允许一起作业获取录音权限。",
                "腾讯手机管家：在“腾讯手机管家->软件管理->软件权限管理”中，允许一起作业获取录音权限。"
            ],
            "contactType":"online"
        },
        {
            "id" : "2.手机做作业提示加载中、读取作业状态失败、页面白屏等怎么办？",
            "text" : [
                "您好，出现以上问题可能是由于网络原因导致，为了不影响学生完成作业，建议在wifi下使用，或退出重新登录尝试刷新。",
                "您也可登录一起作业学生，点击-【个人】-【设置】-【网络监测】检查网络，如其中某项显示失败，请联系我们4001601717，如需发送截图请联系在线客服为您处理。"
            ],
            "contactType":"online"
        },
        {
            "id" : "3.手机APP如何补做作业？",
            "text" : [
                "您好，过期未完成的作业可以补做，打开一起作业学生App的【作业记录】或用电脑登陆一起作业，在【学习中心】标签下，点击左侧的【作业历史】，选择要补做的作业点击后面的【补做作业】按钮即可补做一个月之内的作业（测验、寒/暑假作业过期后是不可以补做的喔）"
            ],
            "contactType":"none"
        },
        {
            "id" : "4.因网络中断作业是否能重做？",
            "text" : [
                "您好，如果作业已经提交了的话，就不能重做了；如果登录一起作业之后，能看到【开始作业】那么就还可以重做。"
            ],
            "contactType":"none"
        },
        {
            "id" : "5.如何在家长通上完成作业？",
            "text" : [
                 "您好，家长通是专为家长提供的手机应用，用于了解孩子的作业状态和表现，不能用于完成作业，您需要让孩子用一起作业学生App或用电脑登陆一起作业才能看到老师布置的作业内容并完成哦～"
            ],
            "contactType":"none"
        },
        {
            "id" : "6.作业显示未完成是怎么回事？",
            "text" : [
                "您好，作业【未完成】说明孩子未在要求的时间内完成作业，请您督促孩子用一起作业学生App或电脑登陆一起作业去完成补做~"
            ],
            "contactType":"none"
        },
        {
            "id" : "7.课本随身听如何更换教材？",
            "text" : [
                "点击【课本随身听】-【换课本】-选择【年级】-选择【教材】。"
            ],
            "contactType":"none"
        },
        {
            "id" : "8.家长通里面的课本随身听是什么？",
            "text" : [
                "【课本随身听】是可以用来听英语书中的学习内容，设置您使用的教材，在手机上听课本内容来预习和复习；"
            ],
            "contactType":"none"
        },
        {
            "id" : "9.没有作业时，我想做更多的练习怎么办？",
            "text" : [
                "您好，如果当前没有作业，您可以使用一起作业学生APP进入【自学乐园】或者在电脑一起作业网页端进入【课外乐园】选择相应产品使用学习；"
            ],
            "contactType":"online"
        },
        {
            "id" : "1.如何在家长通查看学生的作业情况",
            "text" : [
                "您好，在首页点击进入【班级群】--【作业和通知】中就可以查看孩子的作业情况了。如果老师布置了作业，您可以督促孩子使用一起作业学生App完成作业。家长通帮助家长追踪和了解作业情况，不支持做作业哦"
            ],
            "contactType":"phone"
        },
        {
            "id" : "2.如何听孩子的录音",
            "text" : [
                "您好，如果老师布置了语音作业，您可以在【作业报告】中听取孩子的录音。如何查看作业报告：在首页点击进入【班级群】--【作业和通知】中可查看所有作业的完成进度和作业报告"
            ],
            "contactType":"none"
        },
        {
            "id" : "3.怎么查看孩子的错题",
            "text" : [
                "您好，点击【学习成长】-【学业报告】可以查看孩子的【错题本】。在作业报告中也可以查看每次作业的错题详情，哪些知识点掌握的不好，您可以有针对性的对孩子进行辅导哦~"
            ],
            "contactType":"none"
        },
        {
            "id" : "1.在家长通如何查看学生近期获得学豆的记录？",
            "text" : [
                "您好，您登录最新版家长通后请点击学习成长-学业报告-查看近期学豆记录"
            ],
            "contactType":"online"
        },
        {
            "id" : "2.学生获得的学豆会清零吗？",
            "text" : [
                "您好，学豆是可以累加的，期末不会清空。但是六年级同学请注意，5月为六年级学生最后一个兑换月，5月31日之后六年级的孩子将不能再兑换奖品，请您抓紧时间兑换~"
            ],
            "contactType":"none"
        },
        {
            "id" : "1.当月兑换的奖品什么时候送到？",
            "text" : [
                "您好，由于寒暑假期间学校放假，如果您是6月、7月、8月兑换的奖品，我们统一9月份发货，9月20日左右送到您老师手中。",
                "如果你是12月、1月、2月兑换的奖品，我们统一3月份发货，3月20日左右送到您老师手中。",
                "如果您是平常月份兑换的奖品，奖品将于下月20日左右送到您老师手中~"
            ],
            "contactType":"online"
        },
        {
            "id" : "2.奖品出现质量问题的时候怎么办？",
            "text" : [
                "您好，请您将坏掉的奖品交给您的老师，并让老师拨打4001601717联系我们，客服人员会第一时间帮您解决问题~"
            ],
            "contactType":"online"
        },
        {
            "id" : "3.兑换奖品的时候提示【老师没有填写地址】我应该怎么办？",
            "text" : [
                "您好，因为您的奖品是统一寄送给老师，老师再发给同学的，所以地址需要老师填写，如果兑换时提示没有填写地址，请您联系老师在个人中心完善收货地址~"
            ],
            "contactType":"none"
        },
        {
            "id" : "4.家长通可以帮学生兑换奖品吗？",
            "text" : [
                "您好，家长通暂不支持给学生兑换奖品，如果需要兑换奖品，请用电脑登录一起作业网址www.17zuoye.com登录学生账号密码后，在教学用品中心里兑换奖品哦~"
            ],
            "contactType":"none"
        },

        {
            "id" : "1.如何设置目标？",
            "text" : [
                "在“学习成长”--“成长心愿单”页面，就可以设置目标；家长设置的目标将由“目标次数”和“目标内容”共同组成，比如：目标次数选择“3次”，目标内容输入“洗碗”，最后将自动生成目标：完成3次洗碗；"
            ],
            "contactType":"none"
        },
        {
            "id" : "2.如何增加进度、得到奖励",
            "text" : [
                "1、每月免费赠送家长10学豆用于家长奖励学生，更多奖励由家长来奖励学生；",
                "2、家长可以在右上角的“查看学生申请”查看学生的奖励心愿、进度申请；",
                "3、家长设置的目标将由“目标次数”和“目标内容”共同组成，比如：目标次数选择“3次”，目标内容输入“洗碗”，最后将自动生成目标：完成3次洗碗；",
                "4、设置的目标、奖励不能修改，但完成进度可以由家长自由控制；"
            ],
            "contactType":"online"
        },
        {
            "id" : "1.自学应用如何开通？",
            "text" : [
                "在【学习成长】中选择要开通的自学应用，选择相应的周期，点击【立即开通】 付费购买即可，部分学习应用由第三方提供，请自愿购买。"
            ],
            "contactType":"none"
        },
        {
            "id" : "2.我购买的自学产品为什么不能在家长通使用？",
            "text" : [
                "您好，自学应用产品名称后均显示【APP/电脑适用】或者【电脑使用】；",
                "【APP/电脑适用】需要在一起作业学生APP中进入【自学乐园】或者在电脑一起作业网页端进入【课外乐园】选择相应产品使用学习；",
                "【电脑使用】则只能在电脑一起作业网页端进入【课外乐园】选择相应产品使用学习",
                "部分自学产品已可以在家长通直接使用，凡有【免费体验】或【立即使用】按钮的产品都可以在家长通中使用"
            ],
            "contactType":"none"
        },
        {
            "id" : "3.购买自学产品错误怎么办？",
            "text" : [
                "您好，如果产品购买错误，建议您电话联系客服人员进行退款，再购买即可。"
            ],
            "contactType":"phone"
        },

        {
            "id" : "如何参与班级群聊？",
            "text" : [
                "请您在首页点击【消息】，选择相应班级进入群聊界面；",
                "若一个孩子不同科目的老师未关联，则分别显示两个科目的群",
                "若孩子还未加入班级，则显示未加入班级提示",
                "若班群正在开通中，则建议家长点击“申请加速”按钮，可以加速开通"
            ],
            "contactType":"none"
        },
        {
            "id" : "如果我有多个孩子，怎样能够分别进入班级群聊？",
            "text" : [
                "请您在首页点击【消息】，选择相应班级进入群聊界面；",
                "若有两个孩子，则同时显示两个孩子的群（最多显示3个孩子的群）"
            ],
            "contactType":"none"
        },
        {
            "id" : "如何查看老师发布的作业及其他通知？",
            "text" : [
                "请您在首页点击【消息】，选择相应班级进入群聊界面；",
                "然后点击班级顶端【作业和通知】进入，如老师布置作业或者发布通知，则会在该页面看到通知提醒。"
            ],
            "contactType":"none"
        },
        {
            "id" : "教育资讯推送规则？",
            "text" : [
                "教育资讯以精选家庭教育资讯内容为主，每天定时推送，支持分享、点赞和评论，评论经筛选后进行前端展示。"
            ],
            "contactType":"none"
        }
    ]>
<div class="proAnswer-box">
    <#list hashInfo as hashObj>
        <#if hashObj.id==hash!"">
            <div id="${hashObj.id}">
                <div class="pa-main">
                    <h1>${hashObj.id}</h1>
                    <#if hashObj.text?size == 1>
                        <p>${hashObj.text[0]}</p>
                    <#else>
                        <#list hashObj.text as text>
                            <p>${text}</p>
                        </#list>
                    </#if>
                </div>
                <#if hashObj.contactType=="online">
                    <div class="pa-footer">
                        <div class="inner">
                            <a href="/view/mobile/parent/send_question?dest_id=${dest_id!''}&qs_type=${qs_type!''}" class="pa-btn doTrack do_parentApp_entrance" data-track = "m_1dib82tl|o_KnMUdvKq|${type_title!''}_${hashObj.id}">没有解决我的问题，联系在线客服</a>
                        </div>
                    </div>
                <#elseif hashObj.contactType=="phone">
                    <div class="pa-footer">
                        <div class="inner">
                            <a href="tel:400-160-1717" class="pa-btn do_not_add_client_params">没有解决我的问题，拨打电话客服</a>
                        </div>
                    </div>
                <#else>
                </#if>

            </div>
            <p class="hide doAutoTrack" data-track = "m_1dib82tl|o_qjrNeOls|${type_title!''}_${hashObj.id}"></p>
        </#if>
    </#list>
</div>
</@layout.page>
