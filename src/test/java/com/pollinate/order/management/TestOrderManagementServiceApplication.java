package com.pollinate.order.management;

import org.springframework.boot.SpringApplication;

public class TestOrderManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderManagementServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
