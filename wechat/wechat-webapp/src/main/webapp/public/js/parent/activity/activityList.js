define(['jquery','logger',"$17"],function($,logger,$17) {
    var activityListBox = $('#activityListBox');
    var visibleActivityLength = activityListBox.find('div.t-hot-active-meg-box').length;
    if(visibleActivityLength == 0){
        activityListBox.html('<div class="main body_background" style="height: 30%;"><h1 class="logo"></h1></div><div style="text-align: center; margin-top: 50px;">暂无热门活动</div>');
        logger.log({
            module: 'activity',
            op: 'no_activity_pv_index'
        });

    }
    logger.log({
        module: 'activity',
        op: 'activity_pv_index'
    });

    $(document).on("click",".js-openclassBannerBtn",function(){
        var sid = this.dataset.href;
        setTimeout(function(){
            location.href = "/parent/trustee/present.vpage?shopId="+sid+"&utm_source=weixinbanner&utm_medium=weixinbanner1&utm_campaign=o2oyiqigongkaikeDec";
        },1000);
        $17.tongjiTrustee("活动banner列表_"+this.dataset.href,"一起公开课");
        logger.log({
            module: 'activity',
            op: 'open_class_banner_btn_click'
        });
    });

    $(document).on("click",".js-trusteeBannerBtn",function(){
        var sid = this.dataset.href;
        setTimeout(function(){
            location.href = "/parent/trustee/present.vpage?shopId="+sid+"&utm_source=weixinbanner&utm_medium=weixinbanner&utm_campaign=o2otuoguanbanshiyanDec";
        },1000);
        $17.tongjiTrustee("活动banner列表_"+this.dataset.href,"托管");
        logger.log({
            module: 'activity',
            op: 'trustee_banner_btn_click'
        });
    });
});