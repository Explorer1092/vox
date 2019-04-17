<#import "../layout.ftl" as layout>
<@layout.page title="薯条英语">

<div id="container">
    <div id="title" style="text-align: center;font-size: 1.2rem;margin: 1rem;"></div>
    <div id="article"></div>
</div>

<script src="/public/js/utils/weixin-1.4.0.js"></script>
<script src="/public/lib/jquery/dist/jquery.min.js"></script>
<script type="text/javascript">
    var getParams = function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }
    $.get('/chipsv2/center/article_detail.vpage?articleId=' + getParams('articleId'), function(res) {
        
        if(res.success) {
            $('#title').html(res.article.title);
            $('#article').html(res.article.content);

            window.cc = res.article.content;
            wx.config({
                debug: false,
                appId: '${appid!""}',
                timestamp:'${timestamp!""}',
                nonceStr: '${nonceStr!""}',
                signature: '${signature!""}',
                jsApiList: [
                    'updateAppMessageShareData',
                    'updateTimelineShareData',
                    'checkJsApi',
                    'onMenuShareTimeline',
                    'onMenuShareQQ',
                    'onMenuShareAppMessage'
                ]
            });


            wx.ready(function () {   //需在用户可能点击分享按钮前就先调用
                var title = res.article.title;
                var link = location.href;
                var desc = $('#article').text().trim().slice(0, 30) + '...';
                
                var imgUrl = res.article.shareIcon || 'http://cdn.17zuoye.com/fs-resource/5c41527c498ca4ef1dd656e1.png';

                wx.updateAppMessageShareData({ 
                    title: title, // 分享标题
                    link: link,
                    desc: desc, // 分享描述
                    imgUrl: imgUrl, // 分享图标
                    success: function () {},
                    fail: function(err) {
                        console.log(err)
                    }
                });
                wx.updateTimelineShareData({ 
                    title: title, // 分享标题
                    link: link,
                    imgUrl: imgUrl, // 分享图标
                    success: function () {},
                    fail: function(err) {
                        console.log(err)
                    }
                });
                wx.onMenuShareAppMessage({ 
                    title: title, // 分享标题
                    link: link,
                    desc: desc, // 分享描述
                    imgUrl: imgUrl, // 分享图标
                    success: function () {},
                    fail: function(err) {
                        console.log(err)
                    }
                });
                wx.onMenuShareTimeline({
                    title: title, // 分享标题
                    link: link,
                    imgUrl: imgUrl, // 分享图标
                    success: function () {},
                    fail: function(err) {
                        console.log(err)
                    }
                });
                wx.onMenuShareQQ({
                    title: title, // 分享标题
                    link: link,
                    imgUrl: imgUrl, // 分享图标
                    success: function () {},
                    fail: function(err) {
                        console.log(err)
                    }
                });
            });
        }
    });
</script>

</@layout.page>