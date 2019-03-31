package my.xzq.xos.server.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BreadCrumbs {

    private String path;
    private String name;


}
