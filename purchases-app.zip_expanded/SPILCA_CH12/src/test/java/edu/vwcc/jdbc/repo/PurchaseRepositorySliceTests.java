package edu.vwcc.jdbc.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import edu.vwcc.jdbc.model.Purchase;

/**
 * Repository slice tests using Spring Boot's @JdbcTest. This runs against an
 * actual in-memory H2 database with the schema and data from schema.sql and
 * data.sql.
 */
@JdbcTest
@Import(PurchaseRepository.class) // Import the repository to test
@ActiveProfiles("dev") // Use the dev profile with H2 database
public class PurchaseRepositorySliceTests {

	@Autowired
	private PurchaseRepository purchaseRepository;

	@Test
	@DisplayName("findAllPurchases should return all purchases from the database")
	public void testFindAllPurchases() {
		// Execute repository method
		List<Purchase> purchases = purchaseRepository.findAllPurchases();

		// Verify results (data.sql creates 5 records by default)
		assertNotNull(purchases);
		assertEquals(5, purchases.size());
	}

	@Test
	@DisplayName("storePurchase should insert a new purchase")
	public void testStorePurchase() {
		// Create a new purchase
		Purchase purchase = new Purchase();
		purchase.setProduct("Test Product");
		purchase.setPrice(new BigDecimal("99.99"));

		// Execute repository method
		int result = purchaseRepository.storePurchase(purchase);

		// Verify it was inserted
		assertEquals(1, result);

		// Verify we can retrieve it
		List<Purchase> purchases = purchaseRepository.findAllPurchases();
		assertEquals(6, purchases.size()); // Original 5 + our new one
	}

	@Test
	@DisplayName("countAllPurchases should return the correct count")
	public void testCountAllPurchases() {
		int count = purchaseRepository.countAllPurchases();
		assertEquals(5, count);
	}

	@Test
	@DisplayName("getTotalRevenue should return the sum of all purchase prices")
	public void testGetTotalRevenue() {
		BigDecimal total = purchaseRepository.getTotalRevenue();

		// Sum of prices in data.sql: 49.99 + 54.99 + 59.99 + 52.99 + 44.99 = 262.95
		assertEquals(0, new BigDecimal("262.95").compareTo(total));
	}

	@Test
	@DisplayName("getAvgPurchasePrice should return the average price")
	public void testGetAvgPurchasePrice() {
		BigDecimal avg = purchaseRepository.getAvgPurchasePrice();

		// Average of prices: 262.95 / 5 = 52.59
		assertEquals(0, new BigDecimal("52.59").compareTo(avg));
	}

	@Test
	@DisplayName("findPurchaseById should return the purchase when it exists")
	public void testFindPurchaseById() {
		Purchase purchase = purchaseRepository.findPurchaseById(1);

		assertNotNull(purchase);
		assertEquals(1, purchase.getId());
		assertEquals("Spring Boot in Action", purchase.getProduct());
		assertEquals(0, new BigDecimal("49.99").compareTo(purchase.getPrice()));
	}

	@Test
	@DisplayName("findPurchaseById should return null when purchase doesn't exist")
	public void testFindPurchaseByIdNotFound() {
		Purchase purchase = purchaseRepository.findPurchaseById(999);

		assertNull(purchase);
	}

	@Test
	@DisplayName("deletePurchaseById should remove the purchase")
	public void testDeletePurchaseById() {
		// Verify purchase exists
		Purchase purchase = purchaseRepository.findPurchaseById(1);
		assertNotNull(purchase);

		// Delete it
		purchaseRepository.deletePurchaseById(1);

		// Verify it's gone
		purchase = purchaseRepository.findPurchaseById(1);
		assertNull(purchase);

		// Verify count decreased
		int count = purchaseRepository.countAllPurchases();
		assertEquals(4, count);
	}

	@Test
	@DisplayName("updatePurchaseToNoCost should set price to zero")
	public void testUpdatePurchaseToNoCost() {
		// Verify initial price
		Purchase purchase = purchaseRepository.findPurchaseById(1);
		assertNotNull(purchase);
		assertEquals(0, new BigDecimal("49.99").compareTo(purchase.getPrice()));

		// Update price to zero
		purchaseRepository.updatePurchaseToNoCost(1);

		// Verify price is now zero
		purchase = purchaseRepository.findPurchaseById(1);
		assertNotNull(purchase);
		assertEquals(0, new BigDecimal("0.00").compareTo(purchase.getPrice()));
	}
}