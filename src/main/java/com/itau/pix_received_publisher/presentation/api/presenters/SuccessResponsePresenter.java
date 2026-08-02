package com.itau.pix_received_publisher.presentation.api.presenters;

public record SuccessResponsePresenter(String description) {
    public SuccessResponsePresenter() {
        this("Notificação recebida com sucesso");
    }
}
