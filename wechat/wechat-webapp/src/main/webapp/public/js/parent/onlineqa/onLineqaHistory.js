define(['jquery','knockout','$17','logger'],function($,ko,$17,logger){
    var ViewModule = function(){
        var self = this;
        self.historyList = ko.observableArray([]);
        self.currentpage = ko.observable(0);
        self.isLast = ko.observable(false);
        self.productType = ko.observable('');
        self.isShowNull = ko.observable(false);
        self.getProductType = function(productType){
            self.productType(productType);
        };

        //获取历史数据
        self.getHistoryList = function(){
            $17.loadingStart();
            $.post('/parent/onlineqa/loadhistory.vpage',{pn : self.currentpage()},function(data){
                if(data.success){
                    for (var i = 0; i < data.page.content.length; i++) {
                        if (data.page.content[i]) {
                            self.historyList.push(data.page.content[i]);
                        }
                    }
                    self.isLast(data.page.last);
                    self.isShowNull(true);
                }else{
                    $17.jqmHintBox(data.info);
                }
                $17.loadingEnd();
            });
        };

        //获取更多
        self.showMoreBtn = function(){
            self.currentpage(self.currentpage() + 1);
            self.getHistoryList();
        };

        //
        self.jumpUrl = function(url){
            location.href = url;
        };

        //初始化
        self.getHistoryList();

        //logger
        setTimeout(function(){
            logger.log({
                module: 'onlineqa',
                op: 'onlineqa_history_'+self.productType()+'_pv'
            });
        },500);

    };
    ko.applyBindings(new ViewModule());
});