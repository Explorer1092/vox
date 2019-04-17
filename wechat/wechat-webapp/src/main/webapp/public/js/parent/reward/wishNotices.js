/*
 * 孩子愿望
 */
define(["jquery"], function ($) {
    $('.btn-jump').on('click',function(){
        var nid=$(this).data('nid');
        var url=$(this).data('url');
        $.post('cacheclicked.vpage',{nid:nid},function(){

        });
        setTimeout(function(){
            location.href=url;
        },300)
    });
});




