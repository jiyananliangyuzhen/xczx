package dsa1.xczxx.wfs.ws.mservice.oa.entity;

import dsa1.xczxx.wfs.ws.mservice.oa.ScheduledOaTask;
import kd.bos.dataentity.entity.DynamicObject;

/**
 * OA任务类型枚举
 * 定义待办任务的不同处理类型及其对应的处理逻辑
 */
public enum TaskType {

    /**
     * 待办创建任务
     */
    TODO("createToDo", "待办创建") {
        @Override
        public void process(ScheduledOaTask task, DynamicObject record) {
            task.processTodo(record);
        }
    },

    /**
     * 待办处理任务（已办）
     */
    DONE("dealToDo", "待办处理") {
        @Override
        public void process(ScheduledOaTask task, DynamicObject record) {
            task.processDone(record);
        }
    },

    /**
     * 待办删除任务
     */
    DELETE("deleteToDo", "待办删除") {
        @Override
        public void process(ScheduledOaTask task, DynamicObject record) {
            task.processDelete(record);
        }
    };

    /**
     * 任务类型代码
     */
    private final String code;

    /**
     * 任务类型描述
     */
    private final String description;

    /**
     * 构造函数
     * @param code 任务类型代码
     * @param description 任务类型描述
     */
    TaskType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取任务类型代码
     * @return 任务类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取任务类型描述
     * @return 任务类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取对应的任务类型枚举
     * @param code 任务类型代码
     * @return 对应的任务类型枚举，如果未找到返回null
     */
    public static TaskType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据代码获取对应的任务类型枚举（带默认值）
     * @param code 任务类型代码
     * @param defaultType 默认枚举值
     * @return 对应的任务类型枚举，如果未找到返回默认值
     */
    public static TaskType fromCodeWithDefault(String code, TaskType defaultType) {
        TaskType type = fromCode(code);
        return type != null ? type : defaultType;
    }

    /**
     * 判断是否为有效的任务类型代码
     * @param code 任务类型代码
     * @return 如果是有效的返回true，否则返回false
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * 获取所有任务类型代码
     * @return 任务类型代码数组
     */
    public static String[] getAllCodes() {
        TaskType[] types = values();
        String[] codes = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            codes[i] = types[i].code;
        }
        return codes;
    }

    /**
     * 获取所有任务类型描述
     * @return 任务类型描述数组
     */
    public static String[] getAllDescriptions() {
        TaskType[] types = values();
        String[] descriptions = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            descriptions[i] = types[i].description;
        }
        return descriptions;
    }

    /**
     * 处理任务
     * @param task 定时任务实例
     * @param record 待处理的记录
     */
    public abstract void process(ScheduledOaTask task, DynamicObject record);

    @Override
    public String toString() {
        return String.format("TaskType{code='%s', description='%s'}", code, description);
    }
}