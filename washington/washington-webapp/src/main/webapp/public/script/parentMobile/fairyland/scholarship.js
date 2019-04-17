define(['jquery', 'knockout', 'jquery.form', 'voxLogs'], function ($, ko) {
    var getQuery = function (item) {
        var svalue = location.search.match(new RegExp('[\?\&]' + item + '=([^\&]*)(\&?)', 'i'));
        return svalue ? decodeURIComponent(svalue[1]) : '';
    };

    var _getCookie = function (name) {
        var arr, reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
        if (arr = document.cookie.match(reg))
            return unescape(arr[2]);
        else
            return null;
    };

    var getAppVesion = function () {
        var native_version = "";
        if (window["external"] && window.external["getInitParams"]) {
            var $params = window.external.getInitParams();

            if ($params) {
                $params = eval("(" + $params + ")");

                native_version = $params.native_version;
            }
        } else if (getQuery("app_version")) {
            native_version = getQuery("app_version") || "";
        }
        return native_version;
    };

    //获取App版本
    function getStudnetAppVersion() {
        var native_version = "2.5.0";

        if (getExternal()["getInitParams"]) {
            var $params = getExternal().getInitParams();

            if ($params) {
                $params = $.parseJSON($params);
                native_version = $params.native_version;
            }
        }

        return native_version;
    }

    var sid = (getQuery("sid") == '') ? _getCookie("sid") : getQuery("sid"), errorTip = $('.parentApp-error500');

    //功能方法：图片预览
    $.fn.extend({
        uploadPreview: function (opts) {
            var _self = this,
                _this = $(this);
            opts = $.extend({
                Img: "",
                Width: 100,
                Height: 100,
                ImgType: ["gif", "jpeg", "jpg", "bmp", "png"],
                Callback: function () {}
            }, opts || {});
            _self.getObjectURL = function (file) {
                var url = null;
                if (window.createObjectURL != undefined) {
                    url = window.createObjectURL(file);
                } else if (window.URL != undefined) {
                    url = window.URL.createObjectURL(file);
                } else if (window.webkitURL != undefined) {
                    url = window.webkitURL.createObjectURL(file);
                }
                return url;
            };
            _this.change(function () {
                if (this.value) {
                    if (!(new RegExp("\.(" + opts.ImgType.join("|") + ")$", "i")).test(this.value.toLowerCase())) {
                        toastAlert("非" + opts.ImgType.join("，") + "格式");
                        this.value = "";
                        return false;
                    }
                    if ($.browser.msie) {
                        try {
                            $("#" + opts.Img).attr('src', _self.getObjectURL(this.files[0]));
                        } catch (e) {
                            var obj = $("#" + opts.Img);
                            var div = obj.parent("div")[0];
                            _self.select();
                            if (top != self) {
                                window.parent.document.body.focus();
                            } else {
                                _self.blur();
                            }
                            var src = document.selection.createRange().text;
                            document.selection.empty();
                            obj.hide();
                            obj.parent("div").css({
                                'filter': 'progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)',
                                'width': opts.Width + 'px',
                                'height': opts.Height + 'px'
                            });
                            div.filters.item("DXImageTransform.Microsoft.AlphaImageLoader").src = src;
                        }
                    } else {
                        $("#" + opts.Img).attr('src', _self.getObjectURL(this.files[0]))
                    }
                    checkImgView();
                    opts.Callback();
                }
            })
        }
    });

    function checkImgView() {
        //取消图片占位符
        if ($("#preview_img").attr("src") == '') {
            $("#preview").attr("style", "display:none");
        } else {
            $("#preview").attr("style", "display:");
        }
    }

    function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null && r != '') return decodeURI(r[2]);
        return '';
    }

    function redirectTo(url) {
        //所有链接都添加sid和app_version参数
        location.href = url + (url.indexOf('?') === -1 ? '?' : '&') + 'sid=' + getQueryString('sid')
            + '&app_version=' + getQueryString('app_version')
            + '&version=' + getQueryString('version');
    }

    function getExternal() {
        var _WIN = window;
        if (_WIN['yqexternal']) {
            return _WIN.yqexternal;
        } else if (_WIN['external']) {
            return _WIN.external;
        } else {
            return _WIN.external = function () {
            };
        }
    }

    var toast = $('#js-toast');

    var _timer;
    function toastAlert(info) {
        toast.children('p').html(info);
        clearTimeout(_timer);
        toast.stop(true, true).fadeIn('fast');
        _timer = setTimeout(function(){
            toast.fadeOut(1000);
        }, 2000);
    }

    //fix ios fixed 顶起
    $('input, select, textarea').on({
        'focus': function () {
            $('.js-fixed').css('position', 'static');
        },
        'blur': function () {
            $('.js-fixed').css('position', 'fixed');
        }
    });
    $('select').on('change', function(){
        $('.js-fixed').css('position', 'fixed');
    });

    //首页两个按钮
    var applyBtn = $(".js_apply"), obtainBtn = $(".js_obtain");
    //点击：我要申请
    applyBtn.on('click', function () {
        $(this).html('跳转中···');        //处理网络延迟较大情况
        redirectTo('/project/scholarship/apply.vpage');
    });
    //点击：获取资格
    obtainBtn.on('click', function () {
        //打点：自学奖学金_获得资格按钮_被点击
        YQ.voxLogs({
            dataType : /(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent) ? 'app_17Parent_ios' : 'app_17parent_android',
            database : 'normal',
            module: 'm_m8hxDdmm',
            op: 'o_GJaXG25k'
        });

        $(this).html('跳转中···');
        if (currentUser.userType == 2) {              //家长跳转：付费产品开通列表页
            location.href = window.location.origin + '/view/mobile/parent/learning_app/list?&sid='+ sid + '&app_version=' + getAppVesion();
        } else if (currentUser.userType == 3) {        //学生跳转：自学乐园列表页
            location.href = window.location.origin + '/studentMobile/center/fairyland.vpage?pagegoto=yes&version='+getStudnetAppVersion();
        } else {                                      //老师
            toastAlert('请重新登陆学生为空');
            $(this).html('获取资格');
        }
    });

    //安卓家长端删除上传照片字段，IOS学生端使用h5 input标签
    var ua = window.navigator.userAgent.toLowerCase(),
        notForAndroidParent = $('.not-for-android-parent'),
        forIOSStudent = $('.for-ios-student'),
        isAndroidParent = /android/.test(ua) && /17parent/.test(ua),
        isIOSStudent = /iphone|ipad|ipod/.test(ua) && /17student/.test(ua);
    isAndroidParent ? notForAndroidParent.remove() : notForAndroidParent.show();
    if(!isIOSStudent){forIOSStudent.remove();}

    //申请表表单控件
    var subject = $('[name="subject"]').eq(0),
        scholarshipType = $('[name="scholarshipType"]').eq(0),
        phone = $('[name="phone"]').eq(0),
        diffScore = $('#diffScore'),
        diffScoreInput = $('[name="diffScore"]').eq(0),
        experience = $('[name="experience"]').eq(0),
        imgUrl = $('[name="imgUrl"]').eq(0),
        submitBtn = $('.js_submit').eq(0),
        applyForm = $('#apply-form');
    var _submitText = '提交';

    //申请表加载
    if (location.href.indexOf('scholarship/apply') !== -1) {
        //申请表 ko viewModel
        function ViewModel() {
            var self = this,
                _userShips = null,
                _revisedSubjects = [],
                _AUTH = null;
            //表单相关
            self.childName = ko.observable('');
            self.subject = ko.observable('');
            self.diffScore = ko.observable('');
            self.scholarshipType = ko.observable('');
            self.phone = ko.observable('');
            self.experience = ko.observable('');
            self.img = ko.observable('');
            self.submitText = ko.observable('提交');
            //逻辑相关
            self.updateNum = ko.observable('');        //可修改次数
            self.auth = ko.observable(true);        //当前科目是否有申请权限
            self.error = ko.observable('');        //页面是否有错误

            function _default(val, def) {
                return val == undefined ? (def == undefined ? '' : def) : val;
            }

            //transaction：一致更新表单控件值
            function transaction(obj) {
                obj = _default(obj, {});
                self.scholarshipType(_default(obj.scholarshipType, 'none'));
                self.diffScore(_default(obj.diffScore));
                self.phone(_default(obj.phone));
                self.experience(_default(obj.experience));
                self.img((obj.img == undefined || obj.img == '') ? '' : obj.img + (obj.img.indexOf('?x-oss-process') === -1 ? '?x-oss-process=image/resize,w_200' : ''));
                self.updateNum(_default(obj.updateNum));
                self.submitText(self.updateNum() === 0 ? '确认修改' : '提交');
                self.error('');
            }

            function initData() {
                $.get('/activity/selfstudyproduct/scholarship/childinfo.vpage?sid=' + sid, function (res) {
                    _AUTH = res;
                    self.childName(res.childName);
                    if (res.success) {
                        _userShips = res.userShips;
                        _revisedSubjects = _userShips.map(function (ele) {
                            return ele.subject;
                        });
                    } else {
                        toastAlert(res.info);
                        self.error(res.info);
                    }
                });
            }

            initData();

            //改变科目时更新状态
            self.subjectChange = function () {
                var $this = $(this),
                    _index = $.inArray($this.val(), _revisedSubjects);          //当前科目是否提交过

                transaction(_userShips[_index]);        //更新表单

                //改变科目时首先判断该科目是否有申请权限
                if (!self.auth() && self.subject() !== 'none') {
                    toastAlert('该科目暂无申请权限');
                }
                if (self.updateNum() === 1) {
                    toastAlert('已经修改过一次，无法继续修改');
                } else if (self.updateNum() === 0 && self.experience != '') {
                    toastAlert('你还有一次修改机会哦');
                }
                checkImgView();
            };
        }

        var pageViewModel = new ViewModel();
        ko.applyBindings(pageViewModel);
    }

    //上传图片
    var uploadBtn = $("#upload"),
        selectedImage = false;          //标识是否已选择过图片
    //点击：上传照片带预览
    uploadBtn.on('change', function () {
        selectedImage = true;
        $('#friendly_reminder').remove();
    });
    uploadBtn.uploadPreview({Img: "preview_img"});

    //调用原生相机和选取相册
    $('.js-upload').on('click', function () {
        if (getExternal()['showTakePhoto']) {
            getExternal().showTakePhoto(JSON.stringify({
                show: true,
                photoId: new Date().getTime(),
                photoNum: 1,
                photoSize: 5 * 1024
            }));
        } else if (getExternal()['getImageByHtml']) {
            getExternal().getImageByHtml(JSON.stringify({
                uploadUrlPath: "/v1/user/file/upload.vpage",
                NeedAlbum: true,
                NeedCamera: true,
                uploadPara: {
                    "activity": "xbt"
                }
            }));
        } else {
            toastAlert("无法获取您的相机权限");
        }
    });
    //处理原生相机返回
    window.vox = {
        task: {
            uploadPhotoCallback: function (res) {
                res = JSON.parse(res);
                pageViewModel.img(res.pictures[0].url + '?x-oss-process=image/resize,w_200');
            }
        }
    };

    //申请项目为优秀奖时隐藏进步分数
    scholarshipType.on('change', function () {
        $(this).val() === 'excellent' ? diffScore.addClass('hide').hide() : diffScore.removeClass('hide').show();
    });

    //点击：提交申请
    submitBtn.on('click', function () {
        var $this = $(this);
        if ($this.hasClass('error')) {     //数据请求错误
            toastAlert(pageViewModel.error());
            return false;
        }
        if ($this.hasClass('readonly')) {     //只允许修改一次哦
            toastAlert('已经修改过一次，无法继续修改');
            return false;
        }
        if ($this.hasClass('disabled')) {     //没有申请权限
            toastAlert('该科目暂无申请权限');
            return false;
        }
        if ($this.hasClass('loading')) {     //防止连击
            return false;
        }
        //判空及格式验证
        if (subject.val() === "none") {
            toastAlert('请选择科目');
            return false;
        }
        if (scholarshipType.val() === "none") {
            toastAlert('请选择申请项目');
            return false;
        } else if (scholarshipType.val() === "progress") {           //如果选择进步奖则填写进步了多少分
            if (diffScoreInput.val() === '') {
                toastAlert('请填写进步分数');
                return false;
            } else if (parseInt(diffScoreInput.val()) > 100) {
                toastAlert('进步分数不得超过100分');
                return false;
            }
        }
        if (phone.val() === '') {
            toastAlert('请填写电话号码');
            return false;
        }
        if (experience.val() === '') {
            toastAlert('请填写经验心得(最少50字)');
            return false;
        } else if (experience.val().length <= 50) {
            toastAlert('经验心得不得少于50字');
            return false;
        } else if (experience.val().length > 1000) {
            toastAlert('经验心得不能超过1000个字');
            return false;
        }
        //不是安卓家长端时才校验是否选择了图片
        if(!isAndroidParent){
            //IOS学生端需判断file input是否change
            if(isIOSStudent){
                if (!selectedImage && pageViewModel.img() === '') {
                    toastAlert('请选择要上传的图片');
                    return false;
                }
                if (selectedImage) {          //如果用户选择了图片，则删掉imgUrl字段，因为后端会优先读取imgUrl字段
                    imgUrl.remove();
                }
            }else{
                if (pageViewModel.img() === '') {
                    toastAlert('请选择要上传的图片');
                    return false;
                }
            }
        }
        //ajax提交表单
        $this.addClass('loading').html('提交中···');
        applyForm.addClass('noEvents');
        subject.addClass('noEvents');
        $('#apply-form').ajaxSubmit({
            data: {
                sid:sid
            },
            success: function (res) {
                if (res.success) {
                    $this.html('正在跳转···');
                    toastAlert('提交成功');
                    setTimeout(function () {
                        redirectTo('/project/scholarship/index.vpage')
                    }, 2000);
                } else {
                    $this.removeClass('loading');
                    applyForm.removeClass('noEvents');
                    subject.removeClass('noEvents');
                    $this.html(_submitText);
                    toastAlert(res.info);
                }
            }
        });
    });
});