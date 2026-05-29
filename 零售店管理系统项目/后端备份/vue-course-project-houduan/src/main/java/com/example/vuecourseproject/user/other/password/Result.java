package com.example.vuecourseproject.user.other.password;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static Result<?> success(String msg) {
        return new Result<>(200, msg, null);
    }

    public static Result<?> error(String msg) {
        return new Result<>(400, msg, null);
    }
}
