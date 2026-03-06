package com.naturalhub.web.controller.admin;

import com.naturalhub.common.core.domain.AjaxResult;
import com.naturalhub.common.core.page.TableDataInfo;
import com.naturalhub.common.core.controller.BaseController;
import com.naturalhub.system.domain.QaHistory;
import com.naturalhub.system.service.IQaHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin/qa")
public class QaHistoryController extends BaseController {

    @Autowired
    private IQaHistoryService qaHistoryService;

    @GetMapping("/list")
    public TableDataInfo list(QaHistory qaHistory) {
        startPage();
        return getDataTable(qaHistoryService.selectQaHistoryList(qaHistory));
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return AjaxResult.success(qaHistoryService.selectQaHistoryById(id));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(qaHistoryService.deleteQaHistoryByIds(ids));
    }

    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = qaHistoryService.getStatistics();
        return AjaxResult.success(stats);
    }
}
