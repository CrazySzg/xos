package my.xzq.xos.server.controller;

import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.request.DelParam;
import my.xzq.xos.server.dto.request.MD5ListParam;
import my.xzq.xos.server.dto.request.MoveParam;
import my.xzq.xos.server.dto.request.UploadParam;
import my.xzq.xos.server.dto.response.BreadCrumbs;
import my.xzq.xos.server.dto.response.SearchResult;
import my.xzq.xos.server.dto.response.UploadResponse;
import my.xzq.xos.server.dto.response.UploadResult;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.model.ObjectListResult;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.model.XosObject;
import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.services.impl.XosUserService;
import my.xzq.xos.server.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("main")
public class XosController {

    @Autowired
    private XosService xosService;

    @Autowired
    private XosUserService userService;

    @PostMapping("/upload")
    @ResponseBody
    public XosSuccessResponse<UploadResult> createObject(@RequestParam("file") MultipartFile file, UploadParam param) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // XosAuthenticationProvider 中设置进去
            String username = (String) authentication.getPrincipal(); // 返回当前用户名
            User xosUserInfo = userService.getUserInfo(username);
            if (xosUserInfo.getCapacity() < xosUserInfo.getUsed() + file.getSize()) {
                throw new XosException(XosConstant.NO_EXTRA_SPACE_LEAVE);
            }
            ByteBuffer content = ByteBuffer.wrap(file.getBytes());

            XosSuccessResponse<UploadResult> response = xosService.create(param.getUploadId(), param.getPartSeq(), xosUserInfo.getUserUUID(), param.getTargetDir(),
                    param.getFileName(), content, param.getFileSize(), param.getCategory());
            content.clear();
            userService.updateUserCapacity(xosUserInfo.getUserUUID(), file.getSize());

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            if(StringUtils.hasText(e.getMessage()))
                throw new XosException(XosConstant.UPLOAD_FAIL,e.getMessage());
            else
                throw new XosException(XosConstant.UPLOAD_FAIL);
        }
    }


    /**
     * 新建和继续上传任务会调用此方法,此方法是确定还有多少块没有上传，有多少已经上传完成
     *
     * @param param 客户端计算得到的md5列表
     * @return
     */
    @PostMapping("/pre-create")
    @ResponseBody
    public XosSuccessResponse<UploadResponse> preCreate(@RequestBody MD5ListParam param) {
        try {
            UploadResponse response = xosService.createUploadTask(param.getUploadId(),
                    param.getFileName(), param.getCheckMd5());
            return XosSuccessResponse.build(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.UPLOAD_FAIL);
        }
    }


    private String getCurrentUsername() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/mkdir")
    @ResponseBody
    public XosSuccessResponse<Void> mkdir(@RequestParam("parent") String parent,
                                          @RequestParam("newDirName") String newDirName) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.putDir(xosUserInfo.getUserUUID(), parent, newDirName);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.MAKE_DIR_FAIL);
        }
    }


    @PostMapping("/delobj")
    @ResponseBody
    public XosSuccessResponse<Void> delobj(@RequestBody List<DelParam> params) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.deleteObject(xosUserInfo.getUserUUID(), params);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            if(StringUtils.hasText(e.getMessage()))
                throw new XosException(XosConstant.DELETE_OPER_FAIL,e.getMessage());
            else  throw new XosException(XosConstant.DELETE_OPER_FAIL);
        }
    }

    @PostMapping("/rename")
    @ResponseBody
    public  XosSuccessResponse<Void> rename(@RequestParam("path") String path,
                                            @RequestParam("oldName") String oldName,
                                            @RequestParam("newName") String newName,
                                            @RequestParam("isDir") boolean isDir) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.rename(xosUserInfo.getUserUUID(),path,oldName,newName,isDir);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.RENAME_FAIL);
        }
    }


    @PostMapping("/move")
    @ResponseBody
    public XosSuccessResponse<Void> move(@RequestBody  MoveParam param) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            xosService.move(xosUserInfo.getUserUUID(),param.getPaths(),param.getTargetDir());
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.MOVE_FAIL);
        }
    }

    /**
     * 获取导航路径
     * @param path 形如0-1-2-
     * @return
     */
    @GetMapping("/getBreadCrumbs")
    @ResponseBody
    public XosSuccessResponse<List<BreadCrumbs>> getBreadCrumbs(@RequestParam("path") String path) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            List<BreadCrumbs> breadCrumbs = xosService.makeBread(xosUserInfo.getUserUUID(), path);
            return XosSuccessResponse.build(breadCrumbs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.BAD_PARAM);
        }
    }

    @GetMapping("/classify/{category}")
    @ResponseBody
    public XosSuccessResponse<ObjectListResult> classify(@PathVariable("category") String category) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            return XosSuccessResponse.build(xosService.classify(xosUserInfo.getUserUUID(), category));
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.UNRECOGNIZE_CATEGORY);
        }
    }

    /**
     *
     * @param path 文件夹id 形如 0-1-2-
     * @return
     */
    @GetMapping("/page")
    @ResponseBody
    public XosSuccessResponse<ObjectListResult> page(@RequestParam("path") String path) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            return XosSuccessResponse.build(xosService.listDir(xosUserInfo.getUserUUID(), path));
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.LIST_DIR_FAIL);
        }
    }


    @GetMapping("/search")
    @ResponseBody
    public XosSuccessResponse<SearchResult> search(@RequestParam("keyword") String keyword) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            return XosSuccessResponse.build(xosService.search(xosUserInfo.getUserUUID(), keyword));
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.SEARCH_FAIL);
        }
    }

    @GetMapping("/preview/{type}/{userId}/{filePath}/{fileName}")
    public void preview(HttpServletResponse response,@PathVariable("type") String type,
                        @PathVariable("filePath") String filePath,@PathVariable("userId") String userId,
                        @PathVariable("fileName") String fileName) {
        XosObject xosObject = null;
        try {
            // 下载文件能正常显示中文
            xosObject = xosService.getObject(userId, filePath);

            response.setContentType(xosService.getMimeType(type));
            response.addHeader("Content-Length",xosObject.getMetaData().getSize() + "");
            response.addHeader("x-frame-options","SAMEORIGIN");
            byte[] bytes = new byte[XosConstant.CHUNK_SIZE];
            ServletOutputStream outputStream = response.getOutputStream();

            while(xosObject.getContent().read(bytes) != -1) {
                outputStream.write(bytes);
            }
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            if(StringUtils.hasText(e.getMessage())) {
                throw new XosException(XosConstant.DOWNLOAD_FAIL,e.getMessage());
            } else {
                throw new XosException(XosConstant.DOWNLOAD_FAIL);
            }
        } finally {
            if(xosObject != null)
                xosObject.close();
        }
    }


    @GetMapping("/download/{downloadToken}/")
    public XosSuccessResponse<Void> download(@PathVariable("downloadToken") String downloadToken,
                                             HttpServletResponse response) {
        XosObject xosObject = null;
        try {
            Map<String, String> tokenClaims = JWTUtil.getDownloadTokenClaims(downloadToken);
            String bucket = tokenClaims.get(XosConstant.BUCKET);
            String filePath = tokenClaims.get(XosConstant.FILEPATH);
            if(xosService.validateDownloadToken(downloadToken, bucket, filePath)) {
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                xosObject = xosService.getObject(bucket, filePath);
                String fileName = xosObject.getMetaData().getFileName();

                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                response.addHeader("Content-Length",xosObject.getMetaData().getSize() + "");
                byte[] bytes = new byte[XosConstant.CHUNK_SIZE];
                ServletOutputStream outputStream = response.getOutputStream();

                while(xosObject.getContent().read(bytes) != -1) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
            }
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            if(StringUtils.hasText(e.getMessage())) {
                throw new XosException(XosConstant.DOWNLOAD_FAIL,e.getMessage());
            } else {
                throw new XosException(XosConstant.DOWNLOAD_FAIL);
            }
        } finally {
           if(xosObject != null)
                xosObject.close();
        }
    }

    /**
     * 生成下载链接
     * @return
     */
    @GetMapping("/pre-download/{filePath}")
    @ResponseBody
    public XosSuccessResponse<String> preDownload(@PathVariable("filePath") String filePath) {
        try {
            User xosUserInfo = userService.getUserInfo(this.getCurrentUsername());
            if(xosUserInfo != null) {
                String token = xosService.preDownload(xosUserInfo.getUserUUID(), filePath,3600 * 1000);

                return XosSuccessResponse.build(token);
            } else {
                throw new XosException(XosConstant.OPERATION_ILLEGAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new XosException(XosConstant.OPERATION_ILLEGAL);
        }
    }
}
