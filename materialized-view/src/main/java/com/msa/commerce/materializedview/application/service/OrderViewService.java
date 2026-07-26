package com.msa.commerce.materializedview.application.service;

import org.springframework.stereotype.Service;

import com.msa.commerce.materializedview.application.port.in.UpdateOrderViewUseCase;
import com.msa.commerce.materializedview.application.port.out.OrderViewRepository;
import com.msa.commerce.materializedview.application.port.out.ProcessedEventPort;
import com.msa.commerce.materializedview.domain.OrderCreatedView;
import com.msa.commerce.materializedview.domain.OrderStatusChangedView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderViewService implements UpdateOrderViewUseCase {

    private final ProcessedEventPort processedEventPort;

    private final OrderViewRepository orderViewRepository;

    @Override
    public void applyOrderCreated(OrderCreatedView view) {
        applyOnce(view.eventId(), () -> orderViewRepository.applyOrderCreated(view));
    }

    @Override
    public void applyStatusChanged(OrderStatusChangedView view) {
        applyOnce(view.eventId(), () -> orderViewRepository.applyStatusChanged(view));
    }

    // 뷰 갱신 실패 시 처리 마크를 되돌려 컨슈머 재시도가 유실 없이 동작하게 한다.
    private void applyOnce(String eventId, Runnable action) {
        if (!processedEventPort.markProcessed(eventId)) {
            log.info("Skipping already processed event: eventId={}", eventId);
            return;
        }
        try {
            action.run();
        } catch (RuntimeException e) {
            processedEventPort.unmark(eventId);
            throw e;
        }
    }

}
