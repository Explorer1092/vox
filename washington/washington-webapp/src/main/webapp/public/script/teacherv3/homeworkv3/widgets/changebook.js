/**
 * @fileoverview
 * @author cm 2016.6.27
 * @Depend KO.js
 * @demo
 * var Book = new ChangeBook().init({
                    level :1,
                    term :2,
                    clazzGroupIds:["17931972_11323"],
                    bookName:"北京版一年级（上）",
                    subject : 学科
                    isSaveBookInfo : true | false,
                    bookListUrl: "/teacher/new/homework/sortbook.vpage"
                },function(){...});
 OR
 Book.init({...},function(){...});
 *  @Return ChangeBookFun
 **/
var ChangeBook = (function(){

    var ChangeBookFun = function(){
        //更换教材属性
        this.term           = ko.observable(2);
        this.level          = ko.observable(1);
        this.clazzGroupIds  = [];
        this.isLoadData     = false;
        this.bookList       = ko.observableArray([]);
        this.leveTermBookMap= {};  //key : level_term
        this.bookName       = "";
        this.selectBookId   = ko.observable(null);
        this.selectBookName = ko.observable("");
        this.closeChangeBook= null;
        this.subject        = null;
        this.isSaveBookInfo = true;
        this.bookListUrl    = "/teacher/new/homework/sortbook.vpage";

        //筛选
        this.searchText     = ko.observable("");
        this.noFilterRes    = ko.observable(false);
    }

    ChangeBookFun.prototype = {
        constructor : ChangeBookFun,

        init : function() {
            this.bookName = arguments[0].bookName;
            this.level    = ko.observable(arguments[0].level || 1);
            this.term     = ko.observable(arguments[0].term || 2);
            this.subject  = arguments[0].subject;
            this.clazzGroupIds = arguments[0].clazzGroupIds;
            this.isSaveBookInfo = arguments[0].isSaveBookInfo;
            this.selectBookId(null);
            this.selectBookName("");
            this.closeChangeBook = arguments[1];

            if(arguments[0].bookListUrl){
                this.bookListUrl = arguments[0].bookListUrl;
            };
            this.changeBookPop();
            //布置作业的展示log
            self.isSaveBookInfo && $17.voxLog({
                module: "m_H1VyyebB",
                op : "page_assign_changeBook_popup_show",
                s0 : this.subject
            });
            return this;
        },

        changeBookPop : function(){
            var self = this;
            var bookHtml = template("t:换课本",{});
            $.prompt(bookHtml,{
                title : "换课本",
                buttons : {},
                position: { width : 690},
                loaded : function(){
                    ko.applyBindings(self, document.getElementById('bookListV5'));
                    self.changeBook();
                }
            });
        },

        changeBook : function(){
            var that = this;
            var _level = this.level(),_term = this.term();
            var _levelTerm = _level + "_" + _term;
            $("#searchText").val("");
            this.searchText("");
            this.noFilterRes(false);
            if(!that.leveTermBookMap.hasOwnProperty(_levelTerm)){
                that.isLoadData = true;
                var paramData = {
                    level: _level,
                    term : _term,
                    subject : that.subject
                };
                $.get(that.bookListUrl, paramData, function(data){
                    that.isLoadData = false;
                    if(data.success){
                        $.each(data.rows,function(){
                            this.isShow = true;
                        });
                        that.leveTermBookMap[_levelTerm] = data.rows;
                        that.bookList(ko.mapping.fromJS(data.rows)());
                    }else{
                        //$17.alert(data.info || "教材获取失败,请稍后再试");
                        that.leveTermBookMap[_levelTerm] = [];
                        that.bookList([]);
                        $17.voxLog({
                            module : "API_REQUEST_ERROR",
                            op     : "API_STATE_ERROR",
                            s0     : "/teacher/new/homework/sortbook.vpage",
                            s1     : $.toJSON(data),
                            s2     : $.toJSON(paramData),
                            s3     : $uper.env
                        });
                    }
                });

            }else{
                var bookList = that.leveTermBookMap[_levelTerm];
                that.bookList(ko.mapping.fromJS(bookList)());
            }
        },

        destory : function(){

        },

        changeBookTermClick  : function(term){
            if(!this.isLoadData){
                this.term(term);
                this.changeBook();
            }
        },
        changeBookLevelClick : function(level){
            if(!this.isLoadData){
                this.level(level);
                this.changeBook();
            }
        },
        bookClick : function(self){
            self.selectBookId(this.id());
            self.selectBookName(this.name());
            self.isSaveBookInfo && $17.voxLog({
                module: "m_H1VyyebB",
                op : "popup_changeBook_bookName_click",
                s0 : self.subject
            });
        },
        searchBook : function(element){
            var self = this,count=0;
            var searchText = $(element).val().toLowerCase() || "";
            self.isSaveBookInfo && $17.voxLog({
                module: "m_H1VyyebB",
                op : "popup_changeBook_search_click",
                s0 : self.subject
            });
            this.searchText(searchText);
            $.each(self.bookList(),function(){
                if(searchText.trim() != "" && this.name().toLowerCase().indexOf(searchText)==-1){
                    count += 1;
                    this.isShow(false);
                }else{
                    this.isShow(true);
                }
            });

            self.noFilterRes(self.bookList().length==count);
        },

        noBookFeedBack : function(){
            $.prompt.close();
            var chooseText = "是";
            $.prompt(template("t:缺失教材反馈",{}),{
                title : "缺失教材反馈",
                buttons : {"确定": true},
                position: { width : 500},
                loaded: function() {
                    $('.t-fbRegionUniversal').on('click', function(){
                        var $this = $(this);
                        $this.siblings(".t-fbRegionUniversal").find(".radios").removeClass("radios_active");
                        $this.find(".radios").addClass("radios_active");
                        chooseText = $this.text();
                    });
                },
                submit: function(e,v,m,f){
                    if(v){
                        var bookName = $("#t-fbBookInput").val();
                        if ($17.isBlank(bookName)) {
                            $("#t-fbBookInput").addClass("w-int-error");
                            return false;
                        }
                        $("#t-fbBookInput").removeClass("w-int-error");
                        var content = bookName + "（"
                            + $("#currentTeacherDistrict").text()
                            + "通用：" + chooseText + "）";

                        var $postData = {
                            contactPhone: $("#currentUserProfileMobile").text(),
                            content: content,
                            feedbackType: "缺失教材反馈"
                        };

                        $.post("/ucenter/feedback.vpage", $postData, function(data) {
                            if (data.success) {
                                $17.alert("缺失教材反馈成功");
                            } else {
                                $17.alert("缺失教材反馈失败");
                            }
                        });
                        return false;
                    }
                }
            });
        },

        saveChangeBook : function(element){
            var self = this;
            if(self.selectBookId() == null){
                return false;
            }
            var $element = $(element);
            if($element.length > 0){
                if($element.hasClass("submiting")){
                    return false;
                }
                $element.addClass("submiting");
            }
            if(self.isSaveBookInfo){
                $17.voxLog({
                    module: "m_H1VyyebB",
                    op : "popup_changeBook_confirm_click",
                    s0 : self.subject
                });
                $.post("/teacher/new/homework/changebook.vpage", {
                    clazzs  : self.clazzGroupIds.join(","),
                    bookId  : self.selectBookId(),
                    subject : self.subject
                }, function(data){
                    $element.removeClass("submiting");
                    $.prompt.close();
                    self.closeChangeBook(data,{
                        bookId : self.selectBookId(),
                        bookName : self.selectBookName()
                    });
                });
            }else{
                $.prompt.close();
                self.closeChangeBook({
                    bookId : self.selectBookId(),
                    bookName : self.selectBookName()
                });
            }

        }
    };
    return ChangeBookFun;

})();