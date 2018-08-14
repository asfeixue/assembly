package com.feixue.assembly.scheduler;

/**
 * 任务类型
 */
public enum JobType {
    LEAST_ONE(0, "最少一个执行者！"),
    MOST_ONE(1, "最多一个执行者！"),
    ;

    private int code;

    private String desc;

    JobType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
