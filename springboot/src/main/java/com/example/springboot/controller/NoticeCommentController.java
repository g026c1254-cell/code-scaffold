package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.NoticeComment;
import com.example.springboot.service.INoticeCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/noticeComment")
public class NoticeCommentController {

    @Autowired
    private INoticeCommentService noticeCommentService;

    @AuthAccess
    @GetMapping("/selectByNoticeId/{noticeId}")
    public Result selectByNoticeId(@PathVariable Integer noticeId) {
        return Result.success(noticeCommentService.selectByNoticeId(noticeId));
    }

    @PostMapping("/add")
    public Result add(@RequestBody NoticeComment comment) {
        noticeCommentService.add(comment);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        noticeCommentService.delete(id);
        return Result.success();
    }
}
