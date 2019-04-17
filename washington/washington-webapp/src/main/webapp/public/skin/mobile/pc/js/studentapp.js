(function(){
    "use strict";
    function $M(element) {
        if (arguments.length > 1) {
            for (var i = 0, elements = [], length = arguments.length; i < length; i++) {
                elements.push($M(arguments[i]));
            }
            return elements;
        }
        if (Object.prototype.toString.call(element) == "[object String]") {
            element = document.getElementById(element);
        }
        return element;
    }

    function extend(child, parent) {
        var key;
        for (key in parent) {
            if (parent.hasOwnProperty(key)) {
                child[key] = parent[key];
            }
        }
    }

    extend(window, {
        $M: $M
    });
    extend(window.$M, {
        extend: extend
    });

    /*功能区*/
    function ajaxLoadErrorOrTimeout(title){
        title = title || '网络异常，请检查设置后重试';
        $("body").append('<div id="base_error_popup_retry_page" style="position: fixed; width: 100%; top: 0; left: 0; z-index: 1000" class="m-summary-page">' +
        '<div class="ms-error">' +
        '<div class="ms-error-icon"></div>' +
        '<div class="ms-error-info">' + title + '</div>' +
        '<div class="ms-error-btn">' +
        '<div id="base_error_popup_retry_button" class="w-btn w-btn-white">重新获取作业 </div>' +
        '</div></div></div>');

    }

    function promptAlert(content,title){
        title = title || "提示";

        var html = '<div class="layer"> ' +
            '<div class="layer-bg"></div> ' +
            '<div class="layer-box"> ' +
            '<span class="close" id="promptAlertCloseBut"></span> ' +
            '<div class="box"> ' +
            '<p>'+title+'</p> ' +
            '<p>'+content+'</p> ' +
            '</div> ' +
            '</div> </div>';
        $('body').append(html);
    }

    function appLog(tagType,dataJson){
        /*见 http://wiki.17zuoye.net/pages/viewpage.action?pageId=11501578*/
        console.info(dataJson);
        if(window.external && ('log_b' in window.external)){
            window.external.log_b(tagType,JSON.stringify(dataJson));
        }
    }

    function commonAjax(url, requestData, callback, method) {
        var dialogWaiting=$('<div class="dialogWaiting">请稍候……</div>').appendTo('body');
        var options = {
            url: url,
            dataType: 'json',
            method: method ? method : 'get',
            data: requestData,
            timeout: 1000 * 15,
            success: function (data) {
                dialogWaiting.remove();
                if (data['error']) {
                    alert(data['error']);
                } else if (callback) {
                    callback(data);
                }
            },
            error: function () {
                dialogWaiting.remove();
                alert('服务器错误！');
            }
        };
        $.ajax(options);
    }

    //获取手机操作系统
    function getMobileOperatingSystem() {
        var userAgent = navigator.userAgent || navigator.vendor || window.opera;
        if (userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
            return 'iOS';
        }
        else if (userAgent.match(/Android/i)) {
            return 'Android';
        }
        else {
            return 'unknown';
        }
    }

    function showLoading(){
        var html = '<div class="loading_ajax_module"><img src="/public/skin/mobile/pc/images/studentapp/loading.gif" alt=""/><strong>加载中</strong></div>';
        var loading_ajax_module = $('.loading_ajax_module');
        if(loading_ajax_module.length > 0){
            loading_ajax_module.show();
            return;
        }
        $('body').append(html);
    }

    function hideLoading(){
        $('.loading_ajax_module').hide();
    }

    function bottomHint(cotent){
        var html = '<span class="bottomHint" style="background-color: #000; color: #fff; padding: 24px; border-radius: 4px; display: inline-block; font-size: 28px; position: fixed; bottom: 20px; left: 50%; margin-left: -120px;">'+cotent+'</span>';
        $('body').append(html);
        setTimeout(function(){$('.bottomHint').hide();},2000);
    }

    function isBlank(str){
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

    $M.extend($M, {
        ajaxLoadErrorOrTimeout: ajaxLoadErrorOrTimeout,
        promptAlert : promptAlert,
        appLog : appLog,
        commonAjax : commonAjax,
        getMobileOperatingSystem : getMobileOperatingSystem,
        showLoading : showLoading,
        hideLoading :hideLoading,
        bottomHint: bottomHint,
        isBlank:isBlank
    });

    $(document).on("click", ".doClickOpenParent",function(){

        var $self = $(this),
            trackInfo = {
                app : "openParent",
                module : $self.data('module') || "",
                op : $self.data('op') || ""
            };

        if(window.external && ('openparent' in window.external)){
            window.external.openparent("");
        }

        $M.appLog('reward',trackInfo);
    });

}());
