<#import '../apps/list.ftl' as apps>
<#import '../layout/layout.ftl' as temp>
<@temp.page >
    <div class="t-app-container">
        <div class="t-app-inner">
            <!--tai-head-->
            <div class="ta-head">
                <h1>${appName!}</h1>
            </div>
            <!--ta-content-->
            <div class="ta-content">
                <dl class="ta-game-box">
                    <@apps.appsList appName='${appName!}'/>
                    <dd>
                        <div class="game-container-box">
                            <div style="width: 910px; margin: 0 auto;">
                                <div style="text-align: center; font-size: 28px; color: #f00; padding: 250px 0 0;">
                                    ${suspendMessage!'维护中...'}
                                </div>
                            </div>
                        </div>
                    </dd>
                </dl>
            </div>
        </div>
    </div>
</@temp.page>