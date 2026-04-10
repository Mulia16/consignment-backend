package com.consignment.service.service;

import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csrn.CsrnCResponse;
import com.consignment.service.model.csrn.CsrnCSearchCriteria;
import com.consignment.service.model.csrn.CsrnResponseDetail;
import com.consignment.service.persistence.mapper.CsrnMapper;
import com.consignment.service.persistence.model.CsrnCHeaderEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CsrnCService {

    private final CsrnMapper csrnMapper;

    public CsrnCService(CsrnMapper csrnMapper) {
        this.csrnMapper = csrnMapper;
    }

    public record PagedResult(List<CsrnCResponse> items, PageMeta meta) {}

    public PagedResult search(CsrnCSearchCriteria criteria) {
        List<CsrnCResponse> items = csrnMapper.searchCHeaders(criteria).stream()
                .map(h -> toCsrnCResponse(h))
                .toList();
        long total = csrnMapper.countCHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsrnCResponse getById(String id) {
        CsrnCHeaderEntity header = csrnMapper.findCHeaderById(id);
        if (header == null) throw new ResourceNotFoundException("CSRN-C not found: " + id);
        return toCsrnCResponse(header);
    }

    private CsrnCResponse toCsrnCResponse(CsrnCHeaderEntity h) {
        List<CsrnResponseDetail> items = csrnMapper.findCDetailsByCHeaderId(h.getId()).stream()
                .map(d -> new CsrnResponseDetail(d.getId(), d.getItemCode(), d.getUom(), d.getQty()))
                .toList();
        return new CsrnCResponse(
                h.getId(), h.getDocNo(), h.getCsrnId(), h.getCsrnDocNo(),
                h.getCsoDocNo(), h.getCompany(), h.getStore(),
                h.getSupplierCode(), h.getSupplierContract(),
                h.getReasonCode(), h.getRemark(), h.getCreatedBy(),
                h.getCreatedAt(), items
        );
    }
}
