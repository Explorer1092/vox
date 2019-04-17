<#import "../serviceV2Module.ftl" as com>
<@com.page title="大事记">
<style>
    /* module */
    .news-module-1 { background-color: #f8f8f8;}

    .news-module-1 .big-tab { padding: 97px 275px 0; }
    .news-module-1 .big-tab div { float: left; width: 50%; text-align: center; color: #fff; font-size: 16px; line-height: 35px; background-color: #cbcbcb; cursor: pointer; }
    .news-module-1 .big-tab a { display: block; color: #fff; }
    .news-module-1 .big-tab a:hover { text-decoration: none; }
    .news-module-1 .big-tab .active { background-color: #1eaede; }

    /* global-hd */
    .global-hd { padding: 0 152px; }
    .global-hd .inner { height: 29px; border-bottom: 1px #0390c0 solid; text-align: center; }
    .global-hd .inner span { position: relative; zoom: 1; display: inline-block; margin: 0 auto; padding: 0 65px; color: #0390c0; font-size: 36px; line-height: 54px; background-color: #fff; }
    .global-hd .inner .ico-1,.global-hd .inner .ico-2 { position: absolute; top: 15px; color: #0390c0; font: 38px/24px "Microsoft Yahei","\5FAE\8F6F\96C5\9ED1"; }
    .global-hd .inner .ico-1 { left: -2px; }
    .global-hd .inner .ico-2 { right: -2px; }

    .news-module-1 .global-hd { padding-top: 45px; }
    .news-module-2 .global-hd { padding-top: 60px; }
    .news-module-2 .global-hd .inner span { background-color: #f4f4f4; }

    .news-module-1 .bg{height:2341px;width:100%; background: url(<@app.link href="public/skin/default/images/serviceV2/jobs/event/event-module-bg1.jpg"/>) no-repeat center 0;}

</style>
<div class="news-module-1">
    <div class="wrapper">
        <div class="big-tab clearfix">
            <div><a href="../news/index.vpage">新闻报道</a></div>
            <div class="active"><a href="../news/event.vpage">大事记</a></div>
        </div>
    </div>
    <div class="bg">

    </div>
</div>

</@com.page>