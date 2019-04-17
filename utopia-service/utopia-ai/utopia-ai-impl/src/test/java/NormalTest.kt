import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.voxlearning.alps.calendar.DateUtils
import com.voxlearning.alps.lang.mapper.json.JsonUtils
import com.voxlearning.utopia.service.ai.component.KsyunComponent
import com.voxlearning.utopia.service.ai.constant.TobbitScoreType
import com.voxlearning.utopia.service.ai.data.OcrImageDto
import com.voxlearning.utopia.service.ai.entity.TobbitMathHistory
import org.bson.Document
import org.junit.Test
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class NormalTest {


    //    @Test
    fun testTobbitScoreType() {

        println(TobbitScoreType.of(3))

    }

    // @Test
    fun testSplit() {
        var a = ";HCB_SDFFD"
        println(a.split(";"))
    }

    //@Test
    fun testXXXX() {


        var list = listOf("", "c", "", "a", "b")

        var list2 = list.stream().sorted { o1, o2 -> o2.compareTo(o1) }.collect(Collectors.toList())

        println(list2)

    }

    //    @Test
    fun testMongo() {
        var content = File("/home/ra/a4.json").readText()

        var list = JsonUtils.fromJsonToList(content, TobbitMathHistory::class.java)
        var list2 = ArrayList<TobbitMathHistory>()

        list.forEach { x ->
            run {
                var json = x.json
                var dto = JsonUtils.fromJson(json, OcrImageDto::class.java)
                if (dto != null) {
                    var total = dto.forms.size
                    x.totalCount = total
                    list2.add(x)
                }


            }
        }

        var list3 = ArrayList<Document>()
        list2.forEach { x ->
            var da = Document()
            da["openId"] = x.openId
            da["uid"] = x.uid
            da["img"] = x.img
            da["json"] = x.json
            da["errorCount"] = x.errorCount
            da["totalCount"] = x.totalCount
            da["disabled"] = x.disabled
            da["createTime"] = x.createTime
            da["updateTime"] = x.updateTime
            da["version"] = x.version
            list3.add(da)
        }

        //save list
        var seraddr = ServerAddress("", 57000)
        var tial = MongoCredential.createScramSha1Credential("", "admin", "".toCharArray())
        var clist = Collections.singletonList(tial)
        var client = MongoClient(seraddr, clist)
        var db = client.getDatabase("vox-xcx")
        var table = db.getCollection("tobbit_math_history")


        table.insertMany(list3)

        client.close()

        println("ss")

    }

    //    @Test
    fun testDate() {
        var a = "2019-02-16T10:40:59.027Z"
        var date = DateUtils.parseDate(a, "yyyy-mm-dd'T'HH:mm:ss.SSS'Z'")
        println(date)
    }

//    @Test
    fun testImage() {
        var image = listOf("https://cdn-live-image.17zuoye.cn/training/acf/20190226/6174c78cdf1d41758bcff16bf615f94e")

        var kys = KsyunComponent()

        val start = Instant.now()
        println(kys.passImage(image))

        var end=Instant.now()

        println(end.epochSecond-start.epochSecond)

    }
}