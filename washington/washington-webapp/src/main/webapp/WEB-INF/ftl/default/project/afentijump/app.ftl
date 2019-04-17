<#import "../../layout/mobile.layout.ftl" as temp>
<@temp.page title="阿分题-天天练习题" dpi="">
    <@app.css href="public/skin/project/afentijump/css/jumpdetails.css"/>
    <@sugar.capsule js=['jquery','voxLogs'] />

    <div class="jumpDetails-box">
        <div class="d-banner"></div>
        <div class="d-inner">
            <div class="d-info">
                <p>开通阿分题数学或阿分题英语后，每天完成任务都可以获得学豆奖励。</p>
            </div>
            <div class="d-tip">阿分题英语（开通后每日可获得）：</div>
            <div class="d-column">
                <div class="d-head"><p>每日奖励</p></div>
                <div class="d-list">
                    <ul>
                        <li class="title"><b>每日学习任务</b><b>获得奖励</b></li>
                        <li><span><i class="ico"></i><i class="ico ico-star"></i><i class="ico ico-star"></i></span><span>奖励<em>10</em><i class="ico ico-bean"></i></span></li>
                        <li><span><i class="ico"></i><i class="ico"></i><i class="ico ico-star"></i></span><span>奖励<em>15</em><i class="ico ico-bean"></i></span></li>
                        <li><span><i class="ico"></i><i class="ico"></i><i class="ico"></i></span><span>奖励<em>20</em><i class="ico ico-bean"></i></span></li>
                    </ul>
                </div>
                <#if (currentUser.userType == 3)!false>
                    <#if !([1, 2]?seq_contains(currentStudentDetail.getClazzLevelAsInteger()))>
                        <div class="d-btn js_show" style="display: none">
                            <a href="javascript:void(0);" class="see_btn" id="js_afenti">去看看</a>
                        </div>
                    </#if>
                </#if>
            </div>
            <div class="d-tip">阿分题数学（开通后每日可获得）：</div>
            <div class="d-column">
                <div class="d-head headDiffer"><p>每日奖励</p></div>
                <div class="d-list">
                    <ul>
                        <li class="title"><b>每日学习任务</b><b>获得奖励</b></li>
                        <li><span><i class="ico"></i><i class="ico ico-star"></i><i class="ico ico-star"></i></span><span>奖励<em>10</em><i class="ico ico-bean"></i></span></li>
                        <li><span><i class="ico"></i><i class="ico"></i><i class="ico ico-star"></i></span><span>奖励<em>15</em><i class="ico ico-bean"></i></span></li>
                        <li><span><i class="ico"></i><i class="ico"></i><i class="ico"></i></span><span>奖励<em>20</em><i class="ico ico-bean"></i></span></li>
                    </ul>
                </div>
                <#if (currentUser.userType == 3)!false>
                    <div class="d-btn js_show" style="display: none">
                        <a href="javascript:void(0);" class="see_btn" id="js_afentimath">去看看</a>
                    </div>
                </#if>
            </div>
        </div>

    </div>
    <script type="text/javascript">
        $(function(){
            var devTestSwitch = ${(ftlmacro.devTestSwitch)?string};
            var afentiUrl = "https://www.17zuoye.com/resources/apps/hwh5/afenti/V1_0_0/index.html",
                    afentiMathUrl = "https://www.17zuoye.com/resources/apps/hwh5/afenti_math/V1_0_0/index.html";
            if(devTestSwitch){
                afentiUrl = "https://www.test.17zuoye.net/resources/apps/hwh5/afenti/V1_0_0/index.html";
                afentiMathUrl = "https://www.test.17zuoye.net/resources/apps/hwh5/afenti_math/V1_0_0/index.html";
            }

            var session_key = "";
            var native_version = "";

            if( window.external["getInitParams"] ){
                var $params = window.external.getInitParams();

                setTimeout(function(){
                    if($params){
                        $params = $.parseJSON($params);
                        session_key = "?session_key=" + $params.session_key;
                        native_version = $params.native_version;

                        //App版本>=2.5.0时显示跳转按钮
                        var version = native_version.split('.'),
                            part1 = parseInt(version[0]),
                            part2 = parseInt(version[1]);
                        if(part1 > 2){$(".js_show").show();}
                        else if(part1==2 && part2 >= 5){$(".js_show").show();}
                    }
                }, 100);
            }

            var jump=function(url){
                if( window["external"] && window.external["openFairylandPage"] ){
                    window.external.openFairylandPage( JSON.stringify({
                        url: url + session_key,
                        name: "fairyland_app:afentiFinalreview",
                        initParams: JSON.stringify({hwPrimaryVersion: "V2_4_0"})
                    }) );
                }else{
                    alert("版本过低，建议升级APP。");
                }
            };

            $(document).on("click", "#js_afenti", function(){
                jump(afentiUrl);
                YQ.voxLogs({
                    module : "afenti_jump",
                    op : "english_click"
                });
            });
            $(document).on("click", "#js_afentimath", function(){
                jump(afentiMathUrl);
                YQ.voxLogs({
                    module : "afenti_jump",
                    op : "math_click"
                });
            });

            <#--打点-->
            YQ.voxLogs({
                module : "afenti_jump",
                op : "pv"
            });
        });
    </script>
</@temp.page>

