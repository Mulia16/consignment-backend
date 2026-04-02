package com.consignment.service.api;

import com.consignment.service.model.master.MasterSyncRequest;
import com.consignment.service.model.master.MasterSyncResponse;
import com.consignment.service.service.MasterDataSyncService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acmm/master-sync")
public class MasterDataSyncController {

    private final MasterDataSyncService masterDataSyncService;

    public MasterDataSyncController(MasterDataSyncService masterDataSyncService) {
        this.masterDataSyncService = masterDataSyncService;
    }

    @PostMapping("/{entity}")
    public MasterSyncResponse syncEntity(
            @PathVariable("entity") String entity,
            @Valid @RequestBody MasterSyncRequest request
    ) {
        return masterDataSyncService.sync(entity, request.records());
    }
}
