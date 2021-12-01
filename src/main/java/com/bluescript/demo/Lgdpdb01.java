package com.bluescript.demo;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import io.swagger.annotations.ApiResponses;
import com.bluescript.demo.jpa.IDeletePolicyJpa;
import com.bluescript.demo.model.WsHeader;
import com.bluescript.demo.model.ErrorMsg;
import com.bluescript.demo.model.EmVariable;
import com.bluescript.demo.model.Dfhcommarea;

@Getter
@Setter
@RequiredArgsConstructor
@Log4j2
@Component

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 400, message = "This is a bad request, please follow the API documentation for the proper request format"),
        @io.swagger.annotations.ApiResponse(code = 401, message = "Due to security constraints, your access request cannot be authorized"),
        @io.swagger.annotations.ApiResponse(code = 500, message = "The server/Application is down. Please contact support team.") })

public class Lgdpdb01 {

    @Autowired
    private WsHeader wsHeader;
    @Autowired
    private ErrorMsg errorMsg;
    @Autowired
    private EmVariable emVariable;
    @Autowired
    private Dfhcommarea dfhcommarea;
    private String wsTime;
    private String wsDate;
    private String caData;
    private int wsCaHeaderLen = 0;
    private int db2CustomernumInt;
    private int db2PolicynumInt;
    private int eibcalen;
    private String caErrorMsg;
    @Autowired
    private IDeletePolicyJpa deletePolicyJpa;

    @Value("${api.lgdpvs01.uri}")
    private String lgdpvs01_URI;
    @Value("${api.lgdpvs01.host}")
    private String lgdpvs01_HOST;
    @Value("${api.LGSTSQ.uri}")
    private String LGSTSQ_URI;
    @Value("${api.LGSTSQ.host}")
    private String LGSTSQ_HOST;

    @PostMapping("/lgdpdb01")
    public ResponseEntity<Dfhcommarea> mainline(@RequestBody Dfhcommarea payload) {
        log.debug("Methodmainlinestarted..");
        BeanUtils.copyProperties(payload, dfhcommarea);
        // if (eibcalen != 0) {
        // errorMsg.setEmVariable(" NO COMMAREA RECEIVED");
        // writeErrorMessage();
        // log.error("Error code :", LGCA);
        // throw new RuntimeException("LGCA");

        // }
        log.warn("Dfhcommarea:" + dfhcommarea);
        log.warn("dfhcommarea.getCaRequestId():" + dfhcommarea.getCaRequestId().trim());
        dfhcommarea.setCaRequestId(dfhcommarea.getCaRequestId().trim());
        if (dfhcommarea.getCaRequestId() != "01DEND" & dfhcommarea.getCaRequestId() != "01DHOU"
                & dfhcommarea.getCaRequestId() != "01DCOM" & dfhcommarea.getCaRequestId() != "01DMOT") {
            log.warn("inside if ");
            dfhcommarea.setCaReturnCode(99);
        } else {
            deletePolicyDb2Info();
            try {
                WebClient webclientBuilder = WebClient.create(lgdpvs01_HOST);
                Mono<Dfhcommarea> lgdpvs01Resp = webclientBuilder.post().uri(lgdpvs01_URI)
                        .body(Mono.just(dfhcommarea), Dfhcommarea.class).retrieve().bodyToMono(Dfhcommarea.class);// .timeout(Duration.ofMillis(10_000));
                dfhcommarea = lgdpvs01Resp.block();
            } catch (Exception e) {
                log.error(e);
            }

        }

        /* return */

        log.warn("Method mainline completed..");

        return new ResponseEntity<>(dfhcommarea, HttpStatus.OK);

    }

    public void deletePolicyDb2Info() {
        log.warn("Method deletePolicyDb2Infostarted..");
        emVariable.setEmSqlreq(" DELETE POLICY");
        try {
            deletePolicyJpa.deletePolicyByDb2CustomernumIntAndDb2PolicynumInt(db2CustomernumInt, db2PolicynumInt);

        } catch (Exception e) {
            log.error(e);
            writeErrorMessage();
            /* return */
        }

        log.warn("Method deletePolicyDb2Info completed..");
    }

    public void writeErrorMessage() {
        log.debug("MethodwriteErrorMessagestarted..");
        String wsAbstime = LocalTime.now().toString();
        String wsDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // String wsDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        // //yyyyMMdd
        String wsTime = LocalTime.now().toString();
        errorMsg.setEmDate(wsDate.substring(0, 8));
        errorMsg.setEmTime(wsTime.substring(0, 6));
        WebClient webclientBuilder = WebClient.create(LGSTSQ_HOST);
        try {
            Mono<ErrorMsg> lgstsqResp = webclientBuilder.post().uri(LGSTSQ_URI)
                    .body(Mono.just(errorMsg), ErrorMsg.class).retrieve().bodyToMono(ErrorMsg.class);// .timeout(Duration.ofMillis(10_000));
            errorMsg = lgstsqResp.block();
        } catch (Exception e) {
            log.error(e);
        }
        if (eibcalen > 0) {
            if (eibcalen < 91) {
                try {
                    Mono<ErrorMsg> lgstsqResp = webclientBuilder.post().uri(LGSTSQ_URI)
                            .body(Mono.just(errorMsg), ErrorMsg.class).retrieve().bodyToMono(ErrorMsg.class);// .timeout(Duration.ofMillis(10_000));
                    errorMsg = lgstsqResp.block();
                } catch (Exception e) {
                    log.error(e);
                }

            } else {
                try {
                    Mono<String> lgstsqResp = webclientBuilder.post().uri(LGSTSQ_URI)
                            .body(Mono.just(caErrorMsg), String.class).retrieve().bodyToMono(String.class);// .timeout(Duration.ofMillis(10_000));
                    caErrorMsg = lgstsqResp.block();
                } catch (Exception e) {
                    log.error(e);
                }

            }

        }

        log.debug("Method writeErrorMessage completed..");

    }

    /* End of program */
}