package com.saidazi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 组队申请处理请求 DTO
 */
@Data
public class TeamApplicationProcessDTO {

    /** 申请记录 ID */
    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    /** 处理结果：1通过 2拒绝 */
    @NotNull(message = "处理结果不能为空")
    private Integer action;
}
