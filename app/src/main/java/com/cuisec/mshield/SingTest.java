package com.cuisec.mshield;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import com.cuca.security.PKIUtil;
import com.cuca.security.bean.CertInfo;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.utils.SecurityUtil;
public class SingTest {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void main(String[] args) {
        String indata = Constants.APP_ID + "#" + "4455678";
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        String cert = "MIIDAzCCAeugAwIBAgIOA/5febRcb/PCj3IWyQIwDQYJKoZIhvcNAQELBQAwITELMAkGA1UEBhMCQ04xEjAQBgNVBAMMCUFQUElOU0lERTAeFw0xNzA1MTgwMjEyMDZaFw0yNTEyMzExNjAwMDBaMCExCzAJBgNVBAYTAkNOMRIwEAYDVQQDDAlBUFBJTlNJREUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgTQMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAZU+UGkqGuhE2tTtNZ0KNOuQs1qHJD2aWHhQjx/M2GUEKssIBbyOK1YBml5RiM78lLuU5UTy4UK1oot6afP8qUZ8IaSn6P7mcQs2dZRIBbmbK3jXmgFM5DBvXpTqiOaVPqmzXnV0XPsiehJ9/sKLS5XZNj+yQ4UDYkoJa/HNbSO6W5wVRK5B0m9UbbStLTyjWC8Vqz2yZs7N2zVPoNqWFtFEkYrbED1OhPgZFyJ8LI/vQGt3u/jD2LZb5z9WdKDzAIiBkmY7dSCtnNxg7ROBUMecR/BM6YGOXIrzpOG9PgT+UCsFe6GotFOSfzg8u9xPVeE4zM9pCRD8qiR8AmlAh7A==";
//验证签名
        CertInfo certinfo = null;
        try {
            certinfo = PKIUtil.getCertInfo(cert.getBytes());
            PKIUtil.getCertInfo(cert.getBytes());
            String decode1 = Base64.encodeToString(indata.getBytes(), Base64.DEFAULT);
            boolean ret;
            ret = PKIUtil.verify(SecurityUtil.SIGN_ALGORITHMS, certinfo.getPublicKey(), indata.getBytes(),
                    decode1.getBytes(), "SOFT", 1);
            System.out.println(String.valueOf(ret));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
