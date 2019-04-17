<div class="cl-info">
    <p class="cl-number">当前班级剩余学豆：${integral!0}个</p>
    <a class="w-btn w-btn-small" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/smartclazz/clazzdetail.vpage?clazzId=${clazzId!}&subject=${curSubject!}">奖励学生</a>
</div>
<div class="tableBox">
    <ul class="tabBox" id="integralMenu">
        <li <#if ge0!false>class="active"</#if> data-ge="true">获取学豆</li>
        <li <#if (!ge0)!false>class="active"</#if> data-ge="false">消耗学豆</li>
    </ul>
    <div class="w-table">
        <table>
            <thead>
            <tr>
                <td style="width: 180px">日期</td>
                <td style="width: 165px">学豆</td>
                <td>来源</td>
            </tr>
            </thead>
            <tbody>
            <#if pagination?? && pagination.getTotalPages() gt 0>
                <#list pagination.getContent() as row>
                <tr <#if row_index % 2 == 0>class="odd"</#if>>
                    <td>${(row.dateYmdString)!""}</td>
                    <td>${row.integral!""}</td>
                    <td>${row.comment!""}</td>
                </tr>
                </#list>
            <#else>
            <tr>
                <td colspan="3" style="text-align:center;" id="integral_null_box">
                    <#if ge0!false>
                        您还没有任何获得学豆记录。
                    <#else>
                        您还没有任何消耗记录。
                    </#if>
                </td>
            </tr>
            </#if>
            </tbody>
        </table>
    <#if pagination?? && pagination.getTotalPages() gt 0>
        <div class="t-show-box">
            <div class="w-turn-page-list">
                <#if currentPage gt 1>
                    <a id="prePage" style="" class="enable back" href="javascript:void(0);" v="prev"><span>上一页</span></a>
                </#if>
                <a class="this" href="javascript:void(0);"><span id="currentPage">${currentPage}</span></a>
                <span>/</span>
                <a class="total" href="javascript:void(0);"><span id="totalPage">${pagination.getTotalPages()}</span></a>
                <#if pagination.hasNext()>
                    <a id="nextPage" style="" class="enable next" href="javascript:void(0);" v="next"><span>下一页</span></a>
                </#if>
            </div>
        </div>
    </#if>
    </div>
    <div class="infoBox">
        <h3>如何获取班级学豆 <span class="w-icon-public w-icon-gold PNG_24"></span></h3>
        <p>1.检查作业</p>
        <p>2.参与活动</p>
        <p>3.家长点赞兑换班级学豆 <a class="w-btn w-btn-small" href="${(ProductConfig.getMainSiteBaseUrl())!''}/teacher/flower/exchange.vpage?ref=integral&clazzId=${clazzId!}&subject=${curSubject!}" target="_blank">点赞兑换</a></p>
    </div>
</div>

<script type="text/javascript">
    $(function(){
        $("#prePage").on("click", function () {
            var pageNo = $("#currentPage").text() * 1;
            var preNo = pageNo - 1;
            if (preNo < 1) {
                preNo = 1;
            }

            createPageList({
                clazzId : currentClazzId,
                groupId : currentGroupId,
                pageNumber : preNo,
                ge0 : currentGroupGe0
            });
        });

        $("#nextPage").on("click", function () {
            var pageNo = $("#currentPage").text() * 1;
            var totalPage = $("#totalPage").text() * 1;
            var nextNo = pageNo + 1;
            if (nextNo > totalPage) {
                nextNo = totalPage;
            }
            createPageList({
                clazzId : currentClazzId,
                groupId : currentGroupId,
                pageNumber : nextNo,
                ge0 : currentGroupGe0
            });
        });

        $("#integralMenu li").on("click", function () {
            var $this = $(this);

            if($this.hasClass("active")){
                return false;
            }

            currentGroupGe0 = $this.data("ge");
            $this.addClass("active").siblings().removeClass("active");

            createPageList({
                clazzId : currentClazzId,
                groupId : currentGroupId,
                pageNumber : 1,
                ge0 : currentGroupGe0
            });
        });
    });
</script>