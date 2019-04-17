(function () {
    "use strict";
    function TermPages(obj){
        var self = this;
        self.totalPage      = ko.observable(obj.totalPage || 0);
        self.pageSize       = obj.pageSize || 5;
        self.currentPage    = ko.observable(obj.currentPage || 1);
        self.userInputPage  = ko.observable(null);
        self.pageClickCb     = obj.pageClickCb || null;

    }
    TermPages.prototype = {
        constructor : TermPages,
        goSpecifiedPage : function(){
            var self = this; //TermPages
            var totalPage = (+self.totalPage() || 0);
            var pageNo = (+self.userInputPage() || 0);
            if((pageNo <= 0) || (pageNo > totalPage)){
                self.userInputPage(null);
            }else{
                self.page_click(self,pageNo);
            }
        },
        page_click : function(self,pageNo){
            pageNo = +pageNo || 0;
            if(pageNo < 1 || pageNo > self.totalPage() || pageNo == self.currentPage()){
                return false;
            }
            self.currentPage(pageNo);
            typeof self.pageClickCb === "function"
            && self.pageClickCb(pageNo);
        },
        setPage : function(currentPage,totalPage){
            var self = this;
            self.totalPage(totalPage || 0);
            self.currentPage(currentPage || 1);
        },
        fetchPageSize : function(){
            return this.pageSize;
        }
    };

    $17.pagination = $17.pagination || {};
    $17.extend($17.pagination, {
        initPages : function(obj){
            return new TermPages(obj);
        }
    });
})();