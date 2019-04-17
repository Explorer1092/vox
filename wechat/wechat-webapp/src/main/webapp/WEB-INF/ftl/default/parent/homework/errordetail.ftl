<#import "../layout.ftl" as homework>
<@homework.page title="错题解析" pageJs="errordetail">
<div class="wrapper">
    <div class="header">
        <h1>
            <a href="javascript:history.back();" class="r-btn" style="float: right; margin: 15px 10px 0;">返回报告</a>
            错题
        </h1>
    </div>
    <div class="section">
        <div class="report-box" style="bottom: 19px;">
            <h2 class="title-back-2">
                题目&解析
            </h2>
            <div class="content_1" style="bottom: 30px; position: relative;">
                <div id="examViewBox" data-wrong_list='${wrongList!}'></div>
                <h2 class="title-back-2">
                    解析
                </h2>
                <div id="parseBox"></div>
            </div>
        </div>
        <div class="report-box" style="display: none;">
            <div class="write" style="border: 0">
                <div><a href="javascript:history.back();">返回报告</a></div>
            </div>
        </div>
        <div style="text-align: center; position: fixed; width: 100%; bottom: 10px">
            <a id="previous" class="r-btn" href="javascript:void (0);" style="width: 40%;padding: 25px 0;">上一题</a>
            <a id="next" class="r-btn" href="javascript:void (0);" style="width: 40%;padding: 25px 0;">下一题</a>
        </div>
    </div>
</div>
<script>
    var completeUrl = '${completeUrl!''}',examEnv = <@ftlmacro.getCurrentProductDevelopment />;
</script>
</@homework.page>