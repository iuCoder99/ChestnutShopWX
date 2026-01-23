package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.NoticeDTO;
import com.app.uni_app.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "通知管理")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 获取最新 notice
     *
     * @param limit
     * @return
     */
    @GetMapping("/notice/latest")
    @Operation(summary = "查询最新通知", description = "获取最新通知列表，默认最多返回5条，可通过limit参数指定数量")
    public Result getLatestNotice(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return noticeService.getLatestNotice(limit);
    }

    /**
     * 添加通知
     *
     * @return
     */
    @PostMapping("/admin/notice/add")
    @Operation(summary = "管理员新增通知", description = "管理端，管理员新增系统通知")
    public Result addNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.addNotice(noticeDTO);
    }

    /**
     * 更新通知
     * @param noticeDTO
     * @return
     */
    @PutMapping("/admin/notice/update")
    @Operation(summary = "管理员修改通知", description = "管理端，管理员更新已存在的系统通知")
    public Result updateNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.updateNotice(noticeDTO);
    }

    /**
     * 根据指定 id删除通知
     * @param id
     * @return
     */
    @DeleteMapping("/admin/notice/delete")
    @Operation(summary = "管理员删除通知", description = "管理端，管理员根据通知ID删除指定系统通知")
    public Result deleteNotice(@RequestParam String id){
        return noticeService.deleteNotice(id);
    }

}