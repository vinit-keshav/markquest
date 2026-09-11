package com.marketquest.trading.service;
import com.marketquest.trading.dto.TradeRequest;
import com.marketquest.trading.event.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TradingServiceTests {
    @Test void usesServerCurrencyAndPriceAndReportsPending() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradingService service = new TradingService(producer, new InstrumentService(), new LivePriceService(""));
        TradeRequest request = new TradeRequest();
        request.setUserId("alice"); request.setSymbol("AAPL"); request.setSide("BUY");
        request.setQuantity(2); request.setCurrency("INR"); request.setPrice(java.math.BigDecimal.ONE);
        var response = service.executeTrade(request);
        assertEquals("PENDING", response.getStatus());
        ArgumentCaptor<TradeExecutedEvent> event = ArgumentCaptor.forClass(TradeExecutedEvent.class);
        verify(producer).publishTradeExecuted(event.capture());
        assertEquals("USD", event.getValue().getCurrency());
        assertEquals(0, new java.math.BigDecimal("210.00").compareTo(event.getValue().getPrice()));
    }
    @Test void invalidQuantityNeverPublishes() {
        TradeEventProducer producer = mock(TradeEventProducer.class);
        TradingService service = new TradingService(producer, new InstrumentService(), new LivePriceService(""));
        TradeRequest request = new TradeRequest();
        request.setUserId("alice"); request.setSymbol("AAPL"); request.setSide("BUY"); request.setQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> service.executeTrade(request));
        verifyNoInteractions(producer);
    }
}
