package com.quoccuong.aws.cloudwatch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class CloudWatchMetricConfig {

    private final Region region = Region.AP_SOUTHEAST_2;
    private final CloudWatchClient cw = CloudWatchClient.builder()
            .region(region)
            .build();


    private Double loggedInUsers = 0D;

    private final String appInstance = UUID.randomUUID().toString();
    private final String logGroup = "cloudwatch-demo";
    private final String logStream = MessageFormat.format("appInstance/{0}", appInstance);

    public CloudWatchMetricConfig() {

    }

    @Scheduled(fixedRate = 1000)
    public void sendCloudWatchMetric() {
        try {
            Dimension dimension = Dimension.builder()
                    .name("ENV")
                    .value("PROD")
                    .build();

            // Set an Instant object.
            String time = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            Instant instant = Instant.parse(time);

            MetricDatum datum = MetricDatum.builder()
                    .metricName("LOGGED_IN_USERS")
                    .unit(StandardUnit.NONE)
                    .value(++loggedInUsers)
                    .timestamp(instant)
                    .dimensions(dimension).build();

            PutMetricDataRequest request = PutMetricDataRequest.builder()
                    .namespace("SITE/USERS")
                    .metricData(datum).build();

//            cw.putMetricData(request);


            log.info("Current time {}", Instant.now());

        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }


    }

}
