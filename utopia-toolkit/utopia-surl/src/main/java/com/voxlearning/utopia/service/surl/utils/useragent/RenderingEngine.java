package com.voxlearning.utopia.service.surl.utils.useragent;

public enum RenderingEngine {

    /**
     * EdgeHTML is a proprietary layout engine developed for the Microsoft Edge web browser, developed by Microsoft.
     */
    EDGE_HTML("EdgeHTML"),
    /**
     * Trident is the the Microsoft layout engine, mainly used by Internet Explorer.
     */
    TRIDENT("Trident"),
    /**
     * HTML parsing and rendering engine of Microsoft Office Word, used by some other products of the Office suite instead of Trident.
     */
    WORD("Microsoft Office Word"),
    /**
     * Open source and cross platform layout engine, used by Firefox and many other browsers.
     */
    GECKO("Gecko"),
    /**
     * Layout engine based on KHTML, used by Safari, Chrome and some other browsers.
     */
    WEBKIT("WebKit"),
    /**
     * Proprietary layout engine by Opera Software ASA
     */
    PRESTO("Presto"),
    /**
     * Original layout engine of the Mozilla browser and related products. Predecessor of Gecko.
     */
    MOZILLA("Mozilla"),
    /**
     * Layout engine of the KDE project
     */
    KHTML("KHTML"),
    /**
     * Other or unknown layout engine.
     */
    BLINK("Blink"),
    /**
     * Layout engine developed as part ofthe Chromium project. Fored from WebKit.
     */
    OTHER("Other");

    String name;

    private RenderingEngine(String name) {
        this.name = name;
    }

}