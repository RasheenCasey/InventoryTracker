package com.Inventory.productsheet;

import com.Inventory.productsheet.model.InventoryItem;
import com.Inventory.productsheet.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductsheetApplicationTests {

	@Autowired
	private InventoryService inventoryService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void testAddAndRetrieveItem() {
		// Create a new inventory item
		InventoryItem item = new InventoryItem();
		item.setName("Test Burger Buns");
		item.setQuantity(50);
		item.setUnit("packs");
		item.setReorderLevel(20);
		item.setPrice(3.50);

		// Save the item
		inventoryService.addItem(item);

		// Retrieve all items
		List<InventoryItem> items = inventoryService.getAllItems();

		// Verify the item was added
		assertFalse(items.isEmpty(), "Items list should not be empty");
		assertTrue(items.stream().anyMatch(i -> i.getName().equals("Test Burger Buns")),
			"Should find the added item");
	}

	@Test
	@Transactional
	void testUpdateStock() {
		// Create and add an item
		InventoryItem item = new InventoryItem();
		item.setName("Test Cheese");
		item.setQuantity(30);
		item.setUnit("slices");
		item.setReorderLevel(10);
		item.setPrice(0.25);
		inventoryService.addItem(item);

		// Get the item ID
		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Test Cheese"))
			.findFirst()
			.get()
			.getId();

		// Update stock
		inventoryService.updateStock(itemId, 45);

		// Verify update
		InventoryItem updated = inventoryService.getAllItems().stream()
			.filter(i -> i.getId().equals(itemId))
			.findFirst()
			.get();
		assertEquals(45, updated.getQuantity(), "Quantity should be updated to 45");
	}

	@Test
	@Transactional
	void testLowStockDetection() {
		// Create an item at reorder level
		InventoryItem item = new InventoryItem();
		item.setName("Low Stock Item");
		item.setQuantity(5);
		item.setUnit("kg");
		item.setReorderLevel(10);
		item.setPrice(5.00);
		inventoryService.addItem(item);

		// Check that item is detected as low stock
		List<InventoryItem> allItems = inventoryService.getAllItems();
		long lowStockCount = allItems.stream()
			.filter(i -> i.getQuantity() <= i.getReorderLevel())
			.count();

		assertTrue(lowStockCount > 0, "Should detect at least one low stock item");
	}

	@Autowired
	private com.Inventory.productsheet.repository.InventoryRepository inventoryRepository;

	@Test
	@Transactional
	void testSoftDelete() {
		// Create and add an item
		InventoryItem item = new InventoryItem();
		item.setName("To Delete");
		item.setQuantity(10);
		item.setUnit("boxes");
		item.setReorderLevel(5);
		item.setPrice(2.00);
		inventoryService.addItem(item);

		// Get the item ID
		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("To Delete"))
			.findFirst()
			.get()
			.getId();

		// Soft delete
		inventoryService.softDeleteItem(itemId);

		// Verify item is marked as deleted using repository
		InventoryItem deleted = inventoryRepository.findById(itemId).orElse(null);

		assertNotNull(deleted, "Item should still exist");
		assertTrue(deleted.isDeleted(), "Item should be marked as deleted");

		// Verify item no longer appears in active items list
		List<InventoryItem> activeItems = inventoryService.getAllItems();
		boolean isInActiveList = activeItems.stream()
			.anyMatch(i -> i.getId().equals(itemId));
		assertFalse(isInActiveList, "Deleted item should not appear in active items list");
	}

	@Test
	void testCalculateInventoryValue() {
		// Test with known values
		InventoryItem item1 = new InventoryItem();
		item1.setQuantity(10);
		item1.setPrice(5.00);

		InventoryItem item2 = new InventoryItem();
		item2.setQuantity(20);
		item2.setPrice(2.50);

		double value1 = item1.getPrice() * item1.getQuantity();
		double value2 = item2.getPrice() * item2.getQuantity();

		assertEquals(50.00, value1, "Item 1 value should be 50.00");
		assertEquals(50.00, value2, "Item 2 value should be 50.00");
	}

	@Test
	void testInventoryItemGettersAndSetters() {
		InventoryItem item = new InventoryItem();
		item.setId(1L);
		item.setName("Test Item");
		item.setQuantity(100);
		item.setUnit("lbs");
		item.setReorderLevel(25);
		item.setPrice(4.99);
		item.setDeleted(false);

		assertEquals(1L, item.getId());
		assertEquals("Test Item", item.getName());
		assertEquals(100, item.getQuantity());
		assertEquals("lbs", item.getUnit());
		assertEquals(25, item.getReorderLevel());
		assertEquals(4.99, item.getPrice());
		assertFalse(item.isDeleted());
	}

	// ==================== ORDER TESTS ====================

	@Autowired
	private com.Inventory.productsheet.repository.InventoryOrderRepository orderRepository;

	@Test
	@Transactional
	void testCreateAndRetrieveOrder() {
		// Create item to order
		InventoryItem item = new InventoryItem();
		item.setName("Test Meat");
		item.setQuantity(10);
		item.setUnit("kg");
		item.setReorderLevel(5);
		item.setPrice(8.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Test Meat"))
			.findFirst()
			.get()
			.getId();

		// Place an order
		inventoryService.orderFromDistributor(itemId, 25);

		// Verify order was created
		List<com.Inventory.productsheet.model.InventoryOrder> orders = inventoryService.getAllOrders();
		assertFalse(orders.isEmpty(), "Orders list should not be empty");
		assertTrue(orders.stream().anyMatch(o -> o.getItemName().equals("Test Meat")),
			"Should find the order for Test Meat");
	}

	@Test
	@Transactional
	void testMarkOrderDelivered() {
		// Create item
		InventoryItem item = new InventoryItem();
		item.setName("Deliverable Item");
		item.setQuantity(20);
		item.setUnit("boxes");
		item.setReorderLevel(10);
		item.setPrice(5.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Deliverable Item"))
			.findFirst()
			.get()
			.getId();

		// Place order
		inventoryService.orderFromDistributor(itemId, 30);

		// Get the order ID
		Long orderId = inventoryService.getAllOrders().stream()
			.filter(o -> o.getItemName().equals("Deliverable Item"))
			.findFirst()
			.get()
			.getId();

		int originalQty = inventoryService.getAllItems().stream()
			.filter(i -> i.getId().equals(itemId))
			.findFirst()
			.get()
			.getQuantity();

		// Mark as delivered
		inventoryService.markOrderDelivered(orderId);

		// Verify quantity was updated
		InventoryItem updatedItem = inventoryService.getAllItems().stream()
			.filter(i -> i.getId().equals(itemId))
			.findFirst()
			.get();

		assertEquals(originalQty + 30, updatedItem.getQuantity(),
			"Quantity should increase by order amount");

		// Verify order status
		com.Inventory.productsheet.model.InventoryOrder order = orderRepository.findById(orderId).get();
		assertEquals("DELIVERED", order.getStatus(), "Order should be marked as delivered");
		assertNotNull(order.getDeliveryDate(), "Delivery date should be set");
	}

	// ==================== RESTOCK TESTS ====================

	@Test
	@Transactional
	void testRestockItem() {
		// Create item
		InventoryItem item = new InventoryItem();
		item.setName("Restock Test");
		item.setQuantity(15);
		item.setUnit("bottles");
		item.setReorderLevel(5);
		item.setPrice(2.50);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Restock Test"))
			.findFirst()
			.get()
			.getId();

		// Restock
		inventoryService.restockItem(itemId, 20);

		// Verify
		InventoryItem updated = inventoryService.getAllItems().stream()
			.filter(i -> i.getId().equals(itemId))
			.findFirst()
			.get();

		assertEquals(35, updated.getQuantity(), "Quantity should be 15 + 20 = 35");
	}

	// ==================== VALIDATION TESTS ====================

	@Test
	@Transactional
	void testAddItemWithNullName() {
		InventoryItem item = new InventoryItem();
		item.setName(null);
		item.setQuantity(10);

		assertThrows(IllegalArgumentException.class, () -> {
			inventoryService.addItem(item);
		}, "Should throw exception for null name");
	}

	@Test
	@Transactional
	void testAddItemWithEmptyName() {
		InventoryItem item = new InventoryItem();
		item.setName("   ");
		item.setQuantity(10);

		assertThrows(IllegalArgumentException.class, () -> {
			inventoryService.addItem(item);
		}, "Should throw exception for empty name");
	}

	@Test
	@Transactional
	void testUpdateStockWithNegativeQuantity() {
		InventoryItem item = new InventoryItem();
		item.setName("Negative Test");
		item.setQuantity(10);
		item.setUnit("pcs");
		item.setReorderLevel(5);
		item.setPrice(1.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Negative Test"))
			.findFirst()
			.get()
			.getId();

		assertThrows(IllegalArgumentException.class, () -> {
			inventoryService.updateStock(itemId, -5);
		}, "Should throw exception for negative quantity");
	}

	@Test
	@Transactional
	void testOrderWithZeroAmount() {
		InventoryItem item = new InventoryItem();
		item.setName("Zero Order Test");
		item.setQuantity(10);
		item.setUnit("kg");
		item.setReorderLevel(5);
		item.setPrice(5.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Zero Order Test"))
			.findFirst()
			.get()
			.getId();

		assertThrows(IllegalArgumentException.class, () -> {
			inventoryService.orderFromDistributor(itemId, 0);
		}, "Should throw exception for zero amount");
	}

	// ==================== INVENTORY ORDER MODEL TESTS ====================

	@Test
	void testInventoryOrderGettersAndSetters() {
		com.Inventory.productsheet.model.InventoryOrder order = new com.Inventory.productsheet.model.InventoryOrder();
		order.setId(1L);
		order.setItemId(100L);
		order.setItemName("Test Product");
		order.setQuantity(50);
		order.setStatus("PENDING");
		order.setOrderDate(java.time.LocalDateTime.now());

		assertEquals(1L, order.getId());
		assertEquals(100L, order.getItemId());
		assertEquals("Test Product", order.getItemName());
		assertEquals(50, order.getQuantity());
		assertEquals("PENDING", order.getStatus());
		assertNotNull(order.getOrderDate());
	}

	@Test
	void testInventoryOrderDeliveryDate() {
		com.Inventory.productsheet.model.InventoryOrder order = new com.Inventory.productsheet.model.InventoryOrder();
		java.time.LocalDateTime deliveryTime = java.time.LocalDateTime.now();
		order.setDeliveryDate(deliveryTime);

		assertEquals(deliveryTime, order.getDeliveryDate());
	}

	// ==================== HARD DELETE TESTS ====================

	@Test
	@Transactional
	void testHardDeleteItem() {
		InventoryItem item = new InventoryItem();
		item.setName("Hard Delete Test");
		item.setQuantity(10);
		item.setUnit("pcs");
		item.setReorderLevel(5);
		item.setPrice(3.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Hard Delete Test"))
			.findFirst()
			.get()
			.getId();

		// Hard delete
		inventoryService.deleteItem(itemId);

		// Verify item is completely removed
		assertFalse(inventoryRepository.existsById(itemId),
			"Item should be completely deleted");
	}

	@Test
	@Transactional
	void testHardDeleteItemWithPendingOrder() {
		InventoryItem item = new InventoryItem();
		item.setName("Protected Item");
		item.setQuantity(10);
		item.setUnit("boxes");
		item.setReorderLevel(5);
		item.setPrice(4.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Protected Item"))
			.findFirst()
			.get()
			.getId();

		// Create a pending order
		inventoryService.orderFromDistributor(itemId, 20);

		// Try to hard delete - should fail
		assertThrows(RuntimeException.class, () -> {
			inventoryService.deleteItem(itemId);
		}, "Should not allow hard delete with pending orders");
	}

	// ==================== DELETE ORDER TESTS ====================

	@Test
	@Transactional
	void testDeleteOrder() {
		InventoryItem item = new InventoryItem();
		item.setName("Order Delete Test");
		item.setQuantity(10);
		item.setUnit("kg");
		item.setReorderLevel(5);
		item.setPrice(5.00);
		inventoryService.addItem(item);

		Long itemId = inventoryService.getAllItems().stream()
			.filter(i -> i.getName().equals("Order Delete Test"))
			.findFirst()
			.get()
			.getId();

		inventoryService.orderFromDistributor(itemId, 15);

		Long orderId = inventoryService.getAllOrders().stream()
			.filter(o -> o.getItemName().equals("Order Delete Test"))
			.findFirst()
			.get()
			.getId();

		// Delete the order
		inventoryService.deleteOrder(orderId);

		// Verify order is deleted
		assertFalse(orderRepository.existsById(orderId),
			"Order should be deleted");
	}
}
