package my.xzq.xos.server.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
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

    public static String generateToken(UserDetails userDetails) {
        long nowMillis = System.currentTimeMillis();
        long ttlMillis = nowMillis + MILL_DURATION;
        Date now  = new Date(nowMillis);
        Date ttl = new Date(ttlMillis);

        String salt = generateSalt();
        String subject = userDetails.getUsername();
        String userId = subject;
        signKeyPool.put(subject,salt);

        return Jwts.builder()
                .setSubject(subject)
                .setId(userId)
                .setIssuedAt(now)
                .setExpiration(ttl)
                .signWith(SignatureAlgorithm.HS256,salt)
                .compact();
    }

    private static String generateSalt() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String getJwsOwner(String jws) {
        String body = jws.split("\\.")[1];
        return JSON.parseObject(TextCodec.BASE64URL.decodeToString(body)).getString(SUB);
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
        return signKeyPool.get(subject);
    }

    public static void clearUserSignKey(String principle) {
        signKeyPool.invalidate(principle);
    }
}
