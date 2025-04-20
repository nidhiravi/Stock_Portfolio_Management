// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected to WebSocket');
    
    // Subscribe to stock updates
    stompClient.subscribe('/topic/stock-updates', function (message) {
        const stocks = JSON.parse(message.body);
        updateStockPrices(stocks);
    });
});

function updateStockPrices(stocks) {
    // Update available stocks table
    stocks.forEach(stock => {
        const priceElement = document.querySelector(`#stock-price-${stock.symbol}`);
        if (priceElement) {
            const oldPrice = parseFloat(priceElement.textContent);
            const newPrice = stock.currentPrice;
            priceElement.textContent = newPrice.toFixed(2);
            
            // Add visual feedback for price changes
            priceElement.classList.remove('text-success', 'text-danger');
            if (newPrice > oldPrice) {
                priceElement.classList.add('text-success');
            } else if (newPrice < oldPrice) {
                priceElement.classList.add('text-danger');
            }
        }
    });
    
    // Update portfolio stocks
    stocks.forEach(stock => {
        const portfolioStockElements = document.querySelectorAll(`.portfolio-stock[data-symbol="${stock.symbol}"]`);
        portfolioStockElements.forEach(element => {
            const priceElement = element.querySelector('.current-price');
            const totalValueElement = element.querySelector('.total-value');
            const profitLossElement = element.querySelector('.profit-loss');
            
            if (priceElement && totalValueElement && profitLossElement) {
                const oldPrice = parseFloat(priceElement.textContent);
                const newPrice = stock.currentPrice;
                const quantity = parseFloat(element.dataset.quantity);
                const purchasePrice = parseFloat(element.dataset.purchasePrice);
                
                // Update current price
                priceElement.textContent = newPrice.toFixed(2);
                
                // Update total value
                const totalValue = newPrice * quantity;
                totalValueElement.textContent = totalValue.toFixed(2);
                
                // Update profit/loss
                const profitLoss = (newPrice - purchasePrice) * quantity;
                profitLossElement.textContent = profitLoss.toFixed(2);
                
                // Update profit/loss color
                profitLossElement.classList.remove('text-success', 'text-danger');
                if (profitLoss > 0) {
                    profitLossElement.classList.add('text-success');
                } else if (profitLoss < 0) {
                    profitLossElement.classList.add('text-danger');
                }
                
                // Add visual feedback for price changes
                priceElement.classList.remove('text-success', 'text-danger');
                if (newPrice > oldPrice) {
                    priceElement.classList.add('text-success');
                } else if (newPrice < oldPrice) {
                    priceElement.classList.add('text-danger');
                }
            }
        });
    });
}

function formatPrice(price) {
    return '$' + price.toFixed(2);
} 