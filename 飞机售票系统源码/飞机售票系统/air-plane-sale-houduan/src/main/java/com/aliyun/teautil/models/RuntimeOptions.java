package com.aliyun.teautil.models;

import java.io.Serializable;

/**
 * 兼容性占位类：部分阿里云 SDK 版本在运行时引用此类，
 * 如果项目中未包含 tea-util 包，会导致 ClassNotFoundException。
 * 此类作为最小占位实现，避免运行时类缺失问题。
 *
 * 注意：仅用于开发/联调；生产建议引入官方的 tea-util 依赖或使用正确版本的 SDK。
 */
public class RuntimeOptions implements Serializable {
    private static final long serialVersionUID = 1L;

    // 占位构造器
    public RuntimeOptions() {}
}


