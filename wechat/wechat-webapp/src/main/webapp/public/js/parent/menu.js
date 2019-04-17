define(['jquery'], function ($) {
    $(function () {
        $('.dropdownlink').click(function (event) {
            event.stopPropagation();
            var item = $(this);
            if(item.closest('li').find('.dropdownmenuitem').hasClass('opened')){
                $(this).closest('li').find('.dropdownmenuitem').css({'z-index':-1});
            }else{
                $(this).closest('li').find('.dropdownmenuitem').css({'z-index':"auto"});
            }
            //把原来展开的收起
            $('.dropdownlink').not(item).each(function () {
                $(this).closest('li').find('.dropdownmenuitem').css({'z-index':-1}).animate({top: 0}, 100).removeClass('opened');
            });

            var menu = item.closest('li').find('.dropdownmenuitem');
            var newTop = (menu.hasClass('opened')) ? 0 : -menu.height() - 14;
            menu.animate({top: newTop}, 100).toggleClass('opened');
        });

        //点击页面，收起全部菜单
        $('body').click(function () {
            $('.dropdownmenuitem').each(function () {
                $(this).css({'z-index':-1}).animate({top: 0}, 100).removeClass('opened');
            });
        });
    });
});