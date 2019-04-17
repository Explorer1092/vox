/*
 * 家长奖励
 */
define(["jquery","$17","knockout","userpopup","wx"], function ($,$17,knockout,userpopup,wx) {
    /****************变量声明***********/
    wx.config({
        debug: false, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
        appId: wechatConfig.appId, // 必填，公众号的唯一标识
        timestamp: wechatConfig.timestamp, // 必填，生成签名的时间戳
        nonceStr: wechatConfig.noncestr, // 必填，生成签名的随机串
        signature: wechatConfig.signature,// 必填，签名，见附录1
        jsApiList: ['chooseImage','uploadImage'] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
    });

    var parentWardModalAndView = {
        sid: knockout.observable(0),
        page: knockout.observable(1),
        showTab: knockout.observable("ongoing"),
        missions: knockout.observableArray(),
        missionsContent: knockout.observableArray([]),
        haveNext: knockout.observable(false),
        isGraduate : knockout.observable(false),
        showComp: showComp,
        showGoing: showGoing,
        updateProgress: updateProgress,
        nextPage: nextPage,
        doReward: doReward,
        setNewMission:setNewMission,
        setChildNotices: setChildNotices
    };

    /****************方法声明***********/
    function showComp () {
        parentWardModalAndView.showTab("complete");
        loadMissions(parentWardModalAndView.sid(),parentWardModalAndView.showTab(),parentWardModalAndView.page());
    }

    function showGoing () {
        parentWardModalAndView.showTab("ongoing");
        loadMissionsOngoing(parentWardModalAndView.sid(),parentWardModalAndView.showTab(),parentWardModalAndView.page());
    }

    function loadMissionsOngoing(studentId,status,page){
        //加载任务，并渲染页面
        $.post('getmissions.vpage',{studentId:studentId,status:status,page:page},function(data){
            if(data.success){
                renderPage(data);
            }else{
                $17.jqmHintBox("加载任务失败！"+data.info);
            }
            parentWardModalAndView.isGraduate(data.isGraduate);
        })
    }

    function loadMissions(studentId,status,page){
        //加载任务，并渲染页面
        $.post('getfootprint.vpage',{studentId:studentId,status:status,page:page},function(data){
            if(data.success){
                renderPage(data);
            }else{
                $17.jqmHintBox("加载任务失败！"+data.info);
            }
        })
    }

    function renderPage(data){
        parentWardModalAndView.missions(data.missions);
        parentWardModalAndView.missionsContent(data.missions.content);
        parentWardModalAndView.haveNext(data.missions.last);
    }

    //更新进度
    function updateProgress () {
        var missionId = this.id;
        $.post("updateprogress.vpage", {missionId: missionId}, function (data) {
            if(data.success){
                reloadResource();
            }else{
                $17.jqmHintBox("更新进度失败！"+data.info);
            }
        });
    }

    function reloadResource () {
        if(parentWardModalAndView.showTab() == "ongoing"){
            loadMissionsOngoing(parentWardModalAndView.sid(),parentWardModalAndView.showTab(),parentWardModalAndView.page());
        }else{
            loadMissions(parentWardModalAndView.sid(),parentWardModalAndView.showTab(),parentWardModalAndView.page());
        }
    }

    function nextPage () {
        parentWardModalAndView.page(parentWardModalAndView.page()+1);
        reloadResource();
    }

    function doReward () {
        var missionId = this.id;
        $.post("doreward.vpage", {missionId: missionId}, function (data) {
            if(data.success){
                reloadResource();
            }else{
                $17.jqmHintBox("发放奖励失败！"+data.info);
            }
        });
    }

    function setNewMission () {
        location.href = 'setmissions.vpage?sid='+parentWardModalAndView.sid();
    }

    function setChildNotices () {
        location.href = 'notices.vpage?studentId='+parentWardModalAndView.sid();
    }

    /****************事件交互***********/
    //上传图片
    $(document).on('click',".upload-pic",function(){
        //通过jsapi选择图片和上传图片
        var missionId=$(this).attr('data-missionid');
        var imageBox,imageHideBox,type = this.type;
        if(type == "changeImgBtn"){
            imageBox = $("div[data-changeImageBox = "+missionId+"]");
            imageHideBox = $("div[data-imageBox = "+missionId+"]");
        }else{
            imageBox = $("div[data-imageBox = "+missionId+"]");
            imageHideBox = $("div[data-changeImageBox = "+missionId+"]");
        }
        wx.chooseImage({
            count: 1, // 默认9
            sizeType: ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
            success: function (res) {
                $(".v-photoShowBox-"+missionId).show().html('<span class="lod">上传中...</span>');
                var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
                var localId=localIds[0];
                wx.uploadImage({
                    localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
                    isShowProgressTips: 1, // 默认为1，显示进度提示
                    success: function (res) {
                        var serverId = res.serverId; // 返回图片的服务器端ID
                        $.post('uploadimg.vpage',{missionId:missionId,mediaId:serverId},function(data){
                            if(data.success){
                                imageBox.show().html('<img src="'+localId+'"/>');
                                imageHideBox.hide();
                            }else{
                                imageBox.show().html(data.msg);
                                imageHideBox.hide();
                            }
                        })
                    }
                });
            }
        });
    });

    //预览图
    $(document).on('click','.v-clickPic',function(){
        var imgUrl=$(this).find("img").attr('src');
        $('.pr-layerbox').show();
        if($17.isBlank(imgUrl)){
            imgUrl = "{{ url_for('static',filename='css/parentreward/images/photo.jpg') }}";
        }
        $('.pr-layerbox img').attr('src',imgUrl);
    });

    //预览图关闭
    $('.pr-layerbox').on('click',function(){
        $(this).toggle();
    });

    userpopup.selectStudent("parentWard");

    knockout.applyBindings(parentWardModalAndView);

    return {
        loadMessageById: function (sid) {
            parentWardModalAndView.sid(sid);
            parentWardModalAndView.showTab("ongoing");
            loadMissionsOngoing(parentWardModalAndView.sid(),parentWardModalAndView.showTab(),parentWardModalAndView.page());
        }
    };


});