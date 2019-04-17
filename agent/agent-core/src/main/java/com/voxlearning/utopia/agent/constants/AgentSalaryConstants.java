package com.voxlearning.utopia.agent.constants;

/**
 * @author Jia HuanYin
 * @since 2015/9/24
 */
public final class AgentSalaryConstants {

    public static class CachReward {

        public static class ProvinceManager {
            public static float cachReward(AgentCityLevelType level) {
                switch (level) {
                    case CityLevelS:
                        return 1.0f;
                    case CityLevelA:
                        return 0.7f;
                    default:
                        return 0f;
                }
            }
        }

        public static class CityManager {
            public static float cachReward(AgentCityLevelType level) {
                switch (level) {
                    case CityLevelS:
                        return 1.0f;
                    case CityLevelA:
                        return 0.8f;
                    default:
                        return 0f;
                }
            }
        }

        public static class CityAgent {
            public static float cachReward(AgentCityLevelType level) {
                switch (level) {
                    case CityLevelS:
                        return 4.5f;
                    case CityLevelA:
                        return 3.5f;
                    case CityLevelB:
                        return 2.0f;
                    default:
                        return 0f;
                }
            }

            public static float excessCachReward(AgentCityLevelType level) {
                switch (level) {
                    case CityLevelS:
                        return 1.5f;
                    case CityLevelA:
                        return 1.0f;
                    case CityLevelB:
                        return 1.0f;
                    default:
                        return 0f;
                }
            }
        }

        public static class BusinessDeveloper {
            public static float cachReward(AgentCityLevelType level) {
                switch (level) {
                    case CityLevelS:
                        return 1.0f;
                    case CityLevelA:
                        return 0.8f;
                    default:
                        return 0f;
                }
            }
        }
    }
}
