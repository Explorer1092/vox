var imageLoad = new ImageLoad,
	imageUrls = [],
	$loading = $("#J_Loading");
imageLoad.queueImage(imageUrls).queueImage(loadImgArr).imageLoadingProgressCallback(function(a) {
	var a = Math.floor(a);
	$loading.find(".progress span").html(a + "%"), $loading.find(".progress_bar span").css({
		width: a + "%"
	})
}, function() {
	$("#J_Loading").remove();
	$('#J_SalIndex').show();
});