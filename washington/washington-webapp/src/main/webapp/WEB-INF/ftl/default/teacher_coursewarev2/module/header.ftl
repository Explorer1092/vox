<#--header-->
<div class="headBox">
    <div class="headBoxInner">
        <div class="headerMid">
            <a class="headLogo" href="/"></a>
            <div class="headRight">
                <div class="rightPart">
                    <div class="l-btn active headerJoinGame" style="display: none">立即报名</div>
                    <a class="l-btn" href="/help/downloadApp.vpage?refrerer=pc">app下载</a>
                </div>
                <div class="rightPart rightPart02">
                    <div class="r-btn">
                        <span class="icon shareIcon"></span>
                        <span>分享活动</span>

                        <!-- 二维码 -->
                        <div class="qrcode-box">
                            <img alt="" class="qrcode appLinkQrcode">
                        </div>
                    </div>
                    <div class="r-btn r-btn02">
                        <span class="icon signIcon"></span>
                        <#if currentUser?? && currentUser.id?has_content>
                        <span><a href="/teacher/index.vpage">${(currentUser.profile.realname)!''}</a> <a href="javascript:void(0)" class="linktrack" data_op="o_b6SPBYbdBf" data_link="${(ProductConfig.getMainSiteUcenterLogoutUrl())!''}">[退出]</a></span>
                        <#else>
                        <a href="javascript:void(0)" class="linktrack" data_op="o_RWxGlDM9Fn" data_link="${(ProductConfig.getMainSiteUcenterLoginUrl())!''}">登录</a>
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>