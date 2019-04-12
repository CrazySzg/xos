package my.xzq.xos.server.dto.response;

import lombok.Data;
import my.xzq.xos.server.model.XosObjectSummary;

import java.util.List;

/**
 * @author Administrator
 * @create 2019-04-01 14:45
 */
@Data
public class SearchResult {

    private List<XosObjectSummary> data;
}
