(function($17,ko){
    var allConversation = {
        loadImage : {
            title       : '预 览',
            html        : template("t:LOAD_IMAGE",{}),
            buttons     : {},
            position    : { width: 450},
            focus       : 0,
            submit:function(e,v,m,f){}
        },
        itemDetail: {
            title       : '预 览',
            html        : "",
            position    : { width: 600},
            buttons     : {},
            focus       : 1,
            submit:function(e,v,m,f){}
        },
        responseError : {
            title       : '预 览',
            html        : "<div class='w-ag-center' data-bind='text:info'></div>",
            position    : { width: 450},
            buttons     : {"确定": true},
            focus       : 0,
            submit:function(e,v,m,f){
                e.preventDefault();
                $.prompt.close();
            }
        }
    };
    var self = this;
    function viewDubbing(paramOpt){
        var paramData = {
            bookId : null,
            unitId : null,
            dubbingId : null
        };
        var newAllConversation = $.extend(true,{},allConversation,{
            itemDetail : {
                html : template("t:DUBBING_DETAIL_PREVIEW", {})
            }
        });
        self.paramOpt = $.extend(true,paramData,paramOpt);
        var dubbingId = self.paramOpt.dubbingId;
        var subject = self.paramOpt.subject;
        var homeworkType = self.paramOpt.homeworkType;
        var collectDubbingCb = paramOpt.collectDubbingCb || function(){};
        var closeCb = paramOpt.closeCb || function(){};
        $.prompt(newAllConversation,{
            loaded : function(){
                var initData = function(data){
                    var dubbingObj = data.dubbingDetail || {};
                    $.prompt.goToState("itemDetail",function(){
                        var flashWidth = 550,flashHeight = 275;
                        $("#dubbingPlayVideoContainer").getFlash({
                            id       : "DUBBING_PLAY_PREVIEW",
                            width    : flashWidth,//flash 宽度
                            height   : flashHeight, //flash 高度
                            movie    : constantObj.flashPlayerUrl,
                            scale    : 'showall',
                            flashvars: "file=" + dubbingObj.videoUrl + "&amp;image=" + dubbingObj.coverUrl + "&amp;width=" + flashWidth + "&amp;height=" + flashHeight + "&amp;autostart=true"
                        });
                    });
                    ko.applyBindings({
                        homeworkType : homeworkType,
                        dubbingObj : dubbingObj,
                        isCollect   : ko.observable(dubbingObj.isCollection),
                        requestLock    : false,
                        collectDubbing : function(){
                            var that = this;
                            $.get("/teacher/new/homework/dubbing/collection.vpage",{
                                dubbingId : dubbingId,
                                subject   : subject
                            }).done(function(res){
                                if(res.success){
                                    collectDubbingCb(that.isCollect(),!that.isCollect());
                                    that.isCollect(!that.isCollect());
                                }else{
                                    collectDubbingCb(that.isCollect(),that.isCollect());
                                }
                            }).fail(function(){
                                collectDubbingCb(that.isCollect(),that.isCollect());
                            });
                        }
                    }, document.getElementById("jqistate_itemDetail"));
                };

                $.get("/teacher/new/homework/dubbing/detail.vpage", self.paramOpt).done(function(data){
                    if(data.success){
                        initData(data);
                    }else{
                        $.prompt.goToState("responseError");
                        ko.applyBindings(self, document.getElementById("jqistate_responseError"));
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/new/homework/report/examandquiz/detailinfo.vpage",
                            s1     : $.toJSON(data),
                            s2     : $.toJSON(paramData),
                            s3     : self.subject
                        });
                    }
                }).fail(function(){});
            },
            close : closeCb
        });
    }

    function viewOralCommunicationDetail(paramOpt){
        var paramData = {
            bookId : null,
            unitId : null,
            oralCommunicationId : null
        };
        var newAllConversation = $.extend(true,{},allConversation,{
            itemDetail : {
                html        : template("t:ORAL_COMMUNICATION_DETAIL_PREVIEW", {}),
                buttons     : {"确定": true},
                position    : { width: 600}
            }
        });

        self.paramOpt = $.extend(true,paramData,paramOpt);
        var homeworkType = self.paramOpt.homeworkType;
        var closeCb = paramOpt.closeCb || function(){};
        $.prompt(newAllConversation,{
            loaded : function(){
                var initData = function(data){
                    var oralCommunicationDetail = data.oralCommunicationDetail || {};
                    $.prompt.goToState("itemDetail",function(){});
                    ko.applyBindings({
                        homeworkType : homeworkType,
                        item         : oralCommunicationDetail
                    }, document.getElementById("jqistate_itemDetail"));
                };
                $.get("/teacher/new/homework/oralcommunication/detail.vpage", self.paramOpt).done(function(data){
                    if(data.success){
                        initData(data);
                    }else{
                        $.prompt.goToState("responseError");
                        ko.applyBindings(self, document.getElementById("jqistate_responseError"));
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/new/homework/report/examandquiz/detailinfo.vpage",
                            s1     : $.toJSON(data),
                            s2     : $.toJSON(paramData),
                            s3     : self.subject
                        });
                    }
                }).fail(function(){});
            },
            close : closeCb
        });
    }

    $17.homeworkv3 = $17.homeworkv3 || {};
    $17.extend($17.homeworkv3, {
        viewDubbingDetail : viewDubbing.bind(self),
        viewOralCommunicationDetail : viewOralCommunicationDetail.bind(self)
    });

}($17,ko));