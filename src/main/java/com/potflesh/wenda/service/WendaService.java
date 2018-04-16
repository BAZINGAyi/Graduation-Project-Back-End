package com.potflesh.wenda.service;

import org.springframework.stereotype.Service;

/**
 * Created by bazinga on 2017/4/7.
 */

@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hello Message:" + String.valueOf(userId);
    }
}
