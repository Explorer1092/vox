var ImageLoad = function() {
	this.images = {},
	this.imageUrls = [],
	this.imagesLoaded = 0,
	this.imagesFailedToLoad = 0,
	this.imagesIndex = 0,
	this.imageLoadingProgress = 0
};
ImageLoad.prototype = {
	getImage: function(a) {
		return this.images[a]
	},
	imageLoadedCallback: function() {
		this.imagesLoaded++
	},
	imageLoadedErrorCallback: function() {
		this.imagesFailedToLoad++
	},
	loadImage: function(a) {
		var e = new Image,
			i = this;
		e.src = a, e.addEventListener("load", function(a) {
			i.imageLoadedCallback(a)
		}), e.addEventListener("error", function(a) {
			i.imageLoadedErrorCallback(a)
		}), this.images[a] = e
	},
	loadImages: function() {
		return this.imagesIndex < this.imageUrls.length && (this.loadImage(this.imageUrls[this.imagesIndex]), this.imagesIndex++), (this.imagesLoaded + this.imagesFailedToLoad) / this.imageUrls.length * 100
	},
	queueImage: function(a) {
		var e = this;
		return "[object Array]" === Object.prototype.toString.call(a) ? a.forEach(function(a) {
			e.imageUrls.push(a)
		}) : this.imageUrls.push(a), this
	},
	imageLoadingProgressCallback: function(a, e) {
		var i = this,
		s = setInterval(function() {
			i.imageLoadingProgress = isNaN(i.loadImages()) ? 100 : i.loadImages(), 100 === i.imageLoadingProgress && (clearInterval(s), setTimeout(function() {
				e.call(i)
			}, 300)), a.call(i, i.imageLoadingProgress)
		}, 10)
	}
}, window.ImageLoad = ImageLoad;