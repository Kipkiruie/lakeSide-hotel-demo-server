package com.dailycodework.lakesidehotel.controller;

import com.dailycodework.lakesidehotel.service.mpesa.MpesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mpesa")
@CrossOrigin(origins = "*")
public class MpesaController {

    @Autowired
    private MpesaService mpesaService;

    @PostMapping("/stkpush")
    public String stkPush(
            @RequestParam String phone,
            @RequestParam String amount
    ) {
        return mpesaService.stkPush(phone, amount);
    }
}