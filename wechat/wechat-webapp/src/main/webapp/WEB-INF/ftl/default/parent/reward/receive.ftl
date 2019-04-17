<#import "../layout.ftl" as parentWard>
<@parentWard.page title='领取签到奖励' pageJs="parentReceiveLoginReward">
    <@sugar.capsule css=['receiveLoginReward'] />
    <#escape x as x?html>
        <div class="parentGuide-wrap">
            <div class="parentGuide-hdbg">登录家长通领取奖励</div>
            <div class="parentGuide-head">登录家长通领取奖励</div>
            <div class="parentGuide-main">
                <div class="mainTitle">
                    <div class="tag">领取说明</div>
                </div>
                <div class="mainCont">
                    <div>签到奖励需登录【家长通APP】签到后才能领取</div>
                    <div>使用[家长通APP]能够及时关注孩子学习动态及作业报告，接收老师作业通知，更好的实现家校互动。</div>
                </div>
                <div class="mainTitle mainLine">
                    <div class="tag">领取步骤</div>
                </div>
                <div class="mainCont">
                    <div class="list"><span class="tag">1</span>下载：未下载的用户请点击下方“下载家长通 APP”按钮可下载家长通；已下载用户不用再点击下载按钮</div>
                    <div class="list"><span class="tag">2</span>登录：通过孩子账号或已绑定的手机号码登录家长通APP</div>
                    <div class="list"><span class="tag">3</span>领取：家长通APP首页点击“宝贝表现”-“班级榜单”-“家长动态榜”-“领取签到奖励”</div>
                </div>
                <div class="parentGuide-foot"><a href="http://wx.17zuoye.com/download/17parentapp?cid=100315" class="btn doReceive">下载家长通APP</a></div>
            </div>
        </div>
    </#escape>
</@parentWard.page>
