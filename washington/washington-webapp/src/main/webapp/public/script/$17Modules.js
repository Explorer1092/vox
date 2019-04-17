//时间组件
;(function($17){
    "use strict";

    var defaults = {
        startDateId     : null,
        startHourId     : null,
        startMinuteId   : null,
        startDefaultDate: null,
        startMinDate    : null,
        startMaxDate    : null,
        startTime       : "",

        endDateId       : null,
        endHourId       : null,
        endMinuteId     : null,
        endDefaultDate  : null,
        endMinDate      : null,
        endMaxDate      : null,
        endTime         : "",

        dateFormat      : 'yy-mm-dd',
        numberOfMonths  : 2,
        differ          : 5
    };

    var h = ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'];
    var m = [
        '00', '01', '02', '03', '04', '05', '06', '07', '08', '09',
        '10', '11', '12', '13', '14', '15', '16', '17', '18', '19',
        '20', '21', '22', '23', '24', '25', '26', '27', '28', '29',
        '30', '31', '32', '33', '34', '35', '36', '37', '38', '39',
        '40', '41', '42', '43', '44', '45', '46', '47', '48', '49',
        '50', '51', '52', '53', '54', '55', '56', '57', '58', '59'
    ];

    var startDate   = null;
    var endDate     = null;
    var _now        = null;
    var _sh         = null;
    var _sm         = null;
    var _eh         = null;
    var _em         = null;

    function getTimeArray(array, index){
        return $.grep(array, function (val, key) {
            return val >= index;
        });
    }

    function resetStartTime(){
        var nowDate     = defaults.startMinDate;
        var startDate   = $(defaults.startDateId).val();

        _sh = getTimeArray(h, defaults.startTime.split(":")[0]);
        _sm = getTimeArray(m, defaults.startTime.split(":")[1]);

        if(startDate == nowDate){
            $17.setSelect(defaults.startHourId, _sh, _sh, $(defaults.startHourId).val());
            if($(defaults.startHourId).val() == defaults.startTime.split(":")[0]){
                $17.setSelect(defaults.startMinuteId, _sm, _sm, $(defaults.startMinuteId).val());
            }else{
                $17.setSelect(defaults.startMinuteId, m, m, $(defaults.startMinuteId).val());
            }
        }else{
            $17.setSelect(defaults.startHourId, h, h, $(defaults.startHourId).val());
            $17.setSelect(defaults.startMinuteId, m, m, $(defaults.startMinuteId).val());
        }
    }

    function resetEndTime() {
        var nowDate = defaults.startMinDate;
        var endDate = $(defaults.endDateId).val();

        if($17.isBlank(defaults.startHourId)){
            _eh = getTimeArray(h, defaults.endTime.split(":")[0]);
            _em = getTimeArray(m, defaults.endTime.split(":")[1]);

            if(endDate == nowDate){
                $17.setSelect(defaults.endHourId, _eh, _eh, $(defaults.endHourId).val());
                if($(defaults.endHourId).val() == defaults.endTime.split(":")[0]){
                    $17.setSelect(defaults.endMinuteId, _em, _em, $(defaults.endMinuteId).val());
                }else{
                    $17.setSelect(defaults.endMinuteId, m, m, $(defaults.endMinuteId).val());
                }
            }else{
                $17.setSelect(defaults.endHourId, h, h, "23");
                $17.setSelect(defaults.endMinuteId, m, m, "00");
            }
        }else{
            if($(defaults.startDateId).val() == $(defaults.endDateId).val()){
                _eh = getTimeArray(h, $(defaults.startHourId).val());

                if($(defaults.startHourId).val() == $(defaults.endHourId).val()){
                    _em = getTimeArray(m, $(defaults.startMinuteId).val());
                }else{
                    _em = getTimeArray(m, 0);
                }

                $17.setSelect(defaults.endHourId, _eh, _eh, "23");
                $17.setSelect(defaults.endMinuteId, _em, _em, "00");
            }else{
                _em = getTimeArray(m, defaults.endTime.split(":")[1]);
                _eh = getTimeArray(h, defaults.endTime.split(":")[0]);

                $17.setSelect(defaults.endHourId, h, h, "23");
                $17.setSelect(defaults.endMinuteId, m, m, "00");
            }
        }

        checkTime();
    }

    function checkTime(){
        if(!$17.isBlank(defaults.startHourId) && $(defaults.startDateId).val() == $(defaults.endDateId).val() && $(defaults.startHourId).val() == $(defaults.endHourId).val()){
            if($(defaults.startMinuteId).val()*1 + defaults.differ < 60){
                _em = getTimeArray(m, $(defaults.startMinuteId).val()*1 + defaults.differ);
                $17.setSelect(defaults.endMinuteId, _em, _em, _em[0]);
            }else{
                _em = getTimeArray(m, $(defaults.startMinuteId).val()*1 + defaults.differ - 60);
                $17.setSelect(defaults.endMinuteId, _em, _em, _em[0]);

                if($(defaults.startHourId).val()*1 + 1 < 24){
                    _eh = getTimeArray(h, $(defaults.startHourId).val()*1 + 1);
                    $17.setSelect(defaults.endHourId, _eh, _eh, _eh[0]);
                }else{
                    $17.setSelect(defaults.endHourId, h, h, h[0]);

                    var newDate = $17.DateUtils("%Y-%M-%d", 1, "d", new Date($(defaults.endDateId).val()));

                    $(defaults.endDateId).val(newDate);
                }
            }
        }
    }

    function timepicker(option){
        $17.extend(defaults, option);

        $(function(){
            $(defaults.startDateId).datepicker({
                dateFormat      : defaults.dateFormat,
                defaultDate     : defaults.startDefaultDate,
                numberOfMonths  : defaults.numberOfMonths,
                minDate         : defaults.startMinDate,
                maxDate         : defaults.startMaxDate,
                onSelect        : function (selectedDate){
                    if(defaults.endMaxDate == null){
                        $(defaults.endDateId).datepicker('option', 'minDate', selectedDate);
                    }
                    if(defaults.startHourId != null){
                        resetStartTime();
                        resetEndTime();
                        checkTime();
                    }
                }
            });

            $(defaults.endDateId).datepicker({
                dateFormat      : defaults.dateFormat,
                defaultDate     : defaults.endDefaultDate,
                numberOfMonths  : defaults.numberOfMonths,
                minDate         : defaults.endMinDate,
                maxDate         : defaults.endMaxDate,
                onSelect        : function(){
                    resetEndTime();
                    checkTime();
                }
            });

            if(defaults.startHourId != null){
                resetStartTime();
            }
            resetEndTime();

            if(defaults.startHourId != null){
                $(defaults.startHourId).on("change", function(){
                    var newStartDate = $(defaults.startDateId).val();
                    var mArray = getTimeArray(m, defaults.startTime.split(":")[1]);

                    startDate = $(defaults.startDateId).val();
                    _now = defaults.endMinDate;

                    if(startDate == _now && $(defaults.startDateId).val() == defaults.startTime.split(":")[0]){
                        $17.setSelect(defaults.startMinuteId, _sm, _sm, _sm[0]);
                    }else{
                        $17.setSelect(defaults.startMinuteId, m, m, m[0]);
                    }

                    resetEndTime();
                });

                $(defaults.startMinuteId).on("change", function(){
                    $(defaults.endHourId).val("23");
                    resetEndTime();
                });
            }

            $(defaults.endHourId).on("change", function(){
                endDate = $(defaults.endDateId).val();
                _now = defaults.endMinDate;
                _em = getTimeArray(m, defaults.endTime.split(":")[1]);
                if (endDate == _now && $(defaults.endHourId).val() == defaults.endTime.split(":")[0]) {
                    $17.setSelect(defaults.endMinuteId, _em, _em, _em[0]);
                } else {
                    $17.setSelect(defaults.endMinuteId, m, m, m[0]);
                }

                checkTime();
            });
        });
    }

    $17.include(timepicker, {
        getStartDate: function(){
            return $(defaults.startDateId).val();
        },
        getStartTime: function(){
            return $(defaults.startDateId).val() + " " + $(defaults.startHourId).val() + ":" + $(defaults.startMinuteId).val() + ":00";
        },
        getEndTime: function(){
            return $(defaults.endDateId).val() + " " + $(defaults.endHourId).val() + ":" + $(defaults.endMinuteId).val() + ":00"
        }
    });

    $17.modules = $17.modules || {};

    $17.extend($17.modules, {
        timepicker: timepicker
    });
}($17));

//checkbox
(function($17){
    "use strict";

    var defaults = {
        parent              : null,
        checkboxTarget      : null,
        checkboxAllTarget   : null,
        style               : "w-checkbox-current",
        values              : ["data-value"]
    };

    /**
     * 实现简单 checkbox 效果
     * @param checkboxTarget : 单个 checkbox 是独立对象，独立管理变量
     */
    function littleBoy(checkboxTarget){
        $(checkboxTarget).die().live("click", function(){
            var $self       = $(this);
            var eventData   = [];

            if($self.hasClass(defaults.style)){
                $self.removeClass(defaults.style);
                $self.attr(defaults.values[0], "false");
                eventData.push("false");
            }else{
                $self.addClass(defaults.style);
                $self.attr(defaults.values[0], "true");
                eventData.push("true");
            }

            $(defaults.parent).trigger({
                type        : "$17.modules.checkboxs.click",
                eventData   : eventData
            });
        });
    }

    /**
     * 实现带全选的 checkbox 效果
     * @param parent            : 事件范围
     * @param checkboxAllTarget : 全选对象选择器
     * @param checkboxTarget    : checkbox 对象选择器
     */
    function bigBoy(parent, checkboxAllTarget, checkboxTarget){
        var $allTarget  = $(parent + " " + checkboxAllTarget);
        var $nodeTarget = $(parent + " " + checkboxTarget);

        $allTarget.die().live("click", function(){
            var $self       = $(this);
            var eventData   = [];

            if($self.hasClass(defaults.style)){
                $self.removeClass(defaults.style);
                $self.attr(defaults.values[0] + "s", "");
                $nodeTarget.removeClass(defaults.style);
            }else{
                $self.addClass(defaults.style);
                $nodeTarget.addClass(defaults.style);

                var __values = [];
                $nodeTarget.each(function(index, value){
                    __values.push($(value).attr(defaults.values[0]));
                });

                $self.attr(defaults.values[0] + "s", __values);
                eventData.push(__values);
            }

            $self.trigger({
                type        : "$17.modules.checkboxs.click",
                eventData   : eventData
            });
        });

        $nodeTarget.die().live("click", function(){
            $(this).toggleClass(defaults.style);

            var __count  = 0;
            var __values = [];
            var eventData = [];
            $nodeTarget.each(function(index, value){
                if($(value).hasClass(defaults.style)){
                    __count++;
                    __values.push($(value).attr(defaults.values[0]));
                }
            });

            if($nodeTarget.length == __count){
                $allTarget.addClass(defaults.style);
            }else{
                $allTarget.removeClass(defaults.style);
            }

            $allTarget.attr(defaults.values[0] + "s", __values);
            eventData.push(__values);

            $(this).trigger({
                type        : "$17.modules.checkboxs.click",
                eventData   : eventData
            });
        });
    }

    /**
     * 实现可配置样式的 checkbox 效果
     * @param obj :
     *     可配置参数：
     *          parent              : 事件范围
     *          style               : 选中样式，默认 w-checkbox-current
     *          checkboxTarget      : 元素 checkbox 选择器
     *          checkboxAllTarget   : 全选 checkbox 选择器
     *          values              : Array 对象。用于传递多组变量时使用，默认值为：["data-value"]
     *          例: 当 values 的值为 ["data-value", "data-wuhaha"] 时
     *              全选对象会有 data-values 和 data-wuhahas 属性值，将记录被选中时的对应变量值
     */
    function superBoy(obj){
        obj = obj || {};

        $17.extend(defaults, obj);

        var $allTarget  = $(defaults.parent + " " + defaults.checkboxAllTarget);
        var $nodeTarget = $(defaults.parent + " " + defaults.checkboxTarget);

        $allTarget.die().live("click", function(){
            var $self       = $(this);
            var eventData   = [];

            if($self.hasClass(defaults.style)){
                $self.removeClass(defaults.style);
                for(var i = 0, l = defaults.values.length; i < l; i++){
                    $self.attr(defaults.values[i] + "s", "");
                }
                $nodeTarget.removeClass(defaults.style);
            }else{
                $self.addClass(defaults.style);
                $nodeTarget.addClass(defaults.style);

                for(var i = 0, l = defaults.values.length; i < l; i++){
                    var __values = [];
                    $nodeTarget.each(function(index, value){
                        __values.push($(value).attr(defaults.values[i]));
                    });

                    $self.attr(defaults.values[i] + "s", __values);
                    eventData.push(__values);
                }
            }

            $(this).trigger({
                type        : "$17.modules.checkboxs.click",
                eventData   : eventData
            });
        });

        $nodeTarget.die().live("click", function(){
            $(this).toggleClass(defaults.style);

            var eventData = [];

            for(var i = 0, l = defaults.values.length; i < l; i++){
                var __count  = 0;
                var __values = [];
                $nodeTarget.each(function(index, value){
                    if($(value).hasClass(defaults.style)){
                        __count++;
                        __values.push($(value).attr(defaults.values[i]));
                    }
                });

                if(i == 0){
                    if($nodeTarget.length == __count){
                        $allTarget.addClass(defaults.style);
                    }else{
                        $allTarget.removeClass(defaults.style);
                    }
                }

                $allTarget.attr(defaults.values[i] + "s", __values);
                eventData.push(__values);
            }

            $(this).trigger({
                type        : "$17.modules.checkboxs.click",
                eventData   : eventData
            });
        });
    }

    //根据参数进行分发，调用 littleBoy or bigBoy or superBoy
    function checkboxs(){
        if(arguments.length == 1 && typeof arguments[0] == "string"){
            littleBoy.apply(this, arguments);
            return false;
        }

        if(arguments.length == 3){
            bigBoy.apply(this, arguments);
            return false;
        }

        if(arguments.length == 1 && typeof arguments[0] == "object"){
            superBoy.apply(this, arguments);
            return false;
        }

        return false;
    }

    $17.modules = $17.modules || {};

    $17.extend($17.modules, {
        checkboxs : checkboxs
    });
}($17));

//radio
(function($17){
    "use strict";

    var defaults = {
        parent  : null,
        targets : null,
        style   : "w-radio-current",
        values  : ["data-value"]
    };

    function bigBoy(parent, targets){
        var $target = $(parent + " " + targets);

        $target.die().live("click", function(){
            var $self = $(this);

            $target.removeClass(defaults.style);
            $self.addClass(defaults.style);
            var __values = $self.attr("data-value");
            $(parent).attr("data-value", __values);

            $self.trigger("$17.modules.radios.click");
        });

        return false;
    }

    function superBoy(obj){
        obj = obj || {};

        $17.extend(defaults, obj);

        var $target = $(defaults.parent + " " + defaults.targets);

        $target.die().live("click", function(){
            var $self = $(this);
            var $parent = $(obj.parent);

            $target.removeClass(defaults.style);
            $self.addClass(defaults.style);

            for(var i = 0, l = defaults.values.length; i < l; i++){
                $parent.attr(defaults.values[i], $self.attr(defaults.values[i]));
            }

            $self.trigger("$17.modules.radios.click");
        });

        return false;
    }

    //根据参数区分调用哪个函数
    function radios(){
        if(arguments.length == 2){
            bigBoy.apply(this, arguments);
            return false;
        }

        if(arguments.length == 1 && typeof arguments[0] == "object"){
            superBoy.apply(this, arguments);
            return false;
        }

        return false;
    }

    $17.modules = $17.modules || {};

    $17.extend($17.modules, {
        radios : radios
    });
}($17));

//tree
(function($17){
    "use strict";

    var defaults = {
        data        : null,
        showRoot    : true,
        target      : null,     //树根，生成树的对象
        allChecked  : false,    //是否默认全选状态
        allOpen     : false,    //是否默认全部展开
        canClose    : true,     //是否有展开搜索节点功能
        justLeaf    : false,    //是否只取叶子的值。false，意味着获取所有节点的值。
        text        : null,     //显示的具体文字字段
        children    : "children",  //孩子节点的字段
        values      : [],       //树要记录的信息值,
        setText     : function(text){ return text; }    //设置显示的值，如果需要处理，覆盖此函数
    };

    function forInTree(node){
        var k = '<li><a href="javascript:void(0);">';

        if(defaults.canClose && !$17.isBlank(node[defaults.children])){
            if(defaults.allOpen){
                k += '<span class="v-close w-icon-arrow"></span>';
            }else{
                k += '<span class="v-open w-icon-arrow w-icon-arrow-gRight"></span>';
            }
        }

        if(defaults.allChecked){
            k += $17.isBlank(node[defaults.children]) ? '<span class="w-checkbox w-checkbox-current v-left" ' : '<span class="w-checkbox w-checkbox-current v-node" ';
            for(var i = 0, l = defaults.values.length; i < l; i++){
                k += 'data-' + defaults.values[i] + '="' + node[defaults.values[i]] + '" ';
            }
            k += '></span>';
        }else{
            k += $17.isBlank(node[defaults.children]) ? '<span class="w-checkbox v-left" ' : '<span class="w-checkbox v-node" ';
            for(var i = 0, l = defaults.values.length; i < l; i++){
                k += 'data-' + defaults.values[i] + '="' + node[defaults.values[i]] + '" ';
            }
            k += '></span>';
        }
        k += '<span class="w-icon-md">' + defaults.setText(node[defaults.text]) + '</span></a>';

        if(!$17.isBlank(node[defaults.children])){
            k += defaults.allOpen ? '<ul>' : '<ul style="display: none;">';
            for(var i = 0, l = node[defaults.children].length; i < l; i++){
                k += forInTree(node[defaults.children][i]);
            }
            k += '</ul>';
        }

        return k + '</li>';
    }

    function checkTreeState($self){
        var $ul = $self.closest("ul");
        var $parent = $ul.parent("li");
        if($parent.length == 0){
            return false;
        }else{
            if($ul.find(".w-checkbox").size() == $ul.find(".w-checkbox.w-checkbox-current").size()){
                $parent.children("a").find("span.w-checkbox").addClass("w-checkbox-current");
            }else{
                $parent.children("a").find("span.w-checkbox").removeClass("w-checkbox-current");
            }
            checkTreeState($parent.children("a").find("span.w-checkbox"));
        }
    }

    function setValues(){
        var $target     = $(defaults.target);
        var datas       = $target.find(".w-checkbox.w-checkbox-current");
        var eventData   = [];
        for(var i = 0, l = defaults.values.length; i < l; i++){
            var _values = [];
            for(var j = 0, jl = datas.length; j < jl; j++){
                var $this = $(datas[j]);
                if(defaults.justLeaf){
                    if($this.hasClass("v-node")){
                        continue;
                    }
                    _values.push($this.attr("data-" + defaults.values[i]));
                }else{
                    _values.push($this.attr("data-" + defaults.values[i]));
                }
            }
            $target.attr("data-" + defaults.values[i] + "s", _values.toString());
            eventData.push(_values);
        }

        $target.trigger({
            type        : "$17.modules.tree.setValueDone",
            eventData   : eventData
        });
    }

    function tree(obj){
        obj = obj || {};

        $17.extend(defaults, obj);

        $(defaults.target).html('<ul>' + forInTree(defaults.data) + '</ul>');

        if(!defaults.showRoot){
            $(defaults.target).find("a:first").remove();
        }

        $(defaults.target + " .v-open").die().live("click", function(){
            $(this).removeClass("v-open w-icon-arrow-gRight").addClass("v-close").closest("li").children("ul").show();
            return false;
        });

        $(defaults.target + " .v-close").die().live("click", function(){
            $(this).removeClass("v-close").addClass("v-open w-icon-arrow-gRight").closest("li").children("ul").hide();
            return false;
        });

        $(defaults.target + " .w-checkbox").die().live("click", function(){
            var $self = $(this);

            if($self.hasClass("w-checkbox-current")){
                $self.closest("li").find(".w-checkbox").removeClass("w-checkbox-current");
            }else{
                $self.closest("li").find(".w-checkbox").addClass("w-checkbox-current");
            }

            checkTreeState($self);

            setValues();
            return false;
        });

        setValues();
    }

    $17.modules = $17.modules || {};

    $17.extend($17.modules, {
        tree : tree
    });
}($17));

//select
(function($17){
    "use strict";

    function select(target){
        var selectModel = new $17.Model({
            target  : target,
            $target : $(target)
        });
        selectModel.extend({
            set : function(key, text){
                var $dark = this.$target.find("span.content");
                $dark.attr("data-value", key);
                $dark.text(text);
                return false;
            },
            get : function(){
                return this.$target.find("span.content").attr("data-value");
            },
            getContents : function(){
                return $(this).children("ul");
            },
            init: function(){
                var $self = this;

                $self.$target.on({
                    click : function(){
                        $self.getContents.apply(this).show().parent().css({"zIndex": 12}).closest("li").css({"zIndex": 12});
                    },
                    mouseleave : function(){
                        $self.getContents.apply(this).hide().parent().css({"zIndex": 10}).closest("li").removeAttr("style");
                    }
                });
            }
        }).init();

        return selectModel;
    }

    function selectTree(target, treeTarget){
        var littleTree = select(target);
        littleTree.extend({
            getContents : function(){
                return $(treeTarget).children("ul");
            }
        });

        return littleTree;
    }

    $17.modules = $17.modules || {};

    $17.extend($17.modules, {
        select      : select,
        selectTree  : selectTree
    });
}($17));

//弹跳
jQuery.fn.rebound = function(count){
    function rebound($former, $anmt, _count){
        $anmt.animate({'top':'+=0'}, 1000)
            .animate({'top':'-=20', 'font-size':'+=3px'}, 80)
            .animate({'top':'+=20', 'font-size':'-=3px'}, 50)
            .animate({'top':'-=15', 'font-size':'+=2px'}, 100)
            .animate({'top':'+=15', 'font-size':'-=2px'}, 50)
            .animate({'top':'-=10', 'font-size':'+=1px'}, 50)
            .animate({'top':'+=10', 'font-size':'-=1px'}, 30)
            .animate({'top':'+=0'}, 500, function(){
                if(_count == 1){
                    $former.trigger("vox.animate.rebound.done");
                }else{
                    rebound($former, $anmt, _count - 1);
                }
            });
    }

    return this.each(function(){
        var $target = $(this);
        var $offset = $target.offset();

        var anmt = $target.clone();
        var guid = $17.guid("xxxx-xxxx-xxxx");
        anmt.attr("id", guid);

        $target.parent().append(anmt);
        anmt.css({
            position    : "absolute",
            top         : $offset.top,
            left        : $offset.left
        });

        $target.css('visibility', 'hidden');

        rebound($target, anmt, count);

        $target.on("vox.animate.rebound.done", function(){
            $target.css('visibility', 'visible');
            anmt.remove();
        });
    });
};

// 动态获取家长二维码
(function($){

    var JZT_QR_HREF = location.protocol + "//" + location.host + "/parentMobile/home/dimensionCodeIndex.vpage",
         CREATE_QR_HREF = "http://www.17zuoye.com/qrcode?m=",
         CREATE_SHORT_HREF = "/v1/parent/creatparentshorturl.vpage";

    var jzt_qr_cache = {};

    $17.get_jzt_qr = function(channelId, done){

        var jzt_qr_cache_key = "cid_" + channelId;

        if(jzt_qr_cache_key in jzt_qr_cache){
            return done(jzt_qr_cache[jzt_qr_cache_key]);
        }

        $.get(
            CREATE_SHORT_HREF,
            {
                url :  JZT_QR_HREF + "?cid=" + channelId
            },
            function(res){
                if(!res.success){
                    return $17.alert(res.message);
                }

                var jzt_download_short_url = CREATE_QR_HREF +  window.encodeURIComponent(res.shortUrl);

                done(jzt_download_short_url);

                jzt_qr_cache[jzt_qr_cache_key] =  jzt_download_short_url;

            }
        );

    };

})(jQuery);
