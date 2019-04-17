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
})();