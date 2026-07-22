package com.cron_starter.controller;

import com.cron_starter.model.CronJobDescriptor;
import com.cron_starter.service.CronRunService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cron_starter.config.Constants.SPRINGDOC_CRON_DESCRIPTION;
import static com.cron_starter.config.Constants.SPRINGDOC_CRON_TAG;

@RestController
@RequestMapping("/admin/cron")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = SPRINGDOC_CRON_TAG, description = SPRINGDOC_CRON_DESCRIPTION)
public class CronController {

    private final CronRunService cronRunService;

    @GetMapping
    public List<CronJobDescriptor> list() {
        return cronRunService.list();
    }

    @PostMapping("/{code}/run")
    public void run(@PathVariable String code) {
        cronRunService.run(code);
    }
}
