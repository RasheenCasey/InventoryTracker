package com.Inventory.productsheet.controller;

import com.Inventory.productsheet.model.InventoryItem;
import com.Inventory.productsheet.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/chat")
    public String aiChat(Model model) {
        model.addAttribute("suggestions", getInventorySuggestions());
        return "ai/chat";
    }

    @PostMapping("/ask")
    @ResponseBody
    public Map<String, String> askAI(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String aiResponse = generateAIResponse(userMessage);

        Map<String, String> response = new HashMap<>();
        response.put("response", aiResponse);
        return response;
    }

    private String generateAIResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        // Get actual inventory data
        List<InventoryItem> allItems = inventoryService.getAllItems();
        List<InventoryItem> lowStockItems = getLowStockItems(allItems);
        double totalValue = calculateTotalValue(allItems);

        if (message.contains("low stock") || message.contains("reorder") || message.contains("running low")) {
            return generateLowStockResponse(lowStockItems);
        }
        else if (message.contains("what") && (message.contains("have") || message.contains("inventory") || message.contains("stock"))) {
            return generateInventorySummary(allItems, lowStockItems, totalValue);
        }
        else if (message.contains("add") && message.contains("item")) {
            return "Use the form at the top of your inventory dashboard. Enter: item name, quantity, unit (e.g., lbs, boxes), reorder level, and unit price for cost tracking.";
        }
        else if (message.contains("order") || message.contains("supplier") || message.contains("buy more")) {
            return generateOrderingResponse(lowStockItems);
        }
        else if (message.contains("cost") || message.contains("price") || message.contains("value") || message.contains("worth")) {
            return generateCostResponse(allItems, totalValue);
        }
        else if (message.contains("recommend") || message.contains("suggest") || message.contains("advice")) {
            return generateRecommendations(lowStockItems, allItems);
        }
        else if (message.contains("help")) {
            return "I can help you:\n1. Track low stock items - ask 'what's low on stock?'\n2. Check inventory value - ask 'what's my inventory worth?'\n3. Get reorder suggestions - ask 'what should I order?'\n4. View full inventory - ask 'what do I have in stock?'\n5. Add new items or place orders";
        }
        else {
            return "I'm your inventory assistant! Try asking:\n• 'What items are low on stock?'\n• 'What's my inventory worth?'\n• 'What should I order?'\n• 'What do I have in stock?'";
        }
    }

    private List<InventoryItem> getLowStockItems(List<InventoryItem> items) {
        return items.stream()
            .filter(item -> item.getQuantity() <= item.getReorderLevel())
            .collect(Collectors.toList());
    }

    private double calculateTotalValue(List<InventoryItem> items) {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }

    private String generateLowStockResponse(List<InventoryItem> lowStockItems) {
        if (lowStockItems.isEmpty()) {
            return "✅ Great news! All items are well-stocked. No items need reordering right now.";
        }

        StringBuilder response = new StringBuilder("⚠️ You have " + lowStockItems.size() + " item(s) that need reordering:\n\n");
        for (InventoryItem item : lowStockItems) {
            int needed = item.getReorderLevel() - item.getQuantity() + 10; // Suggest 10 above reorder level
            response.append(String.format("• %s: %d %s (reorder at %d)\n  Suggested order: %d %s\n\n",
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getReorderLevel(),
                needed,
                item.getUnit()));
        }
        response.append("Click 'Order' next to any item in your inventory table to place an order.");
        return response.toString();
    }

    private String generateInventorySummary(List<InventoryItem> allItems, List<InventoryItem> lowStockItems, double totalValue) {
        if (allItems.isEmpty()) {
            return "Your inventory is empty. Use the form above to add your first item!";
        }

        int totalCount = allItems.size();
        int lowCount = lowStockItems.size();

        StringBuilder response = new StringBuilder();
        response.append(String.format("📦 Inventory Summary:\n\n"));
        response.append(String.format("• Total items: %d\n", totalCount));
        response.append(String.format("• Items well-stocked: %d\n", totalCount - lowCount));
        response.append(String.format("• Items needing reorder: %d\n", lowCount));
        response.append(String.format("• Total inventory value: $%.2f\n\n", totalValue));

        if (lowCount > 0) {
            response.append("⚠️ Priority items to restock:\n");
            lowStockItems.stream().limit(3).forEach(item -> {
                response.append(String.format("• %s (%d %s left)\n", item.getName(), item.getQuantity(), item.getUnit()));
            });
        }

        return response.toString();
    }

    private String generateOrderingResponse(List<InventoryItem> lowStockItems) {
        if (lowStockItems.isEmpty()) {
            return "No items need ordering right now. All inventory levels are healthy!\n\nWhen you do need to order, click the 'Order' button next to any item in your inventory table.";
        }

        StringBuilder response = new StringBuilder("🛒 Here are my reorder recommendations:\n\n");

        for (InventoryItem item : lowStockItems) {
            int suggestedQty = Math.max(item.getReorderLevel() * 2 - item.getQuantity(), 10);
            double orderCost = suggestedQty * item.getPrice();
            response.append(String.format("• %s:\n", item.getName()));
            response.append(String.format("  Current: %d %s\n", item.getQuantity(), item.getUnit()));
            response.append(String.format("  Suggested order: %d %s\n", suggestedQty, item.getUnit()));
            response.append(String.format("  Estimated cost: $%.2f\n\n", orderCost));
        }

        return response.toString();
    }

    private String generateCostResponse(List<InventoryItem> allItems, double totalValue) {
        if (allItems.isEmpty()) {
            return "No items in inventory to calculate costs.";
        }

        double avgCost = allItems.stream()
            .mapToDouble(InventoryItem::getPrice)
            .average()
            .orElse(0.0);

        InventoryItem highestValue = allItems.stream()
            .max((a, b) -> Double.compare(a.getPrice() * a.getQuantity(), b.getPrice() * b.getQuantity()))
            .orElse(null);

        StringBuilder response = new StringBuilder("💰 Cost Analysis:\n\n");
        response.append(String.format("• Total inventory value: $%.2f\n", totalValue));
        response.append(String.format("• Average unit cost: $%.2f\n", avgCost));
        response.append(String.format("• Number of SKUs: %d\n\n", allItems.size()));

        if (highestValue != null) {
            double itemTotal = highestValue.getPrice() * highestValue.getQuantity();
            response.append(String.format("• Highest value item: %s ($%.2f total)\n",
                highestValue.getName(), itemTotal));
        }

        return response.toString();
    }

    private String generateRecommendations(List<InventoryItem> lowStockItems, List<InventoryItem> allItems) {
        StringBuilder response = new StringBuilder("📋 My Recommendations:\n\n");

        if (lowStockItems.isEmpty()) {
            response.append("✅ Inventory is well-managed! No urgent reorders needed.\n\n");
        } else {
            response.append(String.format("⚠️ Priority: Reorder %d item(s) soon:\n", lowStockItems.size()));
            lowStockItems.forEach(item -> {
                response.append(String.format("  • %s (only %d %s left)\n",
                    item.getName(), item.getQuantity(), item.getUnit()));
            });
            response.append("\n");
        }

        // Add general advice based on inventory size
        if (allItems.size() < 5) {
            response.append("💡 Tip: Consider adding more items to diversify your inventory.\n");
        }
        if (lowStockItems.size() > allItems.size() / 2) {
            response.append("💡 Tip: Many items are low - consider placing a bulk order with your supplier.\n");
        }

        response.append("\nAsk me 'what should I order?' for specific quantities and costs.");
        return response.toString();
    }

    private List<String> getInventorySuggestions() {
        return List.of(
            "What items are low on stock?",
            "What's my inventory worth?",
            "What should I order?",
            "What do I have in stock?",
            "Give me recommendations"
        );
    }
}
