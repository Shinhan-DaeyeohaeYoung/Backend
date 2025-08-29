package com.joeun.partitioner;

import com.joeun.service.organization.OrganizationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrganzationIdPartitioner implements Partitioner {

    @Value("${batch.partition.bucket-size:1000}")
    private int bucketSize;

    private final OrganizationDomainService organizationDomainService;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Long> ids = organizationDomainService.findAllOrganizationIds();
        Map<String, ExecutionContext> result = new HashMap<>();

        for (int i = 0; i < ids.size(); i += bucketSize) {
            int j = Math.min(i + bucketSize, ids.size());
            long fromId = ids.get(i);
            long toId = ids.get(j - 1);

            ExecutionContext ctx = new ExecutionContext();
            ctx.putLong("fromId", fromId);
            ctx.putLong("toId", toId);

            String key = "p-" + fromId + "-" + toId;
            result.put(key, ctx);
        }
        return result;
    }
}
