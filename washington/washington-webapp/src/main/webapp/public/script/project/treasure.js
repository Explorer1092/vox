/**
 * Created by fengwei on 2017/1/5.
 */
define(['jquery',"voxLogs"],function($){
    var logJson = {
        module: "m_ERIgMdYP",
        op : 'o_1zmGRhnX'
    };

    var is_android_device= function(){
        if(navigator && navigator.userAgent && navigator.userAgent.toUpperCase().indexOf('ANDROID') != -1){
            return true;
        }
        return false;
    };

    $(document).on("click",".js-openBtn",function(){
        //android 老版本任然使用openparent
        if(is_android_device() && !devFlag){
            if(window.external && window.external['openparent']){
                window.external['openparent']("");
            }
            YQ.voxLogs(logJson);
        }else{
            if(window.external && window.external['openApp']){
                window.external['openApp'](JSON.stringify({
                    name:"a17parent",
                    log:JSON.stringify(logJson) //log参数使用json子串
                }))
            }else{
                YQ.voxLogs(logJson);
            }
        }
    });
});