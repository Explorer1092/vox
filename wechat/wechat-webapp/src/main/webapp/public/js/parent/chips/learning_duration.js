define(["jquery", "logger"],function($, logger){
    var app_inviter = getParams('app_inviter');

    // m_XzBS7Wlh  选择英语年限页面被加载 load_choosing_english_years     分享人userid
    logger.log({
        module: 'm_XzBS7Wlh',
        op: 'load_choosing_english_years',
        s0: '',
        s1: inviter || app_inviter
    });



    var year = null;
    $('.option').on('click', function(){
        $('.option').removeClass('selected');
        $(this).addClass('selected');
        year = $(this).data('choice');

        // m_XzBS7Wlh  学习一年以下被点击   click_less_than_one_year        分享人userid
        // m_XzBS7Wlh  学习一年以上被点击   click_more_than_one_year        分享人userid
        logger.log({
            module: 'm_XzBS7Wlh',
            op: year === 'MORE' ? 'click_more_than_one_year' : 'click_less_than_one_year',
            s0: '',
            s1: inviter || app_inviter
        });
    });

    

    $('.btn').on('click', function(){
        var $selected = $('.option.selected');
        if($selected.length > 0) {
            var year = $selected.data('choice');
            if(year === 'LESS') {
                location.href = '/chips/be/short/introduction.vpage?primary=true&refer='+ refer + "&channel=" + channel + "&type=" + type + "&inviter=" + inviter + "&app_inviter=" + app_inviter;
            }else {
                location.href = '/chips/be/short/introduction.vpage?primary=false&refer='+ refer + "&channel=" + channel + "&type=" + type + "&inviter=" + inviter + "&app_inviter=" + app_inviter;
            }
        }else {
            alert('请选择学习英语的年限');
        }
    });
});

function getParams(name){
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURI(r[2]); return null;
}