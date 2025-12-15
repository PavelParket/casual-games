package com.bank_service.mapper;

import com.bank_service.domain.dto.ProcessingResult;
import com.bank_service.domain.dto.ProcessingResultResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProcessingResultMapper {

    default ProcessingResultResponse toResponse(ProcessingResult processingResult) {
        return switch (processingResult) {
            case ProcessingResult.Success success ->
                    new ProcessingResultResponse("SUCCESS", "Transactions processed successfully", success.transactions().size());
            case ProcessingResult.Draw draw -> new ProcessingResultResponse("DRAW", draw.reason(), 0);
            case ProcessingResult.Invalid invalid -> new ProcessingResultResponse("INVALID", invalid.reason(), 0);
            case ProcessingResult.AlreadyProcessed alreadyProcessed ->
                    new ProcessingResultResponse("ALREADY_PROCESSED", alreadyProcessed.message(), 0);
        };
    }
}
