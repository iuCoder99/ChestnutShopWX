package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.NoticeDTO;
import com.app.uni_app.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
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
    public Result getLatestNotice(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return noticeService.getLatestNotice(limit);
    }


    /**
     * 添加通知
     *
     * @return
     */
    @PostMapping("/admin/notice/add")
    public Result addNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.addNotice(noticeDTO);
    }


    /**
     * 更新通知
     * @param noticeDTO
     * @return
     */
    @PutMapping("/admin/notice/update")
    public Result updateNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.updateNotice(noticeDTO);
    }


    /**
     * 根据指定 id删除通知
     * @param id
     * @return
     */
    @DeleteMapping("/admin/notice/delete")
    public Result deleteNotice(@RequestParam String id){
        return noticeService.deleteNotice(id);
    }

}
