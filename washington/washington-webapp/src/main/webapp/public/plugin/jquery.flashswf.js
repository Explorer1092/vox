/*
 IE:   <object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=11,1,0">
 <param name="movie" value="a.swf" />
 Other:<object type="application/x-shockwave-flash" data="a.swf">
 */

(function($, flash, Plugin){

    function _attrsFromObject(obj){
        var arr = [];

        for(var i in obj){
            if(obj[i]){
                arr.push([i, '="', obj[i], '"'].join(''));
            }
        }

        return arr.join(' ');
    }

    function _paramsFromObject(obj){
        var arr = [];

        for(var i in obj){
            arr.push([
                '<param name="', i,
                '" value="', (typeof obj[i] == 'object' ? $.param(obj[i]) : obj[i]), '" />'
            ].join(''));
        }

        return arr.join('');
    }

    try{
        var flashVersion = Plugin.description || (function(){
                return (
                    new Plugin('ShockwaveFlash.ShockwaveFlash')
                ).GetVariable('$version');
            }())
    }
    catch(e){
        flashVersion = 'Unavailable';
    }

    var flashVersionMatchVersionNumbers = flashVersion.match(/\d+/g) || [0];

    //IE10: "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)"
    //IE11模拟IE7: "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.3)"
    //IE11本身可能带着别的: "Mozilla/5.0 (MSIE 9.0; Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; InfoPath.3; rv:11.0) like Gecko"

    var tridentVersion = (navigator.userAgent.toLowerCase().match(/trident\/(\d+)/) || ['', 0])[1];
    var msieVersion = (navigator.userAgent.toLowerCase().match(/msie (\d+)/) || ['', 0])[1];

    //如果是 IE11 主浏览器，但工作在兼容模式，此时就不考虑IE11引擎
    if(tridentVersion >= 7){
        if(navigator.userAgent.indexOf("(compatible;") != -1)
            tridentVersion = 0;
    }

    //如果现在用的是IE11引擎，就不考虑MSIE版本（因为可能是错的）
    if(tridentVersion >= 7)
        msieVersion = 0;

    $[flash] = {
        available: flashVersionMatchVersionNumbers[0] > 0,

        activeX: Plugin && !Plugin.name,

        isOldMSIE: ((tridentVersion != 0 && tridentVersion < 7) || (msieVersion != 0 && msieVersion < 11)),

        version: {
            original: flashVersion,
            array   : flashVersionMatchVersionNumbers,
            string  : flashVersionMatchVersionNumbers.join('.'),
            major   : parseInt(flashVersionMatchVersionNumbers[0], 10) || 0,
            minor   : parseInt(flashVersionMatchVersionNumbers[1], 10) || 0,
            release : parseInt(flashVersionMatchVersionNumbers[2], 10) || 0
        },

        hasVersion: function(version){
            var versionArray = (/string|number/.test(typeof version))
                ? version.toString().split('.')
                : (/object/.test(typeof version))
                ? [version.major, version.minor]
                : version || [0, 0];


            function cmpAryInts(a, b){
                var x = (a[0] || 0) - (b[0] || 0);

                return x > 0 || (
                    !x &&
                    a.length > 0 &&
                    cmpAryInts(a.slice(1), b.slice(1))
                    );
            }

            return cmpAryInts(
                flashVersionMatchVersionNumbers,
                versionArray
            );
        },


        /* Cross-browser SWF removal
         - Especially needed to safely and completely remove a SWF in Internet Explorer
         */
        removeFlashById: function(id){

            var removeObjectInIE = function(id){
                var obj = document.getElementById(id);
                if(obj){
                    for(var i in obj){
                        if(typeof obj[i] == "function"){
                            obj[i] = null;
                        }
                    }
                    obj.parentNode.removeChild(obj);
                }
            };

            var obj = document.getElementById(id);
            if(obj && obj.nodeName == "OBJECT"){
                if($[flash].isOldMSIE){
                    obj.style.display = "none";
                    (function(){
                        if(obj.readyState == 4){
                            removeObjectInIE(id);
                        }
                        else{
                            setTimeout(function(){
                                $[flash].removeFlashById(id);
                            }, 10);
                        }
                    })();
                }
                else{
                    obj.parentNode.removeChild(obj);
                }
            }
        },

        createHtml: function(origAttrs, origParams){

            if(!origParams.movie){
                return false;
            }

            if(( !$[flash].available ) || !$[flash].hasVersion(origParams.requiredVersion || 1)){
                if(origParams.requiredVersionCallback){
                    origParams.requiredVersionCallback();
                }
                return false;
            }

            var attrs = {
                id     : origAttrs.id || 'flash_' + Math.floor(Math.random() * 999999999),
                width  : origAttrs.width || 320,
                height : origAttrs.height || 180,
                'class': origAttrs['class'] || '',
                style  : origAttrs.style || ''
            };

            if($[flash].isOldMSIE){
                //IE <= 10 用老机制
                attrs.classid = 'clsid:D27CDB6E-AE6D-11cf-96B8-444553540000';
                attrs.codebase = 'http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=11,1,0';
            }
            else{
                //IE11 需要用新机制
                attrs.data = origParams.movie;
                attrs.type = 'application/x-shockwave-flash';
            }

            var params = $.extend(true, {}, origParams);
            params.wmode = params.wmode || 'opaque';

            delete params.requiredVersion;
            delete params.requiredVersionCallback;

            return [
                '<object ', _attrsFromObject(attrs), '>',
                _paramsFromObject(params),
                '</object>'
            ].join('');
        }
    };

    $.fn[flash] = function(attrs, params){

        this.each(function(){
            var $this = $(this);

            var flashObjectHtml = $[flash].createHtml(attrs, params);

            if(flashObjectHtml){
                $this.children().remove();
                $this.html(flashObjectHtml);
            }
        });

        return this;
    };
}(
    jQuery,
    'flashswf',
    navigator.plugins['Shockwave Flash'] || window.ActiveXObject
));

/**
 * 修改加载 Flash 方式
 */
$.fn.__flashswf = $.fn.flashswf;
$.fn.flashswf = function(){
    throw new Error("别再粗旷的调用这个插件了...");
};
$.fn.getFlash = function(opt){
    var $self = $(this);

    var option = {
        width                  : 700,
        height                 : 450,
        movie                  : null,
        allowScriptAccess      : "always",
        allowFullScreen        : true,
        flashvars              : null,
        requiredVersion        : '11.5',
        requiredVersionCallback: function(){
            var tip = '<a href="' + ___client_setup_url + '" target="_blank" style="color:black;">您的系统组件需要升级。请点这里<span style="color:red;">下载</span>并<span style="color:red;">运行</span> “一起作业安装程序”，然后开始作业。</a>';
            //if(isVoxExternalPluginExisting){
                tip = '您的 Flash 版本太老或者不可用。<a href="http://down.tech.sina.com.cn/content/1149.html" target="_blank">请点此获得最新的 Flash （请下载并安装)</a>';
            //    try{
            //        window.external.execVoxPluginCommand('ForceUpdateFlash', '');
            //    }catch(e){}
            //}
            //
            if(___isBrowserForTablet){
                tip = ($17.getOperatingSystem() == 'iOS' || $17.getOperatingSystem() == 'Android') ? '网页版暂不支持做作业，请下载“一起作业”手机客户端 <br /> <a class="btn_mark w-btn w-btn-green" style="font-size: 20px;" href="http://wx.17zuoye.com/download/17studentapp?cid=102020" target="_blank"><strong>立即下载</strong></a>' : '一起作业暂时不支持手机或平板电脑，请使用电脑完成';
            }

            if($17.getCookieWithDefault("ObjGwchinam")){
                tip = tip + "<p>请先退出作业模式再下载安装</p>";
            }

            var _target = $('#install_flash_player_box');
            var _tip = $('#install_download_tip');

            if(_target.size() == 0){
                $self.append('<div id="install_flash_player_box" style="margin:20px;"></div>');
                _target = $('#install_flash_player_box');
            }

            if(_tip.size() == 0){
                _target.append('<span id="install_download_tip" style="color:#333; background-color:#eee; display:block; text-align:center; padding:70px 0; border:2px solid #ccc;"></span>');
                _tip = $('#install_download_tip');
            }

            _tip.html(tip);
            _target.show();
        }
    };

    $.extend(option, opt);

    if(option.movie == null || option.flashvars == null){
        throw new Error("请检查参数! movie 或 flashvars 为空");
        return false;
    }

    $self.__flashswf(option, option);
};