<#import "../../../layout/mobile.layout.ftl" as temp>
<@temp.page dpi="">
<@app.css href="public/skin/project/afentparentreport/css/knowledge.css"/>
<@sugar.capsule js=["jquery", "core", "voxSpread"]/>
<style>
    #headerBanner img{ width: 100%; display: inline-block; vertical-align: middle;}
</style>

<div class="knowledgeBox-header">
    <div class="head-inner">
        ${headerTitle}
    </div>
</div>

<div id="headerBanner"></div>

<div class="aFenTi-knowledgeBox">
    <div class="k-banner">
        <#if lastPracticeNum?? && lastPracticeNum != 0>
            <div class="k-title">上周练习<span class="num">${lastPracticeNum!0}</span>题 <span class="sub">${practiceAchievement!'进步神速'}！</span></div>
        <#else>
            <div class="k-title">报告已过期</div>
        </#if>
        <div class="k-content">
            <p class="con">${allStudyPointNum!0}</p>
            <p class="info">${studyPointTitle!'---'}</p>
        </div>
        <div class="tag">上周知识点</div>
    </div>
    <div class="k-table">
        <table cellpadding="0" cellspacing="0">
            <thead>
            <tr>
                <td style="width: 10%">序号</td>
                <td style="width: 50%">知识点</td>
                <td style="width: 20%">练习次数</td>
                <td style="width: 20%">正确率</td>
            </tr>
            </thead>
            <tbody id="lastStudyPoints">
            <#if (lastStudyPoints?size gt 0)!false>
                <#list lastStudyPoints as lsp>
                    <tr style="display: ${(lsp_index gt 2)?string("none", "")}" data-index="${lsp_index}">
                        <td>${(lsp_index + 1)!0}</td>
                        <td>${(lsp.word)!0}</td>
                        <td>${(lsp.practiceTimes)!0}</td>
                        <td>${((lsp.correctRate*100)!0)?string('0.##')}%</td>
                    </tr>
                </#list>
            <#else>
                <tr>
                    <td style="padding: 80px; text-align: center;" colspan="4">暂无数据</td>
                </tr>
            </#if>
            </tbody>
        </table>
        <#if (lastStudyPoints?size gt 3)!false>
            <div class="js-clickMore" data-show_count="3" style="background-color: #e33f4a; font-size: 24px; cursor: pointer; text-align: center; color: #fff; padding: 8px;">查看更多</div>
        </#if>
    </div>
    <div class="k-dynamic">
        <h3>同学动态</h3>
        <ul>
            <#if (classmateDynamics?size gt 0)!false>
                <#list classmateDynamics as cds>
                    <li>${(cds)!0}</li>
                </#list>
            <#else>
                <li style="text-align: center; padding: 50px 0;">暂无同学动态</li>
            </#if>
        </ul>
    </div>
</div>
<#if (remainDay lt 7)!false>
<div class="knowledge-footer">
    <div class="k-empty"></div>
    <div class="k-btn">
        <p class="tip">${buyContent!'家长您好，阿分题英语的 3节免费试用课已结束，无法继续学习，请开通全部课程让孩子继续自学。'}</p>
        <a href="javascript:void(0);" class="opened_btn log-buy">${buyBtnText!'开通学习'}</a>
    </div>
</div>
<#else>
    <#if (subject == 'ENGLISH')!false>
    <div class="knowledge-footer">
        <div class="k-empty"></div>
        <div class="k-btn">
            <p class="tip">87.3%的孩子还会继续在走美中学习单词</p>
            <a href="javascript:void(0);" class="open-UsaAdventure open-btn log-UsaAdventure">学习单词</a>
        </div>
    </div>
    <#else>
    <div class="knowledge-footer">
        <div class="k-empty"></div>
        <div class="k-btn">
            <p class="tip">本周所学习知识点还可以观看视频讲解</p>
            <a href="javascript:void(0);" class="open-FeeCourse open-btn log-FeeCourse">观看视频</a>
        </div>
    </div>
    </#if>
</#if>
<script type="text/javascript">
    $17.voxLog({
        module : "LearningReport",
        op: "Click-afentiV2-${subject}",
        sid : $17.getQuery("userId")
    }, "student");

    var listLength = ${(lastStudyPoints?size)!0};
    $(document).on("click", ".js-clickMore", function(){
        var $this = $(this);
        var $currentShow = $this.attr("data-show_count") * 1;

        if($currentShow >= listLength){
            $this.hide();
            return false;
        }

        if($currentShow == 3){
            $this.attr('data-show_count', $currentShow + 7);
            for(var i = $currentShow; i < $currentShow + 7; i++){
                $("#lastStudyPoints tr").eq(i).show();
            }
        }else{
            for(var i = $currentShow; i < $currentShow + 10; i++){
                $("#lastStudyPoints tr").eq(i).show();
            }
            $this.attr('data-show_count', $currentShow + 10);
        }
    });

    $(document).on("click",".opened_btn",function(){
        $17.voxLog({
            module : "LearningReport",
            op: "ToOpen-afentiV2-${subject}",
            sid : $17.getQuery("userId")
        }, "student");

        var buyUrl = "${buyUrl!}";

        if(getAppVersion() != ''){
            buyUrl = buyUrl + '&app_version=' + getAppVersion() + "&refer=" + getRefer("250001");
        }

        location.href = buyUrl;
    });

    //获取订单创建来源
    function getRefer(defaultRefer) {
        var refer = getQueryString("refer");
        return refer == null || refer == '' ? defaultRefer : refer;
    }
    //获取App版本
    function getAppVersion(){
        var native_version = "";

        if(window["external"] && window.external["getInitParams"] ){
            var $params = window.external.getInitParams();

            if($params){
                $params = eval("(" + $params + ")");

                native_version = $params.native_version;
            }
        }else if(getQueryString("app_version")){
            native_version = getQueryString("app_version") || "";
        }
        return native_version;
    }

    //Get Query
    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]); return null;
    }

    function delayedJump(appKey){
        "use strict";


    }

    // 点击：中间页打开自学产品方式
    $(document).on('click', '.open-UsaAdventure', function(){
        "use strict";
        setTimeout(function(){
            location.href = '/app/redirect/openapp.vpage' +
                    '?appKey=UsaAdventure' +
                    '&platform=PARENT_APP' +
                    '&productType=APPS' +
                    '&open=page' +
                    '&app_version=' + getAppVersion() +
                    '&version=' + getAppVersion() +
                    '&refer=250003';
        }, 50);
    });

    function getExternal(){
        var _WIN = window;
        if(_WIN['yqexternal']){
            return _WIN.yqexternal;
        }else if(_WIN['external']){
            return _WIN.external;
        }else{
            return _WIN.external = function(){};
        }
    }

    $(document).on('click', '.open-FeeCourse', function(){
        "use strict";
        getExternal().openSecondWebview(JSON.stringify({
            shareType: 'NO_SHARE_VIEW',
            shareContent: '',
            shareUrl: '',
            type: '',
            url: '/app/redirect/jump.vpage?appKey=FeeCourse&platform=PARENT_APP&productType=APPS&refer=250003'
        }));
    });

    var logModule = 'report_flow';
    //打点：走遍美国学英语推广位被加载
    if(document.querySelector('.log-UsaAdventure')){
        $17.voxLog({
            module : logModule,
            op: "usaadventure_button_load"
        }, "student");
    }
    //打点：走遍美国学英语推广位被点击
    $(document).on('click', '.log-UsaAdventure', function(){
        "use strict";
        $17.voxLog({
            module : logModule,
            op: "usaadventure_button_click"
        }, "student");
    });
    //打点：错题精讲推广位被加载
    if(document.querySelector('.log-FeeCourse')){
        $17.voxLog({
            module : logModule,
            op: "feecourse_button_load"
        }, "student");
    }
    //打点：错题精讲推广位被点击
    $(document).on('click', '.log-FeeCourse', function(){
        "use strict";
        $17.voxLog({
            module : logModule,
            op: "feecourse_button_click"
        }, "student");
    });
    //打点：续费按钮被加载
    if(document.querySelector('.log-buy')){
        $17.voxLog({
            module : logModule,
            op: "renewal_button_load"
        }, "student");
    }
    //打点：续费按钮被点击
    $(document).on('click', '.log-buy', function(){
        "use strict";
        $17.voxLog({
            module : logModule,
            op: "renewal_button_click"
        }, "student");
    });

    <#--顶部广告位 -->
    YQ.voxSpread({
        tag : "div",
        keyId : 320101,
        boxId : $("#headerBanner")
    });
</script>
</@temp.page>