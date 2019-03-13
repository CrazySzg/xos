package my.xzq.xos.server.controller;

import my.xzq.xos.server.services.XOSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XosController {

    @Autowired
    private XOSService xosService;
}
