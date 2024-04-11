package com.quoccuong.aws.cloudwatch.configuration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.UUID;

public class CloudWatchLogsAppender extends ConsoleAppender<ILoggingEvent> {

    private static final Region region = Region.AP_SOUTHEAST_2;
    private static final String appInstance = UUID.randomUUID().toString();
    private static final String logGroup = "cloudwatch-demo";
    private static final String logStream = MessageFormat.format("appInstance/{0}", appInstance);

    private static final CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder().region(region).build();

    static {
        var logGroups = logsClient.describeLogGroups(DescribeLogGroupsRequest.builder().logGroupNamePrefix("cloudwatch-demo").build());
        logGroups.logGroups().forEach(logGroup -> System.out.println(logGroup.getValueForField("logGroupName", String.class)));
        var logGroupExisted = logGroups.logGroups().stream()
                .anyMatch(it -> it.getValueForField("logGroupName", String.class)
                        .map(name -> name.compareTo("cloudwatch-demo") == 0)
                        .orElse(false));
        if (!logGroupExisted) {
            logsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName("cloudwatch-demo").build());
        }

        logsClient.createLogStream(CreateLogStreamRequest.builder().logGroupName(logGroup).logStreamName(logStream).build());
    }

    public CloudWatchLogsAppender() {
        super();
    }

    @Override
    protected void writeOut(ILoggingEvent event) throws IOException {
        byte[] byteArray = this.encoder.encode(event);
        writeBytes(byteArray);

        var logRequest = PutLogEventsRequest.builder()
                .logGroupName(logGroup)
                .logStreamName(logStream)
                .logEvents(InputLogEvent.builder()
                        .timestamp(event.getTimeStamp())
                        .message(new String(byteArray, StandardCharsets.UTF_8))
                        .build())
                .build();

        logsClient.putLogEvents(logRequest);
    }

}