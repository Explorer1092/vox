<#import '../../apps/list.ftl' as apps>
<#import '../../layout/layout.ftl' as temp>
<@temp.page pageName='assignments'>
<style>html,body{ font-family:"微软雅黑", "Microsoft YaHei", arial;}</style>
    <@sugar.capsule js=["jquery.flashswf","ko","examCore","afenti"] />
    <#assign PageBalckList = false/>
    <#if (currentStudentDetail.inPaymentBlackListRegion)!false>
        <#assign PageBalckList = true/>
    </#if>
<div class="t-app-container" style="height: auto; padding: 0 0 20px;">
    <div class="t-app-inner" style="height: auto;">
        <!--tai-head-->
        <div class="ta-head" style="margin: 0 auto; float: none;">
            <h1><@ftlmacro.gameAreaVersion/>阿分题-数学</h1>
            <#if !PageBalckList>
                <div class="btn-area" style="margin: 0 1px; width: auto;">
                    <#if  (daysToExpire gt 0 && daysToExpire lt 8)!false>
                        <#--<strong>您购买的阿分题学习产品还有 <span style="color: green;">${daysToExpire!0}</span> 天过期</strong>-->
                        <a href="/apps/afenti/order/afentimath-cart.vpage?refer=300003&type=1" title="续费"
                           class="w-btn w-btn-green">续费</a>
                    <#elseif (daysToExpire == 0)!false>
                        <a href="/apps/afenti/order/afentimath-cart.vpage?refer=300003&type=1" title="开通"
                           class="w-btn w-btn-green">开通</a>
                    <#else>
                        <#--<strong>您购买的阿分题学习产品已过期！</strong>-->
                        <a href="/apps/afenti/order/afentimath-cart.vpage?refer=300003&type=1" title="续费" class="w-btn w-btn-green">续费</a>
                    </#if>
                </div>
            </#if>
            <div class="ctn-area">
                <div class="ta-info">
                <#--start-->
                    <div class="v-textScrollInfo">
                    ${pageBlockContentGenerator.getPageBlockContentHtml('StudentAfentiMathIndex', 'AfentiMathBanner')}
                    </div>
                    <script type="text/javascript">
                        $(function(){
                            $(".v-textScrollInfo").textScroll({
                                line : 1,
                                speed: 1000,
                                timer: 3000
                            });
                        });
                    </script>
                <#--end-->
                </div>
            </div>
        </div>


        <!--ta-content-->
        <div class="ta-content">
            <dl class="ta-game-box">
                <dd style="width: 1000px; height: auto; float: none; margin: 0 auto; background: none; box-shadow: none; ">
                    <div class="game-container-box">
                        <#--//start-->
                            <#assign file="AfentiSchoolMath"/>
                            <div id="afenti_facade_box"></div>
                            <div id="movie" style="width: 900px; margin: 0 auto;">
                                <div id="install_flash_player_box" style="margin:20px;">
                                    <span id="install_download_tip" style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                                        您未安装Flash Player插件，请 <a href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                                        <br/><br/>
                                        <span>
                                            如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                                        </span>
                                    </span>
                                </div>
                            </div>

                            <#include "../../../flash/prepareflashloadercdntypes.ftl"/>


                            <script type="text/javascript">

                                //购买不同周期的阿分题（30天 90天）
                                function buyAfentiByCycle(cycle){
                                    //cycle 30: thirtyDays, 90 : ninetyDays
                                    cycle = $17.isBlank(cycle) ? 'thirtyDays' : cycle;
                                    setTimeout(function(){
                                        window.location.href = '/apps/afenti/order/afentimath-cart.vpage?afentiCycle=' + cycle
                                    }, 200);
                                }

                                //返回首页
                                function returnbackaft(){
                                    setTimeout(function(){
                                        location.href = '/student/index.vpage';
                                    }, 200);
                                }

                                //跳转到天空竞技场排行榜页面
                                function forwardAfentiArenaRank(){
                                    window.open('/student/afenti/arena/afentiarenarank.vpage');
                                }

                                //换教材按钮
                                function changeBook(){
                                    $.get('/student/book/afenti.vpage?subject=MATH', function(data){
                                        $("#afentiChangeBookBox").html(data);
                                    });
                                }

                                //根据阿分题-flash的高度 调整页面的高度
                                function updateHeight(height){
                                    $("#movie").closest('dd').css({ height: height + "px" });
                                    $("#movie").find('object').css({ height: height + "px" });
                                }

                                $(function(){
                                    var AfentiSchool = 'AfentiSchoolMath';
                                    //设置建议反馈类型
                                    feedBackInner.homeworkType = "AfentiExercise";
                                    feedBackInner.practiceName = AfentiSchool;

                                    //flash参数
                                    p = ${flashVars!'[]'};
                                    p.autoShowFlag = $17.getQuery("autoShowFlag");
                                    p.AFTGameUrl = '<@flash.plugin name="AFTGameMath"/>';
                                    p.flashURL = '<@flash.plugin name="${file!''}"/>';
                                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                    p.imgDomain = '<@app.link_shared href=""/>';
                                    p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                    p.flashId = AfentiSchool;


                                    /** 加载Flash */
                                    $('#movie').getFlash({
                                        id       : AfentiSchool,
                                        width    : '900',
                                        height   : '600',
                                        movie    : '<@flash.plugin name="Preloader"/>',
                                        flashvars: p
                                    });

                                    //调用afenti_facade.js 传flash的object对象 返回div
                                    var tar = afenti.initialize($("#AfentiSchoolMath")[0]);
                                    $("#afenti_facade_box").html(tar);

                                });
                            </script>
                        <#--end//-->
                    </div>
                </dd>
            </dl>
        </div>

    </div>
</div>
<div id="afentiChangeBookBox"><#--换教材--></div>
</@temp.page>
