package my.xzq.xos.server.controller;

import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.request.MD5ListParam;
import my.xzq.xos.server.dto.request.UploadParam;
import my.xzq.xos.server.dto.response.UploadResult;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.dto.response.PreCreateResult;
import my.xzq.xos.server.model.ObjectListResult;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.model.XosObject;
import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.services.impl.XosUserService;
import my.xzq.xos.server.utils.UploadUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

@RestController
@RequestMapping("main")
public class XosController {

    @Autowired
    private XosService xosService;

    @Autowired
    private XosUserService userService;

    @PostMapping("/create-object")
    public XosSuccessResponse<UploadResult> createObject(@RequestParam("file") MultipartFile file, UploadParam param) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // XosAuthenticationProvider 中设置进去
            String username = (String) authentication.getPrincipal(); // 返回当前用户名
            User userInfo = userService.getUserInfo(username);
            if (userInfo.getCapacity() < userInfo.getUsed() + file.getSize()) {
                throw new XosException(XosConstant.NO_EXTRA_SPACE_LEAVE);
            }
            ByteBuffer content = ByteBuffer.wrap(file.getBytes());

            XosSuccessResponse<UploadResult> response = xosService.create(param.getUploadId(), param.getPartSeq(), userInfo.getUserUUID(), param.getTargetDir(),
                    param.getFileName(), content, param.getFileSize(), param.getCategory());
            content.clear();
            userService.updateUserCapacity(userInfo.getUserUUID(), file.getSize());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return XosSuccessResponse.build(new UploadResult(""));
        }
    }


    /**
     * 新建和继续上传任务会调用此方法,此方法是确定还有多少块没有上传，有多少已经上传完成
     *
     * @param param 客户端计算得到的md5列表
     * @return
     */
    @PostMapping("/preCreate")
    public XosSuccessResponse<PreCreateResult> preCreate(@RequestBody MD5ListParam param) {
        try {
            String uploadId = xosService.createUploadTask(param.getFileName(), param.getCheckMd5());
            return XosSuccessResponse.build(new PreCreateResult(uploadId));
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.UPLOAD_FAIL);
        }
    }


    private String getCurrentUsername() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/mkdir")
    public XosSuccessResponse<Void> mkdir(@RequestParam("parent") String parent,
                                          @RequestParam("newDirName") String newDirName) {
        try {
            User userInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.putDir(userInfo.getUserUUID(), parent, newDirName);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.MAKE_DIR_FAIL);
        }
    }


    @DeleteMapping("/deldir")
    public XosSuccessResponse<Void> deldir(@RequestParam("dirName") String dirName) {
        try {
            User userInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.deleteDir(userInfo.getUserUUID(), dirName);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_OPER_FAIL);
        }
    }

    @DeleteMapping("/delobj")
    public XosSuccessResponse<Void> delobj(@RequestParam("parentDir") String parentDir,
                                           @RequestParam("objName") String objName) {
        try {
            User userInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.deleteObject(userInfo.getUserUUID(), parentDir, objName);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DELETE_OPER_FAIL);
        }
    }

    @GetMapping("/list")
    public XosSuccessResponse<ObjectListResult> list(@RequestParam("dir") String dir) {
        try {
            User userInfo = userService.getUserInfo(this.getCurrentUsername());
            return XosSuccessResponse.build(xosService.listDir(userInfo.getUserUUID(), dir));
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.LIST_DIR_FAIL);
        }
    }

    @GetMapping("/download")
    public XosSuccessResponse<Void> download(@RequestParam("dir") String dir, @RequestParam("fileName") String fileName,
                                             HttpServletResponse response) {
        XosObject xosObject = null;
        try {

            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            User userInfo = userService.getUserInfo(this.getCurrentUsername());
            xosObject = xosService.getObject(userInfo.getUserUUID(), dir, fileName);
            byte[] bytes = new byte[XosConstant.CHUNK_SIZE];
            ServletOutputStream outputStream = response.getOutputStream();

            while(xosObject.getContent().read(bytes) != -1) {
                outputStream.write(bytes);
            }
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.DOWNLOAD_FAIL);
        } finally {
            xosObject.close();
        }
    }
}
