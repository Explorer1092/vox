<#--发放记录-->
<div class="smart_clazz_content">
    <div class="smart_clazz_left_box">
        <h3>奖励排名</h3>
        <div class="inner">
        <#if smartClazzRankList?has_content>
            <ul class="smartClazzRankUl <#if smartClazzRankList?size gt 25>smart_scroll</#if>">
                <#list smartClazzRankList as srl>
                    <li>
                        <span>－</span>
                        <b>${srl_index + 1}. </b>
                        <b class="name" title="${srl.studentName!''}">${srl.studentName!''}</b>
                        <strong>${srl.integral!''}</strong>
                    </li>
                </#list>
            </ul>
            <#if smartClazzRankList?size gt 25>
                <ul>
                    <li class="skin_all viewMoreInfo" ref-id=".smartClazzRankUl">查看全部<span>﹀</span></li>
                </ul>
            </#if>
        <#else>
            <ul>
                <li class="text_center">
                    暂无数据
                </li>
            </ul>
        </#if>
        </div>
    </div>
    <div class="smart_clazz_right_box">
        <div style="margin-bottom: 15px;"> <strong>发放学豆数：<span class="w-blue">${totalIntegral!''}</span></strong> </div>
        <#-- 发放记录列表 -->
        <#if rewardhistoryList?has_content>
            <div class="rewardHistoryDiv <#if rewardhistoryList?size gt 10>smart_scroll</#if>">
                <#list rewardhistoryList as rt>
                    <dl class="smart_clazz_right_info">
                        <dt><img width="60" height="60" src="<@app.avatar href='${rt.studentImg}'/>"></dt>
                        <dd>
                            <div class="title">
                                <p style="font-weight: bold">${rt.comment!''}</p>
                                <p style="font-size: 12px;">${rt.createDateToString()!''} &nbsp;&nbsp;奖励人：${rt.addIntegralUserName!''}</p>
                            </div>
                        <#--<a class="delete" href="javascript:void (0);">删除</a>-->
                        </dd>
                    </dl>
                </#list>
            </div>
            <#if rewardhistoryList?size gt 10>
                <div class="smart_clazz_more_box viewMoreInfo" ref-id=".rewardHistoryDiv">
                    查看更多
                </div>
            </#if>
        <#else>
            <div class="smart_clazz_data_box">
                <div class="iconNullData"></div>
                <h4><span>${tabName!''}</span>无发放奖励数据</h4>
                <p>请查看其它时间的发放记录</p>
            </div>
        </#if>
    </div>
</div>


