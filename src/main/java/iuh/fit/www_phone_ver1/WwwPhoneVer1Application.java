package iuh.fit.www_phone_ver1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class WwwPhoneVer1Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(WwwPhoneVer1Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WwwPhoneVer1Application.class);
    }

}
