package my.xzq.xos.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.model.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2019-03-06 23:36
 */
@Slf4j
public class JWTUtil {

    private static LoadingCache<String, String> signKeyPool;
    private static final long MILL_DURATION = 3600 * 1000; //1小时
    private static final String SUB = "sub";

    static {
        signKeyPool = CacheBuilder.newBuilder()
                .maximumSize(Long.MAX_VALUE)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return "";
                    }
                });
    }

    public static String generateToken(User user) {
        long nowMillis = System.currentTimeMillis();
        long ttlMillis = nowMillis + MILL_DURATION;
        Date now  = new Date(nowMillis);
        Date ttl = new Date(ttlMillis);

        String salt = generateSalt();
        String subject = user.getUsername();
        String userId = user.getUserUUID();
        signKeyPool.put(subject,salt);

        return Jwts.builder()
                .setSubject(subject)
                .setId(userId)
                .setIssuedAt(now)
                .setExpiration(ttl)
                .signWith(SignatureAlgorithm.HS256,salt)
                .compact();
    }


    public static String generateDownloadToken(String bucket,String filePath,String salt,Date now,Date ttl) {

        Map<String,Object> claims = new HashMap<>();
        claims.put("bucket",bucket);
        claims.put("filePath",filePath);

        JwtBuilder jwtBuilder = Jwts.builder();
        if(ttl !=null)  jwtBuilder.setExpiration(ttl);

        return jwtBuilder
                .setClaims(claims)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256,salt)
                .compact();

    }

    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String getJwsOwner(String jws) {
        String body = jws.split("\\.")[1];
        return JSON.parseObject(TextCodec.BASE64URL.decodeToString(body)).getString(SUB);
    }


    public static Map<String,String> getDownloadTokenClaims(String jws) {
        Map<String,String> infos = new HashMap<>();

        String body = jws.split("\\.")[1];
        JSONObject jsonObject = JSON.parseObject(TextCodec.BASE64URL.decodeToString(body));

        infos.put(XosConstant.BUCKET,jsonObject.getString(XosConstant.BUCKET));
        infos.put(XosConstant.FILEPATH,jsonObject.getString(XosConstant.FILEPATH));

        return infos;
    }

    public static boolean validateToken(String jws,String userSignKey) {
        try {
            Jwts.parser()
                .setSigningKey(userSignKey)
                .parseClaimsJws(jws);
            return true;
        } catch (Exception e) {
            log.error(">>>> validate json web token error ");
            e.printStackTrace();
            throw new JwtException("validate xos token error");
        }
    }

    public static String getUserSignKey(String subject) throws ExecutionException {
        return signKeyPool.asMap().get(subject);
    }

    public static void clearUserSignKey(String principle) {
        signKeyPool.invalidate(principle);
    }
}
