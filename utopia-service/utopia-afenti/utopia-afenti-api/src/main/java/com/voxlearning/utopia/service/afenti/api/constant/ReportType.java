package com.voxlearning.utopia.service.afenti.api.constant;

import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import lombok.Getter;

/**
 * @author peng.zhang.a
 * @since 2016/5/24
 *
 */
public enum ReportType {
	AfentiExam(OrderProductServiceType.AfentiExam,"afentiLearningReport"),
	SPG(OrderProductServiceType.A17ZYSPG, "spgLearningReport"),
	Unknown(OrderProductServiceType.Unknown, "unKnowReport");

	@Getter
	private String orderProductServiceType;
	@Getter
	private String pushMsgActivityType;

	ReportType(OrderProductServiceType orderProductServiceType, String pushMsgActivityType) {
		this.orderProductServiceType = orderProductServiceType.name();
		this.pushMsgActivityType = pushMsgActivityType;
	}

	public static ReportType find(OrderProductServiceType orderProductServiceType) {
		for (ReportType reportType : ReportType.values()) {
			if (OrderProductServiceType.safeParse(reportType.orderProductServiceType) == orderProductServiceType) {
				return reportType;
			}
		}
		return Unknown;
	}
}
