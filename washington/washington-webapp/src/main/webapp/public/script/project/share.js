/**
 * Created by huhale on 15/2/2.
 * 微信分享Class => WxShare
 */

var WxShare = function(options){
  this.options = $.extend({},options);
  this.init();
};
WxShare.prototype = {
  init:function(){
    this.setOriOptions();
    this.setWxReg();
  },
  /*
   *  注册微信ready后执行事件
   */
  setWxReg:function(){
    var _this = this;
    window.wxSuper.init(_this.options).setWxReady(function(){
      _this.onMenuShareTimeline();
      _this.onMenuShareAppMessage();
      _this.onMenuShareQQ();
      _this.onMenuShareWeibo();
    });
  },
  /*
   *  设置分享参数
   */
  setOriOptions:function(){
    var _success = this.options.success;
    _success && delete this.options.success;
    var _id = this.options.id;
    this.timeLineOptions = {
      title:this.options.desc,
      success:function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareTimeLine", "click", "shareTimeLine"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=1');
        _success && _success();
      }
    };
    this.appMessageOptions = {
      success:function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareAppMessage", "click", "shareAppMessage"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=2');
        _success && _success();
      }
    };
    this.timeLineOptions = $.extend(JSON.parse(JSON.stringify(this.options)),this.timeLineOptions || {});
    this.appMessageOptions = $.extend(JSON.parse(JSON.stringify(this.options)),this.appMessageOptions || {});
    this.qqOptions = $.extend(JSON.parse(JSON.stringify(this.options)),this.qqOptions || {});
    this.weiboOptions = $.extend(JSON.parse(JSON.stringify(this.options)),this.weiboOptions || {});
  },
  /*
   *  重置分享参数
   */
  setOptions:function(options){
    var _success = options.success;
    var _id = this.options.id;
    if(_success){
      _success && delete options.success;
      this.timeLineOptions.success = function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareTimeLine", "click", "shareTimeLine"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=1');
        _success && _success();
      };
      this.appMessageOptions.success = function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareAppMessage", "click", "shareAppMessage"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=2');
        _success && _success();
      };
    }
    this.timeLineOptions = $.extend(this.timeLineOptions || {},options);
    this.appMessageOptions = $.extend(this.appMessageOptions || {},options);
    this.qqOptions = $.extend(this.qqOptions || {},options);
    this.weiboOptions = $.extend(this.weiboOptions || {},options);
  },
  /*
   *  单独设置朋友圈分享参数
   */
  setTimeLineOptions:function(options){
    var _success = options.success;
    var _id = this.options.id;
    if(_success){
      _success && delete options.success;
      this.timeLineOptions.success = function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareTimeLine", "click", "shareTimeLine"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=1');
        _success && _success();
      };
    }
    this.timeLineOptions = $.extend(this.timeLineOptions,options);
  },
  /*
   *  单独设置朋友分享参数
   */
  setAppMessageOptions:function(options){
    var _success = options.success;
    var _id = this.options.id;
    if(_success){
      _success && delete options.success;
      this.appMessageOptions.success = function(){
        if(typeof _hmt !== 'undefined'){
          _hmt.push(["_trackEvent", "shareAppMessage", "click", "shareAppMessage"]);
        }
        $.getJSON('http://api.flyfinger.com/mihoutao/share/hm?id='+_id+'&t=2');
        _success && _success();
      };
    }
    this.appMessageOptions = $.extend(this.appMessageOptions,options);
  },
  /*
   *  单独设置qq分享参数
   */
  setQqOptions:function(options){
    this.qqOptions = $.extend(this.qqOptions,options);
  },
  /*
   *  单独设置微博分享参数
   */
  setWeiboOptions:function(options){
    this.weiboOptions = $.extend(this.weiboOptions,options);
  },
  /*
   *  分享到朋友圈
   */
  onMenuShareTimeline:function(){
    wx.onMenuShareTimeline(this.timeLineOptions);
  },
  /*
   *  分享给朋友
   */
  onMenuShareAppMessage:function(){
    wx.onMenuShareAppMessage(this.appMessageOptions);
  },
  /*
   *  分享到QQ
   */
  onMenuShareQQ:function(){
    wx.onMenuShareQQ(this.qqOptions);
  },
  /*
   * 分享到腾讯微博
   */
  onMenuShareWeibo:function(){
    wx.onMenuShareWeibo(this.weiboOptions);
  }
};

