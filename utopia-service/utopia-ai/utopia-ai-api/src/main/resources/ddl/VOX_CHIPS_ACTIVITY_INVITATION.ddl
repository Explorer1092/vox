CREATE TABLE `VOX_CHIPS_ACTIVITY_INVITATION` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ACTIVITY_TYPE` varchar(32) NOT NULL COMMENT '标识某种活动类型',
  `INVITER`  bigint(20) NOT NULL COMMENT '邀请人',
  `INVITEE` bigint(20) COMMENT '被邀请人',
  `DISABLED` bit(1) NOT NULL,
  `STATUS` int(11) NOT NULL DEFAULT '0' COMMENT '下单未支付:1,成功购买: 2,退款:3',
  `CREATETIME` datetime NOT NULL,
  `UPDATETIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_ACTIVITY_TYPE_INVITER_INVITEE` (`ACTIVITY_TYPE`,`INVITER`,`INVITEE`)

) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8;