package com.lhh.serverbase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshResponse {

    String out;

    String err;

    Integer exitStatus;

    List<String> outList;

    public SshResponse(String out, String err, Integer exitStatus){
        this.out = out;
        this.err = err;
        this.exitStatus = exitStatus;
        this.outList = new ArrayList<>();
    }

}
