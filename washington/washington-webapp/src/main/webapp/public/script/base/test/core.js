describe("base/core - ", function() {
    beforeEach(function(){
        this.$17 = require("./$8.min.js");
    });

    it("extend 函数", function(){
        var test1 = {
            a : 1,
            b : 2
        };

        var test2 = {
            a : 1
        };

        expect(test1).not.toEqual(test2);

        test2 = this.$17.extend(test2, {
            b : 2
        });

        expect(test1).toEqual(test2);
    });

    it("include 函数", function(){
        var testFun = function(){};

        this.$17.include(testFun, {
            name : "testFun"
        });

        expect("testFun").toBe(new testFun().name);
    });

    it("lpad 函数", function(){
        var str1 = "a";
        var str2 = "aaaaaa";

        expect(false).toBe(this.$17.lpad());
        expect("   a").toBe(this.$17.lpad(str1, 4));
        expect("000a").toBe(this.$17.lpad(str1, 4, 0));
        expect("aaaaaa").toBe(this.$17.lpad(str2, 3));
        expect("aaaaaa").toBe(this.$17.lpad(str2, 3, 0));
    });

    it("rpad 函数", function(){
        var str1 = "a";
        var str2 = "aaaaaa";

        expect(false).toBe(this.$17.rpad());
        expect("a   ").toBe(this.$17.rpad(str1, 4));
        expect("a000").toBe(this.$17.rpad(str1, 4, 0));
        expect("aaaaaa").toBe(this.$17.rpad(str2, 3));
        expect("aaaaaa").toBe(this.$17.rpad(str2, 3, 0));
    });
});