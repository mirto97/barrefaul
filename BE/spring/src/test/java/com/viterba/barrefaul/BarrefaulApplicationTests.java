package com.viterba.barrefaul;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.viterba.barrefaul.support.MySqlContainerConfig;

@Import(MySqlContainerConfig.class)
@SpringBootTest
class BarrefaulApplicationTests {

	@Test
	void contextLoads() {
	}

}
