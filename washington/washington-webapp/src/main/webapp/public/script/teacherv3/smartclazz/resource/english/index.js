$(function(){
    var queryString = function(key){
        return (document.location.search.match(new RegExp("(?:^\\?|&)"+key+"=(.*?)(?=&|$)"))||['',null])[1];
    };

    window.nextHomeWork = function(){
        $.prompt.close();
    };

    var bookAndUnit = {
        subject : queryString("subject"),
        clazzId : queryString("clazzId"),
        bookSelect : $17.modules.select("#bookSelect"),
        unitSelect : $17.modules.select("#unitSelect"),

        fillBookList : function(){
            var that = this;
            $.get("/teacher/smartclazzresource/books.vpage",{
                subject : this.subject,
                clazzId : this.clazzId
            },function(data){
                if(data.success){
                    $("#bookList").html(template("t:课本", { bookList: data.books }));
                    $.each(data.books,function(){
                        if(this.defaultBook){
                            that.bookSelect.set(this.bookId, this.bookName);
                            that.fillUnitList(this.bookId);
                            return false;
                        }
                    });
                }else{
                    $17.alert("教材信息获取失败,请稍后再试~");
                }
            });
        },
        fillUnitList  : function(bookId){
            var that = this;
            if($17.isBlank(bookId)){
                return false;
            }
            $.get("/teacher/smartclazzresource/units.vpage",{
                subject : this.subject,
                bookId  : bookId
            },function(data){
                if(data.success){
                    $("#unitList").html(template("t:单元", { unitList: data.units }));
                    that.unitSelect.set(data.units[0].unitId, data.units[0].unitName);

                    var resourceType = $(".resourceType.current").attr("data-value") || "basicPractices";
                    switch(resourceType){
                        case "basicPractices" :
                            filterBasket.drawBasicPractices();
                            break;
                        case "readingPractices":
                            //filterBasket.drawReadingPractices();
                            break;
                        case "ttsPractices" :
                            filterBasket.drawTTSPractices();
                            break;
                    }
                }else{
                    $17.alert("单元信息获取失败,请稍后再试~");
                }
            });
        },
        getBookId : function(){

            return this.bookSelect ? this.bookSelect.get() : null;
        },
        getUnitId : function(){

            return this.unitSelect ? this.unitSelect.get() : null;
        },

        init : function(){
            this.fillBookList();
            this.bind();
        },
        bind : function(){
            var that = this;

            $("#bookList").on("click","a",function(){
                that.bookSelect.set($(this).data("book_id"), $(this).text());
                that.fillUnitList($(this).data("book_id"));
            });

            $("#unitList").on("click","a",function(){
                that.unitSelect.set($(this).data("unit_id"), $(this).text());
                var resourceType = $(".resourceType.current").attr("data-value") || "basicPractices";
                switch(resourceType){
                    case "basicPractices" :
                        filterBasket.drawBasicPractices();
                        break;
                    case "readingPractices":
                        //filterBasket.drawReadingPractices();
                        break;
                    case "ttsPractices" :
                        //filterBasket.drawTTSPractices();
                        break;
                }
            });
        }
    };
    bookAndUnit.init();

    var filterBasket = {
        tts_cache           : {},
        basic_cache         : {},
        reading_filterCache : {},
        $contextList        : $("#contextList"),
        $readingFilter      : $("#readingFilter"),
        $ttspage            : $("#ttspage"),
        searchOptions       : {
                                    bookId      : "",
                                    unitId      : "",
                                    readingName : "",
                                    clazzLevels : "",
                                    topicIds    : "",
                                    seriesIds   : "",
                                    pageNum     : 1,
                                    pageCount   : 0,
                                    pageSize    : 8,
                                    subject     : "ENGLISH"
                                },
        drawTTSPractices    : function(pageNo){
            if($17.isBlank(pageNo)){
                pageNo = 1;
            }
            if($17.isBlank(filterBasket.tts_cache[bookAndUnit.getBookId() + "_" + pageNo])){
                filterBasket.$contextList.html(template("t:加载中", {}));

                $.post("/teacher/smartclazzresource/ttslistening.vpage", { bookId: bookAndUnit.getBookId(), pageNum: pageNo }, function(data){
                    filterBasket.tts_cache[bookAndUnit.getBookId() + "_" + pageNo] = data.paperPage;
                    filterBasket.ttsRefresh(data.paperPage);
                });
            }else{
                filterBasket.ttsRefresh(filterBasket.tts_cache[bookAndUnit.getBookId() + "_" + pageNo]);
            }
        },
        ttsRefresh : function(_paperPage){
            filterBasket.$contextList.html(template("t:听力资源", { paperPage: _paperPage,pageCount: _paperPage.totalPages, pageNum: _paperPage.number }));
        },
        drawBasicPractices  : function(){
            var bookId = bookAndUnit.getBookId();
            var unitId = bookAndUnit.getUnitId();
            var subject = bookAndUnit.subject;
            this.$contextList.html(template("t:加载中", {}));

            if($17.isBlank(filterBasket.basic_cache[unitId])){
                $.get("/teacher/new/homework/content.vpage",{
                    bookId   : bookId,
                    unitId   : unitId,
                    sections : "",
                    type     : "BASIC_APP",
                    subject  : subject
                },function(data) {
                    if(data.success){
                        var content = data.content;
                        var lessonList = [];
                        for(var m = 0,mLen = content.length; m < mLen; m++){
                            lessonList = lessonList.concat(content[m].lessons || []);
                        }
                        filterBasket.basic_cache[unitId] = lessonList;
                        filterBasket.basicRefresh(unitId);
                    }else{
                        $17.alert(data.info || "数据获取失败,请稍后再试~");
                    }
                });
            }else {
                this.basicRefresh(unitId);
            }
        },
        basicRefresh        : function(unitId){
            var content = this.basic_cache[unitId];
            if(content.length > 0){
                this.$contextList.html(template("t:基础练习", { bookId: bookAndUnit.getBookId(), content:content}));
            }else{
                delete this.basic_cache[unitId];
                this.$contextList.html('<div style="padding: 100px 10px; text-align: center;">温馨提示：该单元暂无应用，请选择其他单元</div>');
            }
        },
        drawReadingPractices: function(){
            var bookId = bookAndUnit.getBookId();
            var unitId = bookAndUnit.getUnitId();
            var subject = bookAndUnit.subject;
            if($17.isBlank(filterBasket.reading_filterCache["filter"])){
                $.get("/teacher/new/homework/content.vpage",{
                    bookId   : bookId,
                    unitId   : unitId,
                    subject  : subject,
                    type     : "READING",
                },function(data){
                    if(data.success){
                        $.each(data.content,function(){
                            if(this.type == "search"){
                                filterBasket.reading_filterCache["filter"] = this;
                                $("#readingFilter").html(template("t:绘本筛选", { data: this }));
                                filterBasket.readingRefresh();

                                return false;
                            }
                        });
                    }else{
                        $17.alert(data.info);
                    }
                });
            }else{
                this.readingRefresh();
            }
            return false;
        },
        readingRefresh      : function(){
            var that = this;
            that.searchOptions.bookId = bookAndUnit.getBookId();
            that.searchOptions.unitId = bookAndUnit.getUnitId();
            $.post("/teacher/new/homework/reading/search.vpage",this.searchOptions,function(data){
                if(data.success){
                    if(data.readings.length > 0){
                        that.searchOptions.pageNum = data.pageNum;
                        that.searchOptions.pageCount = data.pageCount;
                        $("#readingList").html(template("t:绘本阅读", { data: data.readings}));
                        $("#readingList").append(template("t:template模板分页", { pageCount: data.pageCount, pageNum: data.pageNum-1 }))

                    }else{
                        $("#readingList").html('<div style="padding: 45px 10px; text-align: center;">对不起，还没有满足条件的绘本</div>');
                    }
                }else{
                    $17.alert(data.info);
                }
            });
        },

        resetReadingFilter : function(){
            var filter = {clazzLevels  : [], topics  : [], series : []};
            for(var temp in filter){
                $(".J_filter-"+temp).find(".filter-item").each(function(){
                    var item = $(this);
                    if(item.hasClass("w-checkbox-current")){
                        filter[temp].push(item.attr("filterId"));
                    }
                });

                if(filter[temp].length == 0){
                    $(".J_filter-" + temp + " .selAll").addClass("label-check-current");
                }else{
                    $(".J_filter-" + temp + " .selAll").removeClass("label-check-current");
                }
            }

            this.searchOptions.pageNum     = 1;
            this.searchOptions.readingName = $("#filter-readingName").val();
            this.searchOptions.clazzLevels = filter.clazzLevels.join(",");
            this.searchOptions.topicIds    = filter.topics.join(",");
            this.searchOptions.seriesIds   = filter.series.join(",");

            this.readingRefresh();
        },

        init : function(){
            var that = this;

            //一级标签绑定事件
            $("p.sl-menu[data-tag]").on("click", function(){
                var $this = $(this);
                $this.addClass("current").siblings("p").removeClass("current");
                $("#unitSelect").toggle($this.attr("data-tag") == "17resource");
                // 二级标签
                var $subTag = $("p.sl-menu[data-tag-ref='" + $this.attr("data-tag") + "']");
                $subTag.show().siblings("p").hide();
                $subTag.show().eq(0).trigger("click",true);
            });
            //二级标签绑定click事件
            $("p.sl-menu[data-tag-ref]").on("click", function(){
                var $this = $(this), $basicPra = $("#basicPra"), $readingPra = $("#readingPra");
                $this.addClass("current").siblings("p").removeClass("current");
                switch($this.attr("data-value")){
                    case "basicPractices" :
                        $("#J_bookInfo").show();
                        $readingPra.hide();
                        filterBasket.drawBasicPractices();
                        $basicPra.show();
                        break;
                    case "readingPractices":
                        $("#J_bookInfo").hide();
                        $basicPra.hide();
                        filterBasket.drawReadingPractices();
                        $readingPra.show();
                        break;

                    case "ttsPractices" :
                        $("#J_bookInfo").show();
                        $readingPra.hide();
                        filterBasket.drawTTSPractices();
                        $basicPra.show();
                        break;
                }
            });

            //基础练习预览
            this.$contextList.on("mouseenter mouseleave",".J_basicMask",function(){
                $(this).find(".lessons-mask").toggle();
            }).on("click",".J_basicMask",function(){
                var $this = $(this).parents("li");
                var categoryId = $this.attr("categoryId"),lessonId = $this.attr("lessonId");
                $.each(filterBasket.basic_cache[bookAndUnit.getUnitId()], function () {
                    if(this.lessonId == lessonId){
                        $.each(this.categories, function () {
                            if(this.categoryId == categoryId){
                                var practices = this.practices || [];
                                if(practices.length <= 0){
                                    $17.alert("没有相应类别应用,暂不能预览");
                                    return false;
                                }
                                var questions = practices[0].questions || [];
                                if(questions.length <= 0){
                                    $17.alert("没有配相应的应试题,暂不能预览");
                                    return false;
                                }
                                var qIds = [];
                                for(var t = 0, tLen = questions.length; t < tLen; t++){
                                    qIds.push(questions[t].questionId);
                                }
                                var paramObj = {
                                    qids : qIds.join(","),
                                    lessonId : lessonId,
                                    practiceId : practices[0].practiceId,
                                    fromModule : ""
                                };
                                var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                                var data = '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="700" marginwidth="0" height="470" marginheight="0" scrolling="no" frameborder="0"></iframe>';

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
                            }
                        });
                    }
                });
            });

            //绘本预览
            $("#readingList").on("click","li",function(){
                var dataHtml = "",pictureBookId = $(this).attr("pictureBookId");
                var keywords = $("#keywords_"+pictureBookId).text();
                var paramObj = {
                    pictureBookId : pictureBookId,
                    fromModule : ""
                };

                var gameUrl = "/flash/loader/newselfstudy.vpage?" + $.param(paramObj);
                if(keywords){
                    dataHtml += "<div class=\"h-homework-pop\"><div class=\"popTitle\">重点词汇：</div><div class=\"popContent\">" + keywords + "</div></div>";
                }
                dataHtml += '<iframe class="vox17zuoyeIframe" src="' + gameUrl + '" width="900" marginwidth="0" height="644" marginheight="0" scrolling="no" frameborder="0"></iframe>';

                $.prompt(dataHtml, {
                    title   : "预 览",
                    buttons : {},
                    position: { width: 960 },
                    close   : function(){
                        $('iframe').each(function(){
                            var win = this.contentWindow || this;
                            if(win.destroyHomeworkJavascriptObject){
                                win.destroyHomeworkJavascriptObject();
                            }
                        });
                    }
                });
            });

            this.$readingFilter.on("click",".J_showAllFilter",function(){
                var $item = $(this).parents(".theme-box");
                if($item.hasClass("showAll")){
                    $item.removeClass("showAll");
                }else{
                    $item.addClass("showAll");
                }
            });

            this.$readingFilter.on("keyup",".filter_search",function(){
                if((window.event && event.keyCode == 13)){
                    that.resetReadingFilter();
                }
            }).on("click",".filter_searchBtn",function(){
                that.resetReadingFilter();
            }).on("click",".filter-item",function(){
                var $item = $(this);
                if($item.hasClass("w-checkbox-current")){
                    $item.removeClass("w-checkbox-current");
                }else{
                    $item.addClass("w-checkbox-current");
                }
                that.resetReadingFilter();
            });

            //reading GO按钮
            $("#readingList").on("click",".goBtn",function(){
                var pageNo = $(this).siblings("input").val();
                if($17.isBlank(pageNo)){
                    return false;
                }
                if(!$17.isNumber(pageNo)){
                    return false;
                }else{
                    pageNo = pageNo * 1;
                }
                if(pageNo <= 0 || pageNo > that.searchOptions.pageCount){
                    return false;
                }

                that.searchOptions.pageNum = pageNo;
                that.readingRefresh();
            });

            //reading 分页
            $("#readingList").on("click","a.v-page-btn",function(){
                that.searchOptions.pageNum = $(this).attr("data-index") * 1 + 1;
                that.readingRefresh();
            });

            //tts分页
            filterBasket.$ttspage.find("a.v-page-btn").on("click", function(){
                filterBasket.drawTTSPractices($(this).attr("data-index") * 1 + 1);
            });

            //ttsGO按钮
            filterBasket.$ttspage.find("#goBtn20141120112130").on("click", function(){
                var pageNo = $(this).siblings("input").val();
                if($17.isBlank(pageNo)){
                    return false;
                }
                if(!$17.isNumber(pageNo)){
                    return false;
                }else{
                    pageNo = pageNo * 1;
                }
                if(pageNo <= 0 || pageNo > filterBasket.totalPage){
                    return false;
                }
                filterBasket.drawTTSPractices(pageNo);
                return false;
            });
        }
    };

    filterBasket.init();

    $17.voxLog({
        module : "m_aHrND8yNXX",
        op     : "wise_resource_load"
    })
});


