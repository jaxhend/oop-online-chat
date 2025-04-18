package Spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "Spring")
public class NettyAdapterApplication {
    // käivitab Spring Booti rakenduse ja seotud serveri
    public static void main(String[] args) {
        SpringApplication.run(NettyAdapterApplication.class, args);
    }
}