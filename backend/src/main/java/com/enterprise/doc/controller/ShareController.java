package com.enterprise.doc.controller;

import com.enterprise.doc.common.Result;
import com.enterprise.doc.dto.ShareCreateDTO;
import com.enterprise.doc.dto.ShareVerifyDTO;
import com.enterprise.doc.service.ShareService;
import com.enterprise.doc.vo.DocumentVO;
import com.enterprise.doc.vo.ShareVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "分享管理")
@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "创建分享")
    @PostMapping
    public Result<ShareVO> create(@Valid @RequestBody ShareCreateDTO dto) {
        return Result.success(shareService.create(dto));
    }

    @Operation(summary = "取消分享")
    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        shareService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "验证分享链接")
    @PostMapping("/verify")
    public Result<ShareVO> verify(@Valid @RequestBody ShareVerifyDTO dto) {
        return Result.success(shareService.verify(dto));
    }

    @Operation(summary = "获取分享的文档")
    @GetMapping("/document/{shareCode}")
    public Result<DocumentVO> getShareDocument(@PathVariable String shareCode) {
        return Result.success(shareService.getShareDocument(shareCode));
    }
}
