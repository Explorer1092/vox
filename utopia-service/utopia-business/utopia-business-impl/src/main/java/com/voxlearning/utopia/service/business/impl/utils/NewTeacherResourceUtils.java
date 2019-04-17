package com.voxlearning.utopia.service.business.impl.utils;

import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;

import java.util.*;

public class NewTeacherResourceUtils {

    public static Map<String, String> prizeMap = new HashMap<>();
    public static Set<String> popularityMap = new HashSet<>();
    public static Set<String> highScoreMap = new HashSet<>();

    static {
        prizeMap.put("14068004-5bbd8eff242de8be2063877e", "年度优秀教学设计作品");
        prizeMap.put("1925873-5be443d20d1efa16c79aec06", "年度优秀教学设计作品");
        prizeMap.put("1903062-5bb2fe8cbcf74dc9da497935", "年度优秀教学设计作品");
        prizeMap.put("13895457-5bc19512242de8a6c83e4f2a", "年度优秀教学设计作品");
        prizeMap.put("13366157-5bd6c9d9242de8a7299eb575", "最具资源整合能力作品");
        prizeMap.put("1801411-5badc9b7242de88944c81ee1", "最具创新智慧设计作品");
        prizeMap.put("12428220-5bc83c82242de81b94f55bc9", "年度优秀教学设计作品");
        prizeMap.put("13903774-5bd470a1bcf74d7d6e3f6897", "年度优秀教学设计作品");
        prizeMap.put("13339300-5bc08676242de891f33d5099", "年度优秀教学设计作品");
        prizeMap.put("13399229-5bc69d26242de87d47a15f53", "年度优秀教学设计作品");
        prizeMap.put("13756712-5bc2f7bebcf74d74cc2e0bdb", "年度优秀教学设计作品");
        prizeMap.put("12326557-5c054fc19191608264f1dbf9", "年度优秀教学设计作品");
        prizeMap.put("13052906-5bdc652d0d1efa619e7d0416", "年度优秀教学设计作品");
        prizeMap.put("1979799-5bac6933242de82500f86328", "年度优秀教学设计作品");
        prizeMap.put("14394967-5bd2a4f2242de8d954eb954e", "年度优秀教学设计作品");
        prizeMap.put("12316258-5c0f6d35242de8bb38be5dbd", "年度优秀教学设计作品");
        prizeMap.put("14230170-5bac486a242de82500f60283", "年度优秀教学设计作品");
        prizeMap.put("13537659-5bc59450242de8fa5eb7a502", "年度优秀教学设计作品");
        prizeMap.put("13158885-5baf797f242de8bf414474a4", "年度优秀教学设计作品");
        prizeMap.put("1627495-5bda9dad242de8446d9b9d4a", "年度优秀教学设计作品");
        prizeMap.put("14062105-5bd8549e242de857a4484fff", "年度优秀教学设计作品");
        prizeMap.put("1400989-5be96eb80d1efaeafc2a1da2", "年度优秀教学设计作品");
        prizeMap.put("14169045-5bae302cbcf74d155b35da4b", "年度优秀教学设计作品");
        prizeMap.put("14331746-5bc93173bcf74d5839ea23e1", "年度优秀教学设计作品");
        prizeMap.put("14118365-5bc0121cbcf74dbad51f26a7", "年度优秀教学设计作品");
        prizeMap.put("14484691-5bda4a0b0d1efa246a3dab0f", "年度优秀教学设计作品");
        prizeMap.put("13233968-5bc003eebcf74dbad51f14e7", "年度优秀教学设计作品");
        prizeMap.put("14080533-5bebd5cbbcf74d04a88bb864", "年度优秀教学设计作品");
        prizeMap.put("13051077-5bf2544c0d1efa576ff8cbbe", "年度优秀教学设计作品");
        prizeMap.put("13271063-5be37bc8919160d57c416948", "年度优秀教学设计作品");
        prizeMap.put("12636640-5bdaaed4bcf74d676ed587f5", "年度优秀教学设计作品");
        prizeMap.put("1211642-5bffeaf30d1efa6bb26e25df", "年度优秀教学设计作品");
        prizeMap.put("12559723-5be14b009191603789d55dd2", "年度优秀教学设计作品");
        prizeMap.put("14406911-5be6914e91916016f8b1d61a", "年度优秀教学设计作品");
        prizeMap.put("1767679-5bda598b919160d95a47ec42", "年度优秀教学设计作品");
        prizeMap.put("1631905-5baf235a242de8af076cbb0c", "最具创新智慧设计作品");
        prizeMap.put("14336179-5bc36ec1242de8a6c87ab93d", "年度优秀教学设计作品");
        prizeMap.put("12961780-5be113c99191603789d3fdf2", "年度最具人气作品");
        prizeMap.put("12404540-5be052670d1efae40d79380d", "最具资源整合能力作品");
        prizeMap.put("13773132-5bdbbbd00d1efa619e428ac2", "年度优秀教学设计作品");
        prizeMap.put("14218498-5bf53c5b9191609ff1aca8dd", "年度优秀教学设计作品");
        prizeMap.put("12377685-5c134719242de87f509c336f", "年度优秀教学设计作品");
        prizeMap.put("1696101-5bbb4f34bcf74d6c9f654102", "最具资源整合能力作品");
        prizeMap.put("1765366-5bbf2e40bcf74df36fb86681", "年度优秀教学设计作品");
        prizeMap.put("14536890-5c092b49242de8b181ac69f8", "年度优秀教学设计作品");
        prizeMap.put("12278154-5bac4fabbcf74d6ce28d44a3", "年度优秀教学设计作品");
        prizeMap.put("13699270-5bd6ca3a242de8a7299f621c", "年度优秀教学设计作品");
        prizeMap.put("12590595-5bb6d9b6bcf74dc9daa42af8", "年度优秀教学设计作品");
        prizeMap.put("12372535-5bbc1030bcf74d6c9f704d55", "年度优秀教学设计作品");
        prizeMap.put("13284927-5bc59824242de8fa5eb7bb89", "年度优秀教学设计作品");
        prizeMap.put("1237801-5bc94ce9bcf74d5839ea4ff5", "年度优秀教学设计作品");
        prizeMap.put("1428201-5bbb4b1d242de8348076ae73", "年度优秀教学设计作品");
        prizeMap.put("1431139-5bce864bbcf74d8d0909faf8", "年度优秀教学设计作品");
        prizeMap.put("14237130-5bf61823242de801757e2a90", "年度优秀教学设计作品");
        prizeMap.put("13686736-5bdc4fcf242de8784ddd6f03", "最具资源整合能力作品");
        prizeMap.put("1545611-5bbf6f03242de84b95a6ad8f", "年度优秀教学设计作品");
        prizeMap.put("13190332-5bc9b950242de81a53d39eb2", "最具创新智慧设计作品");
        prizeMap.put("13388967-5bbd9b37242de8be2063c468", "年度优秀教学设计作品");
        prizeMap.put("1890167-5c123f93242de84fcbf67597", "年度优秀教学设计作品");
        prizeMap.put("1585353-5badbaf2bcf74da07b16f3c8", "年度优秀教学设计作品");
        prizeMap.put("14067304-5bc9c23a242de81a53d82f4b", "最具资源整合能力作品");
        prizeMap.put("14643649-5c13875c919160a4797cf9a4", "年度优秀教学设计作品");
        prizeMap.put("13256676-5bbff4fbbcf74dbad51f05bc", "最具资源整合能力作品");
        prizeMap.put("1751394-5bd2babbbcf74d15b6a4b5c6", "年度优秀教学设计作品");
        prizeMap.put("1820278-5bc700e1242de87d47aa3e71", "年度优秀教学设计作品");
        prizeMap.put("13847404-5bc1bf41bcf74d74cc099831", "最具创新智慧设计作品");
        prizeMap.put("12272646-5bc9e331242de81a53e7f00e", "年度优秀教学设计作品");
        prizeMap.put("14218498-5be6c768bcf74d4debf22ee9", "年度优秀教学设计作品");
        prizeMap.put("1277812-5bb03e90bcf74dc9daf37234", "年度优秀教学设计作品");
        prizeMap.put("13672415-5bd27740242de8d954e17c73", "年度优秀教学设计作品");
        prizeMap.put("1370077-5bad9b72242de880eda80d15", "年度优秀教学设计作品");
        prizeMap.put("12681358-5bd0323e242de8bcbb05b40c", "年度优秀教学设计作品");
        prizeMap.put("13956925-5bacaaccbcf74d47af387bcf", "年度优秀教学设计作品");
        prizeMap.put("1682717-5bad7409242de880eda7ce2c", "年度优秀教学设计作品");
        prizeMap.put("12261678-5bf21d400d1efa576ff8096d", "最具资源整合能力作品");
        prizeMap.put("13221110-5bbda710bcf74d84d4214d23", "年度优秀教学设计作品");
        prizeMap.put("13269104-5bf4ca47bcf74d33976b1586", "年度优秀教学设计作品");
        prizeMap.put("1772017-5bfba722242de8f15e892f69", "年度优秀教学设计作品");
        prizeMap.put("1585354-5bd6ce6d242de8a729a8073f", "年度优秀教学设计作品");
        prizeMap.put("12233896-5baaf4d0bcf74dd5a7daee65", "年度优秀教学设计作品");
        prizeMap.put("1797884-5bd8f234242de8c615a8f5e6", "最具信息化教育精神作品");
        prizeMap.put("13424278-5bced65ebcf74d8d09167bd7", "年度优秀教学设计作品");
        prizeMap.put("14252296-5bd5638cbcf74dcae639e2e5", "年度优秀教学设计作品");
        prizeMap.put("1263287-5be29ee5919160dfb8577250", "最具创新智慧设计作品");
        prizeMap.put("13601514-5bd918c40d1efaad055d6408", "年度优秀教学设计作品");
        prizeMap.put("1875133-5bcc8aa4242de875aa0dafdd", "年度优秀教学设计作品");
        prizeMap.put("12217807-5c04d05a0d1efa7777d636cf", "年度优秀教学设计作品");
        prizeMap.put("1814790-5bada0a3bcf74d3100f08105", "最具创新智慧设计作品");
        prizeMap.put("13179102-5bc2bbe4242de8a6c85f8d89", "年度优秀教学设计作品");
        prizeMap.put("14049437-5bce97fcbcf74d8d090ae260", "最具资源整合能力作品");
        prizeMap.put("14603778-5c0281870d1efa7777cc3cb5", "最具创新智慧设计作品");
        prizeMap.put("1610552-5be8e8bf0d1efa6004ddf841", "最具创新智慧设计作品");
        prizeMap.put("12634488-5bace78cbcf74d47af58a7d0", "年度优秀教学设计作品");
        prizeMap.put("1224304-5bb07510bcf74dc9daf73a90", "年度优秀教学设计作品");
        prizeMap.put("13399234-5bdaf35dbcf74d676e015a0d", "年度优秀教学设计作品");
        prizeMap.put("12461831-5bc96d35bcf74d5839ed44e4", "年度优秀教学设计作品");
        prizeMap.put("13367487-5bde6e40242de87cc44c4645", "年度优秀教学设计作品");
        prizeMap.put("13299357-5bd6ffd0242de8a7295f88cf", "年度最具人气作品");
        prizeMap.put("14450984-5bd951eebcf74dfc2e22d3a7", "年度优秀教学设计作品");
        prizeMap.put("13539314-5beccc640d1efa1609a7bf55", "年度优秀教学设计作品");
        prizeMap.put("12604851-5bab1463bcf74dd5a7dd07d5", "年度优秀教学设计作品");
        prizeMap.put("12397489-5be4f3b1919160612025cb97", "年度优秀教学设计作品");
        prizeMap.put("12295955-5bc00c52bcf74dbad51f1d33", "年度优秀教学设计作品");
        prizeMap.put("12234044-5bdac4b10d1efa95c75164f5", "年度优秀教学设计作品");
        prizeMap.put("14242007-5bb30063bcf74dc9da49b94f", "年度优秀教学设计作品");
        prizeMap.put("13230377-5bd40189242de89ec0a9d6f4", "年度优秀教学设计作品");
        prizeMap.put("13098286-5bea79bb242de8e0dbe31ef2", "年度优秀教学设计作品");
        prizeMap.put("13702496-5baf3c3cbcf74dce9fa25635", "年度优秀教学设计作品");
        prizeMap.put("13494174-5be7ce5091916016f8bf05e1", "年度优秀教学设计作品");
        prizeMap.put("14245504-5bb32b84bcf74dc9da4e85f8", "年度优秀教学设计作品");
        prizeMap.put("13449786-5bc6a641bcf74d3ca2ed90a8", "年度优秀教学设计作品");
        prizeMap.put("13283274-5bdf9bbe242de83b298f11ff", "年度优秀教学设计作品");
        prizeMap.put("1633023-5bc0999a242de891f346118f", "年度优秀教学设计作品");
        prizeMap.put("12285722-5badb578242de88944c6dd9f", "年度优秀教学设计作品");
        prizeMap.put("12201047-5bcaecc3bcf74d08fb40a373", "年度优秀教学设计作品");
        prizeMap.put("14151825-5be84ab791916016f8c610a1", "年度优秀教学设计作品");
        prizeMap.put("13055890-5c0f5d79242de8bb38be2457", "年度优秀教学设计作品");
        prizeMap.put("13232306-5bc2f6cd242de8a6c8689ae9", "年度优秀教学设计作品");
        prizeMap.put("13219359-5be40b7a242de82b51a451ba", "年度优秀教学设计作品");
        prizeMap.put("13419116-5bd520b3bcf74dcae6e73457", "最具资源整合能力作品");
        prizeMap.put("13804164-5be94006242de8a353fa3e68", "年度优秀教学设计作品");
        prizeMap.put("127411-5be4c87cbcf74d04282320ba", "年度优秀教学设计作品");
        prizeMap.put("14471685-5bf663c2242de801757f313b", "年度优秀教学设计作品");
        prizeMap.put("13340360-5be6c7360d1efa6004ccd231", "年度优秀教学设计作品");
        prizeMap.put("12246689-5bc3ee8ebcf74d74cc40fa0d", "年度优秀教学设计作品");
        prizeMap.put("13430478-5bde8bb29191603a2ca018e3", "年度优秀教学设计作品");
        prizeMap.put("13110850-5becd34ebcf74da998824df2", "最具资源整合能力作品");
        prizeMap.put("12316233-5bfcb79d919160331eacdd6d", "年度优秀教学设计作品");
        prizeMap.put("13337613-5be92c5d0d1efaeafc256950", "年度优秀教学设计作品");
        prizeMap.put("1731061-5bde821d9191603a2c9ef7da", "年度优秀教学设计作品");
        prizeMap.put("12246689-5bc32675bcf74d74cc36799c", "年度优秀教学设计作品");
        prizeMap.put("13461317-5bb4366b242de8d63acc95d1", "最具资源整合能力作品");
        prizeMap.put("1919916-5be15b3f9191603789d698c9", "年度优秀教学设计作品");
        prizeMap.put("1384639-5bb96d07bcf74dc9dadd5128", "年度优秀教学设计作品");
        prizeMap.put("1885706-5be3d7840d1efa9d071f99a1", "年度优秀教学设计作品");
        prizeMap.put("14203551-5bea3b8c242de8e0dbe23e99", "年度优秀教学设计作品");
        prizeMap.put("13214012-5bde53bb9191603a2c98cf24", "年度优秀教学设计作品");
        prizeMap.put("13569494-5be8097b242de82ed6f21f64", "最具资源整合能力作品");
        prizeMap.put("12460925-5c00e17f919160f981e86e3b", "年度优秀教学设计作品");
        prizeMap.put("12510287-5bf51ff29191609ff1aacc04", "年度优秀教学设计作品");
        prizeMap.put("1635659-5bbc680d242de85202fe7ef8", "最具资源整合能力作品");
        prizeMap.put("13888264-5bce8937bcf74d8d090a1950", "年度优秀教学设计作品");
        prizeMap.put("13210211-5c03c8d0bcf74dd2f112127d", "最具资源整合能力作品");
        prizeMap.put("13161971-5bc2b15a242de8a6c85d8f4b", "最具资源整合能力作品");
        prizeMap.put("13027485-5bc9bbb0242de81a53d4d7a0", "年度优秀教学设计作品");
        prizeMap.put("12890596-5be031a5bcf74dcf9e2ea29e", "年度优秀教学设计作品");
        prizeMap.put("14390095-5bd7e844242de8856ddc924e", "年度优秀教学设计作品");
        prizeMap.put("13735505-5bc2c53e242de8a6c8613438", "年度优秀教学设计作品");
        prizeMap.put("13726975-5be1ba08bcf74d11c2864f4e", "最具资源整合能力作品");
        prizeMap.put("1125198-5bc5a3f5bcf74dae3116ffac", "年度优秀教学设计作品");
        prizeMap.put("12268301-5bea71f2919160111366967d", "年度优秀教学设计作品");
        prizeMap.put("13836436-5bd267b0bcf74d3f56d15a14", "最具创新智慧设计作品");
        prizeMap.put("13461592-5be12e2d9191603789d4ebd4", "年度优秀教学设计作品");
        prizeMap.put("1533861-5bd570a8bcf74dcae645e651", "最具资源整合能力作品");
        prizeMap.put("1416431-5bc02073242de84b95a91ddd", "年度优秀教学设计作品");
        prizeMap.put("13472046-5be0007a0d1efae40d67b396", "年度优秀教学设计作品");
        prizeMap.put("14120334-5bbb5693bcf74d6c9f68f721", "最具资源整合能力作品");
        prizeMap.put("1898818-5bbfeb6f242de84b95a84740", "年度优秀教学设计作品");
        prizeMap.put("14450279-5bd52540bcf74dcae6eda52f", "年度优秀教学设计作品");
        prizeMap.put("14061511-5bc835ebbcf74d484950e755", "最具信息化教育精神作品");
        prizeMap.put("14213298-5bc051ccbcf74d9d81ae2bcb", "年度优秀教学设计作品");
        prizeMap.put("12603533-5bbf5e82242de817b4ca2de6", "年度优秀教学设计作品");
        prizeMap.put("12956705-5bdc1298bcf74d1cd255401f", "最具创新智慧设计作品");
        prizeMap.put("12272234-5bdbf288bcf74d1cd2503894", "年度优秀教学设计作品");
        prizeMap.put("12420898-5c0c698a91916021a03c1c06", "年度最具人气作品");
        prizeMap.put("13676378-5bbf61b5bcf74df36fd245dc", "年度优秀教学设计作品");
        prizeMap.put("14376823-5bcaf669bcf74d08fb444efa", "年度优秀教学设计作品");
        prizeMap.put("12871717-5bd543fa242de8a729b0fdf4", "年度优秀教学设计作品");
        prizeMap.put("1517229-5bcbf02b242de875aac9d36d", "年度优秀教学设计作品");
        prizeMap.put("12882026-5bcfc64f242de8bcbbe8b3c8", "年度优秀教学设计作品");
        prizeMap.put("1776179-5be14778242de81275b0d932", "年度优秀教学设计作品");
        prizeMap.put("12664604-5bbb3d18242de81c14e520b3", "年度优秀教学设计作品");
        prizeMap.put("13794718-5bc31920bcf74d74cc334399", "最具信息化教育精神作品");
        prizeMap.put("12468434-5bd2c1edbcf74d15b6a7bb41", "年度优秀教学设计作品");
        prizeMap.put("1693647-5c0a514c919160921cdbd1d3", "年度优秀教学设计作品");
        prizeMap.put("13794489-5bc9e4ebbcf74d583912d560", "年度优秀教学设计作品");
        prizeMap.put("1717802-5bc2e566bcf74d74cc2bb377", "年度优秀教学设计作品");
        prizeMap.put("12425495-5bc57e7cbcf74dae311596da", "年度优秀教学设计作品");
        prizeMap.put("13351015-5bd938d9242de8c615b06ced", "年度优秀教学设计作品");
        prizeMap.put("1985330-5c106e8abcf74d23f10a112d", "年度优秀教学设计作品");
        prizeMap.put("1460658-5bb5d532242de8d63af4a8fe", "年度优秀教学设计作品");
        prizeMap.put("12246689-5bcd7559242de80a6e21979b", "年度优秀教学设计作品");
        prizeMap.put("12445039-5bb371a7242de8d63abf7f93", "最具资源整合能力作品");
        prizeMap.put("1561568-5beb83540d1efa443729d59f", "年度优秀教学设计作品");
        prizeMap.put("13609747-5bcd0ab1bcf74d083897a5a8", "年度优秀教学设计作品");
        prizeMap.put("1751644-5bded5be9191603a2ca8c251", "年度优秀教学设计作品");
        prizeMap.put("1382159-5bcc63a0242de875aaf9def0", "年度优秀教学设计作品");
        prizeMap.put("12336410-5bf55922bcf74d339770bcf0", "最具创新智慧设计作品");
        prizeMap.put("14215277-5bbb19e9242de81c14d553c0", "年度优秀教学设计作品");
        prizeMap.put("12690586-5bcf0e4ebcf74d8d09acd983", "年度优秀教学设计作品");
        prizeMap.put("13423002-5bdfe982242de83b29911e6d", "年度优秀教学设计作品");
        prizeMap.put("12678089-5be8dfef0d1efa6004ddeba6", "年度优秀教学设计作品");
        prizeMap.put("1912204-5bea1ffb91916011136592c8", "年度优秀教学设计作品");
        prizeMap.put("13966921-5bc60052bcf74dae3144a268", "年度优秀教学设计作品");
        prizeMap.put("13319364-5be929470d1efaeafc255f9b", "年度优秀教学设计作品");
        prizeMap.put("1936625-5baf244c242de8af076cbd7f", "年度优秀教学设计作品");
        prizeMap.put("13943085-5be4dd4a242de8271cf037db", "年度优秀教学设计作品");
        prizeMap.put("14152902-5bab6839bcf74df7c3475b3b", "年度优秀教学设计作品");
        prizeMap.put("1902243-5bd2c60abcf74d15b6aa2523", "最具资源整合能力作品");

        popularityMap.add("12604851-5bab1463bcf74dd5a7dd07d5");
        popularityMap.add("13221110-5bbda710bcf74d84d4214d23");
        popularityMap.add("12278154-5bac4fabbcf74d6ce28d44a3");
        popularityMap.add("13366157-5bd6c9d9242de8a7299eb575");
        popularityMap.add("1951701-5bdffa8a242de83b29919609");
        popularityMap.add("12246689-5bc32675bcf74d74cc36799c");
        popularityMap.add("12428220-5bc83c82242de81b94f55bc9");
        popularityMap.add("14242007-5bb30063bcf74dc9da49b94f");
        popularityMap.add("13351015-5bd938d9242de8c615b06ced");
        popularityMap.add("13903774-5bd470a1bcf74d7d6e3f6897");
        popularityMap.add("13472046-5be0007a0d1efae40d67b396");
        popularityMap.add("13424278-5bced65ebcf74d8d09167bd7");
        popularityMap.add("12246689-5bcd7559242de80a6e21979b");
        popularityMap.add("14245504-5bb32b84bcf74dc9da4e85f8");
        popularityMap.add("14363042-5bd187e1242de8f55445851a");
        popularityMap.add("12354047-5bac8522bcf74d01114eb519");
        popularityMap.add("14395279-5c09d381bcf74dc3fb592ef8");
        popularityMap.add("12961780-5be113c99191603789d3fdf2");
        popularityMap.add("13727308-5c0e2b820d1efa2b669bea26");
        popularityMap.add("13804164-5be94006242de8a353fa3e68");
        popularityMap.add("14643649-5c13875c919160a4797cf9a4");
        popularityMap.add("14257279-5bbf37e6242de817b4b7476e");
        popularityMap.add("14218498-5bf53c5b9191609ff1aca8dd");
        popularityMap.add("1724534-5bdab3119191609507b70b0e");
        popularityMap.add("12420898-5c0c698a91916021a03c1c06");
        popularityMap.add("13966921-5bc60052bcf74dae3144a268");
        popularityMap.add("12246689-5bc3ee8ebcf74d74cc40fa0d");
        popularityMap.add("13041866-5bfe3a2c242de87449bc4c1c");
        popularityMap.add("1820278-5bc700e1242de87d47aa3e71");
        popularityMap.add("12634488-5bace78cbcf74d47af58a7d0");
        popularityMap.add("13299357-5bd6ffd0242de8a7295f88cf");

        highScoreMap.add("12445039-5bb371a7242de8d63abf7f93");
        highScoreMap.add("14450984-5bd951eebcf74dfc2e22d3a7");
        highScoreMap.add("14363498-5c07dbd39191606b99a7d959");
        highScoreMap.add("12961780-5be113c99191603789d3fdf2");
        highScoreMap.add("14218498-5bf53c5b9191609ff1aca8dd");
        highScoreMap.add("14242007-5bb30063bcf74dc9da49b94f");
        highScoreMap.add("13509473-5c0b31cf0d1efa2b668c142d");
        highScoreMap.add("13226150-5be103899191603789d3aeb9");
        highScoreMap.add("12246689-5bc3ee8ebcf74d74cc40fa0d");
        highScoreMap.add("13319364-5be929470d1efaeafc255f9b");
        highScoreMap.add("12272646-5bc9e331242de81a53e7f00e");
        highScoreMap.add("13472046-5be0007a0d1efae40d67b396");
        highScoreMap.add("13424278-5bced65ebcf74d8d09167bd7");
        highScoreMap.add("12246689-5bcd7559242de80a6e21979b");
        highScoreMap.add("14252296-5bd5638cbcf74dcae639e2e5");
        highScoreMap.add("13299357-5bd6ffd0242de8a7295f88cf");
    }


    private static Map<String, Integer> coursewareReward = new HashMap<>();
    private static Map<String, Integer> levelReward = new HashMap<>();

    static {
        coursewareReward.put("最具信息化教育精神", 1);
        coursewareReward.put("最具创新智慧设计", 2);
        coursewareReward.put("最具资源整合能力", 3);
        coursewareReward.put("年度优秀教学设计", 4);

        levelReward.put("国家级", 1);
        levelReward.put("省级", 2);
        levelReward.put("市级", 3);
        levelReward.put("校级", 4);
    }

    public static final Comparator<NewTeacherResourceWrapper> comparator = new Comparator<NewTeacherResourceWrapper>() {
        @Override
        public int compare(NewTeacherResourceWrapper o1, NewTeacherResourceWrapper o2) {
            Integer integer1 = coursewareReward.getOrDefault(o1.getCoursewarePrize(), 5);
            Integer integer2 = coursewareReward.getOrDefault(o2.getCoursewarePrize(), 5);
            return Integer.compare(integer1, integer2);
        }
    }.thenComparing(new Comparator<NewTeacherResourceWrapper>() {
        @Override
        public int compare(NewTeacherResourceWrapper o1, NewTeacherResourceWrapper o2) {
            Integer integer1 = levelReward.getOrDefault(o1.getPrizeLevel(), 5);
            Integer integer2 = levelReward.getOrDefault(o2.getPrizeLevel(), 5);
            return Integer.compare(integer1, integer2);
        }
    }).thenComparing(new Comparator<NewTeacherResourceWrapper>() {
        @Override
        public int compare(NewTeacherResourceWrapper o1, NewTeacherResourceWrapper o2) {
            return o2.getFirstOnlineTime().compareTo(o1.getFirstOnlineTime());
        }
    });
}
