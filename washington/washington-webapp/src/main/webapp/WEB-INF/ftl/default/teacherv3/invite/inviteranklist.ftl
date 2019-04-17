<div class="w-base">
    <div class="w-base-title">
        <h3>全国邀请达人排行榜</h3>
    </div>
    <!--template container-->
    <div class="w-base-container">
        <!--//start-->
        <div class="w-table w-table-border-bot">
            <table >
                <thead>
                <tr>
                    <th style="width: 80px;">排名</th>
                    <th style="width: 110px">姓名</th>
                    <th>学校</th>
                    <th style="width: 120px;">邀请成功</th>
                </tr>
                </thead>
                <tbody>
                <#if inviteRankList?? && inviteRankList?size gt 0>
                    <#list inviteRankList as inviteRank>
                    <tr>
                        <th>${(inviteRank_index+1)}</th>
                        <th>${(inviteRank.userName)!} </th>
                        <th>${(inviteRank.schoolName)!}</th>
                        <th>${(inviteRank.amount)!}人</th>
                    </tr>
                    </#list>
                <#else>
                <tr>
                    <th colspan="4">暂时无数据</th>
                </tr>
                </#if>
                </tbody>
            </table>
        </div>

        <!--end//-->
    </div>
</div>
