package com.example.demo.controller.dto.response;

import com.example.demo.model.entity.Mstmb;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MstmbResponse {
    private Mstmb mstmb;
    private String status;
}
