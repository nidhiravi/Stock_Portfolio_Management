package com.portfolio.management.controller;

import com.portfolio.management.model.Transaction;
import com.portfolio.management.model.AvailableStock;
import com.portfolio.management.model.Portfolio;
import com.portfolio.management.model.Stock;
import com.portfolio.management.service.PortfolioService;
import com.portfolio.management.service.TransactionService;
import com.portfolio.management.repository.AvailableStockRepository;
import com.portfolio.management.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final PortfolioService portfolioService;
    private final AvailableStockRepository availableStockRepository;
    private final StockRepository stockRepository;

    @Autowired
    public TransactionController(TransactionService transactionService,
            PortfolioService portfolioService,
            AvailableStockRepository availableStockRepository,
            StockRepository stockRepository) {
        this.transactionService = transactionService;
        this.portfolioService = portfolioService;
        this.availableStockRepository = availableStockRepository;
        this.stockRepository = stockRepository;
    }

    @GetMapping
    public String viewTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getTransactionsForCurrentUser());
        return "transaction/list";
    }

    @GetMapping("/buy")
    public String showBuyForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("portfolio", portfolioService.getCurrentUserPortfolio());
        model.addAttribute("availableStocks", availableStockRepository.findAll());
        return "transaction/buy";
    }

    @PostMapping("/buy")
    public String executeBuy(@RequestParam String symbol, @RequestParam String name,
            @RequestParam double quantity, @RequestParam double price, Model model) {
        try {
            // Check if stock is available and has enough shares
            Optional<AvailableStock> availableStockOpt = availableStockRepository.findBySymbol(symbol);

            if (availableStockOpt.isEmpty()) {
                throw new RuntimeException("Stock not available for purchase");
            }

            AvailableStock availableStock = availableStockOpt.get();
            if (availableStock.getTotalShares() < quantity) {
                throw new RuntimeException(
                        "Not enough shares available. Only " + availableStock.getTotalShares() + " shares remaining.");
            }

            // Execute the buy transaction
            transactionService.executeBuyTransaction(symbol, name, quantity, price);

            // Update available shares
            availableStock.setTotalShares(availableStock.getTotalShares() - quantity);
            availableStockRepository.save(availableStock);

            return "redirect:/transactions?success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("portfolio", portfolioService.getCurrentUserPortfolio());
            model.addAttribute("availableStocks", availableStockRepository.findAll());
            return "transaction/buy";
        }
    }

    @GetMapping("/sell")
    public String showSellForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("stocks", portfolioService.getAllStocksForCurrentUser());
        return "transaction/sell";
    }

    @PostMapping("/sell")
    public String executeSell(@RequestParam String symbol, @RequestParam String name,
            @RequestParam double quantity, @RequestParam double price, Model model) {
        try {
            transactionService.executeSellTransaction(symbol, name, quantity, price);
            return "redirect:/transactions?success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stocks", portfolioService.getAllStocksForCurrentUser());
            return "transaction/sell";
        }
    }

    @GetMapping("/api/stocks/{symbol}/price")
    @ResponseBody
    public Map<String, Double> getCurrentPrice(@PathVariable String symbol) {
        Portfolio portfolio = portfolioService.getCurrentUserPortfolio();
        Stock stock = stockRepository.findByPortfolioAndSymbol(portfolio, symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found in your portfolio"));
        Map<String, Double> response = new HashMap<>();
        response.put("currentPrice", stock.getCurrentPrice());
        return response;
    }
}
