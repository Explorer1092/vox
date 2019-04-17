<#import '../../layout/layoutblank.ftl' as temp>
<@temp.page pageName='assignments'>
    <@sugar.capsule js=["jquery.flashswf"] />

    <#assign PageBalckList = false/>
    <#if (currentStudentDetail.inPaymentBlackListRegion)!false>
        <#assign PageBalckList = true/>
    </#if>
<div class="t-app-container">
    <div class="t-app-inner">
        <div class="ta-head">
            <h1>沃克单词冒险</h1>

            <div class="btn-area" id="openPay">
            <#--开通-->
            <#if !PageBalckList>
                <a href="/apps/afenti/order/walker-cart.vpage?type=1" target="_blank" class="w-btn w-btn-green">续费</a>
            </#if>
            </div>
            <div class="ctn-area">
                <div class="ta-info">
                <#--start-->
                    <div class="v-textScrollInfo">
                    ${pageBlockContentGenerator.getPageBlockContentHtml('StudentIndex', 'nekketsuBanner')}
                    </div>
                    <script type="text/javascript">
                        $(function(){
                            $(".v-textScrollInfo").textScroll({
                                line : 1,
                                speed: 1000,
                                timer: 3000
                            });

                            $.get("/student/fairyland/userappinfo.vpage?appKey=${app_key!''}", function(data){
                                if(data.success && data.appInfo && data.appInfo.appStatus == 0){
                                    $("#openPay a").html("开通");
                                }

                                /*start popup*/
                                if(!$17.getCookieWithDefault("advPage") && data.success && data.appInfo){
                                    $17.setCookieOneDay("advPage", "1", 1);
                                    if(data.appInfo.appStatus == 0 || data.appInfo.appStatus == 3 || data.appInfo.dayToExpire < 8){
                                        $.prompt(template("T:homework-wokeCase-box", {}), {
                                            prefix : "null-popup",
                                            buttons: {},
                                            classes: {
                                                fade : 'jqifade',
                                                close: 'w-hide'
                                            }
                                        });
                                    }
                                }
                                /*popup end*/
                            });
                        });
                    </script>
                <#--end-->
                </div>
            </div>
        </div>

        <div class="ta-content">
            <dl class="ta-game-box">
                <#--<@apps.appsList appName='沃克单词冒险'/>-->
                <dd style="margin-left: 100px;">
                    <div class="game-container-box">
                        <!--游戏区域-->
                        <div style="width:900px; margin:0 auto;" class="spacing_vox_tb">
                            <div id="movie">
                                <div id="install_flash_player_box" style="margin:20px;">
                                        <span id="install_download_tip"
                                              style="font:16px/1.125 '微软雅黑', 'Microsoft YaHei', Arial, '黑体'; color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;">
                                            您未安装Flash Player插件，请 <a
                                                href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                                            <br/><br/>
                                            <span>
                                                如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                                            </span>
                                        </span>
                                </div>
                            </div>

                            <#include "../../../flash/prepareflashloadercdntypes.ftl"/>

                            <script type="text/javascript">

                                //换教材按钮
                                function changeBook(){
                                    $.get('/student/nekketsu/adventure/adventure.vpage', function(data){
                                        $("#nekketsuChangeBookBox").html(data);
                                    });
                                }

                                $(function(){
                                    var adventure = 'adventure';

                                    //flash参数
                                    p = {};

                                    p.flashUrl = '<@app.link href="/resources/apps/flashv1/nekketsu/Nekketsu.swf" />';
                                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                    p.imgDomain = '<@app.link_shared href=""/>';
                                    p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                    p.flashId = adventure;

                                    /** 加载Flash */
                                    $('#movie').getFlash({
                                        id       : adventure,
                                        width    : '900',
                                        height   : '600',
                                        movie    : '<@flash.plugin name="Preloader"/>',
                                        flashvars: p
                                    });

                                    //反馈
                                    feedBackInner.homeworkType = "Walker";
                                });
                            </script>
                        </div>
                    </div>
                </dd>
            </dl>
        </div>
    </div>
</div>
<div id="nekketsuChangeBookBox"><#--换教材--></div>
<script type="text/html" id="T:homework-wokeCase-box">
    <div class="homework-picaroCase-box homework-wokeCase-box">
        <a class="close" href="javascript:$.prompt.close();"></a>
    <#--<a class="btn-renewfree" href="javascript:void (0);"></a>-->
        <div class="hp-btn">
            <p>30天奖励200个学豆</p>

            <p>90天奖励500个学豆</p>

            <p>180天奖励1000个学豆</p>

            <p>365天奖励1500个学豆</p>
            <a class="btn-renew" href="/apps/afenti/order/walker-cart.vpage" target="_blank"
               onclick="$.prompt.close();"></a>
        </div>
    </div>
</script>
</@temp.page>
