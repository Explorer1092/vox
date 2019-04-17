<style type="text/css">
    /* reset */
    /*body,h1,h2,h3,h4,h5,h6,dl,dt,dd,ul,ol,li,th,td,p,blockquote,pre,form,fieldset,legend,input,button,textarea,hr { margin: 0; padding: 0; }
    body { color: #333; font: 12px/1.5 Arial,"Microsoft Yahei","\5FAE\8F6F\96C5\9ED1"; }
    select,input,button { vertical-align: middle; font-size: 100%; }
    ul,ol,li { list-style: none; }
    fieldset,img { border: 0; }
    em { font-style: normal; }*/
    .clearfix:after { content:"."; display: block; visibility: hidden; clear: both; height: 0; font-size: 0; }
    .clearfix { *zoom: 1; }
    .wrapper { margin: 0 auto; width: 1000px; }

    /* module */
    .home-module-1 { height: 668px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-bg1.png"/>)  no-repeat 50% 100%;}
    .home-module-2 { height: 670px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-bg2.png"/>)  no-repeat 50% 100%; #c1efd9}
    .home-module-3 { height: 670px; background: #fff4d3; }
    .home-module-4 { height: 670px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-bg3.png"/>) no-repeat 50% 100%; }
    .home-module-5 { height: 670px; background: #c2ecff; }
    .home-module-6 { height: 670px; background: #fee; }
    .home-module-7 { height: 670px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-bg4.png"/>) no-repeat 50% 0; }
    .home-module-7 .box { margin: 34px 0 0; height: 380px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-icon5.png"/>) no-repeat 50% 0;}
    .home-module-8 { height: 700px; background: #f2f2f2; }

    .home-module-1 .wrapper,.home-module-7 .wrapper,.home-module-8 .wrapper { padding: 82px 0 0; }

    .home-module-8 .expert { padding: 60px 0 0; }
    .home-module-8 .expert .pic { float: left; _display: inline; margin: 12px 23px 0 7px; width: 135px; height: 124px; }
    .home-module-8 .expert img { display: block; }
    .home-module-8 .expert .hd { color: #454545; font-size: 18px; line-height: 40px; }
    .home-module-8 .expert p { color: #454545; font-size: 14px; line-height: 24px; }
    .home-module-8 .list { position: relative; padding: 54px 0 0; }
    .home-module-8 .list .box { float: left; _display: inline; margin: 0 8px; width: 184px; text-align: center;}
    .home-module-8 .list .pic { height: 201px;  vertical-align: middle; text-align: center; }
    .home-module-8 .list img { display: inline-block; margin: 0 auto; vertical-align: middle; }
    .home-module-8 .list .hd { padding: 10px 0 0; color: #353535; font-size: 14px; line-height: 38px; }

    /* home-fixed-nav */
    .home-fixed-nav { position: fixed; _position: absolute; z-index:10; top: 150px; left:20px; padding: 72px 0 0;height: 300px; width: 170px;background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-logo-gray.png"/>)  no-repeat 50% 15px; }
    .home-fixed-nav .nav { padding: 0 0 19px; text-align: center; }
    .home-fixed-nav .nav a { display: block; color: #fff; font-size: 14px; line-height: 24px; }
    .home-fixed-nav .nav a:hover { font-weight: 700; }
    .home-fixed-nav .btn { position: relative; height: 28px; overflow: hidden; }
    .home-fixed-nav .btn a { float: left; _display: inline;text-align: center; color: #fff; font-size: 14px; line-height: 28px;}
    .home-fixed-nav .btn a .btns{margin-left: 50px;}
    .home-fixed-nav .btn .regist { width: 58px; }

    /* home-fixed-dot */
    .home-fixed-dot { position: fixed; top: 207px; left: 50%; margin: 0 0 0 614px; width: 16px; _position:absolute;}
    .home-fixed-dot div { position: relative; height: 17px; overflow: hidden; text-align: center; color: #999; cursor: pointer; }
    .home-fixed-dot .active {background: #0e9cd7;border-radius:6px;width:8px;height: 8px;margin-left: 4px;*border-radius: 6px; }
    .home-fixed-dot .span{width: 6px;height: 6px;border-radius: 5px;border: solid 1px #139ed8;display: inline-block; margin-top: -5px;*border-radius: 5px;}

    /* global-hd */
    .global-hd { padding: 0 152px; }
    .global-hd .inner { height: 29px; border-bottom: 1px #0390c0 solid; text-align: center; }
    .global-hd .inner span { position: relative; zoom: 1; display: inline-block; margin: 0 auto; padding: 0 65px; color: #0390c0; font-size: 36px; line-height: 54px; background-color: #fff; }
    .global-hd .inner .ico-1,.global-hd .inner .ico-2 { position: absolute; top: 15px; color: #0390c0; font: 38px/24px "Microsoft Yahei","\5FAE\8F6F\96C5\9ED1"; }
    .global-hd .inner .ico-1 { left: -2px; }
    .global-hd .inner .ico-2 { right: -2px; }

    .home-module-8 .global-hd .inner span { background-color: #f2f2f2; }

    /* home-module-text */
    .home-module-text { text-align: center; }
    .home-module-text .hd { font-size: 36px; line-height: 54px; }
    .home-module-text p { padding: 10px 0 0; color: #000; font-size: 18px; line-height: 32px; }
    .home-module-1 .home-module-text { padding: 126px 40px 50px 382px;  background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-icon1.png"/>)  no-repeat 55px 98px;}
    .home-module-1 .home-module-text .hd { color: #0390c0; }
    .home-module-2 .home-module-text { padding: 213px 354px 0 124px; }
    .home-module-2 .home-module-text .hd { color: #009398; }
    .home-module-3 .home-module-text { padding: 250px 76px 25px 382px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-icon2.png"/>) no-repeat 0 178px; }
    .home-module-3 .home-module-text .hd { color: #c92b2b; }
    .home-module-4 .home-module-text { padding: 300px 355px 0 105px; }
    .home-module-4 .home-module-text .hd { color: #236baf; }
    .home-module-5 .home-module-text { padding: 260px 65px 282px 395px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-icon3.png"/>) no-repeat 0 100%; }
    .home-module-5 .home-module-text .hd { color: #0a92bf; }
    .home-module-6 .home-module-text { padding: 250px 370px 42px 107px; background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/home-module-icon4.png"/>) no-repeat 100% 100%; }
    .home-module-6 .home-module-text .hd { color: #d12c51; }

    /* footer */
    .m-foot-box { background-color: #1b92bf; }
    .m-foot-inner { margin: 0 auto; width: 1000px; }
    .m-foot-inner .foot-fl { float: left; }

    .m-footer,.m-header { font: 14px/100% "微软雅黑", "Microsoft YaHei",arial,"黑体"; }
    .m-header,.m-header .m-inner { height: 53px; background-color: #fff; }
    .m-header { position: relative; z-index: 99; border: 1px solid #eee; }
    .m-header .m-inner { margin: 0 auto; width: 1000px; }
    .m-header .logo { float: left; margin: 8px 0 0; width: 96px; height: 37px; background:  url(<@app.link href="public/skin/default/images/serviceV2/logo.png"/>) no-repeat; }
    .m-header .logo a { display: block; width: 100%; height: 100%; }
    .m-header .r-nav { float: right; padding: 18px 0 0; }
    .m-header .r-nav a { color: #667284; }
    .m-footer,.m-footer .m-inner { clear: both; height: 160px; }
    .m-footer { background-color: #1b92bf; }
    .m-footer .m-inner { margin: 0 auto; width: 1000px; overflow: hidden; }
    .m-footer .copyright { margin-top: 30px; color: #f2f2f2; font-size: 12px; line-height: 20px; }
    .m-footer .link { margin-top: 13px; }
    .m-footer .link .spare-icon { display: inline-block; margin-right: 20px; width: 46px; height: 46px; background: url(<@app.link href="public/skin/default/images/serviceV2/m-icon-share.png"/>) 500px 500px no-repeat; }
    .m-footer .link .spare-icon:hover { background-image: url(<@app.link href="public/skin/default/images/serviceV2/m-icon-share-blue.png"/>); }
    .m-footer .link .spare-weibo { background-position: 0 0; }
    .m-footer .link .spare-rr { background-position: -62px 0; }
    .m-footer .link .spare-qq { background-position: -124px 0; }
    .m-footer .link .spare-wx { background-position: -185px 0; }
    .m-footer .link .spare-qzone { background-position: -50px 0; }
    .m-footer .m-foot-link { float: right; margin-top: 20px; width: 306px; }
    .m-footer .m-foot-link h3 { padding-left: 10px; height: 25px; color: #000; font-size: 16px; line-height: 100%; }
    .m-footer .m-foot-link a,.m-footer .m-foot-link .tel { display: block; color: #fff; font-size: 14px; line-height: 26px; }
    .m-footer .m-foot-link .tel,.m-footer .m-foot-link .advice,.m-footer .m-foot-link .service { margin: 11px 0 0; padding: 0 0 0 24px; background:url(<@app.link href="public/skin/default/images/serviceV2/m-footer-icon-blue.png"/>)  no-repeat; }
    .m-footer .m-foot-link .advice,.m-footer .m-foot-link .service { float: left; }
    .m-footer .m-foot-link .tel { background-position: 0 0; }
    .m-footer .m-foot-link .advice { background-position: 0 -25px; margin-right: 18px; }
    .m-footer .m-foot-link .service { background-position: 0 -50px; }
    .m-footer .m-foot-link .m-left { padding: 10px 0 0; width: 198px; }
    .m-footer .m-foot-link .m-code { float: right; }
    .m-footer .m-foot-link .m-code .c-image { margin: 0 auto; width: 108px; height: 108px; background:  url(<@app.link href="public/skin/default/images/serviceV2/m-code.jpg"/>) no-repeat; }
    .m-footer .m-foot-link .m-code .c-title { padding: 6px 0 0; text-align: center; color: #fff; font-size: 14px; line-height: 26px; }
    .m-footer .m-left { float: left; }

    /**/
    .t-right-backTop{ position: fixed; right: 10px; bottom: 200px; _position: absolute; _display:none; z-index: 1; width: 60px; height: 55px; }
    .t-right-backTop .t-right-backTop-inner{width:60px;height:55px;background: url(<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/back.png"/>) no-repeat;  cursor: pointer; }
</style>
<div style="position: relative; z-index: 1;">
    <div class="home-fixed-nav home_list">
        <div class="nav">
            <a href="javascript:void(0);" class="v-schoolIdeaPage-next v-rightKey" data-type="1">教育理念</a>
            <a href="javascript:void(0);" class="v-schoolIdeaPage-idea v-rightKey" data-type="7">产品创新</a>
            <a href="javascript:void(0);" class="v-schoolIdeaPage-authority v-rightKey" data-type="8">权威认证</a>
            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/uservoice.vpage">用户声音</a>
            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/news/index.vpage">新闻中心</a>
            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/aboutus.vpage">关于我们</a>
            <a href="${(ProductConfig.getMainSiteBaseUrl())!''}/help/jobs.vpage">诚聘英才</a>

        </div>
        <div class="btn">
            <a href="javascript:void (0);" class="login v-backTop" style="margin-left:45px;margin-top: 5px;">登录</a>
            <a href="javascript:void (0);" class="regist v-backTop" style="margin-top: 5px;">注册</a>
        </div>
    </div>
    <div class="home-fixed-dot">
        <div class="v-schoolIdeaPage-next v-rightKey" data-type="1"><span class="span"></span></div>
        <div class="v-second v-rightKey" data-type="2"><span class="span"></span></div>
        <div class="v-third v-rightKey" data-type="3"><span class="span"></span></div>
        <div class="v-force v-rightKey" data-type="4"><span class="span"></span></div>
        <div class="v-five v-rightKey" data-type="5"><span class="span"></span></div>
        <div class="v-six v-rightKey" data-type="6"><span class="span"></span></div>
        <div class="v-schoolIdeaPage-idea v-rightKey" data-type="7"><span class="span"></span></div>
        <div class="v-schoolIdeaPage-authority v-rightKey" data-type="8"><span class="span"></span></div>
    </div>
    <div class="home-module-1">
        <div class="wrapper">
            <div class="global-hd">
                <div class="inner">
                    <span><em class="ico-1">·</em><em class="ico-2">·</em>教育理念</span>
                </div>
            </div>
            <div class="home-module-text">
                <div class="hd">个性化&nbsp;&nbsp;&nbsp;individualized</div>
                <p>一起作业能够迅速捕捉每个学生身上存在的个性化问题，通过数据挖掘，再结合国家的教学大纲，给出一个科学的路径，使学生能最高效地利用时间，创造好成绩。</p>
            </div>
        </div>
    </div>
    <div class="home-module-2">
        <div class="wrapper">
            <div class="home-module-text">
                <div class="hd">有趣&nbsp;&nbsp;&nbsp;interesting</div>
                <p>营造有趣的氛围，动画式的作业方式，让孩子们对学习充满兴趣，一起作业帮助老师寓教于乐，能够让孩子们在学中玩、在玩中学。</p>
            </div>
        </div>
    </div>
    <div class="home-module-3">
        <div class="wrapper">
            <div class="home-module-text">
                <div class="hd">亲密&nbsp;&nbsp;&nbsp;intimate</div>
                <p>在一起作业，学生们可以在虚拟的班级中，互相比赛，用优异的学习成绩互相鼓舞，与此同时也创造了亲密的氛围，让作业真正成为童年美好的记忆。</p>
            </div>
        </div>
    </div>
    <div class="home-module-4">
        <div class="wrapper">
            <div class="home-module-text">
                <div class="hd">即时&nbsp;&nbsp;&nbsp;immediate</div>
                <p>通过一起作业，家长们用手机就可以随时知道课堂上讲了什么，随时知道孩子的课堂表现、作业情况。</p>
            </div>
        </div>
    </div>
    <div class="home-module-5">
        <div class="wrapper">
            <div class="home-module-text">
                <div class="hd">互动&nbsp;&nbsp;&nbsp;interactive</div>
                <p>有了一起作业，老师和家长沟通孩子学习情况和学校表现的渠道变得方便，家校可以更好地互补配合。</p>
            </div>
        </div>
    </div>
    <div class="home-module-6">
        <div class="wrapper">
            <div class="home-module-text">
                <div class="hd">关爱&nbsp;&nbsp;&nbsp;I care</div>
                <p>开启一起作业电脑管家，自动为学生屏蔽不健康网站、网络游戏。即使家长不在身边陪同也可以放心。同时，登录一起作业后，系统每隔40分钟都会提醒孩子休息，避免长时间用眼，为视力健康保驾护航。</p>
            </div>
        </div>
    </div>
    <div class="home-module-7">
        <div class="wrapper">
            <div class="global-hd">
                <div class="inner">
                    <span><em class="ico-1">·</em><em class="ico-2">·</em>产品创新</span>
                </div>
            </div>
            <div class="box"></div>
        </div>
    </div>
    <div class="home-module-8">
        <div class="wrapper">
            <div class="global-hd">
                <div class="inner">
                    <span><em class="ico-1">·</em><em class="ico-2">·</em>权威认证</span>
                </div>
            </div>
            <div class="expert clearfix">
                <div class="pic"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/img-01.png"/>" alt="" width="135" height="124"></div>
                <div class="hd">龚亚夫&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;中国教育科学研究院研究员，中国教育学会外语教学专业委员会理事长</div>
                <p>一起作业的概念很好，现在老师对在线教育也是很感兴趣的。当年我带中学的时候，每天要改三个班的作业本。每个班50名同学也就是150本，150本作业什么概念？摞起来大概要一米厚。至少也得需要几个小时才可以批完。</p>
                <p>现在老师使用一起作业之后，不仅解决了每天几小时的作业批改时间，而且，老师可以在网上直接布置作业，学生也可以直接在网上完成。</p>
            </div>
            <div class="list">
                <div class="box">
                    <div class="pic"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/aprv-01.png"/>" alt=""></div>
                    <div class="hd">全国“十二五”课题认证</div>
                </div>
                <div class="box">
                    <div class="pic"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/aprv-02.png"/>" alt=""></div>
                    <div class="hd">国家科技部支持项目</div>
                </div>
                <div class="box">
                    <div class="pic"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/aprv-03.png"/>" alt=""></div>
                    <div class="hd">教育部重点课题平台</div>
                </div>
                <div class="box">
                    <div class="pic">
                        <div style="padding-top: 42px;"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/aprv-04.png"/>" alt=""></div>
                    </div>
                    <div class="hd">中央电化教育馆专家鉴定</div>
                </div>
                <div class="box">
                    <div class="pic">
                        <div style="padding-top: 30px;"><img src="<@app.link href="public/skin/default/images/serviceV2/schoolIdeaPage/aprv-05.png"/>"  alt=""></div>
                    </div>
                    <div class="hd">北京教育资源网指定教学平台</div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="m-footer">
    <div class="m-inner">
        <div class="m-left w-fl-left">
            <div class="copyright">
                ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
            </div>
            <div class="link">
                <a class="spare-icon spare-qzone" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
                <a class="spare-icon spare-weibo" href="http://user.qzone.qq.com/2484705684/main" target="_blank" title="QQ空间"></a>
            </div>
        </div>
        <div class="m-foot-link w-fl-right" >
            <#--<div class="m-code" style="width: 108px;">
                <p class="c-image"></p>
                <p class="c-title">关注我们</p>
            </div>-->
            <div class="m-left w-fl-left">
                <#--<span class="tel">咨询时间：9:00－21:00</span>-->
                <a href="javascript:void(0);" class="advice message_right_sidebar">反馈建议</a>
            </div>
        </div>
    </div>
</div>
<div class="t-right-backTop">
    <div class="t-right-backTop-inner v-backTop"></div>
</div>
<script type="text/javascript">
    $(function(){
        var togoFlag = 1;
        $(window).scroll(function(){
            var $thisTop = $(this).scrollTop();
            var $offsetTop1 = $(".home-module-1").offset().top;
            var $offsetTop2 = $(".home-module-2").offset().top;
            var $offsetTop3 = $(".home-module-3").offset().top;
            var $offsetTop4 = $(".home-module-4").offset().top;
            var $offsetTop5 = $(".home-module-5").offset().top;
            var $offsetTop6 = $(".home-module-6").offset().top;
            var $offsetTop7 = $(".home-module-7").offset().top;
            var $offsetTop8 = $(".home-module-8").offset().top;

            if($thisTop < 400){
                $(".t-right-backTop").hide();
            }else{
                $(".t-right-backTop").show();
            }
            
            if($thisTop > $offsetTop1 - 100 && $thisTop < $offsetTop1 + 300){
                if(togoFlag == 1){$17.tongji("新登录页-点击滚动下一屏-1");togoFlag++;}
                $(".v-rightKey[data-type='1']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop2 - 300 && $thisTop < $offsetTop2 + 300){
                if(togoFlag == 2){$17.tongji("新登录页-点击滚动下一屏-2");togoFlag++;}
                $(".v-rightKey[data-type='2']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop3 - 300 && $thisTop < $offsetTop3 + 300){
                if(togoFlag == 3){$17.tongji("新登录页-点击滚动下一屏-3");togoFlag++;}
                $(".v-rightKey[data-type='3']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop4 - 300 && $thisTop < $offsetTop4 + 300){
                if(togoFlag == 4){$17.tongji("新登录页-点击滚动下一屏-4");togoFlag++;}
                $(".v-rightKey[data-type='4']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop5 - 300 && $thisTop < $offsetTop5 + 300){
                if(togoFlag == 5){$17.tongji("新登录页-点击滚动下一屏-5");togoFlag++;}
                $(".v-rightKey[data-type='5']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop6 - 300 && $thisTop < $offsetTop6 + 300){
                if(togoFlag == 6){$17.tongji("新登录页-点击滚动下一屏-6");togoFlag++;}
                $(".v-rightKey[data-type='6']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop7 - 300 && $thisTop < $offsetTop7 + 300){
                if(togoFlag == 7){$17.tongji("新登录页-点击滚动下一屏-7");togoFlag++;}
                $(".v-rightKey[data-type='7']").addClass("active").siblings().removeClass("active");
            }

            if($thisTop > $offsetTop8 - 300){
                if(togoFlag == 8){$17.tongji("新登录页-点击滚动下一屏-8");togoFlag++;}
                $(".v-rightKey[data-type='8']").addClass("active").siblings().removeClass("active");
            }
        });

        $(document).on("click",".v-rightKey", function(){
            var $offsetTop = $(".home-module-"+$(this).attr("data-type")).offset().top;
            $("html, body").animate({scrollTop: $offsetTop}, 200);

            $17.tongji("新登录页-点击滚动下一屏-" + $(this).attr("data-type"));
        });

        if( !$17.isBlank($17.getQuery("dataType")) ) {
            $(".v-rightKey[data-type='" + $17.getQuery("dataType") + "']").click();
        }

        //backTop
        $(document).on("click",".v-backTop",function(){
            $("html, body").animate({scrollTop:0}, 200)
        });

    });


</script>