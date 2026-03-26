package dsa1.xczxx.wfs.ws.common.util;

import com.alibaba.fastjson.JSON;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dsa1.xczxx.wfs.ws.common.util.DataSendHttp.doPost;

/**
 * 获取星瀚系统token
 */
public class GetTokneService {
  private static final Log log = LogFactory.getLog(GetTokneService.class);

    public static String getToken(String accountId,String tokenUrl) {

      Map<String, String> map = new HashMap<>();
      map.put("client_id", "xczx");
      map.put("client_secret", "XCzx@123456789#!");
      map.put("username", "jindiejiyanan");
      map.put("accountId", accountId);
      map.put("nonce", String.valueOf(UUID.randomUUID()));
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String currentTime = LocalDateTime.now().format(dtf);
      map.put("timestamp", currentTime);
      map.put("language", "zh_CN");

    String repose =  doPost(JSON.toJSONString( map), tokenUrl);
    String token = JSON.parseObject(repose).getJSONObject("data").getString("access_token");
    log.info("repose = {}",token);
        return token;
    }



}
