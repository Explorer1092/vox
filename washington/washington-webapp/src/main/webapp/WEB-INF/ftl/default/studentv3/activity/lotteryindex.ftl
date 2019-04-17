<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page header="hide">
    <@sugar.capsule js=["jquery.flashswf"] css=["project.lottery"] />
    <!--//start-->
    <div class="header">
        <div class="inner">
            <a href="/" class="logo" target="_blank"></a>
        </div>
    </div>
    <div class="main">
        <div class="drop_eggs_box">
            <div class="fleft">
                <p class="f16">9月1号-12月</p>
                <p class="f16 first_p">1、每天做完一次作业或测验即可获得一次砸蛋机会</p>
                <p class="f16">2、每次砸蛋消耗1学豆，砸蛋机会当天使用， 每天24：00清除当天砸蛋次数</p>
                <p class="f16">3、每次砸蛋有机会获得1学豆、2学豆、3学豆、5学豆、10学豆、50学豆、100学豆的奖励，中奖几率100%</p>
                <p class="black">本活动最终解释权归一起作业（www.17zuoye.com）所有。</p>
            </div>
            <a href="javascript:void(0);"  <#--id="startLotteryButton"--> class="fright" title="开始砸蛋" style="background: none; cursor: default;">
                <span style="position: relative; top: 89px; left: -2px; color: #fff; font-size: 25px;">活动已结束</span>
            </a>
        </div>
        <div class="share_box">
            <div class="jiaThisShare">
                <div class="jiathis jInner">
                    <!-- JiaThis Button BEGIN -->
                    <div class="jiathis_style">
                        <span class="jiathis_txt">分享到：</span>
                        <a class="jiathis_button_qzone" title="QQ空间">QQ空间</a>
                        <a class="jiathis_button_tsina" title="新浪微博">新浪微博</a>
                        <a class="jiathis_button_tqq" title="腾讯微博">腾讯微博</a>
                        <a class="jiathis_button_renren" title="人人网">人人网</a>
                        <a class="jiathis_button_kaixin001" title="开心网">开心网</a>
                        <a class="jiathis_button_douban" title="豆瓣">豆瓣</a>
                        <a href="http://www.jiathis.com/share?uid=1613716" class="jiathis jiathis_txt jiathis_separator jtico jtico_jiathis" target="_blank" title="更多"></a>
                    </div>
                    <script type="text/javascript" >
                        var jiathis_config={
                            data_track_clickback:true,
                            title: "#【开学砸金蛋 学豆天天拿】#",
                            summary:"全国700万位小学生一起来@一起作业网 砸蛋狂欢！我们特意为小朋友们准备了大量学豆奖励哦！快快告诉你的小伙伴儿一起来参加吧！",
//                            pic:"//cdn.17zuoye.com/static/project/student/lotteryShareBanner.jpg",
                            shortUrl:false,
                            hideMore:false
                        }
                    </script>
                    <script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8"></script>
                    <!-- JiaThis Button END -->
                </div>
            </div>
        </div>
    </div>

    <!--end//-->

    <script type="text/javascript">
        $(function(){
            var count = 0;

            //点击开始砸蛋
            /*$("#startLotteryButton").on("click", function(){
                $17.tongji("61砸蛋-活动页");
                $17.traceLog({
                    module  : 'Lottery',
                    op      : 'click'
                });

                //获取砸收次数
                $.post("initsmasheggparam.vpage", {}, function(data){
                    if(data.success){
                        $.prompt("<div id='movie'><div class='loading_vox'></div></div>", {
                            title : "",
                            prefix: 'myPrompt',
                            position: { width: "700" },
                            buttons: {},
                            classes : {
                                close: 'dis_none',
                                fade: 'jqifade'
                            },
                            loaded : function(){
                                count = data.count;

                                *//** 加载Flash *//*
                                $('#movie').getFlash({
                                    width:'700',
                                    height:'470',
                                    movie: '<@flash.plugin name="Zadan"/>',
                                    wmode : "transparent",
                                    flashvars : {
                                        "domain" : "${requestContext.webAppBaseUrl}/",
                                        "count" : count
                                    }
                                });
                            }
                        });

                    }else{
                        $17.alert(data.info);
                    }
                });
                return false;
            });*/
        });

        //flash close box
        function alertClose(){
            $.prompt.close();
        }
    </script>
</@temp.page>