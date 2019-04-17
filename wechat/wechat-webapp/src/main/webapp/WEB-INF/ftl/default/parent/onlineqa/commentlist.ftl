<#import "../layout.ftl" as comment>
<@comment.page title='评论' pageJs="onlineqaComment">
    <@sugar.capsule css=['onlineqa','jbox'] />
    <div class="comment-box m-wrap">
        <!--初次评论-->
        <div class="comm-first">
            <div class="commf-mn1">
                <img style="width:100%;" src="${question.imageUrl}">
            </div>
            <div class="commf-mn2 commstar">
                <div class="total">
                    <p class="comm-left">整体评分</p>
                    <p class="comm-right star">
                        <#if question.hasCommented>
                            <#list 1..5 as x>
                                <#if x<=question.totalStars>
                                    <span class="star-active"></span>
                                <#else>
                                    <span></span>
                                </#if>
                            </#list>
                        <#else>
                            <span></span><span></span><span></span><span></span><span></span>
                        </#if>
                    </p>
                </div>
            </div>
            <div class="commf-mn3 commstar">
                <div class="speed">
                    <p class="comm-left">答题速度</p>
                    <p class="comm-right star">
                        <#if question.hasCommented>
                            <#list 1..5 as x>
                                <#if x<=question.speedStars>
                                    <span class="star-active"></span>
                                <#else>
                                    <span></span>
                                </#if>
                            </#list>
                        <#else>
                            <span></span><span></span><span></span><span></span><span></span>
                        </#if>
                    </p>
                </div>
                <div class="quality">
                    <p class="comm-left">答题质量</p>
                    <p class="comm-right star">
                        <#if question.hasCommented>
                            <#list 1..5 as x>
                                <#if x<=question.qualityStars>
                                    <span class="star-active"></span>
                                <#else>
                                    <span></span>
                                </#if>
                            </#list>
                        <#else>
                            <span></span><span></span><span></span><span></span><span></span>
                        </#if>
                    </p>
                </div>
            </div>
        </div>
        <!--追加评论-->
        <div class="comm-second">
            <div class="comms-mn1">
                <#list question.comments as comment>
                    <div class="comm-content">
                        <p>${ comment.content }</p>
                        <span>${ comment.date }</span>
                    </div>
                </#list>
            </div>
            <div class="comms-mn2">
                <h2>追加评论</h2>
                <textarea placeholder="请输入..." id="tt-comment"></textarea>
            </div>
        </div>
        <!--评论按钮-->
        <div class="comm-buttons">
            <a class="comm-btn" href="javascript:void(0)">发表评论</a><!--发表评论or发表追评-->
        </div>
    </div>

    <script>
        var question=${json_encode(question)};
        var productType=${json_encode(productType)};
    </script>
</@comment.page>