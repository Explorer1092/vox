/**
 * 跨域iframe自适应高度
 */
var Loader = new function() {
    var doc = document, body = doc.body, self = this,

	// 获取url中的参数
	getRequest = function(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i"), r = window.location.search.substr(1).match(reg);
		return (r != null) ? unescape(r[2]) : null;
	},

	// 获取配置，script的优先级大于url中的参数
	getConfig = function() {
		var scripts = doc.getElementsByTagName('script'), script = scripts[scripts.length - 1];
		return function(param) {
			var p = script.getAttribute(param);
			return p ? p : getRequest(param);
		};
	}(),

	// 代理高度
	proxyheight = 0,

	// 最小高度
	minheight = getConfig("data-minheight"),

	// top页frame的id
	frameid = getConfig("data-frameid"),

	// top页side的id
	sideid = getConfig("data-sideid"),

	// 监听实时更新高度间隔
	timer = getConfig("data-timer"),

	// 获取代理的url
	getProxyUrl = getConfig("data-proxy"),

	// 创建代理的iframe
	proxyframe = function() {
		var el = doc.createElement("iframe");
		el.style.display = "none";
		el.name = "proxy";
		return el;
	}();

	// 重置高度
	this.resize = function() {
		proxyheight = $(document).height();
		proxyframe.src = getProxyUrl + "?data-frameid=" + frameid
				+ "&data-sideid=" + sideid + "&data-frameheight=" + proxyheight
				+ "&data-minheight=" + minheight + "&data-version="
				+ Date.parse(new Date());
	};

	this.init = function() {
		var init = function() {
			body.appendChild(proxyframe);
			self.resize();
			// 是否update
			if (!isNaN(timer)) {
				timer = timer < 500 ? 500 : timer;
				window.setInterval(function() {
					if ($(document).height() != proxyheight) {
						self.resize();
					}
				}, timer);
			}
		};

        if(typeof $ != 'undefined') {
            $(function(){init();});
        }
        else if (doc.addEventListener) {
			window.addEventListener("load", init, false);
		}
        else if (window.attachEvent) {
			window.attachEvent("onload", init);
		}

        // 如果引入了JS框架,建议将上面一句改掉 例如：$(function(){init();}); ?? 会不会有问题？或者对用户体验更好，不会因为个别图片卡死？
	};
};

Loader.init();

/**
 * 使用方法：
 * <iframe scrolling="no" id="testframe" width="500" src="http://www/frame.html?data-frameid=testframe&data-timer=2000&data-proxy=http://www/proxy.html"></iframe>
 * 或者
 * <script src="loader.js" data-frameid="testframe" data-timer="2000" data-proxy="http://www/proxy.html"></script>
 */
