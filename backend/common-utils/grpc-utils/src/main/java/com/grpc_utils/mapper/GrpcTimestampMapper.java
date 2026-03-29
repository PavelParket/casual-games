package com.grpc_utils.mapper;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class GrpcTimestampMapper {

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Timestamp toTimestamp(LocalDate date) {
        return toTimestamp(date.atStartOfDay());
    }

    public static LocalDate toLocalDate(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }
}
