(function() {
    var b = document.documentElement,
        a = function() {
            var a = b.getBoundingClientRect().width;
            b.style.fontSize = .0625 * (1080 <= a ? 1080 : a) / 2 + "px";
        },
        c = null;

    window.addEventListener("resize", function() {
        clearTimeout(c);
        c = setTimeout(a, 300)
    });
    a();

    /*var initialContent = 'width=device-width,target-densitydpi=high-dpi,initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no';
    var viewport = document.createElement('meta');
    viewport.name = 'viewport';
    viewport.content = initialContent;

    var head = document.getElementsByTagName('head')[0];
    head.appendChild(viewport);*/
})();