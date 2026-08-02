package com.itau.pix_received_publisher.core.application.dtos;

import java.util.List;

public record PixWebhookBatchRequest(List<PixReceivedEventRequest> pix) {}
