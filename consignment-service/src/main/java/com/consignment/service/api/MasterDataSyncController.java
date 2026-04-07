package com.consignment.service.api;

import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.master.MasterSyncRequest;
import com.consignment.service.model.master.MasterSyncResponse;
import com.consignment.service.service.MasterDataSyncService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acmm/master-sync")
public class MasterDataSyncController {

    private final MasterDataSyncService masterDataSyncService;

    public MasterDataSyncController(MasterDataSyncService masterDataSyncService) {
        this.masterDataSyncService = masterDataSyncService;
    }

    @PostMapping("/{entity}")
    public ResponseEntity<ApiResponse<MasterSyncResponse>> syncEntity(
            @PathVariable("entity") String entity,
            @Valid @RequestBody MasterSyncRequest request) {
        return ResponseEntity.ok(ApiResponse.success(masterDataSyncService.sync(entity, request.records())));
    }
}
