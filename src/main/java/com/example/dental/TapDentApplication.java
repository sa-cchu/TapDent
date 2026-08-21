package com.example.dental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class TapDentApplication {

	@PostConstruct
	public void init() {
		// JVMのタイムゾーンを日本時間に固定（DB保存時のUTCへの勝手な変換ズレを防ぐ）
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TapDentApplication.class, args);
	}

}
