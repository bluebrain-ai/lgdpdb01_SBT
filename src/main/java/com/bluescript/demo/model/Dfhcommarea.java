package com.bluescript.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import java.util.*;

import org.springframework.stereotype.Component;

@Data
@Component

public class Dfhcommarea {
    private String caRequestId;
    private int caReturnCode;
    private long caCustomerNum;
    private String caRequestSpecific;
    // private CaCustomerRequest caCustomerRequest;
    // private CaCustsecrRequest caCustsecrRequest;
    // private CaPolicyRequest caPolicyRequest;

}