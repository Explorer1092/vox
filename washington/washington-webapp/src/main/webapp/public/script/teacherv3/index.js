/*Created by yifei.peng on 2016/3/25*/
(function(){
    /*判断是否为PC客户端*/
    if(VoxExternalPluginExists()){
        $.prompt("<div style='text-align: center'>一起作业电脑客户端目前已停止维护，为了给您更好的体验，<br/>请使用最新浏览器登录一起作业网<a href='http://www.17zuoye.com' target='_blank'>www.17zuoye.com</a>。</div>", {
            focus: 1,
            title: '系统提示',
            buttons: {"取消": false, "打开浏览器": true},
            submit: function(e, v){
                if(v){
                    window.open("http://www.17zuoye.com", "_blank");
                }
            }
        });
        return false;
    }

    //console.log(VoxTeacherHome);
    switch (VoxTeacherHome.dataPopup) {
        case "activate--1" :
            /*$.prompt('<iframe class="vox17zuoyeIframe" src="/teacher/invite/student.vpage" width="720" marginwidth="0" height="450" marginheight="0" scrolling="no" frameborder="0"></iframe>', {
             title: "学生邀请老师",
             position: { width: 730 },
             buttons: {}
             });*/
            break;
        default :
            if(questItemPopup && questItemPopup.length > 0){
                for(var i = 0, len = questItemPopup.length; i < len; i++){
                    var $notAuth = (!$17.isBlank(questItemPopup[i].notAuth) ? questItemPopup[i].notAuth : currentHasAuth);

                    if(questItemPopup[i].isHas && $notAuth && !$17.getCookieWithDefault(questItemPopup[i].cookieName)){
                        $17.setCookieOneDay(questItemPopup[i].cookieName, questItemPopup[i].cookieCount, questItemPopup[i].cookieCount);
                        $.prompt(questItemPopup[i].content,{
                            title : questItemPopup[i].title,
                            buttons : {}
                        });
                        return false;
                    }
                }
            }

            if(VoxTeacherHome.vacationPopup && !$17.getCookieWithDefault("vacationad") && currentSubject != "CHINESE"){
                $17.setCookieOneDay("vacationad", "1", 1);
                $.prompt(template("T:VACATION_POPUP", {subject : currentSubject}),{
                    prefix : "null-popup",
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    }
                });
                return false;
            }

            //大使新活动
            if(VoxTeacherHome.ambassadorPopup && !$17.getCookieWithDefault("ambck")){
                $17.setCookieOneDay("ambck", "1", 1);
                $.prompt(template("T:大使新活动", {}),{
                    prefix : "null-popup",
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    }
                });
                return false;
            }


            if(VoxTeacherHome.extensionPopup){
                //杭州老师APP推广
                if( !$17.getCookieWithDefault("market") ){
                    $17.setCookieOneDay("market", "1", 1);
                    $.prompt(template("T:杭州老师APP推广", {}),{
                        prefix : "null-popup",
                        buttons : { },
                        classes : {
                            fade: 'jqifade',
                            close: 'w-hide'
                        }
                    });

                    $17.voxLog({
                        module : "project-extension",
                        op : "popupShow"
                    });
                    return false;
                }
            }

            if(VoxTeacherHome.dataCapl && true){
                $.prompt(changeClassesPopup($itemDataCapl, {
                    type : systemClazzObj.subjectPopup,
                    title : systemClazzObj.titlePopup
                }));
                return false;
            }

            //认证老师
            if(currentHasAuth){
                //我要当校园大使
                //if(!VoxTeacherHome.schoolHasAmb && !$17.getCookieWithDefault("wdxyds")){
                //    $17.setCookieOneDay("wdxyds", "1", 1);
                //    $.prompt(template("T:我要当校园大使-popup", {}),{
                //        prefix : "null-popup",
                //        buttons : { },
                //        classes : {
                //            fade: 'jqifade',
                //            close: 'w-hide'
                //        }
                //    });
                //    return false;
                //}
            }else{
                //未认证弹出框
            }

            //新版UGC 获取用户当前有效的UGC收集活动 方法
            if(!$17.getCookieWithDefault("ugcxxAty")){
                $.UgcRecordPopup();
                return false;
            }

            /*--首页广告位弹窗--*/
            YQ.voxSpread({
                keyId : 110103
            }, function(result){
                if(result.success && result.data.length > 0){
                    var popupItems = result.data;
                    for(var i = 0; i < popupItems.length; i++){
                        if(!$17.getCookieWithDefault("TADVER" + popupItems[i].id)){
                            $17.setCookieOneDay("TADVER" + popupItems[i].id, "1");
                            if(popupItems[i].img){
                                $.prompt(template("T:PUBLIC-POPUP-BOX", { result : result, index : i }), {
                                    prefix : "null-popup",
                                    position : { width: 680},
                                    buttons : {},
                                    classes : {
                                        fade: 'jqifade',
                                        close: 'w-hide'
                                    }
                                });
                            }else{
                                $.prompt(popupItems[i].content+"<div style='padding-top:50px;text-align: center;'><a href='"+popupItems[i].url+"' class='w-btn w-btn-blue w-btn-well'>"+popupItems[i].btnContent+"</a></div>",{
                                    title:popupItems[i].description,
                                    buttons:{}
                                });
                            }
                            break;
                        }
                    }
                    return false;
                }
            });
    }
}());

function nextHomeWork(){
    $.prompt.close();
}