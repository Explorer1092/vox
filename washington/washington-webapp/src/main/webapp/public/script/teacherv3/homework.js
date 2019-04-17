(function($17){
    "use strict";
    var Levels = new $17.Life();
    Levels.include({
        _levels      : [],
        _focusLevel  : 0,
        _clazzIds    : [],
        _clazzNames  : [],
        _anchor      : null,
        _listening   : null,
        _onlySixthGradeClazz : false,
        getClazzIds  : function(){
            return this._clazzIds;
        },
        getClazzNames: function(){
            return this._clazzNames;
        },
        setDefaults  : function(level, clazzIds){
            var $levels = this;
            var $parent = $(this._anchor);
            var $target = $("li[data-level='" + level + "']");

            $target.radioClass("active");
            $levels._focusLevel = level;
            $parent.find("li[data-level-bycontent='" + level + "']").show();
            clazzIds = clazzIds.split(",");
            for(var i = 0; i < clazzIds.length; i++){
                $("span[data-value='" + clazzIds[i] + "'").trigger("click");
            }

            $("#Mayfly").remove();

            $("body").trigger({
                type      : "homework.clazzsChangeEvent",
                focusLevel: $levels._clazzIds,
                clazzIds  : $levels._clazzNames
            });
        },
        _building    : function(){
            var itsEmpty = true;
            for(var i = 0; i < this._levels.length; i++){
                if(this._levels[i].length > 0){
                    itsEmpty = false;
                    break;
                }
            }
            if(itsEmpty){
                var $not_clazz_tip = $("#not_clazz_tip");
                if(this._onlySixthGradeClazz){
                    $not_clazz_tip.find("div.w-noData-box").text("暂无暑期内容，去推荐普通作业吧");
                }
                $not_clazz_tip.show().siblings("div").remove();
                return false;
            }

            $(this._anchor).empty().html(template("t:levelsAndclazzs", {
                levels: this._levels
            }));
        },
        _setEvents   : function(){
            var $levels = this;
            var $parent = $(this._anchor);

            $parent.off("click");

            $parent.one("click", "li[data-level]", function(){
                $("#Mayfly").remove();
            });

            $("li[data-level-bycontent]").each(function(index, value){
                $17.modules.checkboxs({
                    parent           : "li[data-level-bycontent='" + $(value).attr("data-level-bycontent") + "']",
                    checkboxAllTarget: ".v-alltarget",
                    checkboxTarget   : ".v-targets",
                    values           : ["data-value", "data-clazzname"]
                });
            });

            $(".v-alltarget, .v-targets", $parent).on("$17.modules.checkboxs.click", function(event){
                $levels._clazzIds = event.eventData[0] || [];
                $levels._clazzNames = event.eventData[1] || [];

                $("#clazzNames").html("<div class='text'>"+$levels._clazzNames.toString()+"</div>");

                $("body").trigger({
                    type      : "homework.clazzsChangeEvent",
                    focusLevel: $levels._focusLevel,
                    clazzIds  : event.eventData[0]
                });
            });

            $parent.on("click", "li[data-level]", function(){
                var $self = $(this);
                var newLevel = $self.attr("data-level");

                if(newLevel == $levels._focusLevel){
                    return false;
                }else{
                    $self.radioClass("active");
                    $levels._focusLevel = newLevel;
                    $parent.find("li[data-level-bycontent]").hide();
                    $(".v-targets", $parent).removeClass("w-checkbox-current");
                    $(".v-alltarget", $parent).removeClass("w-checkbox-current").attr({
                        "data-values"    : "",
                        "data-clazznames": ""
                    });
                    $parent.find("li[data-level-bycontent='" + $levels._focusLevel + "']").show().find(".v-alltarget").trigger("click");

                    //这个需要用参数重构
                    $("#basicEn").empty();
                    $("#basicMath").empty();
                    $("#readings").empty();
                    $("#special").empty();
                    $("#exam").empty();
                }
            });
        },
        run          : function(){
            this._building();
            this._setEvents();
        },
        initialise   : function(data, anchor,onlySixthGradeClazz){
            this._levels = data;
            this._anchor = anchor;
            this._onlySixthGradeClazz = !!onlySixthGradeClazz;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getLevels: function(){
            return new Levels();
        }
    });
}($17));

(function($17){
    "usr script";

    var BasicEnglish = new $17.Life();
    BasicEnglish.include({
        _data     : null,
        _targetId : null,
        _bookId   : 0,
        _level    : 0,
        _unitId   : 0,
        _building : function(){
            $(this._targetId).html(template("t:basicEnglish", {
                bookId: this._bookId,
                unitId: this._unitId,
                level : this._level,
                data  : this._data
            })).show();
        },
        _setEvents: function(){
            var $basicEnglish = this;
            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on({
                click     : function(){
                    var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(data, {
                        title   : "预 览",
                        buttons : {},
                        position: { width: 740 },
                        close   : function(){
                            $('iframe').each(function(){
                                var win = this.contentWindow || this;
                                if(win.destroyHomeworkJavascriptObject){
                                    win.destroyHomeworkJavascriptObject();
                                }
                            });
                        }
                    });

                    return false;
                },
                mouseenter: function(){
                    $(this).find(".view").show();
                },
                mouseleave: function(){
                    $(this).find(".view").hide();
                }
            }, ".v-basicenglish-view");

            $parent.on("click", ".v-gametypes", function(){
                var $self = $(this);
                var lessonIndex = +$self.attr("data-lessonindex");
                var categorieindex = +$self.attr("data-categorieindex");

                if($self.hasClass("active")){
                    $self.removeClass("active");
                    $basicEnglish._data[lessonIndex].categories[categorieindex].count = 0;
                }else{
                    $self.addClass("active");
                    $basicEnglish._data[lessonIndex].categories[categorieindex].count = 1;
                }

                $("body").trigger("homework.contentChange");
            });

            $parent.on("click", ".v-basicEnglish-clean-btn", function(){
                for(var i = 0; i < $basicEnglish._data.length; i++){
                    for(var j = 0; j < $basicEnglish._data[i].categories.length; j++){
                        $basicEnglish._data[i].categories[j].count = 0;
                    }
                }
                $parent.find(".v-gametypes").removeClass("active");
                $("body").trigger("homework.contentChange");

                var subjectInfo = $(".t-hootArrow-list").find("li[data-unitid="+$basicEnglish._unitId+"]");
                subjectInfo.find(".hoot-box .c-4").attr("title","展开");
                subjectInfo.find(".hoot-box .c-4 span").removeClass("w-icon-arrow-top w-icon-arrow-topBlue");
                subjectInfo.find(".hoot-info").hide();

                return false;
            });
        },
        run       : function(){
            if(this._data.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building();
                this._setEvents();
            }
        },
        initialise: function(option){
            this._data = option.data.basicEnglish || [];
            this._bookId = option.bookId;
            this._unitId = option.data.unitId;
            this._targetId = option.targetId;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getBasicEnglish: function(){
            return new BasicEnglish();
        }
    });
}($17));

(function($17){
    "use strict";

    var BasicMath = new $17.Life();
    BasicMath.include({
        _data     : null,
        _targetId : null,
        _bookId   : 0,
        _level    : 0,
        _unitId   : 0,
        _building : function(){
            $(this._targetId).html(template("t:basicMath", {
                bookId: this._bookId,
                unitId: this._unitId,
                level : this._level,
                data  : this._data
            })).show();
        },
        _setEvents: function(){
            var $basicMath = this;
            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on({
                click     : function(){
                    var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(data, {
                        title: "预 览", buttons: {}, position: { width: 740 }, close: function(){
                            $('iframe').each(function(){
                                var win = this.contentWindow || this;
                                if(win.destroyHomeworkJavascriptObject){
                                    win.destroyHomeworkJavascriptObject();
                                }
                            });
                        }
                    });

                    return false;
                },
                mouseenter: function(){
                    $(this).find(".view").show();
                },
                mouseleave: function(){
                    $(this).find(".view").hide();
                }
            }, ".v-basicmath-view");

            $parent.on({
                click     : function(){
                    $(this).children("ul").show().parent().css({ "zIndex": 12 }).closest("li").css({ "zIndex": 12 });
                    return false;
                },
                mouseleave: function(){
                    $(this).children("ul").hide().parent().css({ "zIndex": 10 }).closest("li").removeAttr("style");
                }
            }, ".w-select");

            $parent.on("click", ".v-w-select-option", function(){
                var $self = $(this);
                var $parent = $self.closest("li.v-gametypes");
                var count = +$self.attr("data-value");
                var lessonindex = +$parent.attr("data-lessonindex");
                var pointindex = +$parent.attr("data-pointindex");

                if(count > 0){
                    $parent.addClass("active");
                    $parent.find("span.content").attr("data-value", count).text(count);
                }else{
                    $parent.removeClass("active");
                    $parent.find("span.content").attr("data-value", 0).text(0);
                }
                $self.closest("div.w-select").children("ul").hide().end().css({ "zIndex": 10 }).closest("li").removeAttr("style");

                $basicMath._data[lessonindex].points[pointindex].count = count;

                $("body").trigger("homework.contentChange");
                return false;
            });

            $parent.on("click", ".v-gametypes", function(){
                var $self = $(this);
                var lessonIndex = +$self.attr("data-lessonindex");
                var pointindex = +$self.attr("data-pointindex");

                if($self.hasClass("active")){
                    $self.removeClass("active");
                    $self.find("span.content").attr("data-value", 0).text(0);
                    $basicMath._data[lessonIndex].points[pointindex].count = 0;
                }else{
                    $self.addClass("active");
                    $self.find("span.content").attr("data-value", 10).text(10);
                    $basicMath._data[lessonIndex].points[pointindex].count = 10;
                }

                $("body").trigger("homework.contentChange");
            });

            $parent.on("click", ".v-basicMath-clean-btn", function(){
                for(var i = 0; i < $basicMath._data.length; i++){
                    for(var j = 0; j < $basicMath._data[i].points.length; j++){
                        $basicMath._data[i].points[j].count = 0;
                    }
                }
                $parent.find(".v-gametypes").removeClass("active").find("span.content").attr("data-value", 0).text(0);
                $("body").trigger("homework.contentChange");

                return false;
            });
        },
        run       : function(){
            if(this._data.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building();
                this._setEvents();
            }
        },
        initialise: function(option){
            this._data = option.data.basicMath || [];
            this._bookId = option.bookId;
            this._unitId = option.data.unitId;
            this._targetId = option.targetId;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getBasicMath: function(){
            return new BasicMath();
        }
    });
}($17));

(function($17){
    "use strict";

    var Special = new $17.Life();
    Special.include({
        _data     : null,
        _targetId : null,
        _bookId   : 0,
        _level    : 0,
        _unitId   : 0,
        _building : function(){
            $(this._targetId).empty().html(template("t:special", {
                data  : this._data,
                bookId: this._bookId,
                unitId: this._unitId
            })).show();
        },
        _setEvents: function(){
            var $special = this;
            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on({
                click     : function(){
                    var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                    $.prompt(data, {
                        title: "预 览", buttons: {}, position: { width: 740 }, close: function(){
                            $('iframe').each(function(){
                                var win = this.contentWindow || this;
                                if(win.destroyHomeworkJavascriptObject){
                                    win.destroyHomeworkJavascriptObject();
                                }
                            });
                        }
                    });

                    return false;
                },
                mouseenter: function(){
                    $(this).find(".view").show();
                },
                mouseleave: function(){
                    $(this).find(".view").hide();
                }
            }, ".v-special-view");

            $parent.on({
                click     : function(){
                    $(this).children("ul").show().parent().css({ "zIndex": 12 }).closest("li").css({ "zIndex": 12 });
                    return false;
                },
                mouseleave: function(){
                    $(this).children("ul").hide().parent().css({ "zIndex": 10 }).closest("li").removeAttr("style");
                }
            }, ".w-select");

            $parent.on("click", ".v-special-type-option", function(){
                var $self = $(this);
                var $parent = $self.closest("li.special_node");
                var $view = $parent.find("span.v-special-view");
                var gameurl = $view.attr("data-gameurl");
                var imageurl = $self.attr("data-imageurl");
                var type = +$self.attr("data-value");
                var lessonindex = +$parent.attr("data-lessonindex");
                var pointindex = +$parent.attr("data-pointindex");

                $view.find("img").attr("src", imageurl);
                $view.attr("data-gameurl", gameurl.substring(0, gameurl.length - 2) + type);
                var countList = $special._data[lessonindex].points[pointindex].dataTypeCountList;
                var newOptions = [];
                for(var i = 0; i < countList.length; i++){
                    for(var ii in countList[i]){
                        if(ii == type){
                            newOptions = countList[i][ii];
                            break;
                        }
                    }
                    if(newOptions.length != 0){
                        break;
                    }
                }
                var $options = $parent.find("div.w-select.selectNum").find("ul").eq(0).empty();

                $options.append('<li class="v-special-count-option" data-value="0"><a href="javascript:void(0);">0 题</a></li>');
                for(var i = 0; i < newOptions.length; i++){
                    $options.append('<li class="v-special-count-option" data-value="' + newOptions[i] + '"><a href="javascript:void(0);">' + newOptions[i] + ' 题</a></li>');
                }

                $parent.find("div.w-select.selectType").find("span.content").attr("data-value", type).text(type == 91 ? "预习 类型" : (type == 92 ? "巩固 类型" : "拓展 类型"));

                $self.closest("div.w-select").children("ul").hide().end().css({ "zIndex": 10 }).closest("li").removeAttr("style");

                $special._data[lessonindex].points[pointindex].dataType = type;

                return false;
            });

            $parent.on("click", ".v-special-count-option", function(){
                var $self = $(this);
                var $parent = $self.closest("li.special_node");
                var count = +$self.attr("data-value");
                var lessonindex = +$parent.attr("data-lessonindex");
                var pointindex = +$parent.attr("data-pointindex");

                if(count > 0){
                    $parent.addClass("active");
                    $parent.find("div.w-select.selectNum").find("span.content").attr("data-value", count).text(count + " 题");
                }else{
                    $parent.removeClass("active");
                    $parent.find("div.w-select.selectNum").find("span.content").attr("data-value", 0).text("0 题");
                }
                $self.closest("div.w-select").children("ul").hide().end().css({ "zIndex": 10 }).closest("li").removeAttr("style");

                $special._data[lessonindex].points[pointindex].count = count;

                $("body").trigger("homework.contentChange");

                return false;
            });

            $parent.on("click", ".special_node", function(){
                var $self = $(this);
                var lessonIndex = +$self.attr("data-lessonindex");
                var pointindex = +$self.attr("data-pointindex");

                if($self.hasClass("active")){
                    $self.removeClass("active");
                    $self.find("div.w-select.selectNum").find("span.content").attr("data-value", 0).text("0 题");
                    $special._data[lessonIndex].points[pointindex].count = 0;
                }else{
                    $self.addClass("active");
                    $self.find("div.w-select.selectNum").find("span.content").attr("data-value", 5).text("5 题");
                    $special._data[lessonIndex].points[pointindex].count = 5;
                }

                $("body").trigger("homework.contentChange");
            });

            $parent.on("click", ".v-special-clear-btn", function(){
                for(var i = 0; i < $special._data.length; i++){
                    for(var j = 0; j < $special._data[i].points.length; j++){
                        $special._data[i].points[j].count = 0;
                    }
                }
                $parent.find(".special_node").removeClass("active").find("div.selectNum").find("span.content").attr("data-value", 0).text("0 题");
                $("body").trigger("homework.contentChange");

                return false;
            });
        },
        run       : function(){
            if(this._data.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building();
                this._setEvents();
            }
        },
        initialise: function(option){
            this._data = option.data.special || [];
            this._bookId = option.bookId;
            this._unitId = option.data.unitId;
            this._targetId = option.targetId;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getSpecial: function(){
            return new Special()
        }
    });
}($17));

(function($17){
    "use strict";

    var Reading = new $17.Life();
    Reading.include({
        _data         : null,
        _searchData   : [],
        _searchTotal  : 0,
        _targetId     : null,
        _bookId       : 0,
        _level        : 0,
        _unitId       : 0,
        _point        : [],
        _selectTree   : null,
        _select       : null,
        _readingPreFix: "",
        _searchUrl    : "/teacher/homework/reading/search.vpage",
        _searchReading: function(pageIndex, level){
            var $reading = this;

            var readingName = $("input.v-readingName").val();
            var num1 = $("input.v-number-1").val();
            var num2 = $("input.v-number-2").val();
            num1 = num1 == "" ? null : num1;
            num2 = num2 == "" ? null : num2;

            if(Math.max(num1, num2) > 99999){
                $17.alert("词汇量过大，请输入0 ～ 99999以内的数字。");
                return false;
            }

            $("#searchList").html(template("t:加载中", {}));
            $("#readingPage").hide();

            var __data = {
                point        : $("#topicTree").attr("data-keys"),
                readingLevel : level,
                readingName  : readingName,
                maxWordsCount: Math.max(num1, num2),
                minWordsCount: Math.min(num1, num2),
                pageNum      : pageIndex,
                pageSize     : 8,
                unitId       : this._unitId,
                readingIds   : []
            };

            var reading = $reading._data;
            for(var i = 0, l = reading.length; i < l; i++){
                __data.readingIds.push(reading[i].id);
            }
            __data.readingIds = __data.readingIds.toString();

            App.postJSON($reading._searchUrl, __data, function(data){
                if(data.unitId == $reading._unitId){
                    if(data.success){
                        $reading._searchTotal = data.totalPage;
                        $reading._searchData = data.readingList;

                        if(data.readingList.length > 0){
                            $(".v-pageNum").html("<span>" + pageIndex + "</span>");
                            $(".v-pageTotal").html("<span>" + data.totalPage + "</span>");
                            $("#readingPage").show();
                        }
                    }

                    $("#searchList").html(template("t:theReading", {
                        data         : data.readingList || [],
                        searchMessage: "没有符合条件的阅读..."
                    }));
                }
            });
        },
        _building     : function(){
            $(this._targetId).empty().html(template("t:reading", {
                data  : this._data,
                bookId: this._bookId,
                unitId: this._unitId
            })).show();

            $("#readingList").empty().html(template("t:theReading", {
                data         : this._data,
                bookId       : this._bookId,
                unitId       : this._unitId,
                readingPreFix: this._readingPreFix
            }));

            //生成话题树
            $17.modules.tree({
                data   : this._point,
                target : "#topicTree",
                allOpen: true,
                text   : "title",
                values : ["key"],
                setText: function(text){
                    text = text || "";
                    return text.indexOf("话题-") != -1 ? text.split("话题-")[0] + text.split("话题-")[1] : text;
                }
            });

            //生成树下拉
            this._selectTree = $17.modules.selectTree("#selectTree", "#topicTree");

            //生成难度下拉
            this._select = $17.modules.select("#difficulty");
        },
        _setEvents    : function(){
            var $reading = this;
            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on("click", ".v-reading-view-btn", function(){
                var gameDataURL = "/appdata/flash/Reading/obtain-ENGLISH-" + $(this).attr("data-readingid") + ".vpage";
                var $flashurl = $("#flashurl");
                var flashvars = {
                    isPreview   : 0,
                    gameDataURL : $flashurl.attr("data-webappbaseurl") + gameDataURL,
                    nextHomeWork: "nextHomeWork",
                    tts_url     : $("#ttsurl").attr("data-ttsurl"),
                    isTeacher   : 1,
                    imgDomain   : $flashurl.attr("data-imgdomain"),
                    domain      : $flashurl.attr("data-webappbaseurl")
                };

                $.prompt(template("t:readingView", {}), {
                    title   : "阅读预览",
                    position: { width: 750 },
                    buttons : {},
                    loaded  : function(){
                        $("#showViewContent").getFlash({
                            movie    : $flashurl.attr("data-falshurl"),
                            flashvars: flashvars
                        });
                    }
                });

                return false;
            });

            $("#searchList").on("click", "li", function(){
                var $self = $(this);
                var $parent = $self.closest("ul");
                if($reading._data && $reading._data.length > 10){
                    $17.alert("每单元最多选择10个绘本");
                    return false;
                }
                $self.fly({
                    target: "#readingList",
                    border: '2px #189CFB solid',
                    time  : 200
                });

                var _theBoy = $reading._searchData[$self.attr("data-readingindex")];
                _theBoy.isChecked = true;

                $reading._data.push(_theBoy);
                $self.remove();
                if($parent.find("li").size() < 5){
                    $reading._searchReading($(".v-search-btn").attr("data-pageindex"), $reading._select.get());
                }

                $("#readingList ul").prepend(template("t:flyReading", {
                    data         : _theBoy,
                    readingIndex : $reading._data.length - 1,
                    readingPreFix: $reading._readingPreFix
                }));

                $("body").trigger("homework.contentChange");

                return false;
            });

            $parent.on("click", ".v-the-reading", function(){
                var $self = $(this);
                var readingIndex = $self.attr("data-readingindex");

                if($self.hasClass("active")){
                    $self.removeClass("active");
                    $reading._data[readingIndex].isChecked = false;
                }else{
                    $self.addClass("active");
                    $reading._data[readingIndex].isChecked = true;
                }

                $("body").trigger("homework.contentChange");
            });

            $parent.on("click", ".w-offEvent", function(){
                var $self = $(this);
                $self.removeClass("w-offEvent").addClass("w-onEvent");
                $self.find("span.w-icon-md").text("收起");
                $("#searchRegional").show();
                if($("#searchList").find("li").size() != 8){
                    $(".v-search-btn").trigger("click");
                }

                return false;
            });
            $parent.on("click", ".w-onEvent", function(){
                var $self = $(this);
                $self.removeClass("w-onEvent").addClass("w-offEvent");
                $self.find("span.w-icon-md").text("更多");
                $("#searchRegional").hide();

                return false;
            });

            $parent.on("click", ".v-search-btn", function(){
                var $self = $(this);
                var pageIndex = $self.attr("data-pageindex");
                if($17.isBlank(pageIndex)){
                    pageIndex = 1;
                    $self.attr("data-pageindex", 1);
                }
                $reading._searchReading(pageIndex, $reading._select.get());

                return false;
            });

            $parent.on("click", ".v-prev", function(){
                var pageIndex = +$(".v-search-btn").attr("data-pageindex");
                if(pageIndex >= 2){
                    pageIndex--;
                    $(".v-search-btn").attr("data-pageindex", pageIndex);
                    $reading._searchReading(pageIndex, $reading._select.get());
                }
                return false;
            });

            $parent.on("click", ".v-next", function(){
                var pageIndex = +$(".v-search-btn").attr("data-pageindex");
                if(pageIndex < $reading._searchTotal){
                    pageIndex++;
                    $(".v-search-btn").attr("data-pageindex", pageIndex);
                    $reading._searchReading(pageIndex, $reading._select.get());
                }
                return false;
            });

            //监听树设置事件
            $("#selectTree").on("$17.modules.tree.setValueDone", function(event){
                if($reading._selectTree == null){
                    return false;
                }

                var keys = event.eventData[0];
                if(keys.length == 1){
                    if(keys[0].length == 0){
                        $reading._selectTree.set("", "请选择");
                    }else{
                        $reading._selectTree.set("", "已选择了 1 个话题");
                    }
                }else{
                    $reading._selectTree.set("", "已选择了 " + keys.length + " 个话题");
                }
            });

            //难度下拉设置值
            $(".v-select-type", $parent).on("click", function(){
                if($reading._select == null){
                    return false;
                }

                var $self = $(this);
                $reading._select.set($self.attr("data-type"), $self.find("span").text());
            });
        },
        run           : function(){
            if(this._data.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building();
                this._setEvents();
            }
        },
        initialise    : function(option){
            this._data = option.data.reading || [];
            this._bookId = option.bookId;
            this._unitId = option.data.unitId;
            this._targetId = option.targetId;
            this._point = option.point;
            this._readingPreFix = option.readingPreFix;
            this._searchUrl = option.searchUrl || this._searchUrl;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getReading: function(){
            return new Reading();
        }
    });
}($17));

(function($17){
    "use strict";

    var Exam = new $17.Life();
    Exam.include({
        _loadExamInitialize : false,
        _display  : false,
        _subject  : null,
        _examCart : null,
        _examCache: null,
        _targetId : null,
        _bookId   : 0,
        _level    : 0,
        _clazzIds : [],
        _unitId   : 0,
        _examUrl  : "/teacher/homework/book/exam/",
        setDisplay: function(display){
            this._display = display;
        },
        _building : function(type){
            if(this._display){
                if(type == "refresh"){
                    $(this._targetId).html(template("t:exam", this._examCache[this._unitId]));
                }else{
                    $("#exam_centent").html(template("t:theExam", this._examCache[this._unitId]));
                }
            }else{
                if(type == "refresh"){
                    $(this._targetId).html(template("t:exam", this._examCache[this._unitId])).show();
                }else{
                    $("#exam_centent").html(template("t:theExam", this._examCache[this._unitId])).show();
                }

                /*$("#examImgUrl").on("error", function(){
                    $("#failedToLoad").show();
                    $("#examImgUrl").remove();
                });*/
                if(this._examCache[this._unitId]){
                    var examId = this._examCache[this._unitId].currentExam.id;

                    if(!$17.isBlank(examId) && this._loadExamInitialize){
                        var node = document.getElementById('examImgUrl');
                        vox.exam.render(node, 'teacher_preview', {
                            ids       : [examId],
                            imgDomain : this.imgDomain,
                            env       : this.env
                        });
                    }else{
                        $('#examImgUrl').html('<div class="w-noData-block">如果遇到同步习题加载问题，建议使用猎豹浏览器重新打开网站，<a href="//cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank" style="color: #39f;">点击下载</a></div>');
                        $17.voxLog({
                            module: 'vox_exam_create',
                            op:'exam_view_error'
                        });
                    }
                }
            }
        },
        _setEvents: function(){
            var $exam = this;
            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on("click", "#feedback", function(){
                $.prompt("<div><span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span><textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea><p class='init text_red'></p></div>", {
                    title  : "错题反馈",
                    focus  : 1,
                    buttons: { "取消": false, "提交": true },
                    submit : function(e, v){
                        if(v){
                            var feedbackContent = $("#feedbackContent");
                            if($17.isBlank(feedbackContent.val())){
                                feedbackContent.siblings(".init").html("错题反馈不能为空。");
                                feedbackContent.focus();
                                return false;
                            }
                            var feedbackType = 0;
                            if($exam._subject.toUpperCase() == "ENGLISH"){
                                feedbackType = 1;
                            }
                            if($exam._subject.toUpperCase() == "MATH"){
                                feedbackType = 2;
                            }

                            $.post("/project/examfeedback.vpage", {
                                feedbackType: feedbackType,
                                examId      : $exam._examCache[$exam._unitId].currentExam.id,
                                content     : feedbackContent.val()
                            }, function(data){
                                if(data.success){
                                    $17.alert("提交成功，感谢您的支持！");
                                }
                            });
                        }
                    }
                });

                return false;
            });

            $parent.on("click", ".v-examCart-point", function(){
                $(".v-examCart-point", $parent).removeClass("select");

                var $self = $(this);
                var newPoint = $self.text();
                var $target = $exam._examCache[$exam._unitId];
                $target.showType = $target.showType == newPoint ? "all" : newPoint;
                if($target.showType == "all"){
                    var exams = [];
                    var newData = $target.data;
                    for(var i = 0, l = newData.length; i < l; i++){
                        exams = exams.concat(newData[i].exams);
                    }
                    $target.examList = exams;
                    $target.currentExam = exams[0];
                }else{
                    $self.addClass("select");
                    var newData = $target.data;
                    for(var i = 0, l = newData.length; i < l; i++){
                        if(newData[i].point == newPoint){
                            $target.examList = [].concat(newData[i].exams);
                            $target.currentExam = $target.examList[0];
                            break;
                        }
                    }
                }
                $exam._building("");
            });

            $parent.on("click", ".v-examCart-index", function(){
                var $self = $(this);
                var $target = $exam._examCache[$exam._unitId];
                var index = $self.attr("data-examindex");

                if($target.showType != $target.examCart[index].point){
                    $target.showType = $target.examCart[index].point;

                    var newData = $target.data;
                    for(var i = 0, l = newData.length; i < l; i++){
                        if(newData[i].point == $target.showType){
                            $target.examList = $.extend([], newData[i].exams);
                        }
                    }
                }
                $target.currentExam = $.extend(true, {}, $target.examCart[index]);

                $exam._building("");

                return false;
            });

            $parent.on("click", "#v-exam-empty-btn", function(){
                while($exam._examCart.length > 0){
                    $exam._examCart.pop();
                }
                $exam._building("");
                $("body").trigger("homework.contentChange");

                var subjectInfo = $(".t-hootArrow-list").find("li[data-unitid="+$exam._unitId+"]");
                subjectInfo.find(".hoot-box .c-4").attr("title","展开");
                subjectInfo.find(".hoot-box .c-4 span").removeClass("w-icon-arrow-top w-icon-arrow-topBlue");
                subjectInfo.find(".hoot-info").hide();
            });

            $parent.on("click", ".v-examCart-page-btn", function(){
                var $target = $exam._examCache[$exam._unitId];
                $target.currentExam = $target.examList[$(this).attr("data-index")];
                $exam._building("");

                return false;
            });

            $parent.on("click", ".v-putin", function(){
                var $target = $exam._examCache[$exam._unitId];
                if($target.examCart.length == 10){
                    $17.alert("为了确保学生能按时完成，每单元最多可以选取10道同步习题。");
                    return false;
                }
                $target.examCart.push($.extend(true, {}, $target.currentExam));
                $exam._building("");

                $("body").trigger("homework.contentChange");

                return false;
            });

            $parent.on("click", ".v-takeaway", function(){
                var index = null;
                var $target = $exam._examCache[$exam._unitId];
                for(var i = 0, l = $target.examCart.length; i < l; i++){
                    if($target.examCart[i].id == $target.currentExam.id){
                        index = i;
                        break;
                    }
                }
                $target.examCart.splice(index, 1);
                $exam._building("");

                $("body").trigger("homework.contentChange");

                return false;
            });

            $parent.on("focus", "#inputPageNo", function(){
                $(this).val("");

                return false;
            });
            $parent.on("keyup", "#inputPageNo", function(){
                if(/\D/g.test(this.value)){
                    this.value = this.value.replace(/\D/g, '');
                }
                return false;
            });
            $parent.on("click", "#goBtn20141120112130", function(){
                var $target = $exam._examCache[$exam._unitId];
                var pageNo = +$("#inputPageNo").val();
                if(!$17.isNumber(pageNo)){
                    return false;
                }
                if(pageNo <= 0 || pageNo > $target.examList.length){
                    return false;
                }
                $target.currentExam = $target.examList[pageNo - 1];
                $exam._building("");

                return false;
            });

            $parent.on("click", "#examImgUrl", function(){
                var $self = $(this);
                if($self.attr("data-hasaudio") != "false"){
                    $self.siblings("span").html("").jmp3({
                        autoStart: 'true', file: $self.attr("data-audiourl"), width: "1", height: "1"
                    });
                }
            });
        },
        _run      : function(){
            if(this._examCache[this._unitId].examList.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building("refresh");
                this._setEvents();
            }
        },
        initialise: function(option){
            var $exam = this;

            this._subject = option.subject;
            this._examCart = option.data.examCart || [];
            this._level = option.level;
            this._clazzIds = option.clazzIds;
            this._bookId = option.bookId;
            this._unitId = option.data.unitId;
            this._targetId = option.targetId;
            this._examUrl = option.examUrl || this._examUrl;
            this._loadExamInitialize = option.examCoreInit;
            this._examCache = examCache || {};

            if($17.isBlank(this._examCache[this._unitId]) && this._subject.toUpperCase() == "ENGLISH"){
                if(this._level <= 2){
                    //1,2年级不显示应试
                    return false;
                }
                $($exam._targetId).html(template("t:加载中", {})).show();
                this._examUrl = "/teacher/homework/book/exam.vpage";
                var _url = this._clazzIds.length > 0 ? this._examUrl + "?unitId=" + this._unitId + "&clazzId=" + this._clazzIds.toString() : this._examUrl + "?unitId=" + this._unitId;

                $.get(_url, function(data){
                    var examList = data.examList;
                    //整理数据，按显示要求排序。英语知识点优先，然后是中文，最后是带斜杠的知识点
                    var character = [];
                    var characterKeypoint = [];
                    var phonetic = [];
                    var phoneticKeypoint = [];
                    var word = [];
                    var wordKeypoint = [];
                    var grammar = [];
                    var grammarKeypoint = [];
                    var topic = [];
                    var topicKeypoint = [];

                    for(var i = 0, l = examList.length; i < l; i++){
                        var pointType = examList[i].pointType;
                        /*var pointType = examList[i].point.split(":")[0];
                        var _ = examList[i].point.split(":");
                        _.shift();
                        examList[i].point = _.join(":");*/

                        switch(pointType){
                            case "CHARACTERS":
                                if(examList[i].keyPoint){
                                    characterKeypoint.push(examList[i]);
                                }else{
                                    character.push(examList[i]);
                                }
                                break;
                            case "PHONETICS":
                                if(examList[i].keyPoint){
                                    phoneticKeypoint.push(examList[i]);
                                }else{
                                    phonetic.push(examList[i]);
                                }
                                break;
                            case "WORDS":
                                if(examList[i].keyPoint){
                                    wordKeypoint.push(examList[i]);
                                }else{
                                    word.push(examList[i]);
                                }
                                break;
                            case "GRAMMARS":
                                if(examList[i].keyPoint){
                                    grammarKeypoint.push(examList[i]);
                                }else{
                                    grammar.push(examList[i]);
                                }
                                break;
                            case "TOPICS":
                                if(examList[i].keyPoint){
                                    topicKeypoint.push(examList[i]);
                                }else{
                                    topic.push(examList[i]);
                                }
                                break;
                        }
                    }
                    character = $17.listSort(character, "point", "asc");
                    characterKeypoint = $17.listSort(characterKeypoint, "point", "asc");
                    phonetic = $17.listSort(phonetic, "point", "asc");
                    phoneticKeypoint = $17.listSort(phoneticKeypoint, "point", "asc");
                    word = $17.listSort(word, "point", "asc");
                    wordKeypoint = $17.listSort(wordKeypoint, "point", "asc");
                    grammar = $17.listSort(grammar, "point", "asc");
                    grammarKeypoint = $17.listSort(grammarKeypoint, "point", "asc");
                    topic = $17.listSort(topic, "point", "asc");
                    topicKeypoint = $17.listSort(topicKeypoint, "point", "asc");

                    var newData = $.merge(characterKeypoint, character);
                    newData = $.merge(newData, phoneticKeypoint);
                    newData = $.merge(newData, phonetic);
                    newData = $.merge(newData, wordKeypoint);
                    newData = $.merge(newData, word);
                    newData = $.merge(newData, grammarKeypoint);
                    newData = $.merge(newData, grammar);
                    newData = $.merge(newData, topicKeypoint);
                    newData = $.merge(newData, topic);

                    var points = [];
                    var exams = [];
                    for(var i = 0, l = newData.length; i < l; i++){
                        points.push(newData[i].point);
                        for(var j = 0, jl = newData[i].exams.length; j < jl; j++){
                            newData[i].exams[j].point = newData[i].point;
                        }
                        exams = exams.concat(newData[i].exams);
                    }


                    $exam._examCache[$exam._unitId] = {
                        data       : newData,
                        points     : points,
                        examCart   : $exam._examCart,
                        examList   : exams,
                        showType   : "all",
                        currentExam: exams[0] || {},
                        unitId     : $exam._unitId
                    };

                    $exam._run();
                });
            }else if($17.isBlank(this._examCache[this._unitId]) && this._subject.toUpperCase() == "MATH"){
                this._examUrl = "/math/book/exam/";
                $($exam._targetId).html(template("t:加载中", {})).show();
                var _url = $exam._clazzIds.length > 0 ? this._examUrl + $exam._unitId + ".vpage?clazzId=" + $exam._clazzIds.toString() : this._examUrl + $exam._unitId + ".vpage";

                $.get(_url, function(data){
                    var points = [];
                    var exams = [];
                    var examList = data.examList;
                    for(var i = 0, l = examList.length; i < l; i++){
                        points.push(examList[i].point);
                        for(var j = 0, jl = examList[i].exams.length; j < jl; j++){
                            examList[i].exams[j].point = examList[i].point;
                        }
                        exams = exams.concat(examList[i].exams);
                    }

                    $exam._examCache[$exam._unitId] = {
                        data       : examList,
                        points     : points,
                        examCart   : $exam._examCart,
                        examList   : exams,
                        showType   : "all",
                        currentExam: exams[0] || {},
                        unitId     : $exam._unitId
                    };

                    $exam._run();
                });
            }else{
                $exam._run();
            }
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getExam: function(){
            return new Exam();
        }
    });
}($17));

(function($17){
    "use strict";

    var Confirm = new $17.Life();
    Confirm.include({
        _data       : null,
        _bookId     : null,
        _targetId   : null,
        _statistical: null,
        _subject    : null,
        _building   : function(){
            $(this._targetId).empty().html(template("t:confirm", {
                data       : this._data,
                bookId     : this._bookId,
                statistical: this._statistical,
                subject    : this._subject
            })).show();

            $(".confirm-spring").each(function(index, value){
                var $target = $(value);
                $target.find("li:lt(2)").show();
                if($target.find("li").size() > 2){
                    $target.closest("div.w-base").find("div.w-base-more").show();
                }
            });
        },
        _setEvents  : function(){
            var $confirm = this;

            var $parent = $(this._targetId);

            $parent.off("click");

            $parent.on("click", ".w-offEvent", function(){
                var $self = $(this);
                $self.removeClass("w-offEvent").addClass("w-onEvent").find("span.w-icon-md").text("收起");
                $self.closest("div.t-homework-confirm-vacation").find(".confirm-spring").find("li").show();

                return false;
            });

            $parent.on("click", ".w-onEvent", function(){
                var $self = $(this);
                $self.removeClass("w-onEvent").addClass("w-offEvent").find("span.w-icon-md").text("展开");
                $self.closest("div.t-homework-confirm-vacation").find(".confirm-spring").find("li").hide().end().find("li:lt(2)").show();

                return false;
            });

            $parent.on("click", ".v-confirm-basicenglish-view-btn", function(){
                $17.voxLog({
                    module: "VacationHomeworkSecondConfirmLog",
                    op    : "VHSC-BasicPreview"
                });

                var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                $.prompt(data, {
                    title   : "预 览",
                    buttons : {},
                    position: { width: 740 },
                    close   : function(){
                        $('iframe').each(function(){
                            var win = this.contentWindow || this;
                            if(win.destroyHomeworkJavascriptObject){
                                win.destroyHomeworkJavascriptObject();
                            }
                        });
                    }
                });

                return false;
            });

            $parent.on("click", ".v-confirm-basicmath-view-btn", function(){
                $17.voxLog({
                    module: "VacationHomeworkSecondConfirmLog",
                    op    : "VHSC-MathBasicPreview"
                });

                var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                $.prompt(data, {
                    title: "预 览", buttons: {}, position: { width: 740 }, close: function(){
                        $('iframe').each(function(){
                            var win = this.contentWindow || this;
                            if(win.destroyHomeworkJavascriptObject){
                                win.destroyHomeworkJavascriptObject();
                            }
                        });
                    }
                });

                return false;
            });

            $parent.on("click", ".v-confirm-reading-view-btn", function(){
                $17.voxLog({
                    module: "VacationHomeworkAlllog",
                    op    : "VHSC-ReadingPreview"
                });
                var gameDataURL = "/appdata/flash/Reading/obtain-ENGLISH-" + $(this).attr("data-readingid") + ".vpage";
                var $flashurl = $("#flashurl");
                var flashvars = {
                    isPreview   : 0,
                    gameDataURL : $flashurl.attr("data-webappbaseurl") + gameDataURL,
                    nextHomeWork: "closeReviewWindow",
                    tts_url     : $("#ttsurl").attr("data-ttsurl"),
                    isTeacher   : 1,
                    imgDomain   : $flashurl.attr("data-imgdomain"),
                    domain      : $flashurl.attr("data-webappbaseurl")
                };

                $.prompt(template("t:readingView", {}), {
                    title   : "阅读预览",
                    position: { width: 750 },
                    buttons : {},
                    loaded  : function(){
                        $("#showViewContent").getFlash({
                            movie    : $flashurl.attr("data-falshurl"),
                            flashvars: flashvars
                        });
                    }
                });

                return false;
            });

            $parent.on("click", ".v-confirm-special-view-btn", function(){
                var data = '<iframe class="vox17zuoyeIframe" src="' + $(this).attr("data-gameurl") + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                $.prompt(data, {
                    title: "预 览", buttons: {}, position: { width: 740 }, close: function(){
                        $('iframe').each(function(){
                            var win = this.contentWindow || this;
                            if(win.destroyHomeworkJavascriptObject){
                                win.destroyHomeworkJavascriptObject();
                            }
                        });
                    }
                });

                return false;
            });

            $parent.on("click", ".v-confirm-exam-view-btn", function(){
                $17.voxLog({
                    module: "VacationHomeworkAlllog",
                    op    : "VHSC-ExamPreview"
                });

                if($confirm._subject.toUpperCase() == "ENGLISH"){
                    var examIds = [];
                    for(var i = 0; i < $confirm._data.length; i++){
                        for(var ii = 0; ii < $confirm._data[i].examCart.length; ii++){
                            examIds.push($confirm._data[i].examCart[ii].id);
                        }
                    }
                    var dummy = new $confirm._iframeform('/exam/paper/questionpreview.vpage');
                    dummy.addParameter('examIds', examIds.toString());
                    dummy.addParameter('bookId', $confirm._bookId);
                    dummy.send();
                }else{
                    var examIds = [];
                    for(var i = 0; i < $confirm._data.length; i++){
                        for(var ii = 0; ii < $confirm._data[i].examCart.length; ii++){
                            examIds.push($confirm._data[i].examCart[ii].id);
                        }
                    }

                    var dummy = new $confirm._iframeform('/exam/paper/questionpreview.vpage');
                    dummy.addParameter('examIds', examIds.toString());
                    dummy.addParameter('bookId', $confirm._bookId);
                    dummy.send();
                    /*App.postJSON("/math/book/preview.vpage", {
                        paperJson: examIds.toString(),
                        clazzId  : 999999
                    }, function(data){
                        $.prompt('<iframe src="/teacher/exam/viewpaper.vpage?paperId=' + data.paperId + '" width="940" marginwidth="0" height="450" marginheight="0" scrolling="no" frameborder="0"></iframe>', {
                            title   : "预览试卷",
                            position: { width: 960 },
                            buttons : { "关闭": false }
                        });
                    });*/
                }

                return false;
            });

            $("#v-leave-message").on("keyup", function(){
                var _ = $(this).val().length;
                if(_ == 0){
                    $("#v-leave-message-num").text("100").removeClass("w-red");
                }else{
                    $("#v-leave-message-num").text(100 - _);
                    if(100 - _ < 0){
                        $("#v-leave-message-num").addClass("w-red");
                    }else{
                        $("#v-leave-message-num").removeClass("w-red");
                    }
                }
                $(this).attr("data-length", 100 - _);
            });
        },
        _iframeform : function(url){
            var object = this;
            object.time = new Date().getTime();
            object.form = $('<form action="' + url + '" target="iframe' + object.time + '" method="post" style="display:none;" id="form' + object.time + '"></form>');

            object.addParameter = function(parameter, value){
                $("<input type='hidden' />").attr("name", parameter).attr("value", value).appendTo(object.form);
            };

            object.send = function(){
                var iframe = $('<iframe class="vox17zuoyeIframe" data-time="' + object.time + '" style="display:none;" id="iframe' + object.time + '"></iframe>');
                $("body").append(iframe);
                $("body").append(object.form);
                object.form.submit();
                iframe.load(function(){
                    $('#form' + $(this).data('time')).remove();
                    $(this).remove();
                });
                return iframe;
            };
        },
        run         : function(){
            if(this._data.length == 0){
                $(this._targetId).empty().hide();
            }else{
                this._building();
                this._setEvents();
            }
        },
        initialise  : function(option){
            this._data = option.data;
            this._targetId = option.targetId;
            this._bookId = option.bookId;
            this._statistical = option.statistical;
            this._subject = option.subject;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getConfirm: function(){
            return new Confirm();
        }
    });
}($17));

(function($17){
    "use strict";

    var Contents = new $17.Life();
    Contents.include({
        _basicEnglish        : null,            //当前英语基础对象
        _basicMath           : null,            //当前数学基础对象
        _reading             : null,            //当前阅读绘本对象
        _special             : null,            //当前专项训练对象
        _exam                : null,            //当前随堂练习对象
        _confirm             : null,            //当前确认部分对象
        _subject             : null,
        _focusLevel          : null,
        _clazzIds            : null,
        _bookId              : null,
        _bookName            : null,
        _color               : "",
        _imgUrl              : "",
        _latestVersion       : null,
        _viewContent         : "",
        _adjust              : false,           //是否是调整练习
        _point               : [],              //阅读绘本搜索知识点下拉
        _units               : [],
        _bookInfoId          : "#bookInfo",
        _basicEnId           : "#basicEn",
        _basicMathId         : "#basicMath",
        _readingId           : "#readings",
        _readingPreFix       : "",
        _specialId           : "#special",
        _examId              : "#exam",
        _examUrl             : null,
        _readingSearchUrl    : null,
        _confirmId           : "#confirm",
        _statistical         : {                //统计数据
            basicEnglishCount: 0,
            basicEnglishTime : 0,
            basicMathCount   : 0,
            basicMathTime    : 0,
            readingCount     : 0,
            readingTime      : 0,
            specialCount     : 0,
            specialTime      : 0,
            examCount        : 0,
            examTime         : 0,
            totalCount       : 0,
            totalTime        : 0,
            unitCount        : 0,
            unitIds          : []
        },
        _loadExCoreInit      : false,
        getStatisticalInfo   : function(unitId){
            var _return = this._statistical[unitId + "_info"];
            if($17.isBlank(_return)){
                this._statistical[unitId + "_info"] = {
                    basicEnglishCount: 0,
                    basicEnglishTime : 0,
                    basicMathCount   : 0,
                    basicMathTime    : 0,
                    readingCount     : 0,
                    readingTime      : 0,
                    specialCount     : 0,
                    specialTime      : 0,
                    examCount        : 0,
                    examTime         : 0
                };
                return this._statistical[unitId + "_info"];
            }else{
                return _return;
            }
        },
        _calculateAll        : function(){
            var __units = this._units;
            this._statistical.unitIds = [];
            for(var i = 0, l = __units.length; i < l; i++){
                this._statistical.unitIds.push(__units[i].unitId);

                var abacus = this.getStatisticalInfo(__units[i].unitId);
                abacus.basicEnglishCount = 0;
                abacus.basicEnglishTime = 0;
                abacus.basicMathCount = 0;
                abacus.basicMathTime = 0;
                abacus.readingCount = 0;
                abacus.readingTime = 0;
                abacus.specialCount = 0;
                abacus.specialTime = 0;
                abacus.examCount = 0;
                abacus.examTime = 0;

                //循环basicEnglish
                var basicEnglish = __units[i].basicEnglish || [];
                for(var ii = 0, ll = basicEnglish.length; ii < ll; ii++){
                    var categories = basicEnglish[ii].categories;
                    for(var iii = 0, lll = categories.length; iii < lll; iii++){
                        abacus.basicEnglishCount += +categories[iii].count;
                        for(var iiii = 0, llll = categories[iii].count; iiii < llll; iiii++){
                            abacus.basicEnglishTime += +categories[iii].practices[iiii].time;
                        }
                    }
                }

                //循环basicMath
                var basicMath = __units[i].basicMath || [];
                for(var ii = 0, ll = basicMath.length; ii < ll; ii++){
                    var points = basicMath[ii].points;
                    for(var iii = 0, lll = points.length; iii < lll; iii++){
                        abacus.basicMathCount += +points[iii].count;
                        abacus.basicMathTime += +points[iii].count * +points[iii].basePlayTime;
                    }
                }

                //循环reading
                var reading = __units[i].reading || [];
                for(var ii = 0, ll = reading.length; ii < ll; ii++){
                    if(reading[ii].isChecked){
                        abacus.readingCount += 1;
                        abacus.readingTime += +reading[ii].recommentTime;
                    }
                }

                //循环special
                var special = __units[i].special || [];
                for(var ii = 0, ll = special.length; ii < ll; ii++){
                    var points = special[ii].points;
                    for(var iii = 0, lll = points.length; iii < lll; iii++){
                        abacus.specialCount += +points[iii].count;
                        abacus.specialTime += +points[iii].count * +points[iii].basePlayTime;
                    }
                }

                //循环exam
                var examCart = __units[i].examCart || [];
                abacus.examCount = examCart.length;
                for(var ii = 0, ll = examCart.length; ii < ll; ii++){
                    abacus.examTime += +examCart[ii].time;
                }
            }

            this._statistical.basicEnglishCount = 0;
            this._statistical.basicEnglishTime = 0;
            this._statistical.basicMathCount = 0;
            this._statistical.basicMathTime = 0;
            this._statistical.readingCount = 0;
            this._statistical.readingTime = 0;
            this._statistical.specialCount = 0;
            this._statistical.specialTime = 0;
            this._statistical.examCount = 0;
            this._statistical.examTime = 0;
            this._statistical.unitCount = 0;
            var unitIds = this._statistical.unitIds;
            for(var i = 0, l = unitIds.length; i < l; i++){
                var info = this._statistical[unitIds[i] + "_info"];
                this._statistical.basicEnglishCount += +info.basicEnglishCount;
                this._statistical.basicEnglishTime += +info.basicEnglishTime;
                this._statistical.basicMathCount += +info.basicMathCount;
                this._statistical.basicMathTime += Math.ceil(+info.basicMathTime / 1000 / 60);
                this._statistical.readingCount += +info.readingCount;
                this._statistical.readingTime += +info.readingTime;
                this._statistical.specialCount += +info.specialCount;
                this._statistical.specialTime += Math.ceil(+info.specialTime / 1000 / 60);
                this._statistical.examCount += +info.examCount;
                this._statistical.examTime += (+info.examCount != 0 && +(+info.examTime / 60).toFixed() == 0 ? 1 : +(+info.examTime / 60).toFixed());

                if(+info.basicEnglishCount + +info.basicMathCount + +info.readingCount + +info.specialCount + +info.examCount > 0){
                    this._statistical.unitCount += 1;
                }
            }

            this._statistical.totalCount = this._statistical.basicEnglishCount + this._statistical.basicMathCount + this._statistical.readingCount + this._statistical.specialCount + this._statistical.examCount;
            this._statistical.totalTime = this._statistical.basicEnglishTime + this._statistical.basicMathTime + this._statistical.readingTime + this._statistical.specialTime + this._statistical.examTime;
        },
        _showCalculateInfo   : function(){
            var info = this._statistical;
            var $parent = $(this._bookInfoId);
            for(var i = 0, l = info.unitIds.length; i < l; i++){
                var $target = $("li[data-unitid='" + info.unitIds[i] + "']", $parent);
                var abacus = this.getStatisticalInfo(info.unitIds[i]);

                //basicEnglish 信息显示
                if(abacus.basicEnglishCount > 0){
                    $target.find("p.c-3").find("strong.basicEnCount").text(abacus.basicEnglishCount).closest("span").show();
                    $target.find("div.hoot-info").find("strong.basicEnCount").text(abacus.basicEnglishCount).closest("p").show();
                    $target.find("div.hoot-info").find("strong.basicEnTime").text(abacus.basicEnglishTime);
                }else{
                    $target.find("p.c-3").find("strong.basicEnCount").text(0).closest("span").hide();
                    $target.find("div.hoot-info").find("strong.basicEnCount").text(0).closest("p").hide();
                    $target.find("div.hoot-info").find("strong.basicEnTime").text(0);
                }

                //basicMath 信息显示
                if(abacus.basicMathCount > 0){
                    $target.find("p.c-3").find("strong.basicMathCount").text(abacus.basicMathCount).closest("span").show();
                    $target.find("div.hoot-info").find("strong.basicMathCount").text(abacus.basicMathCount).closest("p").show();
                    $target.find("div.hoot-info").find("strong.basicMathTime").text(Math.ceil(abacus.basicMathTime / 1000 / 60));
                }else{
                    $target.find("p.c-3").find("strong.basicMathCount").text(0).closest("span").hide();
                    $target.find("div.hoot-info").find("strong.basicMathCount").text(0).closest("p").hide();
                    $target.find("div.hoot-info").find("strong.basicMathTime").text(0);
                }

                //reading 信息显示
                if(abacus.readingCount > 0){
                    $target.find("p.c-3").find("strong.readingCount").text(abacus.readingCount).closest("span").show();
                    $target.find("div.hoot-info").find("strong.readingCount").text(abacus.readingCount).closest("p").show();
                    $target.find("div.hoot-info").find("strong.readingTime").text(abacus.readingTime);
                }else{
                    $target.find("p.c-3").find("strong.readingCount").text(0).closest("span").hide();
                    $target.find("div.hoot-info").find("strong.readingCount").text(0).closest("p").hide();
                    $target.find("div.hoot-info").find("strong.readingTime").text(0);
                }

                //special 信息显示
                if(abacus.specialCount > 0){
                    $target.find("p.c-3").find("strong.specialCount").text(abacus.specialCount).closest("span").show();
                    $target.find("div.hoot-info").find("strong.specialCount").text(abacus.specialCount).closest("p").show();
                    $target.find("div.hoot-info").find("strong.specialTime").text(Math.ceil(abacus.specialTime / 1000 / 60));
                }else{
                    $target.find("p.c-3").find("strong.specialCount").text(0).closest("span").hide();
                    $target.find("div.hoot-info").find("strong.specialCount").text(0).closest("p").hide();
                    $target.find("div.hoot-info").find("strong.specialTime").text(0);
                }

                //exam 信息显示
                if(abacus.examCount > 0){
                    $target.find("p.c-3").find("strong.examCount").text(abacus.examCount).closest("span").show();
                    $target.find("div.hoot-info").find("strong.examCount").text(abacus.examCount).closest("p").show();
                    $target.find("div.hoot-info").find("strong.examTime").text((abacus.examCount != 0 && +(abacus.examTime / 60).toFixed() == 0 ? 1 : +(abacus.examTime / 60).toFixed()));
                }else{
                    $target.find("p.c-3").find("strong.examCount").text(0).closest("span").hide();
                    $target.find("div.hoot-info").find("strong.examCount").text(0).closest("p").hide();
                    $target.find("div.hoot-info").find("strong.examTime").text(0);
                }

                if(abacus.basicEnglishCount + abacus.basicMathCount + abacus.readingCount + abacus.specialCount + abacus.examCount > 0){
                    $target.find("p.c-4").show();
                }else{
                    $target.find("p.c-4").hide();
                }
            }

            $("#v-unit-count").text(info.unitCount);
            $("#v-ufo-total-unit-count").text(info.unitCount);
            $("#v-total-time").text(info.totalTime);
            $("#v-total-time, #v-ufo-total-time").text(info.totalTime);

            if(info.unitCount > 0){
                $("#total_info").show();
                $("#select_btns").show();
                $("#v-ufo-next-btn").show();
            }else{
                $("#total_info").hide();
                $("#select_btns").hide();
                $("#v-ufo-next-btn").hide();
            }

            if(info.basicEnglishCount > 0){
                $("#v-confirm-basicEnglish-count").text(info.basicEnglishCount).parent().show();
                $("#v-ufo-total-basicEnglish-count").text(info.basicEnglishCount).parent().show();
                $("#v-confirm-basicEnglish-time").text(info.basicEnglishTime);
                $("span.v-basicEnglish-count").text(info.basicEnglishCount).parent().show();
            }else{
                $("#v-confirm-basicEnglish-count").text(0).parent().hide();
                $("#v-ufo-total-basicEnglish-count").text(0).parent().hide();
                $("#v-confirm-basicEnglish-time").text(0);
                $("span.v-basicEnglish-count").text(0).parent().hide();
            }

            if(info.basicMathCount > 0){
                $("#v-confirm-basicMath-count").text(info.basicMathCount).parent().show();
                $("#v-ufo-total-basicMath-count").text(info.basicMathCount).parent().show();
                $("#v-confirm-basicMath-time").text(info.basicMathTime);
                $("span.v-basicMath-count").text(info.basicMathCount).parent().show();
            }else{
                $("#v-confirm-basicMath-count").text(0).parent().hide();
                $("#v-ufo-total-basicMath-count").text(0).parent().hide();
                $("#v-confirm-basicMath-time").text(0);
                $("span.v-basicMath-count").text(0).parent().hide();
            }

            if(info.readingCount > 0){
                $("#v-confirm-reading-count").text(info.readingCount).parent().show();
                $("#v-ufo-total-reading-count").text(info.readingCount).parent().show();
                $("#v-confirm-reading-time").text(info.readingTime);
                $("span.v-reading-count").text(info.readingCount).parent().show();
            }else{
                $("#v-confirm-reading-count").text(0).parent().hide();
                $("#v-ufo-total-reading-count").text(0).parent().hide();
                $("#v-confirm-reading-time").text(0);
                $("span.v-reading-count").text(0).parent().hide();
            }

            if(info.specialCount > 0){
                $("#v-confirm-special-count").text(info.specialCount).parent().show();
                $("#v-ufo-total-special-count").text(info.specialCount).parent().show();
                $("#v-confirm-special-time").text(info.specialTime);
                $("span.v-special-count").text(info.specialCount).parent().show();
            }else{
                $("#v-confirm-special-count").text(0).parent().hide();
                $("#v-ufo-total-special-count").text(0).parent().hide();
                $("#v-confirm-special-time").text(0);
                $("span.v-special-count").text(0).parent().hide();
            }

            if(info.examCount > 0){
                $("#v-confirm-exam-count").text(info.examCount).parent().show();
                $("#v-ufo-total-exam-count").text(info.examCount).parent().show();
                $("#v-confirm-exam-time").text(info.examTime);
                $("span.v-exam-count").text(info.examCount).parent().show();
            }else{
                $("#v-confirm-exam-count").text(0).parent().hide();
                $("#v-ufo-total-exam-count").text(0).parent().hide();
                $("#v-confirm-exam-time").text(0);
                $("span.v-exam-count").text(0).parent().hide();
            }
        },
        _buildingBandU       : function(){
            $(this._bookInfoId).empty().html(template("t:bookAndUnitTemplate", {
                adjust     : this._adjust,
                bookId     : this._bookId,
                bookName   : this._bookName,
                units      : this._units,
                color      : this._color,
                viewContent: this._viewContent
            }));
            this._showCalculateInfo();
        },
        _setBandUEvents      : function(){
            var $contents = this;

            var $parent = $(this._bookInfoId);

            $parent.off("click");

            $parent.on("click", ".v-change-book", function(){
                if($17.isBlank($contents._clazzIds)){
                    $17.alert("请先选择要更换课本的班级");
                    return false;
                }else{
                    $17.voxLog({
                        module: 'VacationHomeworkAlllog',
                        op:'VH-ChangeBook'
                    });

                    if($contents._statistical.totalCount == 0){
                        setTimeout(function(){
                            location.href = "/teacher/homework/changebook.vpage?cs=" + $contents._clazzIds.toString() + "&f=holiday";
                        }, 200);
                    }else{
                        $.prompt("选择其它教材会清空当前推荐的作业内容，您是否还要更换教材？", {
                            title  : "系统提示",
                            focus  : 1,
                            buttons: { "取消": false, "确定": true },
                            submit : function(e, v){
                                if(v){
                                    setTimeout(function(){
                                        location.href = "/teacher/homework/changebook.vpage?cs=" + $contents._clazzIds.toString() + "&f=holiday";
                                    }, 200);
                                }
                            }
                        });
                    }
                    return false;
                }
            });

            //单元单击事件
            $parent.on("click", ".v-unit", function(event){
                var $self = $(this);
                var $target = $self.find(".c-4 span.w-icon-arrow");

                if(event.target.className.split(" ")[0] == "w-icon-arrow"){
                    if($target.hasClass("w-icon-arrow-topBlue")){//焦点展开变焦点收缩
                        $target.removeClass("w-icon-arrow-top w-icon-arrow-topBlue").addClass("w-icon-arrow-blue");
                        $self.find(".c-4").attr("title", "展开");
                        $self.find("div.hoot-info").hide();
                    }else if($target.hasClass("w-icon-arrow-top")){//展开变收缩
                        $target.removeClass("w-icon-arrow-top");
                        $self.find(".c-4").attr("title", "展开");
                        $self.find("div.hoot-info").hide();
                    }else if($target.hasClass("w-icon-arrow-blue")){//焦点收缩变焦点展开
                        $target.removeClass("w-icon-arrow-blue").addClass("w-icon-arrow-top w-icon-arrow-topBlue");
                        $self.find(".c-4").attr("title", "收缩");
                        $self.find("div.hoot-info").show();
                    }else{//收缩变展开
                        $target.addClass("w-icon-arrow-top");
                        $self.find(".c-4").attr("title", "收缩");
                        $self.find("div.hoot-info").show();
                    }
                }else{
                    $(".v-unit", $parent).removeClass("w-blue").find(".c-1 span.w-hook").removeClass("w-hook-current").end().find(".c-4 span.w-icon-arrow").removeClass("w-icon-arrow-blue w-icon-arrow-topBlue");

                    $self.addClass("w-blue").find(".c-1 span.w-hook").addClass("w-hook-current");
                    $target.addClass($target.hasClass("w-icon-arrow-top") ? "w-icon-arrow-topBlue" : "w-icon-arrow-blue");

                    var unitId = +$self.closest("li[data-unitid]").attr("data-unitid");
                    var unitInfo = null;
                    for(var i = 0, l = $contents._units.length; i < l; i++){
                        if($contents._units[i].unitId == unitId){
                            unitInfo = $contents._units[i];
                            break;
                        }
                    }

                    $($contents._basicEnId).empty();
                    $($contents._basicMathId).empty();
                    $($contents._readingId).empty();
                    $($contents._specialId).empty();
                    $($contents._examId).empty();

                    $("body").trigger({
                        type    : "homework.unitChangeEvent",
                        unitId  : unitId,
                        unitInfo: unitInfo
                    });

                    $self.backToCenter(500);
                }
            });
        },
        _buildingBasicEnglish: function(){
            var $contents = this;

            $("body").on("homework.unitChangeEvent", function(event){
                $contents._basicEnglish = $17.homework.getBasicEnglish();
                $contents._basicEnglish.initialise({
                    data    : event.unitInfo,
                    targetId: $contents._basicEnId,
                    bookId  : $contents._bookId,
                    level   : $contents._focusLevel
                });
                $contents._basicEnglish.run();
            });
        },
        _setBasicEnglishEvent: function(){
            var $contents = this;

            $("body").on("homework.contentChange", function(){
                $contents._calculateAll();
                $contents._showCalculateInfo();
            });
        },
        _buildingBasicMath   : function(){
            var $contents = this;

            $("body").on("homework.unitChangeEvent", function(event){
                $contents._basicMath = $17.homework.getBasicMath();
                $contents._basicMath.initialise({
                    data    : event.unitInfo,
                    targetId: $contents._basicMathId,
                    bookId  : $contents._bookId,
                    level   : $contents._focusLevel
                });
                $contents._basicMath.run();
            });
        },
        _setBasicMathEvents  : function(){
            var $contents = this;
            $("body").on("homework.contentChange", function(){
                $contents._calculateAll();
                $contents._showCalculateInfo();
            });
        },
        _buildingReading     : function(){
            var $contents = this;
            $("body").on("homework.unitChangeEvent", function(event){
                $contents._reading = $17.homework.getReading();
                $contents._reading.initialise({
                    data         : event.unitInfo,
                    targetId     : $contents._readingId,
                    bookId       : $contents._bookId,
                    level        : $contents._focusLevel,
                    point        : $contents._point,
                    readingPreFix: $contents._readingPreFix,
                    searchUrl    : $contents._readingSearchUrl
                });
                $contents._reading.run();
            });
        },
        _setReadingEvents    : function(){
            var $contents = this;
            $("body").on("homework.contentChange", function(){
                $contents._calculateAll();
                $contents._showCalculateInfo();
            });
        },
        _buildingSpecial     : function(){
            var $contents = this;

            $("body").on("homework.unitChangeEvent", function(event){
                $contents._special = $17.homework.getSpecial();
                $contents._special.initialise({
                    data    : event.unitInfo,
                    targetId: $contents._specialId,
                    bookId  : $contents._bookId,
                    level   : $contents._focusLevel
                });
                $contents._special.run();
            });
        },
        _setSpecialEvents    : function(){
            var $contents = this;
            $("body").on("homework.contentChange", function(){
                $contents._calculateAll();
                $contents._showCalculateInfo();
            });
        },
        _buildingExam        : function(){
            var $contents = this;

            $("body").on("homework.unitChangeEvent", function(event){
                $contents._exam = $17.homework.getExam();
                $contents._exam.initialise({
                    data    : event.unitInfo,
                    targetId: $contents._examId,
                    examUrl : $contents._examUrl,
                    bookId  : $contents._bookId,
                    level   : $contents._focusLevel,
                    clazzIds: $contents._clazzIds,
                    subject : $contents._subject,
                    examCoreInit : $contents._loadExCoreInit
                });
            });
        },
        _setExamEvents       : function(){
            var $contents = this;
            $("body").on("homework.contentChange", function(){
                $contents._calculateAll();
                $contents._showCalculateInfo();
            });
        },
        _buildingConfirm     : function(){
            var $contents = this;
            $contents._confirm = $17.homework.getConfirm();
            $contents._confirm.initialise({
                data       : $contents._units,
                targetId   : $contents._confirmId,
                bookId     : $contents._bookId,
                statistical: $contents._statistical,
                subject    : $contents._subject
            });
            $contents._confirm.run();
        },
        _setConfirmEvents    : function(){

        },
        setFoucsLevel        : function(newLevel){
            this._focusLevel = newLevel;
        },
        setClazzIds          : function(newClazzIds){
            this._clazzIds = newClazzIds;
        },
        setExamDisplay       : function(display){
            if(!$17.isBlank(this._exam)){
                this._exam.setDisplay(display);
            }
        },
        clearRecord          : function(){
            var $contents = this;
            var currentUnitId;
            if($contents._basicEnglish){
                currentUnitId = +$contents._basicEnglish._unitId;
            }
            for(var i = 0; i < $contents._units.length; i++){
                var unitId = +$contents._units[i].unitId;
                for(var ii = 0; ii < $contents._units[i].basicEnglish.length; ii++){
                    for(var iii = 0; iii < $contents._units[i].basicEnglish[ii].categories.length; iii++){
                        var have = false;
                        for(var iiii = 0; iiii < $contents._units[i].basicEnglish[ii].categories[iii].practices.length; iiii++){
                            if(!!$contents._units[i].basicEnglish[ii].categories[iii].practices[iiii].needRecord){
                                have = true;
                                break;
                            }

                        }
                        if(have){
                            $contents._units[i].basicEnglish[ii].categories[iii].count = 0;
                            if(currentUnitId === unitId){
                                $("#" + ii + "_" + iii + "_gametypes").removeClass("active");
                            }
                        }
                    }
                }
            }

            $contents._calculateAll();
            $contents._showCalculateInfo();
        },
        clearAll             : function(){
            var $contents = this;
            for(var i = 0; i < $contents._units.length; i++){
                if(!$17.isBlank($contents._units[i].basicEnglish)){
                    for(var ii = 0; ii < $contents._units[i].basicEnglish.length; ii++){
                        for(var iii = 0; iii < $contents._units[i].basicEnglish[ii].categories.length; iii++){
                            $contents._units[i].basicEnglish[ii].categories[iii].count = 0;
                        }
                    }
                }

                if(!$17.isBlank($contents._units[i].basicMath)){
                    for(var ii = 0; ii < $contents._units[i].basicMath.length; ii++){
                        for(var iii = 0; iii < $contents._units[i].basicMath[ii].points.length; iii++){
                            $contents._units[i].basicMath[ii].points[iii].count = 0;
                        }
                    }
                }

                if(!$17.isBlank($contents._units[i].reading)){
                    for(var ii = 0; ii < $contents._units[i].reading.length; ii++){
                        $contents._units[i].reading[ii].isChecked = false;
                    }
                }

                if(!$17.isBlank($contents._units[i].special)){
                    for(var ii = 0; ii < $contents._units[i].special.length; ii++){
                        for(var iii = 0; iii < $contents._units[i].special[ii].points.length; iii++){
                            $contents._units[i].special[ii].points[iii].count = 0;
                        }
                    }

                }

                if(!$17.isBlank($contents._units[i].examCart)){
                    while($contents._units[i].examCart.length > 0){
                        $contents._units[i].examCart.pop();
                    }
                }
            }
            $($contents._basicEnId).find(".v-gametypes").removeClass("active");
            $($contents._basicMathId).find(".v-gametypes").removeClass("active").find("span.content").attr("data-value", 0).text(0);
            $($contents._readingId).find("#readingList").find("li").removeClass("active");
            $($contents._specialId).find(".special_node").removeClass("active").find("div.selectNum").find("span.content").attr("data-value", 0).text("0 题");
            if($contents._exam != null){
                $contents._exam._building("");
            }

            $contents._calculateAll();
            $contents._showCalculateInfo();

            var subjectInfo = $(".t-hootArrow-list li");
            subjectInfo.find(".hoot-box .c-4").attr("title","展开");
            subjectInfo.find(".hoot-box .c-4 span").removeClass("w-icon-arrow-top w-icon-arrow-topBlue");
            subjectInfo.find(".hoot-info").hide();
        },
        getLittle            : function(){
            for(var i = this._units.length - 1; i >= 0; i--){
                if(!$17.isBlank(this._units[i].basicEnglish)){
                    for(var ii = this._units[i].basicEnglish.length - 1; ii >= 0; ii--){
                        for(var iii = this._units[i].basicEnglish[ii].categories.length - 1; iii >= 0; iii--){
                            if(this._units[i].basicEnglish[ii].categories[iii].count > 0){
                                var practices = [];
                                for(var iiii = 0; iiii < this._units[i].basicEnglish[ii].categories[iii].count; iiii++){
                                    practices.push(this._units[i].basicEnglish[ii].categories[iii].practices[iiii].practiceId);
                                }
                                var categorie = {
                                    categoryName: this._units[i].basicEnglish[ii].categories[iii].categoryName,
                                    practices   : practices
                                }
                                this._units[i].basicEnglish[ii].categories[iii] = categorie;
                            }else{
                                this._units[i].basicEnglish[ii].categories.splice(iii, 1);
                            }
                        }
                        if(this._units[i].basicEnglish[ii].categories.length == 0){
                            this._units[i].basicEnglish.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(this._units[i].basicMath)){
                    for(var ii = this._units[i].basicMath.length - 1; ii >= 0; ii--){
                        for(var iii = this._units[i].basicMath[ii].points.length - 1; iii >= 0; iii--){
                            if(this._units[i].basicMath[ii].points[iii].count > 0){
                                var _ = this._units[i].basicMath[ii].points[iii];
                                var point = {
                                    pointId     : _.pointId,
                                    pointName   : _.pointName,
                                    basePlayTime: _.basePlayTime,
                                    count       : _.count,
                                    practiceId  : _.practiceId,
                                    dataType    : _.dataType,
                                    practiceName: _.practiceName
                                };
                                this._units[i].basicMath[ii].points[iii] = point;
                            }else{
                                this._units[i].basicMath[ii].points.splice(iii, 1);
                            }
                        }
                        if(this._units[i].basicMath[ii].points.length == 0){
                            this._units[i].basicMath.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(this._units[i].reading)){
                    var reading = [];
                    for(var ii = 0; ii < this._units[i].reading.length; ii++){
                        if(this._units[i].reading[ii].isChecked){
                            reading.push(this._units[i].reading[ii].id);
                        }
                    }
                    this._units[i].reading = reading;
                }

                if(!$17.isBlank(this._units[i].special)){
                    for(var ii = this._units[i].special.length - 1; ii >= 0; ii--){
                        for(var iii = this._units[i].special[ii].points.length - 1; iii >= 0; iii--){
                            if(this._units[i].special[ii].points[iii].count > 0){
                                var _ = this._units[i].special[ii].points[iii];
                                var point = {
                                    pointId     : _.pointId,
                                    pointName   : _.pointName,
                                    basePlayTime: _.basePlayTime,
                                    count       : _.count,
                                    practiceId  : _.practiceId,
                                    dataType    : _.dataType,
                                    practiceName: _.practiceName
                                };
                                this._units[i].special[ii].points[iii] = point;
                            }else{
                                this._units[i].special[ii].points.splice(iii, 1);
                            }
                        }
                        if(this._units[i].special[ii].points.length == 0){
                            this._units[i].special.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(this._units[i].examCart)){
                    var examCart = [];
                    for(var ii = 0; ii < this._units[i].examCart.length; ii++){
                        examCart.push(this._units[i].examCart[ii].id);
                    }
                    this._units[i].examCart = examCart;
                }

                if(this._units[i].basicEnglish.length == 0 && this._units[i].basicMath.length == 0 && this._units[i].reading.length == 0 &&(!this._units[i].special || this._units[i].special.length == 0) && this._units[i].examCart.length == 0){
                    this._units.splice(i, 1);
                }
            }

            return this._units;
        },
        getSelectedUnits            : function(){
            var tempUnits = $.extend(true,[],this._units);
            for(var i = tempUnits.length - 1; i >= 0; i--){
                if(!$17.isBlank(tempUnits[i].basicEnglish)){
                    for(var ii = tempUnits[i].basicEnglish.length - 1; ii >= 0; ii--){
                        for(var iii = tempUnits[i].basicEnglish[ii].categories.length - 1; iii >= 0; iii--){
                            if(tempUnits[i].basicEnglish[ii].categories[iii].count > 0){
                                var practices = [];
                                for(var iiii = 0; iiii < tempUnits[i].basicEnglish[ii].categories[iii].count; iiii++){
                                    practices.push(tempUnits[i].basicEnglish[ii].categories[iii].practices[iiii].practiceId);
                                }
                                var categorie = {
                                    categoryName: tempUnits[i].basicEnglish[ii].categories[iii].categoryName,
                                    practices   : practices
                                }
                                tempUnits[i].basicEnglish[ii].categories[iii] = categorie;
                            }else{
                                tempUnits[i].basicEnglish[ii].categories.splice(iii, 1);
                            }
                        }
                        if(tempUnits[i].basicEnglish[ii].categories.length == 0){
                            tempUnits[i].basicEnglish.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(tempUnits[i].basicMath)){
                    for(var ii = tempUnits[i].basicMath.length - 1; ii >= 0; ii--){
                        for(var iii = tempUnits[i].basicMath[ii].points.length - 1; iii >= 0; iii--){
                            if(tempUnits[i].basicMath[ii].points[iii].count > 0){
                                var _ = tempUnits[i].basicMath[ii].points[iii];
                                var point = {
                                    pointId     : _.pointId,
                                    pointName   : _.pointName,
                                    basePlayTime: _.basePlayTime,
                                    count       : _.count,
                                    practiceId  : _.practiceId,
                                    dataType    : _.dataType,
                                    practiceName: _.practiceName
                                };
                                tempUnits[i].basicMath[ii].points[iii] = point;
                            }else{
                                tempUnits[i].basicMath[ii].points.splice(iii, 1);
                            }
                        }
                        if(tempUnits[i].basicMath[ii].points.length == 0){
                            tempUnits[i].basicMath.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(tempUnits[i].reading)){
                    var reading = [];
                    for(var ii = 0; ii < tempUnits[i].reading.length; ii++){
                        if(tempUnits[i].reading[ii].isChecked){
                            reading.push(tempUnits[i].reading[ii].id);
                        }
                    }
                    tempUnits[i].reading = reading;
                }

                if(!$17.isBlank(tempUnits[i].special)){
                    for(var ii = tempUnits[i].special.length - 1; ii >= 0; ii--){
                        for(var iii = tempUnits[i].special[ii].points.length - 1; iii >= 0; iii--){
                            if(tempUnits[i].special[ii].points[iii].count > 0){
                                var _ = tempUnits[i].special[ii].points[iii];
                                var point = {
                                    pointId     : _.pointId,
                                    pointName   : _.pointName,
                                    basePlayTime: _.basePlayTime,
                                    count       : _.count,
                                    practiceId  : _.practiceId,
                                    dataType    : _.dataType,
                                    practiceName: _.practiceName
                                };
                                tempUnits[i].special[ii].points[iii] = point;
                            }else{
                                tempUnits[i].special[ii].points.splice(iii, 1);
                            }
                        }
                        if(tempUnits[i].special[ii].points.length == 0){
                            tempUnits[i].special.splice(ii, 1);
                        }
                    }
                }

                if(!$17.isBlank(tempUnits[i].examCart)){
                    var examCart = [];
                    for(var ii = 0; ii < tempUnits[i].examCart.length; ii++){
                        examCart.push(tempUnits[i].examCart[ii].id);
                    }
                    tempUnits[i].examCart = examCart;
                }

                if(tempUnits[i].basicEnglish.length == 0 && tempUnits[i].basicMath.length == 0 && tempUnits[i].reading.length == 0 &&(!tempUnits[i].special || tempUnits[i].special.length == 0) && tempUnits[i].examCart.length == 0){
                    tempUnits.splice(i, 1);
                }
            }

            return tempUnits;
        },
        run                  : function(){
            this._calculateAll();

            this._buildingBandU();
            this._setBandUEvents();

            this._buildingBasicEnglish();
            this._setBasicEnglishEvent();

            this._buildingBasicMath();
            this._setBasicMathEvents();

            this._buildingSpecial();
            //this._setSpecialEvents();

            this._buildingReading();
            //this._setSpecialEvents();

            if(this._subject && this._subject !== "ENGLISH"){
                this._buildingExam();
                this._setExamEvents();
            }
        },
        changeToConfirm      : function(){
            this._buildingConfirm();
            //this._setConfirmEvents();
        },
        getFinishTime        : function(){
            return this._statistical.totalTime;
        },
        getBookId            : function(){
            return this._bookId;
        },
        getBookName          : function(){
            return this._bookName;
        },
        initialise           : function(option){
            if($17.isBlank(option)){
                return false;
            }

            var _info = option.data.homework[0];//多课本练习需要修改这里
            this._subject = option.subject;
            this._bookId = _info.bookId;
            this._bookName = _info.bookName;
            this._color = _info.color;
            this._imgUrl = _info.imgUrl;
            this._latestVersion = _info.latestVersion;
            this._viewContent = _info.viewContent;
            this._units = _info.units;
            this._loadExCoreInit = option._loadExCoreInit;

            this._adjust = option.adjust || this._adjust;

            this._point = option.point || this._point;

            this._bookInfoId = option.bookInfoId || this._bookInfoId;
            this._basicEnId = option.basicEnId || this._basicEnId;
            this._basicMathId = option.basicMathId || this._basicMathId;
            this._readingId = option.readingId || this._readingId;
            this._readingPreFix = option.data.readingPrefix;
            this._specialId = option.specialId || this._specialId;
            this._examId = option.examId || this._examId;
            this._readingSearchUrl = option.readingSearchUrl || this._readingSearchUrl;
            this._examUrl = option.examUrl || this._examUrl;
            this._confirmId = option.confirmId || this._confirmId;
        }
    });

    $17.homework = $17.homework || {};
    $17.extend($17.homework, {
        getContents: function(){
            return new Contents();
        }
    });
}($17));

(function(){
    var cartObj = {
        rootElement : "#ufo",
        cartWhere : function(){
            var $vUfo = $(this.rootElement);
            if(parseInt(($(window).width() - 1000)/2) >= 140 + 15){
                var left = parseInt(($(window).width() - 1000)/2) + 1000 + 15;
                $vUfo.css({'left':left+'px','right':'auto'});
            }else{
                $vUfo.css({'left':'auto','right':'0'});
            }
        },
        init : function(){
            $(window).on('resize',function(){
                this.cartWhere();
            }.bind(this));
        }
    };
    cartObj.init();
}());