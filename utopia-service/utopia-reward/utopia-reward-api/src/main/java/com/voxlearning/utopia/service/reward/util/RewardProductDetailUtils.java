package com.voxlearning.utopia.service.reward.util;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.*;

public class RewardProductDetailUtils {

    public static List<RewardProductDetail> orderProducts(List<RewardProductDetail> realDatas, final String orderBy, final String upDown) {
        Collections.sort(realDatas, new Comparator<RewardProductDetail>() {
            @Override
            public int compare(RewardProductDetail o1, RewardProductDetail o2) {
                int result = 0;
                switch (orderBy) {
                    case "createDatetime": {
                        if ("up".equals(upDown)) {
                            result = o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
                        } else {
                            result = o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                        }
                        break;
                    }
                    case "soldQuantity": {
                        if ("up".equals(upDown)) {
                            result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                        } else {
                            result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                        }
                        break;
                    }
                    case "wishQuantity": {
                        if ("up".equals(upDown)) {
                            result = o1.getWishQuantity().compareTo(o2.getWishQuantity());
                        } else {
                            result = o2.getWishQuantity().compareTo(o1.getWishQuantity());
                        }
                        break;
                    }
                    case "price": {
                        if ("up".equals(upDown)) {
                            result = o2.getPrice().compareTo(o1.getPrice());
                        } else {
                            result = o1.getPrice().compareTo(o2.getPrice());
                        }
                        break;
                    }
                    case "teacherLevel": {
                        if ("up".equals(upDown)) {
                            result = o2.getTeacherLevel().compareTo(o1.getTeacherLevel());
                        } else {
                            result = o1.getTeacherLevel().compareTo(o2.getTeacherLevel());
                        }
                        break;
                    }
                    case "ambassadorLevel": {
                        if ("up".equals(upDown)) {
                            result = o2.getAmbassadorLevel().compareTo(o1.getAmbassadorLevel());
                        } else {
                            result = o1.getAmbassadorLevel().compareTo(o2.getAmbassadorLevel());
                        }
                        break;
                    }
                    case "studentOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.getStudentOrderValue());
                        Integer v2 = SafeConverter.toInt(o2.getStudentOrderValue());
                        if ("up".equals(upDown)) {
                            if (Objects.equals(v1, v2)) {
                                result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                            } else {
                                result = v1.compareTo(v2);
                            }
                        } else {
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        }
                        break;
                    }
                    case "teacherOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.getTeacherOrderValue());
                        Integer v2 = SafeConverter.toInt(o2.getTeacherOrderValue());
                        if ("up".equals(upDown)) {
                            if (Objects.equals(v1, v2)) {
                                result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                            } else {
                                result = v1.compareTo(v2);
                            }
                        } else {
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        }
                        break;
                    }
                    case "productType": {
                        // 体验(虚拟)类别的的排前面
                        if (Objects.equals(o1.getProductType(), o2.getProductType()))
                            return 0;
                        else if (Objects.equals(o1.getProductType(), RewardProductType.JPZX_TIYAN.name()))
                            return -1;
                        else if (Objects.equals(o2.getProductType(), RewardProductType.JPZX_TIYAN.name()))
                            return 1;
                        else
                            return 0;
                    }
                }
                return result;
            }
        });

        if ("random".equals(orderBy)) {
            Collections.shuffle(realDatas);
        }

        return realDatas;
    }

    public static List<RewardProductDetail> orderProducts(User user, List<RewardProductDetail> realDatas, final String orderBy, final String upDown) {
        Collections.sort(realDatas, new Comparator<RewardProductDetail>() {
            @Override
            public int compare(RewardProductDetail o1, RewardProductDetail o2) {
                int result = 0;
                switch (orderBy) {
                    case "createDatetime": {
                        if ("up".equals(upDown)) {
                            result = o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
                        } else {
                            result = o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                        }
                        break;
                    }
                    case "soldQuantity": {
                        if ("up".equals(upDown)) {
                            result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                        } else {
                            result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                        }
                        break;
                    }
                    case "wishQuantity": {
                        if ("up".equals(upDown)) {
                            result = o1.getWishQuantity().compareTo(o2.getWishQuantity());
                        } else {
                            result = o2.getWishQuantity().compareTo(o1.getWishQuantity());
                        }
                        break;
                    }
                    case "price": {
                        if ("up".equals(upDown)) {
                            result = o2.getPrice().compareTo(o1.getPrice());
                        } else {
                            result = o1.getPrice().compareTo(o2.getPrice());
                        }
                        break;
                    }
                    case "teacherLevel": {
                        if ("up".equals(upDown)) {
                            result = o2.getTeacherLevel().compareTo(o1.getTeacherLevel());
                        } else {
                            result = o1.getTeacherLevel().compareTo(o2.getTeacherLevel());
                        }
                        break;
                    }
                    case "ambassadorLevel": {
                        if ("up".equals(upDown)) {
                            result = o2.getAmbassadorLevel().compareTo(o1.getAmbassadorLevel());
                        } else {
                            result = o1.getAmbassadorLevel().compareTo(o2.getAmbassadorLevel());
                        }
                        break;
                    }
                    case "studentOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.getStudentOrderValue());
                        Integer v2 = SafeConverter.toInt(o2.getStudentOrderValue());
                        if ("up".equals(upDown)) {
                            if (Objects.equals(v1, v2)) {
                                result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                            } else {
                                result = v1.compareTo(v2);
                            }
                        } else {
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        }
                        break;
                    }
                    case "teacherOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.getTeacherOrderValue());
                        Integer v2 = SafeConverter.toInt(o2.getTeacherOrderValue());
                        if ("up".equals(upDown)) {
                            if (Objects.equals(v1, v2)) {
                                result = o1.getSoldQuantity().compareTo(o2.getSoldQuantity());
                            } else {
                                result = v1.compareTo(v2);
                            }
                        } else {
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        }
                        break;
                    }
                    case "productType": {
                        // 体验(虚拟)类别的的排前面
                        if (Objects.equals(o1.getProductType(), o2.getProductType()))
                            return 0;
                        else if (Objects.equals(o1.getProductType(), RewardProductType.JPZX_TIYAN.name()))
                            return -1;
                        else if (Objects.equals(o2.getProductType(), RewardProductType.JPZX_TIYAN.name()))
                            return 1;
                        else
                            return 0;
                    }
                    default: {
                        if (user.isTeacher()) {
                            Integer v1 = SafeConverter.toInt(o1.getTeacherOrderValue());
                            Integer v2 = SafeConverter.toInt(o2.getTeacherOrderValue());
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        } else {
                            Integer v1 = SafeConverter.toInt(o1.getStudentOrderValue());
                            Integer v2 = SafeConverter.toInt(o2.getStudentOrderValue());
                            if (Objects.equals(v1, v2)) {
                                result = o2.getSoldQuantity().compareTo(o1.getSoldQuantity());
                            } else {
                                result = v2.compareTo(v1);
                            }
                        }
                        break;
                    }
                }
                return result;
            }
        });

        if ("random".equals(orderBy)) {
            Collections.shuffle(realDatas);
        }

        return realDatas;
    }

    public static List<Map<String, Object>> orderSimbleProduct(List<Map<String, Object>> realDatas, final String orderBy, final String upDown) {
        Collections.sort(realDatas, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                int result = 0;
                switch (orderBy) {
                    case "id": {
                        Integer v1 = SafeConverter.toInt(o1.get("id").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("id").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "priceS": {
                        Double v1 = SafeConverter.toDouble(o1.get("priceS").toString());
                        Double v2 = SafeConverter.toDouble(o2.get("priceS").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "buyingPrice": {
                        Double v1 = SafeConverter.toDouble(o1.get("buyingPrice")!=null ? o1.get("buyingPrice").toString():0L);
                        Double v2 = SafeConverter.toDouble(o2.get("buyingPrice")!=null ? o2.get("buyingPrice").toString():0L);
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "soldQuantity": {
                        Integer v1 = SafeConverter.toInt(o1.get("soldQuantity").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("soldQuantity").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "inventory": {
                        Integer v1 = SafeConverter.toInt(o1.get("inventory").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("inventory").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "studentOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.get("studentOrderValue").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("studentOrderValue").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    case "teacherOrderValue": {
                        Integer v1 = SafeConverter.toInt(o1.get("teacherOrderValue").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("teacherOrderValue").toString());
                        if ("up".equals(upDown)) {
                            result = v1.compareTo(v2);
                        } else {
                            result = v2.compareTo(v1);
                        }
                        break;
                    }
                    default: {
                        Integer v1 = SafeConverter.toInt(o1.get("studentOrderValue").toString());
                        Integer v2 = SafeConverter.toInt(o2.get("studentOrderValue").toString());
                        result = v2.compareTo(v1);
                        break;
                    }
                }
                return result;
            }
        });

        if ("random".equals(orderBy)) {
            Collections.shuffle(realDatas);
        }

        return realDatas;
    }
}
