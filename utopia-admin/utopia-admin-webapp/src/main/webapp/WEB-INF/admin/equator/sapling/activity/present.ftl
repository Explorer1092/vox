<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="17作业赠送的小U产品" page_num=24>
<style xmlns="http://www.w3.org/1999/html">

    .panel-info {
      border-color: #bce8f1;
    }

    .panel {
        margin-bottom: 10px;
        background-color: #fff;
        border: 1px solid transparent;
        border-radius: 4px;
        -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
        box-shadow: 0 1px 1px rgba(0, 0, 0, .05);
    }

    .panel-info > .panel-heading {
        color: #31708f;
        background-color: #d9edf7;
        border-color: #bce8f1;
    }

    .panel-heading {
        padding: 10px 15px;
        border-bottom: 1px solid transparent;
        border-top-left-radius: 3px;
        border-top-right-radius: 3px;
    }

    .panel-title {
        margin-top: 0;
        margin-bottom: 0;
        font-size: 16px;
        color: inherit;
    }

    .panel-body {
        padding-top: 10px;
        padding-bottom: 10px;
        padding-left: 15px;
        padding-right: 15px;
    }

    .present-info span {
        font-weight: bold;
        padding: 0 10px;
        转    }
</style>

<span class="span9" style="font-size: 14px">

    <h4>青苗计划当前礼品池信息</h4>

    <div class="panel panel-info">
        <div class="panel-heading">
            <h4 class="panel-title">免费小U产品池信息</h4>
        </div>
        <#if presentPool??>
            <div class="panel-body present-info">
                17作业免费投放：<span>${presentPool.postBy17}</span>
                施助者购买触发投放：<span>${presentPool.postByRicher}</span>
                当前可被领取的免费小U产品：<span>${presentPool.leftPresent}</span>
                已被领走的免费小U数量：<span>${presentPool.postBy17?default(0) + presentPool.postByRicher?default(0) - presentPool.leftPresent?default(0)}</span>
            </div>
        </#if>
    </div>

    <div class="panel panel-info">
        <div class="panel-heading">
            <h4 class="panel-title">17作业免费投放历史</h4>
        </div>
        <#if presentList?has_content>
            <table class="table table-bordered table-condensed" style="margin-bottom: 0px">
                <thead>
                <tr>
                    <th>投放状态</th>
                    <th>投放进度</th>
                    <th>开始投放时间</th>
                    <th>结束投放时间</th>
                    <th>投放数量</th>
                </tr>
                </thead>
                <tbody>
                <#list presentList?sort_by(["begin"])?reverse as present>
                <#assign beginDateTime = present.begin?number * 1000 />
                <#assign endDateTime = present.end?number * 1000 />
                <#assign processing = presentProcess?has_content && presentProcess == present.id>
                <tr <#if processing>style="background: greenyellow" title="投放中" </#if>>
                    <td><#if processing>正在投放<#else >投放完毕</#if></td>
                    <td><#if processing>${((present.count - present.getLeftToSend(.now)) / present.count)?string("0.00%")}<#else >100%</#if></td>
                    <td>${beginDateTime?number_to_datetime}</td>
                    <td>${endDateTime?number_to_datetime}</td>
                    <td>${present.count}</td>
                </tr>
                </#list>
                </tbody>
            </table>
        </#if>
    </div>

</span>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script>

    $(function () {
    });

</script>
</@layout_default.page>