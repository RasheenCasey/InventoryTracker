package com.Inventory.productsheet.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Inventory.productsheet.model.InventoryItem;
import com.Inventory.productsheet.model.InventoryOrder;
import com.Inventory.productsheet.service.InventoryService;

@Controller
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	// ---------------- DASHBOARD ----------------
	@GetMapping("/inventory")
	public String inventory(Model model) {

		try {
			List<InventoryItem> items = inventoryService.getAllItems();
			List<InventoryOrder> orders = inventoryService.getAllOrders();

			long totalItems = items != null ? items.size() : 0;

			long lowStockCount = items != null
					? items.stream().filter(i -> i != null && i.getQuantity() <= i.getReorderLevel()).count()
					: 0;

			double totalInventoryValue = items != null
					? items.stream().mapToDouble(i -> i != null ? i.getPrice() * i.getQuantity() : 0).sum()
					: 0;

			double avgUnitCost = totalItems > 0
					? items.stream().mapToDouble(InventoryItem::getPrice).average().orElse(0)
					: 0;

			model.addAttribute("inventoryItems", items);
			model.addAttribute("orders", orders);

			model.addAttribute("totalItems", totalItems);
			model.addAttribute("lowStockCount", lowStockCount);
			model.addAttribute("totalInventoryValue", String.format("%.2f", totalInventoryValue));
			model.addAttribute("avgUnitCost", String.format("%.2f", avgUnitCost));

			return "inventory";
		} catch (Exception e) {
			model.addAttribute("error", "Failed to load inventory: " + e.getMessage());
			return "inventory";
		}
	}

	// ---------------- INVENTORY ----------------

	@PostMapping("/inventory/add")
	public String addItem(@RequestParam String name, @RequestParam int quantity, @RequestParam String unit,
			@RequestParam int reorderLevel, @RequestParam double price, RedirectAttributes redirectAttributes) {

		try {
			InventoryItem item = new InventoryItem();
			item.setName(name.trim());
			item.setQuantity(quantity);
			item.setUnit(unit.trim());
			item.setReorderLevel(reorderLevel);
			item.setPrice(price);

			inventoryService.addItem(item);
			redirectAttributes.addFlashAttribute("success", "Item added successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to add item: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	@PostMapping("/inventory/update")
	public String updateStock(@RequestParam Long id, @RequestParam int quantity,
			RedirectAttributes redirectAttributes) {

		try {
			inventoryService.updateStock(id, quantity);
			redirectAttributes.addFlashAttribute("success", "Stock updated successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to update stock: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	// SOFT DELETE INVENTORY ITEM
	@PostMapping("/inventory/delete/{id}")
	public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {

		try {
			inventoryService.softDeleteItem(id);
			redirectAttributes.addFlashAttribute("success", "Item deleted successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to delete item: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	// EDIT INVENTORY ITEM
	@PostMapping("/inventory/edit")
	public String editItem(@RequestParam Long id, @RequestParam String name, @RequestParam int quantity,
			@RequestParam String unit, @RequestParam int reorderLevel, @RequestParam double price,
			RedirectAttributes redirectAttributes) {

		try {
			InventoryItem item = inventoryService.getAllItems().stream()
					.filter(i -> i.getId().equals(id))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("Item not found"));

			item.setName(name.trim());
			item.setQuantity(quantity);
			item.setUnit(unit.trim());
			item.setReorderLevel(reorderLevel);
			item.setPrice(price);

			inventoryService.updateItem(item);
			redirectAttributes.addFlashAttribute("success", "Item updated successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to update item: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	// ---------------- ORDERS ----------------

	@PostMapping("/inventory/order")
	public String orderItem(@RequestParam Long id, @RequestParam int amount, RedirectAttributes redirectAttributes) {

		try {
			inventoryService.orderFromDistributor(id, amount);
			redirectAttributes.addFlashAttribute("success", "Order placed successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	@PostMapping("/orders/{id}/deliver")
	public String markDelivered(@PathVariable Long id, RedirectAttributes redirectAttributes) {

		try {
			inventoryService.markOrderDelivered(id);
			redirectAttributes.addFlashAttribute("success", "Order marked as delivered");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to mark order as delivered: " + e.getMessage());
		}

		return "redirect:/inventory";
	}

	@PostMapping("/orders/{id}/delete")
	public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {

		try {
			inventoryService.deleteOrder(id);
			redirectAttributes.addFlashAttribute("success", "Order deleted successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Failed to delete order: " + e.getMessage());
		}

		return "redirect:/inventory";
	}
}
