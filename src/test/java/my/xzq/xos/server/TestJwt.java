package my.xzq.xos.server;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import my.xzq.xos.server.utils.JWTUtil;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @create 2019-03-06 19:43
 */
public class TestJwt {

    String salt = "salt";

    @Test
    public void test1() {
        SignatureAlgorithm hs256 = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        long ttlMillis = nowMillis + (3600L * 1000L);
        Date now  = new Date(nowMillis);
        Date ttl = new Date(ttlMillis);
        Map<String,Object> claims = new HashMap<>();
        claims.put("username","testUser");
        String jws  = Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString()) //jti
                .setIssuedAt(now) //iat
                .setExpiration(ttl) //exp
                .setSubject("testUser")//sub,username
                .signWith(hs256, salt)
                .compact();

        System.out.println(jws);
    }


    // A signed JWT is called a 'JWS'.
    @Test
    public void testGetToken() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImV4cCI6MTU1MTkzMDg4NiwiaWF0IjoxNTUxOTI3Mjg2LCJqdGkiOiI0OGFhNWNjZS0yN2U5LTQwZmQtOTQ1MC1iMTEzNDc2OWIwZDciLCJ1c2VybmFtZSI6InRlc3RVc2VyIn0.GIrg8G5ryMJ62wLQk-sAtiSndl9pzrja4F_uS81CIZw";
        String[] strings = token.split("\\.");
        System.out.println(TextCodec.BASE64URL.decodeToString(strings[0]));
        System.out.println(TextCodec.BASE64URL.decodeToString(strings[1]));
        /*Jws<Claims> claimsJws = Jwts.parser()
              //  .setSigningKey("salttt")
                .parseClaimsJws(token);
        System.out.println(claimsJws.getSignature());
        System.out.println(claimsJws.getHeader());
        System.out.println(claimsJws.getBody());*/
     //   System.out.println(claimsJws.getBody().getExpiration().getTime());
    //    System.out.println(claimsJws.getBody().getIssuedAt().getTime());
     //   System.out.println(claimsJws.getBody().getSubject());
    }


    @Test
    public void testGuava() {
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .maximumSize(Long.MAX_VALUE)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return "ll-0---";
                    }
                });
        try {
            cache.put("1","11");
            cache.put("2","22");

            System.out.println(cache.get("1"));
            System.out.println(cache.get("2"));

            cache.invalidate("2");
            System.out.println(cache.get("2"));

            Thread.sleep(2000l);

            System.out.println(cache.get("1"));
            System.out.println(cache.get("5"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAntPath() {
        String reg = "^([A-Za-z0-9_\\-\\.])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,4})$";
        String email = "76443274@qq.com";
        System.out.println(Pattern.compile(reg).matcher(email).matches());
        String phone = "15626164246";
        String error="12";
        System.out.println(Pattern.compile("\\d{11}").matcher(phone).matches());
        System.out.println(Pattern.compile("\\d{11}").matcher(error).matches());
    }

    @Test
    public void test6() {
        System.out.println(JWTUtil.getDownloadTokenClaims("eyJhbGciOiJIUzI1NiJ9.eyJidWNrZXQiOiJkNzRlYzZkZmMwM2E0ZjFiYmQzMjRjZjhlOGRmOGVkYiIsImZpbGVQYXRoIjoiMC1fOGZmMmU4ZDMzZmRhNDQxNWJiZjA1ODFhMTRjNWZkMWMiLCJleHAiOjE1NTM5MjAxMjIsImlhdCI6MTU1MzkxNjUyMn0.RM7ckKHvozixCG8NumRgGlzImUhvsm_rw6nLujd3qgI"));

    }
}
