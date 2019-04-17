/**
 * Created by pengmin.chen on 2017/11/16.
 */
define(["jquery","knockout","YQ","knockout-switch-case","impromptu"],function($,ko,YQ){
    var locaPathName = window.location.pathname;

    // 左侧tab切换（使用click而不用href的原因：由于改版的需要，切换一级tab的时候必须刷新页面 且 如果点击的是当前tab 不可刷新，收起二级列表）
    // 点击班级管理
    $('.JS-clazzManageBox').on('click', function (event) {
        var $thisNode = $(event.currentTarget);
        window.location.href = '/specialteacher/clazz/index.vpage';
        // if (locaPathName === '/specialteacher/clazz/index.vpage') { // 点击的是当前tab
        //     return false;
        // } else {
        //     window.location.href = '/specialteacher/clazz/index.vpage';
        // }
    });

    // 点击老师学生管理
    $('.JS-teacStuManageBox').on('click', function (event) {
        var $thisNode = $(event.currentTarget);
        if (locaPathName === '/specialteacher/admin/index.vpage') { // 点击的是当前tab
            // if ($thisNode.hasClass('tab-active')) {
            //     $thisNode.removeClass('tab-active bg-active color-active');
            //     $('.JS-teacStuManageContent').hide();
            // } else {
            //     $thisNode.addClass('tab-active bg-active color-active');
                $thisNode.addClass('bg-active color-active');
                $('.JS-teacStuManageContent').show();
            // }
        } else {
            window.location.href = '/specialteacher/admin/index.vpage';
        }
    });

    // 点击个人中心
    $('.JS-personCenterBox').on('click', function (event) {
        var $thisNode = $(event.currentTarget);
        if (locaPathName === '/specialteacher/center/index.vpage') { // 点击的是当前tab
            // if ($thisNode.hasClass('tab-active')) {
            //     $thisNode.removeClass('tab-active bg-active color-active');
            //     $('.JS-personCenterContent').find('.JS-secTabContent').removeClass("color-active");
            //     $('.JS-personCenterContent').hide();
            // } else {
            //     $thisNode.addClass('tab-active bg-active color-active');
                $thisNode.addClass('bg-active color-active');
                $('.JS-personCenterContent').show();
            // }
        } else {
            window.location.href = '/specialteacher/center/index.vpage';
        }
    });
});