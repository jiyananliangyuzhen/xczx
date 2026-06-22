package dsa1.xczxx.wfs.ws.mservice.sk;

import com.alibaba.dubbo.common.utils.StringUtils;
import dsa1.xczxx.wfs.ws.mservice.BankAccountBalanceDetailRepairServicePlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.util.ExceptionUtils;

import java.io.*;

public class ElectronicReceiptService {


    private static Log logger = LogFactory.getLog(ElectronicReceiptService.class);


    public String getElectronicReceipt(DynamicObject receipt) {
        try {

            String uploadFilName = receipt.getString("uploadfilename");
            String realPath = "";
            if (StringUtils.isNotEmpty(uploadFilName)) {
                realPath = uploadFilName;
                if (!uploadFilName.contains(".") && !uploadFilName.contains("/")) {
                    //不是以路径结尾的, 作为查询条件去查实际路径
                    try {
                        FileService attachmentFileService = FileServiceFactory.getAttachmentFileService();
                        realPath = attachmentFileService.getFileServiceExt().getRealPath(uploadFilName);
                        String[] split = realPath.split("/");
                        String fileName = split[split.length - 1];
                        String desPlace = "./".concat(fileName);
                        OutputStream out = downloadFile(desPlace, realPath);
                        File ofdFile = getFile(out, realPath, desPlace);
                    }catch (Exception e) {
                        logger.error("获取文件真实路径失败" + ExceptionUtils.getExceptionStackTraceMessage(e));
                    }
                }
            }
            return realPath;

        } catch (Exception e) {
            logger.error("获取电子回单失败: " + e.getMessage(), e);
        }
        return null;
    }

    public static OutputStream downloadFile(String desPlace, String realUploadUrl) {
        OutputStream out = null;
        try {
            FileService fs = FileServiceFactory.getAttachmentFileService();
            String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
            try {
                out = new FileOutputStream(desPlace);
            } catch (FileNotFoundException e) {
                logger.error("根据目标路径创建文件输出流出错 {}", e.getMessage());
            }
            fs.download(realUploadUrl, out, userAgent);
        } catch (Exception e) {
            logger.error("执行download下载操作出错 {}", e.getMessage());
        }
        return out;
    }

    public static File getFile(OutputStream outStream, String relativeUrl, String targetPdfRoute){
        FileService fileService = FileServiceFactory.getAttachmentFileService();
        try (InputStream inputStream = fileService.getInputStream(relativeUrl)){
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            File targetFile = new File(targetPdfRoute);
            outStream.write(buffer);
            return targetFile;
        }catch (Exception e) {
            logger.error("从容器获取电子回单pdf失败 {}", e.getMessage());
            return null;
        }
    }
}
