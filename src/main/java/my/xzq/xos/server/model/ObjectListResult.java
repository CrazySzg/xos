package my.xzq.xos.server.model;

import lombok.Data;

import java.util.List;


@Data
public class ObjectListResult {

    private int objectCount;

    private List<XosObjectSummary> objectSummaryList;
}
