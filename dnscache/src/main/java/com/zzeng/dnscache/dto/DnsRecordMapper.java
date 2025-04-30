package com.zzeng.dnscache.dto;

import com.zzeng.dnscache.model.DnsRecord;

public class DnsRecordMapper {

    public static DnsRecordResponse toResponse(DnsRecord record) {
        DnsRecordResponse response = new DnsRecordResponse(
                record.getDomain(),
                record.getIp(),
                record.getTtl()
        );

        if (record.isManual()) {
            response.setResolvedBy("manual");
        } else {
            response.setResolvedBy("cache");
        }

        return response;
    }

    public static DnsRecord toManualDnsRecord(DnsRecordCreateRequest request) {
        return new DnsRecord(
                request.getDomain(),
                request.getIp(),
                request.getTtl(),
                true
        );
    }
}
