/*
* create by chunbao.cai on 2018-5-4
* 薯条英语公众号
* -- 广告
*
* */
define(["jquery", "../../../public/lib/weixin/jweixin-1.0.0.js","logger", "../../../public/lib/swiper/js/swiper.js"],function($,wx,logger){

    function getParams(name){
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURI(r[2]); return null;
    }
    var url = '/chips/short/product.vpage';
    var duration = '';
    var inviter = getParams("inviter") || 'noinfo';
    var app_inviter = getParams("app_inviter") || 'noinfo';
    if(getParams('primary') === false) {     // more 一年以上
        url += '?primary=false&type=' + getParams("type");
        duration = 'more';
    }else {
        url += '?primary=true&type=' + getParams("type");
        duration = 'less';
    }

    // m_XzBS7Wlh  广告页被加载  load_invitation_adpage   一年以下（less） 一年以上（more） 分享人userid
    logger.log({
        module: 'm_XzBS7Wlh',
        op: 'load_invitation_adpage',
        s0: duration,
        s1: inviter || app_inviter
    });

    $.get(url, function(res) {
        $('#sellOutDate').text(res.sellOutDate);
        $('#beginDate').text(res.beginDate);
        $('#originalPrice').text(res.originalPrice);
        $('#price').text(res.price);
        if(res.sellOut) {
            $('#buy').css({'background': 'gray', 'box-shadow': 'none'}).html('<span class="sellOut">售罄</span>');
        }else {
            $('#buy').click(function() {
                logger.log({
                    module: 'm_XzBS7Wlh',
                    op: 'click_invitation_adpage_purchasebutton',
                    s0: duration,
                    s1: inviter || app_inviter
                });
                window.location.href = "/chips/order/create.vpage?productId="+ res.productId + "&inviter=" + inviter + "&refer=" + getParams("refer") + "&channel=" + getParams("channel") + "&duration=" + duration + '&app_inviter=' + app_inviter;
            });
        }
        $('#hideList').hide()
        $('#arrowBox').click(function(){
            if($(".arrowUp").length<=0){
                $('html,body').animate({scrollTop:$('#evaluate-Box').offset().top},500);
                $('#hideList').slideDown(500);
                $('.arrowIcon').addClass('arrowUp');
            }else if($(".arrowUp").length=1){
                $('html,body').animate({scrollTop:$('#evaluate-Box').offset().top},500);
                $('#hideList').slideUp(200);
                $('.arrowIcon').removeClass('arrowUp');
            }
        })
    });
    $('#trial-btn').click(function(){
        location.href='/chips/center/studylist.vpage?back=' + encodeURIComponent(location.pathname+location.search);
    });
    var mySwiper = new Swiper('.swiper-container', {
        autoplay: 3000,
        initialSlide: 0,
        loop: true,
        centeredSlides: true,
        spaceBetween: '6%',
        slidesPerView: 2,
        pagination: '.swiper-pagination',
        effect: 'coverflow',
        coverflow: {
            rotate: 0,
            stretch: 10,
            depth: 200,
            modifier: 2,
            slideShadows : true
        }
    });


});