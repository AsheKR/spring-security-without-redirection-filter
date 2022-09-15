package com.example.demo.config.security;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Token {
    private String token;
}
