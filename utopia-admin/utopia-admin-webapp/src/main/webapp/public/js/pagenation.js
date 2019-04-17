/**
 * Created by xinqiang.wang on 2016/6/15.
 */
$.fn.page = function(option){
    function draw($target, def){
        if(def.model == "normal"){
            var current = parseInt(def.current);
            var maxNum = parseInt(def.maxNumber);
            var _total = parseInt(def.total);
            var _start = current - Math.floor(def.maxNumber / 2);
            var _end = current + Math.floor(def.maxNumber / 2);

            _start = _start < maxNum ? 1 : _start;
            _end = _end > _total ? _total : _end;

            $target.html('<a v="prev" href="' + def.href + '" class="' + (def.current > 1 ? def.enableMark : def.disabledMark) + ' ' + def.prev.className + '" style="' + def.prev.style + '">' + def.prev.text + '</a>');
            if(_start > 1){
                $target.append('<a href="' + def.href + '"><span>1</span></a>');
                $target.append('<span class="points"> ... </span>');
            }
            for(var i = _start; i <= _end; i++){
                $target.append('<a href="' + def.href + '" ' + (i == def.current ? ('class="' + def.currentMark + '"') : '') + '><span>' + i + '</span></a>');
            }
            if(_end < _total){
                $target.append('<span class="points"> ... </span>');
                if (def.showTotalPage)
                    $target.append('<a href="' + def.href + '"><span>' + _total + '</span></a>');
            }
            $target.append('<a v="next" href="' + def.href + '" class="' + ( _total <= 1 || def.current >= _total ? def.disabledMark : def.enableMark ) + ' ' + def.next.className + '" style="' + def.next.style + '">' + def.next.text + '</a>');
            $target.show();

            $target.find('a[class != ' + def.currentMark + '][class != ' + def.disabledMark + ']').one("click", function(){
                switch($(this).attr("v")){
                    case "prev":
                        jump($target, def, current - 1);
                        break;
                    case "next":
                        jump($target, def, current + 1);
                        break;
                    default:
                        jump($target, def, $(this).find('span').html());
                        break;
                }
            });
        }else{
            //为非常规页码，如“A，B..”，预留
        }
    }

    function jump($target, def, index){
        if(index < 1 || index > def.total){
            return false;
        }

        def.current = index;

        draw($target, def);

        if($.isFunction(def.jumpCallBack)){
            def.jumpCallBack(def.current);
        }else if(def.jumpCallBack){
            eval(def.jumpCallBack + "(options.current)");
        }
        if(def.autoBackToTop){
            $17.backToTop();
        }
    }

    return this.each(function(){
        var $target = $(this);
        var def = {
            total        : 0,
            current      : 1,
            maxNumber    : 5,
            currentMark  : "this",
            disabledMark : "disable",
            enableMark   : "enable",
            model        : "normal",
            showTotalPage: true,
            autoBackToTop: true,
            next         : {
                text     : "<span>下一页</span>",
                className: "",
                style    : ""
            },
            prev         : {
                text     : "<span>上一页</span>",
                className: "",
                style    : ""
            },
            href         : "javascript:void(0);",
            jumpCallBack : null
        };

        $.extend(def, option);

        if($target.length < 1){
            return false;
        }

        if(def.total < 1){
            $target.empty().hide();
            return false;
        }

        def.maxNumber = def.maxNumber > 5 ? def.maxNumber : 5;

        draw($target, def);
    });
};
