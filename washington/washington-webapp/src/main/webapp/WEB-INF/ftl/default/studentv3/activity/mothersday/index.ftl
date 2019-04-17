<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="感恩母亲节" header="hide">
    <@sugar.capsule js=["jquery.flashswf"]/>
<style>
    body {
        background-color: #fdf0d6;
    }

    .main .head, .head .inner {
        background: url(/public/skin/studentv3/images/mothers-day.jpg) no-repeat center 0;
        width: 100%;
        height: 802px;
    }

    .main .head, .head .inner {
        position: relative;
    }

    .main .head .inner .mother-day-logo {
        width: 176px;
        height: 76px;
        position: absolute;
        display: block;
        left: 0;
        top: 0;
    }

    .main .head .inner .mother-day-box {
        position: absolute;
        bottom: 20px;
        right: 30px;
        width: 440px;
        height: 297px;
    }

    .main .head .inner .btn {
        padding: 0 0 20px 68px;
    }

    .main .head .inner .btn a {
        width: 276px;
        height: 60px;
        color: #fff;
        font-size: 26px;
        text-align: center;
        line-height: 60px;
        border-radius: 4px;
        display: inline-block;
        border-bottom: 2px solid #df3026;
    }

    .main .head .inner .btn a.make-card {
        background-color: #ff6152;
    }

    .main .head .inner .btn a.make-card:hover {
        background-color: #ff7552;
    }

    .main .head .inner .font p {
        font-size: 16px;
        line-height: 24px;
        color: #fff;
        padding: 2px 0;
    }

    body .main .inner {
        width: 1000px;
        margin: 0 auto;
        overflow: hidden;
        *zoom: 1;
    }

    .special-footer, .special-footer .m-inner {
        background-color: #977890;
        border: 0;
        height: 118px;
    }

    .special-footer .link {
        display: none;
    }

    .special-footer .m-foot-link {
        margin-top: 0;
    }

    .special-footer .copyright, .special-footer .m-foot-link .m-code .c-title, .special-footer .m-foot-link a, .special-footer .m-foot-link a:hover, .special-footer .m-service .c-title {
        color: #fff;
    }
</style>
<div class="main">
    <div class="head">
        <div class="inner">
            <a class="mother-day-logo" href="/"></a>

            <div class="mother-day-box">
                <div class="btn">
                    <a id="makeCardBut" class="make-card" href="javascript:void (0);">制作卡片</a>
                </div>
                <div class="font">
                    <p>活动时间：2015年5月5日至2015年5月15日。</p>

                    <p>制作奖励：制作母亲节贺卡并发送，获得5学豆。</p>

                    <p>分享奖励：家长通过微信将贺卡分享给同班家长们，获得5学豆。</p>

                    <p>奖励规则：制作和分享贺卡，学豆奖励都只能获得一次。</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/html" id="flashTemplateBox">
    <div id="flashBox"></div>

</script>

<script type="text/javascript">
    function closeCallback(){
        $.prompt.close();
        location.reload();
    }

    $(function(){
        $('#makeCardBut').on('click', function(){
            $.prompt(template('flashTemplateBox', {}), {
                title   : "感恩母亲节",
                buttons : {},
                position: { width: 960 },
                close   : function(){
                    location.reload();
                },
                loaded  : function(){
                    var mothersDay = 'mothersDay';
                    var p = ${flashVars!'{}'};

                    p.domain = '${requestContext.getWebAppFullUrl('/')}';
                    p.imgDomain = '<@app.link_shared href=""/>';
                    p.flashId = mothersDay;
                    p.close = 'closeCallback';
                    p.voiceEngineType = 'ChiVox';
                    p.speechEnabled = true;

                    $('#flashBox').getFlash({
                        id       : mothersDay,
                        width    : '900',
                        height   : '600',
                        movie    : '<@flash.plugin name="homeworkloader"/>',
                        flashvars: p
                    });
                }
            });

            //
            $17.tongji('感恩母亲节', '制作贺卡');
        });
    });
</script>
</@temp.page>