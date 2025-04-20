package com.portfolio.management.service;

import com.portfolio.management.model.AvailableStock;
import com.portfolio.management.model.Stock;
import com.portfolio.management.repository.AvailableStockRepository;
import com.portfolio.management.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class StockPriceUpdateService {

    private final AvailableStockRepository availableStockRepository;
    private final StockRepository stockRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Random random = new Random();

    @Autowired
    public StockPriceUpdateService(AvailableStockRepository availableStockRepository,
            StockRepository stockRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.availableStockRepository = availableStockRepository;
        this.stockRepository = stockRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 1000) // Run every 1 second
    @Transactional
    public void updateStockPrices() {
        // Update available stocks
        List<AvailableStock> availableStocks = availableStockRepository.findAll();
        for (AvailableStock stock : availableStocks) {
            double currentPrice = stock.getCurrentPrice();
            double change = generatePriceChange(currentPrice);
            double newPrice = Math.max(0.01, Math.min(currentPrice + change, currentPrice + 20)); // Reduced max change
                                                                                                  // to $20
            stock.setCurrentPrice(newPrice);
            availableStockRepository.save(stock);

            // Update all portfolio stocks with the same symbol
            List<Stock> portfolioStocks = stockRepository.findBySymbol(stock.getSymbol());
            for (Stock portfolioStock : portfolioStocks) {
                portfolioStock.setCurrentPrice(newPrice);
                stockRepository.save(portfolioStock);
            }
        }

        // Send updates via WebSocket
        messagingTemplate.convertAndSend("/topic/stock-updates", availableStocks);
    }

    private double generatePriceChange(double currentPrice) {
        // Generate a random change between -1% and +1% of current price
        double maxChange = Math.min(20, currentPrice * 0.01); // Reduced to 1% change
        return random.nextDouble() * (maxChange * 2) - maxChange;
    }
}