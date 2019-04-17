define(['jquery','voxLogs'], function ($) {

    var moduleName = "m_Wppv7y2u";

    $(document).on("click", ".age-tab li", function(){
        var $this = $(this),
            s0Name = "";
        $this.addClass('active').siblings().removeClass('active');
        $(".ah-main").children().eq($this.index()).show().siblings().hide();

        if($this.index() == 0){
            s0Name = "外语";
        }else if($this.index() == 1){
            s0Name = "才艺";
        }else if($this.index()== 2){
            s0Name = "玩乐";
        }

        YQ.voxLogs({
            database: 'parent',
            module: moduleName,
            op : "o_0KNNpGZf",
            s0 : s0Name
        });

    });

    //商品详情
    $(document).on("click",".js-shopItem",function(){
        var $this = $(this),
            index = $this.data("index"),
            sid = $this.data("sid"),
            s1Name = "";

        if(index == 0){
            s1Name = "外语";
        }else if(index == 1){
            s1Name = "才艺";
        }else if(index == 2){
            s1Name = "玩乐";
        }

        YQ.voxLogs({
            database: 'parent',
            module: moduleName,
            op : "o_iGq20mwI",
            s0 : sid,
            s1 : s1Name
        });

        location.href = "/mizar/shopdetail.vpage?shopId="+sid;
    });



    YQ.voxLogs({
        database: 'parent',
        module: moduleName,
        op : "o_YG0UtBE0"
    });
});