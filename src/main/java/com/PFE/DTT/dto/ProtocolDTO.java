package com.PFE.DTT.dto;

import com.PFE.DTT.model.Protocol;
import com.PFE.DTT.model.ProtocolType;

public class ProtocolDTO {
    private int id;
    private String name;
    private ProtocolType protocolType;

    public ProtocolDTO(Protocol protocol) {
        this.id = protocol.getId();
        this.name = protocol.getName();
        this.protocolType = protocol.getProtocolType();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public ProtocolType getProtocolType() { return protocolType; }
}

