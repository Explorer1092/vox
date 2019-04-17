<#import '../layout/layout.ftl' as temp>
<@temp.page pageName='assignments'>
    <@sugar.capsule js=["jquery.flashswf"] />
<div class="t-app-container">
    <div class="t-app-inner">
        <div class="ta-head" style="margin-right: 0;">
            <h1>假期奖励任务</h1>
        </div>
        <div class="ta-content">
            <dl class="ta-game-box" style="padding: 0 40px 0 0; float: right;">
                <dd>
                    <div class="game-container-box">
                        <div style="width:900px; margin:0 auto;" class="spacing_vox_tb">
                            <div id="winterMission">
                                        <span>
                                            您未安装Flash Player插件，请 <a
                                                href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">［点击这里］</a> 下载并安装。
                                            <br/><br/>
                                            <span>
                                                如果已经是最新版，<a href="http://get.adobe.com/flashplayer" target="_top">请允许加载flash</a>
                                            </span>
                                        </span>
                            </div>
                        </div>

                        <script type="text/javascript">
                            //根据寒假奖励任务-flash的高度 调整页面的高度
                            function updateHeight(height){
                                $("#winterMission").closest('dd').css({ height: height + "px" });
                                $("#winterMission").find('object').css({ height: height + "px" });
                            }

                            //返回"寒假奖励任务"页
                            function exitExercise(){
                                setTimeout(function(){
                                    location.href = '/student/wintermission/index.vpage';
                                }, 200);
                            }

                            $(function(){
                                var CommonExercise = 'CommonExercise';
                                //设置建议反馈类型
                                //feedBackInner.homeworkType = "CommonExercise";
                                //feedBackInner.practiceName = CommonExercise;

                                //flash参数
                                p = {};

                                p.flashUrl = '<@app.link href="/resources/apps/flash/commonExercise/CommonExercise.swf" />';
                                p.domain = '${requestContext.getWebAppFullUrl('/')}';
                                p.imgDomain = '<@app.link_shared href="" />';
                                p.debug = '${(!ProductDevelopment.isProductionEnv())?string}'; //判断当前是否线上
                                p.flashId = CommonExercise;

                                /** 加载Flash */
                                $('#winterMission').getFlash({
                                    id       : CommonExercise,
                                    width    : '900',
                                    height   : '600',
                                    movie    : '<@flash.plugin name="Preloader"/>',
                                    flashvars: p
                                });
                            });
                        </script>
                    </div>
        </div>
        </dd>
        </dl>
    </div>
</div>
</div>
</@temp.page>
