;(function () {

    var independent = function () {
        this.webLoading      = ko.observable(true);
        this.term            = constantObj.term;
        this.clazzLevel      = 0;
        this.clazzGroupIds   = [];
        this.checkedGroups   = [];
        this.bookId          = ko.observable("");
        this.bookName        = ko.observable("");
        this.paperModuleList = ko.observableArray([]);

        this.init();
    };

    independent.prototype = {

        constructor : independent,

        init : function () {

            this.initClazz();
            $17.voxLog({
                module : "m_jr0GAJRd",
                op     : "page_exam_select_papers"
            });
        },

        initClazz : function () {
            var self = this,
                levelAndclazzs = $17.homeworkv3.getLevels();

            levelAndclazzs.extendLevelClick = self.extendLevelClick.bind(self);
            levelAndclazzs.initialise({
                batchclazzs : constantObj.batchclazzs,
                hasStudents : true,
                clazzClickCb  : function(){
                    self.clazzGroupIds = this.checkedClazzGroupIds();
                    self.checkedGroups = this.getCheckedGroups();
                }
            });
            self.clazzLevel    = levelAndclazzs.showLevel();
            self.clazzGroupIds = levelAndclazzs.checkedClazzGroupIds();
            self.checkedGroups = levelAndclazzs.getCheckedGroups();

            ko.applyBindings(levelAndclazzs, document.getElementById('level_and_clazzs'));
        },

        extendLevelClick : function (obj) {

            this.clazzLevel    = obj.level;
            this.clazzGroupIds = obj.clazzGroupIds;
            this.checkedGroups = obj.checkedGroups;
            this.loadBookByClazzIds(this.clazzGroupIds);
        },

        loadBookByClazzIds  : function(clazzGroupIds){
            var self = this;

            $.get("/teacher/new/homework/clazz/book.vpage",{
                clazzs    : clazzGroupIds.join(","),
                isTermEnd : false,
                subject   : constantObj.subject
            },function(data){
                if(data.success && data.clazzBook){
                    var _book = data.clazzBook;

                    self.bookId(_book.bookId);
                    self.bookName(_book.bookName);
                    self.initQuestionBox();
                }
            });
        },

        initQuestionBox : function(){
            var self = this;

            $.get("/teacher/newexam/paperlist.vpage",{
                bookId : self.bookId()
            },function (res) {
                self.webLoading(false);
                self.paperModuleList(res.paperModuleList || []);
            });
        },

        paperDetail : function(self){

            if(self.checkedGroups.length == 0){

                $17.alert("请选择需要布置的班级～");
                return false;
            }
            $17.homeworkv3.newexamReview({
                checkedGroups   : self.checkedGroups,
                paperId         : this.paperId,
                paperName       : this.paperName,
                questionCount   : this.questionCount,
                durationMinutes : this.durationMinutes
            });

            $("#arrangenewexam").hide();
            $("#newexamreview").show();

            $17.voxLog({
                module : "m_jr0GAJRd",
                op     : "page_exam_papers_review"
            });
        },

        changeBook : function(){
            var self = this,
                changeBookOption = {
                    level :self.clazzLevel,
                    term : self.term,
                    clazzGroupIds:self.clazzGroupIds,
                    bookName:self.bookName(),
                    subject : constantObj.subject,
                    isSaveBookInfo : true
                };
            if(!self.changeBookModule){
                self.changeBookModule = new ChangeBook();
            }
            self.changeBookModule.init(changeBookOption,function(data,bookinfo){

                if(data.success){
                    self.bookId(bookinfo.bookId);
                    self.bookName(bookinfo.bookName);
                    self.initQuestionBox();
                }else{
                    $17.alert(data.info);
                }
            });
        }
    };

    ko.applyBindings(new independent(), document.getElementById('paperModule'));
}());